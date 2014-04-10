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
import java.util.Random;
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
public class EventOccurrencesTest {

    private Random rg = new Random();
    
    public EventOccurrencesTest() {
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
     * Test of getFirst method, of class EventOccurrences.
     */
    @Test
    public void testGetFirst() {
        for(int i=0; i<100; i++){
            int n = rg.nextInt(5)+1;
            int[] maxOcc = new int[n];
            for(int e=0; e<n; e++){
                maxOcc[e] = rg.nextInt(5)+1;
            }
            EventOccurrences ev = new EventOccurrences(maxOcc);
            int[] first = ev.getFirst();
            assertArrayEquals(new int[n], first);
        }
    }

    /**
     * Test of successor method, of class EventOccurrences.
     */
    @Test
    public void testSuccessor() {
        for(int i=0; i<100; i++){
            int n = rg.nextInt(5)+2;
            int[] maxOcc = new int[n];
            for(int e=0; e<n; e++){
                maxOcc[e] = rg.nextInt(5)+1; // max in [1,5]
            }
            System.out.println("---");
            System.out.println("Max:");
            System.out.println(Arrays.toString(maxOcc));
            EventOccurrences ev = new EventOccurrences(maxOcc);
            // get first
            int[] cur = ev.getFirst();
            System.out.println("First:");
            System.out.println(Arrays.toString(cur));
            assertArrayEquals(new int[n], cur);
            // get successor
            cur = ev.successor(cur);
            System.out.println("Inc last:");
            System.out.println(Arrays.toString(cur));
            int[] exp = new int[n];
            exp[n-1] = 1;
            assertArrayEquals(exp, cur);
            // call successor until last pos reaches max
            for(int k=0; k<maxOcc[n-1]-1; k++){
                cur = ev.successor(cur);
            }
            System.out.println("Max last:");
            System.out.println(Arrays.toString(cur));
            // test if max reached
            exp = new int[n];
            exp[n-1] = maxOcc[n-1];
            assertArrayEquals(exp, cur);
            // test flip
            cur = ev.successor(cur);
            System.out.println("Flip last:");
            System.out.println(Arrays.toString(cur));
            exp = new int[n];
            exp[n-2] = 1;
            assertArrayEquals(exp, cur);
            // call successor until finished (null returned)
            int[] copy = Arrays.copyOf(cur, n);
            while(cur != null){
                // copy cur
                copy = Arrays.copyOf(cur, n);
                // call successor
                cur = ev.successor(cur);
            }
            System.out.println("Max all:");
            System.out.println(Arrays.toString(copy));
            // check if all occurrences have reached max in final option
            exp = new int[n];
            for(int k=0; k<n; k++){
                exp[k] = maxOcc[k];
            }
            assertArrayEquals(exp, copy);
        }
    }

}