package com.ernstthuer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

class HypothesisFactory {

    /**
     * Takes the Gene lists provided by main , after read in of the individual BAM files
     * tests each SNP for the available hypothesis
     * <p>
     * Highest level for testing,  this class creates the hypothesis, and the bayesClassifiers
     * <p>
     * initiate 3 standard hypothesis,  if they explain all SNPs return the results
     * If they do not,  create more hypothesis until all SNPs are fit.
     * <p>
     * new Hypothesis are created in the following way,
     * sample 3 random unclassified SNPs
     * <p>
     * take the ratio of their expression and find the closest SNP available
     *
     *
     * implements the loop for the hypothesis for genes
     *
     *
     */

    private ArrayList<Hypothesis> testableHypothese;
    private ArrayList<Gene> geneList;
    private ArrayList<SNP> snplist;
    private int FIXED_INTENSITY = 10;

    private double modifier = 0.1;

    HypothesisFactory(ArrayList<Gene> geneList) {
        this.geneList = geneList;
        this.snplist = getSnpList();
        testableHypothese = getDefaultHypothesis();
        // aif not all SNPs are accounted for,  make more hypothesis
        for (Hypothesis hyp : testableHypothese
                ) {
            hyp.testHypothesis(geneList);

        }
        //boolean notallSNPinHypothesis = false;


        HashMap<String, Integer> availableHypothesis = getHypeNamesAsHashMap(testableHypothese);
        // This should happen before noise extension
        for (Gene gene : geneList
                ) {
            // search for synonymous SNPs
            gene.findSynonymity(0);


            testGeneForKeyHypothesis(gene, availableHypothesis);


            // keep analysis gene wise
            ArrayList<SNP> noisySNPs = getPotentiallyNoisySNPs(gene.getSnpsOnGene());
            evaluateNoisySNPlist(noisySNPs);


            for (SNP newSNP: noisySNPs
                 ) {
                int index = gene.getSnpsOnGene().indexOf(newSNP);
                if(gene.getSnpsOnGene().get(index).getHypothesisEval().containsKey("Noise")) {
                    gene.getSnpsOnGene().get(index).removeHypothesisEval("Noise");
                }
                if(gene.getSnpsOnGene().get(index).getHypothesisEval().containsKey("FullSNP")) {
                    gene.getSnpsOnGene().get(index).removeHypothesisEval("FullSNP");
                }
            }
        }
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
        for (Hypothesis hyp : testableHypothese
                ) {
            hyp.testHypothesis(geneList);
        }
        //availableHypothesis = getHypeNamesAsHashMap(testableHypothese);
        //String[] keysInHypothesis = getHypeNames(testableHypothese);
//        for(String key: availableHypothesis.keySet()){
//            System.out.println("available " + key);
//        }
        // System.out.println(gene.getHypothesisArray()[0]);
        // ToDo   implement noise reclassification by evaluating expression over replicates and synonymity / DONE
        // ToDo  Ka/Ks ratio calculation and synonymity expectation  / DONE
    }


    private ArrayList<SNP> getPotentiallyNoisySNPs(ArrayList<SNP> snpList) {
        ArrayList<SNP> lowExpressionSNPList = new ArrayList<>();

        for (SNP snp : snpList
                ) {

            if (snp.getHypothesisEval().containsKey("Noise") || snp.getHypothesisEval().containsKey("FullSNP")) {
                lowExpressionSNPList.add(snp);
            }
        }
        //System.out.println(lowExpressionSNPList.size());
        return lowExpressionSNPList;
    }


    private SNP makeSNPgreatAgain(SNP snp, double ratioALT) {
        // returns a temporary SNP with the same configuration as the input, but modified to fit the input mean of expression
        SNP outSNP = new SNP(snp.getGene(), snp.getORG(), snp.getALT(), snp.getPosition());
        outSNP.setHypothesisEval(snp.getHypothesisEval());
        outSNP.setALTcov((int) Math.round((snp.getALTcov() + snp.getORGcov()) * ratioALT));
        outSNP.setORGcov((int) Math.round((snp.getALTcov() + snp.getORGcov()) - (snp.getALTcov() + snp.getORGcov()) * ratioALT));
        //System.out.println("makeSNPgreat " +  ratioALT + " " + snp.getALTcov() + "  :  " + outSNP.getALTcov());
        return outSNP;
    }


    private void evaluateNoisySNPlist(ArrayList<SNP> snpList) {
        Hypothesis noiseHypothesis = testableHypothese.get(0); // the noise  hypothesis is at position 0
        //System.out.println(noiseHypothesis.getName());
        int count = 0;
        for (SNP snp : snpList
                ) {
            double mean = adjustSNPmeanBySynonymity(snp);
            SNP changedSNP = makeSNPgreatAgain(snp, mean);


            if (!noiseHypothesis.testSNPBCL(changedSNP) && snp.getHypothesisEval().containsKey("Noise")) {
                snp.removeHypothesisEval("Noise");
                //System.out.println(snp.getORGcov() +" " + snp.getALTcov() + " changed " + changedSNP.getORGcov() + " " + changedSNP.getALTcov() + " ");
                count++;
            }
            if (!noiseHypothesis.testSNPBCL(changedSNP) && snp.getHypothesisEval().containsKey("FullSNP")) {
                snp.removeHypothesisEval("FullSNP");
                count++;
            }
//            System.out.println("before  "  + noiseHypothesis.testSNPBCL(snp) +  "  org "  + snp.getALTcov() + " " +snp.getORGcov() );
//            System.out.println("after  "  + noiseHypothesis.testSNPBCL(changedSNP) +  "  alt  "  + changedSNP.getALTcov() + " " +changedSNP.getORGcov() );
            //System.out.println( (double) snp.getORGcov() / ((double) snp.getORGcov() + (double) snp.getALTcov())+ "  : new mean"  +  mean );
        }


        //System.out.println("[STATUs] Clearing Hypothesis for " + count + " Noisy SNPs ");
    }

    private double adjustSNPmeanBySynonymity(SNP snp) {
        // method to evaluate equal allelic expression, and generate an evaluation based on the available observations
        // First check if Equal allelic Expression exists
        // If not, test " noise SNPs "  exists in all replicates and are synonymous
        double confidenceModifier = (20.0 / 64.0);
        double expectedConfidence;

        if (snp.isSynonymous()) {
            expectedConfidence = (1 - confidenceModifier) * snp.getFoundInReplicates();  // this doubles the confidence towards the SNP if it's synonymous.
        } else {
            expectedConfidence = confidenceModifier * snp.getFoundInReplicates();
        }

        // ToDo  remove
        //System.out.println(" reevaluate " + mean + " by " + expectedConfidence);

        if (snp.getHypothesisEval().containsKey("FullSNP")) {
            return ((double) snp.getORGcov() * expectedConfidence / ((double) snp.getORGcov() * expectedConfidence + (double) snp.getALTcov()));
        } else {
            return ((double) snp.getALTcov() * expectedConfidence / ((double) snp.getORGcov() + (double) snp.getALTcov()) * expectedConfidence);
        }


    }

    private void testGeneForKeyHypothesis(Gene gene, HashMap<String, Integer> availableHypothesis) {

        HashMap<String, Integer> geneHypothesisCount = new HashMap<>();

        for (SNP snp : gene.getSnpsOnGene()
                ) {
            for (String key : availableHypothesis.keySet()) {

                // check if the hashMap contains the hypothesis, if not create
                if (!geneHypothesisCount.containsKey(key)) {
                    geneHypothesisCount.put(key, 0);
                }

                // check if the SNP contains the hypothesis, and increment
                if (snp.getHypothesisEval().containsKey(key)) {

                    int count = geneHypothesisCount.get(key);
                    count += 1;
                    geneHypothesisCount.put(key, count);
                    //int count = gene.getHypothesisEval().get(key);
                    //gene.getHypothesisEval().put(key, count + 1 );
                }
            }
        }
        gene.setHypothesisEval(geneHypothesisCount);
    }




    private HashMap<String, Integer> getHypeNamesAsHashMap(ArrayList<Hypothesis> testableHypothese) {
        HashMap<String, Integer> HypothesisNames = new HashMap<>();

        for (Hypothesis hype : testableHypothese
                ) {
            HypothesisNames.put(hype.getName(), 0);
        }
        return HypothesisNames;
    }


    private ArrayList<SNP> getSnpList() {
        ArrayList<SNP> snps = new ArrayList<>();

        for (Gene gene : this.geneList
                ) {
            snps.addAll(gene.getSnpsOnGene());
        }
        return snps;
    }

    private boolean allSNPsAccountedForByHypothesis() {
        boolean allSNPsAccountedFor = true;
        for (SNP snp : snplist
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
            if (!hasAnExplanation) {
                //System.out.println("SNP doesn't have a hypothesis " + snp.getPosition());
                allSNPsAccountedFor = false;
            }
        }
        //System.out.println(allSNPsAccountedFor);
        return allSNPsAccountedFor;
    }


    private ArrayList<Hypothesis> getDefaultHypothesis() {
        ArrayList<Hypothesis> hypothesis = new ArrayList<>();
        // mean of 0.01    and  0.99 for noise and fullSNP
        // mean of 0.5 for EAX

        double baseImpact = 0.01;

        hypothesis.add(new Hypothesis(baseImpact, FIXED_INTENSITY, "Noise"));
        hypothesis.add(new Hypothesis(1 - 0.01, FIXED_INTENSITY, "FullSNP"));
        hypothesis.add(new Hypothesis(0.5, FIXED_INTENSITY, "Equal Allelic Expression"));

        return hypothesis;
    }


    private ArrayList<Hypothesis> extendHypothesis() {
        //  If the available Hypothesis do not explain the observed SNPs, more hypothesis have to be created,
        ArrayList<Hypothesis> newHypothesis = new ArrayList<>();
        ArrayList<SNP> snpsWithoutExplanation = new ArrayList<>();
        for (SNP snp : snplist
                ) {
            boolean hasAnExplanation = false;

            for (Hypothesis hype : this.testableHypothese
                    ) {
                if (!hasAnExplanation) {
                    hasAnExplanation = hype.testSNPBCL(snp);
                }
            }
            if (!hasAnExplanation) {
                snpsWithoutExplanation.add(snp);
            }
        }
        Random randomGenerator;
        // ToDo  make this more stable, by using the mean of a few SNPs if they are close by mean
        // random snp as new center
        randomGenerator = new Random();
        int index = 0;
        if (snpsWithoutExplanation.size() > 0) {
            index = randomGenerator.nextInt(snpsWithoutExplanation.size());
        }
        if (index != 0) {
            SNP newcenter = snpsWithoutExplanation.get(index);
            double mean = ((double) newcenter.getALTcov() / ((double) newcenter.getALTcov() + (double) newcenter.getORGcov()));
            Hypothesis newHype = new Hypothesis(mean, FIXED_INTENSITY, "Allele_Specific");
            Hypothesis newHypeAntiparental = new Hypothesis((1 - mean), FIXED_INTENSITY, "Allele_Specific_AntiParental");
            newHypothesis.add(newHype);
            newHypothesis.add(newHypeAntiparental);

            return newHypothesis;
        } else {
            return null;
        }
    }

    ArrayList<Gene> getGeneList() {
        return geneList;
    }


}
