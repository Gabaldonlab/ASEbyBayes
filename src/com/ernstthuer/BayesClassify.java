package com.ernstthuer;


import jdk.nashorn.internal.codegen.CompilerConstants;
import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.special.Beta;

import static java.lang.Math.exp;
import static java.lang.Math.sqrt;

/**
 * Created by ethuer on 2/08/16.
 */
public class BayesClassify {
    /**
     * Classifier for Posterior calculation based on expectations from the data
     *
     *
     * 3 expectations for the ratio :    0:1  0.5:0.5 and 1:0  (noise, equal expression, and noise)
     * model likelihood according to negative binomial distribution and priors according to beta dist.
     *
     * alpha and beta value for the beta distribution are bimodal for noise detection, and strongly informative for ASE,
     * at an expected ratio of 0.5 , this can be influenced by the real distribution, and results in the true classification
     *
     */



    private double alpha;
    private double beta;
    private int TrueCalls;
    private int CallsTotal;
    private BetaDistribution posterior;
    private double sigmaPosterior ;
    

    private int expectation;

    public BayesClassify(double alpha, double beta, int trueCalls, int callsTotal) {
        this.alpha = alpha;
        this.beta = beta;
        this.TrueCalls = trueCalls;
        this.CallsTotal = callsTotal;
        this.posterior = getBetaFunctionPosterior();
        this.sigmaPosterior = getSigma();

    }

    public BayesClassify(int trueCalls, int callsTotal) {
        this.TrueCalls = trueCalls;
        this.CallsTotal = callsTotal;
        this.alpha = 1.0;
        this.beta = 1.0;

    }

    public void getConfidence (double input) {
        System.out.println("Alpha and Beta : ");
        System.out.println(posterior.getAlpha());
        System.out.println(posterior.getBeta());
        System.out.println("SupportLimits : ");

        System.out.println(posterior.getSupportLowerBound());
        System.out.println(posterior.getSupportUpperBound());
        System.out.println(posterior.logDensity(input));

    }

    //
    public BetaDistribution getBetaFunctionPosterior (){
        double meanBeta = this.alpha / (alpha+this.beta);  // actual prior believe of theta
        double sampleSize = alpha + beta;  // directly dependent on strength of belief
        //posterior

        //double a = (TrueCalls + (meanBeta * sampleSize) - 1 );
        //double b = (CallsTotal - TrueCalls ) + (sampleSize * (1- meanBeta)) - 1;
        double a = (TrueCalls + (alpha) - 1 );
        double b = (CallsTotal - TrueCalls ) + (beta) - 1;
        BetaDistribution betaDist = new BetaDistribution(a, b);

        return betaDist ;

    }

    public double getSigma(){
        double meanBeta = alpha / (alpha+beta);  // actual prior believe of theta
        double sampleSize = alpha + beta;  // directly dependent on strength of belief
        double a = (TrueCalls + (meanBeta * sampleSize) - 1 );
        double b = (CallsTotal - TrueCalls ) + (sampleSize * (1- meanBeta)) - 1;

        double sigma = sqrt((a*b)/ ((a+b)*(a+b)*(a+b+1)));
        double mean = a / (a + b);

        return sigma;
    }

    public double getMean(){
        double meanBeta = alpha / (alpha+beta);  // actual prior believe of theta
        double sampleSize = alpha + beta;  // directly dependent on strength of belief
        double a = (TrueCalls + (meanBeta * sampleSize) - 1 );
        double b = (CallsTotal - TrueCalls ) + (sampleSize * (1- meanBeta)) - 1;

        double sigma = sqrt((a*b)/ ((a+b)*(a+b)*(a+b+1)));
        double mean = a / (a + b);

        return mean;
    }


    // the intermediate functions for priors and likelihood for testing the accuracy.
    public BetaDistribution getPrior(  double alpha, double beta){
//        int prior = 0;
        BetaDistribution betaDist = new BetaDistribution(alpha,beta);

        return betaDist ;
    }


    public BetaDistribution likelihood( double TrueCalls,  double CallsTotal){
//        int likelihood = 0;

        BetaDistribution betaDist = new BetaDistribution(TrueCalls,CallsTotal);
        return betaDist ;
    }

    public BetaDistribution getPosterior() {
        return posterior;
    }

    // This has to be done according to expectations of ratio and the coverage of the gene / amount of SNPs per gene...






}
