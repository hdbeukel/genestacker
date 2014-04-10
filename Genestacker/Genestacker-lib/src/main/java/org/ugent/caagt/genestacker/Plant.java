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

/**
 * Represents a specific plant in a crossing scheme.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class Plant {
    
    // genotype of this plant (w.r.t. target genes)
    private Genotype genotype;
    
    public Plant(Genotype genotype){
        this.genotype = genotype;
    }

    public Genotype getGenotype(){
        return genotype;
    }
    
    @Override
    public String toString(){
        return genotype.toString();
    }
    
    public boolean isHomozygousAtAllTargetLoci(){
        return genotype.isHomozygousAtAllContainedLoci();
    }
    
    @Override
    public boolean equals(Object p){
        boolean equal = false;
        if(p instanceof Plant){
            Plant pp = (Plant) p;
            equal = genotype.equals(pp.getGenotype());
        }
        return equal;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + (genotype != null ? genotype.hashCode() : 0);
        return hash;
    }
    
    public boolean isDummyPlant(){
        return false;
    }
            
}
