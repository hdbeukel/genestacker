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
 * Represents a collection of ambiguous phase-known genotypes, i.e genotypes having
 * the same allelic frequencies (0,1,2 per locus) but perhaps a different linkage phase,
 * as part of a seed lot.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class GenotypeGroupWithSameAllelicFrequencies {
    
    // shared allelic frequencies
    private GenotypeAllelicFrequencies allelicFreqs;
    
    // probability of obtaining any genotype with these allelic frequencies
    private double prob;
    
    // (absolute) probabilities of obtaining a specific phase-known genotype with these allelic frequencies
    private Map<Genotype, Double> genotypeProbs;
    
    /**
     * Create a new instance.
     * 
     * @param prob probability of obtaining any genotype with the given allelic frequencies
     * @param allelicFreqs allelic frequencies
     * @param genotypeProbs absolute probabilities of obtaining each specific phase-known genotype
     */
    public GenotypeGroupWithSameAllelicFrequencies(double prob, GenotypeAllelicFrequencies allelicFreqs,
                                                    Map<Genotype, Double> genotypeProbs){
        this.prob = prob;
        this.allelicFreqs = allelicFreqs;
        this.genotypeProbs = genotypeProbs;
    }
    
    public GenotypeAllelicFrequencies getAllelicFrequencies(){
        return allelicFreqs;
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
     * Get the probability of obtaining any genotype with these allelic frequencies. 
     *
     * @return respective probability
     */
    public double getProbabilityOfGenotypeWithArbitraryLinkagePhase(){
        return prob;
    }
    
    /**
     * Get the probability of obtaining a specific phase-known genotype with these allelic frequencies.
     * 
     * @param g specific phase-known genotype for which the probability is determined
     * @return respective probability
     */
    public double getProbabilityOfPhaseKnownGenotype(Genotype g){
        if(genotypeProbs.containsKey(g)){
            return genotypeProbs.get(g);
        } else {
            return 0.0;
        }
    }
    
    /**
     * Get the linkage phase ambiguity of a given phase-known genotype with these allelic frequencies.
     * 
     * @param g genotype for which the linkage phase ambiguity is determined
     * @return linkage phase ambiguity
     */
    public Double getLinkagePhaseAmbiguity(Genotype g){
        return 1.0 - getProbabilityOfPhaseKnownGenotype(g)/prob;
    }
    
    public int nrOfGenotypes(){
        return genotypeProbs.size();
    }
    
}
