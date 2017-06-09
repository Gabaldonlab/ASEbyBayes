package com.ernstthuer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * Created by ethur on 6/9/17.
 */
public class GeneEvaluator {

    /**
     * Takes the information on the gene, and tests it against the available hypothesis
     *
     * Accumulates the evaluation score on each SNP
     *
     *
     *
     */


    private ArrayList<Hypothesis> hypothesises;
    private ArrayList<Double> scoring = new ArrayList<>();
    private Hypothesis result;
    private Gene gene;

    public GeneEvaluator(ArrayList<Hypothesis> hypothesises, Gene gene) {
        this.hypothesises = hypothesises;
        this.gene = gene;
    }



    private void accumulateSNPdata (){

        HashMap<String, Double> hypothesisEval = new HashMap<>();
        for (Hypothesis hype: hypothesises
             ) {

            // initialize empty
            hypothesisEval.put(hype.getName(),0.0);

        }

        for (SNP snp: gene.getSnpsOnGene()
             ) {
            for (Hypothesis hype : hypothesises
                 ) {
                // update observations per SNP
                hypothesisEval.put(hype.getName(), hypothesisEval.get(hype.getName()) + hype.testSNPBCL(snp,"quantitiative"));
            }
        }
    }

    private ResultHypothesis findMajorityHypothesis(HashMap<String,Double> hypothesisEval) {

        double maxval = 0.0;
        double accumulativeValue = 0.0;
        String bestHype = "";

        // this finds the last highest entry  What if they are equally likely ?  get the global likelihood for EAX vs ASE correct by global prob, it's bayesian after all
        for (Map.Entry<String, Double> entry : hypothesisEval.entrySet()) {

            accumulativeValue += entry.getValue();

            if(hypothesisEval.get(entry.getKey()) > maxval){
                maxval = hypothesisEval.get(entry.getKey());
                bestHype= entry.getKey();
            }

            // check a seperate loop for equally likely hypes,  if so, for the moment don't give a result
            if(hypothesisEval.get(entry.getKey()) == maxval  && maxval > 0.0 ) {
                maxval = hypothesisEval.get(entry.getKey());
                return new ResultHypothesis(gene.getIdent(),maxval,"UnclearResult", accumulativeValue);
            }
        }

        return new ResultHypothesis(gene.getIdent(),maxval,bestHype, accumulativeValue);
    }


    private void checkForEqualLikelihoods(HashMap<String,Double> hypothesisEval , HashMap<String,Double> globalProbabilities){

        double maxval = 0.0;
        String bestHype = "";

        for (Map.Entry<String, Double> entry : hypothesisEval.entrySet()) {

            if(hypothesisEval.get(entry.getKey()) > maxval){
                maxval = hypothesisEval.get(entry.getKey());
            }
        }
    }


    private ArrayList<SNP> returnPotentialContamination (ResultHypothesis resultHype) {

        // goes through the list of SNPs available, and
        ArrayList<SNP> snpsThatCouldBeContaminants = new ArrayList<>();

        String majorHype = resultHype.getName();

        for (SNP snp: gene.getSnpsOnGene()
                ) {
            double maxval = 0.0;
            String bestHypothesis = "";

            for (Hypothesis hype : hypothesises
                    ) {


                if(hype.testSNPBCL(snp,"quantitiative") > maxval){
                    maxval = hype.testSNPBCL(snp,"quantitiative");
                    bestHypothesis = hype.getName();
                }
                // update observations per SNP
            }

            if(! bestHypothesis.equals(majorHype)){

                snpsThatCouldBeContaminants.add(snp);

            }

        }


        return snpsThatCouldBeContaminants;

    }



}
