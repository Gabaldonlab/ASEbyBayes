package com.ernstthuer;

/**
 * Created by ethur on 7/26/16.
 */

import htsjdk.samtools.*;
import htsjdk.samtools.util.CloserUtil;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.SamFileValidator;
//import htsjdk.samtools.util.CloserUtil;
import htsjdk.samtools.util.Locus;
import htsjdk.samtools.util.LocusImpl;
import htsjdk.samtools.util.SamLocusIterator;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class BamHandler extends FileHandler {


    // bam is only import, no direction,
    private String locale;
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
            SamLocusIterator iterator = new SamLocusIterator(fileBam);

            //System.out.println(iterator.toList().size());
            int count = 0;
            while (iterator.hasNext()) {
                Object locus = iterator.next();
                System.out.println(locus.toString());
                System.out.println();
            }
        }
            catch(Exception e){
                System.out.println(e);
            }

            }














}
