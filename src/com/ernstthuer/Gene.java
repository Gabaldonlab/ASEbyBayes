package com.ernstthuer;

import org.biojava.nbio.core.exceptions.CompoundNotFoundException;
import org.biojava.nbio.core.sequence.DNASequence;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class Gene implements Cloneable {

    private ArrayList<SNP> snpsOnGene = new ArrayList<>();
    private DNASequence sequence = new DNASequence();
    private String chromosome;
    private int start;
    private int stop;
    private String ident;
    private char orientation = '+';
    private HashMap<Integer, Codon> codonList = new HashMap<>();
    private ArrayList<SimpleRead> geneReadList = new ArrayList<>();
    //private String ASE;
    private LinkedHashMap<Integer, Integer> positioncount = new LinkedHashMap<>();
    private HashMap<String, Integer> hypothesisEval = new HashMap<>();


    // needs to be more flexible than this array
    //private double[] hypothesisArray = new double[4];

    @Override
    protected Object clone() throws CloneNotSupportedException {

        this.snpsOnGene = new ArrayList<>();
        return super.clone();

    }

    public Gene(String chromosome, int start, int stop, String ident) {
        this.chromosome = chromosome;
        this.start = start;
        this.stop = stop;
        this.ident = ident;
    }

    public void addCoverage(int start, int stop) {
        for (int i = start; i < stop; i++) {
            int count = this.positioncount.containsKey(i) ? positioncount.get(i) : 0;
            positioncount.put(i, count + 1);
        }

    }

    public LinkedHashMap<Integer, Integer> getPositioncount() {
        return positioncount;
    }

    ArrayList<SNP> getSnpsOnGene() {
        return snpsOnGene;
    }

    boolean addSNP(SNP snp, boolean existingKnowledge) {

        if (existingKnowledge) {
            if (snpsOnGene.contains(snp)) {
                int idx = snpsOnGene.indexOf(snp);
                snpsOnGene.get(idx).increaseAltCoverage();
                // all SNPs are validated
                snpsOnGene.get(idx).setValidated(2);
            }

            return false;  // if SNPs are already known from an imported vcf file, no need to add new ones
        }

        if (!snpsOnGene.contains(snp)) {
            if (snp.getPosition() > this.start && snp.getPosition() < this.stop) {
                snpsOnGene.add(snp);
                return true;
            }
            return false;
        } else {
            int idx = snpsOnGene.indexOf(snp);
            snpsOnGene.get(idx).increaseAltCoverage();
            if (snpsOnGene.get(idx).getALTcov() > 2) {
                snpsOnGene.get(idx).setValidated(1);
            }
            return false;
        }
    }

    public void setSnpsOnGene(ArrayList<SNP> snpsOnGene) {
        this.snpsOnGene = snpsOnGene;
    }



    public char getOrientation() {
        return orientation;
    }

    void setOrientation(char orientation) {
        this.orientation = orientation;
    }

    private void geneToCodon(String codingSequence) {
        // Codon list for all positions, for each position
        // HashMap<Integer, org.biojava.nbio.core.sequence.template.Sequence> CodonList = new HashMap<>();
        // for each codon on sequence

        // make sure coding sequence is divisible by 3
        int overshot = codingSequence.length() % 3;


        for (int i = 0; i < codingSequence.length() - overshot; i = i + 3) {
            //org.biojava.nbio.core.sequence.template.Sequence codon = sequence.getSubSequence(i,i+2).getViewedSequence();
            Codon codon = new Codon(codingSequence.substring(i, i + 3));
            for (int j = i; j < (i + 3); j++) {
                codonList.put(j, codon);
            }
        }
    }

    public String getAltCodon(int pos, char altChar, String orgCodon) {
        StringBuilder newCodon = new StringBuilder(orgCodon);
        newCodon.setCharAt(pos, altChar);
        return newCodon.toString();
    }

    void findSynonymity(int validationLVL) {

        try {
            if (orientation == '+') {
                geneToCodon(this.sequence.getSequenceAsString());

            }
            if (orientation == '-') {
                geneToCodon(this.sequence.getInverse().getSequenceAsString());
            }
        } catch (NullPointerException e) {
            System.out.println("Did not find gene orientation");
            try {
                geneToCodon(this.sequence.getSequenceAsString());
            } catch (NullPointerException e2) {
                System.out.println("no valid gene sequence found");
            }
        }
        for (SNP snp : snpsOnGene) {
            if (snp.isValidated() >= validationLVL) {

                // find SNP on codon
                int relativePosition = snp.getPosition() - start;
                int remain = relativePosition % 3;


                Codon refCodon = codonList.get(relativePosition);


                try {

                    StringBuilder altSeq = new StringBuilder(refCodon.getSequence());
                    altSeq.setCharAt(remain, snp.getALT());

                    Codon altCodon = new Codon(altSeq.toString());

                    snp.setSynonymous(altCodon.equals(refCodon));

                } catch (NullPointerException e) {
                    //System.out.println(snp.getPosition() + "  " + snp.getGene().getIdent());
                }
                //String altCodon = getAltCodon(0, snp.getALT(), refCodon.getSequence());
                //altCodon = snp.getALT() + altCodon.substring(1,2);
                //StringBuilder newCodon = new StringBuilder(altCodon);
                //newCodon.setCharAt(remain, snp.getALT());

            }
        }

    }


    DNASequence getSequence() {
        return sequence;
    }


    String getChromosome() {
        return chromosome;
    }

    int getStart() {
        return start;
    }

    int getStop() {
        return stop;
    }

    String getIdent() {
        return ident;
    }


/*
    public void setSnpsOnGene(ArrayList<SNP> snpsOnGene) {
        this.snpsOnGene = snpsOnGene;
    }
*/

    void addSnpInformation(ArrayList<SNP> outsideSnpsOnGene) {

        for (SNP snp : outsideSnpsOnGene) {
            if (snpsOnGene.contains(snp)) {

                int idx = snpsOnGene.indexOf(snp);
                int currentORG = snpsOnGene.get(idx).getORGcov();
                int currentALT = snpsOnGene.get(idx).getALTcov();

                int newORG = snpsOnGene.get(idx).getORGcov();
                int newALT = snpsOnGene.get(idx).getALTcov();

                double ratio = (double) newALT / (double) newORG;
                double newRatio = (double) newALT / (double) newORG;

                double avgRatio = (ratio + newRatio) / 2;
                double avgORG = (currentORG + newORG) / 2;
                double avgALT = avgORG * avgRatio;

            } else {
                snpsOnGene.add(snp);
            }
        }
    }

    void loadSequence(DNASequence fullGenome, boolean fullChrom) {

        if (fullChrom) {
            try {
                try {

                    this.sequence = new DNASequence(fullGenome.getSubSequence(this.start, this.stop).getSequenceAsString());
                } catch (CompoundNotFoundException e) {
                    System.out.println("[ERROR] Caught error in sequence parsing ");
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    //sw.toString();
                    //System.out.println(sw);
                }

            } catch (Exception e) {
                System.out.println("[ERROR]  Coldn't parse fasta sequence " + e);
            }
        } else {
            try {
                this.sequence = fullGenome;
            } catch (Exception e) {
                System.out.println("[ERROR]  Couldn't parse fasta sequence " + e);
            }
        }

    }
/*

    public void evaluateGeneExpression() {

        // THis is for the whole gene
        for (SNP snp: snpsOnGene) {

            boolean FullSNPevidence = snp.validateSNP(bimodalPrimersForNoise, bimodalPrimersForNoise, 1);
            boolean CentralExpressionEvidence = snp.validateSNP(strongCentralInformativePrimers, strongCentralInformativePrimers, 2);



            if (FullSNPevidence) {
                snp.setExpression("FULLSNP");
            }
            if (CentralExpressionEvidence) {
                snp.setExpression("EQUALALLELICEXPRESSION");
            }
        }
        }
        */


/*    public void evaluateSNPs() {
        for (SNP snp : snpsOnGene) {
            if (snp.isValidated() >= 1) {
                //snpsOnGene.remove(snp);
                //}
                //else{
                if (!snp.validateSNP(10, -1, 0)) {
                    //snpsOnGene.remove(snp);
                    snp.disableValidation();
                } else {
                    snp.raiseValidation();
                }

                if (snp.validateSNP(-1, 10, 1)) {
                    snp.setExpression("FULLSNP");
                } else {
                    snp.raiseValidation();
                }

                if (snp.validateSNP(10, 10, 2)) {
                    snp.setExpression("EQUALALLELICEXPRESSION");

                } else {
                    snp.raiseValidation();
                }


            }

        }
    }*/


    HashMap<String, Integer> getHypothesisEval() {
        return hypothesisEval;
    }

    //void addToHypothesisEval(String name, Double eval) {
    //    this.hypothesisEval.put(name, eval);
    //}

    //public void addRead(SimpleRead read) {
//        this.geneReadList.add(read);
//    }


    ArrayList<SNP> unifySNPLists(ArrayList<SNP> otherSNPList) {
        ArrayList<SNP> snpArrayList = new ArrayList<>();

        for (SNP snp : snpsOnGene) {
            snpArrayList.add(snp);
        }
        for (SNP snp : otherSNPList) {
            if (!snpArrayList.contains(snp)) {
                snpArrayList.add(snp);
            } else {
                int indexOfSNP = snpArrayList.indexOf(snp);
                SNP orgSNP = snpArrayList.get(indexOfSNP);
                //orgSNP.combineSNPInformation(snp);

                int[] newCov = orgSNP.combineSNPInformation(orgSNP, snp);

                orgSNP.setALTcov(newCov[0]);
                orgSNP.setORGcov(newCov[1]);


            }
        }
        return snpArrayList;
    }

    void setHypothesisEval(HashMap<String, Integer> hypothesisEval) {
        this.hypothesisEval = hypothesisEval;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Gene gene = (Gene) o;

        if (start != gene.start || stop != gene.stop ) return false;
        return chromosome != null ? chromosome.equals(gene.chromosome) : gene.chromosome == null;

    }

    @Override
    public int hashCode() {
        int result = chromosome != null ? chromosome.hashCode() : 0;
        result = 31 * result + start;
        result = 31 * result + stop;
        return result;
    }

//
//    void addHypothesisCount(double[] hypothesisArray) {
//        this.hypothesisArray = hypothesisArray;
//    }
//
//    public double[] getHypothesisArray() {
//        return hypothesisArray;
//    }
//


}




