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

import java.util.Arrays;

/**
 * Represents the observable allelic frequencies of a diploid chromosome. For each target locus,
 * it is indicated whether the target allele is present once, twice or not at all.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class ChromosomeAllelicFrequencies {
    
    // state
    private AllelicFrequency[] freqs;
    
    /**
     * Create new instance, given the array of allelic frequencies.
     * 
     * @param freqs allelic frequency of each locus
     */
    public ChromosomeAllelicFrequencies(AllelicFrequency[] freqs){
        this.freqs = freqs;
    }
    
    public AllelicFrequency[] getAllelicFrequencies(){
        return freqs;
    }
    
    public int nrOfLoci(){
        return freqs.length;
    }
    
    @Override
    public boolean equals(Object s){
        boolean equal = false;
        if(s instanceof ChromosomeAllelicFrequencies){
            ChromosomeAllelicFrequencies ss = (ChromosomeAllelicFrequencies) s;
            equal = (Arrays.equals(freqs, ss.freqs));
        }
        return equal;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Arrays.deepHashCode(freqs);
        return hash;
    }
    
    @Override
    public String toString(){
        return Arrays.toString(freqs);
    }
    
}
