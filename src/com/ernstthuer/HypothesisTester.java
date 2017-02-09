package com.ernstthuer;

import java.util.ArrayList;
import java.util.List;

class HypothesisTester {

    /**
     * Takes the Gene lists provided by main , after read in of the individual BAM files
     * tests each SNP for the available hypothesis
     */

    private ArrayList<Hypothesis> testableHypothese;
    private ArrayList<Gene> geneList;
    private ArrayList<Gene> affectedGenes;
    private ArrayList<Gene> unAffectedGenes;


    HypothesisTester(ArrayList<Gene> geneList) {

        this.testableHypothese = new ArrayList<>();
        this.geneList = geneList;
        this.affectedGenes = new ArrayList<>();
        this.unAffectedGenes = new ArrayList<>();

        // initiate with base hypotesis,  add more in case none fit.
        // Information is stored on the SNPs themselves,  but maybe also on gene Level
        this.testableHypothese.add(createZeroHypothesis(this.geneList));
        this.testableHypothese.add(createASEHypothesis(this.geneList));
        this.testableHypothese.add(createFullSNPHypothesis(this.geneList));
        geneWiseComparison(this.geneList);
    }

    // the three default hypothesis

    private Hypothesis createZeroHypothesis(ArrayList<Gene> geneList) {

        return new Hypothesis(0.1, 10, "NoiseHyp", geneList);
    }

    private Hypothesis createASEHypothesis(ArrayList<Gene> geneList) {
        return new Hypothesis(5, 5, "EqualAllelicExpression", geneList);
    }

    private Hypothesis createFullSNPHypothesis(ArrayList<Gene> geneList) {
        return new Hypothesis(10, 0.1, "FullSNPExpression", geneList);
    }




    ArrayList<Gene> getGeneList() {
        return geneList;
    }


    void geneWiseComparison(ArrayList<Gene> geneList) {

        for (Gene gene : geneList) {

        /*    double ASEexpression = 0;
            double FullSNPs = 0;
            double NoHypothesisAssembled = 0;
            double total = 0;
            */
            /**
            // noise is deleted here
            //  0:total,   1:FullSNP,  2: EqualAllelic   3:NoHyothsis
            */
            double[] checkedhypothesis = new double[4];


            for (SNP snp : gene.getSnpsOnGene()) {

                checkedhypothesis[0]+=1;

                if (snp.getHypothesisEval().containsKey("FullSNPExpression")) {
                    //FullSNPs += 1;
                    checkedhypothesis[1]+=1;
                }

                if (snp.getHypothesisEval().containsKey("EqualAllelicExpression")) {
                    //ASEexpression += 1;
                    checkedhypothesis[2]+=1;
                    //System.out.println(snp.getORGcov() + "  " + snp.getALTcov() + "  " + snp.getFoundInReplicates() );
                }

                if (snp.getHypothesisEval().isEmpty()) {
                    //NoHypothesisAssembled += 1;
                    checkedhypothesis[3]+=1;
                }
            }
            gene.addHypothesisCount(checkedhypothesis);
            //System.out.println("Gene : " + gene.getIdent() +" with " + gene.getSnpsOnGene().size() + " : " + total + " total snps contains " + FullSNPs +" full " + ASEexpression +" half " + NoHypothesisAssembled +" unknown SNPs"   );

        }
    }

    private Hypothesis generateHypothesis(ArrayList<SNP> indicatorSNPs){

        // THis method takes a list of related SNPs,  possibly a subset of the unclassified, and creates a hypothesis that fits


        double fixedRatio = 10.0; // ToDo currently alpha + beta = 10    that can be more flexible,  make depth dependant
        // mean  = alpha / (alpha + beta)
        // make inversible
        // make adjustable
        // allow only one per gene

        // get SNP, assume it's mean of the distribution,  fit new distribution from mean

        ArrayList<Double> ratioList = new ArrayList<>() ;

        for (SNP indicator: indicatorSNPs
             ) {
            ratioList.add(((double)indicator.getALTcov()/((double)indicator.getORGcov() + (double)indicator.getALTcov())));

        }

        double ratio = calculateAverage(ratioList);

        // ratio  = alpha / 10
        // alpha = ratio * 10
        // beta = 10 - alpha
        double alpha = ratio*10;
        double beta = 10-alpha;






        Hypothesis morehype = new Hypothesis(alpha, beta,"AlleleSpecificExpression" );

        return morehype;
    }

    private double calculateAverage(List <Double> ratios) {
        double sum = 0;
        if(!ratios.isEmpty()) {
            for (double ratio : ratios) {
                sum += ratio;
            }
            return sum / ratios.size();
        }
        return sum;
    }



}
