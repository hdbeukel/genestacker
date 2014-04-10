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
import org.ugent.caagt.genestacker.search.bb.PlantDescriptor;

/**
 * This heuristic checks whether a newly selected target is an improvement over each of its ancestor plants.
 * An improvement is detected if (a) the plant contains an improvement or (b) both plants have the same genotype
 * but the current plant has a higher observation probability or lower linkage phase ambiguity compared to
 * its ancestor, taking into account the precise seed lots from which each plant is grown.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class ImprovementOverAncestorsHeuristic extends Heuristic {
    
    // plant improvement detector
    private PlantImprovement impr;
    
    public ImprovementOverAncestorsHeuristic(PlantImprovement impr){
        this.impr = impr;
    }
    
    @Override
    public boolean pruneGrowPlantFromAncestors(Set<PlantDescriptor> ancestors, PlantDescriptor p) {
        // p should improve on all ancestors
        boolean prune = false;
        Iterator<PlantDescriptor> it = ancestors.iterator();
        while(!prune && it.hasNext()){
            PlantDescriptor a = it.next();
            boolean allow = impr.improvesOnOtherPlant(p.getPlant(), a.getPlant())                                           // plant improves
                         || p.getPlant().getGenotype().equals(a.getPlant().getGenotype())
                            && (p.getProb() > a.getProb() || p.getLinkagePhaseAmbiguity() < a.getLinkagePhaseAmbiguity());  // equal genotypes, better prob or LPA
            prune = !allow;
        }
        return prune;
    }
    
}
