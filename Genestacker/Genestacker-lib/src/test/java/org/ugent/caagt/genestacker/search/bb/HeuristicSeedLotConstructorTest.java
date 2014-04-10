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

import org.ugent.caagt.genestacker.search.bb.heuristics.HeuristicSeedLotConstructor;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.junit.Test;
import org.ugent.caagt.genestacker.DiploidChromosome;
import org.ugent.caagt.genestacker.GeneticMap;
import org.ugent.caagt.genestacker.Genotype;
import org.ugent.caagt.genestacker.Haplotype;
import org.ugent.caagt.genestacker.SeedLot;
import org.ugent.caagt.genestacker.exceptions.GenestackerException;
import org.ugent.caagt.genestacker.util.GenestackerConstants;

/**
 *
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class HeuristicSeedLotConstructorTest extends TestCase {
    
    /**
     * Test heuristic seed lot constructor.
     */
    @Test
    public void testHeuristicSeedLotConstructor() throws GenestackerException{

       System.out.println("\n### TEST HEURISTIC SEED LOT CONSTRUCTOR ###\n");
        
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
        
        // create seed lot constructor
        SeedLotConstructor seedLotConstructor = new HeuristicSeedLotConstructor(map, ideotype, GenestackerConstants.UNLIMITED_CROSSOVERS, true);
        
        // create some haplotypes
        Haplotype hap1 = new Haplotype(new boolean[]{true, true, true, true});
        Haplotype hap2 = new Haplotype(new boolean[]{false, false, false, false});
        
        // create some chromosomes
        DiploidChromosome chr1 = new DiploidChromosome(hap1, hap2);
        
        // create some genotypes
        
        //  g1:
        // [1 1 1 1]
        // [0 0 0 0]
        chrs = new ArrayList<>();
        chrs.add(chr1);
        Genotype g1 = new Genotype(chrs);
        
        // construct seed lot obtained by selfing genotype g1
        
        SeedLot sl = seedLotConstructor.self(g1);
        
        // verify obtained genotypes inside seed lot
        assertEquals(1, sl.nrOfGenotypes());
        assertTrue(sl.contains(ideotype));
        
        // repeat with default constructor
        seedLotConstructor = new DefaultSeedLotConstructor(map);
        SeedLot sl2 = seedLotConstructor.self(g1);
        assertEquals((16*17)/2, sl2.nrOfGenotypes());
        assertTrue(sl2.contains(ideotype));
        
        // compare probabilities and linkage phase ambiguities for all heuristically generated genotypes
        for(Genotype g : sl.getGenotypes()){
            assertEquals(sl.getGenotypeGroup(g.getAllelicFrequencies()).getProbabilityOfPhaseKnownGenotype(g),
                         sl2.getGenotypeGroup(g.getAllelicFrequencies()).getProbabilityOfPhaseKnownGenotype(g),
                         1e-10);
            assertEquals(sl.getGenotypeGroup(g.getAllelicFrequencies()).getLinkagePhaseAmbiguity(g),
                         sl2.getGenotypeGroup(g.getAllelicFrequencies()).getLinkagePhaseAmbiguity(g),
                         1e-10);
        }
        
        /***************/
        /* TEST CASE 2 */
        /***************/

        // create ideotype
        // [1 1]
        // [1 1]
        hap = new Haplotype(new boolean[]{true, true});
        hap1 = new Haplotype(new boolean[]{true, true});
        chr = new DiploidChromosome(hap, hap1);
        chrs = new ArrayList<>();
        chrs.add(chr);
        ideotype = new Genotype(chrs);
     
        // create genetic map
        double[][] d2 = {
            {100}  
        };
        map = new GeneticMap(d2);
        
        // create seed lot constructor
        seedLotConstructor = new HeuristicSeedLotConstructor(map, ideotype, GenestackerConstants.UNLIMITED_CROSSOVERS, true);

        // create some haplotypes
        hap2 = new Haplotype(new boolean[]{false, true,});
        Haplotype hap3 = new Haplotype(new boolean[]{true, false});
        
        // create some chromosomes
        chr1 = new DiploidChromosome(hap2, hap3);
        
        // create some genotypes
        
        //  g1:
        // [0 1]
        // [1 0]
        chrs = new ArrayList<>();
        chrs.add(chr1);
        g1 = new Genotype(chrs);
        
        // construct seed lot obtained by selfing genotype g1
        
        sl = seedLotConstructor.self(g1);
        
        // verify obtained genotypes inside seed lot
        assertEquals(6, sl.nrOfGenotypes());
        assertTrue(sl.contains(ideotype));
        
        // repeat for default seed lot constructor
        seedLotConstructor = new DefaultSeedLotConstructor(map);
        sl2 = seedLotConstructor.self(g1);
        
        assertEquals(10, sl2.nrOfGenotypes());
        assertTrue(sl2.contains(ideotype));
        
        // compare probabilities and linkage phase ambiguities for all heuristically generated genotypes
        for(Genotype g : sl.getGenotypes()){
            assertEquals(sl.getGenotypeGroup(g.getAllelicFrequencies()).getProbabilityOfPhaseKnownGenotype(g),
                         sl2.getGenotypeGroup(g.getAllelicFrequencies()).getProbabilityOfPhaseKnownGenotype(g),
                         1e-10);
            assertEquals(sl.getGenotypeGroup(g.getAllelicFrequencies()).getLinkagePhaseAmbiguity(g),
                         sl2.getGenotypeGroup(g.getAllelicFrequencies()).getLinkagePhaseAmbiguity(g),
                         1e-10);
        }
        
        /***************/
        /* TEST CASE 3 */
        /***************/

        // create ideotype
        hap = new Haplotype(new boolean[]{true, true, true, true});
        hap1 = new Haplotype(new boolean[]{false, false, false, false});
        chr = new DiploidChromosome(hap, hap1);
        chrs = new ArrayList<>();
        chrs.add(chr);
        ideotype = new Genotype(chrs);
     
        // create genetic map
        double[][] d3 = {
            {100, 1, 100}  
        };
        map = new GeneticMap(d3);
        
        // create seed lot constructor
        seedLotConstructor = new HeuristicSeedLotConstructor(map, ideotype, GenestackerConstants.UNLIMITED_CROSSOVERS, true);
        
        // create some chromosomes
        chr1 = new DiploidChromosome(hap, hap1);
        
        // create some genotypes
        
        //  g1:
        // [1 1 1 1]
        // [0 0 0 0]
        chrs = new ArrayList<>();
        chrs.add(chr1);
        g1 = new Genotype(chrs);
        
        // construct seed lot obtained by selfing genotype g1
        
        sl = seedLotConstructor.self(g1);
        /*for(Genotype g : sl.getGenotypes()){
            System.out.println("p(O(g)) = " + sl.getGenotypeGroup(g.getAllelicFrequencies()).getProbability());
            System.out.println(g);
        }*/
        
        // verify obtained genotypes inside seed lot
        assertEquals(3, sl.nrOfGenotypes());
        assertTrue(sl.contains(ideotype));
        
        // redo with default seed lot constructor
        seedLotConstructor = new DefaultSeedLotConstructor(map);
        sl2 = seedLotConstructor.self(g1);
        
        assertEquals(136, sl2.nrOfGenotypes());
        assertTrue(sl2.contains(ideotype));
        
        // compare probabilities and linkage phase ambiguities for all heuristically generated genotypes
        for(Genotype g : sl.getGenotypes()){
            assertEquals(sl.getGenotypeGroup(g.getAllelicFrequencies()).getProbabilityOfPhaseKnownGenotype(g),
                         sl2.getGenotypeGroup(g.getAllelicFrequencies()).getProbabilityOfPhaseKnownGenotype(g),
                         1e-10);
            assertEquals(sl.getGenotypeGroup(g.getAllelicFrequencies()).getLinkagePhaseAmbiguity(g),
                         sl2.getGenotypeGroup(g.getAllelicFrequencies()).getLinkagePhaseAmbiguity(g),
                         1e-10);
        }
                
    }

}