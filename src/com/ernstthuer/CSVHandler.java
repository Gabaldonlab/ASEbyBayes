package com.ernstthuer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.ernstthuer.Main.geneList;


// vcf format specification
// CHROM POS ID REF ALT QUAL FILTER INFO FORMAT

/**
 * Created by ethur on 7/26/16.
 */
class CSVHandler extends FileHandler {

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


    CSVHandler(String locale, String type, String direction) {
        super(locale, type, direction);
        this.locale = locale;
        this.direction = direction;
        this.type = type;

    }

    public CSVHandler(String locale, String type, String direction, String feature) {
        super(locale, type, direction, feature);
    }

    ArrayList<SNP> readVCF() {

        ArrayList<SNP> snpArrayList = new ArrayList<>();

        int rowCount = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(locale))) {

            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                String[] aCurrentLine = sCurrentLine.split("\t");

                String Chromosome = aCurrentLine[0];
                int position = Integer.parseInt(aCurrentLine[1]);
                int quality = Integer.parseInt(aCurrentLine[5]);
                char ORG = aCurrentLine[3].charAt(0);


                char ALT = aCurrentLine[4].charAt(0);


                // taking access from the main to ArrayList geneList;
                for (Gene gene : geneList) {
                    if (gene.getChromosome() == Chromosome && gene.getStart() < position && gene.getStop() > position && quality >= qualityThreshold) {
                        SNP snp = new SNP(gene, ORG, ALT, position);
                        gene.addSNP(snp,true);  // ToDO  follow up on the true setting of SNPs for exosting information, this has to override any attempt on removal
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


    boolean writeSNPToVCF(ArrayList<SNP> snpArrayList, int bamFiles) {
        List<String> lines = new ArrayList<String>();
        int writecount = 0;
        for (SNP snp : snpArrayList) {

            if(bamFiles == 1){
                // hard theshold without replicates
                if (snp.getORGcov() > 8 && snp.getALTcov() > 8) {
                    // create String
                    lines.add(snp.toString());
                    writecount++;
                }
            }


            else{
            if (snp.getORGcov() > 2 && snp.getALTcov() > 2) {
                    // create String
                    lines.add(snp.toString());
                    writecount++;
                }
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


    boolean writeGenesToTsv(ArrayList<Gene> geneArrayList){

        List<String> lines = new ArrayList<String>();
        int writecount = 0;
        for (Gene gene: geneArrayList) {
            //if (snp.isValidated() >= validationLvL) {
            // create String
            lines.add(gene.toString());
            writecount++;
            //}
        }

        Path outFile = Paths.get(locale);
        //if(Files.isWritable(outFile)) {
        try {
            System.out.println("[STATUS] Writing gene wise output file with " + writecount + " Genes ");
            Files.write(outFile, lines, Charset.forName("UTF-8"));
        } catch (IOException e) {
            System.out.println("No output file created" + e);
        }
        //}
        //System.out.println(writecount + " SNPs written to file ");
        return false;

    }

    boolean writeResultsToTsv(ArrayList<ResultHypothesis> resultHypothesises){
        List<String> lines = new ArrayList<>();
        int writecount = 0;

        for (ResultHypothesis resultHypothesis: resultHypothesises){

            lines.add(resultHypothesis.toString());
            writecount++;
        }

        return true;
    }


}
