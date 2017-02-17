package com.ernstthuer;

import org.apache.logging.log4j.core.util.Integers;
import org.biojava.nbio.core.sequence.DNASequence;

import java.util.ArrayList;
import java.util.HashMap;

class SNP implements Comparable<SNP> {

    /**
     * Core of the analysis  the SNP class catches individual SNPs, evaluation has to be carried out here
     * <p>
     * stored in their respective genes.  if no gene information is provided, chromosomes take the place of genes
     */

    private Gene gene;
    private char ORG = "N".charAt(0);
    private char ALT;
    private int position;
    private int ORGcov = 0;
    private int ALTcov = 1;
    private int validated;
    private double logDensityThreshold = 0;
    private double borderApproximation = 0.01;
    private int ratioExpression;
    private String expression = " ";
    private boolean isSynonymous;
    private HashMap<String, Double> hypothesisEval = new HashMap<>();
    private ArrayList<Integers> evaluationResults = new ArrayList<>();
    private int foundInReplicates;

    public SNP(Gene gene, char ALT, int position) {
        this.gene = gene;
        this.ALT = ALT;
        this.position = position;
        this.validated = 0;
        this.foundInReplicates = 1;
    }

    SNP(Gene gene, char ORG, char ALT, int position) {
        this.gene = gene;
        this.ORG = ORG;
        this.ALT = ALT;
        this.position = position;
        this.validated = 0;
        this.foundInReplicates = 1;
    }

    public SNP(char ORG, char ALT, int position) {
        this.gene = null;
        this.ORG = ORG;
        this.ALT = ALT;
        this.position = position;
        this.validated = 0;
        this.foundInReplicates = 1;
    }

    void setSynonymous(boolean synonymous) {
        isSynonymous = synonymous;
    }

    public boolean isSynonymous() {
        return isSynonymous;
    }

    int getORGcov() {
        return ORGcov;
    }

    char getORG() {
        return ORG;
    }

     char getALT() {
        return ALT;
    }

    Gene getGene() {
        return gene;
    }

    void setORGcov(int ORGcov) {
        this.ORGcov = ORGcov;
    }

    int getALTcov() {
        return ALTcov;
    }

    void increaseAltCoverage() {
        this.ALTcov++;
    }

    void setALTcov(int ALTcov) {
        this.ALTcov = ALTcov;
    }

    int isValidated() {
        return validated;
    }

    int getPosition() {
        return position;
    }

    void setValidated(int validated) {
        this.validated = validated;
    }

    void raiseValidation() {
        this.validated++;
    }
    void disableValidation(){ this.validated = -5;}



    int getFoundInReplicates() {
        return foundInReplicates;
    }

    void setFoundInReplicates(int foundInReplicates) {
        this.foundInReplicates = foundInReplicates;
    }
    void incrementFOundInReplicates(){
        this.foundInReplicates ++;
    }



    public void findTrueORG() {
        DNASequence dnaseq = gene.getSequence();
        String seq = dnaseq.toString();
        //System.out.println("gene at " + gene.getStart() + " snp position" + position);
        try {
            int snpPos = (this.position) - (gene.getStart() + 3);
            this.ORG = seq.charAt(snpPos);
        } catch (Exception e) {
            System.out.println("[ERROR] -> " + gene.getStart() + this.position + "  ?? ¿¿ ");
        }
    }


    void addHypothesis(String hypothesis, double value){
        this.hypothesisEval.put(hypothesis,value);
    }




    void setExpression(String expression) {
        this.expression = expression;
    }

    public String getExpression() {
        return expression;
    }

    HashMap<String, Double> getHypothesisEval() {
        return hypothesisEval;
    }

    public void setHypothesisEval(HashMap<String, Double> hypothesisEval) {
        this.hypothesisEval = hypothesisEval;
    }

    void addHypothesisEval(String test, Double results) {
        if(this.hypothesisEval.containsKey(test)){

            double valueSoFar =  this.hypothesisEval.get(test);
            if(results > logDensityThreshold) {
                double newValue = valueSoFar + 1;
                this.hypothesisEval.replace(test,newValue);
            }
        }else {
            if(results > logDensityThreshold) {
                this.hypothesisEval.put(test, 1.0);
            }
        }
        //this.hypothesisEval = hypothesisEval;
    }

    void removeHypothesisEval(String key){

        if(this.hypothesisEval.containsKey(key)){
            this.hypothesisEval.remove(key);
        }else{
            System.out.println("Cannot remove hypothesis,   not found");
        }
    }


    void combineSNPInformation(SNP snpOnOtherReplicate ){
        int foundsofar = foundInReplicates;
        foundInReplicates +=1;
        //System.out.println(ALTcov + " / "+ORGcov);

        this.ALTcov = (((ALTcov*foundsofar) + snpOnOtherReplicate.getALTcov()) /(1+foundsofar));
        this.ORGcov = (((ORGcov*foundsofar) + snpOnOtherReplicate.getORGcov()) /(1+foundsofar));

    }

    // improve this
    int[] combineSNPInformation(SNP first, SNP second) {

        // ToDo , treat ALtCov as ratio instead of integer values
        // [0] is ALT,  [1] is org
        int[] outcov = new int[2];
        int foundInReplicates = first.foundInReplicates;

        outcov[0] = (((first.getALTcov()*foundInReplicates) + second.getALTcov()) /(1+foundInReplicates));
        outcov[1] = (((first.getALTcov()*foundInReplicates) + second.getORGcov()) /(1+foundInReplicates));
        first.foundInReplicates+=1;
        //System.out.println( first.getALTcov() + " " + second.getALTcov()  +   "  " + foundInReplicates + "   " + outcov[0]);
        return outcov;


    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SNP snp = (SNP) o;

        if (ALT != snp.ALT) return false;
        if (position != snp.position) return false;
        return gene != null ? gene.equals(snp.gene) : snp.gene == null;

    }

    @Override
    public int hashCode() {
        int result = gene != null ? gene.hashCode() : 0;
        result = 31 * result + (int) ALT;
        result = 31 * result + position;
        return result;
    }

    @Override
    public String toString() {

        // chrom position ident ref alt qual filter info
        return this.gene.getChromosome() + "\t" + this.position + "\t" + this.gene.getIdent()
                + "\t" + this.ORG + "\t" + this.ALT + "\t" +
                this.getALTcov() + "\t" + this.getORGcov() +
                "\t" + "TestInfo" + "\t" + this.expression +
                "\t" + this.isSynonymous + "\t" +
                this.isValidated() + "\t" + this.foundInReplicates ;
    }

    @Override
    public int compareTo(SNP o) {
        if (this.ALT == o.ALT && this.position == o.position) {
            return 1;
        } else {
            return 0;
        }
    }


    public double[] deriveAlphaBeta(){
        // this calculates the mean of the beta distribution
        double[] alphaBeta = new double[2];

        return alphaBeta;
    }



}


