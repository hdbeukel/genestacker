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
import org.ugent.caagt.genestacker.SeedLot;
import org.ugent.caagt.genestacker.util.ProbabilityTools;

/**
 * Computes population sizes so that each target is observed at least once with a desired success rate.
 * The success rate per target is automatically inferred from the given total success rate, so that the
 * entire crossing scheme will have this total success rate.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
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
        return Math.pow(getGlobalSuccessRate(), 1.0 / numTargetsFromNonUniformSeedLots);
    }
    
    @Override
    public double computeTargetProbLowerBound(SeedLot seedLot, int maxPopSizePerGen){
        // lower bound based on gamma instead of gamma'; still holds as gamma' >= gamma
        return 1 - Math.pow(1-getGlobalSuccessRate(), 1.0/maxPopSizePerGen); // same for any seed lot
    }
    
    /**
     * Compute the required number of seeds to obtain a collection of specified target plants (each at least once)
     * simultaneously among the offspring of the same seed lot. The success probability is inferred from the desired
     * total success probability of the crossing scheme and the number of targets grown from non uniform seed lots.
     * The list of given targets should never be empty.
     */
    @Override
    public long computeRequiredSeedsForMultipleTargets(Collection<PlantNode> plantNodes) {
        
        PlantNode firstPn = plantNodes.iterator().next();
        
        // compute desired success rate per target (equal for all targets)
        double successProbPerTarget = computeDesiredSuccessProbPerTarget(
                                            firstPn.getNumTargetsFromNonUniformSeedLotsInScheme()
                                        );
        
        long numSeeds;
        if(plantNodes.size() == 1 && firstPn.getNumDuplicates() == 1){

            /*******************************************/
            /* Case 1: single plant, single occurrence */
            /*******************************************/

            // compute seeds required for single plant
            numSeeds = computeRequiredSeedsForTargetPlant(firstPn);

        } else {
            
            /******************************************************/
            /* Case 2: multiple plants and/or multiple occurences */
            /******************************************************/

            // 1) compute max and sum of seeds required for each plant individually,
            //    count total number of plants (taking into account duplicates), and
            //    create arrays with (1) probability of each target and (2) number of
            //    desired duplicates of that target

            long numPlants = 0;
            // note: first compute seed counts as doubles to avoid overflow issues
            double maxSeedsD = 0;
            double sumSeedsD = 0;
            double[] targetProbs = new double[plantNodes.size()];
            int[] targetDups = new int[plantNodes.size()];
            int i=0;
            for(PlantNode pn : plantNodes){
                // update num plants
                numPlants += pn.getNumDuplicates();
                // compute seeds required for plant
                double seeds = computeRequiredSeedsForTargetPlant(pn);
                // update max and sum
                if(seeds > maxSeedsD){
                    maxSeedsD = seeds;
                }
                sumSeedsD += pn.getNumDuplicates()*seeds;
                // update arrays
                targetProbs[i] = pn.getProbabilityOfPhaseKnownGenotype();
                targetDups[i] = pn.getNumDuplicates();
                // increase counter
                i++;
            }
            // convert to long values
            long maxSeeds = (long) maxSeedsD;
            long sumSeeds = (long) sumSeedsD;

            // first guess for seeds required for this collection of targets:
            // at least one seed needed per target occurrence, and also at least
            // the maximum number of seeds needed for any target individually
            numSeeds = Math.max(numPlants, maxSeeds);

            // 2) check if current num seeds suffices to observe all targets

            // compute desired joint success probability
            double desiredSuccessProbForAllTargets = Math.pow(successProbPerTarget, numPlants);
            // compute obtained joint success probability
            double obtainedSuccessProbForAllTargets = ptools.computeProbMinOcc(targetProbs, targetDups, numSeeds);

            // 3) if necessary, increase number of seeds using binary search (at most sumSeeds)

            if(obtainedSuccessProbForAllTargets < desiredSuccessProbForAllTargets){
                long lbound = numSeeds;
                long ubound = sumSeeds;
                while(Math.abs(ubound-lbound) > 1){
                    long newGuess = (lbound+ubound)/2;
                    obtainedSuccessProbForAllTargets = ptools.computeProbMinOcc(targetProbs, targetDups, newGuess);
                    if(obtainedSuccessProbForAllTargets < desiredSuccessProbForAllTargets){
                        // not enough seeds --> move lower bound
                        lbound = newGuess;
                    } else {
                        // yay! enough seeds --> move upper bound
                        ubound = newGuess;
                    }
                }
                numSeeds = ubound;
            }

        }
        
        // return required number of seeds for all targets
        return numSeeds;
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
