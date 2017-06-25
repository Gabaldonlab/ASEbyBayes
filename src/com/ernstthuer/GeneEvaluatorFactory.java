package com.ernstthuer;

import java.util.ArrayList;

/**
 * Created by ethur on 6/9/17.
 */
class GeneEvaluatorFactory {

    // no need to store the evaluators
    //ArrayList<GeneEvaluator> geneEvaluators = new ArrayList<>();


    ArrayList<ResultHypothesis> resultHypos = new ArrayList<>();
    ArrayList<Gene> geneArrayList;
    ArrayList<Hypothesis> hypothesisList;

    GeneEvaluatorFactory(ArrayList<Gene> geneArrayList, ArrayList<Hypothesis> hypothesisList) {
        this.geneArrayList = geneArrayList;
        this.hypothesisList = hypothesisList;
        TestGenes();
    }

    private void TestGenes(){
        for (Gene gene: geneArrayList
             ) {
            GeneEvaluator geEv = new GeneEvaluator(hypothesisList,gene);
            resultHypos.add(geEv.getResult());
        }

    }


    public ArrayList<ResultHypothesis> getResultHypothesis(){return this.resultHypos; }



}
