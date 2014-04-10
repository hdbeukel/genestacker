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
 * Represents a possible gamete produced during meiosis.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class Gamete {

    // haploid gamete genotype (grouped by chromosomes)
    private List<HaploidChromosome> chromosomes;
    
    public Gamete(){
        chromosomes = new ArrayList<>();
    }
    
    public Gamete(List<HaploidChromosome> chromosomes){
        this.chromosomes = chromosomes;
    }
    
    public List<HaploidChromosome> getChromosomes(){
        return chromosomes;
    }
    
    public int nrOfChromosomes(){
        return chromosomes.size();
    }
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        for(int i=0; i<chromosomes.size(); i++){
            str.append(chromosomes.get(i));
        }
        return str.toString();
    }
    
    @Override
    public boolean equals(Object g){
        boolean equal = false;
        if(g instanceof Gamete){
            Gamete gg = (Gamete) g;
            equal = (chromosomes.equals(gg.chromosomes));
        }
        return equal;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (chromosomes != null ? chromosomes.hashCode() : 0);
        return hash;
    }
    
}
