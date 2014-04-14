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
import junit.framework.TestCase;
import org.junit.Test;
import org.ugent.caagt.genestacker.DiploidChromosome;
import org.ugent.caagt.genestacker.GeneticMap;
import org.ugent.caagt.genestacker.Genotype;
import org.ugent.caagt.genestacker.Haplotype;
import org.ugent.caagt.genestacker.Plant;
import org.ugent.caagt.genestacker.SeedLot;
import org.ugent.caagt.genestacker.exceptions.GenestackerException;
import org.ugent.caagt.genestacker.search.CrossingNode;
import org.ugent.caagt.genestacker.search.CrossingScheme;
import org.ugent.caagt.genestacker.search.DefaultPopulationSizeTools;
import org.ugent.caagt.genestacker.search.DummyPlantNode;
import org.ugent.caagt.genestacker.search.PlantNode;
import org.ugent.caagt.genestacker.search.PopulationSizeTools;
import org.ugent.caagt.genestacker.search.SeedLotNode;
import org.ugent.caagt.genestacker.search.bb.heuristics.OptimalSeedLotHeuristic;
import org.ugent.caagt.genestacker.search.bb.heuristics.OptimalSeedLotParetoFrontierFactory;

/**
 *
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class OptimalSeedLotHeuristicTest extends TestCase {
    
    /**
     * Test optimal seed lot heuristic.
     */
    @Test
    public void testOptimalSeedLotHeuristic() throws GenestackerException{

        System.out.println("\n### TEST OPTIMAL SEED LOT HEURISTIC ###\n");
        
        final double SUCCESS_PROB = 0.9;
        PopulationSizeTools popSizeTools = new DefaultPopulationSizeTools(SUCCESS_PROB);
        
        OptimalSeedLotHeuristic heur = new OptimalSeedLotHeuristic(new OptimalSeedLotParetoFrontierFactory());
        
        /***************/
        /* TEST CASE 1 */
        /***************/

        // create genetic map
        double[][] dist = new double[][]{
          new double[]{10000, 10000, 1}
        };
        GeneticMap map = new GeneticMap(dist);
        SeedLotConstructor seedLotConstructor = new DefaultSeedLotConstructor(map);
        
        // create some haplotypes
        Haplotype hap1 = new Haplotype(new boolean[]{true, true, true, false});
        Haplotype hap2 = new Haplotype(new boolean[]{false, false, false, true});
        Haplotype hap3 = new Haplotype(new boolean[]{true, true, false, true});
        Haplotype hap4 = new Haplotype(new boolean[]{true, true, true, false});
        Haplotype hap5 = new Haplotype(new boolean[]{true, true, true, true});
        
        // create some chromosomes
        DiploidChromosome chr1 = new DiploidChromosome(hap1, hap1);
        DiploidChromosome chr2 = new DiploidChromosome(hap2, hap2);
        DiploidChromosome chr3 = new DiploidChromosome(hap1, hap2);
        DiploidChromosome chr4 = new DiploidChromosome(hap1, hap3);
        DiploidChromosome chr5 = new DiploidChromosome(hap4, hap5);
        DiploidChromosome chr6 = new DiploidChromosome(hap5, hap5);
        
        // create some genotypes
        ArrayList<DiploidChromosome> chroms1 = new ArrayList<>();
        chroms1.add(chr1);
        Genotype g1 = new Genotype(chroms1);
        ArrayList<DiploidChromosome> chroms2 = new ArrayList<>();
        chroms2.add(chr2);
        Genotype g2 = new Genotype(chroms2);
        ArrayList<DiploidChromosome> chroms3 = new ArrayList<>();
        chroms3.add(chr3);
        Genotype g3 = new Genotype(chroms3);
        ArrayList<DiploidChromosome> chroms4 = new ArrayList<>();
        chroms4.add(chr4);
        Genotype g4 = new Genotype(chroms4);
        ArrayList<DiploidChromosome> chroms5 = new ArrayList<>();
        chroms5.add(chr5);
        Genotype g5 = new Genotype(chroms5);
        ArrayList<DiploidChromosome> chroms6 = new ArrayList<>();
        chroms6.add(chr6);
        Genotype g6 = new Genotype(chroms6);
        
        // create some plants
        Plant p1 = new Plant(g1);
        Plant p2 = new Plant(g2);
        Plant p3 = new Plant(g3);
        Plant p4 = new Plant(g4);
        Plant p5 = new Plant(g5);
        Plant p6 = new Plant(g6);
        
        // create initial seed lot nodes
        SeedLot s1 = new SeedLot(g1);
        SeedLotNode sln1 = new SeedLotNode(s1, 0);
        SeedLot s2 = new SeedLot(g2);
        SeedLotNode sln2 = new SeedLotNode(s2, 0);
        
        // create initial plant nodes
        PlantNode pn1 = new PlantNode(p1, 0, sln1);
        PlantNode pn2 = new PlantNode(p2, 0, sln2);
        
        // partialCross initial plants
        SeedLot s3 = seedLotConstructor.cross(p1.getGenotype(), p2.getGenotype());
        CrossingNode cr1 = new CrossingNode(pn1, pn2);
        SeedLotNode sln3 = new SeedLotNode(s3, 1, cr1);
        
        // grow new plant in generation 1
        PlantNode pn3 = new PlantNode(p3, 1, sln3);
        // regrow plant p1 in generation 1
        PlantNode pn4 = new PlantNode(p1, 1, sln1);
        
        // partialCross plants
        SeedLot s4 = seedLotConstructor.cross(p3.getGenotype(), p1.getGenotype());
        CrossingNode cr2 = new CrossingNode(pn3, pn4);
        SeedLotNode sln4 = new SeedLotNode(s4, 2, cr2);
        
        // grow final plant
        PlantNode pn5 = new PlantNode(p4, 2, sln4);
        
        // create scheme
        CrossingScheme scheme = new CrossingScheme(popSizeTools, pn5);
        scheme.print();
        
        /******************/
        /* TEST HEURISTIC */
        /******************/
        
        assertFalse(heur.pruneCurrentScheme(scheme));
        
        /***************/
        /* TEST CASE 2 */
        /***************/
        
        // change final plant to dummy
        sln4.removeChild(pn5);
        PlantNode dummy = new DummyPlantNode(2, sln4);
        
        // create scheme
        scheme = new CrossingScheme(popSizeTools, dummy);
        scheme.print();
        
        /******************/
        /* TEST HEURISTIC */
        /******************/
        
        assertFalse(heur.pruneCurrentScheme(scheme));
                
    }

}