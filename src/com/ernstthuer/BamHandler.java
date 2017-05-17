package com.ernstthuer;

/**
 * Bamhandler implementation,   is handled by BamThreader,   goes through the mapped files with a variation of the Samtools  htsjdk LocusWalker
 */

import htsjdk.samtools.*;
import htsjdk.samtools.util.*;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.SamFileValidator;
import org.biojava.nbio.core.sequence.DNASequence;
import org.biojava.nbio.core.sequence.compound.NucleotideCompound;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.Callable;

class BamHandler extends FileHandler implements Callable  {
//    public class BamHandler extends FileHandler implements Runnable {

    // bam is only import, no direction,
    //public static int lengthOfReads = 0;
    private String locale;
    private String type;
    // each BAM file contains a potential snplist, this should be merged after reading.  keep separate for threading.
    private ArrayList<SNP> snpArrayList = new ArrayList<>();
    private ArrayList<Gene> localGeneList = new ArrayList<>();
    //private HashMap<String, DNASequence> fasta = new HashMap<>();

    // contain the reads   SAM format


    private boolean existingknowledge;
    private HashMap<String, DNASequence> fastaMap;
    private HashMap<String, DNASequence> fasta;

/*
    public void testWalker(AlignmentContext alignmentContext, ReferenceContext referenceContext, FeatureContext featureContext ){
        bamWalker.apply(alignmentContext,referenceContext,featureContext );
        System.out.println(bamWalker.hasReads());
    }*/

    BamHandler(String locale, String type, String direction) {
        super(locale, type, direction);
        this.type = type;
        this.locale = locale;
    }

    BamHandler(String locale, String type, String direction, HashMap<String, DNASequence> fasta) {
        super(locale, type, direction);
        this.type = type;
        this.locale = locale;
        this.fasta = fasta;
    }


    /*
    @Override
    public void run() {
        this.localGeneList = readBam(fastaMap,existingknowledge );
        }

*/
    @Override
    public ArrayList<Gene> call() {

        //return readBam(fastaMap,existingknowledge );
        //System.out.println(readBamLocus(fastaMap, existingknowledge));
        return readBamLocus(fastaMap, existingknowledge);

    }


    // helper methods to be called from main before threadin intiation,  creates copies of the fasta map and the boolean

    public void findExistingknowledge(boolean existingKnowledge) {
        this.existingknowledge = existingKnowledge;
    }

    public void loadFastaMap(HashMap<String, DNASequence> fastaMap) {
        this.fastaMap = fastaMap;
    }

    void setGeneList(ArrayList<Gene> geneList) {

        // create a deep copy to keep it independent

        ArrayList<Gene> copy = new ArrayList<>(geneList.size());

        for (Gene gene : geneList) {
            try {
                copy.add((Gene) gene.clone());
            } catch (CloneNotSupportedException e) {
                System.out.println(e);
            }
        }

        this.localGeneList = copy;
    }

    public ArrayList<Gene> getGeneList() {
        return localGeneList;
    }

// 67 is C   84 = T    65 = A    71 = G

    //  ToDo  feed gene list into the bam evaluator to check only genes
    //  Read locus by locus for each gene in the available geneList
    ArrayList<Gene> readBamLocus(HashMap fastaMap, boolean existingKnowledge) {

        /**
         * reads a Bam file, stores SNPs.  check if there are gene names used as reference, or chromosome names.
         * associate SNPs to genes.  SNP by gene will be the main SNP storage.  SNPs are temporarily stored in the BAMreader, to ease thread ability
         *
         * build on the HTSJDK  locuswalker
         */

        //ArrayList<Gene> geneArrayList = new ArrayList<>();
        ArrayList<Gene> geneArrayList = localGeneList;


        try {

            int MINIMUM_MAPPING_QUALITY = 0;
            final SamFileValidator validator = new SamFileValidator(new PrintWriter(System.out), 8000);
            validator.setIgnoreWarnings(true);
            validator.setVerbose(true, 1000);
            validator.setErrorsToIgnore(Collections.singletonList(SAMValidationError.Type.MISSING_READ_GROUP));
            SamReaderFactory factory = SamReaderFactory.makeDefault().validationStringency(ValidationStringency.STRICT);
            SamReader fileBam = factory.open(new File(this.locale));
            //SAMRecordIterator iterator = fileBam.iterator();
            SamLocusIterator locusIterator = new SamLocusIterator(fileBam);  // intervals to gene positions

            Gene currentGene = null;


            char[] decodeArray =  {'A','C','G','T'};

            while (locusIterator.hasNext()) {

                SamLocusIterator.LocusInfo locusIt = locusIterator.next();

                int location = locusIt.getPosition();
                String chromosome = locusIt.getSequenceName();


                currentGene = findGene(chromosome, location, localGeneList, currentGene);
                // get reference base for location

                if (currentGene != null) {

                    byte refBase = getRefbase(chromosome, location);


                    // store thresholds as array,
                    int[] threshold = new int[4];

                    int MINTHRESH = 3;  // get this from main

                    int ORG_COVERAGE = 0;

                    for (final SamLocusIterator.RecordAndOffset rec : locusIt.getRecordAndPositions()) {
                        byte base = rec.getReadBase();
                        if (refBase != base && rec.getBaseQuality() > MINIMUM_MAPPING_QUALITY) {
                            switch (base) {
                                case 65:
                                    threshold[0] += 1;
                                    break;
                                case 67:
                                    threshold[1] += 1;
                                    break;
                                case 71:
                                    threshold[2] += 1;
                                    break;
                                case 84:
                                    threshold[3] += 1;
                                    break;
                            }
                        }else{
                            ORG_COVERAGE+=1;
                        }
                    }


                    for (int i = 0; i<threshold.length;i++){

                        if(threshold[i] > MINTHRESH) {
                            // NEW SNP here
                            char RefNuc =  decodeRef(refBase);
                            char AltNuc =  decodeRef(decodeArray[i]);

                            SNP snp = new SNP(currentGene,RefNuc,AltNuc,location);
                            snp.setALTcov(threshold[i]);
                            snp.setORGcov(ORG_COVERAGE);


                            currentGene.addSNP(snp,false);
                            //snpArrayList.add(snp);

                            //System.out.println(currentGene.getIdent() + "  " + currentGene.getSnpsOnGene().size());

                        }
                    }
                        //System.out.println(base); // unicode , needs translating
                        // 67 is C   84 = T    65 = A    71 = G
                }
            }



            locusIterator.close();

        } catch (Exception e) {
            //System.out.println(e);
            System.out.println("[WARNING] Malformed read");
        }


        return geneArrayList;
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

//    private Gene findGene(String chromosome, int start, ArrayList<Gene> geneList, Gene currentgene) {
//
//        // first check if read belongs to the same gene as before:  if not find the next one
//        if (currentgene != null && currentgene.getChromosome().equals(chromosome) && currentgene.getStop() > start && currentgene.getStart() < start) {
//            return currentgene;
//        }
//
//
//        for (Gene gene : geneList) {
//            if (gene.getChromosome().equals(chromosome)) {
//                if (gene.getStart() < start && gene.getStop() > start) {
//                    return gene;
//                }
//            }
//        }
//        return null;
//    }

    private Gene findGene(String chromosome, int location, ArrayList<Gene> geneList, Gene currentgene) {
        /**
         * overloading this to check for gene location instead of read start,  due to change from read iterator to
         *
         */

        // first check if read belongs to the same gene as before:  if not find the next one
        if (currentgene != null && currentgene.getChromosome().equals(chromosome) && currentgene.getStop() > location && currentgene.getStart() < location) {
            return currentgene;
            // same gene as last locus
        }


        for (Gene gene : geneList) {
            if (gene.getChromosome().equals(chromosome)) {
                //System.out.println(gene.getStart() + "  "  + location);
                if (gene.getStart() < location && gene.getStop() > location) {
                    return gene;
                }
            }
        }
        return null;
    }


    private byte getRefbase(String chromosome, int location) {
        //System.out.println("fastas in file "+ this.fasta.keySet().size() +   " location = " + location);
        String nucl = fasta.get(chromosome).getCompoundAt(location).toString();
        // 67 is C   84 = T    65 = A    71 = G
        switch (nucl) {
            case "A":
                return 65;
            case "C":
                return 67;
            case "G":
                return 71;
            case "T":
                return 84;
            default:
                return 0;
        }
    }


    private char decodeRef(int decimalCode){
        switch (decimalCode){
            case 65 : return 'A' ;
            case 67 : return 'C' ;
            case 71 : return 'G' ;
            case 84 : return 'T' ;
            default:return 0;
        }
    }

}
