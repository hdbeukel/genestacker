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
import org.ugent.caagt.genestacker.exceptions.IncompatibleHaplotypesException;

/**
 *
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class ChromosomeTest extends TestCase {
    
    public ChromosomeTest(String testName) {
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

    @Test
    public void testExceptions() throws GenestackerException{
        // try to create diploid chromosome with incompatible haplotypes
        Haplotype hom1 = new Haplotype(new boolean[]{true, true, false});
        Haplotype hom2 = new Haplotype(new boolean[]{false});
        boolean thrown = false;
        try{
            Chromosome chr = new DiploidChromosome(hom1, hom2);
        } catch (IncompatibleHaplotypesException ex){
            thrown = true;
        }
        assertTrue(thrown);
        // try to create diploid chromosome with compatible haplotypes
        Haplotype hom3 = new Haplotype(new boolean[]{true, true, false});
        Haplotype hom4 = new Haplotype(new boolean[]{false, true, false});
        thrown = false;
        try{
            Chromosome chr = new DiploidChromosome(hom3, hom4);
        } catch (IncompatibleHaplotypesException ex){
            thrown = true;
        }
        assertFalse(thrown);
    }

    @Test
    public void testHomozygousHeterozygous() throws GenestackerException{
        // create haplotypes
        Haplotype hom1 = new Haplotype(new boolean[]{true, true, false});
        Haplotype hom2 = new Haplotype(new boolean[]{false, true, false});
        // create diploid chromosome
        DiploidChromosome chr = new DiploidChromosome(hom1, hom2);
        // test for heterozygous / homozygous loci
        assertTrue(chr.isHeterozygousAtLocus(0));
        assertFalse(chr.isHomozygousAtLocus(0));
        assertFalse(chr.isHeterozygousAtLocus(1));
        assertTrue(chr.isHomozygousAtLocus(1));
        assertFalse(chr.isHeterozygousAtLocus(2));
        assertTrue(chr.isHomozygousAtLocus(2));
    }
    
    @Test
    public void testNrOfTargets() throws GenestackerException{
        // test with haploid chromosome
        Haplotype hom1 = new Haplotype(new boolean[]{true, true, false, true, true});
        Chromosome chr = new HaploidChromosome(hom1);
        assertEquals(5, chr.nrOfLoci());
        // test with diploid chromosome
        Haplotype hom2 = new Haplotype(new boolean[]{false, true, false, true});
        Haplotype hom3 = new Haplotype(new boolean[]{false, false, false, false});
        Chromosome chr2 = new DiploidChromosome(hom2, hom3);
        assertEquals(4, chr2.nrOfLoci());
    }
    
    @Test
    public void testEquals() throws GenestackerException{
        // create some haplotypes
        Haplotype hom1 = new Haplotype(new boolean[]{true, true, false, true, true});
        Haplotype hom2 = new Haplotype(new boolean[]{true, true, false, true, true});
        Haplotype hom3 = new Haplotype(new boolean[]{true, true, true, true, true});
        Haplotype hom4 = new Haplotype(new boolean[]{true, true});
        // create some haploid chromosomes
        HaploidChromosome hapChr1 = new HaploidChromosome(hom1);
        HaploidChromosome hapChr2 = new HaploidChromosome(hom1);
        HaploidChromosome hapChr3 = new HaploidChromosome(hom2);
        HaploidChromosome hapChr4 = new HaploidChromosome(hom3);
        HaploidChromosome hapChr5 = new HaploidChromosome(hom4);
        HaploidChromosome hapChr6 = new HaploidChromosome(hapChr5);
        // tests for hapChr1
        assertEquals(hapChr1, hapChr1);
        assertEquals(hapChr1, hapChr2);
        assertEquals(hapChr1, hapChr3);
        assertFalse(hapChr1.equals(hapChr4));
        assertFalse(hapChr1.equals(hapChr5));
        assertFalse(hapChr1.equals(hapChr6));
        // tests for hapChr2
        assertEquals(hapChr2, hapChr1);
        assertEquals(hapChr2, hapChr2);
        assertEquals(hapChr2, hapChr3);
        assertFalse(hapChr2.equals(hapChr4));
        assertFalse(hapChr2.equals(hapChr5));
        assertFalse(hapChr2.equals(hapChr6));
        // tests for hapChr3
        assertEquals(hapChr3, hapChr1);
        assertEquals(hapChr3, hapChr2);
        assertEquals(hapChr3, hapChr3);
        assertFalse(hapChr3.equals(hapChr4));
        assertFalse(hapChr3.equals(hapChr5));
        assertFalse(hapChr3.equals(hapChr6));
        // tests for hapChr4
        assertFalse(hapChr4.equals(hapChr1));
        assertFalse(hapChr4.equals(hapChr2));
        assertFalse(hapChr4.equals(hapChr3));
        assertEquals(hapChr4, hapChr4);
        assertFalse(hapChr4.equals(hapChr5));
        assertFalse(hapChr4.equals(hapChr6));
        // tests for hapChr5
        assertFalse(hapChr5.equals(hapChr1));
        assertFalse(hapChr5.equals(hapChr2));
        assertFalse(hapChr5.equals(hapChr3));
        assertFalse(hapChr5.equals(hapChr4));
        assertEquals(hapChr5, hapChr5);
        assertEquals(hapChr5, hapChr6);
        // tests for hapChr6
        assertFalse(hapChr6.equals(hapChr1));
        assertFalse(hapChr6.equals(hapChr2));
        assertFalse(hapChr6.equals(hapChr3));
        assertFalse(hapChr6.equals(hapChr4));
        assertEquals(hapChr6, hapChr5);
        assertEquals(hapChr6, hapChr6);
        
        // create some more haplotypes
        Haplotype hom5 = new Haplotype(new boolean[]{true, true, true, true, true});
        Haplotype hom6 = new Haplotype(new boolean[]{false, true, false, true, true});
        Haplotype hom7 = new Haplotype(new boolean[]{true, true, true, true, true});
        Haplotype hom8 = new Haplotype(new boolean[]{false, true, false, true, true});
        Haplotype hom9 = new Haplotype(new boolean[]{true, true, true, true, true});
        Haplotype hom10 = new Haplotype(new boolean[]{false, false, false, false, false});
        // create some diploid chromosomes
        Chromosome dipChr1 = new DiploidChromosome(hom5, hom6);
        Chromosome dipChr2 = new DiploidChromosome(hom5, hom6);
        Chromosome dipChr3 = new DiploidChromosome(hom7, hom8);
        Chromosome dipChr4 = new DiploidChromosome(hom9, hom10);
        // tests for dipChr1
        assertEquals(dipChr1, dipChr1);
        assertEquals(dipChr1, dipChr2);
        assertEquals(dipChr1, dipChr3);
        assertFalse(dipChr1.equals(dipChr4));
        // tests for dipChr2
        assertEquals(dipChr2, dipChr1);
        assertEquals(dipChr2, dipChr2);
        assertEquals(dipChr2, dipChr3);
        assertFalse(dipChr2.equals(dipChr4));
        // tests for dipChr3
        assertEquals(dipChr3, dipChr1);
        assertEquals(dipChr3, dipChr2);
        assertEquals(dipChr3, dipChr3);
        assertFalse(dipChr3.equals(dipChr4));
        // tests for dipChr4
        assertFalse(dipChr4.equals(dipChr1));
        assertFalse(dipChr4.equals(dipChr2));
        assertFalse(dipChr4.equals(dipChr3));
        assertEquals(dipChr4, dipChr4);
        
        // finally: compare diploid with haploid chromosome
        
        Chromosome[] hapChroms = new Chromosome[]{hapChr1, hapChr2, hapChr3, hapChr4, hapChr5, hapChr6};
        Chromosome[] dipChroms = new Chromosome[]{dipChr1, dipChr2, dipChr3, dipChr4};
        for (int i=0; i<hapChroms.length; i++){
            for(int j=0; j<dipChroms.length; j++){
                assertFalse(hapChroms[i].equals(dipChroms[j]));
            }
        }
    }
    
    @Test
    public void testSwappedHaplotypes() throws GenestackerException{
        
        // create some haplotypes
        
        // hom1: [0 0 0]
        Haplotype hom1 = new Haplotype(new boolean[]{false, false, false});
        // hom2: [0 0 1]
        Haplotype hom2 = new Haplotype(new boolean[]{false, false, true});
        // hom3: [1 0]
        Haplotype hom3 = new Haplotype(new boolean[]{true, false});
        // hom4: [1 1]
        Haplotype hom4 = new Haplotype(new boolean[]{true, true});
        
        // create some diploid chromosomes
        
        DiploidChromosome chr1_1 = new DiploidChromosome(hom1, hom2);
        DiploidChromosome chr1_2 = new DiploidChromosome(hom2, hom1);
        
        DiploidChromosome chr2_1 = new DiploidChromosome(hom3, hom4);
        DiploidChromosome chr2_2 = new DiploidChromosome(hom4, hom3);
        
        assertEquals(chr1_1, chr1_2);
        assertEquals(chr2_1, chr2_2);
        
    }
    
    @Test
    public void testObservableState() throws GenestackerException{
        
        System.out.println("\n### OBSERVABLE STATE ###\n");
        
        // create some hapotypes
        
        // hom1: [0 1 0]
        Haplotype hom1 = new Haplotype(new boolean[]{false, true, false});
        // hom2: [0 0 1]
        Haplotype hom2 = new Haplotype(new boolean[]{false, false, true});
        // hom3: [0 0 0]
        Haplotype hom3 = new Haplotype(new boolean[]{false, false, false});
        // hom4: [0 1 1]
        Haplotype hom4 = new Haplotype(new boolean[]{false, true, true});
        
        // create some diploid chromosomes
        
        DiploidChromosome chr1 = new DiploidChromosome(hom1, hom2);
        DiploidChromosome chr2 = new DiploidChromosome(hom3, hom4);
        
        System.out.println("---");
        System.out.println("Chromosome 1:\n" + chr1);
        System.out.println("Observable state: " + chr1.getAllelicFrequencies());
        System.out.println("---");
        System.out.println("---");
        System.out.println("Chromosome 2:\n" + chr2);
        System.out.println("Observable state: " + chr2.getAllelicFrequencies());
        System.out.println("---");
        
        // check observable state
        
        AllelicFrequency[] targetStates = new AllelicFrequency[] {
            AllelicFrequency.NONE,
            AllelicFrequency.ONCE,
            AllelicFrequency.ONCE
        };
        ChromosomeAllelicFrequencies state = new ChromosomeAllelicFrequencies(targetStates);
        assertEquals(state, chr1.getAllelicFrequencies());
        
        assertEquals(chr1.getAllelicFrequencies(), chr2.getAllelicFrequencies());
        
        // create some more chromosomes
        DiploidChromosome chr3 = new DiploidChromosome(hom2, hom4);
        
        System.out.println("---");
        System.out.println("Chromosome 3:\n" + chr3);
        System.out.println("Observable state: " + chr3.getAllelicFrequencies());
        System.out.println("---");
        
        // check state
        
        targetStates = new AllelicFrequency[] {
            AllelicFrequency.NONE,
            AllelicFrequency.ONCE,
            AllelicFrequency.TWICE
        };
        state = new ChromosomeAllelicFrequencies(targetStates);
        assertEquals(state, chr3.getAllelicFrequencies());
        
        assertFalse(chr3.getAllelicFrequencies().equals(chr1.getAllelicFrequencies()));
    }
    
}
