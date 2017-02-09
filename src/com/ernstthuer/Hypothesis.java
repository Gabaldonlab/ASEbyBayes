package com.ernstthuer;

import java.util.ArrayList;


class Hypothesis {

    /**
     * Separate the hypothesis from the code,
     *
     * This keeps the Bayesian classification more
     * flexible
     *
     * 3 core hypothesis with initialized alpha and beta values has to be evaluated on this
     *
     *
     * Stores the individual alpha and beta values
     *
     *
     * the 3 conditions should start with  :
     * 25:0  25:25 and 0:25 ,  most likely candidates of SNPs will call the hypothesis
     *
     *
     * needs a linear function to incorporate the observed coverage into the alpha and beta values
     */
    private double alpha;
    private double beta;

    private double culminatingProb;
    private String name;

    private ArrayList<String> affectedGenes;
    private ArrayList<Gene> geneList;

    private boolean adjustable;

    Hypothesis(double alpha, double beta, String name, ArrayList<Gene> geneList) {
        this.alpha = alpha;
        this.beta = beta;
        this.name = name;
        this.geneList = geneList;
        this.affectedGenes = new ArrayList<>();
        // immediately run the calculation
        calculateHypothisForSNPsOnGenes(geneList);
    }

    Hypothesis(double alpha, double beta, String name) {
        this.alpha = alpha;
        this.beta = beta;
        this.name = name;
        this.geneList = new ArrayList<>();
        this.affectedGenes = new ArrayList<>();
    }

    Hypothesis(double alpha, double beta) {
        this.alpha = alpha;
        this.beta = beta;
        this.name = "default";
        this.geneList = new ArrayList<>();
        this.affectedGenes = new ArrayList<>();
    }

    void addGene(Gene gene){
        this.geneList.add(gene);
    }
    // evaluate geneList over their SNPs
    //

    private double alphaBetaCorrection(int TotalCoverage, double alphaBeta){

        double mincoverage = 0.001;

        /**
         * a static alpha/ beta set value cannot classify the observed changes in coverage
         * implement a linear function for classification
         *
         * a linear function should be sufficiently complex
         *
         * y = kx+d
         *
         * d = minimum of 0.01
         * k = multiplier for presets in expectations
         *
         *
         */
       //double newBeta = mincoverage + ((beta*0.1) *TotalCoverage);
        return (mincoverage + ((alphaBeta* 0.1) *TotalCoverage));
    }


    ArrayList<Gene> getGeneList() {
        return geneList;
    }


    private void evaluateGenesForExpression(ArrayList<Gene> geneList){

        ArrayList<Double> observationsFromSNPs = new ArrayList<>();
        Double totalObserv =  0.0;

        for(Gene gene : geneList) {
            for (SNP snp : gene.getSnpsOnGene()) {

                if(snp.getHypothesisEval().containsKey(this.name)){
                    observationsFromSNPs.add(snp.getHypothesisEval().get(this.name));
                    totalObserv = ( totalObserv + snp.getHypothesisEval().get(this.name));
                }
            }
            gene.addToHypothesisEval(this.name,totalObserv);
        }
        // there should be 2x the amount of EAX snps,  since the range is 2x as high
    }

    void calculateHypothisForSNPsOnGenes(ArrayList<Gene> geneList){

        //ArrayList<Gene> affectedGenes;
        //ArrayList<SNP> affectedSNPs;

        for(Gene gene:geneList){

            //System.out.println(gene.getIdent());
            for(SNP snp: gene.getSnpsOnGene()){

                int coverage = snp.getALTcov() + snp.getORGcov();
                //int callsTotal = snp.getALTcov() + snp.getORGcov();
                BayesClassify bcl = new BayesClassify( alphaBetaCorrection(coverage, alpha), alphaBetaCorrection(coverage, beta), snp.getALTcov(), coverage);

                //double mean = bcl.getMean();
                //double sigma = bcl.getSigma();
                double logdensity = bcl.getPosterior().logDensity((alpha/(alpha+beta)));
                //double ratio = (double) snp.getALTcov() / ((double)snp.getORGcov()+(double)snp.getALTcov());


                snp.addHypothesisEval(this.name,logdensity);
            }
        }
        evaluateGenesForExpression(geneList);
    }



}
