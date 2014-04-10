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

import java.util.Set;
import org.ugent.caagt.genestacker.Plant;
import org.ugent.caagt.genestacker.search.CrossingScheme;
import org.ugent.caagt.genestacker.search.CrossingSchemeDescriptor;
import org.ugent.caagt.genestacker.search.bb.PlantDescriptor;
import org.ugent.caagt.genestacker.search.bb.PruningCriterion;

/**
 * Dummy heuristic which never prunes anything. Extend this class
 * to implement actual heuristics, overriding the appropriate methods.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class Heuristic implements PruningCriterion {
    
    @Override
    public boolean pruneCrossCurrentScheme(CrossingScheme scheme) {
        return false;
    }
    
    /**
     * Heuristically extend computed bounds when combining the given scheme with any arbitrary other scheme.
     * 
     * @param curBounds currently computed bounds
     * @param scheme scheme to be extended by combining it with any other scheme
     * @return heuristically extended bounds
     */
    public CrossingSchemeDescriptor extendBoundsUponCrossing(CrossingSchemeDescriptor curBounds, CrossingScheme scheme){
        return curBounds;
    }

    @Override
    public boolean pruneCrossCurrentSchemeWithSpecificOther(CrossingScheme scheme, CrossingScheme other) {
        return false;
    }
    
    /**
     * Heuristically extend computed bounds when combining the given scheme with a given other scheme.
     * 
     * @param curBounds currently computed bounds
     * @param scheme scheme to be extended by combining it with the given other scheme
     * @param other given other scheme
     * @return heuristically extended bounds
     */
    public CrossingSchemeDescriptor extendBoundsUponCrossingWithSpecificOther(CrossingSchemeDescriptor curBounds,
                                                                                       CrossingScheme scheme,
                                                                                       CrossingScheme other){
        return curBounds;
    }
    
    @Override
    public boolean pruneCrossCurrentSchemeWithSpecificOtherWithSelectedTarget(CrossingScheme scheme, CrossingScheme other, PlantDescriptor target) {
        return false;
    }
    
    /**
     * Heuristically extend computed bounds when combining the given scheme with a given other scheme, with a
     * preselected target to be aimed for among the offspring of the performed crossing.
     * 
     * @param curBounds currently computed bounds
     * @param scheme scheme to be extended by combining it with the given other scheme
     * @param other given other scheme
     * @param target preselected target to be aimed for among the offspring
     * @return heuristically extended bounds
     */
    public CrossingSchemeDescriptor extendBoundsUponCrossingWithSpecificOtherWithSelectedTarget(CrossingSchemeDescriptor curBounds,
                                                                                                         CrossingScheme scheme,
                                                                                                         CrossingScheme other,
                                                                                                         PlantDescriptor target){
        return curBounds;
    }

    @Override
    public boolean pruneSelfCurrentScheme(CrossingScheme scheme) {
        return false;
    }
    
    /**
     * Heuristically extend computed bounds when selfing the final plant of a given crossing scheme.
     * 
     * @param curBounds currently computed bounds
     * @param scheme scheme to be extended through a selfing
     * @return heuristically extended bounds
     */
    public CrossingSchemeDescriptor extendBoundsUponSelfing(CrossingSchemeDescriptor curBounds, CrossingScheme scheme){
        return curBounds;
    }
    
    @Override
    public boolean pruneSelfCurrentSchemeWithSelectedTarget(CrossingScheme scheme, PlantDescriptor target) {
        return false;
    }
    
    /**
     * Heuristically extend computed bounds when selfing the final plant of a given crossing scheme, with a
     * preselected target to be aimed for among the offspring.
     * 
     * @param curBounds currently computed bounds
     * @param scheme scheme to be extended through a selfing
     * @param target preselected target to be aimed for among the offspring
     * @return heuristically extended bounds
     */
    public CrossingSchemeDescriptor extendBoundsUponSelfingWithSelectedTarget(CrossingSchemeDescriptor curBounds,
                                                                                       CrossingScheme scheme,
                                                                                       PlantDescriptor target){
        return curBounds;
    }
    
    @Override
    public boolean pruneCurrentScheme(CrossingScheme scheme) {
        return false;
    }
    
    /**
     * Heuristically extend computed bounds for current scheme.
     * 
     * @param curBounds currently computed bounds
     * @param scheme current scheme
     * @return heuristically extended bounds
     */
    public CrossingSchemeDescriptor extendBoundsForCurrentScheme(CrossingSchemeDescriptor curBounds, CrossingScheme scheme){
        return curBounds;
    }
    
    @Override
    public boolean pruneGrowPlantFromAncestors(Set<PlantDescriptor> ancestors, PlantDescriptor p) {
        return false;
    }
    
    @Override
    public boolean pruneGrowPlantInGeneration(Plant p, int generation){
        return false;
    }
    
    @Override
    public boolean pruneQueueScheme(CrossingScheme s){
        return false;
    }
    
    @Override
    public boolean pruneDequeueScheme(CrossingScheme s){
        return false;
    }
    
}
