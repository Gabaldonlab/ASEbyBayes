package com.ernstthuer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by ethur on 6/9/17.
 */
class GeneEvaluator {

    /**
     * Takes the information on the gene, and tests it against the available hypothesis
     *
     * Accumulates the evaluation score on each SNP
     *
     */


    private ArrayList<Hypothesis> hypothesises;
    private ArrayList<Double> scoring = new ArrayList<>();
    private Gene gene;
    private ResultHypothesis result;

    GeneEvaluator(ArrayList<Hypothesis> hypothesises, Gene gene) {
        this.hypothesises = hypothesises;
        this.gene = gene;
        HashMap<String, Double> hypeEval =  accumulateSNPdata();

        // ToDo  validate, if SNP hypothesis are stored accurately
        result = findMajorityHypothesis(gene);

    }

    ResultHypothesis getResult() {
        return result;
    }

    private HashMap<String, Double>  accumulateSNPdata (){

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
                double currentobs = hype.testSNPBCL(snp,"quantitiative");
                if((currentobs) >= 0.0 ) {
                    double total = hypothesisEval.get(hype.getName()) + currentobs;
                    hypothesisEval.put(hype.getName(), (hypothesisEval.get(hype.getName()) + currentobs));
                }
            }
            snp.setHypothesisEval(hypothesisEval);
        }
        return hypothesisEval;
    }

    private ResultHypothesis findMajorityHypothesis(Gene gene) {


        if(gene.getSnpsOnGene().size() > 0) {

            // this finds the last highest entry  What if they are equally likely ?  get the global likelihood for EAX vs ASE correct by global prob, it's bayesian after all


            // collect the observations from SNPs here
            HashMap<String, Double> geneHypothesis = new HashMap<>();

            double maxval = 0.0;
            double accumulativeValue = 0.0;
            String bestHype = "";

            for (SNP snp : gene.getSnpsOnGene()
                    ) {
                HashMap<String, Double> hypes = snp.getHypothesisEval();
                Map.Entry<String, Double> maxEntry = null;

                double maxSNP = 0.0;
                for (Map.Entry<String, Double> entry : hypes.entrySet()) {
                    if (maxSNP == 0.0 || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
                        maxEntry = entry;
                        maxSNP = entry.getValue();
                    }

                }

                if (!geneHypothesis.containsKey(maxEntry.getKey())) {
                    geneHypothesis.put(maxEntry.getKey(), maxEntry.getValue());
                }
                if (geneHypothesis.containsKey(maxEntry.getKey())) {
                    double current = (geneHypothesis.get(maxEntry.getKey()) + maxEntry.getValue());
                    geneHypothesis.put(maxEntry.getKey(), current);
                }

            }

            Map.Entry<String, Double> maxHypothesis = null;
            double accumulatedValue = 0.0;
            double maxHype = 0.0;
            for (Map.Entry<String, Double> entry : geneHypothesis.entrySet()) {
                accumulatedValue = accumulativeValue + entry.getValue();
                if (maxHype == 0.0 || entry.getValue().compareTo(maxHypothesis.getValue()) > 0) {
                    maxHypothesis = entry;
                    maxHype = entry.getValue();

                }
            }

            return new ResultHypothesis(gene.getIdent(), maxHypothesis.getValue(), maxHypothesis.getKey(), accumulatedValue);
        }
        else return null;
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


    @Override
    public String toString() {
        return gene.getChromosome() + "\t" + gene.getIdent() + "\t" + result.getName() + "\t" + result.getProb() + "\t" + result.getRelativeToTotal()  ;
    }
}
