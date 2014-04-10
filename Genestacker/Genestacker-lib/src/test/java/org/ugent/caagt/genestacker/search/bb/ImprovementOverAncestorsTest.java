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
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ugent.caagt.genestacker.DiploidChromosome;
import org.ugent.caagt.genestacker.GeneticMap;
import org.ugent.caagt.genestacker.Genotype;
import org.ugent.caagt.genestacker.GenotypeTest;
import org.ugent.caagt.genestacker.Haplotype;
import org.ugent.caagt.genestacker.Plant;
import org.ugent.caagt.genestacker.exceptions.GenestackerException;
import org.ugent.caagt.genestacker.search.bb.heuristics.DefaultPlantImprovement;
import org.ugent.caagt.genestacker.search.bb.heuristics.GenotypeImprovement;
import org.ugent.caagt.genestacker.search.bb.heuristics.ImprovementOverAncestorsHeuristic;
import org.ugent.caagt.genestacker.search.bb.heuristics.StrongGenotypeImprovement;
import org.ugent.caagt.genestacker.search.bb.heuristics.WeakGenotypeImprovement;

/**
 *
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class ImprovementOverAncestorsTest extends TestCase {

    private static final Random rg = new Random();
    
    public ImprovementOverAncestorsTest() {
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
    
    @Test
    public void testPruneGrowPlantFromAncestorsWeak() throws GenestackerException{

        System.out.println("\n### TEST (WEAK) IMPROVEMENT OVER ANCESTORS ###\n");
        
        /***************/
        /* TEST CASE 1 */
        /***************/

        // create ideotype
        Haplotype hap = new Haplotype(new boolean[]{true, true, true, true});
        DiploidChromosome chr = new DiploidChromosome(hap, hap);
        List<DiploidChromosome> chrs = new ArrayList<>();
        chrs.add(chr);
        Genotype ideotype = new Genotype(chrs);
        
        // create heuristic
        ImprovementOverAncestorsHeuristic heur = new ImprovementOverAncestorsHeuristic(new DefaultPlantImprovement(new WeakGenotypeImprovement(ideotype)));
        
        // create some haplotypes
        Haplotype hap1 = new Haplotype(new boolean[]{false, false, false, false});
        Haplotype hap2 = new Haplotype(new boolean[]{false, false, true, false});
        Haplotype hap3 = new Haplotype(new boolean[]{false, true, false, false});
        Haplotype hap4 = new Haplotype(new boolean[]{false, true, true, false});
        
        // create some chromosomes
        DiploidChromosome chr1 = new DiploidChromosome(hap1, hap1);
        DiploidChromosome chr2 = new DiploidChromosome(hap1, hap2);
        DiploidChromosome chr3 = new DiploidChromosome(hap2, hap2);
        DiploidChromosome chr4 = new DiploidChromosome(hap2, hap3);
        DiploidChromosome chr5 = new DiploidChromosome(hap4, hap1);        
        
        // create some plants and descriptors (for now, without interest
        // in prob and/or LPA, so we put -1 for these values in each
        // descriptor)
        
        //  p1:
        // [0 0 0 0]
        // [0 0 0 0]
        chrs = new ArrayList<>();
        chrs.add(chr1);
        Genotype g1 = new Genotype(chrs);
        Plant p1 = new Plant(g1);
        PlantDescriptor pd1 = new PlantDescriptor(p1, -1, -1, false);
        
        //  p2:
        // [0 0 0 0]
        // [0 0 1 0]
        chrs = new ArrayList<>();
        chrs.add(chr2);
        Genotype g2 = new Genotype(chrs);
        Plant p2 = new Plant(g2);
        PlantDescriptor pd2 = new PlantDescriptor(p2, -1, -1, false);
        
        //  p3:
        // [0 0 1 0]
        // [0 0 1 0]
        chrs = new ArrayList<>();
        chrs.add(chr3);
        Genotype g3 = new Genotype(chrs);
        Plant p3 = new Plant(g3);
        PlantDescriptor pd3 = new PlantDescriptor(p3, -1, -1, false);
        
        //  p4:
        // [0 0 1 0]
        // [0 1 0 0]
        chrs = new ArrayList<>();
        chrs.add(chr4);
        Genotype g4 = new Genotype(chrs);
        Plant p4 = new Plant(g4);
        PlantDescriptor pd4 = new PlantDescriptor(p4, -1, -1, false);
        
        //  p5:
        // [0 1 1 0]
        // [0 0 0 0]
        chrs = new ArrayList<>();
        chrs.add(chr5);
        Genotype g5 = new Genotype(chrs);
        Plant p5 = new Plant(g5);
        PlantDescriptor pd5 = new PlantDescriptor(p5, -1, -1, false);

        
        /*******************************/
        /* CHECK IMPROVEMENT HEURISTIC */
        /*******************************/
        
        Set<PlantDescriptor> ancestors = new HashSet<>();
        ancestors.add(pd1);
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd1));
        assertFalse(heur.pruneGrowPlantFromAncestors(ancestors, pd2));
        assertFalse(heur.pruneGrowPlantFromAncestors(ancestors, pd3));
        assertFalse(heur.pruneGrowPlantFromAncestors(ancestors, pd4));
        assertFalse(heur.pruneGrowPlantFromAncestors(ancestors, pd5));
        
        ancestors.clear();
        ancestors.add(pd2);
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd1));
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd2));
        assertFalse(heur.pruneGrowPlantFromAncestors(ancestors, pd3));
        assertFalse(heur.pruneGrowPlantFromAncestors(ancestors, pd4));
        assertFalse(heur.pruneGrowPlantFromAncestors(ancestors, pd5));
        
        ancestors.clear();
        ancestors.add(pd3);
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd1));
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd2));
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd3));
        assertFalse(heur.pruneGrowPlantFromAncestors(ancestors, pd4));
        assertFalse(heur.pruneGrowPlantFromAncestors(ancestors, pd5));
        
        ancestors.clear();
        ancestors.add(pd4);
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd1));
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd2));
        assertFalse(heur.pruneGrowPlantFromAncestors(ancestors, pd3));
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd4));
        assertFalse(heur.pruneGrowPlantFromAncestors(ancestors, pd5));
        
        ancestors.clear();
        ancestors.add(pd5);
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd1));
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd2));
        assertFalse(heur.pruneGrowPlantFromAncestors(ancestors, pd3));
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd4));
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd5));
        
        /***************/
        /* TEST CASE 2 */
        /***************/
        
        // create ideotype
        hap = new Haplotype(new boolean[]{true, true, true, true});
        chr = new DiploidChromosome(hap, hap);
        chrs = new ArrayList<>();
        chrs.add(chr);
        ideotype = new Genotype(chrs);
        
        // create heuristic
        heur = new ImprovementOverAncestorsHeuristic(new DefaultPlantImprovement(new WeakGenotypeImprovement(ideotype)));
        
        // create some haplotypes
        hap1 = new Haplotype(new boolean[]{false, false, false, true});
        hap2 = new Haplotype(new boolean[]{true, true, true, false});
        hap3 = new Haplotype(new boolean[]{true, false, true, false});
        
        // create some chromosomes
        chr1 = new DiploidChromosome(hap1, hap1);
        chr2 = new DiploidChromosome(hap1, hap2);
        chr3 = new DiploidChromosome(hap1, hap3);

        // create some plants
        
        //  p1:
        // [0 0 0 1]
        // [0 0 0 1]
        chrs = new ArrayList<>();
        chrs.add(chr1);
        g1 = new Genotype(chrs);
        p1 = new Plant(g1);
        pd1 = new PlantDescriptor(p1, -1, -1, false);
        
        //  p2:
        // [0 0 0 1]
        // [1 1 1 0]
        chrs = new ArrayList<>();
        chrs.add(chr2);
        g2 = new Genotype(chrs);
        p2 = new Plant(g2);
        pd2 = new PlantDescriptor(p2, -1, -1, false);
        
        //  p3:
        // [0 0 0 1]
        // [1 0 1 0]
        chrs = new ArrayList<>();
        chrs.add(chr3);
        g3 = new Genotype(chrs);
        p3 = new Plant(g3);
        pd3 = new PlantDescriptor(p3, -1, -1, false);
        
        /*******************************/
        /* CHECK IMPROVEMENT HEURISTIC */
        /*******************************/
        
        ancestors = new HashSet<>();
        ancestors.add(pd1);
        ancestors.add(pd2);
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd3));
        
        /***************/
        /* TEST CASE 3 */
        /***************/
        
        // now we test with equal genotypes but differing prob/LPA
        
        // create descriptors
        pd1 = new PlantDescriptor(p1, 0.8, 0, false);
        pd2 = new PlantDescriptor(p1, 0.6, 0, false);
        pd3 = new PlantDescriptor(p1, 0.8, 0.1, false);
        pd4 = new PlantDescriptor(p1, 0.6, 0.1, false);
        
        /*******************************/
        /* CHECK IMPROVEMENT HEURISTIC */
        /*******************************/
        
        ancestors = new HashSet<>();
        ancestors.add(pd1);
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd1));
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd2));
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd3));
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd4));
        
        ancestors = new HashSet<>();
        ancestors.add(pd2);
        assertFalse(heur.pruneGrowPlantFromAncestors(ancestors, pd1));
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd2));
        assertFalse(heur.pruneGrowPlantFromAncestors(ancestors, pd3));
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd4));
        
        ancestors = new HashSet<>();
        ancestors.add(pd3);
        assertFalse(heur.pruneGrowPlantFromAncestors(ancestors, pd1));
        assertFalse(heur.pruneGrowPlantFromAncestors(ancestors, pd2));
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd3));
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd4));
        
        ancestors = new HashSet<>();
        ancestors.add(pd4);
        assertFalse(heur.pruneGrowPlantFromAncestors(ancestors, pd1));
        assertFalse(heur.pruneGrowPlantFromAncestors(ancestors, pd2));
        assertFalse(heur.pruneGrowPlantFromAncestors(ancestors, pd3));
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd4));
        
        /***************/
        /* TEST CASE 4 */
        /***************/

        // create ideotype
        hap = new Haplotype(new boolean[]{true, true, true, true, true});
        chr = new DiploidChromosome(hap, hap);
        chrs = new ArrayList<>();
        chrs.add(chr);
        ideotype = new Genotype(chrs);
        
        // create heuristic
        heur = new ImprovementOverAncestorsHeuristic(new DefaultPlantImprovement(new WeakGenotypeImprovement(ideotype)));
        
        // create some haplotypes
        // [0 0 0 1 0]
        hap1 = new Haplotype(new boolean[]{false, false, false, true, false});
        // [1 1 1 0 1]
        hap2 = new Haplotype(new boolean[]{true, true, true, false, true});
        // [0 0 0 1 1]
        hap3 = new Haplotype(new boolean[]{false, false, false, true, true});
        // [1 1 1 0 0]
        hap4 = new Haplotype(new boolean[]{true, true, true, false, false});
        
        // create some chromosomes
        chr1 = new DiploidChromosome(hap1, hap2);
        chr2 = new DiploidChromosome(hap3, hap4);        
        
        // create some plants and descriptors (without any interest
        // in prob and/or LPA, so we put -1 for these values in each
        // descriptor)
        
        //  p1:
        // [0 0 0 1 0]
        // [1 1 1 0 1]
        chrs = new ArrayList<>();
        chrs.add(chr1);
        g1 = new Genotype(chrs);
        p1 = new Plant(g1);
        pd1 = new PlantDescriptor(p1, -1, -1, false);
        
        //  p2:
        // [0 0 0 1 1]
        // [1 1 1 0 0]
        chrs = new ArrayList<>();
        chrs.add(chr2);
        g2 = new Genotype(chrs);
        p2 = new Plant(g2);
        pd2 = new PlantDescriptor(p2, -1, -1, false);
        
        /*******************************/
        /* CHECK IMPROVEMENT HEURISTIC */
        /*******************************/
        
        ancestors = new HashSet<>();
        ancestors.add(pd1);
        assertFalse(heur.pruneGrowPlantFromAncestors(ancestors, pd2));
        
        ancestors = new HashSet<>();
        ancestors.add(pd2);
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd1));
        
        /***************/
        /* TEST CASE 5 */
        /***************/

        // create ideotype
        hap = new Haplotype(new boolean[]{true, true, true, true});
        chr = new DiploidChromosome(hap, hap);
        chrs = new ArrayList<>();
        chrs.add(chr);
        ideotype = new Genotype(chrs);
        
        // create heuristic
        heur = new ImprovementOverAncestorsHeuristic(new DefaultPlantImprovement(new WeakGenotypeImprovement(ideotype)));
        
        // create some haplotypes
        // [0 1 0 1]
        hap1 = new Haplotype(new boolean[]{false, true, false, true});
        // [1 0 1 0]
        hap2 = new Haplotype(new boolean[]{true, false, true, false});
        // [0 1 1 0]
        hap3 = new Haplotype(new boolean[]{false, true, true, false});
        // [1 0 0 1]
        hap4 = new Haplotype(new boolean[]{true, false, false, true});
        
        // create some chromosomes
        chr1 = new DiploidChromosome(hap1, hap2);
        chr2 = new DiploidChromosome(hap3, hap4);        
        
        // create some plants and descriptors (without any interest
        // in prob and/or LPA, so we put -1 for these values in each
        // descriptor)
        
        //  p1:
        // [0 1 0 1]
        // [1 0 1 0]
        chrs = new ArrayList<>();
        chrs.add(chr1);
        g1 = new Genotype(chrs);
        p1 = new Plant(g1);
        pd1 = new PlantDescriptor(p1, -1, -1, false);
        
        //  p2:
        // [0 1 1 0]
        // [1 0 0 1]
        chrs = new ArrayList<>();
        chrs.add(chr2);
        g2 = new Genotype(chrs);
        p2 = new Plant(g2);
        pd2 = new PlantDescriptor(p2, -1, -1, false);
        
        /*******************************/
        /* CHECK IMPROVEMENT HEURISTIC */
        /*******************************/
        
        ancestors = new HashSet<>();
        ancestors.add(pd1);
        assertFalse(heur.pruneGrowPlantFromAncestors(ancestors, pd2));
        
        ancestors = new HashSet<>();
        ancestors.add(pd2);
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd1));
                
    }

    @Test
    public void testPruneGrowPlantFromAncestorsStrong() throws GenestackerException{

        System.out.println("\n### TEST (STRONG) IMPROVEMENT OVER ANCESTORS ###\n");
        
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
        
        // create heuristic
        ImprovementOverAncestorsHeuristic heur = new ImprovementOverAncestorsHeuristic(new DefaultPlantImprovement(new StrongGenotypeImprovement(ideotype, map)));
        
        // create some haplotypes
        Haplotype hap1 = new Haplotype(new boolean[]{false, false, false, false});
        Haplotype hap2 = new Haplotype(new boolean[]{false, false, true, false});
        Haplotype hap3 = new Haplotype(new boolean[]{false, true, false, false});
        Haplotype hap4 = new Haplotype(new boolean[]{false, true, true, false});
        
        // create some chromosomes
        DiploidChromosome chr1 = new DiploidChromosome(hap1, hap1);
        DiploidChromosome chr2 = new DiploidChromosome(hap1, hap2);
        DiploidChromosome chr3 = new DiploidChromosome(hap2, hap2);
        DiploidChromosome chr4 = new DiploidChromosome(hap2, hap3);
        DiploidChromosome chr5 = new DiploidChromosome(hap4, hap1);        
        
        // create some plants and descriptors (for now, without interest
        // in prob and/or LPA, so we put -1 for these values in each
        // descriptor)
        
        //  p1:
        // [0 0 0 0]
        // [0 0 0 0]
        chrs = new ArrayList<>();
        chrs.add(chr1);
        Genotype g1 = new Genotype(chrs);
        Plant p1 = new Plant(g1);
        PlantDescriptor pd1 = new PlantDescriptor(p1, -1, -1, false);
        
        //  p2:
        // [0 0 0 0]
        // [0 0 1 0]
        chrs = new ArrayList<>();
        chrs.add(chr2);
        Genotype g2 = new Genotype(chrs);
        Plant p2 = new Plant(g2);
        PlantDescriptor pd2 = new PlantDescriptor(p2, -1, -1, false);
        
        //  p3:
        // [0 0 1 0]
        // [0 0 1 0]
        chrs = new ArrayList<>();
        chrs.add(chr3);
        Genotype g3 = new Genotype(chrs);
        Plant p3 = new Plant(g3);
        PlantDescriptor pd3 = new PlantDescriptor(p3, -1, -1, false);
        
        //  p4:
        // [0 0 1 0]
        // [0 1 0 0]
        chrs = new ArrayList<>();
        chrs.add(chr4);
        Genotype g4 = new Genotype(chrs);
        Plant p4 = new Plant(g4);
        PlantDescriptor pd4 = new PlantDescriptor(p4, -1, -1, false);
        
        //  p5:
        // [0 1 1 0]
        // [0 0 0 0]
        chrs = new ArrayList<>();
        chrs.add(chr5);
        Genotype g5 = new Genotype(chrs);
        Plant p5 = new Plant(g5);
        PlantDescriptor pd5 = new PlantDescriptor(p5, -1, -1, false);

        
        /*******************************/
        /* CHECK IMPROVEMENT HEURISTIC */
        /*******************************/
        
        Set<PlantDescriptor> ancestors = new HashSet<>();
        ancestors.add(pd1);
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd1));
        assertFalse(heur.pruneGrowPlantFromAncestors(ancestors, pd2));
        assertFalse(heur.pruneGrowPlantFromAncestors(ancestors, pd3));
        assertFalse(heur.pruneGrowPlantFromAncestors(ancestors, pd4));
        assertFalse(heur.pruneGrowPlantFromAncestors(ancestors, pd5));
        
        ancestors.clear();
        ancestors.add(pd2);
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd1));
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd2));
        assertFalse(heur.pruneGrowPlantFromAncestors(ancestors, pd3));
        assertFalse(heur.pruneGrowPlantFromAncestors(ancestors, pd4));
        assertFalse(heur.pruneGrowPlantFromAncestors(ancestors, pd5));
        
        ancestors.clear();
        ancestors.add(pd3);
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd1));
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd2));
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd3));
        assertFalse(heur.pruneGrowPlantFromAncestors(ancestors, pd4));
        assertFalse(heur.pruneGrowPlantFromAncestors(ancestors, pd5));
        
        ancestors.clear();
        ancestors.add(pd4);
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd1));
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd2));
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd3));
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd4));
        assertFalse(heur.pruneGrowPlantFromAncestors(ancestors, pd5));
        
        ancestors.clear();
        ancestors.add(pd5);
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd1));
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd2));
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd3));
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd4));
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd5));
        
        /***************/
        /* TEST CASE 2 */
        /***************/
        
        // create ideotype
        hap = new Haplotype(new boolean[]{true, true, true, true});
        chr = new DiploidChromosome(hap, hap);
        chrs = new ArrayList<>();
        chrs.add(chr);
        ideotype = new Genotype(chrs);
     
        // create genetic map
        d = new double[1][3];
        d[0][0] = 10000;
        d[0][1] = 10000;
        d[0][2] = 1;
        map = new GeneticMap(d);
        
        // create heuristic
        heur = new ImprovementOverAncestorsHeuristic(new DefaultPlantImprovement(new StrongGenotypeImprovement(ideotype, map)));
        
        // create some haplotypes
        hap1 = new Haplotype(new boolean[]{false, false, false, true});
        hap2 = new Haplotype(new boolean[]{true, true, true, false});
        hap3 = new Haplotype(new boolean[]{true, false, true, false});
        
        // create some chromosomes
        chr1 = new DiploidChromosome(hap1, hap1);
        chr2 = new DiploidChromosome(hap1, hap2);
        chr3 = new DiploidChromosome(hap1, hap3);

        // create some plants
        
        //  p1:
        // [0 0 0 1]
        // [0 0 0 1]
        chrs = new ArrayList<>();
        chrs.add(chr1);
        g1 = new Genotype(chrs);
        p1 = new Plant(g1);
        pd1 = new PlantDescriptor(p1, -1, -1, false);
        
        //  p2:
        // [0 0 0 1]
        // [1 1 1 0]
        chrs = new ArrayList<>();
        chrs.add(chr2);
        g2 = new Genotype(chrs);
        p2 = new Plant(g2);
        pd2 = new PlantDescriptor(p2, -1, -1, false);
        
        //  p3:
        // [0 0 0 1]
        // [1 0 1 0]
        chrs = new ArrayList<>();
        chrs.add(chr3);
        g3 = new Genotype(chrs);
        p3 = new Plant(g3);
        pd3 = new PlantDescriptor(p3, -1, -1, false);
        
        /*******************************/
        /* CHECK IMPROVEMENT HEURISTIC */
        /*******************************/
        
        ancestors = new HashSet<>();
        ancestors.add(pd1);
        ancestors.add(pd2);
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd3));
        
        /***************/
        /* TEST CASE 3 */
        /***************/
        
        // now we test with equal genotypes but differing prob/LPA
        
        // create descriptors
        pd1 = new PlantDescriptor(p1, 0.8, 0, false);
        pd2 = new PlantDescriptor(p1, 0.6, 0, false);
        pd3 = new PlantDescriptor(p1, 0.8, 0.1, false);
        pd4 = new PlantDescriptor(p1, 0.6, 0.1, false);
        
        /*******************************/
        /* CHECK IMPROVEMENT HEURISTIC */
        /*******************************/
        
        ancestors = new HashSet<>();
        ancestors.add(pd1);
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd1));
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd2));
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd3));
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd4));
        
        ancestors = new HashSet<>();
        ancestors.add(pd2);
        assertFalse(heur.pruneGrowPlantFromAncestors(ancestors, pd1));
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd2));
        assertFalse(heur.pruneGrowPlantFromAncestors(ancestors, pd3));
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd4));
        
        ancestors = new HashSet<>();
        ancestors.add(pd3);
        assertFalse(heur.pruneGrowPlantFromAncestors(ancestors, pd1));
        assertFalse(heur.pruneGrowPlantFromAncestors(ancestors, pd2));
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd3));
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd4));
        
        ancestors = new HashSet<>();
        ancestors.add(pd4);
        assertFalse(heur.pruneGrowPlantFromAncestors(ancestors, pd1));
        assertFalse(heur.pruneGrowPlantFromAncestors(ancestors, pd2));
        assertFalse(heur.pruneGrowPlantFromAncestors(ancestors, pd3));
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd4));
        
        /***************/
        /* TEST CASE 4 */
        /***************/

        // create ideotype
        hap = new Haplotype(new boolean[]{true, true, true, true, true});
        chr = new DiploidChromosome(hap, hap);
        chrs = new ArrayList<>();
        chrs.add(chr);
        ideotype = new Genotype(chrs);
     
        // create genetic map
        map = GenotypeTest.genRandomGeneticMap(ideotype);
        
        // create heuristic
        heur = new ImprovementOverAncestorsHeuristic(new DefaultPlantImprovement(new StrongGenotypeImprovement(ideotype, map)));
        
        // create some haplotypes
        // [0 0 0 1 0]
        hap1 = new Haplotype(new boolean[]{false, false, false, true, false});
        // [1 1 1 0 1]
        hap2 = new Haplotype(new boolean[]{true, true, true, false, true});
        // [0 0 0 1 1]
        hap3 = new Haplotype(new boolean[]{false, false, false, true, true});
        // [1 1 1 0 0]
        hap4 = new Haplotype(new boolean[]{true, true, true, false, false});
        
        // create some chromosomes
        chr1 = new DiploidChromosome(hap1, hap2);
        chr2 = new DiploidChromosome(hap3, hap4);        
        
        // create some plants and descriptors (without any interest
        // in prob and/or LPA, so we put -1 for these values in each
        // descriptor)
        
        //  p1:
        // [0 0 0 1 0]
        // [1 1 1 0 1]
        chrs = new ArrayList<>();
        chrs.add(chr1);
        g1 = new Genotype(chrs);
        p1 = new Plant(g1);
        pd1 = new PlantDescriptor(p1, -1, -1, false);
        
        //  p2:
        // [0 0 0 1 1]
        // [1 1 1 0 0]
        chrs = new ArrayList<>();
        chrs.add(chr2);
        g2 = new Genotype(chrs);
        p2 = new Plant(g2);
        pd2 = new PlantDescriptor(p2, -1, -1, false);
        
        /*******************************/
        /* CHECK IMPROVEMENT HEURISTIC */
        /*******************************/
        
        ancestors = new HashSet<>();
        ancestors.add(pd1);
        assertFalse(heur.pruneGrowPlantFromAncestors(ancestors, pd2));
        
        ancestors = new HashSet<>();
        ancestors.add(pd2);
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd1));
        
        /***************/
        /* TEST CASE 5 */
        /***************/

        // create ideotype
        hap = new Haplotype(new boolean[]{true, true, true, true});
        chr = new DiploidChromosome(hap, hap);
        chrs = new ArrayList<>();
        chrs.add(chr);
        ideotype = new Genotype(chrs);
     
        // create genetic map
        map = GenotypeTest.genRandomGeneticMap(ideotype);
        
        // create heuristic
        heur = new ImprovementOverAncestorsHeuristic(new DefaultPlantImprovement(new StrongGenotypeImprovement(ideotype, map)));
        
        // create some haplotypes
        // [0 1 0 1]
        hap1 = new Haplotype(new boolean[]{false, true, false, true});
        // [1 0 1 0]
        hap2 = new Haplotype(new boolean[]{true, false, true, false});
        // [0 1 1 0]
        hap3 = new Haplotype(new boolean[]{false, true, true, false});
        // [1 0 0 1]
        hap4 = new Haplotype(new boolean[]{true, false, false, true});
        
        // create some chromosomes
        chr1 = new DiploidChromosome(hap1, hap2);
        chr2 = new DiploidChromosome(hap3, hap4);        
        
        // create some plants and descriptors (without any interest
        // in prob and/or LPA, so we put -1 for these values in each
        // descriptor)
        
        //  p1:
        // [0 1 0 1]
        // [1 0 1 0]
        chrs = new ArrayList<>();
        chrs.add(chr1);
        g1 = new Genotype(chrs);
        p1 = new Plant(g1);
        pd1 = new PlantDescriptor(p1, -1, -1, false);
        
        //  p2:
        // [0 1 1 0]
        // [1 0 0 1]
        chrs = new ArrayList<>();
        chrs.add(chr2);
        g2 = new Genotype(chrs);
        p2 = new Plant(g2);
        pd2 = new PlantDescriptor(p2, -1, -1, false);
        
        /*******************************/
        /* CHECK IMPROVEMENT HEURISTIC */
        /*******************************/
        
        ancestors = new HashSet<>();
        ancestors.add(pd1);
        assertFalse(heur.pruneGrowPlantFromAncestors(ancestors, pd2));
        
        ancestors = new HashSet<>();
        ancestors.add(pd2);
        assertTrue(heur.pruneGrowPlantFromAncestors(ancestors, pd1));
                
    }
    
    @Test
    public void testComputeLongestMatchingStretch() throws GenestackerException{
        
        System.out.println("\n### TEST COMPUTATION OF LONGEST MATCH AFTER <= 1 CROSSOVER ###\n");
     
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
            {10, 10, 10}  
        };
        GeneticMap map = new GeneticMap(d);
        
        // create heuristic
        StrongGenotypeImprovement impr = new StrongGenotypeImprovement(ideotype, map);
        
        // create some haplotypes
        Haplotype hap1 = new Haplotype(new boolean[]{false, false, false, false});
        Haplotype hap2 = new Haplotype(new boolean[]{false, false, true, false});
        Haplotype hap3 = new Haplotype(new boolean[]{false, true, false, false});
        Haplotype hap4 = new Haplotype(new boolean[]{false, true, true, false});
        Haplotype hap5 = new Haplotype(new boolean[]{true, true, true, true});
        
        // create some chromosomes
        
        // [0 0 0 0]
        // [0 0 0 0]
        DiploidChromosome chr1 = new DiploidChromosome(hap1, hap1);
        // [0 0 0 0]
        // [0 0 1 0]
        DiploidChromosome chr2 = new DiploidChromosome(hap1, hap2);
        // [0 0 1 0]
        // [0 0 1 0]
        DiploidChromosome chr3 = new DiploidChromosome(hap2, hap2);
        // [0 0 1 0]
        // [0 1 0 0]
        DiploidChromosome chr4 = new DiploidChromosome(hap2, hap3);
        // [0 1 1 0]
        // [0 0 0 0]
        DiploidChromosome chr5 = new DiploidChromosome(hap4, hap1);
        // [1 1 1 1]
        // [1 1 1 1]
        DiploidChromosome chr6 = new DiploidChromosome(hap5, hap5);
        
        // check computation of longest matching stretch and corresponding prob
        // (after at most one crossover)
        
        double[] stretch = impr.computeLongestMatchingStretch(0, chr1, hap);
        assertEquals(0, (int) stretch[0]);
        assertEquals(0.0, stretch[1]);
        
        stretch = impr.computeLongestMatchingStretch(0, chr2, hap);
        assertEquals(1, (int) stretch[0]);
        assertEquals(0.5, stretch[1]);
        
        stretch = impr.computeLongestMatchingStretch(0, chr3, hap);
        assertEquals(1, (int) stretch[0]);
        assertEquals(1.0, stretch[1]);
        
        stretch = impr.computeLongestMatchingStretch(0, chr4, hap);
        assertEquals(2, (int) stretch[0]);
        assertEquals(0.0453173, stretch[1], 0.0000001);
        
        stretch = impr.computeLongestMatchingStretch(0, chr5, hap);
        assertEquals(2, (int) stretch[0]);
        assertEquals(0.4546827, stretch[1], 0.0000001);
        
        stretch = impr.computeLongestMatchingStretch(0, chr6, hap);
        assertEquals(4, (int) stretch[0]);
        assertEquals(1.0, stretch[1], 0.0000001);
        
        /***************/
        /* TEST CASE 2 */
        /***************/

        // create ideotype
        hap = new Haplotype(new boolean[]{true, true, true, true, true, true, true, true, true, true, true, true});
        chr = new DiploidChromosome(hap, hap);
        chrs = new ArrayList<>();
        chrs.add(chr);
        ideotype = new Genotype(chrs);
     
        // create genetic map
        d = new double[][]{
            {10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10}  
        };
        map = new GeneticMap(d);
        
        // create heuristic
        impr = new StrongGenotypeImprovement(ideotype, map);
        
        // create some haplotypes
        hap1 = new Haplotype(new boolean[]{true, true, false, true, true, true, false, true, false, false, false, true});
        hap2 = new Haplotype(new boolean[]{false, false, false, true, false, true, true, false, true, true, true, false});
        hap3 = new Haplotype(new boolean[]{true, false, false, false, false, true, true, false, false, false, false, true});
        hap4 = new Haplotype(new boolean[]{false, true, true, false, false, false, false, true, true, true, true, true});
        hap5 = new Haplotype(new boolean[]{true, false, false, true, true, false, false, true, false, true, false, true});
        Haplotype hap6 = new Haplotype(new boolean[]{false, false, true, true, false, true, true, false, false, false, true, false});
        Haplotype hap7 = new Haplotype(new boolean[]{true, true, true, false, false, false, false, true, false, true, false, true});
        Haplotype hap8 = new Haplotype(new boolean[]{false, false, false, false, false, true, true, false, true, true, false, false});
        Haplotype hap9 = new Haplotype(new boolean[]{true, true, false, true, true, true, false, false, false, false, false, false});
        Haplotype hap10 = new Haplotype(new boolean[]{false, false, true, true, true, false, true, true, true, true, true, true});

        // create some chromosomes
        
        // [1 1 0 1 1 1 0 1 0 0 0 1]
        // [0 0 0 1 0 1 1 0 1 1 1 0]
        chr1 = new DiploidChromosome(hap1, hap2);
        // [1 0 0 0 0 1 1 0 0 0 0 1]
        // [0 1 1 0 0 0 0 1 1 1 1 1]
        chr2 = new DiploidChromosome(hap3, hap4);
        // [1 0 0 1 1 0 0 1 0 1 0 1]
        // [0 0 1 1 0 1 1 0 0 0 1 0]
        chr3 = new DiploidChromosome(hap5, hap6);
        // [1 1 1 0 0 0 0 1 0 1 0 1]
        // [0 0 0 0 0 1 1 0 1 1 0 0]
        chr4 = new DiploidChromosome(hap7, hap8);
        // [1 1 0 1 1 1 0 0 0 0 0 0]
        // [0 0 1 1 1 0 1 1 1 1 1 1]
        chr5 = new DiploidChromosome(hap9, hap10);
        
        // test computations
        
        stretch = impr.computeLongestMatchingStretch(0, chr1, hap);
        assertEquals(4, (int) stretch[0]);
        assertEquals(0.0824200, stretch[1], 0.0000001);
        
        stretch = impr.computeLongestMatchingStretch(0, chr2, hap);
        assertEquals(7, (int) stretch[0]);
        assertEquals(0.0309897, stretch[1], 0.0000001);
        
        stretch = impr.computeLongestMatchingStretch(0, chr3, hap);
        assertEquals(4, (int) stretch[0]);
        assertEquals(0.0412100, stretch[1], 0.0000001);
        
        stretch = impr.computeLongestMatchingStretch(0, chr4, hap);
        assertEquals(3, (int) stretch[0]);
        assertEquals(0.413473, stretch[1], 0.000001);
        
        stretch = impr.computeLongestMatchingStretch(0, chr5, hap);
        assertEquals(9, (int) stretch[0]);
        assertEquals(0.0281810, stretch[1], 0.000001);
        
    }

    @Test
    public void testCompareStrongAndWeakImprovement() throws GenestackerException{
        
        System.out.println("\n### COMPARE STRONG AND WEAK IMPROVEMENT ###\n");
        
        final int NUM_CHROMS = 2;
        final int MAX_LOCI_PER_CHROM = 6;
        
        // generate random genotype pairs
        int numWeakImpr = 0;
        int numStrongImpr = 0;
        int numPairs = 100;
        for(int n=0; n<numPairs; n++){
            // generate random genotype pair
            int[] numLociPerChrom = new int[NUM_CHROMS];
            for(int c=0; c<NUM_CHROMS; c++){
                numLociPerChrom[c] = rg.nextInt(MAX_LOCI_PER_CHROM)+1;
            }
            Genotype g1 = GenotypeTest.genRandomGenotype(NUM_CHROMS, numLociPerChrom);
            Genotype g2 = GenotypeTest.genRandomGenotype(NUM_CHROMS, numLociPerChrom);
            // set ideotype to all ones
            List<DiploidChromosome> ideotypeChroms = new ArrayList<>();
            for(int c=0; c<NUM_CHROMS; c++){
                boolean[] targets = new boolean[numLociPerChrom[c]];
                for(int l=0; l<numLociPerChrom[c]; l++){
                    targets[l] = true;
                }
                Haplotype hap = new Haplotype(targets);
                ideotypeChroms.add(new DiploidChromosome(hap, hap));
            }
            Genotype ideotype = new Genotype(ideotypeChroms);
            // generate random genetic map
            GeneticMap map = GenotypeTest.genRandomGeneticMap(g1);
            // create heuristics
            GenotypeImprovement weakImpr = new WeakGenotypeImprovement(ideotype);
            GenotypeImprovement strongImpr = new StrongGenotypeImprovement(ideotype, map);
            // assert: if strong improvement detected, then weak improvement should also be detected
            if(strongImpr.improvesOnOtherGenotype(g2, g1)){
                assertTrue(weakImpr.improvesOnOtherGenotype(g2, g1));
            }
            if(strongImpr.improvesOnOtherGenotype(g1, g2)){
                assertTrue(weakImpr.improvesOnOtherGenotype(g1, g2));
            }
            System.out.println("Pair " + (n+1) + ":");
            System.out.println("g1:");
            System.out.println(g1);
            System.out.println("g2:");
            System.out.println(g2);
            System.out.print("  g2 > g1 ? ");
            if(weakImpr.improvesOnOtherGenotype(g2, g1)){
                System.out.print("WEAK ");
                numWeakImpr++;
            }
            if(strongImpr.improvesOnOtherGenotype(g2, g1)){
                System.out.print("STRONG ");
                numStrongImpr++;
            }
            System.out.println("");
            System.out.print("  g1 > g2 ? ");
            if(weakImpr.improvesOnOtherGenotype(g1, g2)){
                System.out.print("WEAK ");
                numWeakImpr++;
            }
            if(strongImpr.improvesOnOtherGenotype(g1, g2)){
                System.out.print("STRONG ");
                numStrongImpr++;
            }
            System.out.println("");
        }
        
        System.out.println("---");
        System.out.println("% weak impr: " + (int) ((double) numWeakImpr / (2*numPairs) * 100));
        System.out.println("% strong impr: " + (int) ((double) numStrongImpr / (2*numPairs) * 100));
        System.out.println("---");
        
    }
}
