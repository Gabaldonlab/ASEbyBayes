package com.ernstthuer;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by ethuer on 18/10/16.
 */



public class HypothesisTest {

    private int length = 12000;
    private static ArrayList<SNP> snpList = new ArrayList<>();
    //public String gene = "Testgene";
    private Gene testgene = new Gene("CHr1",0,length,"+");
    //private Gene testgene2 = new Gene("CHr1",0,length,"+");
    private Random randomGenerator = new Random();

    // make alpha a linear function of coverage




    public Hypothesis hypothesis = new Hypothesis(0.1,100,"NoiseHyp");
    public Hypothesis hypothesisEAX = new Hypothesis(50,50,"EqualAllelicExpression");
    public Hypothesis hypothesisFullSNP = new Hypothesis(100,0.1,"FullSNPcall");

    @Before
    public void createData(){

        for(int i = 0; i< length; i++){
            SNP snp = new SNP(testgene,'A',i);
            snp.setALTcov(i);
            snp.setORGcov(length);
            snpList.add(snp);
            testgene.addSNP(snp,false);
        }
        hypothesis.addGene(testgene);
        hypothesisEAX.addGene(testgene);
        System.out.printf( " "+hypothesis.getGeneList().size() );
        System.out.println("populated gene List with " + testgene.getSnpsOnGene().size() + " SNPs");
    }





    @Test
    public void NoiseCaptureTest() throws Exception {
        System.out.println(hypothesis.getGeneList().size());
        hypothesis.accessSNPs(hypothesis.getGeneList());
    }

/*

    @Test
    public void EqualAllelicExpressionTest() throws Exception{
        hypothesisEAX.accessSNPs(hypothesisEAX.getGeneList());
    }

    @Test
    public void FullSNPCaptureTest() throws Exception {
        hypothesisFullSNP.accessSNPs(hypothesisFullSNP.getGeneList());

    }
*/



}