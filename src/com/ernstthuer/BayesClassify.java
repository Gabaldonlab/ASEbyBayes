package com.ernstthuer;


import jdk.nashorn.internal.codegen.CompilerConstants;
import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.special.Beta;

import java.sql.BatchUpdateException;
import java.util.ArrayList;

import static java.lang.Math.exp;
import static java.lang.Math.sqrt;
import static java.lang.Math.toRadians;

class BayesClassify {
/**
 * Classifier for Posterior calculation based on expectations from the data
 * 3 expectations for the ratio :    0:1  0.5:0.5 and 1:0  (noise, equal expression, and noise)
 * model likelihood according to negative binomial distribution and priors according to beta dist.
 * <p>
 * alpha and beta value for the beta distribution are bimodal for noise detection, and strongly informative for ASE,
 * at an expected ratio of 0.5 , this can be influenced by the real distribution, and results in the true classification
 */


    private double FIXED_INTENSITY ;
    private double mean;
    private double alpha;
    private double beta;
    private int CallsTotal;
    private int TrueCalls;
    private BetaDistribution posterior;

    BayesClassify(double mean, double FIXED_INTENSITY,  int altCov, int totalCov) {
        this.mean = mean;
        this.FIXED_INTENSITY = FIXED_INTENSITY;
        this.alpha = getAlphaBeta(mean)[0];
        this.beta = getAlphaBeta(mean)[1];

        this.posterior = getBetaFunctionPosterior(altCov,totalCov);

    }




    private double[] getAlphaBeta(double mean) {
        double[] alphaBeta = new double[2];
        // alpha
        alphaBeta[0] = mean*this.FIXED_INTENSITY;
        alphaBeta[1] = FIXED_INTENSITY - alphaBeta[0];
        return alphaBeta;
    }


    boolean baseSNPTest(double sigmaMultiplier, int CallsTotal, int TrueCalls){
        // sigma multiplier assumes gaussian distibution, testing for confidence interval around mean. ERF could replace that
        // True if Truecalls are below Totalcalls
        //

        // Testing if Truecalls is bigger than the modified total of observations


        return (Math.sqrt(this.posterior.getNumericalVariance()) * sigmaMultiplier * CallsTotal) < TrueCalls  ;

        // alpha beta implementation is important,   alpha should be < 0.5 ,  beta > 1   for fullSNP  depending on replicates available
        // alpha beta implementation is important,   alpha should be > 1,  beta < 0.5  for noSNP   depending on replicates available

        //if(flag == 0) {
            /*
            if ((Math.sqrt(this.posterior.getNumericalVariance()) * sigmaMultiplier * this.CallsTotal) > this.TrueCalls) {
                return false;
            } else {
                return true;
            }*/
        //    return (Math.sqrt(this.posterior.getNumericalVariance()) * sigmaMultiplier * CallsTotal) < TrueCalls ;
        //}


        //if(flag == 1) {

        //    return (Math.sqrt(this.posterior.getNumericalVariance()) * sigmaMultiplier * TrueCalls) + TrueCalls < CallsTotal;


          /*  if ((Math.sqrt(this.posterior.getNumericalVariance()) * sigmaMultiplier * this.TrueCalls) + this.TrueCalls > this.CallsTotal) {
                return false;
            } else {
                return true;
            }*/
        //}
        //else return false;

    }


    boolean erfSNPTest( int CallsTotal, int TrueCalls){
        this.posterior.logDensity((double)CallsTotal/(double) TrueCalls);
        return false;
    }


    boolean baseSNPTest(double sigmaMultiplier){
        return (Math.sqrt(this.posterior.getNumericalVariance()) * sigmaMultiplier * CallsTotal) < TrueCalls;
    }


    private BetaDistribution getBetaFunctionPosterior(int TrueCalls,int CallsTotal) {
        double meanBeta = this.alpha / (alpha + this.beta);  // actual prior believe of theta
        double sampleSize = alpha + beta;  // directly dependent on strength of belief
        //posterior

        //double a = (TrueCalls + (meanBeta * sampleSize) - 1 );
        //double b = (CallsTotal - TrueCalls ) + (sampleSize * (1- meanBeta)) - 1;
        double a = (TrueCalls + (alpha) - 1);
        double b = (CallsTotal - TrueCalls) + (beta) - 1;

        return new BetaDistribution(a, b);
    }

}