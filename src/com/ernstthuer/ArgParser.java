package com.ernstthuer;

import java.util.ArrayList;
import java.util.List;
/**
 * Created by ethur on 7/26/16.
 */


import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;


class ArgParser {
    private static ArgumentParser parser = ArgumentParsers.newArgumentParser("Checksum").defaultHelp(true).description("ACEcalc");
    static List<FileHandler> fileList = new ArrayList<>();
    private boolean maskFasta;
    private boolean existingSNPinfo;

    ArgParser(String[] args) {

        parser.addArgument("-f", "--fasta")
                .help("input file in FASTA format").required(true).dest("inFasta");
        parser.addArgument("-g", "--gff")
                .help("input file in GFF3 format").required(true).dest("inGFF");
        parser.addArgument("-v", "--vcf")
                .help("input a vcf file containing existing Allele specific SNP information").required(false).setDefault("NOVCF").dest("VCFIN");
        parser.addArgument("-o", "--outfile")
                .help("output file in tsv format").required(false).setDefault("output.csv").dest("outFinal");
        parser.addArgument("-OF", "--outfasta")
                .help("output silenced fasta file,  all SNPs replaced by N ").required(false).setDefault("outfasta.fasta").dest("outFasta");
        parser.addArgument("-F", "--feature")
                .choices("exon", "gene", "cds").setDefault("exon").dest("feature").help("choose feature to analyze, either exon, gene or cds");
        parser.addArgument("-m", "--mask")
                .choices("True", "False").setDefault(true).dest("mask").help("create an intermediate masked FASTA");
        parser.addArgument("-mo", "--maskFastaOutput")
                .dest("mOut").setDefault("").help("write an intermediate masked FASTA to file");
        parser.addArgument("-b", "--bamInput")
                .required(true).dest("bamInput").nargs("+");

        Namespace ns = null;

        try {
            ns = parser.parseArgs(args);

            //
            this.maskFasta = ns.getBoolean("mask");


        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }


        if (ns.get("VCFIN") != "NOVCF") {
            existingSNPinfo = true;
        } else {
            existingSNPinfo = false;
        }

        for (Object element : ns.getList("bamInput")) {
            try {
                FileHandler bamFile = new BamHandler(element.toString(), "Bam", "Input");
                fileList.add(bamFile);
            } catch (NullPointerException e) {
                System.out.println(e);
            }
        }


        //System.out.println(ns.get("inGFF").toString());

        // String locale, String type, String feature, String direction))
        GFFHandler gffreader = new GFFHandler(ns.get("inGFF").toString(), "GFF", "Input", ns.get("feature").toString());
        FastaHandler inFasta = new FastaHandler(ns.get("inFasta").toString(), "FASTA", "Input");
        FastaHandler outFasta = new FastaHandler(ns.get("mOut").toString(), "FASTA", "Output");
        CSVHandler finalOut = new CSVHandler(ns.get("outFinal").toString(), "VCF", "Output");

        if (existingSNPinfo) {
            CSVHandler vcfInput = new CSVHandler(ns.get("VCFIN").toString(), "VCF", "INPUT");
            fileList.add(vcfInput);
        }


        fileList.add(gffreader);
        fileList.add(inFasta);
        fileList.add(outFasta);
        fileList.add(finalOut);
    }

    boolean isExistingSNPinfo() {
        return existingSNPinfo;
    }

    FileHandler returnType(String type, String direction) {
        // type is self explanatory,
        // direction is whether input or output file  fasta and vcf could be either

        for (FileHandler fileHandler : fileList) {
            if (fileHandler.getType().equals(type) && fileHandler.getDirection().equals(direction)) {
                return fileHandler;
            }
        }
        return null;
    }
}


