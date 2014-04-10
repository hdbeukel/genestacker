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

package org.ugent.caagt.genestacker.search;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.ugent.caagt.genestacker.Genotype;
import org.ugent.caagt.genestacker.SeedLot;

/**
 * Used to cache seed lots created by crossing two specific genotypes.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class SeedLotCache {

    private Map<Genotype, Map<Genotype, SeedLot>> cache;
    
    public SeedLotCache(){
        // use concurrent hash map (parallel access by cross workers)
        cache = new ConcurrentHashMap<>();
    }
    
    /**
     * Get the cached seed lot obtained from crossing genotype g1 with genotype g2
     * (possibly equal in case of a selfing). If this seed lot is not yet present
     * in the cache, null is returned.
     * 
     * @param g1 genotype 1
     * @param g2 genotype 2
     * @return cached seed lot obtained from crossing the given genotypes, <code>null</code>
     *         if this seed lot is not yet present in the cache
     */
    public SeedLot getCachedSeedLot(Genotype g1, Genotype g2){
        SeedLot seedlot = null;
        // check if present in cache
        if(cache.containsKey(g1) && cache.get(g1).containsKey(g2)){
            seedlot = cache.get(g1).get(g2);
        } else if (cache.containsKey(g2) && cache.get(g2).containsKey(g1)){
            seedlot = cache.get(g2).get(g1);
        }
        return seedlot;
    }
    
    /**
     * Store the seed lot obtained from crossing genotype g1 with genotype g2 in the cache.
     * 
     * @param g1 genotype 1
     * @param g2 genotype 2
     * @param seedlot seed lot obtained by crossing the given genotypes
     */
    public void cache(Genotype g1, Genotype g2, SeedLot seedlot){
        if(!cache.containsKey(g1)){
            // use concurrent hash map
            cache.put(g1, new ConcurrentHashMap<Genotype, SeedLot>());
        }
        cache.get(g1).put(g2, seedlot);
    }
    
    public void clear(){
        cache.clear();
    }
    
}
