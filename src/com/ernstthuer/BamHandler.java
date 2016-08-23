package com.ernstthuer;

/**
 * Created by ethur on 7/26/16.
 */

import htsjdk.samtools.*;
import htsjdk.samtools.util.*;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.SamFileValidator;
import org.biojava.nbio.core.sequence.DNASequence;

//import htsjdk.samtools.util.CloserUtil;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

public class BamHandler extends FileHandler implements Runnable {


    // bam is only import, no direction,
    //public static int lengthOfReads = 0;
    private String locale;
    private String type;
    // each BAM file contains a potential snplist, this should be merged after reading.  keep separate for threading.
    private ArrayList<SNP> snpArrayList = new ArrayList<>();
    private ArrayList<Gene> localGeneList = new ArrayList<>();

    // contain the reads   SAM format


    private boolean existingknowledge ;
    private HashMap<String, DNASequence> fastaMap;

    /**
     * Tricky,  no mpileup function available.
     * Samreader has to compensate, read MD:Z score, supplement with CIGAR to get SNP information on read.
     * Store read skeleton on gene to get full coverage information
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


    @Override
    public void run() {
        this.localGeneList = readBam(fastaMap,existingknowledge );
        }
    }


    // helper methods to be called from main before threadin intiation,  creates copies of the fasta map and the boolean

    public void findExistingknowledge(boolean existingKnowledge){
        this.existingknowledge = existingKnowledge;
    }

    public void loadFastaMap(HashMap<String, DNASequence>  fastaMap ){
        this.fastaMap = fastaMap;
    }

    public void setGeneList(ArrayList<Gene> geneList) {

        // create a deep copy to keep it independent

        ArrayList<Gene> copy = new ArrayList<Gene>(geneList.size());

        for (Gene gene: geneList) {
            try {
                copy.add((Gene) gene.clone());
            }catch (CloneNotSupportedException e){
                System.out.println(e);
            }
        }

        this.localGeneList = copy;
    }

    public ArrayList<Gene> getGeneList() {
        return localGeneList;
    }


    public ArrayList<Gene> readBam(HashMap fastaMap, boolean existingKnowledge) {
    //public void readBam(HashMap fastaMap, ArrayList<Gene> geneListExternal, boolean existingKnowledge) {

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
            // a default gene
            Gene currentGene = null;

            while (iterator.hasNext()) {


                // sort to the genes then parse for SNPs

                SAMRecord read = iterator.next();


                // associate to gene and store barebone there
                int start = read.getAlignmentStart();
                String chromosome = read.getReferenceName();

                // MZ scores the mapping in form of a simple sequence

                String CIGAR = read.getCigarString();
                String MZ = read.getSAMString().split("\t")[11].split(":")[2];
                String[] MZArray = MZ.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");

                int stop = read.getStart() + read.getReadLength();
                SimpleRead splR = new SimpleRead(start, stop);

                try {
                    currentGene = findGene(chromosome, start, localGeneList, currentGene);
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

                                    int positionOnChrom = placeOnRead + read.getStart();
                                    SNP snp = new SNP(currentGene, refBase, altBase, positionOnChrom);


                                    if (refBase != altBase && !CIGAR.contains("D") && !CIGAR.contains("I")) {
                                        currentGene.addSNP(snp,existingKnowledge);
                                        //System.out.println(" Created SNP on " + snp.getGene().getIdent());
                                        //System.out.println(" on position " + snp.getPosition() + "  " + snp.getALT());
                                    }
                                }
                            }
                        }

                    }

                } catch (NullPointerException e) {
                    System.out.println(e);
                }
            }
            //System.out.println("bad SNPs counted : " + count + " vs good SNPs counted :" + goodCount);
        } catch (Exception e) {
            System.out.println(e);
        }finally {
            return localGeneList;
        }




    }

/**
    public Gene findSNP(Gene currentGene, String chromosome, int start, String[] MZArray, String readSeq) {


        try {
            // if reads are sorted a memory of the last gene will speed things up significantly
            currentGene = findGene(chromosome, start, geneList, currentGene);

            for (int i = 0; i < MZArray.length; i++) {
                if (MZArray[i].matches("\\D")) {
                    int positionOnRead = Integer.parseInt(MZArray[i - 1]);
                    char altBase = MZArray[i].charAt(0);

                    // equals position on gene, if genes were used as a reference , then their start is 0
                    int positionOnChrom = positionOnRead + start - currentGene.getStart();
                    if (positionOnChrom < currentGene.getStop() && positionOnChrom > currentGene.getStart()) {
                        char refBase = currentGene.getSequence().getCompoundAt(positionOnChrom + 1).toString().charAt(0);
                        System.out.println(altBase + "  " + refBase);
                        SNP snp = new SNP(currentGene, refBase, altBase, positionOnChrom);

                        System.out.println(" Created SNP on " + snp.getGene().getIdent() + currentGene.getIdent());
                        System.out.println("  on position " + snp.getPosition() + "  " + snp.getALT());
                        currentGene.addSNP(snp, false);
                    }
                }
            }
            return currentGene;
        } catch (Exception e) {
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

    public Gene findGene(String chromosome, int start, ArrayList<Gene> geneList, Gene currentgene) {

        // first check if read belongs to the same gene as before:  if not find the next one
        if (currentgene != null && currentgene.getChromosome() == chromosome && currentgene.getStop() > start && currentgene.getStart() < start) {
            return currentgene;
        }


        for (Gene gene : geneList) {
            if (gene.getChromosome().equals(chromosome)) {
                if (gene.getStart() < start && gene.getStop() > start) {
                    return gene;
                }
            }
        }
        return null;
    }

}
