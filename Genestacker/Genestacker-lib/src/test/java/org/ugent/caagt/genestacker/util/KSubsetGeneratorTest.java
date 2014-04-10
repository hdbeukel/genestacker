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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
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
public class KSubsetGeneratorTest {

    private Random rg = new Random();
    
    public KSubsetGeneratorTest() {
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
     * Test of first method, of class KSubsetGenerator.
     */
    @Test
    public void testFirst() {
        for(int i=0; i<100; i++){
            int n = rg.nextInt(20)+1; // n in {1,20}
            int k = rg.nextInt(n)+1;  // k in {1,n}
            KSubsetGenerator ksub = new KSubsetGenerator(k, n);   
            // check first k-subset
            int[] first = ksub.first();
            for(int l=0; l<k; l++){
                assertEquals(l+1, first[l]);
            }
        }
    }

    /**
     * Test of successor method, of class KSubsetGenerator.
     */
    @Test
    public void testSuccessor() {
        
        System.out.println("");
        System.out.println("###");
        System.out.println("TESTING KSUBSET GENERATOR");
        System.out.println("###");
        System.out.println("");
        
        // START TEST
        
        // 3 out of 8
        
        KSubsetGenerator ksub = new KSubsetGenerator(3, 8);
        int[] cur = ksub.first();
        int count = 0;
        Set<int[]> generated = new HashSet<>();
        while(cur != null){
            System.out.println(Arrays.toString(cur));
            assertTrue(generated.add(Arrays.copyOf(cur, cur.length)));
            count++;
            cur = ksub.successor(cur);
        }
        System.out.println("# = " + count);
        assertEquals(56, count);
        
        // 5 out of 14
        
        ksub = new KSubsetGenerator(5, 14);
        cur = ksub.first();
        count = 0;
        generated = new HashSet<>();
        while(cur != null){
            System.out.println(Arrays.toString(cur));
            assertTrue(generated.add(Arrays.copyOf(cur, cur.length)));
            count++;
            cur = ksub.successor(cur);
        }
        System.out.println("# = " + count);
        assertEquals(2002, count);
        
        // 2 out of 2
        
        ksub = new KSubsetGenerator(2, 2);
        cur = ksub.first();
        count = 0;
        generated = new HashSet<>();
        while(cur != null){
            System.out.println(Arrays.toString(cur));
            assertTrue(generated.add(Arrays.copyOf(cur, cur.length)));
            count++;
            cur = ksub.successor(cur);
        }
        System.out.println("# = " + count);
        assertEquals(1, count);
        
        // END TEST
        
        System.out.println("");
        
    }

}