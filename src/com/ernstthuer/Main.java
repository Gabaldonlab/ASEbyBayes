package com.ernstthuer;

import org.biojava.nbio.core.sequence.DNASequence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    //ArrayList<Chromosome> chromosomeArrayList = new ArrayList<>();
    // gene List should be static
    static ArrayList<Gene> geneList = new ArrayList<>();
    ArrayList<SNP> snips = new ArrayList<>();
    static HashMap<String, DNASequence> fasta = new HashMap<>();

    public static void main(String[] args) {

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
         *
         *
         */

        ArgParser parser = new ArgParser(args);
        // call the argument parser
        //GFF import

        for (FileHandler file : parser.fileList) {
            if (file instanceof GFFHandler && file.getDirection() == "Input")
                //if(file.getType() == "GFF" && file.getDirection() == "Input"){
                System.out.println("[STATUS]  parsing GFF file");
            try {
                geneList = ((GFFHandler) file).getGeneList();
            } catch (Exception e) {
                System.out.println(e);
            }
        }

        // individual loadings
        for (FileHandler file : parser.fileList) {
            if (file instanceof FastaHandler && file.getDirection() == "Input") {
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
            if (file instanceof BamHandler && file.getDirection() == "Input") {
                try {
                    //fasta = (((FastaHandler) file).readFasta(geneList));
                    //fasta2gene
                    //System.out.println("Read fasta");

                    if (fasta != null) {
                        BamHandler bhdlr = new BamHandler(file.getLocale(), "Bam", "Input");
                        bhdlr.readBam(fasta,geneList);
                    }

                } catch (Exception e) {
                    System.out.println(e.getCause());
                    fasta = null;
                }
            }
        }



    }
}
