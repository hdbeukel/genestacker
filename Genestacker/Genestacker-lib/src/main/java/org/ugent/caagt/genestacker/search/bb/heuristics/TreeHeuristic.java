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

import java.util.Iterator;
import java.util.Set;
import org.ugent.caagt.genestacker.Genotype;
import org.ugent.caagt.genestacker.search.CrossingNode;
import org.ugent.caagt.genestacker.search.CrossingScheme;
import org.ugent.caagt.genestacker.search.SeedLotNode;
import org.ugent.caagt.genestacker.search.bb.PlantDescriptor;

/**
 * Forces the crossing schedule to have a tree structure, similar to the one proposed
 * by Servin et al in 2004. This means that no reuse is allowed, and also selfings are
 * forbidden. As an exception, only the final crossing is allowed to be a selfing.
 * To prevent reuse, before crossing it is checked if each initial seed lot occurs
 * in exactly one branch of the scheme.
 *
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class TreeHeuristic extends Heuristic {
    
    // ideotype
    private Genotype ideotype;
    
    public TreeHeuristic(Genotype ideotype){
        this.ideotype = ideotype;
    }
    
    @Override
    public boolean pruneCrossCurrentSchemeWithSpecificOther(CrossingScheme scheme, CrossingScheme other) {
        // check for overlapping initial seed lots
        Set<Long> ids1 = scheme.getInitialSeedLotNodeIDs();
        Set<Long> ids2 = other.getInitialSeedLotNodeIDs();
        boolean overlap = false;
        Iterator<Long> it = ids1.iterator();
        while(!overlap && it.hasNext()){
            overlap = ids2.contains(it.next());
        }
        // prune if overlap
        return overlap;
    }
    
    @Override
    public boolean pruneSelfCurrentSchemeWithSelectedTarget(CrossingScheme scheme, PlantDescriptor target) {
        // prune any selfing which does not yield the ideotype
        return !target.getPlant().getGenotype().equals(ideotype);
    }
    
    @Override
    public boolean pruneCurrentScheme(CrossingScheme scheme){
        // prune in case of duplicated crossings
        for(SeedLotNode sln : scheme.getSeedLotNodes()){
            if(sln.numDuplicatesOfParentCrossing() > 1){
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean pruneQueueScheme(CrossingScheme scheme){
        // as only one final selfing is allowed, do not queue schemes which already contain a selfing
        // so that these will not be further extended
        if(scheme.getNumGenerations() == 0){
            return false; // initial partial scheme, do not prune
        }
        // only have to check final crossing(s)
        for(CrossingNode c : scheme.getCrossingNodesFromGeneration(scheme.getNumGenerations()-1)){
            // check for selfing
            if(c.isSelfing()){
                return true;
            }
        }
        return false;
    }
    
}
