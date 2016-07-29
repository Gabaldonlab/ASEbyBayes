package com.ernstthuer;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ethur on 7/26/16.
 */
public class CSVHandler extends FileHandler {

    /**
     * implement tsv writer for the standard output in vcf format
     *
     *
     * @param locale
     * @param type
     * @param direction
     */

    private String locale;
    private String direction;
    private String type;


    public CSVHandler(String locale, String type, String direction) {
        super(locale, type, direction);
        this.locale = locale;
        this.direction = direction;
        this.type = type ;

    }

    public CSVHandler(String locale, String type, String direction, String feature) {
        super(locale, type, direction, feature);
    }


    public boolean writeSNPToVCF(ArrayList<SNP> snpArrayList, int validationLvL){
        List<String> lines = new ArrayList<String>();
        int writecount = 0;
        for(SNP snp:snpArrayList){
            if(snp.isValidated() >= validationLvL){
                // create String
                lines.add(snp.toString());
                writecount ++;
            }
        }

        Path outFile = Paths.get(locale);
        //if(Files.isWritable(outFile)) {
            try {
                System.out.println("Writing");
                Files.write(outFile, lines, Charset.forName("UTF-8"));
            }catch(IOException e){
                System.out.println("No output file created" + e);
            }
        //}

        System.out.println(writecount + " SNPs written to file ");
        return false;
    }


    //List<String> lines = Arrays.asList("The first line", "The second line");
    //Path file = Paths.get(this.getLocale());

    /*



    }
    */



    //Files.write(file, lines, Charset.forName("UTF-8"));
//Files.write(file, lines, Charset.forName("UTF-8"), StandardOpenOption.APPEND);



}
