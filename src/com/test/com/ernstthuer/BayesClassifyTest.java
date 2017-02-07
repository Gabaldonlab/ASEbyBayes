package com.ernstthuer;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by ethur on 2/7/17.
 */
public class BayesClassifyTest {

    BayesClassify bcl = new BayesClassify(0.1,10,100,1000);

    @Before
    public void getNumbers(){
    }

    @Test
    public void testAlpha() throws Exception {

        // 100 SNPs detected so far, average coverage of 100, downside
        bcl.calculateAlpha(100,0.1,100,false);

    }

}