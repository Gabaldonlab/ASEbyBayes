package com.ernstthuer;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;





public class BayesClassifyTest {

    private BayesClassify bcl = new BayesClassify(0.1,10,100,1000);

    ArrayList<Gene> genelist = new ArrayList<>();
    // test file 1

    @Before
    public void getNumbers(){
        // empty the list and start new
        genelist = new ArrayList<>();
        Gene gene = new Gene("chr1",0,1000000,"Gene1");

        //ArrayList<SNP> snps = new ArrayList<>();
        for(int i = 0; i <= 500 ; i++){
            SNP snp1 = new SNP(gene,'A','T',(i*2));
            snp1.setALTcov(i);
            snp1.setORGcov(500-i);
            //snps.add(snp1);
            gene.addSNP(snp1, false);
        }
        genelist.add(gene);

    }

    @Test
    public void testAlpha() throws Exception {
        // 100 SNPs detected so far, average coverage of 100, downside
        bcl.calculateAlpha(100,10000,0.1,100,false);
    }

    @Test
    public void testSNPclassify() throws Exception{

        HypothesisTester hypotester = new HypothesisTester(genelist);
        ArrayList<Gene> testedList = hypotester.getGeneList();

        for (Gene gene: testedList
             ) {

            System.out.println(gene.getHypothesisArray()[0] +"  " + gene.getHypothesisArray()[1] +"  "
                    +gene.getHypothesisArray()[2] +"  " + gene.getHypothesisArray()[3]);

        }



    }




}