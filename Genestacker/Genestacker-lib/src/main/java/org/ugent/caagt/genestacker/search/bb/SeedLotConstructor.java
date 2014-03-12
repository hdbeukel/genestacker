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

package org.ugent.caagt.genestacker.search.bb;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.ugent.caagt.genestacker.GeneticMap;
import org.ugent.caagt.genestacker.Genotype;
import org.ugent.caagt.genestacker.Haplotype;
import org.ugent.caagt.genestacker.SeedLot;
import org.ugent.caagt.genestacker.exceptions.GenotypeException;

/**
 * Seed lot constructor interface.
 * 
 * @author Herman De Beukelaer <herman.debeukelaer@ugent.be>
 */
public abstract class SeedLotConstructor {
    
    // cached gametes per chromosome for previously considered genotypes
    protected Map<Genotype, List<Map<Haplotype, Double>> > cachedGametesPerChrom;
    
    // genetic map
    protected GeneticMap map;
    
    public SeedLotConstructor(GeneticMap map){
        this.map = map;
        // use concurrent hash map (accessed in parallel by different cross workers)
        cachedGametesPerChrom = new ConcurrentHashMap<>();
    }
    
    public void clearCache(){
        cachedGametesPerChrom.clear();
    }
    
    /**
     * Create seed lot obtained by crossing the two given genotypes.
     */
    public abstract SeedLot cross(Genotype g1, Genotype g2) throws GenotypeException;
    public abstract SeedLot partialCross(Genotype g1, Genotype g2, Set<Genotype> desiredChildGenotypes) throws GenotypeException;
    
    /**
     * Selfing: cross genotype with itself. 
     */
    public SeedLot self(Genotype g) throws GenotypeException{
        return cross(g, g);
    }
    public SeedLot partialSelf(Genotype g, Set<Genotype> desiredChildGenotypes) throws GenotypeException{
        return partialCross(g, g, desiredChildGenotypes);
    }
    
}
