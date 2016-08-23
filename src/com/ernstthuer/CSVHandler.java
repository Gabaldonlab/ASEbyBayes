package com.ernstthuer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.ernstthuer.Main.geneList;


// vcf format specification
// CHROM POS ID REF ALT QUAL FILTER INFO FORMAT

/**
 * Created by ethur on 7/26/16.
 */
public class CSVHandler extends FileHandler {

    /**
     * implement tsv writer for the standard output in vcf format
     *
     * @param locale
     * @param type
     * @param direction
     */

    private String locale;
    private String direction;
    private String type;
    private int qualityThreshold = 30;


    public CSVHandler(String locale, String type, String direction) {
        super(locale, type, direction);
        this.locale = locale;
        this.direction = direction;
        this.type = type;

    }

    public CSVHandler(String locale, String type, String direction, String feature) {
        super(locale, type, direction, feature);
    }

    public ArrayList<SNP> readVCF(){

        ArrayList<SNP> snpArrayList = new ArrayList<>();

        int rowCount = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(locale))) {

            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                String [] aCurrentLine = sCurrentLine.split("\t");

                String Chromosome = aCurrentLine[0];
                int position = Integer.parseInt(aCurrentLine[1]);
                int quality = Integer.parseInt(aCurrentLine[5]);
                char ORG = aCurrentLine[3].charAt(0);


                char ALT = aCurrentLine[4].charAt(0);


                // taking access from the main to ArrayList geneList;
                for(Gene gene:geneList){
                    if(gene.getChromosome() == Chromosome &&  gene.getStart()< position && gene.getStop() > position && quality >= qualityThreshold ){
                        SNP snp = new SNP(gene,ORG,ALT,position);
                        /////  REIMPLEMENT THIS gene.addSNP(snp);
                    }
                }
                //CHROM POS ID REF ALT QUAL FILTER INFO FORMAT
            }
        } catch (IOException e) {
            e.printStackTrace();
        }



        return snpArrayList;
    }


    public boolean writeSNPToVCF(ArrayList<SNP> snpArrayList, int validationLvL) {
        List<String> lines = new ArrayList<String>();
        int writecount = 0;
        for (SNP snp : snpArrayList) {
            if (snp.isValidated() >= validationLvL) {
                // create String
                lines.add(snp.toString());
                writecount++;
            }
        }

        Path outFile = Paths.get(locale);
        //if(Files.isWritable(outFile)) {
        try {
            System.out.println("[STATUS] Writing vcf like output file with " + writecount + " SNPs");
            Files.write(outFile, lines, Charset.forName("UTF-8"));
        } catch (IOException e) {
            System.out.println("No output file created" + e);
        }
        //}
        //System.out.println(writecount + " SNPs written to file ");
        return false;
    }
}
