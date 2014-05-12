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
import org.ugent.caagt.genestacker.Plant;
import org.ugent.caagt.genestacker.search.CrossingNode;
import org.ugent.caagt.genestacker.search.CrossingScheme;
import org.ugent.caagt.genestacker.search.CrossingSchemeDescriptor;
import org.ugent.caagt.genestacker.search.DominatesRelation;
import org.ugent.caagt.genestacker.search.ParetoFrontier;
import org.ugent.caagt.genestacker.search.SelfingNode;

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
        // special case
        if(detectSpecialCase(scheme)){
            return false;
        }
        
        // check if scheme is not dominated by previously constructed scheme with
        // same final genotype
        
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
        // special case
        if(detectSpecialCase(scheme)){
            return false;
        }
        
        // check if scheme is still contained in the respective Pareto frontier
        // when it has been dequeued
        
        // get final plant's genotype
        Genotype g = scheme.getFinalPlantNode().getPlant().getGenotype();
        // get corresponding Pareto frontier
        ParetoFrontier f = frontiers.get(g);
        // prune if scheme no longer contained in frontier
        return !f.contains(scheme);
        
    }
    
    /**
     * Detects the special case where the penultimate plant of the given scheme is homozygous at all target loci and
     * has been selfed to recreate it in the final generation. In such case, the scheme is not pruned althrough it is
     * certainly not Pareto optimal. This pattern is frequently observed in efficient schedules, as it is very easy to
     * reproduce a homozygous genotype through a selfing.
     * 
     * @param scheme considered crossing scheme
     * @return <code>true</code> if the described special case is detected, so that the scheme should not be pruned
     */
    private boolean detectSpecialCase(CrossingScheme scheme){
        Plant finalPlant = scheme.getFinalPlantNode().getPlant();
        CrossingNode lastCrossing = scheme.getFinalPlantNode().getParent().getParentCrossing();
        return lastCrossing != null && lastCrossing.isSelfing()
                && finalPlant.isHomozygousAtAllTargetLoci()
                && ((SelfingNode)lastCrossing).getParent().getPlant().equals(finalPlant);
    }
    
}
