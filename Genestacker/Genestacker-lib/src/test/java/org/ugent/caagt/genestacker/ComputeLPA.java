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
import org.junit.Test;
import org.ugent.caagt.genestacker.exceptions.GenestackerException;
import org.ugent.caagt.genestacker.search.DefaultPopulationSizeTools;
import org.ugent.caagt.genestacker.search.FuturePlantNode;
import org.ugent.caagt.genestacker.search.PopulationSizeTools;
import org.ugent.caagt.genestacker.search.bb.DefaultSeedLotConstructor;

/**
 *
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class ComputeLPA {

    private PopulationSizeTools popSizeTools = new DefaultPopulationSizeTools(0.95);
        
    @Test
    public void computeLPA() throws GenestackerException {
        

        /***************************************************/
        
        double[][] d = new double[][]{
          new double[] {},
          new double[] {5.1730100561463055, 26.988641283592518}
        };
        GeneticMap map = new GeneticMap(d);
        
        String[] parent = new String[]{"0 010", "0 101"};
        String[] child = new String[]{"0 011", "0 101"};
        
        showProps(parent, parent, child, map, 5);
        
        /***************************************************/
        
        d = new double[][]{
          new double[] {1000000, 31, 42}
        };
        map = new GeneticMap(d);
        
        String[] parent1 = new String[]{"0010", "0101"};
        String[] parent2 = new String[]{"1001", "0101"};
        child = new String[]{"1101", "0011"};
        
        showProps(parent1, parent2, child, map, 4);
        
        /***************************************************/
        
        d = new double[][]{
          new double[] {},
          new double[] {31, 42}
        };
        map = new GeneticMap(d);
        
        parent1 = new String[]{"0 010", "0 101"};
        parent2 = new String[]{"1 001", "0 101"};
        child = new String[]{"1 101", "0 011"};
        
        showProps(parent1, parent2, child, map, 4);
        
        /***************************************************/
        
        d = new double[][]{
          new double[] {1000000, 31, 42}
        };
        map = new GeneticMap(d);
        
        parent1 = new String[]{"0010", "0101"};
        parent2 = parent1;
        child = new String[]{"0101", "0011"};
        
        showProps(parent1, parent2, child, map, 4);
        
        /***************************************************/
        
        d = new double[][]{
          new double[] {1000000, 1000000, 1000000, 1000000, 1000000}
        };
        map = new GeneticMap(d);
        
        parent1 = new String[]{"000000", "111110"};
        parent2 = new String[]{"111110", "011111"};
        child = new String[]{"000000", "111111"};
        
        showProps(parent1, parent2, child, map, 2);
        
        /***************************************************/
        
    }
    
    public void showProps(String[] parent1, String[] parent2, String[] child, GeneticMap map, int numTargetsFromNonUniformSeedLot) throws GenestackerException {
        // convert string arrays to genotypes
        Genotype g1 = convertToGenotype(parent1);
        Genotype g2 = convertToGenotype(parent2);
        Genotype c = convertToGenotype(child);
        // create seed lot by crossing parents
        SeedLot sl = new DefaultSeedLotConstructor(map).cross(g1, g2);
        // output parents, child, LPA, observable state probability and population size
        DecimalFormat df = new DecimalFormat("#.##");
        System.out.println("----------");
        System.out.println("parents:");
        System.out.println(g1 + ",");
        System.out.println(g2);
        System.out.println("child:");
        System.out.println(c);
        System.out.println("LPA = " + df.format(100 * sl.getGenotypeGroup(c.getAllelicFrequencies()).getLinkagePhaseAmbiguity(c)) + "%");
        System.out.println("p(obs) = " + df.format(sl.getGenotypeGroup(c.getAllelicFrequencies()).getProbabilityOfPhaseKnownGenotype(c)));
        System.out.println("pop = " + popSizeTools.computeRequiredSeedsForTargetPlant(
                                        new FuturePlantNode(
                                            numTargetsFromNonUniformSeedLot,
                                            sl.getGenotypeGroup(c.getAllelicFrequencies()).getProbabilityOfPhaseKnownGenotype(c)
                                        )
                                      ));
        System.out.println("----------");
    }
    
    public static Genotype convertToGenotype(String[] genotype) throws GenestackerException{
        // split both strings on spaces
        String[] topHaplotypes = genotype[0].split(" ");
        String[] bottomHaplotypes = genotype[1].split(" ");
        // create haplotypes and chromosomes
        List<DiploidChromosome> chroms = new ArrayList<>();
        for(int c=0; c<topHaplotypes.length; c++){
            // create chrom
            chroms.add(new DiploidChromosome(convertToHaplotype(topHaplotypes[c]), convertToHaplotype(bottomHaplotypes[c])));
        }
        // create and return genotype
        return new Genotype(chroms);
    }
    
    public static Haplotype convertToHaplotype(String hap) throws GenestackerException{
        List<Boolean> targets = new ArrayList<>();
        for(int l=0; l<hap.length(); l++){
            targets.add(hap.charAt(l) == '1' ? true : false);
        }
        return new Haplotype(targets);
    }

}