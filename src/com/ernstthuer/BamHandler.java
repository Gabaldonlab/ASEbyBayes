package com.ernstthuer;

/**
 * Created by ethur on 7/26/16.
 */

import htsjdk.samtools.*;
import htsjdk.samtools.util.*;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.SamFileValidator;
//import htsjdk.samtools.util.CloserUtil;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class BamHandler extends FileHandler {


    // bam is only import, no direction,
    private String locale;
    private String type ;
    // each BAM file contains a potential snplist, this should be merged after reading.  keep separate for threading.
    private ArrayList<SNP> snpArrayList = new ArrayList<>();

    // contain the reads   SAM format

    /**
     *
     * Tricky,  no mpileup function available.
     * Samreader has to compensate
     *
     *
     *
     *
     *
     *
     *
     * @param locale
     * @param type
     * @param direction
     */


    public BamHandler(String locale, String type, String direction) {
        super(locale, type, direction);

        this.type = type;
        this.locale = locale;


    }


    public void readBam(HashMap fastaMap, ArrayList<Gene> geneList) {

        /**
         * reads a Bam file, stores SNPs.  check if there are gene names used as reference, or chromosome names.
         * associate SNPs to genes.  SNP by gene will be the main SNP storage.  SNPs are temporarily stored in the BAMreader, to ease thread ability
         *
         *
         */
        //HashMap<String,Read> ReadMap = new HashMap<>();
        try {
            final SamFileValidator validator = new SamFileValidator(new PrintWriter(System.out), 8000);
            validator.setIgnoreWarnings(true);
            validator.setVerbose(true, 1000);
            validator.setErrorsToIgnore(Collections.singletonList(SAMValidationError.Type.MISSING_READ_GROUP));
            SamReaderFactory factory = SamReaderFactory.makeDefault().validationStringency(ValidationStringency.STRICT);
            SamReader fileBam = factory.open(new File(this.locale));
            //SAMRecordIterator iterator = fileBam.iterator();
            //SamLocusIterator iterator = new SamLocusIterator(fileBam);
            SAMRecordIterator iterator = fileBam.iterator();


            //System.out.println(iterator.toList().size());
            int count = 0;
            while (iterator.hasNext()) {

                // sort to the genes then parse for SNPs

                SAMRecord read =  iterator.next();
                // associate to gene and store barebone there
                int start = read.getAlignmentStart();
                String chromosome = read.getReferenceName();

                



                //System.out.println(locus.toString());
                //System.out.println();
            }
        }
            catch(Exception e){
                System.out.println(e);
            }

            }


    @Override
    public String getLocale() {
        return locale;
    }

    @Override
    public String getType() {
        return type;
    }

    public ArrayList<SNP> getSnpArrayList() {
        return snpArrayList;
    }
}
