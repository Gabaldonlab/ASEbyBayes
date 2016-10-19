package com.ernstthuer;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by ethuer on 18/10/16.
 */



public class HypothesisTest {

    private int length = 500;
    private static ArrayList<SNP> snpList = new ArrayList<>();
    //public String gene = "Testgene";
    private Gene testgene = new Gene("CHr1",0,length,"+");
    //private Gene testgene2 = new Gene("CHr1",0,length,"+");
    private Random randomGenerator = new Random();
    // make alpha a linear function of coverage


    public Hypothesis hypothesis = new Hypothesis(0.1,10,"NoiseHyp");
    public Hypothesis hypothesisEAX = new Hypothesis(5,5,"EqualAllelicExpression");
    public Hypothesis hypothesisFullSNP = new Hypothesis(10,0.1,"FullSNPcall");

    @Before
    public void createData(){

        for(int i = 0; i< length; i++){
            SNP snp = new SNP(testgene,'A',i);
            snp.setALTcov(i);
            snp.setORGcov(length-i);
            snpList.add(snp);
            testgene.addSNP(snp,false);
        }
        hypothesis.addGene(testgene);
        hypothesisEAX.addGene(testgene);
        hypothesisFullSNP.addGene(testgene);
        System.out.printf( " "+hypothesis.getGeneList().size() );
        System.out.println("populated gene List with " + testgene.getSnpsOnGene().size() + " SNPs");
    }




/*
    @Test
    public void NoiseCaptureTest() throws Exception {
        System.out.println(hypothesis.getGeneList().size());
        hypothesis.calculateHypothisForSNPsOnGenes(hypothesis.getGeneList());
    }



    @Test
    public void EqualAllelicExpressionTest() throws Exception{
        System.out.println("Second test");
        hypothesisEAX.calculateHypothisForSNPsOnGenes(hypothesisEAX.getGeneList());
    }

*/
    @Test
    public void FullSNPCaptureTest() throws Exception {
        System.out.println("Third test");
        hypothesisFullSNP.calculateHypothisForSNPsOnGenes(hypothesisFullSNP.getGeneList());

    }




}