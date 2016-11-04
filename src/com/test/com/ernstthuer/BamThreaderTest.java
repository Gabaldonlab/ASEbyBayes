package com.ernstthuer;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by ethur on 11/3/16.
 */
public class BamThreaderTest {

    // needs to load the data from test set, or create own

    @Before
    public void generateGeneLists() throws Exception{

        ArrayList<Gene> geneList = new ArrayList<>();
        for(int i = 0; i < 10 ; i++){

            Gene gene = new Gene("chr1",(i*100),(1*100)+50, " " + i + " _ " );

            ArrayList<SNP> snpList = new ArrayList<>();

            for(int j = 0; j < 10;j++){
                SNP snp = new SNP(gene,'A',j+i);
                snpList.add(snp);
            }

            gene.addSnpInformation(snpList);
        }


    }

    @Test
    public void cloneGeneLists() throws Exception {

    }

    @Test
    public void loadBamDataviaThreading() throws Exception {

    }

    @Test
    public void unifyGeneLists() throws Exception {

    }

}