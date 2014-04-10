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

package org.ugent.caagt.genestacker.search.bb.heuristics;

import java.util.List;
import java.util.Set;
import org.ugent.caagt.genestacker.Genotype;
import org.ugent.caagt.genestacker.Haplotype;
import org.ugent.caagt.genestacker.SeedLot;

/**
 * Only retain genotypes of which each chromosome contains haplotypes from a given set,
 * filter all others.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class RestrictedHaplotypesSeedLotFilter implements SeedLotFilter {

    private List<Set<Haplotype>> allowedHaplotypesPerChrom;
    
    public RestrictedHaplotypesSeedLotFilter(List<Set<Haplotype>> allowedHaplotypesPerChrom){
        this.allowedHaplotypesPerChrom = allowedHaplotypesPerChrom;
    }
    
    @Override
    public SeedLot filterSeedLot(SeedLot seedLot) {
        for(Genotype g : seedLot.getGenotypes()){
            // check chromosomes
            boolean filter = false;
            int c = 0;
            while(!filter && c < g.nrOfChromosomes()){
                // check chromosome's haplotypes
                Haplotype[] haps = g.getChromosomes().get(c).getHaplotypes();
                // filter if not both haplotypes are contained in allowed set
                filter = !(allowedHaplotypesPerChrom.get(c).contains(haps[0])
                               && allowedHaplotypesPerChrom.get(c).contains(haps[1]));
                c++;
            }
            if(filter){
                seedLot.filterGenotype(g);
            }
        }
        return seedLot;
    }
    
    @Override
    public String toString(){
        return "Haplotype filter";
    }

}
