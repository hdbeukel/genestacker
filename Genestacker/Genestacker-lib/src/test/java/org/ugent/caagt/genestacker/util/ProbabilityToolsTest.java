//  Copyright 2012 Herman De Beukelaer
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

package org.ugent.caagt.genestacker.util;

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class ProbabilityToolsTest {
    
    public ProbabilityToolsTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of computeProbMaxOcc method, of class ProbabilityTools.
     */
    @Test
    public void testComputeProbMaxOcc() {
        
        System.out.println("");
        System.out.println("###");
        System.out.println("TEST COMPUTE PROB MAX OCC");
        System.out.println("###");
        System.out.println("");
        
        // START TEST
        
        ProbabilityTools ptools = new ProbabilityTools();
        
        // compute P(A <= 0), with:
        //  pA = 0.10
        //  n = 100
        
        double[] probs = new double[] {0.10};
        int[] occ = new int[] {0};
        int n = 100;
        
        double successProb = ptools.computeProbMaxOcc(probs, occ, n);
        System.out.println("Prob: " + successProb);
        assertEquals(0.0000265614, successProb, 0.0000000001);
        
        // compute P(A <= 1), with:
        //  pA = 0.10
        //  n = 100
        probs = new double[] {0.10};
        occ = new int[] {1};
        n = 100;
        
        successProb = ptools.computeProbMaxOcc(probs, occ, n);
        System.out.println("Prob: " + successProb);
        assertEquals(0.000321688, successProb, 0.000000001);
        
        // compute P(A <= 3), with:
        //  pA = 0.10
        //  n = 100
        probs = new double[] {0.10};
        occ = new int[] {3};
        n = 100;
        
        successProb = ptools.computeProbMaxOcc(probs, occ, n);
        System.out.println("Prob: " + successProb);
        assertEquals(0.00783649, successProb, 0.00000001);
        
        // compute P(A <= 0 & B <= 0), with:
        //  pA = 0.10
        //  pB = 0.30
        //  n = 1000
        probs = new double[] {0.10, 0.30};
        occ = new int[] {0, 0};
        n = 1000;
        
        successProb = ptools.computeProbMaxOcc(probs, occ, n);
        System.out.println("Prob: " + successProb);
        assertEquals(1.41661e-222, successProb, 1e-227);
        
        // compute P(A <= 1 & B <= 3), with:
        //  pA = 0.10
        //  pB = 0.30
        //  n = 300
        probs = new double[] {0.10, 0.30};
        occ = new int[] {1, 3};
        n = 300;
        
        successProb = ptools.computeProbMaxOcc(probs, occ, n);
        System.out.println("Prob: " + successProb);
        assertEquals(8.00269e-60, successProb, 1e-65);
        
        // END TEST
        
        System.out.println("");
        
    }
    
    /**
     * Test of computeProbMinOcc method, of class ProbabilityTools.
     */
    @Test
    public void testComputeProbMinOcc() {
        
        System.out.println("");
        System.out.println("###");
        System.out.println("TEST COMPUTE PROB MIN OCC");
        System.out.println("###");
        System.out.println("");
        
        // START TEST
        
        ProbabilityTools ptools = new ProbabilityTools();
        
        // compute P(A >= 1), with:
        //  pA = 0.10
        //  n = 100
        
        double[] probs = new double[] {0.10};
        int[] occ = new int[] {1};
        int n = 100;
        
        double successProb = ptools.computeProbMinOcc(probs, occ, n);
        System.out.println("Prob: " + successProb);
        assertEquals(0.999973, successProb, 0.000001);
        
        // compute P(A >= 2), with:
        //  pA = 0.10
        //  n = 100
        
        probs = new double[] {0.10};
        occ = new int[] {2};
        n = 100;
        
        successProb = ptools.computeProbMinOcc(probs, occ, n);
        System.out.println("Prob: " + successProb);
        assertEquals(0.999678, successProb, 0.000001);
        
        // compute P(A >= 2), with:
        //  pA = 0.10
        //  n = 3
        
        probs = new double[] {0.10};
        occ = new int[] {2};
        n = 3;
        
        successProb = ptools.computeProbMinOcc(probs, occ, n);
        System.out.println("Prob: " + successProb);
        assertEquals(0.028, successProb, 0.001);
        
        // compute P(A >= 1 & B >= 1), with:
        //  pA = 0.10
        //  pB = 0.30
        //  n = 100
        
        probs = new double[] {0.10, 0.30};
        occ = new int[] {1, 1};
        n = 100;
        
        successProb = ptools.computeProbMinOcc(probs, occ, n);
        System.out.println("Prob: " + successProb);
        assertEquals(0.999973, successProb, 0.000001);
        
        // compute P(A >= 1 & B >= 1), with:
        //  pA = 0.10
        //  pB = 0.30
        //  n = 5
        
        probs = new double[] {0.10, 0.30};
        occ = new int[] {1, 1};
        n = 5;
        
        successProb = ptools.computeProbMinOcc(probs, occ, n);
        System.out.println("Prob: " + successProb);
        assertEquals(0.3192, successProb, 0.0001);
        
        // compute P(A >= 1 & B >= 2), with:
        //  pA = 0.10
        //  pB = 0.30
        //  n = 50
        
        probs = new double[] {0.10, 0.30};
        occ = new int[] {1, 2};
        n = 50;
        
        successProb = ptools.computeProbMinOcc(probs, occ, n);
        System.out.println("Prob: " + successProb);
        assertEquals(0.994846, successProb, 0.000001);
        
        // compute P(A >= 1 & B >= 2), with:
        //  pA = 0.10
        //  pB = 0.30
        //  n = 10
        
        probs = new double[] {0.10, 0.30};
        occ = new int[] {1, 2};
        n = 10;
        
        successProb = ptools.computeProbMinOcc(probs, occ, n);
        System.out.println("Prob: " + successProb);
        assertEquals(0.538293, successProb, 0.000001);
        
        // END TEST
        
        System.out.println("");
        
    }

}