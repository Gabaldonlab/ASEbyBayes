package com.ernstthuer;

import java.util.ArrayList;

public class Main {

    //ArrayList<Chromosome> chromosomeArrayList = new ArrayList<>();
    ArrayList<Gene> geneList = new ArrayList<Gene>();
    ArrayList<SNP> snips = new ArrayList<>();

    public static void main(String[] args) {

        // open the input files in sequence,  fasta  gff then bam

        /**
         * THe pipeline goes as follows:
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



    }
}
