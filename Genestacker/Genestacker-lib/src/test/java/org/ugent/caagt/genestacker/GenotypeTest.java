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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import junit.framework.TestCase;
import org.junit.Test;
import org.ugent.caagt.genestacker.exceptions.GenestackerException;
import org.ugent.caagt.genestacker.exceptions.GenotypeException;
import org.ugent.caagt.genestacker.search.bb.DefaultSeedLotConstructor;
import org.ugent.caagt.genestacker.search.bb.SeedLotConstructor;

/**
 *
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class GenotypeTest extends TestCase {
    
    private static final Random rg = new Random();
    
    public GenotypeTest(String testName) {
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
    public void testSwappedHaplotypes() throws GenestackerException{
        
        // create some haplotypes
        
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
        // hom7: [1 0]
        Haplotype hom7 = new Haplotype(new boolean[]{true, false});
        // hom8: [1 1]
        Haplotype hom8 = new Haplotype(new boolean[]{true, true});
        
        // create some diploid chromosomes
        
        DiploidChromosome chr1_1 = new DiploidChromosome(hom1, hom2);
        DiploidChromosome chr1_2 = new DiploidChromosome(hom2, hom1);
        
        DiploidChromosome chr2_1 = new DiploidChromosome(hom3, hom4);
        DiploidChromosome chr2_2 = new DiploidChromosome(hom4, hom3);
        
        DiploidChromosome chr3_1 = new DiploidChromosome(hom5, hom6);
        DiploidChromosome chr3_2 = new DiploidChromosome(hom6, hom5);
        
        DiploidChromosome chr4_1 = new DiploidChromosome(hom7, hom8);
        DiploidChromosome chr4_2 = new DiploidChromosome(hom8, hom7);
        
        // create some genotypes
        
        List<DiploidChromosome> chroms = new ArrayList<>();
        chroms.add(chr1_1);
        chroms.add(chr2_1);
        Genotype g1 = new Genotype(chroms);
        
        chroms = new ArrayList<>();
        chroms.add(chr1_1);
        chroms.add(chr2_2);
        Genotype g2 = new Genotype(chroms);
        
        chroms = new ArrayList<>();
        chroms.add(chr1_2);
        chroms.add(chr2_1);
        Genotype g3 = new Genotype(chroms);
        
        chroms = new ArrayList<>();
        chroms.add(chr1_2);
        chroms.add(chr2_2);
        Genotype g4 = new Genotype(chroms);
        
        // check equality of genotypes with swapped haplotypes at some chromosomes
        
        assertEquals(g1, g2);
        assertEquals(g1, g3);
        assertEquals(g1, g4);
        
        // create some more genotypes
        
        chroms = new ArrayList<>();
        chroms.add(chr3_1);
        chroms.add(chr4_1);
        Genotype g5 = new Genotype(chroms);
        
        chroms = new ArrayList<>();
        chroms.add(chr3_1);
        chroms.add(chr4_2);
        Genotype g6 = new Genotype(chroms);
        
        chroms = new ArrayList<>();
        chroms.add(chr3_2);
        chroms.add(chr4_1);
        Genotype g7 = new Genotype(chroms);
        
        chroms = new ArrayList<>();
        chroms.add(chr3_2);
        chroms.add(chr4_2);
        Genotype g8 = new Genotype(chroms);
        
        // check equality
        
        assertEquals(g5, g6);
        assertEquals(g5, g7);
        assertEquals(g5, g8);
        
    }

    /**
     * Test of derivePossibleGametes method, of class Genotype.
     */
    @Test
    public void testDerivePossibleGametes() throws GenestackerException{
        System.out.println("\n### DERIVE GAMETES ###\n");
        
        /****************************************/
        /* TESTS WITH ONE TARGET PER CHROMOSOME */
        /****************************************/
       
        // Create empty genetic map:
        //  - 2 chromosomes
        //  - 1 target per chromosome
        double[][] distances = new double[][]{new double[]{}, new double[]{}};
        GeneticMap map = new GeneticMap(distances, new HaldaneMapFunction());
        DefaultSeedLotConstructor seedLotConstructor = new DefaultSeedLotConstructor(map);
        
        // Genotype:
        // [0][0]
        // [1][1]
        Haplotype hom1 = new Haplotype(new boolean[]{false});
        Haplotype hom2 = new Haplotype(new boolean[]{true});
        DiploidChromosome chrom1 = new DiploidChromosome(hom1, hom2);
        Haplotype hom3 = new Haplotype(new boolean[]{false});
        Haplotype hom4 = new Haplotype(new boolean[]{true});
        DiploidChromosome chrom2 = new DiploidChromosome(hom3, hom4);
        List<DiploidChromosome> chromosomes = new ArrayList<>();
        chromosomes.add(chrom1);
        chromosomes.add(chrom2);
        Genotype genotype = new Genotype(chromosomes);
        List<Map<Haplotype, Double>> gametesPerChrom = seedLotConstructor.genGametesPerChromosome(genotype);
        assertEquals(2, gametesPerChrom.get(0).size());
        assertEquals(2, gametesPerChrom.get(1).size());
        // Compare with expected gametes:
        // - gamete 1        
        Haplotype g1hom1 = new Haplotype(new boolean[]{false});
        Haplotype g1hom2 = new Haplotype(new boolean[]{false});
        assertEquals(0.25, gametesPerChrom.get(0).get(g1hom1)* gametesPerChrom.get(1).get(g1hom2));
        // - gamete 2    
        Haplotype g2hom1 = new Haplotype(new boolean[]{false});
        Haplotype g2hom2 = new Haplotype(new boolean[]{true});
        assertEquals(0.25, gametesPerChrom.get(0).get(g2hom1)* gametesPerChrom.get(1).get(g2hom2));
        // - gamete 1        
        Haplotype g3hom1 = new Haplotype(new boolean[]{true});
        Haplotype g3hom2 = new Haplotype(new boolean[]{false});
        assertEquals(0.25, gametesPerChrom.get(0).get(g3hom1)* gametesPerChrom.get(1).get(g3hom2));
        // - gamete 4        
        Haplotype g4hom1 = new Haplotype(new boolean[]{true});
        Haplotype g4hom2 = new Haplotype(new boolean[]{true});
        assertEquals(0.25, gametesPerChrom.get(0).get(g4hom1)* gametesPerChrom.get(1).get(g4hom2));
        
        // Genotype:
        // [0][1]
        // [1][1]
        hom1 = new Haplotype(new boolean[]{false});
        hom2 = new Haplotype(new boolean[]{true});
        chrom1 = new DiploidChromosome(hom1, hom2);
        hom3 = new Haplotype(new boolean[]{true});
        hom4 = new Haplotype(new boolean[]{true});
        chrom2 = new DiploidChromosome(hom3, hom4);
        chromosomes = new ArrayList<>();
        chromosomes.add(chrom1);
        chromosomes.add(chrom2);
        genotype = new Genotype(chromosomes);
        gametesPerChrom = seedLotConstructor.genGametesPerChromosome(genotype);
        assertEquals(2, gametesPerChrom.get(0).size());
        assertEquals(1, gametesPerChrom.get(1).size());
        // Compare with expected gametes:
        // - gamete 1        
        g1hom1 = new Haplotype(new boolean[]{false});
        g1hom2 = new Haplotype(new boolean[]{true});
        assertEquals(0.5, gametesPerChrom.get(0).get(g1hom1)* gametesPerChrom.get(1).get(g1hom2));
        // - gamete 2    
        g2hom1 = new Haplotype(new boolean[]{true});
        g2hom2 = new Haplotype(new boolean[]{true});
        assertEquals(0.5, gametesPerChrom.get(0).get(g2hom1)* gametesPerChrom.get(1).get(g2hom2));
        
        // Genotype:
        // [1][1]
        // [1][1]
        hom1 = new Haplotype(new boolean[]{true});
        hom2 = new Haplotype(new boolean[]{true});
        chrom1 = new DiploidChromosome(hom1, hom2);
        hom3 = new Haplotype(new boolean[]{true});
        hom4 = new Haplotype(new boolean[]{true});
        chrom2 = new DiploidChromosome(hom3, hom4);
        chromosomes = new ArrayList<>();
        chromosomes.add(chrom1);
        chromosomes.add(chrom2);
        genotype = new Genotype(chromosomes);
        gametesPerChrom = seedLotConstructor.genGametesPerChromosome(genotype);
        assertEquals(1, gametesPerChrom.get(0).size());
        assertEquals(1, gametesPerChrom.get(1).size());
        // Compare with expected gametes:
        // - gamete 1        
        g1hom1 = new Haplotype(new boolean[]{true});
        g1hom2 = new Haplotype(new boolean[]{true});
        assertEquals(1.0, gametesPerChrom.get(0).get(g1hom1)* gametesPerChrom.get(0).get(g1hom2));
        
        /*****************************************************/
        /* NOW TEST WITH MULTIPLE TARGETS ON SAME CHROMOSOME */
        /*****************************************************/
        
        DecimalFormat df = new DecimalFormat("#.#####");

        // 1 chromosome, 4 targets: equal distance of 20 cM
        distances = new double[][]{new double[]{20, 20, 20}};
        map = new GeneticMap(distances, new HaldaneMapFunction());
        seedLotConstructor = new DefaultSeedLotConstructor(map);
        
        // Genotype:
        // [1 0 0 0]
        // [1 0 0 0]
        hom1 = new Haplotype(new boolean[]{true, false, false, false});
        hom2 = new Haplotype(hom1);
        chrom1 = new DiploidChromosome(hom1, hom2);
        chromosomes = new ArrayList<>();
        chromosomes.add(chrom1);
        genotype = new Genotype(chromosomes);
        gametesPerChrom = seedLotConstructor.genGametesPerChromosome(genotype);
        // check nr of gametes
        assertEquals(1, gametesPerChrom.get(0).size());
        // check gametes
        hom3 = new Haplotype(hom1);
        assertEquals(1.0, gametesPerChrom.get(0).get(hom3));
        
        // Genotype:
        // [0 1 0 0]
        // [1 0 0 0]
        hom1 = new Haplotype(new boolean[]{false, true, false, false});
        hom2 = new Haplotype(new boolean[]{true, false, false, false});
        chrom1 = new DiploidChromosome(hom1, hom2);
        chromosomes = new ArrayList<>();
        chromosomes.add(chrom1);
        genotype = new Genotype(chromosomes);
        gametesPerChrom = seedLotConstructor.genGametesPerChromosome(genotype);
        // check nr of gametes
        assertEquals(4, gametesPerChrom.get(0).size());
        // check gametes
        double precision = 0.00001;
        // g1: [0 1 0 0]
        Haplotype hom = new Haplotype(hom1);
        assertEquals(0.41758, gametesPerChrom.get(0).get(hom), precision);
        // g2: [1 0 0 0]
        hom = new Haplotype(hom2);
        assertEquals(0.41758, gametesPerChrom.get(0).get(hom), precision);
        // g3: [0 0 0 0]
        hom = new Haplotype(new boolean[]{false, false, false, false});
        assertEquals(0.08242, gametesPerChrom.get(0).get(hom), precision);
        // g4: [1 1 0 0]
        hom = new Haplotype(new boolean[]{true, true, false, false});
        assertEquals(0.08242, gametesPerChrom.get(0).get(hom), precision);
        
        // Genotype:
        // [0 1 1 0]
        // [1 0 0 1]
        hom1 = new Haplotype(new boolean[]{false, true, true, false});
        hom2 = new Haplotype(new boolean[]{true, false, false, true});
        chrom1 = new DiploidChromosome(hom1, hom2);
        chromosomes = new ArrayList<>();
        chromosomes.add(chrom1);
        genotype = new Genotype(chromosomes);
        gametesPerChrom = seedLotConstructor.genGametesPerChromosome(genotype);
        df = new DecimalFormat("#.#########");
        // check nr of gametes
        assertEquals(16, gametesPerChrom.get(0).size());
        // check gametes
        precision = 0.000000001;
        double a = 0.011346575;
        double b = 0.057487304;
        double c = 0.002239534;
        double d = 0.291258828;
        // g1: [0 0 0 0]
        hom = new Haplotype(new boolean[]{false, false, false, false});
        assertEquals(a, gametesPerChrom.get(0).get(hom), precision);
        // g2: [0 0 0 1]
        hom = new Haplotype(new boolean[]{false, false, false, true});
        assertEquals(b, gametesPerChrom.get(0).get(hom), precision);
        // g3: [0 0 1 0]
        hom = new Haplotype(new boolean[]{false, false, true, false});
        assertEquals(a, gametesPerChrom.get(0).get(hom), precision);
        // g4: [0 0 1 1]
        hom = new Haplotype(new boolean[]{false, false, true, true});
        assertEquals(c, gametesPerChrom.get(0).get(hom), precision);
        // g5: [0 1 0 0]
        hom = new Haplotype(new boolean[]{false, true, false, false});
        assertEquals(a, gametesPerChrom.get(0).get(hom), precision);
        // g6: [0 1 0 1]
        hom = new Haplotype(new boolean[]{false, true, false, true});
        assertEquals(b, gametesPerChrom.get(0).get(hom), precision);
        // g7: [0 1 1 0]
        hom = new Haplotype(new boolean[]{false, true, true, false});
        assertEquals(d, gametesPerChrom.get(0).get(hom), precision);
        // g8: [0 1 1 1]
        hom = new Haplotype(new boolean[]{false, true, true, true});
        assertEquals(b, gametesPerChrom.get(0).get(hom), precision);
        // g9: [1 0 0 0]
        hom = new Haplotype(new boolean[]{true, false, false, false});
        assertEquals(b, gametesPerChrom.get(0).get(hom), precision);
        // g10: [1 0 0 1]
        hom = new Haplotype(new boolean[]{true, false, false, true});
        assertEquals(d, gametesPerChrom.get(0).get(hom), precision);
        // g11: [1 0 1 0]
        hom = new Haplotype(new boolean[]{true, false, true, false});
        assertEquals(b, gametesPerChrom.get(0).get(hom), precision);
        // g12: [1 0 1 1]
        hom = new Haplotype(new boolean[]{true, false, true, true});
        assertEquals(a, gametesPerChrom.get(0).get(hom), precision);
        // g13: [1 1 0 0]
        hom = new Haplotype(new boolean[]{true, true, false, false});
        assertEquals(c, gametesPerChrom.get(0).get(hom), precision);
        // g14: [1 1 0 1]
        hom = new Haplotype(new boolean[]{true, true, false, true});
        assertEquals(a, gametesPerChrom.get(0).get(hom), precision);
        // g15: [1 1 1 0]
        hom = new Haplotype(new boolean[]{true, true, true, false});
        assertEquals(b, gametesPerChrom.get(0).get(hom), precision);
        // g16: [1 1 1 1]
        hom = new Haplotype(new boolean[]{true, true, true, true});
        assertEquals(a, gametesPerChrom.get(0).get(hom), precision);
        
        // 1 chromosome, 4 targets:
        //  - distance between first three targets: 10000 cM
        //  - distance between 3th and 4th locus: 1 cM
        distances  = new double[][]{new double[]{10000, 10000, 1}};
        map = new GeneticMap(distances, new HaldaneMapFunction());
        seedLotConstructor = new DefaultSeedLotConstructor(map);
        
        // Genotype:
        // [0 0 0 1]
        // [1 1 1 0]
        hom1 = new Haplotype(new boolean[]{false, false, false, true});
        hom2 = new Haplotype(new boolean[]{true, true, true, false});
        chrom1 = new DiploidChromosome(hom1, hom2);
        chromosomes = new ArrayList<>();
        chromosomes.add(chrom1);
        genotype = new Genotype(chromosomes);
        gametesPerChrom = seedLotConstructor.genGametesPerChromosome(genotype);
        df = new DecimalFormat("#.#########");
        // check nr of gametes
        assertEquals(16, gametesPerChrom.get(0).size());
        // check gametes
        precision = 0.000000000000001;
        a = 0.001237582918328;
        b = 0.123762417081672;
        // g1: [0 0 0 0]
        hom = new Haplotype(new boolean[]{false, false, false, false});
        assertEquals(a, gametesPerChrom.get(0).get(hom), precision);
        // g2: [0 0 0 1]
        hom = new Haplotype(new boolean[]{false, false, false, true});
        assertEquals(b, gametesPerChrom.get(0).get(hom), precision);
        // g3: [0 0 1 0]
        hom = new Haplotype(new boolean[]{false, false, true, false});
        assertEquals(b, gametesPerChrom.get(0).get(hom), precision);
        // g4: [0 0 1 1]
        hom = new Haplotype(new boolean[]{false, false, true, true});
        assertEquals(a, gametesPerChrom.get(0).get(hom), precision);
        // g5: [0 1 0 0]
        hom = new Haplotype(new boolean[]{false, true, false, false});
        assertEquals(a, gametesPerChrom.get(0).get(hom), precision);
        // g6: [0 1 0 1]
        hom = new Haplotype(new boolean[]{false, true, false, true});
        assertEquals(b, gametesPerChrom.get(0).get(hom), precision);
        // g7: [0 1 1 0]
        hom = new Haplotype(new boolean[]{false, true, true, false});
        assertEquals(b, gametesPerChrom.get(0).get(hom), precision);
        // g8: [0 1 1 1]
        hom = new Haplotype(new boolean[]{false, true, true, true});
        assertEquals(a, gametesPerChrom.get(0).get(hom), precision);
        // g9: [1 0 0 0]
        hom = new Haplotype(new boolean[]{true, false, false, false});
        assertEquals(a, gametesPerChrom.get(0).get(hom), precision);
        // g10: [1 0 0 1]
        hom = new Haplotype(new boolean[]{true, false, false, true});
        assertEquals(b, gametesPerChrom.get(0).get(hom), precision);
        // g11: [1 0 1 0]
        hom = new Haplotype(new boolean[]{true, false, true, false});
        assertEquals(b, gametesPerChrom.get(0).get(hom), precision);
        // g12: [1 0 1 1]
        hom = new Haplotype(new boolean[]{true, false, true, true});
        assertEquals(a, gametesPerChrom.get(0).get(hom), precision);
        // g13: [1 1 0 0]
        hom = new Haplotype(new boolean[]{true, true, false, false});
        assertEquals(a, gametesPerChrom.get(0).get(hom), precision);
        // g14: [1 1 0 1]
        hom = new Haplotype(new boolean[]{true, true, false, true});
        assertEquals(b, gametesPerChrom.get(0).get(hom), precision);
        // g15: [1 1 1 0]
        hom = new Haplotype(new boolean[]{true, true, true, false});
        assertEquals(b, gametesPerChrom.get(0).get(hom), precision);
        // g16: [1 1 1 1]
        hom = new Haplotype(new boolean[]{true, true, true, true});
        assertEquals(a, gametesPerChrom.get(0).get(hom), precision);
        
        /************************************************/
        /* TEST IF SUM OF PROBABILITIES ALWAYS EQUALS 1 */
        /************************************************/
        
        System.out.println("\n### GAMETE PROBABILITY FOR RANDOM GENOTYPES (SUM = 1.0 within each chromosome) ###\n");
        
        precision = 0.0000001;
        for(int i=0; i<25; i++){
            Genotype g = genRandomGenotype();
            System.out.println(g);
            seedLotConstructor = new DefaultSeedLotConstructor(genRandomGeneticMap(g));
            gametesPerChrom = seedLotConstructor.genGametesPerChromosome(g);
            for(int chrom=0; chrom<g.nrOfChromosomes(); chrom++){
                double sum = 0;
                for(Double p : gametesPerChrom.get(chrom).values()){
                    sum += p;
                }
                assertEquals(1.0, sum, precision);
            }
        }

    }
    
    @Test
    public void testEquals() throws GenestackerException{
        // create some haplotypes
        Haplotype hom1 = new Haplotype(new boolean[]{true, true, true, true, true});
        Haplotype hom2 = new Haplotype(new boolean[]{false, true, false, true, true});
        Haplotype hom3 = new Haplotype(new boolean[]{true, true, true, true, true});
        Haplotype hom4 = new Haplotype(new boolean[]{false, true, false, true, true});
        Haplotype hom5 = new Haplotype(new boolean[]{true, true, true, true, true});
        Haplotype hom6 = new Haplotype(new boolean[]{false, false, false, false, false});
        
        // create some diploid chromosomes
        
        // chr1:
        // [1 1 1 1 1]
        // [0 1 0 1 1]
        DiploidChromosome chr1 = new DiploidChromosome(hom1, hom2);
        // chr2:
        // [1 1 1 1 1]
        // [0 1 0 1 1]
        DiploidChromosome chr2 = new DiploidChromosome(hom1, hom2);
        // chr3:
        // [1 1 1 1 1]
        // [0 1 0 1 1]
        DiploidChromosome chr3 = new DiploidChromosome(hom3, hom4);
        // chr4:
        // [1 1 1 1 1]
        // [0 0 0 0 0]
        DiploidChromosome chr4 = new DiploidChromosome(hom5, hom6);
        
        // create some genotypes
        
        // g1:
        // [1 1 1 1 1]
        // [0 1 0 1 1]
        List<DiploidChromosome> g1chroms = new ArrayList<>();
        g1chroms.add(chr1);
        Genotype g1 = new Genotype(g1chroms);
        // g2:
        // [1 1 1 1 1] [1 1 1 1 1]
        // [0 1 0 1 1] [0 1 0 1 1]
        List<DiploidChromosome> g2chroms = new ArrayList<>();
        g2chroms.add(chr2);
        g2chroms.add(chr3);
        Genotype g2 = new Genotype(g2chroms);
        // g3:
        // [1 1 1 1 1]
        // [0 1 0 1 1]
        List<DiploidChromosome> g3chroms = new ArrayList<>();
        g3chroms.add(chr3);
        Genotype g3 = new Genotype(g3chroms);
        // g4:
        // [1 1 1 1 1] [1 1 1 1 1]
        // [0 1 0 1 1] [0 0 0 0 0]
        List<DiploidChromosome> g4chroms = new ArrayList<>();
        g4chroms.add(chr3);
        g4chroms.add(chr4);
        Genotype g4 = new Genotype(g4chroms);
        
        // tests for g1
        assertEquals(g1, g1);
        assertFalse(g1.equals(g2));
        assertEquals(g1, g3);
        assertFalse(g1.equals(g4));
        // tests for g2
        assertFalse(g2.equals(g1));
        assertEquals(g2, g2);
        assertFalse(g2.equals(g3));
        assertFalse(g2.equals(g4));
        // tests for g3
        assertEquals(g3, g1);
        assertFalse(g3.equals(g2));
        assertEquals(g3, g3);
        assertFalse(g3.equals(g4));
        // tests for g4
        assertFalse(g4.equals(g1));
        assertFalse(g4.equals(g2));
        assertFalse(g4.equals(g3));
        assertEquals(g4, g4);
        
    }
    
    @Test
    public void testCrossWith() throws GenestackerException{
        
        // create some haplotypes
        Haplotype hom1 = new Haplotype(new boolean[]{true});  // [1]
        Haplotype hom2 = new Haplotype(new boolean[]{false}); // [0]
        Haplotype hom3 = new Haplotype(new boolean[]{false, true}); // [0 1]
        Haplotype hom4 = new Haplotype(new boolean[]{false, false}); // [0 0]

        // create some diploid chromosomes
        
        // chr1:
        // [0]
        // [0]
        DiploidChromosome chr1 = new DiploidChromosome(hom2, hom2);
        // chr2:
        // [0]
        // [1]
        DiploidChromosome chr2 = new DiploidChromosome(hom2, hom1);
        // chr3:
        // [1]
        // [0]
        DiploidChromosome chr3 = new DiploidChromosome(hom1, hom2);
        // chr4:
        // [1]
        // [1]
        DiploidChromosome chr4 = new DiploidChromosome(hom1, hom1);
        // chr5:
        // [0 1]
        // [0 0]
        DiploidChromosome chr5 = new DiploidChromosome(hom3, hom4);
        
        // create some genotypes
        
        // g1:
        // [0][0]
        // [0][0]
        List<DiploidChromosome> g1chroms = new ArrayList<>();
        g1chroms.add(chr1);
        g1chroms.add(chr1);
        Genotype g1 = new Genotype(g1chroms);
        // g2:
        // [0][0]
        // [0][1]
        List<DiploidChromosome> g2chroms = new ArrayList<>();
        g2chroms.add(chr1);
        g2chroms.add(chr2);
        Genotype g2 = new Genotype(g2chroms);
        // g3:
        // [0][0]
        // [1][0]
        List<DiploidChromosome> g3chroms = new ArrayList<>();
        g3chroms.add(chr2);
        g3chroms.add(chr1);
        Genotype g3 = new Genotype(g3chroms);
        // g4:
        // [0][0]
        // [1][1]
        List<DiploidChromosome> g4chroms = new ArrayList<>();
        g4chroms.add(chr2);
        g4chroms.add(chr2);
        Genotype g4 = new Genotype(g4chroms);
        // g5:
        // [0]
        // [1]
        List<DiploidChromosome> g5chroms = new ArrayList<>();
        g5chroms.add(chr2);
        Genotype g5 = new Genotype(g5chroms);
        // g6:
        // [0 1]
        // [0 0]
        List<DiploidChromosome> g6chroms = new ArrayList<>();
        g6chroms.add(chr5);
        Genotype g6 = new Genotype(g6chroms);
        // g7:
        // [1][1]
        // [1][1]
        List<DiploidChromosome> g7chroms = new ArrayList<>();
        g7chroms.add(chr4);
        g7chroms.add(chr4);
        Genotype g7 = new Genotype(g7chroms);
        // g8:
        // [1][0]
        // [0][0]
        List<DiploidChromosome> g8chroms = new ArrayList<>();
        g8chroms.add(chr3);
        g8chroms.add(chr1);
        Genotype g8 = new Genotype(g8chroms);
        // g9:
        // [1][0]
        // [1][0]
        List<DiploidChromosome> g9chroms = new ArrayList<>();
        g9chroms.add(chr4);
        g9chroms.add(chr1);
        Genotype g9 = new Genotype(g9chroms);
        
        /**********************************/
        /* TEST INCOMPATIBILITY EXCEPTION */
        /**********************************/
        
        SeedLotConstructor seedLotConstructor = new DefaultSeedLotConstructor(null);
        boolean thrown = false;
        try{
            seedLotConstructor.cross(g4, g5);
        } catch (GenotypeException ex){
            thrown = true;
        }
        assertTrue(thrown);
        
        thrown = false;
        try{
           seedLotConstructor.cross(g5, g6);
        } catch (GenotypeException ex){
            thrown = true;
        }
        assertTrue(thrown);
        
        thrown = false;
        try{
            seedLotConstructor.cross(g6, g4);
        } catch (GenotypeException ex){
            thrown = true;
        }
        assertTrue(thrown);
        
        /**************************************************************/
        /* PERFORM COMPATIBLE CROSSINGS: SINGLE TARGET PER CHROMOSOME */
        /**************************************************************/
        
        double[][] distances = new double[][]{new double[]{}, new double[]{}};
        GeneticMap map = new GeneticMap(distances, new HaldaneMapFunction());
        seedLotConstructor = new DefaultSeedLotConstructor(map);
        
        System.out.println("\n### CROSSINGS ###\n");
        
        //   g1     x    g4:
        // [0][0]      [0][0]
        // [0][0]      [1][1]
        System.out.println("---");
        System.out.println("Parent 1:\n" + g1);
        System.out.println("Parent 2:\n" + g4);
        System.out.println("---");
        SeedLot offspring = seedLotConstructor.cross(g1, g4);
        // inspect offspring
        System.out.println("Offspring:");
        for(GenotypeAllelicFrequencies state : offspring.getAllelicFrequencies()){
            System.out.println("p=" + offspring.getGenotypeGroup(state).getProbabilityOfGenotypeWithArbitraryLinkagePhase()+ ":");
            System.out.println(state);
        }
        assertEquals(4, offspring.nrOfGenotypes());
        assertEquals(0.25, offspring.getGenotypeGroup(g1.getAllelicFrequencies()).getProbabilityOfPhaseKnownGenotype(g1));
        assertEquals(0.25, offspring.getGenotypeGroup(g2.getAllelicFrequencies()).getProbabilityOfPhaseKnownGenotype(g2));
        assertEquals(0.25, offspring.getGenotypeGroup(g3.getAllelicFrequencies()).getProbabilityOfPhaseKnownGenotype(g3));
        assertEquals(0.25, offspring.getGenotypeGroup(g4.getAllelicFrequencies()).getProbabilityOfPhaseKnownGenotype(g4));
        
        //   g1     x    g1:
        // [0][0]      [0][0]
        // [0][0]      [0][0]
        System.out.println("---");
        System.out.println("Selfed parent:\n" + g1);
        System.out.println("---");
        offspring = seedLotConstructor.self(g1);
        // inspect offspring
        System.out.println("Offspring:");
        for(GenotypeAllelicFrequencies state : offspring.getAllelicFrequencies()){
            System.out.println("p=" + offspring.getGenotypeGroup(state).getProbabilityOfGenotypeWithArbitraryLinkagePhase()+ ":");
            System.out.println(state);
        }
        assertEquals(1, offspring.nrOfGenotypes());
        assertEquals(1.0, offspring.getGenotypeGroup(g1.getAllelicFrequencies()).getProbabilityOfPhaseKnownGenotype(g1));
        
        //   g1     x    g7:
        // [0][0]      [1][1]
        // [0][0]      [1][1]
        System.out.println("---");
        System.out.println("Parent 1:\n" + g1);
        System.out.println("Parent 2:\n" + g7);
        System.out.println("---");
        offspring = seedLotConstructor.cross(g1, g7);
        // inspect offspring
        System.out.println("Offspring:");
        for(GenotypeAllelicFrequencies state : offspring.getAllelicFrequencies()){
            System.out.println("p=" + offspring.getGenotypeGroup(state).getProbabilityOfGenotypeWithArbitraryLinkagePhase()+ ":");
            System.out.println(state);
        }
        assertEquals(1, offspring.nrOfGenotypes());
        assertEquals(1.0, offspring.getGenotypeGroup(g4.getAllelicFrequencies()).getProbabilityOfPhaseKnownGenotype(g4));
        
        //   g3     x    g3:
        // [0][0]      [0][0]
        // [1][0]      [1][0]
        System.out.println("---");
        System.out.println("Selfed parent:\n" + g3);
        System.out.println("---");
        offspring = seedLotConstructor.self(g3);
        // inspect offspring
        System.out.println("Offspring:");
        for(GenotypeAllelicFrequencies state : offspring.getAllelicFrequencies()){
            System.out.println("p=" + offspring.getGenotypeGroup(state).getProbabilityOfGenotypeWithArbitraryLinkagePhase()+ ":");
            System.out.println(state);
        }
        assertEquals(3, offspring.nrOfGenotypes());
        assertEquals(0.25, offspring.getGenotypeGroup(g1.getAllelicFrequencies()).getProbabilityOfPhaseKnownGenotype(g1));
        assertEquals(0.5, offspring.getGenotypeGroup(g3.getAllelicFrequencies()).getProbabilityOfPhaseKnownGenotype(g3));
        assertEquals(0.25, offspring.getGenotypeGroup(g9.getAllelicFrequencies()).getProbabilityOfPhaseKnownGenotype(g9));
        
        /*********************************************************************/
        /* PERFORM COMPATIBLE CROSSINGS: MULTIPLE TARGETS ON SAME CHROMOSOME */
        /*********************************************************************/
                
        // 1 chromosome, 4 targets: equally spaced at distance of 20 cM
        distances = new double[][]{new double[]{20, 20, 20}};
        map = new GeneticMap(distances, new HaldaneMapFunction());
        seedLotConstructor = new DefaultSeedLotConstructor(map);
  
        DecimalFormat df = new DecimalFormat("#.####");
        double precision = 0.0001;
        
        // genotype 1:
        // [0 0 0 1]
        // [1 0 0 0]
        hom1 = new Haplotype(new boolean[]{false, false, false, true});
        hom2 = new Haplotype(new boolean[]{true, false, false, false});
        chr1 = new DiploidChromosome(hom1, hom2);
        g1chroms = new ArrayList<>();
        g1chroms.add(chr1);
        g1 = new Genotype(g1chroms);
        // genotype 2:
        // [0 1 0 0]
        // [0 0 1 0]
        hom1 = new Haplotype(new boolean[]{false, true, false, false});
        hom2 = new Haplotype(new boolean[]{false, false, true, false});
        chr1 = new DiploidChromosome(hom1, hom2);
        g2chroms = new ArrayList<>();
        g2chroms.add(chr1);
        g2 = new Genotype(g2chroms);
        // partialCross genotypes
        System.out.println("---");
        System.out.println("Parent 1:\n" + g1);
        System.out.println("Parent 2:\n" + g2);
        System.out.println("---");
        offspring = seedLotConstructor.cross(g1, g2);
        System.out.println("Offspring:");
        for(GenotypeAllelicFrequencies state : offspring.getAllelicFrequencies()){
            System.out.println("p=" + offspring.getGenotypeGroup(state).getProbabilityOfGenotypeWithArbitraryLinkagePhase()+ ":");
            System.out.println(state);
        }
        
        // check offspring size
        assertEquals(16, offspring.nrOfGenotypes());
        
        // check some genotypes among offspring
        
        // child:
        // [1 0 0 1] 
        // [0 1 1 0]
        hom3 = new Haplotype(new boolean[]{true, false, false, true});
        hom4 = new Haplotype(new boolean[]{false, true, true, false});
        chr1 = new DiploidChromosome(hom3, hom4);
        g1chroms = new ArrayList<>();
        g1chroms.add(chr1);
        g1 = new Genotype(g1chroms);
        assertEquals(0.0144, offspring.getGenotypeGroup(g1.getAllelicFrequencies()).getProbabilityOfPhaseKnownGenotype(g1), precision);
        // child:
        // [0 0 0 0] 
        // [0 0 0 0]
        hom3 = new Haplotype(new boolean[]{false, false, false, false});
        hom4 = new Haplotype(new boolean[]{false, false, false, false});
        chr1 = new DiploidChromosome(hom3, hom4);
        g1chroms = new ArrayList<>();
        g1chroms.add(chr1);
        g1 = new Genotype(g1chroms);
        assertEquals(0.0144, offspring.getGenotypeGroup(g1.getAllelicFrequencies()).getProbabilityOfPhaseKnownGenotype(g1), precision);
        // child:
        // [0 0 0 1] 
        // [0 1 1 0]
        hom3 = new Haplotype(new boolean[]{false, false, false, true});
        hom4 = new Haplotype(new boolean[]{false, true, true, false});
        chr1 = new DiploidChromosome(hom3, hom4);
        g1chroms = new ArrayList<>();
        g1chroms.add(chr1);
        g1 = new Genotype(g1chroms);
        assertEquals(0.0268, offspring.getGenotypeGroup(g1.getAllelicFrequencies()).getProbabilityOfPhaseKnownGenotype(g1), precision);
        
        // 1 chromosome, 4 targets:
        //  - first 3 targets at distance 10000 cM
        //  - distance between 3th and 4th target: 1 cM
        distances = new double[][]{new double[]{10000, 10000, 1}};
        map = new GeneticMap(distances, new HaldaneMapFunction());
        seedLotConstructor = new DefaultSeedLotConstructor(map);
        
        // genotype 1:
        // [1 1 1 0]
        // [1 1 1 1]
        hom1 = new Haplotype(new boolean[]{true, true, true, false});
        hom2 = new Haplotype(new boolean[]{true, true, true, true});
        chr1 = new DiploidChromosome(hom1, hom2);
        g1chroms = new ArrayList<>();
        g1chroms.add(chr1);
        g1 = new Genotype(g1chroms);
        // self genotype
        System.out.println("---");
        System.out.println("Selfed parent:\n" + g1);
        System.out.println("---");
        offspring = seedLotConstructor.self(g1);
        System.out.println("Offspring:");
        for(GenotypeAllelicFrequencies state : offspring.getAllelicFrequencies()){
            System.out.println("p=" + offspring.getGenotypeGroup(state).getProbabilityOfGenotypeWithArbitraryLinkagePhase()+ ":");
            System.out.println(state);
        }
        
        // check offspring size
        assertEquals(3, offspring.nrOfGenotypes());
        
        // check some genotypes among offspring
        
        // child:
        // [1 1 1 1] 
        // [1 1 1 1]
        hom3 = new Haplotype(new boolean[]{true, true, true, true});
        hom4 = new Haplotype(new boolean[]{true, true, true, true});
        chr1 = new DiploidChromosome(hom3, hom4);
        g1chroms = new ArrayList<>();
        g1chroms.add(chr1);
        g1 = new Genotype(g1chroms);
        assertEquals(0.25, offspring.getGenotypeGroup(g1.getAllelicFrequencies()).getProbabilityOfPhaseKnownGenotype(g1), precision);
        
        // child:
        // [1 1 1 0] 
        // [1 1 1 1]
        hom3 = new Haplotype(new boolean[]{true, true, true, false});
        hom4 = new Haplotype(new boolean[]{true, true, true, true});
        chr1 = new DiploidChromosome(hom3, hom4);
        g1chroms = new ArrayList<>();
        g1chroms.add(chr1);
        g1 = new Genotype(g1chroms);
        assertEquals(0.5, offspring.getGenotypeGroup(g1.getAllelicFrequencies()).getProbabilityOfPhaseKnownGenotype(g1), precision);
        
        // child:
        // [1 1 1 0] 
        // [1 1 1 0]
        hom3 = new Haplotype(new boolean[]{true, true, true, false});
        hom4 = new Haplotype(new boolean[]{true, true, true, false});
        chr1 = new DiploidChromosome(hom3, hom4);
        g1chroms = new ArrayList<>();
        g1chroms.add(chr1);
        g1 = new Genotype(g1chroms);
        assertEquals(0.25, offspring.getGenotypeGroup(g1.getAllelicFrequencies()).getProbabilityOfPhaseKnownGenotype(g1), precision);
        
        
        /******************************/
        /* TEST CASE WITH MANY CHROMS */
        /******************************/

        // create parents
        hom1 = new Haplotype(new boolean[] {false});
        hom2 = new Haplotype(new boolean[] {true});
        chr1 = new DiploidChromosome(hom1, hom1);
        chr2 = new DiploidChromosome(hom1, hom2);
    
        map = new GeneticMap(new double[6][0]);
        seedLotConstructor = new DefaultSeedLotConstructor(map);
        
        g1chroms = new ArrayList<>();
        g1chroms.add(chr1);
        g1chroms.add(chr2);
        g1chroms.add(chr2);
        g1chroms.add(chr2);
        g1chroms.add(chr2);
        g1chroms.add(chr2);
        g1 = new Genotype(g1chroms);
        
        g2chroms = new ArrayList<>();
        g2chroms.add(chr2);
        g2chroms.add(chr2);
        g2chroms.add(chr2);
        g2chroms.add(chr2);
        g2chroms.add(chr2);
        g2chroms.add(chr1);
        g2 = new Genotype(g2chroms);

        g3chroms = new ArrayList<>();
        g3chroms.add(chr2);
        g3chroms.add(chr2);
        g3chroms.add(chr2);
        g3chroms.add(chr2);
        g3chroms.add(chr2);
        g3chroms.add(chr2);
        g3 = new Genotype(g3chroms);        
        
        offspring = seedLotConstructor.cross(g1, g2);
        assertEquals(1.0/64, offspring.getGenotypeGroup(g3.getAllelicFrequencies()).getProbabilityOfPhaseKnownGenotype(g3), 0.00000001);
        
        
    }
    
    @Test
    public void testIndistinguishableGenotypes() throws GenestackerException{
        
        // create some haplotypes
        Haplotype hom1 = new Haplotype(new boolean[]{false, true}); // [0 1]
        Haplotype hom2 = new Haplotype(new boolean[]{true, false}); // [1 0]
        
        // create diploid chromosome
        // chr:
        // [0 1]
        // [1 0]
        DiploidChromosome chr = new DiploidChromosome(hom1, hom2);
        
        // create parent genotype
        // g1:
        // [0 1]
        // [1 0]
        List<DiploidChromosome> chroms = new ArrayList<>();
        chroms.add(chr);
        Genotype g1 = new Genotype(chroms);
        
        // self g1 (random distance between markers)
        double precision = 0.0000001;

        SeedLotConstructor seedLotConstructor = new DefaultSeedLotConstructor(genRandomGeneticMap(g1));
        SeedLot offspring = seedLotConstructor.self(g1);
        double sum = 0.0;
        for(GenotypeAllelicFrequencies state : offspring.getAllelicFrequencies()){
            System.out.println("---\np=" + offspring.getGenotypeGroup(state).getProbabilityOfGenotypeWithArbitraryLinkagePhase()
                                         + " (" + offspring.nrOfGenotypes(state)  + " genotypes):");
            System.out.println(state);
            sum += offspring.getGenotypeGroup(state).getProbabilityOfGenotypeWithArbitraryLinkagePhase();
        }
        System.out.println("---");
        
        // check sum of probabilities of all observable genotype states
        assertEquals(1.0, sum, precision);
    }
      
    public static Genotype genRandomGenotype() throws GenestackerException{
        int numChroms = rg.nextInt(5) + 1;
        int[] numLociPerChrom = new int[numChroms];
        for(int i=0; i<numChroms; i++){
            numLociPerChrom[i] = rg.nextInt(5) + 1;
        }
        return genRandomGenotype(numChroms, numLociPerChrom);
    }
    
    public static Genotype genRandomGenotype(int numChroms, int[] numLociPerChrom) throws GenestackerException{
        List<DiploidChromosome> chroms = new ArrayList<>(numChroms);
        for(int i=0; i<numChroms; i++){
            boolean[] tmp1 = new boolean[numLociPerChrom[i]];
            boolean[] tmp2 = new boolean[numLociPerChrom[i]];
            for(int j=0; j<numLociPerChrom[i]; j++){
                tmp1[j] = rg.nextBoolean();
                tmp2[j] = rg.nextBoolean();
            }
            Haplotype hom1 = new Haplotype(tmp1);
            Haplotype hom2 = new Haplotype(tmp2);
            DiploidChromosome chrom = new DiploidChromosome(hom1, hom2);
            chroms.add(chrom);
        }
        Genotype g = new Genotype(chroms);
        return g;
    }

    public static GeneticMap genRandomGeneticMap(Genotype g){
        double[][] distances = new double[g.nrOfChromosomes()][];
        for(int i=0; i<distances.length; i++){
            distances[i] = new double[g.getChromosomes().get(i).nrOfLoci()-1];
            for(int j=0; j<distances[i].length; j++){
                distances[i][j] = rg.nextDouble() * 100;
            }
        }
        return new GeneticMap(distances, new HaldaneMapFunction());
    }
    
}
