package com.ernstthuer;

/**
 * Created by ethur on 6/9/17.
 *
 * Storage for Gene evaluation
 *
 */
final class ResultHypothesis {

    private String gene;
    private double prob;
    private String name;
    private double accumulativeValue;

    ResultHypothesis(String gene, double prob, String name, double relativeToTotal) {
        this.gene = gene;
        this.prob = prob;
        this.name = name;
        this.accumulativeValue = relativeToTotal;
    }

    Double getProb() {
        return prob;
    }

    String getName() {
        return name;
    }

    String getGene() {
        return gene;
    }

    public double getRelativeToTotal() {
        return accumulativeValue;
    }
}
