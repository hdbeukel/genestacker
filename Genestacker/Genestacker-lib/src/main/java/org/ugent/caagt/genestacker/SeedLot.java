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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents a seed lot containing seeds with different possible genotypes.
 * 
 * @author Herman De Beukelaer <herman.debeukelaer@ugent.be>
 */
public class SeedLot {
    
    // map: observable genotype state -> indistinguishable genotype group
    private Map<ObservableGenotypeState, IndistinguishableGenotypeGroup> genotypeGroups;
    
    // flags uniform seed lots
    private boolean uniform;
    
    /**
     * Create a new seed lot with given genotype groups.
     * 
     * @param genotypeGroups  
     */
    public SeedLot(boolean uniform, Map<ObservableGenotypeState, IndistinguishableGenotypeGroup> genotypeGroups){
        this.genotypeGroups = genotypeGroups;
        this.uniform = uniform;
    }
    /**
     * Create a new uniform seed lot with only one single genotype, with probability 1.0.
     */
    public SeedLot(Genotype genotype){
        Map<Genotype, Double> genotypes = new HashMap<>();
        genotypes.put(genotype, 1.0);
        genotypeGroups = new HashMap<>();
        genotypeGroups.put(genotype.getObservableState(), new IndistinguishableGenotypeGroup(1.0, genotype.getObservableState(), genotypes));
        uniform = true;
    }
    
    /**
     * Get the remaining genotypes, after possible filtering(s).
     */
    public Set<Genotype> getGenotypes(){
        Set<Genotype> genotypes = new HashSet<>();
        for(IndistinguishableGenotypeGroup group : genotypeGroups.values()){
            genotypes.addAll(group.getGenotypes());
        }
        return genotypes;
    }
    
    /**
     * Removes a genotype from the seed lot and returns whether this operation
     * was successful.
     */
    public boolean filterGenotype(Genotype g){
        IndistinguishableGenotypeGroup group = genotypeGroups.get(g.getObservableState());
        if(group == null){
            // genotype's observable state not present
            return false;
        } else {
            // remove genotype from its group
            boolean removed = group.filterGenotype(g);
            // if the group is now empty, remove it as well
            if(group.nrOfGenotypes() == 0){
                genotypeGroups.remove(g.getObservableState());
            }
            return removed;
        }
    }
    
    /**
     * Check whether a given genotype can be grown from this seed lot.
     */
    public boolean canProduceGenotype(Genotype g){
        if(!genotypeGroups.containsKey(g.getObservableState())){
            // observable state not obtainable, so genotype definitely not obtainable
            return false;
        } else {
            // observable state is obtainable, check for specific genotype
            return genotypeGroups.get(g.getObservableState()).contains(g);
        }
    }
    
    public Set<ObservableGenotypeState> getObservableGenotypeStates(){
        return genotypeGroups.keySet();
    }
    
    /**
     * Get the group of genotypes with the same observable state.
     */
    public IndistinguishableGenotypeGroup getGenotypeGroup(ObservableGenotypeState state){
        return genotypeGroups.get(state);
    }
    
    /**
     * Get the current number of genotypes, after possible filtering(s).
     */
    public int nrOfGenotypes(){
        int nr = 0;
        for(IndistinguishableGenotypeGroup group : genotypeGroups.values()){
            nr += group.nrOfGenotypes();
        }
        return nr;
    }
    
    /**
     * Get number of genotypes with a specific observable state, after possible
     * filtering(s).
     */
    public int nrOfGenotypes(ObservableGenotypeState state){
        int nr = 0;
        if(genotypeGroups.containsKey(state)){
            nr = genotypeGroups.get(state).nrOfGenotypes();
        }
        return nr;
    }
    
    /**
     * Check whether this seed lot is uniform, i.e. whether only one single
     * observation is obtained from it with a probability of 1.0 (possibly with
     * multiple underlying genotypes). Return value is based on the original seed
     * lot before possible filtering.
     */
    public boolean isUniform(){
        return uniform;
    }
    
}
