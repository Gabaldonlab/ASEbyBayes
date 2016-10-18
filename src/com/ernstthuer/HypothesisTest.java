package com.ernstthuer;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by ethuer on 18/10/16.
 */



public class HypothesisTest {

    private int length = 100;
    private static ArrayList<SNP> snpList = new ArrayList<>();
    //public String gene = "Testgene";
    private Gene testgene = new Gene("CHr1",0,length,"+");
    //private Gene testgene2 = new Gene("CHr1",0,length,"+");
    private Random randomGenerator = new Random();


    public Hypothesis hypothesis = new Hypothesis(25,25,"TestHyp");

    @Before
    public void createData(){

        for(int i = 0; i< length; i++){
            SNP snp = new SNP(testgene,'A',i);
            snp.setALTcov(900 + randomGenerator.nextInt(200));
            snp.setORGcov(900 + randomGenerator.nextInt(200));
            snpList.add(snp);
            testgene.addSNP(snp,false);
        }
        hypothesis.addGene(testgene);
        System.out.printf( " "+hypothesis.getGeneList().size() );
        System.out.println("populated gene List with " + testgene.getSnpsOnGene().size() + " SNPs");
    }




    @Test
    public void EqAlExTest() throws Exception {
        System.out.println(hypothesis.getGeneList().size());
        hypothesis.accessSNPs(hypothesis.getGeneList());



    }



}