package com.ernstthuer;

import java.io.IOException;
import java.util.ArrayList;

public class Main {

    //ArrayList<Chromosome> chromosomeArrayList = new ArrayList<>();
    // gene List should be static
    static ArrayList<Gene> geneList = new ArrayList<>();
    ArrayList<SNP> snips = new ArrayList<>();

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
            if(file instanceof GFFHandler && file.getDirection() == "Input")
            //if(file.getType() == "GFF" && file.getDirection() == "Input"){
                System.out.println("[STATUS]  parsing GFF file");
                try {
                    geneList = ((GFFHandler)file).getGeneList();
                }catch(Exception e){
                    System.out.println(e);
                }
            }

            if (file.getType() == "FASTA" && file.getDirection() == "Input") {
                try {
                    fasta = file.readFasta(geneList);

                    //fasta2gene




                    System.out.println("Read fasta");
                } catch (IOException e) {
                    System.out.println(e.getCause());
                    fasta = null;
                }
            }
        }



    }
}
