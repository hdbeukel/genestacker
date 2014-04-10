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
import org.ugent.caagt.genestacker.Plant;
import org.ugent.caagt.genestacker.search.DominatesRelation;
import org.ugent.caagt.genestacker.search.GenericParetoFrontierWithoutDescriptor;

/**
 * Filter initial plants by removing:
 *      - duplicates
 *      - plants which are strictly dominated by an other initial plant, according to a given plant improvement criterion
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class InitialPlantFilter implements PlantCollectionFilter {

    // duplicate plant filter
    private PlantCollectionFilter duplicateFilter;
    
    // plant improvement criterion
    private PlantImprovement impr;
    
    public InitialPlantFilter(PlantCollectionFilter duplicateFilter, PlantImprovement impr){
        this.duplicateFilter = duplicateFilter;
        this.impr = impr;
    }
    
    @Override
    public Collection<Plant> filter(Collection<Plant> initialPlants) {
        
        // 1) remove duplicates according to given duplicate filter
            
        initialPlants = duplicateFilter.filter(initialPlants);

        // 2) filter based on given improvement criterion

        // Pareto frontier used for filtering
        GenericParetoFrontierWithoutDescriptor<Plant> pf = new GenericParetoFrontierWithoutDescriptor<>(
                    new DominatesRelation<Plant>() {
                        @Override
                        public boolean dominates(Plant p1, Plant p2) {
                            // dominates if strict improvement
                            boolean p1ImprovesOnp2 = impr.improvesOnOtherPlant(p1, p2);
                            boolean p2ImprovesOnp1 = impr.improvesOnOtherPlant(p2, p1);
                            return p1ImprovesOnp2 && !p2ImprovesOnp1;
                        }
                    });

        // register all plants in Pareto frontier created for filtering
        pf.registerAll(initialPlants);
        // only retain registered plants
        initialPlants.retainAll(pf.getFrontier());
        
        return initialPlants;
        
    }

}
