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

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the observable genotype scores (allelic frequencies) of a certain diploid genotype.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class GenotypeAllelicFrequencies {
    
    // allelic frequencies per chromosome
    private List<ChromosomeAllelicFrequencies> chromFreqs;

    /**
     * Create a new instance for a given genotype.
     * 
     * @param genotype genotype for which the allelic frequencies are determined
     */
    public GenotypeAllelicFrequencies(Genotype genotype){
        chromFreqs = new ArrayList<>(genotype.nrOfChromosomes());
        for(int i=0; i<genotype.nrOfChromosomes(); i++){
            chromFreqs.add(genotype.getChromosomes().get(i).getAllelicFrequencies());
        }
    }
    
    /**
     * Get the allelic frequencies per chromosome.
     * 
     * @return allelic frequencies per chromosome
     */
    public List<ChromosomeAllelicFrequencies> getChromosomeAllelicFrequencies(){
        return chromFreqs;
    }
    
    public int nrOfChromosomes(){
        return chromFreqs.size();
    }
    
    @Override
    public boolean equals(Object s){
        boolean equal = false;
        if(s instanceof GenotypeAllelicFrequencies){
            GenotypeAllelicFrequencies ss = (GenotypeAllelicFrequencies) s;
            equal = (chromFreqs.equals(ss.getChromosomeAllelicFrequencies()));
        }
        return equal;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.chromFreqs != null ? this.chromFreqs.hashCode() : 0);
        return hash;
    }
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        for(int i=0; i<chromFreqs.size(); i++){
            str.append(chromFreqs.get(i));
        }
        return str.toString();
    }

}
