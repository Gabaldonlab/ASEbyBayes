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
    ArrayList<SNP> snips = new ArrayList<>();
    static HashMap<String, DNASequence> fasta = new HashMap<>();

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
                    System.out.println(e);
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    sw.toString();
                    System.out.println(sw);

                }}
            }catch(ClassCastException expected){
                System.out.println(expected);
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                expected.printStackTrace(pw);
                sw.toString();
                System.out.println(sw);
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
