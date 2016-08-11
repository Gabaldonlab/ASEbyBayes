package com.ernstthuer;

import org.biojava.nbio.core.sequence.DNASequence;

import java.util.List;

/**
 * Created by ethur on 7/26/16.
 */
public class SNP implements Comparable<SNP> {

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
    private double logDensityThreshold = 1;
    private double borderApproximation = 0.01;
    private int ratioExpression;
    private String expression = " ";

    public SNP(Gene gene, char ALT, int position) {
        this.gene = gene;
        this.ALT = ALT;
        this.position = position;
        this.validated = 0;
    }

    public SNP(Gene gene, char ORG, char ALT, int position) {
        this.gene = gene;
        this.ORG = ORG;
        this.ALT = ALT;
        this.position = position;
        this.validated = 0;
    }

    public SNP(char ORG, char ALT, int position) {
        this.gene = null;
        this.ORG = ORG;
        this.ALT = ALT;
        this.position = position;
        this.validated = 0;
    }


    public int getORGcov() {
        return ORGcov;
    }

    public char getORG() {
        return ORG;
    }

    public char getALT() {
        return ALT;
    }

    public Gene getGene() {
        return gene;
    }

    public void setORGcov(int ORGcov) {
        this.ORGcov = ORGcov;
    }

    public int getALTcov() {
        return ALTcov;
    }

    public void increaseAltCoverage() {
        this.ALTcov++;
    }

    public void setALTcov(int ALTcov) {
        this.ALTcov = ALTcov;
    }

    public int isValidated() {
        return validated;
    }

    public int getPosition() {
        return position;
    }

    public void setValidated(int validated) {
        this.validated = validated;
    }

    public void raiseValidation() {
        this.validated++;
    }

    public void increaseORGcov() {
        this.ORGcov++;
    }

    /*
    public int addCoverageToSNPs( List<SimpleRead> splRds) {
        int totalCoverage = 0;
        //int length = lengthOfReads;
        for (SimpleRead splrd : splRds) {
            int position = splrd.getStart();
            int stop = splrd.getStop();
            if(this.position > position && this.position < stop){
                totalCoverage ++;
            }
        }
        this.ORGcov = totalCoverage - this.ALTcov;
        return totalCoverage;
    }
*/


    public boolean validateSNP(double alphaValue, double betaValue, int setMode) {

        BayesClassify bcl = new BayesClassify(alphaValue, betaValue, this.getALTcov(), this.getORGcov());
        //System.out.println("logdensity : " + bcl.getBetaFunctionPosterior().logDensity(borderApproximation));
        switch (setMode) {
            case 0:
                // lower bound validation
                if (bcl.getBetaFunctionPosterior().logDensity(borderApproximation) > logDensityThreshold) {
                    return false;
                } else {
                    return true;
                }
            case 1:
                // foll SNP validation
                if (bcl.getBetaFunctionPosterior().logDensity(1 - borderApproximation) > logDensityThreshold) {
                    return false;
                } else {
                    return true;
                }
            case 2:
                // Equal allele Expression
                if (bcl.getBetaFunctionPosterior().logDensity(0.5) > logDensityThreshold) {
                    return false;
                } else {
                    return true;
                }


        }
        return false;
    }

    public void findTrueORG() {
        DNASequence dnaseq = gene.getSequence();
        String seq = dnaseq.toString();
        //System.out.println("gene at " + gene.getStart() + " snp position" + position);
        try {
            int snpPos = (this.position) - (gene.getStart() + 3);
            this.ORG = seq.charAt(snpPos);
        } catch (Exception e) {
            System.out.println(" -> " + gene.getStart() + this.position);
        }
    }


    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getExpression() {
        return expression;
    }

    /*
                public boolean validateSNPforASE(){
                    BayesClassify bcl = new BayesClassify(25,25,this.getALTcov(),this.getORGcov());
                    if(bcl.getBetaFunctionPosterior().logDensity(borderApproximation) < logDensityThreshold){
                        return false;
                    }else {
                        return true;
                    }
                }

            */
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
                + "\t" + this.ORG + "\t" + this.ALT + "\t" + this.getALTcov() + "\t" + this.getORGcov() + "\t" + "TestInfo" + "\t" + this.expression;
    }

    @Override
    public int compareTo(SNP o) {
        if (this.ALT == o.ALT && this.position == o.position) {
            return 1;
        } else {
            return 0;
        }
    }


}


