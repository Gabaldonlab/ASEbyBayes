package com.ernstthuer;

import org.biojava.nbio.core.sequence.DNASequence;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;


public class Main {

    // gene List should be static
    static ArrayList<Gene> geneList = new ArrayList<>();
    static ArrayList<SNP> snips = new ArrayList<>();
    static HashMap<String, DNASequence> fasta = new HashMap<>();
    static boolean verbose = true;
    static HashMap<String,String> codonConversion = new HashMap<>();


    // Core Hypothesis with preset alpha beta values
    public static Hypothesis hypothesisZero = new Hypothesis(0.1,10,"NullExpHyp");
    public static Hypothesis hypothesisEAX = new Hypothesis(5,5,"EqualAllelicExpression");
    public static Hypothesis hypothesisFullSNP = new Hypothesis(10,0.1,"FullSNPHyp");


    // bimodial primers for noise correction
    static double bimodalPrimersForNoise = 0.5;

    // strong informative primers for the assumption of 0.5 ratio expression
    static double strongCentralInformativePrimers = 25;
    static int minCovThreshold = 50;

    // 0= initiation state ; 1 = repeated observation  ; 2 = significant true positive expectation
    static int validationLVL = 2;


    public static void main(String[] args) {


        System.out.println("Running ASEbyBayes Version  0.22");
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

        // Try block to open obligatory Input files,  GFF import   Fasta reference Input ,  casts as GFFHandler and FastaHandler objects
        try {
            GFFHandler gffHandler = (GFFHandler) parser.returnType("GFF", "Input");

            System.out.println("[STATUS] loading Fasta file");
            FastaHandler fastaHandler = (FastaHandler) parser.returnType("FASTA","Input");
            geneList = gffHandler.getGeneList();
            fasta = fastaHandler.readFasta(geneList);
            //for(Gene gene:geneList){
            //    System.out.println(gene.getIdent() + " " +gene.getStart() + "  " + gene.getStop());
            //}
        }
        catch (ClassCastException e){
            errorCaller(e);
        }catch (IOException IOex){
            System.out.println("File not Found exception at GFF reader input");
            errorCaller(IOex);

        }



        // Block to read information from the BAM files,   depends on whether a vcf input file was given ( SNPs are known) and simple quantification has to
        // be carried out,  or no VCF is provided, and SNP calling is carried out.
        if (parser.isExistingSNPinfo()) {
            // vcf was provided,  overrides SNP calling functionality
            CSVHandler csvHandler = (CSVHandler) parser.returnType("CSV", "INPUT");
            csvHandler.readVCF(); // this primes the genes with full SNP lists


        }


        /**

        for (FileHandler file : parser.fileList) {
            if (file.getType() == "Bam" && file.getDirection() == "Input") {

                //make them implement Runnable   ... later
                System.out.println("[STATUS] Loading BAM file " + file.getLocale());


                try {
                    if (fasta != null) {
                        BamHandler bhdlr = new BamHandler(file.getLocale(), "Bam", "Input");
                        // to loosen this for threading should create copies of the genelists
                        bhdlr.readBam(fasta, geneList,parser.isExistingSNPinfo());

                    }

                } catch (Exception e) {
                    errorCaller(e);
                    //fasta = null;
                }
            }


         */

        ArrayList<BamHandler> bamList = new ArrayList<>();
        ArrayList<List<Gene>> listOfGeneLists = new ArrayList<>();
        ArrayList<Thread> threads = new ArrayList<>();


        ArrayList<BamHandler> listOfBamFilesFromParserForBamThreader = new ArrayList<>();

        for (FileHandler file : parser.fileList) {
            if (file.getType().equals("Bam") && file.getDirection().equals("Input")) {
                //make them implement Runnable   ... later
                System.out.println("[STATUS] Loading BAM file " + file.getLocale());

                listOfBamFilesFromParserForBamThreader.add(new BamHandler(file.getLocale(), "Bam", "Input"));




                //Cloner cloner=new Cloner();
                /*
                try {
                    if (fasta != null) {
                        BamHandler bhdlr = new BamHandler(file.getLocale(), "Bam", "Input");
                        // to loosen this for threading should create copies of the genelists
                        //bamList.add(bhdlr);

                        // deep clone this list
                        ArrayList<Gene> individualGeneList = new ArrayList<Gene>(geneList.size());
                        for(Gene gene:geneList) {
                            // cloning the genes.
                            Object clonedGene = gene.clone();
                            individualGeneList.add((Gene) clonedGene);
                        }

                        bhdlr.setGeneList(individualGeneList);

                        Thread thread = new Thread(bhdlr);
                        thread.start();
                        threads.add(thread);
                        bamList.add(bhdlr);


                        //bhdlr.readBam(fasta, parser.isExistingSNPinfo());
                    }
                } catch (Exception e) {
                    errorCaller(e);
                    //fasta = null;
                }*/
            }
        }
        BamThreader bamThreader = new BamThreader(listOfBamFilesFromParserForBamThreader,geneList);




        // wait for thread completion

        /**  ToDo move everything related to the threads to the BamThreader ...

        for(Thread thread:threads){
            try {
                System.out.println(thread.getName() + " thread joined ");
                thread.join();
            }catch (InterruptedException e){
                errorCaller(e);
            }
        }

        // unify te obtained gene lists
        for(BamHandler bhdlr:bamList){
            ArrayList<Gene> individualGeneList = bhdlr.getGeneList();
            unifyGeneLists(individualGeneList);
        }

         /*

        /*
        for(List<Gene> geneList : listOfGeneLists) {
            System.out.println("found genes in geneLists " + geneList.size());
            for(Gene gene:geneList){
                System.out.println("Gene with " + gene.snpsOnGene.size());

            }
        }

*/
        /*
        for (Gene gene : geneList) {
            System.out.println("Gene :" + gene.getIdent());
            System.out.println("with " + gene.getGeneReadList().size() + " reads");
            System.out.println("SNP" + gene.getSnpsOnGene().size());

        }

*/
        // here comes the unification of the genes




        // This Block gathers information on the SNPs,  validates them according to their expression and conservation over replicates
        // ToDo improve threading output by combining n geneLists // SNPlists

        for (Gene gene : geneList) {



            // this is not working yet, check SNP full coverage vs ALT cov.
            try {

                //gene.getGeneReadList().stream( s -> System.out.println(gene.getStart() + "  :  " + gene.getStop() + "  ::: "+  ));

                gene.findORGCoverageOfSNPs();  //Validate IF THIS WORKS RIGHT
            } catch (NullPointerException e) {
                errorCaller(e);

            }
            /*for(Integer key : gene.getPositioncount().keySet()){
                for(SNP snp:gene.getSnpsOnGene()) {

                    if(snp.getPosition() == key) {
                        System.out.println("SNP : cov " + gene.getPositioncount().get(key) + " SNP orgCov : " + snp.getORGcov() + " SNP. altCov " + snp.getALTcov() );
                    }
                }


            }*/



        }






        // ToDO Hypothesis wise classification
        for(Gene gene:geneList){
            try {
                gene.evaluateSNPs();
            }catch (ConcurrentModificationException e){
                errorCaller(e);
            }

 /*
            for (SNP snp : gene.getSnpsOnGene()) {
                if (snp.isValidated() == 1) {




                    // validate for noise  set mode to 0 for validation
                   if (snp.validateSNP(bimodalPrimersForNoise, bimodalPrimersForNoise, 0)) {
                        if (snp.getORGcov() > minCovThreshold) {
                            snp.raiseValidation();


                        } else {
                            snips.remove(snp);
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
                }*/

            gene.findSynonymity(validationLVL);
        }


        for (Gene gene : geneList) {
            gene.evaluateGeneExpression();
            for(SNP snp: gene.getSnpsOnGene()){
                if(snp.isValidated()>=2) {
                    snips.add(snp);
                }
            }
        }

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
         //*///DISABLED FOR TESTING

    private static void errorCaller(Exception e) {
        if (verbose) {
            //System.out.println(e);
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            //sw.toString();
            System.out.println(sw);
        }
    }

    private static void unifyGeneLists(ArrayList<Gene> individualListOfGenes){

        for(Gene gene : geneList) {
            // find gene in original genelist,
            int index = individualListOfGenes.lastIndexOf(gene);
            if(gene.getSnpsOnGene() == null){
                gene.setSnpsOnGene(individualListOfGenes.get(index).getSnpsOnGene());
            }else{
                gene.addSnpInformation(individualListOfGenes.get(index).getSnpsOnGene());
            }
            }
        }




    private static void  populateCodonConversion(){
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
