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
 * Chromosome containing one or more target genes.
 *
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public abstract class Chromosome {
    
    // haplotypes
    protected Haplotype[] haplotypes;

    public int nrOfLoci(){
        return haplotypes[0].nrOfLoci();
    }
    
    public int nrOfTargetsPresent(){
        int numTargets = 0;
        for(Haplotype hom : haplotypes){
            numTargets += hom.nrOfTargetsPresent();
        }
        return numTargets;
    }
    
    public Haplotype[] getHaplotypes(){
        return haplotypes;
    }
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        for(int i=0; i<haplotypes.length; i++){
            str.append(haplotypes[i]);
            if(i<haplotypes.length-1){
                str.append("\n");
            }
        }
        return str.toString();
    }
    
    @Override
    public boolean equals(Object c){
        boolean equal = false;
        if(c instanceof Chromosome){
            Chromosome cc = (Chromosome) c;
            equal = Arrays.equals(haplotypes, cc.haplotypes);
        }
        return equal;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Arrays.hashCode(haplotypes);
        return hash;
    }
    
}
