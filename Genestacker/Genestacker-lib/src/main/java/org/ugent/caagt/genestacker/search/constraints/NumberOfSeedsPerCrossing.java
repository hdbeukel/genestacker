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

package org.ugent.caagt.genestacker.search.constraints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.ugent.caagt.genestacker.search.CrossingScheme;
import org.ugent.caagt.genestacker.search.SeedLotNode;

/**
 * Expresses the number of seeds that are produced from one crossing, and is used
 * to detect and resolve possible depleted seed lots.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class NumberOfSeedsPerCrossing {
    
    // number of seeds produced from one crossing
    private int nrOfSeedsPerCrossing;
    
    public NumberOfSeedsPerCrossing(int nrOfSeedsPerCrossing){
        this.nrOfSeedsPerCrossing = nrOfSeedsPerCrossing;
    }
    
    /**
     * Returns a list of depleted seed lots, i.e seed lots from which more seeds
     * are taken than the total number of available seeds in this lot (taking into
     * account the number of parent crossings of the seed lot). Initial seed lots
     * are assumed never to be depleted.
     * 
     * @param scheme crossing scheme in which depleted seed lots are identified
     * @return list of all depleted seed lot nodes
     */
    public List<SeedLotNode> getDepletedSeedLots(CrossingScheme scheme){
        // get seed lot nodes in scheme
        Collection<SeedLotNode> seedLots = scheme.getSeedLotNodes();
        // check for depleted lots
        List<SeedLotNode> depleted = new ArrayList<>();
        for(SeedLotNode seedLot : seedLots){
            if(isDepeleted(seedLot)){
                // depleted!
                depleted.add(seedLot);
            }
        }
        return depleted;
    }
    
    /**
     * Verifies whether a given seed lot is depleted.
     * 
     * @param seedLotNode given seed lot node
     * @return <code>true</code> if the given seed lot is depleted
     */
    public boolean isDepeleted(SeedLotNode seedLotNode){
        long numSeedsTaken = seedLotNode.getSeedsTakenFromSeedLot();
        int numCrossings = seedLotNode.numDuplicatesOfParentCrossing();
        // note: initial seed lot nodes are assumed never to be depleted
        return !seedLotNode.isInitialSeedLot() && numSeedsTaken > nrOfSeedsPerCrossing * numCrossings;
    }
    
    /**
     * Compute the required number of crossings to provide sufficient seeds for this seed lot,
     * so that all target plants can be obtained.
     * 
     * @param seedLotNode given seed lot node
     * @return number of crossings required to produce sufficient seeds to obtain all targets
     *         grown from this seed lot
     */
    public int getRequiredCrossingsForSufficientSeeds(SeedLotNode seedLotNode){
        return (int) Math.ceil(((double) seedLotNode.getSeedsTakenFromSeedLot()) / nrOfSeedsPerCrossing); 
    }
    
}
