package com.ernstthuer;


import jdk.nashorn.internal.codegen.CompilerConstants;
import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.special.Beta;

import static java.lang.Math.exp;
import static java.lang.Math.sqrt;
import static java.lang.Math.toRadians;

class BayesClassify {
    /**
     * Classifier for Posterior calculation based on expectations from the data
     * <p>
     * <p>
     * 3 expectations for the ratio :    0:1  0.5:0.5 and 1:0  (noise, equal expression, and noise)
     * model likelihood according to negative binomial distribution and priors according to beta dist.
     * <p>
     * alpha and beta value for the beta distribution are bimodal for noise detection, and strongly informative for ASE,
     * at an expected ratio of 0.5 , this can be influenced by the real distribution, and results in the true classification
     */


    private double alpha;
    private double beta;
    private int TrueCalls;
    private int CallsTotal;
    private BetaDistribution posterior;
    private double sigmaPosterior;


    private int expectation;

    BayesClassify(double alpha, double beta, int trueCalls, int callsTotal) {
        this.alpha = alpha;
        this.beta = beta;
        this.TrueCalls = trueCalls;
        this.CallsTotal = callsTotal;
        this.posterior = getBetaFunctionPosterior();
        this.sigmaPosterior = getSigma();

    }

    public double updateAlpha(int snpCountSoFar, double avgCoverage , boolean upside_downside ){
        // valid formulas for alpha / beta   impact on general noisyness from data,  linear function to test,
        // quadratic function
        // y = kx+d    d is minimum set to 0.1   ,
        // linear function over SNPs predicted in first draft SNP assembly and average coverage of those SNPs
        //  use quadratic to analyze impact of noise
        // use a sigmoid function to return actual alpha and beta values

        System.out.println("True");

        return 0.0;
    }


    public double calculateAlpha(int snpCountSoFar, double lenghtOfFastaFile,  double baseValue, double avgCoverage , boolean upside_downside ){
        // valid formulas for alpha / beta   impact on general noisyness from data,  linear function to test,
        // quadratic function
        // y = kx+d    d is minimum set to 0.1   ,
        // linear function over SNPs predicted in first draft SNP assembly and average coverage of those SNPs
        //  use quadratic to analyze impact of noise
        // use a sigmoid function to return actual alpha and beta values

            // early estimation of SNPs per kilobase mapped sequence  *  coverage/100
        double noisynessCoefficient = ((snpCountSoFar/lenghtOfFastaFile)*1000) * avgCoverage/100;

        // alpha should be higher if data is very messy 

        if(upside_downside) return baseValue*(avgCoverage/100) + 0.01;
        else return baseValue*(avgCoverage/100) + 0.01;

    }


    public void getVerbose(double input) {
        System.out.println("Alpha and Beta : ");
        System.out.println(posterior.getAlpha());
        System.out.println(posterior.getBeta());
        System.out.println("SupportLimits : ");

        System.out.println(posterior.getSupportLowerBound());
        System.out.println(posterior.getSupportUpperBound());
        System.out.println(posterior.logDensity(input));

    }
/*
    public boolean noiseTest(double sigmaMultiplier){
        // sigma multiplier assumes gaussian distibution. ERF could replace that
        // True if Truecalls are above threshold

        // alpha beta implementation is important,   alpha should be > 1,  beta < 0.5    depending on replicates available

        if((Math.sqrt(this.posterior.getNumericalVariance()) * sigmaMultiplier * this.CallsTotal) > this.TrueCalls){
            return false;
        }
        else{
            return true;
        }

    }




    public boolean fullSNPTest(double sigmaMultiplier){
        // sigma multiplier assumes gaussian distibution. ERF could replace that
        // True if Truecalls are below Totalcalls

        // alpha beta implementation is important,   alpha should be < 0.5 ,  beta > 1    depending on replicates available

        if((Math.sqrt(this.posterior.getNumericalVariance()) * sigmaMultiplier * this.TrueCalls) + this.TrueCalls > this.CallsTotal){
            return false;
        }
        else{
            return true;
        }
    }*/

    public boolean baseSNPTest(double sigmaMultiplier, int flag){
        // sigma multiplier assumes gaussian distibution. ERF could replace that
        // True if Truecalls are below Totalcalls
        //

        // alpha beta implementation is important,   alpha should be < 0.5 ,  beta > 1   for fullSNP  depending on replicates available
        // alpha beta implementation is important,   alpha should be > 1,  beta < 0.5  for noSNP   depending on replicates available

        if(flag == 0) {
            /*
            if ((Math.sqrt(this.posterior.getNumericalVariance()) * sigmaMultiplier * this.CallsTotal) > this.TrueCalls) {
                return false;
            } else {
                return true;
            }*/
            return (Math.sqrt(this.posterior.getNumericalVariance()) * sigmaMultiplier * this.CallsTotal) < this.TrueCalls ;
        }


        if(flag == 1) {

            return (Math.sqrt(this.posterior.getNumericalVariance()) * sigmaMultiplier * this.TrueCalls) + this.TrueCalls < this.CallsTotal;


          /*  if ((Math.sqrt(this.posterior.getNumericalVariance()) * sigmaMultiplier * this.TrueCalls) + this.TrueCalls > this.CallsTotal) {
                return false;
            } else {
                return true;
            }*/
        }
        else return false;

    }




    boolean equalAllelicTest(double logDensityThreshold){

        double logdensity = this.posterior.logDensity(0.5);
        if(logdensity > logDensityThreshold){
            return true;
        } else {return false;}
    }


    //
    private BetaDistribution getBetaFunctionPosterior() {
        double meanBeta = this.alpha / (alpha + this.beta);  // actual prior believe of theta
        double sampleSize = alpha + beta;  // directly dependent on strength of belief
        //posterior

        //double a = (TrueCalls + (meanBeta * sampleSize) - 1 );
        //double b = (CallsTotal - TrueCalls ) + (sampleSize * (1- meanBeta)) - 1;
        double a = (TrueCalls + (alpha) - 1);
        double b = (CallsTotal - TrueCalls) + (beta) - 1;

        return new BetaDistribution(a, b);
    }

    private double getSigma() {
        double meanBeta = alpha / (alpha + beta);  // actual prior believe of theta
        double sampleSize = alpha + beta;  // directly dependent on strength of belief
        double a = (TrueCalls + (meanBeta * sampleSize) - 1);
        double b = (CallsTotal - TrueCalls) + (sampleSize * (1 - meanBeta)) - 1;

        double sigma = sqrt((a * b) / ((a + b) * (a + b) * (a + b + 1)));
        double mean = a / (a + b);

        return sigma;
    }

    public double getMean() {
        double meanBeta = alpha / (alpha + beta);  // actual prior believe of theta
        double sampleSize = alpha + beta;  // directly dependent on strength of belief
        double a = (TrueCalls + (meanBeta * sampleSize) - 1);
        double b = (CallsTotal - TrueCalls) + (sampleSize * (1 - meanBeta)) - 1;

        double sigma = sqrt((a * b) / ((a + b) * (a + b) * (a + b + 1)));
        double mean = a / (a + b);

        return mean;
    }


    // the intermediate functions for priors and likelihood for testing the accuracy.
    public BetaDistribution getPrior(double alpha, double beta) {
//        int prior = 0;
        return new BetaDistribution(alpha, beta);
    }


    public BetaDistribution likelihood(double TrueCalls, double CallsTotal) {
//        int likelihood = 0;
        BetaDistribution betaDist = new BetaDistribution(TrueCalls, CallsTotal);
        return betaDist;
    }

    BetaDistribution getPosterior() {
        return posterior;
    }
    // This has to be done according to expectations of ratio and the coverage of the gene / amount of SNPs per gene...
}
