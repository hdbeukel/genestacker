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

import java.util.Map;
import java.util.Set;

/**
 * Represents a collection of ambiguous phase-known genotypes, i.e. having
 * the same genotype scores (0,1,2 per locus) but perhaps a different linkage phase.
 * 
 * @author Herman De Beukelaer <herman.debeukelaer@ugent.be>
 */
public class IndistinguishableGenotypeGroup {
    
    // shared observable state
    private ObservableGenotypeState observableState;
    
    // probability of obtaining a genotype with this observable state
    private double prob;
    
    // (absolute) probabilities of obtaining a specific phase-known genotype
    private Map<Genotype, Double> genotypeProbs;
    
    public IndistinguishableGenotypeGroup(double prob, ObservableGenotypeState observableState,
                                            Map<Genotype, Double> genotypeProbs){
        this.prob = prob;
        this.observableState = observableState;
        this.genotypeProbs = genotypeProbs;
    }
    
    public ObservableGenotypeState getObservableGenotypeState(){
        return observableState;
    }
    
    public Set<Genotype> getGenotypes(){
        return genotypeProbs.keySet();
    }
    
    public boolean contains(Genotype g){
        return genotypeProbs.containsKey(g);
    }
    
    public boolean filterGenotype(Genotype g){
        return genotypeProbs.remove(g) != null;
    }
    
    /**
     * Get the probability of obtaining a genotype corresponding genotype scores and arbitrary linkage phase.
     */
    public double getProbabilityOfGenotypeWithArbitraryLinkagePhase(){
        return prob;
    }
    
    /**
     * Get the probability of obtaining a specific phase-known genotype with corresponding genotype scores.
     */
    public double getProbabilityOfPhaseKnownGenotype(Genotype g){
        if(genotypeProbs.containsKey(g)){
            return genotypeProbs.get(g);
        } else {
            return 0.0;
        }
    }
    
    /**
     * Get linkage phase ambiguity of a given phase-known genotype from the group.
     */
    public Double getLinkagePhaseAmbiguity(Genotype g){
        return 1.0 - getProbabilityOfPhaseKnownGenotype(g)/prob;
    }
    
    public int nrOfGenotypes(){
        return genotypeProbs.size();
    }
    
}
