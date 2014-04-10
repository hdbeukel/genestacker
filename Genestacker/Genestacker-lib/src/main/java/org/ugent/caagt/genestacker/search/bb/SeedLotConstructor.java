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
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public abstract class SeedLotConstructor {
    
    // cached gametes per chromosome for previously considered genotypes
    protected Map<Genotype, List<Map<Haplotype, Double>> > cachedGametesPerChrom;
    
    // genetic map
    protected GeneticMap map;
    
    public SeedLotConstructor(GeneticMap map){
        this.map = map;
        // use concurrent hash map for caching (accessed in parallel by different cross workers)
        cachedGametesPerChrom = new ConcurrentHashMap<>();
    }
    
    public void clearCache(){
        cachedGametesPerChrom.clear();
    }
    
    /**
     * Create entire seed lot obtained by crossing the two given genotypes.
     * 
     * @param g1 genotype 1
     * @param g2 genotype 2
     * @return seed lot obtained by crossing the given genotypes
     * @throws GenotypeException if anything goes wrong while creating the seed lot
     */
    public abstract SeedLot cross(Genotype g1, Genotype g2) throws GenotypeException;
    
    /**
     * Generate PART of the seed lot obtained by crossing two given genotypes, confined to a predefined
     * set of genotypes among the offspring for which properties (LPA, probability) are to be inferred.
     * 
     * @param g1 genotype 1
     * @param g2 genotype 2
     * @param desiredChildGenotypes child genotypes to be included in the seed lot
     * @return partial seed lot obtained by crossing the given genotypes, confined to the predefined set of children
     * @throws GenotypeException if anything goes wrong while creating the seed lot
     */
    public abstract SeedLot partialCross(Genotype g1, Genotype g2, Set<Genotype> desiredChildGenotypes) throws GenotypeException;
    
    /**
     * Selfing: cross the given genotype with itself. 
     * 
     * @param g genotype
     * @return seed lot obtained by selfing the given genotype
     * @throws GenotypeException if anything goes wrong while creating the seed lot
     */
    public SeedLot self(Genotype g) throws GenotypeException{
        return cross(g, g);
    }
    
    /**
     * Partial selfing.
     * 
     * @param g genotype
     * @param desiredChildGenotypes child genotypes to be included in the seed lot
     * @return partial seed lot obtained by selfing the given genotype, confined to the predefined set of children
     * @throws GenotypeException if anything goes wrong while creating the seed lot
     */
    public SeedLot partialSelf(Genotype g, Set<Genotype> desiredChildGenotypes) throws GenotypeException{
        return partialCross(g, g, desiredChildGenotypes);
    }
    
}
