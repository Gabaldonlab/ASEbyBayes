package com.ernstthuer;


import jdk.nashorn.internal.codegen.CompilerConstants;
import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.special.Beta;

import java.sql.BatchUpdateException;
import java.util.ArrayList;

import static java.lang.Math.*;

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
        this.CallsTotal = totalCov;
        this.TrueCalls = altCov;
        this.posterior = getBetaFunctionPosterior(altCov,totalCov);

    }

    private double[] getAlphaBeta(double mean) {
        double[] alphaBeta = new double[2];
        // alpha
        alphaBeta[0] = mean * this.FIXED_INTENSITY;
        alphaBeta[1] = FIXED_INTENSITY - alphaBeta[0];
        return alphaBeta;
    }

    boolean erfSNPTest(double threshold){
        // returns the posterior probability / probability mass function on the given ratio
        //double ratio = (double) TrueCalls  / (double) CallsTotal ;
        double ratio = this.mean;
        //System.out.println(ratio + " " + this.posterior.logDensity(ratio) + " " + Threshold);
        //System.out.println( " probability " + posterior.cumulativeProbability(ratio) + "  ratio " + ratio);
        //Returns the natural logarithm of the probability density function (PDF) of this distribution evaluated at the specified point x.
        //System.out.println("here" + this.alpha + this.beta + " " + posterior.getAlpha() + " "+ posterior.getBeta() + " ratio " + ratio);
        //System.out.println(" ratio here " + ratio + "  " + posterior.logDensity(ratio) + " alpha " + posterior.getAlpha() + " reference 0.05 " + posterior.logDensity(0.05) + "   XX   0.5 " +  posterior.logDensity(0.5) );

        try {
            return (posterior.logDensity(ratio) > threshold);
        }catch (NumberIsTooSmallException e){
            // this is always false
            return false;
        }
    }

    double erfSNPTest(double threshold, String flag){
        // returns the posterior probability / probability mass function on the given ratio
        //double ratio = (double) TrueCalls  / (double) CallsTotal ;
        double ratio = this.mean;

        try {
            return (posterior.logDensity(ratio));
        }catch (NumberIsTooSmallException e){
            // this is always false
            return 0.0;
        }
    }



    boolean baseSNPTest(double sigmaMultiplier ){
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