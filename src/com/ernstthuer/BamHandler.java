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
import java.io.StringWriter;
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
        this.geneList = geneList;

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
            SAMRecordIterator iterator = fileBam.iterator();



            //System.out.println(iterator.toList().size());
            int count = 0;
            int goodCount = 0;
            // a default gene
            //Gene currentGene = new Gene("default", 1 , 2, "bare");
            Gene currentGene = null;

            while (iterator.hasNext()) {


                // sort to the genes then parse for SNPs

                SAMRecord read = iterator.next();


                if (this.lengthOfReads == 0) {
                    this.lengthOfReads = (read.getReadLength());
                }


                // associate to gene and store barebone there
                int start = read.getAlignmentStart();
                String chromosome = read.getReferenceName();

                // MZ scores the mapping in form of a simple sequence

                String CIGAR = read.getCigarString();
                // SNP calling has to be done here, so the information doesn't have to be stored...
                // this has to change,  snp calling before read storage, or there will be more information
                String MZ = read.getSAMString().split("\t")[11].split(":")[2];
                String[] MZArray = MZ.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");


                // optionalize this,  if mz is clear, this is not necessary;

                int stop = read.getStart() + read.getReadLength();
                SimpleRead splR = new SimpleRead(start, stop);


                try {
                    currentGene = findGene(chromosome, start, geneList, currentGene);
                    if (currentGene != null) {
                        currentGene.addRead(splR);


                        int currentPosition = 0;
                        if (MZArray.length > 1) {
                            for (int i = 0; i < MZArray.length; i++) {
                                if (MZArray[i].matches("\\D")) {
                                    int placeOnRead = currentPosition + Integer.parseInt(MZArray[i - 1]);
                                    currentPosition = placeOnRead + 1;

                                    char altBase = read.getSAMString().split("\t")[9].charAt(placeOnRead);
                                    char refBase = MZArray[i].charAt(0);

                                    int positionOnChrom = currentPosition + currentGene.getStart();
                                    SNP snp = new SNP(currentGene, refBase, altBase, positionOnChrom);

                                    if (refBase == altBase) {
                                        count++;
                                        System.out.println(currentPosition + " " + placeOnRead + " " + refBase + " " + altBase + "  : CIGAR " + read.getCigarString());
                                        System.out.println(MZ);
                                        System.out.println(read.getSAMString().split("\t")[9]);
                                    }
                                    if (refBase != altBase) {
                                        goodCount++;
                                        currentGene.addSNP(snp);
                                    }
                                }
                            }

                            // case CIGAR string is more complex ...
                            if (!CIGAR.contains("I") || CIGAR.contains("D")) {
                                //currentGene = findSNP(currentGene, chromosome, read.getStart(), MZArray, read.getSAMString().split("\t")[9]);
                            }
                        }

                        // no longer necessary,  only start and stop are relevant since SNPs are already checked
                        //SimpleRead splR = new SimpleRead(start, MZ, CIGAR);
                        //System.out.println(locus.toString());
                        //System.out.println();
                    }

                } catch (NullPointerException e) {
                    System.out.println(e);
                }
            }
            //System.out.println("bad SNPs counted : " + count + " vs good SNPs counted :" + goodCount);
        }
            catch(Exception e){
                System.out.println(e);
            }

        this.geneList = geneList;

            }


    public Gene findSNP(Gene currentGene, String chromosome, int start, String[] MZArray, String readSeq){


        try {
            // if reads are sorted a memory of the last gene will speed things up significantly
            currentGene = findGene(chromosome, start, geneList, currentGene);

            //if(currentGene != null) {
            //    currentGene.addRead(splR);

            for (int i = 0; i < MZArray.length; i++) {
                if (MZArray[i].matches("\\D")) {
                    int positionOnRead = Integer.parseInt(MZArray[i - 1]);
                    char altBase = MZArray[i].charAt(0);
                    //int positionOnChrom = positionOnRead + start;
                    // equals position on gene, if genes were used as a reference , then their start is 0
                    int positionOnChrom = positionOnRead + start - currentGene.getStart();

                        //System.out.println(read.getSAMString().split("\t")[9].charAt(positionOnRead));

                        //System.out.println(position + "  " + MZArray[i]);
                        // this should feed into a new SNP
                        //System.out.println("placement on gene "  + currentGene.getStart()+ "  " + currentGene.getStop() + "  " + positionOnChrom);
                        if(positionOnChrom < currentGene.getStop() && positionOnChrom > currentGene.getStart()) {
                            char refBase = currentGene.getSequence().getCompoundAt(positionOnChrom+1).toString().charAt(0);
                            //char altBase = readSeq.charAt(positionOnRead);
                            System.out.println(altBase + "  " + refBase);
                            SNP snp = new SNP(currentGene, refBase, altBase, positionOnChrom);

                         //System.out.println(" Created SNP on" + snp.getGene().getIdent() + currentGene.getIdent());
                         currentGene.addSNP(snp);
                         }

                        // the full coverage can be gotten from a locusiterator after SNP calling
                    }

            }
            return currentGene;
        }
        catch(Exception e ){
            /*for (int i = 0; i < MZArray.length; i++) {
                if (MZArray[i].matches("\\D")) {
                    int positionOnRead = Integer.parseInt(MZArray[i - 1]);
                    int positionOnChrom = positionOnRead + start - geneStart;

                    System.out.println(positionOnChrom + " : " + positionOnRead);
                }
            }*/


            System.out.println("Caught badly formatted MZ string");
            System.out.println(currentGene.getStart() + "  " + currentGene.getSequence().getLength() + " " + currentGene.getStop());
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            sw.toString();
            System.out.println(sw);
            //System.out.println(e);
        }
        return currentGene;
    }


/**
    public void findSNPs(){
        for(Gene gene:geneList){

            /*
            gene.getGeneReadList().sort(SimpleRead);

            Collections.sort(Database, new Comparator<SimpleRead>() {
                @Override public int compare(SimpleRead read1 , SimpleRead read2) {
                    return read1.getStart() - read2.getStart(); // Ascending
                }
                */
/**

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
                            if(position < gene.getStop()) {
                                SNP snp = new SNP(gene, MZArray[i].charAt(0), position);
                                gene.addSNP(snp);
                            }
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
*/



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
