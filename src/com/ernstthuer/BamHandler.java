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
import java.util.*;

public class BamHandler extends FileHandler {


    // bam is only import, no direction,
    public static int lengthOfReads = 0;
    private String locale;
    private String type ;
    // each BAM file contains a potential snplist, this should be merged after reading.  keep separate for threading.
    private ArrayList<SNP> snpArrayList = new ArrayList<>();
    private ArrayList<Gene> geneList = new ArrayList<>();
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
            // a default gene
            //Gene currentGene = new Gene("default", 1 , 2, "bare");
            Gene currentGene = null;

            while (iterator.hasNext()) {



                // sort to the genes then parse for SNPs

                SAMRecord read =  iterator.next();

                if(this.lengthOfReads == 0) {
                    this.lengthOfReads = (read.getReadLength());
                }


                // associate to gene and store barebone there
                int start = read.getAlignmentStart();
                String chromosome = read.getReferenceName();


                // MZ scores the mapping in form of a simple sequence
                String MZ = read.getSAMString().split("\t")[11].split(":")[2];
                SimpleRead splR = new SimpleRead(start,MZ);


                try {
                    currentGene = findGene(chromosome, start, geneList, currentGene);
                    if(currentGene != null) {
                        currentGene.addRead(splR);
                    }

                }catch(NullPointerException e){
                    System.out.println(e);
                }

                count ++;


                //System.out.println(locus.toString());
                //System.out.println();
            }
        }
            catch(Exception e){
                System.out.println(e);
            }

        this.geneList = geneList;

            }


    public void findSNPs(){
        for(Gene gene:geneList){

            /*
            gene.getGeneReadList().sort(SimpleRead);

            Collections.sort(Database, new Comparator<SimpleRead>() {
                @Override public int compare(SimpleRead read1 , SimpleRead read2) {
                    return read1.getStart() - read2.getStart(); // Ascending
                }
                */

            List<SimpleRead> splRds = gene.getGeneReadList();
            //List<SNP> snpOnGene = gene.getSnpsOnGene();
            for(SimpleRead splRd:splRds ){
                //System.out.println(splRd.getMZ());
                // split between letters and digits or digits and letters
                String[] MZArray = splRd.getMZ().split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");

                // digest the MZ score
                try {
                    for (int i = 0; i < MZArray.length; i++) {
                        if (MZArray[i].matches("\\D")) {
                            int position = Integer.parseInt(MZArray[i - 1]) + splRd.getStart();
                            //System.out.println(position + "  " + MZArray[i]);
                            // this should feed into a new SNP
                            SNP snp = new SNP(gene,MZArray[i].charAt(0), position);
                            gene.addSNP(snp);
                            // the full coverage can be gotten from a locusiterator after SNP calling
                        }
                    }
                }
                catch(Exception e ){
                    //System.out.println("Caught badly formatted MZ string");
                    //System.out.println(e);
                }
            }
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

    public Gene findGene(String chromosome, int start, ArrayList<Gene> geneList , Gene currentgene ){

        // first check if read belongs to the same gene as before:  if not find the next one


        if ( currentgene!= null && currentgene.getChromosome() == chromosome && currentgene.getStop() > start && currentgene.getStart() < start) {
            return currentgene;
        }


        for (Gene gene:geneList){
            if(gene.getChromosome().equals(chromosome)){
                if(gene.getStart() < start && gene.getStop() > start){
                    return gene;
                }
            }
        }
        return null;
    }

}
