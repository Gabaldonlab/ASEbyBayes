package com.ernstthuer;

/**
 * Created by ethur on 7/26/16.
 */
public class SNP implements Comparable<SNP>{

    /**
     * Core of the analysis  the SNP class catches individual SNPs, evaluation has to be carried out here
     *
     *stored in their respective genes.  if no gene information is provided, chromosomes take the place of genes
     *
     */


    private Gene gene;
    private char ORG;
    private char ALT;
    private int position;
    private int ORGcov = 0;
    private int ALTcov = 1;
    private int validated ;

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

    public Gene getGene() {
        return gene;
    }

    public void setORGcov(int ORGcov) {
        this.ORGcov = ORGcov;
    }

    public int getALTcov() {
        return ALTcov;
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

    public void raiseValidation(){
        this.validated ++;
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
    public String toString(){
        return this.ALT + " " + this.position;
    }

    @Override
    public int compareTo(SNP o) {
        if (this.ALT == o.ALT && this.position == o.position){
            return 1;
        }else {
            return 0;
        }
    }
}


