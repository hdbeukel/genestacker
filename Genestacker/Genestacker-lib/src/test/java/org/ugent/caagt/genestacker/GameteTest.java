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

import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.junit.Test;
import org.ugent.caagt.genestacker.exceptions.GenestackerException;

/**
 *
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class GameteTest extends TestCase {
    
    public GameteTest(String testName) {
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
     * Test of equals method, of class Gamete.
     */
    @Test
    public void testEquals() throws GenestackerException{
        // create some haplotypes
        Haplotype hom1 = new Haplotype(new boolean[]{true, false});
        Haplotype hom2 = new Haplotype(new boolean[]{true, true});
        Haplotype hom3 = new Haplotype(new boolean[]{true, true});
        Haplotype hom4 = new Haplotype(new boolean[]{true});
        Haplotype hom5 = new Haplotype(new boolean[]{false});
        Haplotype hom6 = new Haplotype(new boolean[]{false});
        // create some haploid chromosomes
        HaploidChromosome chr1 = new HaploidChromosome(hom1);
        HaploidChromosome chr2 = new HaploidChromosome(hom2);
        HaploidChromosome chr3 = new HaploidChromosome(hom3);
        HaploidChromosome chr4 = new HaploidChromosome(hom4);
        HaploidChromosome chr5 = new HaploidChromosome(hom5);
        HaploidChromosome chr6 = new HaploidChromosome(hom6);
        
        // create some gametes
        
        List<HaploidChromosome> g1chroms = new ArrayList<>();
        g1chroms.add(chr1);
        g1chroms.add(chr4);
        // g1: [1 0] [1]
        Gamete g1 = new Gamete(g1chroms);
        
        List<HaploidChromosome> g3chroms = new ArrayList<>();
        g3chroms.add(chr2);
        g3chroms.add(chr5);
        // g3: [1 1] [0]
        Gamete g3 = new Gamete(g3chroms);
        
        List<HaploidChromosome> g4chroms = new ArrayList<>();
        g4chroms.add(chr3);
        g4chroms.add(chr6);
        // g4: [1 1] [0]
        Gamete g4 = new Gamete(g4chroms);
        
        List<HaploidChromosome> g5chroms = new ArrayList<>();
        g5chroms.add(chr1);
        // g5: [1 0]
        Gamete g5 = new Gamete(g5chroms);
        
        // tests for g1
        assertEquals(g1, g1);
        assertFalse(g1.equals(g3));
        assertFalse(g1.equals(g4));
        assertFalse(g1.equals(g5));
        // tests for g3
        assertFalse(g3.equals(g1));
        assertEquals(g3, g3);
        assertEquals(g3, g4);
        assertFalse(g3.equals(g5));
        // tests for g4
        assertFalse(g4.equals(g1));
        assertEquals(g4, g3);
        assertEquals(g4, g4);
        assertFalse(g4.equals(g5));
        // tests for g5
        assertFalse(g5.equals(g1));
        assertFalse(g5.equals(g3));
        assertFalse(g5.equals(g4));
        assertEquals(g5, g5);
   }

}
