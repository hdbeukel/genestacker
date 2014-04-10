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

import org.ugent.caagt.genestacker.Genotype;
import org.ugent.caagt.genestacker.SeedLot;
import org.ugent.caagt.genestacker.search.DominatesRelation;
import org.ugent.caagt.genestacker.search.GenericParetoFrontier;
import org.ugent.caagt.genestacker.search.SeedLotNode;

/**
 * Creates a Pareto frontier to store Pareto optimal seed lots w.r.t a genotype that is to be grown from these
 * seed lots. A seed lot will be accepted in the Pareto frontier for a given genotype, if it is Pareto optimal
 * w.r.t the probability to produce this genotype and the corresponding linkage phase ambiguity. These criteria
 * are modelled in a private class SeedLotDominatesRelation, which is used to instantiate the Pareto frontier
 * for a given genotype.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class OptimalSeedLotParetoFrontierFactory {

    /**
     * Creates a seed lot Pareto frontier for a given genotype.
     * 
     * @param g genotype
     * @return corresponding Pareto frontier to identify Pareto optimal seed lots for this genotype
     */
    public GenericParetoFrontier<SeedLotNode, SeedLot> createSeedLotParetoFrontier(Genotype g){
        return new SeedLotParetoFrontier(g);
    }
    
    /**
     * Implements Pareto frontier for seed lot nodes, using the appropriate dominates relation.
     */
    private class SeedLotParetoFrontier extends GenericParetoFrontier<SeedLotNode, SeedLot> {
        
        public SeedLotParetoFrontier(Genotype g){
            super(createDominatesRelation(g));
        }

        @Override
        public SeedLot inferDescriptor(SeedLotNode sln) {
            return sln.getSeedLot();
        }
        
    }
    
    /**
     * Creates an instance of the dominates relation used for the produced Pareto frontier for a given genotype.
     * 
     * @param g genotype
     * @return corresponding dominates relation to identify Pareto optimal seed lots for this genotype
     */
    protected SeedLotDominatesRelation createDominatesRelation(Genotype g){
        return new SeedLotDominatesRelation(g);
    }
    
    /**
     * Implements default dominates relation for seed lots.
     */
    protected class SeedLotDominatesRelation extends DominatesRelation<SeedLot>{
        
        // the genotype under consideration
        protected Genotype genotype;
        
        public SeedLotDominatesRelation(Genotype genotype){
            this.genotype = genotype;
        }
        
        @Override
        public boolean dominates(SeedLot s1, SeedLot s2) {
            // if s1 cannot produce the desired genotype, it can never dominate
            if(!s1.contains(genotype)){
                return false;
            } else if(!s2.contains(genotype)){
                // s1 can produce the desired genotype so if s2 can't, s1 dominates
                return true;
            } else {
                // both seed lots can produce the desired genotype, so compare
                // based on the probability and LPA of the desired genotype
                double s1p = s1.getGenotypeGroup(genotype.getAllelicFrequencies()).getProbabilityOfPhaseKnownGenotype(genotype);
                double s2p = s2.getGenotypeGroup(genotype.getAllelicFrequencies()).getProbabilityOfPhaseKnownGenotype(genotype);
                double s1lpa = s1.getGenotypeGroup(genotype.getAllelicFrequencies()).getLinkagePhaseAmbiguity(genotype);
                double s2lpa = s2.getGenotypeGroup(genotype.getAllelicFrequencies()).getLinkagePhaseAmbiguity(genotype);
                
                boolean noWorse = (s1p >= s2p && s1lpa <= s2lpa);
                boolean atLeastOneBetter = (s1p > s2p || s1lpa < s2lpa);
                return noWorse && atLeastOneBetter;
            }
        }
    }
    
}
