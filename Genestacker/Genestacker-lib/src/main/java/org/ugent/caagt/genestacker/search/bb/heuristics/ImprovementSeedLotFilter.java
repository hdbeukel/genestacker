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

package org.ugent.caagt.genestacker.search.bb.heuristics;

import java.util.Set;
import org.ugent.caagt.genestacker.Genotype;
import org.ugent.caagt.genestacker.SeedLot;
import org.ugent.caagt.genestacker.search.DominatesRelation;
import org.ugent.caagt.genestacker.search.GenericParetoFrontierWithoutDescriptor;

/**
 *
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class ImprovementSeedLotFilter implements SeedLotFilter {

    private GenotypeImprovement impr;
    
    public ImprovementSeedLotFilter(GenotypeImprovement impr){
        this.impr = impr;
    }
    
    /**
     * Heuristically filters the genotypes from a given seed lot, where a genotype
     * is removed if there is an other genotype in the seed lot that is strictly "better"
     * according the ImprovementOverAncestorsHeuristic, and which is obtained with a higher
     * or equal probability and a lower or equal LPA.
     */
    @Override
    public SeedLot filterSeedLot(final SeedLot seedLot) {
        
        // create Pareto frontier used for filtering
        GenericParetoFrontierWithoutDescriptor<Genotype> pf = new GenericParetoFrontierWithoutDescriptor<>(
                new DominatesRelation<Genotype>() {
                    @Override
                    public boolean dominates(Genotype g1, Genotype g2) {
                        // dominates if strict improvement and prob/LPA at least as high/low
                        boolean g1ImprovesOng2 = impr.improvesOnOtherGenotype(g1, g2);
                        boolean g2ImprovesOng1 = impr.improvesOnOtherGenotype(g2, g1);
                        double probg1 = seedLot.getGenotypeGroup(g1.getAllelicFrequencies()).getProbabilityOfPhaseKnownGenotype(g1);
                        double probg2 = seedLot.getGenotypeGroup(g2.getAllelicFrequencies()).getProbabilityOfPhaseKnownGenotype(g2);
                        double lpag1 = seedLot.getGenotypeGroup(g1.getAllelicFrequencies()).getLinkagePhaseAmbiguity(g1);
                        double lpag2 = seedLot.getGenotypeGroup(g2.getAllelicFrequencies()).getLinkagePhaseAmbiguity(g2);
                        return g1ImprovesOng2 && !g2ImprovesOng1    // strict improvement
                                && probg1 >= probg2                 // equal or higher probability
                                && lpag1 <= lpag2;                  // equal or lower LPA
                    }
                });
        
        // register all genotypes in the Pareto frontier
        Set<Genotype> allGenotypes = seedLot.getGenotypes();
        pf.registerAll(allGenotypes);
        
        // filter non Pareto optimal genotypes
        for(Genotype g : allGenotypes){
            if(!pf.contains(g)){ // quick lookup in hashset
                seedLot.filterGenotype(g);
            }
        }

        // return modified seed lot object
        return seedLot;
    }

    
    @Override
    public String toString(){
        return "Improvement filter";
    }
    
}
