package com.ernstthuer;

import org.junit.Before;

import java.util.ArrayList;
import java.util.Random;

public class HypothesisTest {

    private int length = 500;


    private static ArrayList<SNP> snpList = new ArrayList<>();
    //public String gene = "Testgene";
    private Gene testgene = new Gene("CHr1", 0, length, "+");
    //private Gene testgene2 = new Gene("CHr1",0,length,"+");
    private Random randomGenerator = new Random();
    // make alpha a linear function of coverage


    private Hypothesis hypothesis = new Hypothesis(0.1, 10, "NoiseHyp");
    private Hypothesis hypothesisEAX = new Hypothesis(5, 5, "EqualAllelicExpression");
    private Hypothesis hypothesisFullSNP = new Hypothesis(10, 0.1, "FullSNPcall");

    @Before
    public void createData() {

        for (int i = 0; i < length; i++) {
            SNP snp = new SNP(testgene, 'A', i);
            snp.setALTcov(i);
            snp.setORGcov(length - i);
            snpList.add(snp);
            testgene.addSNP(snp, false);
        }

        ArrayList<Gene> geneList = new ArrayList<>();
        geneList.add(testgene);

        HypothesisFactory hypothesisFactory = new HypothesisFactory(geneList);

    }






/*




    @Test
    public void EvaluateMultipleTestInteractions() throws Exception {
        System.out.println("Third test");
        hypothesis.calculateHypothisForSNPsOnGenes(hypothesis.getGeneList());
        hypothesisEAX.calculateHypothisForSNPsOnGenes(hypothesisEAX.getGeneList());
        hypothesisFullSNP.calculateHypothisForSNPsOnGenes(hypothesisFullSNP.getGeneList());

        for(SNP snp: testgene.getSnpsOnGene()){
            System.out.println("SNP classify " + snp.getHypothesisEval());
        }
    }
*/

//
//    @Test
//    public void classifyGenes() throws Exception {
//        System.out.println("Third test");
//        hypothesis.calculateHypothisForSNPsOnGenes(hypothesis.getGeneList());
//
//
//        hypothesisEAX.calculateHypothisForSNPsOnGenes(hypothesisEAX.getGeneList());
//        hypothesisFullSNP.calculateHypothisForSNPsOnGenes(hypothesisFullSNP.getGeneList());
//
//        //for(SNP snp: testgene.getSnpsOnGene()){
//        //    System.out.println("SNP classify " + snp.getHypothesisEval());
//        //}
//
//        System.out.println(testgene.getHypothesisEval().size());
//        Iterator it = testgene.getHypothesisEval().entrySet().iterator();
//        while (it.hasNext()) {
//            System.out.println(it.next());
//        }
//
//
//    }


}