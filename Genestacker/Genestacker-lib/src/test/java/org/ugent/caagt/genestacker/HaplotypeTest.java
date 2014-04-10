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

package org.ugent.caagt.genestacker;

import junit.framework.TestCase;
import org.junit.Test;
import org.ugent.caagt.genestacker.exceptions.GenestackerException;

/**
 *
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class HaplotypeTest extends TestCase {
    
    public HaplotypeTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of nrOfLoci method, of class Haplotype.
     */
    @Test
    public void testNrOfTargets() throws GenestackerException{
        Haplotype hom1 = new Haplotype(new boolean[]{true, false, true});
        Haplotype hom2 = new Haplotype(new boolean[]{false, false, true, true});
        assertEquals(3, hom1.nrOfLoci());
        assertEquals(4, hom2.nrOfLoci());
        Haplotype hom3 = new Haplotype(hom2);
        assertEquals(hom2.nrOfLoci(), hom3.nrOfLoci());
        assertEquals(4, hom3.nrOfLoci());
    }

    /**
     * Test of targetPresent method, of class Haplotype.
     */
    @Test
    public void testTargetPresent() throws GenestackerException{
        Haplotype hom = new Haplotype(new boolean[]{true, false, true, true, false});
        assertTrue(hom.targetPresent(0));
        assertFalse(hom.targetPresent(1));
        assertTrue(hom.targetPresent(2));
        assertTrue(hom.targetPresent(3));
        assertFalse(hom.targetPresent(4));
    }

    /**
     * Test of equals method, of class Haplotype.
     */
    @Test
    public void testEquals() throws GenestackerException{
        Haplotype hom1 = new Haplotype(new boolean[]{true, false, true, true, false});
        Haplotype hom2 = new Haplotype(new boolean[]{true, false, true, true, false});
        Haplotype hom3 = new Haplotype(new boolean[]{true, false, true, true});
        Haplotype hom4 = new Haplotype(new boolean[]{true, false, false, true});
        // test hom1
        assertEquals(hom1, hom1);
        assertEquals(hom1, hom2);
        assertFalse(hom1.equals(hom3));
        assertFalse(hom1.equals(hom4));
        // test hom2
        assertEquals(hom2, hom1);
        assertEquals(hom2, hom2);
        assertFalse(hom2.equals(hom3));
        assertFalse(hom2.equals(hom4));
        // test hom3
        assertFalse(hom3.equals(hom1));
        assertFalse(hom3.equals(hom2));
        assertEquals(hom3, hom3);
        assertFalse(hom3.equals(hom4));
        // test hom4
        assertFalse(hom4.equals(hom1));
        assertFalse(hom4.equals(hom2));
        assertFalse(hom4.equals(hom3));
        assertEquals(hom4, hom4);
    }
    
    @Test
    public void testOrdering() throws GenestackerException{
        // hom1: [0 0 0]
        Haplotype hom1 = new Haplotype(new boolean[]{false, false, false});
        // hom2: [0 0 1]
        Haplotype hom2 = new Haplotype(new boolean[]{false, false, true});
        // hom3: [0 1 0]
        Haplotype hom3 = new Haplotype(new boolean[]{false, true, false});
        // hom4: [0 1 1]
        Haplotype hom4 = new Haplotype(new boolean[]{false, true, true});
        // hom5: [1 0 0]
        Haplotype hom5 = new Haplotype(new boolean[]{true, false, false});
        // hom6: [1 0 1]
        Haplotype hom6 = new Haplotype(new boolean[]{true, false, true});
        // hom7: [1 1 0]
        Haplotype hom7 = new Haplotype(new boolean[]{true, true, false});
        // hom8: [1 1 1]
        Haplotype hom8 = new Haplotype(new boolean[]{true, true, true});
        // hom9: [1 0]
        Haplotype hom9 = new Haplotype(new boolean[]{true, false});
        
        // compare some haplotypes
        
        assertEquals(0, hom1.compareTo(hom1));
        assertEquals(0, hom1.compareTo(new Haplotype(hom1)));
        
        assertTrue(hom1.compareTo(hom2) < 0);
        assertTrue(hom1.compareTo(hom3) < 0);
        assertTrue(hom1.compareTo(hom4) < 0);
        assertTrue(hom1.compareTo(hom5) < 0);
        assertTrue(hom1.compareTo(hom6) < 0);
        assertTrue(hom1.compareTo(hom7) < 0);
        assertTrue(hom1.compareTo(hom8) < 0);
        assertTrue(hom1.compareTo(hom9) > 0);
        
        assertTrue(hom4.compareTo(hom1) > 0);
        assertTrue(hom4.compareTo(hom2) > 0);
        assertTrue(hom4.compareTo(hom3) > 0);
        
        assertEquals(0, hom4.compareTo(hom4));
        assertEquals(0, hom4.compareTo(new Haplotype(hom4)));
        
        assertTrue(hom4.compareTo(hom5) < 0);
        assertTrue(hom4.compareTo(hom6) < 0);
        assertTrue(hom4.compareTo(hom7) < 0);
        assertTrue(hom4.compareTo(hom8) < 0);
        
    }
}
