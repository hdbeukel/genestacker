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

import org.ugent.caagt.genestacker.exceptions.IncompatibleHaplotypesException;

/**
 * Diploid chromosome (two haplotypes).
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class DiploidChromosome extends Chromosome {

    /**
     * Create a new diploid chromosome. Because ordering of haplotypes is arbitrary
     * in nature, hap1 and hap2 are automatically reordered so that the first haplotype
     * is the smallest one according to the standard ordering of haplotypes.
     * 
     * @param hap1 haplotype 1
     * @param hap2 haplotype 2
     * @throws IncompatibleHaplotypesException when trying to create a chromosome with two haplotypes
     *         of different length
     */
    public DiploidChromosome(Haplotype hap1, Haplotype hap2) throws IncompatibleHaplotypesException{
        // check for incompatible haplotypes
        if(hap1.nrOfLoci() != hap2.nrOfLoci()){
            // different number of targets!
            throw new IncompatibleHaplotypesException("Attempted to construct a diploid chromosomes with incompatible haplotypes "
                                                        +"(different number of loci: " + hap1.nrOfLoci() + " <-> " + hap2.nrOfLoci() +")");
        }
        haplotypes = new Haplotype[2];

        if(hap1.compareTo(hap2) <= 0){
            haplotypes[0] = hap1;
            haplotypes[1] = hap2;
        } else {
            haplotypes[0] = hap2;
            haplotypes[1] = hap1;
        }
    }
    
    /**
     * Check whether the chromosome is homozygous at a specific target locus.
     * 
     * @param targetLocus locus to check for homozygosity
     * @return <code>true</code> if this chromosome is homozygous at the given locus
     */
    public boolean isHomozygousAtLocus(int targetLocus){
        return (haplotypes[0].targetPresent(targetLocus) == haplotypes[1].targetPresent(targetLocus));
    }
    
    /**
     * Check whether the chromosome is homozygous at each target locus.
     * 
     * @return <code>true</code> if this chromosome is homozygous at all considered loci
     */
    public boolean isHomozygousAtAllContainedLoci(){
        boolean homozygous = true;
        int l=0;
        while(homozygous && l<nrOfLoci()){
            homozygous = isHomozygousAtLocus(l);
            l++;
        }
        return homozygous;
    }
    
    /**
     * Check whether the chromosome is heterozygous at a specific target locus.
     * 
     * @param targetLocus locus to check for heterozygosity
     * @return <code>true</code> if this chromosome is heterozygous at the given locus
     */
    public boolean isHeterozygousAtLocus(int targetLocus){
        return !isHomozygousAtLocus(targetLocus);
    }
    
    /**
     * Compute the allelic frequencies at all considered loci of this diploid chromosome.
     * In practice, it is hard to reveal the linkage phase of a chromosome, so that usually
     * only allelic frequencies are available.
     * 
     * @return allelic frequencies of this chromosome, regardless of the linkage phase
     */
    public ChromosomeAllelicFrequencies getAllelicFrequencies(){
        AllelicFrequency[] state = new AllelicFrequency[nrOfLoci()];
        for(int i=0; i<nrOfLoci(); i++){
            if(!haplotypes[0].targetPresent(i) && !haplotypes[1].targetPresent(i)){
                state[i] = AllelicFrequency.NONE;
            } else if(haplotypes[0].targetPresent(i) && haplotypes[1].targetPresent(i)){
                state[i] = AllelicFrequency.TWICE;
            } else {
                state[i] = AllelicFrequency.ONCE;
            }
        }
        return new ChromosomeAllelicFrequencies(state);
    }
        
}
