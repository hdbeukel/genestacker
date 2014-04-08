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
import org.ugent.caagt.genestacker.Genotype;
import org.ugent.caagt.genestacker.ObservableGenotypeState;
import org.ugent.caagt.genestacker.SeedLot;
import org.ugent.caagt.genestacker.util.ProbabilityTools;

/**
 * Computes population sizes so that each target is observed at least once with a desired success rate.
 * The success rate per target is automatically inferred from the given total success rate, so that the
 * entire crossing scheme will have this total success rate.
 */
public class DefaultPopulationSizeTools extends PopulationSizeTools {

    // probability tools
    private ProbabilityTools ptools;
    
    public DefaultPopulationSizeTools(double successProb){
        super(successProb);
        ptools = new ProbabilityTools();
    }
    
    /**
     * For this strategy, the desired success rate is treated as an OVERALL success rate, so that the success rate
     * per plant is computed in such a way the this overall success rate is guaranteed.
     */
    @Override
    public double computeDesiredSuccessProbPerTarget(int numTargetsFromNonUniformSeedLots){
        return Math.pow(getSuccessProbability(), 1.0 / numTargetsFromNonUniformSeedLots);
    }
    
    @Override
    public double computeTargetProbLowerBound(SeedLot seedLot, int maxPopSizePerGen){
        // lower bound based on gamma instead of gamma'; still holds as gamma' >= gamma
        return 1 - Math.pow(1-getSuccessProbability(), 1.0/maxPopSizePerGen); // same for any seed lot
    }
    
    /**
     * Compute the required number of seeds to obtain a collection of specified target plants (each at least once)
     * simultaneously among the offspring of the same seed lot. The success probability is inferred from the desired
     * total success probability of the crossing scheme and the number of targets grown from non uniform seed lots.
     * The list of given targets should never be empty.
     */
    @Override
    public long computeRequiredSeedsForMultipleTargets(Collection<PlantNode> plantNodes) {
        
        // compute desired success rate per target (equal for all targets)
        double successProbPerTarget = computeDesiredSuccessProbPerTarget(plantNodes.iterator().next().getNumTargetsFromNonUniformSeedLotsInScheme());
        
        long requiredSeeds;
        if(plantNodes.size() == 1){

            /************************/
            /* Case 1: single plant */
            /************************/

            // compute seeds required for single plant
            requiredSeeds = computeRequiredSeedsForTargetPlant(plantNodes.iterator().next());

        } else {

            /***************************/
            /* Case 2: multiple plants */
            /***************************/

            // 1) compute max and sum of seeds required for each plant individually,
            //    count the number of occurences of each genotoype and store it together
            //    with the corresponding probability with which this genotoype is produced

            long maxSeeds = 0;
            long sumSeeds = 0;
            Map<Genotype, Integer> genoFreqMap = new HashMap<>();
            Map<Genotype, Double> genoProbMap = new HashMap<>();
            for(PlantNode pn : plantNodes){
                // compute seeds required for plant
                long seeds = computeRequiredSeedsForTargetPlant(pn);
                // update max and sum
                if(seeds > maxSeeds){
                    maxSeeds = seeds;
                }
                sumSeeds += seeds;
                // track number of plants with identical genotypes
                Genotype g = pn.getPlant().getGenotype();
                if(!genoFreqMap.containsKey(g)){
                    // first plant with this genotype
                    genoFreqMap.put(g, 1);
                    genoProbMap.put(g, pn.getProbabilityOfPhaseKnownGenotype());
                } else {
                    // increase counter
                    genoFreqMap.put(g, genoFreqMap.get(g)+1);
                }
            }

            // first guess for seeds required for this collection of targets:
            // at least one seed needed per target, and also at least the
            // maximum number of seeds needed for any target individually
            requiredSeeds = Math.max(maxSeeds, plantNodes.size());

            // 2) check if current num seeds suffices to observe all targets

            // compute desired combined success probability
            double desiredSuccessProbForAllTargets = Math.pow(successProbPerTarget, plantNodes.size());
            // compute obtained combined success probability
            double[] genoProbs = new double[genoFreqMap.size()];
            int[] genoFreqs = new int[genoFreqMap.size()];
            int i=0;
            for(Genotype g : genoFreqMap.keySet()){
                genoProbs[i] = genoProbMap.get(g);
                genoFreqs[i] = genoFreqMap.get(g);
                i++;
            }
            // compute actual obtained success rate
            double obtainedSuccessProbForAllTargets = ptools.computeProbMinOcc(genoProbs, genoFreqs, requiredSeeds);

            // 3) if necessary, increase number of seeds using binary search

            if(obtainedSuccessProbForAllTargets < desiredSuccessProbForAllTargets){
                long lbound = requiredSeeds;
                long ubound = sumSeeds;
                while(Math.abs(ubound-lbound) > 1){
                    long newGuess = (lbound+ubound)/2;
                    obtainedSuccessProbForAllTargets = ptools.computeProbMinOcc(genoProbs, genoFreqs, newGuess);
                    if(obtainedSuccessProbForAllTargets < desiredSuccessProbForAllTargets){
                        // not enough seeds --> move lower bound
                        lbound = newGuess;
                    } else {
                        // yay! enough seeds --> move upper bound
                        ubound = newGuess;
                    }
                }
                requiredSeeds = ubound;
            }

        }
        
        // return required number of seeds for all targets
        return requiredSeeds;
    }

    /**
     * Compute the number of seeds required to observe the target genotype at least once
     * among the offspring generated from the parent seed lot. The success probability for
     * this target is inferred from the desired total success rate of the crossing scheme
     * in which the given plant node occurs.
     */
    @Override
    public long computeRequiredSeedsForTargetPlant(PlantNode plantNode) {
        double T = Math.log(1.0 - computeDesiredSuccessProbPerTarget(plantNode.getNumTargetsFromNonUniformSeedLotsInScheme()));
        double N = Math.log(1.0 - plantNode.getProbabilityOfPhaseKnownGenotype());
        // watch out for errors in log computation for very small probabilities
        if(N > -(1e-15)){
            // close to infinite number of seeds required, return maximum integer value
            return Long.MAX_VALUE;
        } else {
            long numSeeds = (long) Math.ceil(T/N);
            numSeeds = Math.max(numSeeds, 1); // at least 1 seed
            return numSeeds;
        }
    }

}
