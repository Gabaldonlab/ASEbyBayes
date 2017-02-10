package com.ernstthuer;

import java.util.ArrayList;


class Hypothesis {

/**
 * Separate the hypothesis from the classification code,
 *
 * This keeps the Bayesian classification more
 * flexible,  possibly replace the bayesian classifier.
 *
 * THe hypothesis is predefined and contains the mean of expression ratio and the fixed intensity derived from the data
 *
 *
 * 3 core hypothesis with initialized alpha and beta values has to be evaluated on this
 *
 *
 * the 3 conditions should start with  :
 * 25:0  25:25 and 0:25 ,  most likely candidates of SNPs will call the hypothesis
 *
 *
 */

    private double mean;
    private double FIXED_INTENSITY ;
    private String name;
    private double STATIC_SIGMA_MULTIPLIER = 2.0;


    Hypothesis(double mean,double fixedIntensity,  String name) {
        this.mean = mean;
        this.FIXED_INTENSITY = fixedIntensity;
        this.name = name;
        //this.snps = snps;
        //bcl = new BayesClassify(mean,FIXED_INTENSITY);

    }



    void testHypothesis(ArrayList<Gene> geneList){
        for (Gene gene: geneList
             ) {
            for (SNP snp : gene.getSnpsOnGene()
                 ) {

                testSNPBCL(snp);
                System.out.println(snp.getPosition() + "  "+ snp.getALTcov() + " " + name + "  " + testSNPBCL(snp));

            }

        }



    }



    boolean testSNPBCL(SNP snp){
        // This feeds a SNP and tests it against the 'default' classifier
        BayesClassify bcl = new BayesClassify(this.mean,FIXED_INTENSITY,snp.getALTcov(), (snp.getALTcov()+snp.getORGcov()));
        return bcl.baseSNPTest(STATIC_SIGMA_MULTIPLIER);
    }





}