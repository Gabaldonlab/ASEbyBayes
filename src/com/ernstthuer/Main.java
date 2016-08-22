package com.ernstthuer;

import org.biojava.nbio.core.sequence.DNASequence;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;


public class Main {

    // gene List should be static
    static ArrayList<Gene> geneList = new ArrayList<>();
    static ArrayList<SNP> snips = new ArrayList<>();
    static HashMap<String, DNASequence> fasta = new HashMap<>();
    static boolean verbose = true;
    static HashMap<String,String> codonConversion = new HashMap<>();

    // bimodial primers for noise correction
    static double bimodalPrimersForNoise = 0.5;

    // strong informative primers for the assumption of 0.5 ratio expression
    static double strongCentralInformativePrimers = 25;
    static int minCovThreshold = 50;

    // 0= initiation state ; 1 = repeated observation  ; 2 = significant true positive expectation
    static int validationLVL = 2;


    public static void main(String[] args) {


        System.out.println("Running ASEbyBayes Version  0.2");
        populateCodonConversion();

        // open the input files in sequence,  fasta  gff then bam

        /**
         * The pipeline flows as follows:
         *
         * loading Files,  obligatory are fasta and bam files.
         * load genes to list
         *
         * checking fasta reference against the bam files , store coverage of SNPs
         * add the SNPs to genes
         *
         *THe flow of data:
         * import of fasta and gff
         * import of bam
         * storage of simplereads(mz score + location) in genes
         * parsing gene by gene for SNPs,  storing only occurances where more than 2 mappings were found.
         * SNPs are validated from SNP class functions,
         *
         *
         */

        ArgParser parser = new ArgParser(args);
        // call the argument parser
        // GFF import   Fasta reference Input

        // obligatory input
        try {
            GFFHandler gffHandler = (GFFHandler) parser.returnType("GFF", "Input");

            System.out.println("[STATUS] loading Fasta file");
            FastaHandler fastaHandler = (FastaHandler) parser.returnType("FASTA","Input");
            geneList = gffHandler.getGeneList();
            fasta = fastaHandler.readFasta(geneList);

            for(Gene gene:geneList){
                System.out.println(gene.getIdent() + " " +gene.getStart() + "  " + gene.getStop());
            }


        }
        catch (ClassCastException e){
            errorCaller(e);
        }catch (IOException IOex){
            errorCaller(IOex);
        }


        //parser.returnType("FASTA","Input");

        /** Primed for removal

        for (FileHandler file : parser.fileList) {
            try {
                if (file.getType() == "GFF" && file.getDirection() == "Input") {
                    //if(file.getType() == "GFF" && file.getDirection() == "Input"){
                    //System.out.println("[STATUS] Parsing GFF file");
                    try {
                        geneList = ((GFFHandler) file).getGeneList();
                    } catch (ClassCastException e) {
                        errorCaller(e);
                    }
                }
            } catch (ClassCastException expected) {
                errorCaller(expected);
            }
        }

        // individual loadings
        for (FileHandler file : parser.fileList) {
            if (file.getType() == "FASTA" && file.getDirection() == "Input") {
                System.out.println("[STATUS] loading Fasta file");
                try {
                    fasta = (((FastaHandler) file).readFasta(geneList));
                    //fasta2gene
                    //System.out.println("Read fasta");
                } catch (IOException e) {
                    System.out.println(e.getCause());
                    fasta = null;
                }
            }
        }
*/


        for (FileHandler file : parser.fileList) {
            if (file.getType() == "Bam" && file.getDirection() == "Input") {

                //make them implement Runnable   ... later

                System.out.println("[STATUS] Loading BAM file " + file.getLocale());
                try {

                    if (fasta != null) {
                        BamHandler bhdlr = new BamHandler(file.getLocale(), "Bam", "Input");

                        // to loosen this for threading i should create copies of the genelists
                        bhdlr.readBam(fasta, geneList);
                        //bhdlr.findSNPs();
                    }

                } catch (Exception e) {
                    errorCaller(e);
                    //fasta = null;
                }
            }

        }

/**  for testing purposes
        for (Gene gene : geneList) {
            System.out.println("Gene :" + gene.getIdent());
            System.out.println("with " + gene.getGeneReadList().size() + " reads");
            System.out.println("SNP" + gene.getSnpsOnGene().size());

        }
*/
        // here comes the unification of the genes

        for (Gene gene : geneList) {

            // this is not working yet, check SNP full coverage vs ALT cov.
            gene.findORGCoverageOfSNPs();  //Validate IF THIS WORKS RIGHT

            //System.out.println(gene.getIdent() + "  :  " +   gene.getGeneReadList().size());
            for (SNP snp : gene.getSnpsOnGene()) {
                //System.out.println("SNP found on gene : " + snp.getPosition()+ " with coverage " + snp.getALTcov()+ " and total " + snp.getORGcov());

                if (snp.isValidated() == 1) {
                    // validate for noise  set mode to 0 for validation
                    if (snp.validateSNP(bimodalPrimersForNoise, bimodalPrimersForNoise, 0)) {
                        if (snp.getORGcov() > minCovThreshold) {
                            snp.raiseValidation();
                        } else {
                            //snips.remove(snp);
                        }

                    } else {
                        //System.out.println(snp.getALTcov() + "  : " + snp.getORGcov() + "  was removed");
                        //snips.remove(snp);
                    }

                    if (snp.isValidated() > 1) {
                        if (snp.getORG() == snp.getALT()) {
                            System.out.println("[WARNING] malformed Read encountered in" +  snp.getGene().getIdent());
                        }
                        //snp.addCoverageToSNPs(gene.getGeneReadList());
                    }
                }
            }
            // asks for validationLimit
            gene.findSynonymity(validationLVL);
        }

        for (Gene gene : geneList) {
            gene.evaluateGeneExpression();
            for(SNP snp: gene.getSnpsOnGene()){
                snips.add(snp);
            }
        }

        //System.out.println("[STATUS] A total of " + totcount + " SNPs were found,  of which  " + poscount + " Could be validated");
        // output processing

        try {
            CSVHandler csvHandler = (CSVHandler) parser.returnType("VCF", "Output");
            FastaHandler fastaHandler = (FastaHandler) parser.returnType("FASTA","Output");

            int minThresh = 2;

            csvHandler.writeSNPToVCF(snips,minThresh);

            // reimplemented the output into a fastasilencer class
            System.out.println("[STATUS] Writing Silenced fasta sequence of SNPs to file : " + fastaHandler.getLocale());
            FastaSilencer fastaSilencer = new FastaSilencer(snips,fasta,fastaHandler.getLocale());
        }
        catch (ClassCastException e){
            errorCaller(e);
        }
    }



        /**  Primed for removal
        for (FileHandler file : parser.fileList) {
            if (file instanceof CSVHandler && file.getDirection() == "Output") {
                //System.out.println("[STATUS] Writing vcf like output to file to " + file.getLocale());
                try {
                    ((CSVHandler) file).writeSNPToVCF(snips, 1);
                } catch (Exception e) {
                    errorCaller(e);
                }
            }
        }
         */
/**
        // optional silenced Fasta writeout
        if(parser.isMaskFasta()){
            for(FileHandler file : parser.fileList){
                if(file.getType()=="FASTA" && file.getDirection() == "Output"){

                    FastaSilencer fastaSilencer = new FastaSilencer(snips,fasta,file.getLocale());
                }
            }
        }
    }
 */
    public static void errorCaller(Exception e) {
        if (verbose) {
            System.out.println(e);
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            sw.toString();
            System.out.println(sw);
        }
    }

    public static void  populateCodonConversion(){
        codonConversion.put("TCT","Ser");
        codonConversion.put("TAT","Tyr");
        codonConversion.put("TGT","Cys");
        codonConversion.put("TTT","Phe");
        codonConversion.put("TTC","Phe");
        codonConversion.put("TCC","Ser");
        codonConversion.put("TAC","Tyr");
        codonConversion.put("TGC","Cys");
        codonConversion.put("TTA","Leu");
        codonConversion.put("TCA","Ser");
        codonConversion.put("TAA","TER");
        codonConversion.put("TGA","TER");
        codonConversion.put("TTG","Leu");
        codonConversion.put("TCG","Ser");
        codonConversion.put("TAG","TER");
        codonConversion.put("TGG","Trp");
        codonConversion.put("CTT","Leu");
        codonConversion.put("CCT","Pro");
        codonConversion.put("CAT","His");
        codonConversion.put("CGT","Arg");
        codonConversion.put("CTC","Leu");
        codonConversion.put("CCC","Pro");
        codonConversion.put("CAC","His");
        codonConversion.put("CGC","Arg");
        codonConversion.put("CTA","Leu");
        codonConversion.put("CCA","Pro");
        codonConversion.put("CAA","Gln");
        codonConversion.put("CGA","Arg");
        codonConversion.put("CTG","Leu");
        codonConversion.put("CCG","Pro");
        codonConversion.put("CAG","Gln");
        codonConversion.put("CGG","Arg");
        codonConversion.put("ATT","Ile");
        codonConversion.put("ACT","Thr");
        codonConversion.put("AAT","Asn");
        codonConversion.put("AGT","Ser");
        codonConversion.put("ATC","Ile");
        codonConversion.put("ACC","Thr");
        codonConversion.put("AAC","Asn");
        codonConversion.put("AGC","Ser");
        codonConversion.put("ATA","Ile");
        codonConversion.put("ACA","Thr");
        codonConversion.put("AAA","Lys");
        codonConversion.put("AGA","Arg");
        codonConversion.put("ATG","Met");
        codonConversion.put("ACG","Thr");
        codonConversion.put("AAG","Lys");
        codonConversion.put("AGG","Arg");
        codonConversion.put("GTT","Val");
        codonConversion.put("GCT","Ala");
        codonConversion.put("GAT","Asp");
        codonConversion.put("GGT","Gly");
        codonConversion.put("GTC","Val");
        codonConversion.put("GCC","Ala");
        codonConversion.put("GAC","Asp");
        codonConversion.put("GGC","Gly");
        codonConversion.put("GTA","Val");
        codonConversion.put("GCA","Ala");
        codonConversion.put("GAA","Glu");
        codonConversion.put("GGA","Gly");
        codonConversion.put("GTG","Val");
        codonConversion.put("GCG","Ala");
        codonConversion.put("GAG","Glu");
        codonConversion.put("GGG","Gly");

    }


}
