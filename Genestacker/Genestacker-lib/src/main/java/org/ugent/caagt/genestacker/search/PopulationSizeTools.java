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
 * Tools used to compute population sizes based on the probability of obtaining the desired target genotypes.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public abstract class PopulationSizeTools {

    // desired global success probability
    private double globalSuccessRate;
    
    /**
     * Create a new instance.
     * 
     * @param globalSuccessRate desired global success rate for each entire schedule
     */
    public PopulationSizeTools(double globalSuccessRate){
        this.globalSuccessRate = globalSuccessRate;
    }
    
    /**
     * Get the desired global success rate for each entire schedule.
     * 
     * @return desired global success rate
     */
    public double getGlobalSuccessRate(){
        return globalSuccessRate;
    }
    
    /**
     * Computes the success rate per target, based on the desired global success rate
     * and the number of targets obtained from nonuniform seed lots throughout the schedule.
     * 
     * @param numTargetsFromNonUniformSeedLots number of targets obtained from nonuniform seed lots
     * @return success rate per target that guarantees the desired global success rate
     */
    public abstract double computeDesiredSuccessProbPerTarget(int numTargetsFromNonUniformSeedLots);
    
    /**
     * Computes a necessary lower bound for the probability to obtain any target among the offspring
     * of a given seed lot, taking into account the maximum allowed population size per generation.
     * 
     * @param seedLot considered seed lot
     * @param maxPopSizePerGen maximum population size per generation
     * @return necessary lower bound for probability of desired target genotype to fall within the constraints
     */
    public abstract double computeTargetProbLowerBound(SeedLot seedLot, int maxPopSizePerGen);
    
    /**
     * Compute the required number of seeds taken from this seed lot per generation in which
     * plants are grown from this seed lot. Returns a map indicating how many seeds are taken
     * in each generation.
     * 
     * @param seedLotNode seed lot node
     * @return map containing number of seeds taken from this seed lot, per generation
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
