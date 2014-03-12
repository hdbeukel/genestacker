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
 * A Pareto optimal seed lot w.r.t a desired genotype is defined as follows:
 * <ul>
 *  <li>
 *      it must be able to produce the desired genotype (the set of all these seed lots are the candidates)
 *  </li>
 *  <li>
 *      there exists no other candidate seed lot which dominates the current candidate, i.e which has both a higher or equal probability
 *      and lower or equal linkage phase probability for the desired plant and which excels w.r.t at least one of both criteria
 *  </li>
 * </ul>
 * These criteria are expressed in the private class SeedLotDominatesRelation.
 * 
 * @author Herman De Beukelaer <herman.debeukelaer@ugent.be>
 */
public class OptimalSeedLotParetoFrontierFactory {

    /**
     * Creates a seed lot Pareto frontier for a given genotype.
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
     * Create dominates relation.
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
            if(!s1.canProduceGenotype(genotype)){
                return false;
            } else if(!s2.canProduceGenotype(genotype)){
                // s1 can produce the desired genotype so if s2 can't, s1 dominates
                return true;
            } else {
                // both seed lots can produce the desired genotype, so compare
                // based on the probability and LPA of the desired genotype
                double s1p = s1.getGenotypeGroup(genotype.getObservableState()).getProbabilityOfPhaseKnownGenotype(genotype);
                double s2p = s2.getGenotypeGroup(genotype.getObservableState()).getProbabilityOfPhaseKnownGenotype(genotype);
                double s1lpa = s1.getGenotypeGroup(genotype.getObservableState()).getLinkagePhaseAmbiguity(genotype);
                double s2lpa = s2.getGenotypeGroup(genotype.getObservableState()).getLinkagePhaseAmbiguity(genotype);
                
                boolean noWorse = (s1p >= s2p && s1lpa <= s2lpa);
                boolean atLeastOneBetter = (s1p > s2p || s1lpa < s2lpa);
                return noWorse && atLeastOneBetter;
            }
        }
    }
    
}
