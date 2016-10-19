package com.ernstthuer;

import java.util.ArrayList;

/**
 * Created by ethuer on 17/10/16.
 */
public class Hypothesis {

    /**
     * Seperate the hupothesis from the code,
     *
     * This keeps the Bayesian classification more
     * flexible
     *
     * 3 core hypothesis with initialized alpha and beta values has to be evaluated on this
     *
     *
     * Stores the individual alpha and beta values
     *
     *
     * the 3 conditions should start with  :
     * 25:0  25:25 and 0:25 ,  most likely candidates of SNPs will call the hypothesis
     *
     *
     * needs a linear function to incorporate the observed coverage into the alpha and beta values
     *
     *
     *
     *
     *
     */
    private double alpha;
    private double beta;

    private double culminatingProb;
    private String name;

    private ArrayList<String> affectedGenes;
    private ArrayList<Gene> geneList;

    public Hypothesis(double alpha, double beta, String name, ArrayList<Gene> geneList) {
        this.alpha = alpha;
        this.beta = beta;
        this.name = name;
        this.geneList = geneList;
        this.affectedGenes = new ArrayList<>();



    }

    public Hypothesis(double alpha, double beta, String name) {
        this.alpha = alpha;
        this.beta = beta;
        this.name = name;
        this.geneList = new ArrayList<>();
        this.affectedGenes = new ArrayList<>();
    }

    public void addGene(Gene gene){
        this.geneList.add(gene);
    }


    // evaluate geneList over their SNPs
    //

    public double alphaBetaCorrection(int TotalCoverage, double alphaBeta){

        double mincoverage = 0.001;

        /**
         * a static alpha/ beta set value cannot classify the observed changes in coverage
         * implement a linear function for classification
         *
         * a linear function should be sufficiently complex
         *
         * y = kx+d
         *
         * d = minimum of 0.01
         * k = multiplier for presets in expectations
         *
         *
         */
        double newAlphaorBeta = mincoverage + ((alphaBeta* 0.1) *TotalCoverage);
       //double newBeta = mincoverage + ((beta*0.1) *TotalCoverage);
        return newAlphaorBeta  ;
    }


    public ArrayList<Gene> getGeneList() {
        return geneList;
    }

    public void calculateHypothisForSNPsOnGenes(ArrayList<Gene> geneList){

        for(Gene gene:geneList){
            System.out.println(gene.getIdent());
            for(SNP snp: gene.getSnpsOnGene()){
                int coverage = snp.getALTcov() + snp.getORGcov();
                int callsTotal = snp.getALTcov() + snp.getORGcov();
                BayesClassify bcl = new BayesClassify( alphaBetaCorrection(coverage, alpha), alphaBetaCorrection(coverage, beta), snp.getALTcov(), callsTotal);

                double mean = bcl.getMean();
                double sigma = bcl.getSigma();
                double logdensity = bcl.getPosterior().logDensity((alpha/(alpha+beta)));
                double ratio = (double) snp.getALTcov() / ((double)snp.getORGcov()+(double)snp.getALTcov());




                if(logdensity > -1.3) {
                    //System.out.println(" ratio :" + ratio + "  ALTcov = " + snp.getALTcov());
                    System.out.println("Mean : " + mean + "  Sigma :" + sigma + " logDensity : " + logdensity + "  Altcov " + snp.getALTcov());
                }
            }
        }
    }
}
