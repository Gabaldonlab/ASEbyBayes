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
        //boolean notallSNPinHypothesis = false;

        System.out.println("Check " +allSNPsAccountedForByHypothesis());

        int maxHypothesis = 4;
        for(int i = 0; i < maxHypothesis;i++){
            if(! allSNPsAccountedForByHypothesis()) {
                ArrayList<Hypothesis> newHypes = extendHypothesis();
                try {
                    if ( newHypes.size() > 0) {
                        this.testableHypothese.addAll(newHypes);
                    }
                    System.out.println("Hypothesis added" + this.testableHypothese.size());
                }catch (NullPointerException e ){
                    // empty list has no value,  and causes no problem
                }
            }
        }

        String[] keysInHypothesis =  getHypeNames(testableHypothese);


        for (Gene gene : geneList
             ) {
            for (SNP snp : gene.getSnpsOnGene()
                 ) {
                if(snp.getHypothesisEval().containsKey("Noise")){
                    gene.setSnpsOnGene(dropSNP(gene.getSnpsOnGene(),snp));
                }

                // Removed noise hypothesis from index 1  get estimations for the other hypothesis, abundance etc.
                for(int i = 1; i<(keysInHypothesis.length - 1) ; i++ ){
                    System.out.println();
                }

                //System.out.println(snp.getHypothesisEval().containsKey("Noise"));
            }
            //System.out.println(gene.getHypothesisArray()[0]);




        }
    }


    private String[] getHypeNames(ArrayList<Hypothesis> testableHypothese){
        String[] HypothesisNames = new String[testableHypothese.size()];

        int index = 0;
        for (Hypothesis hype: testableHypothese
             ) {
            HypothesisNames[index] = hype.getName();
            index++;
        }

     return HypothesisNames;
    }

    private ArrayList<SNP> dropSNP(ArrayList<SNP> snplist, SNP snp){
        int index = snplist.indexOf(snp);
        snplist.remove(index);
        return snplist;
    }


    private ArrayList<SNP> getSnpList (){
        ArrayList<SNP> snps = new ArrayList<>();

        for (Gene gene : this.geneList
                 ) {
            snps.addAll(gene.getSnpsOnGene());
        }
        return snps;
    }

    private boolean allSNPsAccountedForByHypothesis(){
        boolean allSNPsAccountedFor = true;
        for (SNP snp: snplist
             ) {

            //System.out.println(snp.getPosition());

            boolean hasAnExplanation = false;

            for (Hypothesis hype : testableHypothese
                    ) {
                if (!hasAnExplanation) {
                    hasAnExplanation = hype.testSNPBCL(snp);
                    //System.out.println(" snp explanation " + hasAnExplanation + "  " + hype.getName());
                }
            }
            if(! hasAnExplanation){
                //System.out.println("SNP doesn't have a hypothesis " + snp.getPosition());
                allSNPsAccountedFor = false;
            }
        }
        System.out.println(allSNPsAccountedFor);
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

            for (Hypothesis hype: this.testableHypothese
                 ) {
                if (!hasAnExplanation) {
                    //System.out.println(hype.testSNPBCL(snp));
                    hasAnExplanation = hype.testSNPBCL(snp);
                }
            }
            if(!hasAnExplanation){
                snpsWithoutExplanation.add(snp);
                //System.out.println("added snp to list " + snpsWithoutExplanation.size());
            }

        }
        System.out.println(snpsWithoutExplanation.size() + " snps unaccounted for");
        Random randomGenerator;
        // random snp as new center
        randomGenerator = new Random();
        int index = 0;
        if(snpsWithoutExplanation.size() > 0) {
            index = randomGenerator.nextInt(snpsWithoutExplanation.size());
        }
        if(index != 0) {
            SNP newcenter = snpsWithoutExplanation.get(index);
            double mean = ((double) newcenter.getALTcov() / ((double) newcenter.getALTcov() + (double) newcenter.getORGcov()));
            Hypothesis newHype = new Hypothesis(mean, FIXED_INTENSITY, "Allele_Specific");
            Hypothesis newHypeAntiparental = new Hypothesis((1 - mean), FIXED_INTENSITY, "Allele_Specific_AntiParental");
            newHypothesis.add(newHype);
            newHypothesis.add(newHypeAntiparental);

            return newHypothesis;
        }else{
            return null;
        }
    }

    ArrayList<Gene> getGeneList() {
        return geneList;
    }
}
