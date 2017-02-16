package com.ernstthuer;

import java.util.ArrayList;
import java.util.HashMap;
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
        for (Hypothesis hyp : testableHypothese
                ) {
            hyp.testHypothesis(geneList);
        }
        //boolean notallSNPinHypothesis = false;

        int maxHypothesis = 4;

        for (int i = 0; i < maxHypothesis; i++) {
            if (!allSNPsAccountedForByHypothesis()) {
                ArrayList<Hypothesis> newHypes = extendHypothesis();
                try {
                    if (newHypes.size() > 0) {
                        this.testableHypothese.addAll(newHypes);
                    }
                    //System.out.println("Hypothesis added" + this.testableHypothese.size());
                } catch (NullPointerException e) {
                    // empty list has no value,  and causes no problem
                }
            }
        }

        //String[] keysInHypothesis = getHypeNames(testableHypothese);

        HashMap<String, Integer> availableHypothesis = getHypeNamesAsHashMap(testableHypothese);

//        for(String key: availableHypothesis.keySet()){
//            System.out.println("available " + key);
//        }


        for (Hypothesis hyp : testableHypothese
                ) {
            hyp.testHypothesis(geneList);
        }


        for (Gene gene: geneList
             ) {
            gene.findSynonymity(0);
            testGeneForKeyHypothesis(gene, availableHypothesis);

            // keep analysis gene wise
            ArrayList<SNP> noisySNPs = getPotentiallyNoisySNPs(gene.getSnpsOnGene());

            evaluateNoisySNPlist(noisySNPs);




        }
            //System.out.println(gene.getHypothesisArray()[0]);

        // ToDo   implement noise reclassification by evaluating expression over replicates and synonymity




        // ToDo  Ka/Ks ratio calculation and synonymity expectation


    }


    private ArrayList<SNP> getPotentiallyNoisySNPs(ArrayList<SNP> snpList){
        ArrayList<SNP> lowExpressionSNPList =  new ArrayList<>();

        for (SNP snp: snpList
             ) {

            if(snp.getHypothesisEval().containsKey("Noise") || snp.getHypothesisEval().containsKey("FullSNP")){
                lowExpressionSNPList.add(snp);
            }
        }
        return lowExpressionSNPList;
    }




    private void evaluateNoisySNPlist(ArrayList<SNP> snpList){
        for (SNP snp : snpList
             ) {
            double mean = checkLowExpressionSNPs(snp);

        }
    }

    private double checkLowExpressionSNPs(SNP snp){
        // method to evaluate equal allelic expression, and generate an evaluation based on the available observations
        // First check if Equal allelic Expression exists
        // If not, test " noise SNPs "  exists in all replicates and are synonymous
        double confidenceModifier = (20.0/64.0);
        double expectedConfidence;

        if(snp.isSynonymous()){
            expectedConfidence = 1+(confidenceModifier)*snp.getFoundInReplicates();
        }else{
            expectedConfidence = confidenceModifier*snp.getFoundInReplicates();
        }
        double mean = (double) snp.getALTcov() / ((double) snp.getORGcov() + (double) snp.getALTcov());

        // ToDo  remove
        System.out.println(" reevaluate " + mean + " by " + expectedConfidence);

        if(snp.getHypothesisEval().containsKey("FullSNP")){
            return ((double) snp.getORGcov() / ((double) snp.getORGcov() + (double) snp.getALTcov()) * expectedConfidence);
        }

        else {
            return ((double) snp.getALTcov() / ((double) snp.getORGcov() + (double) snp.getALTcov()) * expectedConfidence);
        }


    }

    private void testGeneForKeyHypothesis(Gene gene,  HashMap<String, Integer> availableHypothesis){

        HashMap<String, Integer> geneHypothesisCount = new HashMap<>();



        for (SNP snp: gene.getSnpsOnGene()
             ) {
            for (String key : availableHypothesis.keySet()) {

                // check if the hashMap contains the hypothesis, if not create
                if(!geneHypothesisCount.containsKey(key)){
                    geneHypothesisCount.put(key,0);
                }

                // check if the SNP contains the hypothesis, and increment
                if (snp.getHypothesisEval().containsKey(key)) {

                    int count = geneHypothesisCount.get(key);
                    count +=1;
                    geneHypothesisCount.put(key, count);
                    //int count = gene.getHypothesisEval().get(key);
                    //gene.getHypothesisEval().put(key, count + 1 );
                }
            }
        }
        gene.setHypothesisEval(geneHypothesisCount);
    }


    private HashMap<String,Integer> getHypeNamesAsHashMap(ArrayList<Hypothesis> testableHypothese){
        HashMap<String,Integer> HypothesisNames = new HashMap<>();

        for (Hypothesis hype: testableHypothese
                ) {
            HypothesisNames.put(hype.getName(),0);
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
        //System.out.println(allSNPsAccountedFor);
        return allSNPsAccountedFor;
    }


    private ArrayList<Hypothesis> getDefaultHypothesis(){
        ArrayList<Hypothesis> hypothesis = new ArrayList<>();
        // mean of 0.01    and  0.99 for noise and fullSNP
        // mean of 0.5 for EAX

        double baseImpact = 0.01;

        hypothesis.add(new Hypothesis(baseImpact,FIXED_INTENSITY ,"Noise"));
        hypothesis.add(new Hypothesis(1-0.01,FIXED_INTENSITY ,"FullSNP"));
        hypothesis.add(new Hypothesis(0.5,FIXED_INTENSITY ,"Equal Allelic Expression"));

        return hypothesis;
    }


    private ArrayList<Hypothesis> extendHypothesis (){
        //  If the available Hypothesis do not explain the observed SNPs, more hypothesis have to be created,
        ArrayList<Hypothesis> newHypothesis = new ArrayList<>();
        ArrayList<SNP> snpsWithoutExplanation = new ArrayList<>();
        for (SNP snp: snplist
             ) {
            boolean hasAnExplanation = false;

            for (Hypothesis hype: this.testableHypothese
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
        // ToDo  make this more stable, by using the mean of a few SNPs if they are close by mean 
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
