package com.ernstthuer;

import java.util.ArrayList;

/**
 * Created by ethur on 11/7/16.
 */
public class HypothesisTester {

    /**
     * Combining the Hypothesis create a
     *
     *
     *
     *
     *
     */


    private ArrayList<Hypothesis> testableHypothese ;
    private ArrayList<Gene>  geneList ;
    private ArrayList<Gene> affectedGenes;
    private ArrayList<Gene> unAffectedGenes;


    public HypothesisTester( ArrayList<Gene> geneList) {

        this.testableHypothese = new ArrayList<>();
        this.geneList = geneList;
        this.affectedGenes = new ArrayList<>();
        this.unAffectedGenes = new ArrayList<>();

        // initiate with base hypotesis,  add more in case none fit.


        // Information is stored on the SNPs themselves,  but maybe also on gene Level
        this.testableHypothese.add(createZeroHypothesis (this.geneList));
        this.testableHypothese.add(createASEHypothesis (this.geneList));
        this.testableHypothese.add(createFullSNPHypothesis (this.geneList));

    }



    public Hypothesis createZeroHypothesis (ArrayList<Gene> geneList){

        Hypothesis zero = new Hypothesis(0.1,10,"NoiseHyp",geneList);

     return zero ;
    }

    public Hypothesis createASEHypothesis (ArrayList<Gene> geneList){

        Hypothesis ASEhypothesis = new Hypothesis(5,5,"EqualAllelicExpression",geneList);

        return ASEhypothesis ;
    }


    public Hypothesis createFullSNPHypothesis (ArrayList<Gene> geneList){

        Hypothesis fullSNPhypothesis = new Hypothesis(10,0.1,"FullSNPExpression",geneList);

        return fullSNPhypothesis ;
    }


}
