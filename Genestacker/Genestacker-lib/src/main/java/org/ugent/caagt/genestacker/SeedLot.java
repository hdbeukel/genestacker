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
 * Represents a seed lot modelling all possible offspring of a crossing and the probability with
 * which each specific genotype is obtained.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class SeedLot {
    
    // map: allelic frequencies -> genotype group
    private Map<GenotypeAllelicFrequencies, GenotypeGroupWithSameAllelicFrequencies> genotypeGroups;
    
    // flags uniform seed lots
    private boolean uniform;
    
    /**
     * Create a new seed lot with the given genotype groups. Note that not all genotypes have necessarily been created,
     * in case heuristics have been applied to omit non-promising genotypes. However, probabilities are available for
     * all constructed genotypes.
     * 
     * @param uniform indicates whether the seed lot is uniform (i.e. whether the parents are both homozygous)
     * @param genotypeGroups genotype groups (grouped by overlapping allelic frequencies)
     */
    public SeedLot(boolean uniform, Map<GenotypeAllelicFrequencies, GenotypeGroupWithSameAllelicFrequencies> genotypeGroups){
        this.genotypeGroups = genotypeGroups;
        this.uniform = uniform;
    }
    /**
     * Create a new uniform seed lot with only one single genotype, with probability 1.0.
     * 
     * @param genotype the single genotype in this seed lot
     */
    public SeedLot(Genotype genotype){
        Map<Genotype, Double> genotypes = new HashMap<>();
        genotypes.put(genotype, 1.0);
        genotypeGroups = new HashMap<>();
        genotypeGroups.put(genotype.getAllelicFrequencies(), new GenotypeGroupWithSameAllelicFrequencies(1.0, genotype.getAllelicFrequencies(), genotypes));
        uniform = true;
    }
    
    /**
     * Get the remaining genotypes, after possible filtering(s).
     * 
     * @return set of (remaining) genotypes
     */
    public Set<Genotype> getGenotypes(){
        Set<Genotype> genotypes = new HashSet<>();
        for(GenotypeGroupWithSameAllelicFrequencies group : genotypeGroups.values()){
            genotypes.addAll(group.getGenotypes());
        }
        return genotypes;
    }
    
    /**
     * Removes a genotype from the seed lot.
     * 
     * @param g genotype to be removed
     * @return <code>true</code> if the given genotype has been successfully removed
     */
    public boolean filterGenotype(Genotype g){
        GenotypeGroupWithSameAllelicFrequencies group = genotypeGroups.get(g.getAllelicFrequencies());
        if(group == null){
            // genotype's alleic frequencies not present
            return false;
        } else {
            // remove genotype from its group
            boolean removed = group.filterGenotype(g);
            // if the group is now empty, remove it as well
            if(group.nrOfGenotypes() == 0){
                genotypeGroups.remove(g.getAllelicFrequencies());
            }
            return removed;
        }
    }
    
    /**
     * Check whether a given genotype is contained in this seed lot.
     * 
     * @param g considered genotype
     * @return <code>true</code> if the considered genotype is contained in this seed lot
     */
    public boolean contains(Genotype g){
        if(!genotypeGroups.containsKey(g.getAllelicFrequencies())){
            // observable state not obtainable, so genotype definitely not obtainable
            return false;
        } else {
            // observable state is obtainable, check for specific genotype
            return genotypeGroups.get(g.getAllelicFrequencies()).contains(g);
        }
    }
    
    /**
     * Get the allelic frequencies of all genotypes contained in this seed lot.
     * 
     * @return set of allelic frequencies
     */
    public Set<GenotypeAllelicFrequencies> getAllelicFrequencies(){
        return genotypeGroups.keySet();
    }
    
    /**
     * Get the group of genotypes with the given allelic frequencies.
     * 
     * @param freqs allelic frequencies
     * @return genotype group containing all genotypes from this seed lot with the given allelic frequencies
     */
    public GenotypeGroupWithSameAllelicFrequencies getGenotypeGroup(GenotypeAllelicFrequencies freqs){
        return genotypeGroups.get(freqs);
    }
    
    /**
     * Get the current number of genotypes, after possible filtering(s).
     * 
     * @return number of (remaining) genotypes
     */
    public int nrOfGenotypes(){
        int nr = 0;
        for(GenotypeGroupWithSameAllelicFrequencies group : genotypeGroups.values()){
            nr += group.nrOfGenotypes();
        }
        return nr;
    }
    
    /**
     * Get number of genotypes with specific allelic frequencies, after possible
     * filtering(s).
     * 
     * @param freqs allelic frequencies
     * @return number of (remaining) genotypes with the given allelic frequencies
     */
    public int nrOfGenotypes(GenotypeAllelicFrequencies freqs){
        int nr = 0;
        if(genotypeGroups.containsKey(freqs)){
            nr = genotypeGroups.get(freqs).nrOfGenotypes();
        }
        return nr;
    }
    
    /**
     * Check whether this seed lot is uniform, i.e whether it has been obtained by
     * crossing two homozygous genotypes so that the offspring is fixed to a single possibility.
     * 
     * @return <code>true</code> if this seed lot is uniform
     */
    public boolean isUniform(){
        return uniform;
    }
    
}
