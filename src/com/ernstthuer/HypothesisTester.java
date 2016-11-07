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


    public HypothesisTester( ArrayList<Gene> geneList) {
        this.testableHypothese = new ArrayList<>();
        this.geneList = geneList;

        // initiate with base hypotesis,  add more in case none fit.

        this.testableHypothese.add(createZeroHypothesis ());
        this.testableHypothese.add(createASEHypothesis ());
        this.testableHypothese.add(createFullSNPHypothesis ());

    }



    public Hypothesis createZeroHypothesis (){

        Hypothesis zero = new Hypothesis(0.1,10,"NoiseHyp");

     return zero ;
    }

    public Hypothesis createASEHypothesis (){

        Hypothesis ASEhypothesis = new Hypothesis(5,5,"EqualAllelicExpression");

        return ASEhypothesis ;
    }


    public Hypothesis createFullSNPHypothesis (){

        Hypothesis fullSNPhypothesis = new Hypothesis(10,0.1,"FullSNPExpression");

        return fullSNPhypothesis ;
    }


}
