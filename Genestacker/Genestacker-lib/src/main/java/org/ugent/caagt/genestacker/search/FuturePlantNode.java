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


/**
 * This class is used to represent a future plant node that will be attached to an extended crossing scheme.
 * It represents the key properties of the plant node and the extended scheme to which it will be attached
 * which are required to be able to compute the population size required to grow this target.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class FuturePlantNode extends PlantNode {
    
    // probability of obtaining targeted genotype
    private double prob;
    
    // min. nr of targets grown from non-uniform seed lots in extended scheme
    private int minNumTargetsFromNonUniformSeedLots;
    
    public FuturePlantNode(int minNumTargetsFromNonUniformSeedLots, double prob) {
        super(null, -1, null, -1, 0, 1);
        this.prob = prob;
        this.minNumTargetsFromNonUniformSeedLots = minNumTargetsFromNonUniformSeedLots;
    }
    
    @Override
    public double getProbabilityOfPhaseKnownGenotype(){
        return prob;
    }
    
    @Override
    public int getNumTargetsFromNonUniformSeedLotsInScheme(){
        return minNumTargetsFromNonUniformSeedLots;
    }

}
