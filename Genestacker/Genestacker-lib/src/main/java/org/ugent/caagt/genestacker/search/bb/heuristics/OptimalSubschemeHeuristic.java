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

import java.util.HashMap;
import java.util.Map;
import org.ugent.caagt.genestacker.Genotype;
import org.ugent.caagt.genestacker.search.CrossingScheme;
import org.ugent.caagt.genestacker.search.CrossingSchemeDescriptor;
import org.ugent.caagt.genestacker.search.DominatesRelation;
import org.ugent.caagt.genestacker.search.ParetoFrontier;

/**
 * This heuristic keeps track of a Pareto frontier for each intermediary genotype
 * and prunes partial schemes which are dominated by previous partial schemes
 * resulting in the same intermediary genotype.
 *
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class OptimalSubschemeHeuristic extends Heuristic {

    // Pareto frontiers
    private Map<Genotype, ParetoFrontier> frontiers;
    
    // dominates relation
    private DominatesRelation<CrossingSchemeDescriptor> dominatesRelation;
    
    public OptimalSubschemeHeuristic(DominatesRelation<CrossingSchemeDescriptor> dominatesRelation){
        this.dominatesRelation = dominatesRelation;
        frontiers = new HashMap<>();
    }
    
    @Override
    public boolean pruneQueueScheme(CrossingScheme scheme){
        // get genotype of final plant of the scheme
        Genotype g = scheme.getFinalPlantNode().getPlant().getGenotype();
        // check if pareto frontier already present
        if(!frontiers.containsKey(g)){
            // create Pareto frontier for this plant
            frontiers.put(g, new ParetoFrontier(dominatesRelation));
        }
        // get the Pareto frontier for this plant
        ParetoFrontier f = frontiers.get(g);
        // try to register scheme in frontier, if not successful the scheme
        // should not be queued so true (=prune) is returned
        return !f.register(scheme);
    }
    
    @Override
    public boolean pruneDequeueScheme(CrossingScheme scheme){
        // check if scheme is still contained in the respective Pareto frontier
        // when it has been dequeued
        
        // get final plant's genotype
        Genotype g = scheme.getFinalPlantNode().getPlant().getGenotype();
        // get corresponding Pareto frontier
        ParetoFrontier f = frontiers.get(g);
        // prune if scheme no longer contained in frontier
        return !f.contains(scheme);
        
    }
    
}
