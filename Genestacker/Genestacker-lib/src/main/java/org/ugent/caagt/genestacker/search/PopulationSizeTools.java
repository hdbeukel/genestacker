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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.ugent.caagt.genestacker.SeedLot;

/**
 *
 * @author Herman De Beukelaer <herman.debeukelaer@ugent.be>
 */
public abstract class PopulationSizeTools {

    // desired success probability
    private double successProbability;
    
    public PopulationSizeTools(double successProbability){
        this.successProbability = successProbability;
    }
    
    public double getSuccessProbability(){
        return successProbability;
    }
    
    /**
     * Computes the desired success probability per target.
     */
    public abstract double computeDesiredSuccessProbPerTarget(int numTargetsFromNonUniformSeedLots);
    
    /**
     * Computes a lower bound for the probability to observe any target as offspring of a given seed lot,
     * taking into account the maximum allowed population size per generation.
     */
    public abstract double computeTargetProbLowerBound(SeedLot seedLot, int maxPopSizePerGen);
    
    /**
     * Compute the required number of seeds taken from this seed lot per
     * generation in which some plants are grown from this seed lot. Returns a map
     * indicating how many seeds are taken in each generation.
     */
    public Map<Integer, Long> computeSeedsTakenFromSeedLotPerGeneration(SeedLotNode seedLotNode){
        // compute seeds required for each generation in which plants are grown from this seed lot
        Map<Integer, Long> seeds = new HashMap<>();
        Map<Integer, Set<PlantNode>> children = seedLotNode.getChildren();
        for(int g : children.keySet()){
            // compute seeds taken in generation g:
            Set<PlantNode> plantNodes = children.get(g);
            long seedsGen = computeRequiredSeedsForMultipleTargets(plantNodes);
            // store num seeds of gen in map
            seeds.put(g, seedsGen);
        }
        // return map
        return seeds;
    }
    
    public abstract long computeRequiredSeedsForMultipleTargets(Collection<PlantNode> plantNodes);
    
    public abstract long computeRequiredSeedsForTargetPlant(PlantNode plantNode);
        
}
