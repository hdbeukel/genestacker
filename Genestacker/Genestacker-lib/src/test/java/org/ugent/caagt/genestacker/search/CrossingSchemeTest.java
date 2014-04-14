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

import java.util.ArrayList;
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
        List<CrossingSchemeAlternatives> merged = new MergeFirstSchemeMerger(schemes.get(1), schemes.get(0), map, solManager, sl, ancestors,
                                                        new boolean[schemes.get(1).nrOfAlternatives()][schemes.get(0).nrOfAlternatives()],
                                                        new HashSet<Genotype>()).combineSchemes();
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
        merged = new MergeFirstSchemeMerger(schemes.get(2), schemes.get(0), map, solManager, sl, ancestors,
                                                new boolean[schemes.get(2).nrOfAlternatives()][schemes.get(0).nrOfAlternatives()],
                                                new HashSet<Genotype>()).combineSchemes();
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
        merged = new MergeFirstSchemeMerger(schemes.get(3), schemes.get(2), map, solManager, sl, ancestors,
                                                    new boolean[schemes.get(3).nrOfAlternatives()][schemes.get(2).nrOfAlternatives()],
                                                    new HashSet<Genotype>()).combineSchemes();
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
        merged = new MergeFirstSchemeMerger(schemes.get(10), schemes.get(8), map, solManager, sl, ancestors,
                                                    new boolean[schemes.get(10).nrOfAlternatives()][schemes.get(8).nrOfAlternatives()],
                                                    new HashSet<Genotype>()).combineSchemes();
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
        merged = new MergeFirstSchemeMerger(schemes.get(2), selfed.get(0), map, solManager, sl, ancestors,
                                                    new boolean[schemes.get(2).nrOfAlternatives()][selfed.get(0).nrOfAlternatives()],
                                                    new HashSet<Genotype>()).combineSchemes();
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

}