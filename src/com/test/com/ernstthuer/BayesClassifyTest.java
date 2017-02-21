package com.ernstthuer;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;


public class BayesClassifyTest {

    private BayesClassify bcl = new BayesClassify(0.1,10,100,1000);
    private ArrayList<Gene> genelist = new ArrayList<>();
    // test file 1
    @Before
    public void getNumbers(){
        int snpcount = 200;
        // empty the list and start new
        genelist = new ArrayList<>();
        Gene gene = new Gene("chr1",0,1000000,"Gene1");

        //ArrayList<SNP> snps = new ArrayList<>();
        for(int i = 0; i <= snpcount ; i++){
            SNP snp1 = new SNP(gene,'A','T',(i*2));
            snp1.setALTcov(i);
            snp1.setORGcov(snpcount-i);

            snp1.setFoundInReplicates(3);
            snp1.setSynonymous(true);
            //snps.add(snp1);
            gene.addSNP(snp1, false);
        }
        genelist.add(gene);

        Gene gene2 = new Gene("chr1",1000000,2000000,"Gene2");

        //ArrayList<SNP> snps = new ArrayList<>();
        for(int i = 0; i <= snpcount ; i++){
            SNP snp1 = new SNP(gene2,'A','T',((i*2)+100000));
            snp1.setALTcov(i);
            snp1.setORGcov(snpcount-i);
            snp1.setFoundInReplicates(3);
            snp1.setSynonymous(true);

            //snps.add(snp1);
            gene2.addSNP(snp1, false);
        }
        genelist.add(gene2);

    }


    @Test
    public void testSNPclassify() throws Exception{
        System.out.println("test starts here");
        HypothesisTester hypotester = new HypothesisTester(genelist);

        for (Gene gene :hypotester.getGeneList()
             ) {
            System.out.println(gene.getIdent());
            gene.evaluateAvailableHypothesis();
        }
        //ArrayList<Gene> testedList = hypotester.getGeneList();

 /*       for (Gene gene: testedList
             ) {


            for (SNP snp : gene.getSnpsOnGene()
                 ) {
                System.out.println(snp.getHypothesisEval() + "   " + snp.getALTcov() + "  /  " + snp.getORGcov());
            }


            System.out.println(gene.getHypothesisArray()[0] +"  " + gene.getHypothesisArray()[1] +"  "
                    +gene.getHypothesisArray()[2] +"  " + gene.getHypothesisArray()[3]);

        }*/
    }






}