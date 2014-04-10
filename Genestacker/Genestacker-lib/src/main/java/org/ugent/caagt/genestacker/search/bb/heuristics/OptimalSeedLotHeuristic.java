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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.ugent.caagt.genestacker.Genotype;
import org.ugent.caagt.genestacker.SeedLot;
import org.ugent.caagt.genestacker.search.CrossingScheme;
import org.ugent.caagt.genestacker.search.GenericParetoFrontier;
import org.ugent.caagt.genestacker.search.PlantNode;
import org.ugent.caagt.genestacker.search.SeedLotNode;

/**
 * Heuristic that enforces each plant to be grown from a Pareto optimal seed lot
 * among all those which are available at the generation in question. Pareto optimality
 * of seed lots is defined in the Pareto frontiers created by the given factory class.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class OptimalSeedLotHeuristic extends Heuristic {

    // seed lot Pareto frontier factory
    private OptimalSeedLotParetoFrontierFactory frontierFactory;
    
    public OptimalSeedLotHeuristic(OptimalSeedLotParetoFrontierFactory frontierFactory){
        this.frontierFactory = frontierFactory;
    }
    
    @Override
    public boolean pruneQueueScheme(CrossingScheme scheme){        
        boolean ok = true;
        // map containing pareto frontier for each occuring genotype, with seed lot nodes
        // that have been created up to the current generation
        Map<Genotype, GenericParetoFrontier<SeedLotNode, SeedLot>> frontiers = new HashMap<>(); 
        // init map
        Set<Genotype> allGenotypes = getGenotypes(scheme.getPlantNodes());
        for(Genotype g : allGenotypes){
            frontiers.put(g, frontierFactory.createSeedLotParetoFrontier(g));
        }
        // go through generations
        int gen=0;
        while(ok && gen <= scheme.getNumGenerations()){
            // update Pareto frontiers with seed lot nodes from this generation
            for(Genotype g : allGenotypes){
                frontiers.get(g).registerAll(scheme.getSeedLotNodesFromGeneration(gen));
            }
            // check if plant nodes of this generation have a Pareto optimal parent, w.r.t. to
            // their contained genotype -- as soon as one is not ok we can stop
            Iterator<PlantNode> pnIt = scheme.getPlantNodesFromGeneration(gen).iterator();
            while(ok && pnIt.hasNext()){
                PlantNode pn = pnIt.next();
                if(!pn.isDummy() && !pn.isDanglingPlantNode()){
                    ok = frontiers.get(pn.getPlant().getGenotype()).contains(pn.getParent());
                }
            }
            gen++;
        }
        
        // prune if not ok
        return !ok;
    }
    
    /**
     * Get a set containing all genotypes contained in the given collection of plant nodes.
     * 
     * @param pns collection of plant nodes
     * @return set of genotypes contained in the given collection of plant nodes
     */
    private Set<Genotype> getGenotypes(Collection<PlantNode> pns){
        Set<Genotype> plants = new HashSet<>();
        for(PlantNode pn : pns){
            // ignore dummy/dangling plant nodes
            if(!pn.isDummy() && !pn.isDanglingPlantNode()){
                plants.add(pn.getPlant().getGenotype());
            }
        }
        return plants;
    }
    
}
