package com.ernstthuer;

import org.biojava.nbio.core.sequence.DNASequence;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;

//import org.slf4j.LoggerFactory;

public class Main {


    //ArrayList<Chromosome> chromosomeArrayList = new ArrayList<>();
    // gene List should be static
    static ArrayList<Gene> geneList = new ArrayList<>();
    static ArrayList<SNP> snips = new ArrayList<>();
    static HashMap<String, DNASequence> fasta = new HashMap<>();
    static boolean verbose = true;
    static double bimodalPrimersForNoise = 0.5;
    static int minCovThreshold = 100;


    public static void main(String[] args) {



        System.out.println("Version  0.1");

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
        //GFF import

        for (FileHandler file : parser.fileList) {
            try {
                if (file.getType() == "GFF" && file.getDirection() == "Input"){
                    //if(file.getType() == "GFF" && file.getDirection() == "Input"){
                    System.out.println("[STATUS]  parsing GFF file");
                try {
                    geneList = ((GFFHandler) file).getGeneList();
                } catch (ClassCastException e) {
                    errorCaller(e);
                }}
            }catch(ClassCastException expected){
                errorCaller(expected);
            }
        }

        // individual loadings
        for (FileHandler file : parser.fileList) {
            if (file.getType() == "FASTA" && file.getDirection() == "Input") {
                System.out.println("Fasta file loading");
                try {
                    fasta = (((FastaHandler) file).readFasta(geneList));
                    //fasta2gene
                    System.out.println("Read fasta");
                } catch (IOException e) {
                    System.out.println(e.getCause());
                    fasta = null;
                }
            }
        }

        for (FileHandler file : parser.fileList) {
            if (file.getType() == "Bam" && file.getDirection() == "Input") {
                System.out.println(" BAM file " +file.getLocale());
                try {

                    if (fasta != null) {
                        BamHandler bhdlr = new BamHandler(file.getLocale(), "Bam", "Input");

                        // to loosen this for threading i should create copies of the genelists
                        bhdlr.readBam(fasta,geneList);
                        //bhdlr.findSNPs();
                    }

                } catch (Exception e) {
                    System.out.println(e.getCause());
                    fasta = null;
                }
            }

        }


        for(Gene gene:geneList){
            System.out.println("Gene :" +gene.getIdent());
            System.out.println("with " + gene.getGeneReadList().size() + " reads");
            System.out.println("SNP" + gene.getSnpsOnGene().size());

        }




        // here comes the unification of the genes


        int poscount = 0;
        int unvalidatedCount = 0;
        int totcount = 0;


        // temporary SNPlist

        int falsecount = 0;

        for(Gene gene:geneList){

            gene.findORGCoverageOfSNPs();

            //System.out.println(gene.getIdent() + "  :  " +   gene.getGeneReadList().size());
            for(SNP snp: gene.getSnpsOnGene()){
                //System.out.println("SNP found on gene : " + snp.getPosition()+ " with coverage " + snp.getALTcov()+ " and total " + snp.getORGcov());
                snips.add(snp);
                if(snp.isValidated() == 1) {

                    // validate for noise
                    if (snp.validateSNP(bimodalPrimersForNoise)) {
                        if(snp.getORGcov() > minCovThreshold) {
                            snp.raiseValidation();
                        }
                        else{
                            snips.remove(snp);
                        }

                    }else{
                        //System.out.println(snp.getALTcov() + "  : " + snp.getORGcov() + "  was removed");
                        snips.remove(snp);
                    }
                    ;

                    if (snp.isValidated() > 1) {
                        poscount++;
                        totcount++;
                        //snp.findTrueORG();
                        //System.out.println(snp.getPosition() + "  " + snp.getALTcov() + "   " + snp.getORGcov());
                        if(snp.getORG() == snp.getALT()){
                            System.out.println(snp.getORG() + "  _  " + snp.getALT());
                            falsecount ++;
                            System.out.println(falsecount);
                        }
                        //snp.addCoverageToSNPs(gene.getGeneReadList());
                    }
                    //System.out.println(snp.getALTcov() + " alt : org  " + snp.getORGcov());
                    else {
                        totcount++;
                        unvalidatedCount ++;
                    }
                }
            }
        }


        System.out.println("A total of " + totcount + " SNPs was found,  of which  " + poscount + " Could be validated");

        for (FileHandler file : parser.fileList) {
            if (file instanceof CSVHandler && file.getDirection() == "Output" ) {
                System.out.println("[STATUS] Writing vcf like output to file to " + file.getLocale());
                try {
                    ((CSVHandler) file).writeSNPToVCF(snips,1);
                } catch (Exception e) {
                    errorCaller(e);
                }
            }
        }
    }


    public static void errorCaller(Exception e ){
        if(verbose) {
            System.out.println(e);
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            sw.toString();
            System.out.println(sw);
        }
    }
}
