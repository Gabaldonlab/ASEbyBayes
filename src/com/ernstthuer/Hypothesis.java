package com.ernstthuer;

import java.util.ArrayList;

/**
 * Created by ethuer on 17/10/16.
 */
public class Hypothesis {

    /**
     * Seperate the hupothesis from the code,
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
     *
     *
     *
     *
     *
     */
    private double alpha;
    private double beta;

    private double culminatingProb;
    private String name;

    private ArrayList<String> affectedGenes;
    private ArrayList<Gene> geneList;

    public Hypothesis(double alpha, double beta, String name, ArrayList<Gene> geneList) {
        this.alpha = alpha;
        this.beta = beta;
        this.name = name;
        this.geneList = geneList;
        this.affectedGenes = new ArrayList<>();



    }

    public Hypothesis(double alpha, double beta, String name) {
        this.alpha = alpha;
        this.beta = beta;
        this.name = name;
        this.geneList = new ArrayList<>();
        this.affectedGenes = new ArrayList<>();
    }

    public void addGene(Gene gene){
        this.geneList.add(gene);
    }


    // evaluate geneList over their SNPs
    //


    public ArrayList<Gene> getGeneList() {
        return geneList;
    }

    public void accessSNPs(ArrayList<Gene> geneList){

        for(Gene gene:geneList){
            System.out.println(gene.getIdent());
            for(SNP snp: gene.getSnpsOnGene()){
                System.out.println(snp.getPosition());
                BayesClassify bcl = new BayesClassify(alpha, beta, snp.getALTcov(), snp.getORGcov());

                double mean = bcl.getMean();
                double sigma = bcl.getSigma();
                double logdensity = bcl.getPosterior().logDensity((alpha/(alpha+beta)));
                double ratio = (double) snp.getALTcov() / ((double)snp.getORGcov()+(double)snp.getALTcov());

                System.out.println(" ratio :" + ratio);
                if(ratio > (mean) && logdensity < 1){
                            System.out.println("Hypothesis confirmed ");
                        }
                System.out.println("Mean : "+mean + "  Sigma :" + sigma +" logDensity : "+ logdensity );
            }
        }
    }
}
