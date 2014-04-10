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

import org.ugent.caagt.genestacker.exceptions.EmptyHaplotypeException;

/**
 * Haploid chromosome (single haplotype).
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class HaploidChromosome extends Chromosome {

    public HaploidChromosome(Haplotype hom){
        haplotypes = new Haplotype[1];
        haplotypes[0] = hom;
    }
    
    /**
     * Deep copy constructor.
     * 
     * @param chrom other haploid chromosome to copy (deep copy)
     * @throws EmptyHaplotypeException when the given haploid chromosome does not contain any loci
     */
    public HaploidChromosome(HaploidChromosome chrom) throws EmptyHaplotypeException{
        this(new Haplotype(chrom.getHaplotype()));
    }
    
    public Haplotype getHaplotype(){
        return haplotypes[0];
    }
    
}
