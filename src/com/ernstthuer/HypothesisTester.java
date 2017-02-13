package com.ernstthuer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class HypothesisTester {

    /**
     * Takes the Gene lists provided by main , after read in of the individual BAM files
     * tests each SNP for the available hypothesis
     *
     * Highest level for testing,  this class creates the hypothesis, and the bayesClassifiers
     *
     * initiate 3 standard hypothesis,  if they explain all SNPs return the results
     * If they do not,  create more hypothesis until all SNPs are fit.
     *
     * new Hypothesis are created in the following way,
     * sample 3 random unclassified SNPs
     *
     * take the ratio of their expression and find the closest SNP available
     *
     */

    private ArrayList<Hypothesis> testableHypothese;
    private ArrayList<Gene> geneList;
    private ArrayList<SNP> snplist;



    private int FIXED_INTENSITY = 10;

    HypothesisTester(ArrayList<Gene> geneList) {
        this.geneList = geneList;
        this.snplist = getSnpList();
        testableHypothese = getDefaultHypothesis();


        // aif not all SNPs are accounted for,  make more hypothesis
        for (Hypothesis hyp: testableHypothese
             ) {
            hyp.testHypothesis(geneList);
        }

//        while(! allSNPsAccountedForByHopethesis()) {
//            testableHypothese.addAll(extendHypothesis());
//            System.out.println("Hypothesis added" + testableHypothese.size());
//
//
//        }


    }

    private ArrayList<SNP> getSnpList (){
        ArrayList<SNP> snps = new ArrayList<>();

        for (Gene gene : this.geneList
                 ) {
            snps.addAll(gene.getSnpsOnGene());
        }
        return snps;
    }

    private boolean allSNPsAccountedForByHopethesis(){

        boolean allSNPsAccountedFor = false;
        for (SNP snp: snplist
             ) {
            boolean hasAnExplanation = false;

            for (Hypothesis hype : testableHypothese
                    ) {
                if (!hasAnExplanation) {
                    hasAnExplanation = hype.testSNPBCL(snp);
                }
            }

            if(! allSNPsAccountedFor){
                allSNPsAccountedFor = hasAnExplanation;
            }
        }


        return allSNPsAccountedFor;

    }


    private ArrayList<Hypothesis> getDefaultHypothesis(){
        ArrayList<Hypothesis> hypothesis = new ArrayList<>();
        // mean of 0.01    and  0.99 for noise and fullSNP
        // mean of 0.5 for EAX

        hypothesis.add(new Hypothesis(0.01,FIXED_INTENSITY ,"Noise"));
        hypothesis.add(new Hypothesis(1,FIXED_INTENSITY ,"FullSNP"));
        hypothesis.add(new Hypothesis(0.5,FIXED_INTENSITY ,"Equal Allelic Expression"));

        return hypothesis;
    }


    private ArrayList<Hypothesis> extendHypothesis (){
        ArrayList<Hypothesis> newHypothesis = new ArrayList<>();
        ArrayList<SNP> snpsWithoutExplanation = new ArrayList<>();
        for (SNP snp: snplist
             ) {

            boolean hasAnExplanation = false;

            for (Hypothesis hype: testableHypothese
                 ) {
                if (!hasAnExplanation) {
                    hasAnExplanation = hype.testSNPBCL(snp);
                }
            }
            if(!hasAnExplanation){
                snpsWithoutExplanation.add(snp);
            }
        }


        Random randomGenerator;
        // random snp as new center
        randomGenerator = new Random();
        int index = randomGenerator.nextInt(snpsWithoutExplanation.size());
        SNP newcenter = snpsWithoutExplanation.get(index);

        double mean = (newcenter.getALTcov() /(newcenter.getALTcov()+newcenter.getORGcov()));

        Hypothesis newHype = new Hypothesis(mean,FIXED_INTENSITY,"Allele_Specific");
        Hypothesis newHypeAntiparental = new Hypothesis((1-mean),FIXED_INTENSITY,"Allele_Specific_AntiParental");


        newHypothesis.add(newHype);
        newHypothesis.add(newHypeAntiparental);

        return newHypothesis;
    }

    public ArrayList<Gene> getGeneList() {
        return geneList;
    }
}
