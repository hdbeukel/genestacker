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

package org.ugent.caagt.genestacker.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import org.ugent.caagt.genestacker.HaldaneMapFunction;
import org.ugent.caagt.genestacker.Haplotype;
import org.ugent.caagt.genestacker.GenotypeGroupWithSameAllelicFrequencies;
import org.ugent.caagt.genestacker.GenotypeAllelicFrequencies;
import org.ugent.caagt.genestacker.Plant;
import org.ugent.caagt.genestacker.SeedLot;
import org.ugent.caagt.genestacker.exceptions.GenestackerException;
import org.ugent.caagt.genestacker.io.GenestackerInput;
import org.ugent.caagt.genestacker.search.bb.BranchAndBound;
import org.ugent.caagt.genestacker.search.bb.BranchAndBoundSolutionManager;
import org.ugent.caagt.genestacker.search.bb.DefaultSeedLotConstructor;
import org.ugent.caagt.genestacker.search.bb.MergeFirstSchemeMerger;
import org.ugent.caagt.genestacker.search.bb.PlantDescriptor;
import org.ugent.caagt.genestacker.search.bb.SchemeMerger;
import org.ugent.caagt.genestacker.search.bb.SeedLotConstructor;

/**
 *
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class CrossingSchemeTest extends TestCase {
    
    public CrossingSchemeTest() {
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
     * Test of print method, of class CrossingScheme.
     */
    @Test
    public void testPrintAndCopy() throws GenestackerException{
        
        System.out.println("\n### TEST PRINT ###\n");
        
        final double SUCCESS_PROB = 0.9;
        
        PopulationSizeTools popSizeTools = new DefaultPopulationSizeTools(SUCCESS_PROB);
        
        double[][] distances = new double[][]{new double[]{10000, 10000, 1}};
        GeneticMap map = new GeneticMap(distances, new HaldaneMapFunction());
        
        SeedLotConstructor seedLotConstructor = new DefaultSeedLotConstructor(map);
        
        // Create haplotypes for initial seedlots
        
        Haplotype hom1 = new Haplotype(new boolean[]{true, false, false, true});
        Haplotype hom2 = new Haplotype(new boolean[]{false, true, true, false});
        Haplotype hom3 = new Haplotype(new boolean[]{false, false, false, true});
        
        // Create chromosomes
        
        DiploidChromosome chr1 = new DiploidChromosome(hom1, hom1);
        DiploidChromosome chr2 = new DiploidChromosome(hom2, hom2);
        DiploidChromosome chr3 = new DiploidChromosome(hom3, hom3);
        
        // Create genotypes
        
        List<DiploidChromosome> g1chroms = new ArrayList<>();
        g1chroms.add(chr1);
        Genotype g1 = new Genotype(g1chroms);
        
        List<DiploidChromosome> g2chroms = new ArrayList<>();
        g2chroms.add(chr2);
        Genotype g2 = new Genotype(g2chroms);
        
        List<DiploidChromosome> g3chroms = new ArrayList<>();
        g3chroms.add(chr3);
        Genotype g3 = new Genotype(g3chroms);
        
        // Create initial seedlots
        
        SeedLot sf0 = new SeedLot(g1);
        
        SeedLot sf1 = new SeedLot(g2);
        
        SeedLot sf2 = new SeedLot(g3);
        
        // Create initial seedlot nodes
        
        SeedLotNode a = new SeedLotNode(sf0, 0);
        SeedLotNode b = new SeedLotNode(sf1, 0);
        SeedLotNode c = new SeedLotNode(sf2, 0);
        
        // Generation 0: grow initial plants
        
        PlantNode A0 = new PlantNode(new Plant(g1), 0, a);
        PlantNode B0 = new PlantNode(new Plant(g2), 0, b);
        PlantNode C0 = new PlantNode(new Plant(g3), 0, c);
        
        // Generation 1:
        
        // partialCross A0 x B0 => c0 => s3
        // partialCross B0 x C0 => c1 => s4
        
        CrossingNode c0 = new CrossingNode(A0, B0);
        
        SeedLot sf3 = seedLotConstructor.cross(c0.getParent1().getPlant().getGenotype(), c0.getParent2().getPlant().getGenotype());
        SeedLotNode s3 = new SeedLotNode(sf3, 1, c0);
        
        CrossingNode c1 = new CrossingNode(C0, B0);

        SeedLot sf4 = seedLotConstructor.cross(c1.getParent1().getPlant().getGenotype(), c1.getParent2().getPlant().getGenotype());
        SeedLotNode s4 = new SeedLotNode(sf4, 1, c1);
        
        // grow D0 from s3
        
        DiploidChromosome chr4 = new DiploidChromosome(hom1, hom2);
        List<DiploidChromosome> g4chroms = new ArrayList<>();
        g4chroms.add(chr4);
        Genotype g4 = new Genotype(g4chroms);
        
        PlantNode D0 = new PlantNode(new Plant(g4), 1, s3);
        
        // grow E0 from s4
        
        DiploidChromosome chr5 = new DiploidChromosome(hom2, hom3);
        List<DiploidChromosome> g5chroms = new ArrayList<>();
        g5chroms.add(chr5);
        Genotype g5 = new Genotype(g5chroms);
        
        PlantNode E0 = new PlantNode(new Plant(g5), 1, s4);
        
        // Generation 2:
        
        // partialCross E0 x D0 => c2 => s5 twice (many seeds required)
        
        CrossingNode c2 = new CrossingNode(E0, D0);
        c2.incNumDuplicates();

        SeedLot sf5 = seedLotConstructor.cross(c2.getParent1().getPlant().getGenotype(), c2.getParent2().getPlant().getGenotype());
        SeedLotNode s5 = new SeedLotNode(sf5, 2, c2);
        
        // grow F0 from s5

        Haplotype hom4 = new Haplotype(new boolean[]{true, true, true, false});
        Haplotype hom5 = new Haplotype(new boolean[]{false, true, true, true});
        DiploidChromosome chr6 = new DiploidChromosome(hom4, hom5);
        List<DiploidChromosome> g6chroms = new ArrayList<>();
        g6chroms.add(chr6);
        Genotype g6 = new Genotype(g6chroms);
        
        PlantNode F0 = new PlantNode(new Plant(g6), 2, s5);
        
        // Generation 3:
        
        // self F0 => c3 => s6
        
        SelfingNode c3 = new SelfingNode(F0);

        SeedLot sf6 = seedLotConstructor.self(c3.getParent1().getPlant().getGenotype());
        SeedLotNode s6 = new SeedLotNode(sf6, 3, c3);
        
        // grow G0 from s6
        
        Haplotype hom6 = new Haplotype(new boolean[]{true, true, true, true});
        DiploidChromosome chr7 = new DiploidChromosome(hom6, hom6);
        List<DiploidChromosome> g7chroms = new ArrayList<>();
        g7chroms.add(chr7);
        Genotype g7 = new Genotype(g7chroms);
        
        PlantNode G0 = new PlantNode(new Plant(g7), 3, s6);
        
        // create and print crossing scheme with ideotype G0
        
        CrossingScheme scheme = new CrossingScheme(popSizeTools, G0);
        scheme.print();
        
        // check nr of nodes
        assertEquals(18, scheme.getNumNodes());
        
        System.out.println("\n!Copied scheme:");
        
        // create copy of scheme s
        CrossingScheme schemeCopy = new CrossingScheme(scheme.getPopulationSizeTools(), G0.deepUpwardsCopy());
        schemeCopy.print();
    }
    
    @Test
    public void testCrossWith() throws GenestackerException{
        
        PlantNode.resetIDs();
        SeedLotNode.resetIDs();
        CrossingNode.resetIDs();
        CrossingSchemeAlternatives.resetIDs();
        
        final double SUCCES_PROB = 0.99;
        PopulationSizeTools popSizeTools = new DefaultPopulationSizeTools(SUCCES_PROB);
        
        System.out.println("\n### TEST CROSSING OF IDEOTYPES ###");
        
        /****************************/
        /* CREATE INITIAL SEED LOTS */
        /****************************/
        
        // create some haplotypes
        Haplotype hom0 = new Haplotype(new boolean[]{true, false});
        Haplotype hom1 = new Haplotype(new boolean[]{false, true});
        Haplotype hom2 = new Haplotype(new boolean[]{true, true});
        
        // create some diploid chromosomes
        
        // chr1:
        // [1 0]
        // [1 0]
        DiploidChromosome chr1 = new DiploidChromosome(hom0, hom0);
        // chr2:
        // [0 1]
        // [0 1]
        DiploidChromosome chr2 = new DiploidChromosome(hom1, hom1);
        // chr3:
        // [1 1]
        // [1 1]
        DiploidChromosome chr3 = new DiploidChromosome(hom2, hom2);
        
        // create genotypes
        
        List<DiploidChromosome> g1chroms = new ArrayList<>();
        g1chroms.add(chr1);
        Genotype g1 = new Genotype(g1chroms);
        List<DiploidChromosome> g2chroms = new ArrayList<>();
        g2chroms.add(chr2);
        Genotype g2 = new Genotype(g2chroms);
        List<DiploidChromosome> ideotypeChroms = new ArrayList<>();
        ideotypeChroms.add(chr3);
        Genotype ideotype = new Genotype(ideotypeChroms);
        
        // create seedlots
        
        SeedLot sf1 = new SeedLot(g1);
        SeedLot sf2 = new SeedLot(g2);
        
        // create initial partial crossing schemes
        
        int numScheme=0;
        
        SeedLotNode sfn1 = new SeedLotNode(sf1, 0);
        PlantNode pn1 = new PlantNode(new Plant(g1), 0, sfn1);
        CrossingScheme s1 = new CrossingScheme(popSizeTools, pn1);
        numScheme++;
        System.out.println("\n===\nScheme " + numScheme + ":\n===");
        s1.print();
        
        SeedLotNode sfn2 = new SeedLotNode(sf2, 0);
        PlantNode pn2 = new PlantNode(new Plant(g2), 0, sfn2);
        CrossingScheme s2 = new CrossingScheme(popSizeTools, pn2);
        numScheme++;
        System.out.println("\n===\nScheme " + numScheme + ":\n===");
        s2.print();
        
        /**************************/
        /* MERGE CROSSING SCHEMES */
        /**************************/
        
        double[][] distances = new double[][]{new double[]{20}};
        GeneticMap map = new GeneticMap(distances, new HaldaneMapFunction());
        
        SeedLotConstructor seedLotConstructor = new DefaultSeedLotConstructor(map);            
        
        BranchAndBoundSolutionManager solManager = new BranchAndBoundSolutionManager(new DefaultDominatesRelation(),
                                                        ideotype, popSizeTools, null, null, null, null, false);
        BranchAndBound bb = new BranchAndBound(new GenestackerInput(null, ideotype, map), popSizeTools, null, null,
                                                                         null, null, null, seedLotConstructor);

        List<CrossingSchemeAlternatives> schemes = new ArrayList<>();
        schemes.add(new CrossingSchemeAlternatives(s1));
        schemes.add(new CrossingSchemeAlternatives(s2));
        
        // partialCross s1 with s2 --> gives s3
        
        Set<PlantDescriptor> ancestors = new HashSet<>();
        ancestors.addAll(schemes.get(1).getAncestorDescriptors());
        ancestors.addAll(schemes.get(0).getAncestorDescriptors());
        SeedLot sl = seedLotConstructor.cross(schemes.get(1).getFinalPlant().getGenotype(), schemes.get(0).getFinalPlant().getGenotype());
        List<CrossingSchemeAlternatives> merged = new MergeFirstSchemeMerger(schemes.get(1), schemes.get(0), map, solManager, sl).combineSchemes();
        System.out.println("\n--!! Crossed ideotype of scheme 2 with 1 !!--");
        for(int i=0; i<merged.size(); i++){
            for(CrossingScheme s : merged.get(i).getAlternatives()){
                numScheme++;
                System.out.println("\n===\nScheme " + numScheme + ":\n===");
                s.print();
    //            File img = merged.get(i).createImage();
    //            System.out.println("\nImage:");
    //            System.out.println(img.getAbsoluteFile());
            }
            schemes.add(merged.get(i));
        }
        
        // partialCross s3 with s1 --> gives s4 up to s7
        
        ancestors = new HashSet<>();
        ancestors.addAll(schemes.get(2).getAncestorDescriptors());
        ancestors.addAll(schemes.get(0).getAncestorDescriptors());
        sl = seedLotConstructor.cross(schemes.get(2).getFinalPlant().getGenotype(), schemes.get(0).getFinalPlant().getGenotype());
        merged = new MergeFirstSchemeMerger(schemes.get(2), schemes.get(0), map, solManager, sl).combineSchemes();
        System.out.println("\n--!! Crossed ideotype of scheme 3 with 1 !!--");
        for(int i=0; i<merged.size(); i++){
            for(CrossingScheme s : merged.get(i).getAlternatives()){
                numScheme++;
                System.out.println("\n===\nScheme " + numScheme + ":\n===");
                s.print();
    //            File img = merged.get(i).createImage();
    //            System.out.println("\nImage:");
    //            System.out.println(img.getAbsoluteFile());
            }
            schemes.add(merged.get(i));
        }
        
        // partialCross s4 with s3
        
        ancestors = new HashSet<>();
        ancestors.addAll(schemes.get(3).getAncestorDescriptors());
        ancestors.addAll(schemes.get(2).getAncestorDescriptors());
        sl = seedLotConstructor.cross(schemes.get(3).getFinalPlant().getGenotype(), schemes.get(2).getFinalPlant().getGenotype());
        merged = new MergeFirstSchemeMerger(schemes.get(3), schemes.get(2), map, solManager, sl).combineSchemes();
        System.out.println("\n--!! Crossed ideotype of scheme 4 with 3 !!--");
        for(int i=0; i<merged.size(); i++){
            for(CrossingScheme s : merged.get(i).getAlternatives()){
                numScheme++;
                System.out.println("\n===\nScheme " + numScheme + ":\n===");
                s.print();
    //            File img = merged.get(i).createImage();
    //            System.out.println("\nImage:");
    //            System.out.println(img.getAbsoluteFile());
            }
            schemes.add(merged.get(i));
        }
        
        // partialCross s11 with s9
        
        ancestors = new HashSet<>();
        ancestors.addAll(schemes.get(10).getAncestorDescriptors());
        ancestors.addAll(schemes.get(8).getAncestorDescriptors());
        sl = seedLotConstructor.cross(schemes.get(10).getFinalPlant().getGenotype(), schemes.get(8).getFinalPlant().getGenotype());
        merged = new MergeFirstSchemeMerger(schemes.get(10), schemes.get(8), map, solManager, sl).combineSchemes();
        System.out.println("\n--!! Crossed ideotype of scheme 11 with 9 !!--");
        for(int i=0; i<merged.size(); i++){
            for(CrossingScheme s : merged.get(i).getAlternatives()){
                numScheme++;
                System.out.println("\n===\nScheme " + numScheme + ":\n===");
                s.print();
    //            File img = merged.get(i).createImage();
    //            System.out.println("\nImage:");
    //            System.out.println(img.getAbsoluteFile());
            }
            schemes.add(merged.get(i));
        }
        
        /***************************/
        /* EXPERIMENT WITH SELFING */
        /***************************/
        
        System.out.println("\n\n### SELFING OF SCHEMES ###\n");
        
        // self s3
        
        List<CrossingSchemeAlternatives> selfed = bb.selfScheme(schemes.get(2), map, solManager);
        System.out.println("\n--!! Selfed ideotype of scheme 3 !!--");
        int numSchemeS = 0;
        for(int i=0; i<selfed.size(); i++){
            for(CrossingScheme s : selfed.get(i).getAlternatives()){
                numSchemeS++;
                System.out.println("\n===\nScheme S" + numSchemeS + ":\n===");
                s.print();
    //            File img = merged.get(i).createImage();
    //            System.out.println("\nImage:");
    //            System.out.println(img.getAbsoluteFile());
            }
        }
        
        // partialCross scheme 3 with scheme S1
        
        ancestors = new HashSet<>();
        ancestors.addAll(schemes.get(2).getAncestorDescriptors());
        ancestors.addAll(selfed.get(0).getAncestorDescriptors());
        sl = seedLotConstructor.cross(schemes.get(2).getFinalPlant().getGenotype(), selfed.get(0).getFinalPlant().getGenotype());
        merged = new MergeFirstSchemeMerger(schemes.get(2), selfed.get(0), map, solManager, sl).combineSchemes();
        System.out.println("\n--!! Crossed ideotype of scheme 3 with S1 !!--");
        for(int i=0; i<merged.size(); i++){
            for(CrossingScheme s : merged.get(i).getAlternatives()){
                numScheme++;
                System.out.println("\n===\nScheme " + numScheme + ":\n===");
                s.print();
    //            File img = merged.get(i).createImage();
    //            System.out.println("\nImage:");
    //            System.out.println(img.getAbsoluteFile());
            }
        }
                
    }
    
    /**
     * Test schemes with multiple plants from same seed lot in same generation,
     * where max of num seeds does not suffice.
     */
    @Test
    public void testNumSeeds() throws GenestackerException{
        
        PlantNode.resetIDs();
        SeedLotNode.resetIDs();
        CrossingNode.resetIDs();
        CrossingSchemeAlternatives.resetIDs();
        
        System.out.println("");
        System.out.println("###");
        System.out.println("TEST NUM SEEDS > MAX");
        System.out.println("###");
        System.out.println("");
        
        PopulationSizeTools popSizeTools = new DefaultPopulationSizeTools(0.9);
        
        // Grow same plant twice from uniform lot
                
        // create genotype
        // Genotype:
        // [1][0]
        // [1][0]
        Haplotype hom1 = new Haplotype(new boolean[]{true});
        Haplotype hom2 = new Haplotype(new boolean[]{true});
        DiploidChromosome chrom1 = new DiploidChromosome(hom1, hom2);
        Haplotype hom3 = new Haplotype(new boolean[]{false});
        Haplotype hom4 = new Haplotype(new boolean[]{false});
        DiploidChromosome chrom2 = new DiploidChromosome(hom3, hom4);
        List<DiploidChromosome> chromosomes = new ArrayList<>();
        chromosomes.add(chrom1);
        chromosomes.add(chrom2);
        Genotype genotype = new Genotype(chromosomes);
    
        GeneticMap map = GenotypeTest.genRandomGeneticMap(genotype);
        SeedLotConstructor seedLotConstructor = new DefaultSeedLotConstructor(map);

        // create uniform seed lot
        SeedLot seedlot = new SeedLot(genotype);
        
        // create seed lot node
        SeedLotNode sln = new SeedLotNode(seedlot, 0);
        
        // add plant node
        Plant p = new Plant(genotype);
        PlantNode pn = new PlantNode(p, 0, sln);
        // grow twice
        pn.incNumDuplicates();
        
        // self plant node
        CrossingNode cr = new SelfingNode(pn);
        
        // create new seeds from crossing
        SeedLotNode newSln = new SeedLotNode(seedLotConstructor.cross(p.getGenotype(), p.getGenotype()), 1, cr, 1, 0);
        
        // grow ideotype from new seed lot
        PlantNode ideotype = new PlantNode(p, 1, newSln);
        
        // create crossing scheme
        CrossingScheme s = new CrossingScheme(popSizeTools, ideotype);
        s.print();
        
        // check num seeds
        assertEquals(2, sln.getSeedsTakenFromSeedLot());
        assertEquals(1, newSln.getSeedsTakenFromSeedLot());
        
        /******************************/
        /* TEST "WORST CASE" SCENARIO */
        /******************************/
        
        // create genotypes
        
        // g1:
        // [1 0]
        // [1 0]
        hom1 = new Haplotype(new boolean[]{true, false});
        hom2 = new Haplotype(new boolean[]{true, false});
        chrom1 = new DiploidChromosome(hom1, hom2);
        chromosomes = new ArrayList<>();
        chromosomes.add(chrom1);
        Genotype g1 = new Genotype(chromosomes);
        // g2:
        // [0 0]
        // [1 1]
        hom1 = new Haplotype(new boolean[]{false, false});
        hom2 = new Haplotype(new boolean[]{true, true});
        chrom1 = new DiploidChromosome(hom1, hom2);
        chromosomes = new ArrayList<>();
        chromosomes.add(chrom1);
        Genotype g2 = new Genotype(chromosomes);
        
        map = GenotypeTest.genRandomGeneticMap(g2);
        seedLotConstructor = new DefaultSeedLotConstructor(map);
        
        // create seed lot with both genotypes with equal prob of 0.5
        Map<Genotype, Double> g1map = new HashMap<>();
        g1map.put(g1, 0.5);
        Map<Genotype, Double> g2map = new HashMap<>();
        g2map.put(g2, 0.5);
        Map<GenotypeAllelicFrequencies, GenotypeGroupWithSameAllelicFrequencies> states = new HashMap<>();
        states.put(g1.getAllelicFrequencies(), new GenotypeGroupWithSameAllelicFrequencies(0.5, g1.getAllelicFrequencies(), g1map));
        states.put(g2.getAllelicFrequencies(), new GenotypeGroupWithSameAllelicFrequencies(0.5, g2.getAllelicFrequencies(), g2map));
        seedlot = new SeedLot(false, states);
        
        // create seed lot node
        sln = new SeedLotNode(seedlot, 0);
        
        // grow both plants
        Plant p1 = new Plant(g1);
        Plant p2 = new Plant(g2);
        pn = new PlantNode(p1, 0, sln);
        PlantNode pn2 = new PlantNode(p2, 0, sln);
        
        // partialCross plants
        cr = new CrossingNode(pn, pn2);
        
        // create new seeds from crossing
        SeedLot newSl = seedLotConstructor.cross(p1.getGenotype(), p2.getGenotype());
        newSln = new SeedLotNode(newSl, 1, cr, 1, 0);
        
        // grow ideotype from new sln:
        // [1 0]
        // [1 1]
        hom1 = new Haplotype(new boolean[]{true, false});
        hom2 = new Haplotype(new boolean[]{true, true});
        chrom1 = new DiploidChromosome(hom1, hom2);
        chromosomes = new ArrayList<>();
        chromosomes.add(chrom1);
        Genotype i = new Genotype(chromosomes);
        
        ideotype = new PlantNode(new Plant(i), 1, newSln);
        
        // create crossing scheme
        popSizeTools = new DefaultPopulationSizeTools(0.421875);
        s = new CrossingScheme(popSizeTools, ideotype);
        s.print();
        
        // check num seeds required for both plants individually (in generation 0)
        assertEquals(2, popSizeTools.computeRequiredSeedsForTargetPlant(pn));
        assertEquals(2, popSizeTools.computeRequiredSeedsForTargetPlant(pn2));
        // check num seeds in 0th generation (not 2 but 3 !!)
        assertEquals(3, sln.getSeedsTakenFromSeedLot());
    }
    
    @Test
    public void testAlignments() throws GenestackerException, IOException{
        
        // create some haplotypes
        Haplotype h1_0 = new Haplotype(new boolean[]{false});
        Haplotype h1_1 = new Haplotype(new boolean[]{true});
        Haplotype h2_000 = new Haplotype(new boolean[]{false, false, false});
        Haplotype h2_001 = new Haplotype(new boolean[]{false, false, true});
        Haplotype h2_010 = new Haplotype(new boolean[]{false, true, false});
        Haplotype h2_011 = new Haplotype(new boolean[]{false, true, true});
        Haplotype h2_100 = new Haplotype(new boolean[]{true, false, false});
        Haplotype h2_101 = new Haplotype(new boolean[]{true, false, true});
        Haplotype h2_110 = new Haplotype(new boolean[]{true, true, false});
        Haplotype h2_111 = new Haplotype(new boolean[]{true, true, true});
        
        // create some diploid chromosomes

        DiploidChromosome chr1_0_0 = new DiploidChromosome(h1_0, h1_0);
        DiploidChromosome chr1_0_1 = new DiploidChromosome(h1_0, h1_1);
        DiploidChromosome chr1_1_1 = new DiploidChromosome(h1_1, h1_1);
        DiploidChromosome chr2_000_001 = new DiploidChromosome(h2_000, h2_001);
        DiploidChromosome chr2_010_101 = new DiploidChromosome(h2_010, h2_101);
        DiploidChromosome chr2_010_110 = new DiploidChromosome(h2_010, h2_110);
        DiploidChromosome chr2_001_001 = new DiploidChromosome(h2_001, h2_001);
        DiploidChromosome chr2_110_110 = new DiploidChromosome(h2_110, h2_110);
        DiploidChromosome chr2_001_110 = new DiploidChromosome(h2_001, h2_110);
        
        // create parental genotypes
        Genotype g1 = new Genotype(Arrays.asList(chr1_0_1, chr2_000_001));
        Genotype g2 = new Genotype(Arrays.asList(chr1_0_0, chr2_010_101));
        
        // create parental genotype seed lots
        SeedLot sl1 = new SeedLot(g1);
        SeedLot sl2 = new SeedLot(g2);
        
        // create minimal schemes growing parental genotypes
        
        PopulationSizeTools popSizeTools = new DefaultPopulationSizeTools(0.95);
        GeneticMap map = GenotypeTest.genRandomGeneticMap(g1);
        SeedLotConstructor seedLotConstructor = new DefaultSeedLotConstructor(map);
        
        SeedLotNode sln1 = new SeedLotNode(sl1, 0);
        PlantNode pn1 = new PlantNode(new Plant(g1), 0, sln1);
        // shifted version (1 empty generation)
        SeedLotNode sln1_shifted = new SeedLotNode(sl1, 0);
        PlantNode pn1_shifted = new PlantNode(new Plant(g1), 1, sln1_shifted); // shifted
        
        SeedLotNode sln2 = new SeedLotNode(sl2, 0);
        PlantNode pn2 = new PlantNode(new Plant(g2), 0, sln2);
        
        // perform two consecutive selfings with plant from node pn2
        
        CrossingNode c2 = new SelfingNode(pn2);
        SeedLot c2sl = seedLotConstructor.self(pn2.getPlant().getGenotype());
        SeedLotNode c2sln = new SeedLotNode(c2sl, 1, c2);
        Genotype c2target = new Genotype(Arrays.asList(chr1_0_0, chr2_010_110));
        PlantNode c2targetpn = new PlantNode(new Plant(c2target), 1, c2sln);
                
        CrossingNode c3 = new SelfingNode(c2targetpn);
        SeedLot c3sl = seedLotConstructor.self(c2targetpn.getPlant().getGenotype());
        SeedLotNode c3sln = new SeedLotNode(c3sl, 2, c3);
        Genotype c3target = new Genotype(Arrays.asList(chr1_0_0, chr2_110_110));
        PlantNode c3targetpn = new PlantNode(new Plant(c3target), 2, c3sln);
        CrossingScheme s2_2g = new CrossingScheme(popSizeTools, c3targetpn);
        
        // extend scheme s1 through one selfing; three different versions:
        //  version 1: extension of non-shifted s1, in which a generation shift is now introduced
        //  version 2: extension of shifted s1, no more shifts are introduced here
        //  version 3: extension of non-shifted s1 with no shifts introduced here
        
        // extend non-shifted version of s1 (shift now)
        CrossingNode c1_shift_last = new SelfingNode(pn1);
        SeedLot c1sl_shift_last = seedLotConstructor.self(pn1.getPlant().getGenotype());
        SeedLotNode c1sln_shift_last = new SeedLotNode(c1sl_shift_last, 1, c1_shift_last);
        Genotype c1target_shift_last = new Genotype(Arrays.asList(chr1_1_1, chr2_001_001));
        PlantNode c1targetpn_shift_last = new PlantNode(new Plant(c1target_shift_last), 2, c1sln_shift_last); // shift now
        
        // extend shifted version of s1 (do not shift now)
        CrossingNode c1_shift_first = new SelfingNode(pn1_shifted); // already shifted before
        SeedLot c1sl_shift_first = seedLotConstructor.self(pn1_shifted.getPlant().getGenotype());
        SeedLotNode c1sln_shift_first = new SeedLotNode(c1sl_shift_first, 2, c1_shift_first); 
        Genotype c1target_shift_first = new Genotype(Arrays.asList(chr1_1_1, chr2_001_001));
        PlantNode c1targetpn_shift_first = new PlantNode(new Plant(c1target_shift_first), 2, c1sln_shift_first); // no more shift here
        
        // extend non-shifted version of s1, where no shift is introduced now neither
        CrossingNode c1_no_shift = new SelfingNode(pn1.deepUpwardsCopy());
        SeedLot c1sl_no_shift = seedLotConstructor.self(pn1.getPlant().getGenotype());
        SeedLotNode c1sln_no_shift = new SeedLotNode(c1sl_no_shift, 1, c1_no_shift); 
        Genotype c1target_no_shift = new Genotype(Arrays.asList(chr1_1_1, chr2_001_001));
        PlantNode c1targetpn_no_shift = new PlantNode(new Plant(c1target_no_shift), 1, c1sln_no_shift); // no shifts
        CrossingScheme s1_1g_no_shift = new CrossingScheme(popSizeTools, c1targetpn_no_shift);
        
        // combine both schemes through a crossing (align with one of both shifted version of the smallest scheme)
        
        // alignment 1 (shifted last)
        CrossingNode c4_1 = new CrossingNode(c1targetpn_shift_last, c3targetpn);
        SeedLot c4sl_1 = seedLotConstructor.cross(c1targetpn_shift_last.getPlant().getGenotype(), c3targetpn.getPlant().getGenotype());
        SeedLotNode c4sln_1 = new SeedLotNode(c4sl_1, 3, c4_1);
        Genotype ideotype_1 = new Genotype(Arrays.asList(chr1_0_1, chr2_001_110));
        PlantNode ideotypepn_1 = new PlantNode(new Plant(ideotype_1), 3, c4sln_1);
        CrossingScheme sfinal_1 = new CrossingScheme(popSizeTools, ideotypepn_1);
        
        // alignment 2 (shifted first)
        CrossingNode c4_2 = new CrossingNode(c1targetpn_shift_first, c3targetpn.deepUpwardsCopy());
        SeedLot c4sl_2 = seedLotConstructor.cross(c1targetpn_shift_first.getPlant().getGenotype(), c3targetpn.getPlant().getGenotype());
        SeedLotNode c4sln_2 = new SeedLotNode(c4sl_2, 3, c4_2);
        Genotype ideotype_2 = new Genotype(Arrays.asList(chr1_0_1, chr2_001_110));
        PlantNode ideotypepn_2 = new PlantNode(new Plant(ideotype_2), 3, c4sln_2);
        CrossingScheme sfinal_2 = new CrossingScheme(popSizeTools, ideotypepn_2);  
        
        // now compute all (non discarded) alignments, both should be contained in the resulting collection
        
        BranchAndBoundSolutionManager solManager = new BranchAndBoundSolutionManager(new DefaultDominatesRelation(),
                                                                                        ideotype_2, popSizeTools, null,
                                                                                        null, null, null, false);
        SchemeMerger merger = new MergeFirstSchemeMerger(new CrossingSchemeAlternatives(s1_1g_no_shift), new CrossingSchemeAlternatives(s2_2g),
                                                                                        map, solManager, c4sl_2);
        List<CrossingSchemeAlternatives> merged = merger.combineSchemes();
        List<CrossingScheme> mergedIndividual = new ArrayList<>();
        for(CrossingSchemeAlternatives ca : merged){
            mergedIndividual.addAll(ca.getAlternatives());
        }
                
        // verify: the two manually constructed alignments should be the only ones which are retained
        assertEquals(2, mergedIndividual.size());
        assertTrue(mergedIndividual.contains(sfinal_1));
        assertTrue(mergedIndividual.contains(sfinal_2));
        
    }
    
    @Test
    public void testAlignments2() throws GenestackerException, IOException{
        
        // create some haplotypes
        Haplotype h1_0 = new Haplotype(new boolean[]{false});
        Haplotype h1_1 = new Haplotype(new boolean[]{true});
        Haplotype h2_000 = new Haplotype(new boolean[]{false, false, false});
        Haplotype h2_001 = new Haplotype(new boolean[]{false, false, true});
        Haplotype h2_010 = new Haplotype(new boolean[]{false, true, false});
        Haplotype h2_011 = new Haplotype(new boolean[]{false, true, true});
        Haplotype h2_100 = new Haplotype(new boolean[]{true, false, false});
        Haplotype h2_101 = new Haplotype(new boolean[]{true, false, true});
        Haplotype h2_110 = new Haplotype(new boolean[]{true, true, false});
        Haplotype h2_111 = new Haplotype(new boolean[]{true, true, true});
        
        // create some diploid chromosomes

        DiploidChromosome chr1_0_0 = new DiploidChromosome(h1_0, h1_0);
        DiploidChromosome chr1_0_1 = new DiploidChromosome(h1_0, h1_1);
        DiploidChromosome chr1_1_1 = new DiploidChromosome(h1_1, h1_1);
        DiploidChromosome chr2_000_001 = new DiploidChromosome(h2_000, h2_001);
        DiploidChromosome chr2_010_101 = new DiploidChromosome(h2_010, h2_101);
        DiploidChromosome chr2_010_110 = new DiploidChromosome(h2_010, h2_110);
        DiploidChromosome chr2_001_001 = new DiploidChromosome(h2_001, h2_001);
        DiploidChromosome chr2_110_110 = new DiploidChromosome(h2_110, h2_110);
        DiploidChromosome chr2_001_110 = new DiploidChromosome(h2_001, h2_110);
        
        // create parental genotypes
        Genotype g1 = new Genotype(Arrays.asList(chr1_0_1, chr2_000_001));
        Genotype g2 = new Genotype(Arrays.asList(chr1_0_0, chr2_010_101));
        
        // create parental genotype seed lots
        SeedLot sl1 = new SeedLot(g1);
        SeedLot sl2 = new SeedLot(g2);
        
        // create minimal schemes growing parental genotypes
        
        PopulationSizeTools popSizeTools = new DefaultPopulationSizeTools(0.95);
        GeneticMap map = GenotypeTest.genRandomGeneticMap(g1);
        SeedLotConstructor seedLotConstructor = new DefaultSeedLotConstructor(map);
        
        SeedLotNode sln1 = new SeedLotNode(sl1, 0);
        PlantNode pn1 = new PlantNode(new Plant(g1), 0, sln1);
        
        SeedLotNode sln2 = new SeedLotNode(sl2, 0);
        PlantNode pn2 = new PlantNode(new Plant(g2), 0, sln2);
        
        // perform 2 consecutive selfings with plant from node pn2 (2 generations)
        
        CrossingNode c2 = new SelfingNode(pn2);
        SeedLot c2sl = seedLotConstructor.self(pn2.getPlant().getGenotype());
        SeedLotNode c2sln = new SeedLotNode(c2sl, 1, c2);
        Genotype c2target = new Genotype(Arrays.asList(chr1_0_0, chr2_010_110));
        PlantNode c2targetpn = new PlantNode(new Plant(c2target), 1, c2sln);
                
        CrossingNode c3 = new SelfingNode(c2targetpn);
        SeedLot c3sl = seedLotConstructor.self(c2targetpn.getPlant().getGenotype());
        SeedLotNode c3sln = new SeedLotNode(c3sl, 2, c3);
        Genotype c3target = new Genotype(Arrays.asList(chr1_0_0, chr2_110_110));
        PlantNode c3targetpn = new PlantNode(new Plant(c3target), 2, c3sln);
        CrossingScheme s2_2g = new CrossingScheme(popSizeTools, c3targetpn);
        
        // create second, smaller (1 generation) partial scheme by crossing the two parental genotypes

        CrossingNode c1 = new CrossingNode(pn1, pn2.deepUpwardsCopy()); // copy pn2 because already used in other branch
        SeedLot c1sl = seedLotConstructor.cross(pn1.getPlant().getGenotype(), pn2.getPlant().getGenotype());
        SeedLotNode c1sln = new SeedLotNode(c1sl, 1, c1); 
        Genotype c1target = new Genotype(Arrays.asList(chr1_0_1, chr2_001_001));
        PlantNode c1targetpn = new PlantNode(new Plant(c1target), 1, c1sln);
        CrossingScheme s1_1g = new CrossingScheme(popSizeTools, c1targetpn);
        
        // combine both schemes through a crossing
        
        SeedLot ideotypeSl = seedLotConstructor.cross(c1targetpn.getPlant().getGenotype(), c3targetpn.getPlant().getGenotype());
        Genotype ideotype = new Genotype(Arrays.asList(chr1_0_1, chr2_001_110));
        
        // now compute all (non discarded) alignments, only one alignment should be retained
        
        BranchAndBoundSolutionManager solManager = new BranchAndBoundSolutionManager(new DefaultDominatesRelation(),
                                                                                        ideotype, popSizeTools, null,
                                                                                        null, null, null, false);
        SchemeMerger merger = new MergeFirstSchemeMerger(new CrossingSchemeAlternatives(s1_1g), new CrossingSchemeAlternatives(s2_2g),
                                                                                        map, solManager, ideotypeSl);
        List<CrossingSchemeAlternatives> merged = merger.combineSchemes();
        List<CrossingScheme> mergedIndividual = new ArrayList<>();
        for(CrossingSchemeAlternatives ca : merged){
            mergedIndividual.addAll(ca.getAlternatives());
        }
                
        // verify: only one alignment should be retained (other alignments are dominated by this single alignment)
        // NOTE: there are two possible genotypes among the offspring of the final crossing, so this yields TWO
        // new crossing schedules with the SAME alignment of generations of the smaller schedules
        assertEquals(2, mergedIndividual.size());
        
    }

}