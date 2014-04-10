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

package org.ugent.caagt.genestacker.search.bb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ugent.caagt.genestacker.DiploidChromosome;
import org.ugent.caagt.genestacker.GeneticMap;
import org.ugent.caagt.genestacker.Genotype;
import org.ugent.caagt.genestacker.Haplotype;
import org.ugent.caagt.genestacker.GenotypeGroupWithSameAllelicFrequencies;
import org.ugent.caagt.genestacker.GenotypeAllelicFrequencies;
import org.ugent.caagt.genestacker.SeedLot;
import org.ugent.caagt.genestacker.exceptions.GenestackerException;
import org.ugent.caagt.genestacker.search.bb.heuristics.GenotypeImprovement;
import org.ugent.caagt.genestacker.search.bb.heuristics.ImprovementSeedLotFilter;
import org.ugent.caagt.genestacker.search.bb.heuristics.WeakGenotypeImprovement;

/**
 *
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class FilterSeedLotTest extends TestCase {

    public FilterSeedLotTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    @Override
    public void setUp() {
    }

    @After
    @Override
    public void tearDown() {
    }

    /**
     * Test heuristic seed lot filtering.
     */
    @Test
    public void testPruneGrowPlantFromAncestors() throws GenestackerException{

        System.out.println("\n### TEST HEURISTIC SEED LOT FILTERING ###\n");
                
        /***************/
        /* TEST CASE 1 */
        /***************/

        // create ideotype
        Haplotype hap = new Haplotype(new boolean[]{true, true, true, true});
        DiploidChromosome chr = new DiploidChromosome(hap, hap);
        List<DiploidChromosome> chrs = new ArrayList<>();
        chrs.add(chr);
        Genotype ideotype = new Genotype(chrs);
     
        // create genetic map
        double[][] d = {
            {100, 1, 100}  
        };
        GeneticMap map = new GeneticMap(d);
        
        GenotypeImprovement impr = new WeakGenotypeImprovement(ideotype);
        
        // create some haplotypes
        Haplotype hap1 = new Haplotype(new boolean[]{false, true, true, true});
        Haplotype hap2 = new Haplotype(new boolean[]{true, true, true, false});
        Haplotype hap3 = new Haplotype(new boolean[]{true, true, false, false});
        
        // create some chromosomes
        DiploidChromosome chr1 = new DiploidChromosome(hap1, hap2);
        DiploidChromosome chr2 = new DiploidChromosome(hap1, hap3);
        
        // create some genotypes
        
        //  p1:
        // [0 1 1 1]
        // [1 1 1 0]
        chrs = new ArrayList<>();
        chrs.add(chr1);
        Genotype g1 = new Genotype(chrs);
        
        //  p2:
        // [0 1 1 1]
        // [1 1 0 0]
        chrs = new ArrayList<>();
        chrs.add(chr2);
        Genotype g2 = new Genotype(chrs);
        
        // create seed lot (g1 more probable than g2)
        Map<Genotype, Double> g1map = new HashMap<>();
        g1map.put(g1, 0.4);
        Map<Genotype, Double> g2map = new HashMap<>();
        g2map.put(g2, 0.2);
        Map<GenotypeAllelicFrequencies, GenotypeGroupWithSameAllelicFrequencies> states = new HashMap<>();
        states.put(g1.getAllelicFrequencies(), new GenotypeGroupWithSameAllelicFrequencies(0.4, g1.getAllelicFrequencies(), g1map));
        states.put(g2.getAllelicFrequencies(), new GenotypeGroupWithSameAllelicFrequencies(0.2, g2.getAllelicFrequencies(), g2map));
        SeedLot sl = new SeedLot(false, states);

        /*******************/
        /* CHECK FILTERING */
        /*******************/
        
        ImprovementSeedLotFilter filter = new ImprovementSeedLotFilter(impr);
        assertEquals(2, sl.nrOfGenotypes());
        // filterSeedLot
        sl = filter.filterSeedLot(sl);
        // check that g2 was filtered
        assertEquals(1, sl.nrOfGenotypes());
        assertTrue(sl.getGenotypes().contains(g1));
        assertFalse(sl.getGenotypes().contains(g2));
        
        /***************/
        /* TEST CASE 2 */
        /***************/
        
        // create seed lot (both g1 and g2 equally probable)
        g1map = new HashMap<>();
        g1map.put(g1, 0.4);
        g2map = new HashMap<>();
        g2map.put(g2, 0.4);
        states = new HashMap<>();
        states.put(g1.getAllelicFrequencies(), new GenotypeGroupWithSameAllelicFrequencies(0.4, g1.getAllelicFrequencies(), g1map));
        states.put(g2.getAllelicFrequencies(), new GenotypeGroupWithSameAllelicFrequencies(0.4, g2.getAllelicFrequencies(), g2map));
        sl = new SeedLot(false, states);

        /*******************/
        /* CHECK FILTERING */
        /*******************/
        
        filter = new ImprovementSeedLotFilter(impr);
        assertEquals(2, sl.nrOfGenotypes());
        // filterSeedLot
        sl = filter.filterSeedLot(sl);
        // check that g2 was filtered
        assertEquals(1, sl.nrOfGenotypes());
        assertTrue(sl.getGenotypes().contains(g1));
        assertFalse(sl.getGenotypes().contains(g2));
        
        /***************/
        /* TEST CASE 3 */
        /***************/
        
        // create seed lot (g2 most probable)
        g1map = new HashMap<>();
        g1map.put(g1, 0.2);
        g2map = new HashMap<>();
        g2map.put(g2, 0.4);
        states = new HashMap<>();
        states.put(g1.getAllelicFrequencies(), new GenotypeGroupWithSameAllelicFrequencies(0.2, g1.getAllelicFrequencies(), g1map));
        states.put(g2.getAllelicFrequencies(), new GenotypeGroupWithSameAllelicFrequencies(0.4, g2.getAllelicFrequencies(), g2map));
        sl = new SeedLot(false, states);

        /*******************/
        /* CHECK FILTERING */
        /*******************/
        
        filter = new ImprovementSeedLotFilter(impr);
        assertEquals(2, sl.nrOfGenotypes());
        // filterSeedLot
        sl = filter.filterSeedLot(sl);
        // check that filtering did not have any effect (better plant has lower probability)
        assertEquals(2, sl.nrOfGenotypes());
        assertTrue(sl.getGenotypes().contains(g1));
        assertTrue(sl.getGenotypes().contains(g2));
        
        /***************/
        /* TEST CASE 4 */
        /***************/
        
        // create some haplotypes
        hap1 = new Haplotype(new boolean[]{true, false, false, false});
        hap2 = new Haplotype(new boolean[]{false, false, false, true});
        
        // create some chromosomes
        chr1 = new DiploidChromosome(hap1, hap1);
        chr2 = new DiploidChromosome(hap2, hap2);
        
        // create some genotypes
        
        //  p1:
        // [1 0 0 0]
        // [1 0 0 0]
        chrs = new ArrayList<>();
        chrs.add(chr1);
        g1 = new Genotype(chrs);
        
        //  p2:
        // [0 0 0 1]
        // [0 0 0 1]
        chrs = new ArrayList<>();
        chrs.add(chr2);
        g2 = new Genotype(chrs);
        
        // create seed lot (g1 more probable than g2)
        g1map = new HashMap<>();
        g1map.put(g1, 0.4);
        g2map = new HashMap<>();
        g2map.put(g2, 0.2);
        states = new HashMap<>();
        states.put(g1.getAllelicFrequencies(), new GenotypeGroupWithSameAllelicFrequencies(0.4, g1.getAllelicFrequencies(), g1map));
        states.put(g2.getAllelicFrequencies(), new GenotypeGroupWithSameAllelicFrequencies(0.2, g2.getAllelicFrequencies(), g2map));
        sl = new SeedLot(false, states);

        /*******************/
        /* CHECK FILTERING */
        /*******************/
        
        filter = new ImprovementSeedLotFilter(impr);
        assertEquals(2, sl.nrOfGenotypes());
        // filterSeedLot
        sl = filter.filterSeedLot(sl);
        // check that filtering had no effect (both plants improve on each other)
        assertEquals(2, sl.nrOfGenotypes());
        assertTrue(sl.getGenotypes().contains(g1));
        assertTrue(sl.getGenotypes().contains(g2));
        
    }

}