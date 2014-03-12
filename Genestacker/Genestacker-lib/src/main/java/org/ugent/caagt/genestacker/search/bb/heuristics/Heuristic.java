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
import org.ugent.caagt.genestacker.search.bb.SearchBounder;

/**
 * Dummy heuristic which never bounds. Extend this class to implement real heuristics,
 * overriding the appropriate methods.
 * 
 * @author Herman De Beukelaer <herman.debeukelaer@ugent.be>
 */
public class Heuristic implements SearchBounder {
    
    @Override
    public boolean boundCrossCurrentScheme(CrossingScheme scheme) {
        return false;
    }
    
    /**
     * Heuristically extend computed bounds when extending a crossing scheme by combining it with an arbitrary other scheme.
     */
    public CrossingSchemeDescriptor extendBoundsUponCrossing(CrossingSchemeDescriptor curBounds, CrossingScheme scheme){
        return curBounds;
    }

    @Override
    public boolean boundCrossCurrentSchemeWithSpecificOther(CrossingScheme scheme, CrossingScheme other) {
        return false;
    }
    
    /**
     * Heuristically extend computed bounds when extending a crossing scheme by combining it with a given other scheme.
     */
    public CrossingSchemeDescriptor extendBoundsUponCrossingWithSpecificOther(CrossingSchemeDescriptor curBounds,
                                                                                       CrossingScheme scheme,
                                                                                       CrossingScheme other){
        return curBounds;
    }
    
    @Override
    public boolean boundCrossCurrentSchemeWithSpecificOtherWithSelectedTarget(CrossingScheme scheme, CrossingScheme other, PlantDescriptor target) {
        return false;
    }
    
    /**
     * Heuristically extend computed bounds when extending a crossing scheme by combining it with a given other scheme, and growing
     * a preselected next target from the resulting seed lot.
     */
    public CrossingSchemeDescriptor extendBoundsUponCrossingWithSpecificOtherWithSelectedTarget(CrossingSchemeDescriptor curBounds,
                                                                                                         CrossingScheme scheme,
                                                                                                         CrossingScheme other,
                                                                                                         PlantDescriptor target){
        return curBounds;
    }

    @Override
    public boolean boundSelfCurrentScheme(CrossingScheme scheme) {
        return false;
    }
    
    /**
     * Heuristically extend computed bounds when extending a crossing scheme through a selfing.
     */
    public CrossingSchemeDescriptor extendBoundsUponSelfing(CrossingSchemeDescriptor curBounds, CrossingScheme scheme){
        return curBounds;
    }
    
    @Override
    public boolean boundSelfCurrentSchemeWithSelectedTarget(CrossingScheme scheme, PlantDescriptor target) {
        return false;
    }
    
    /**
     * Heuristically extend computed bounds when extending a crossing scheme through a selfing, growing a
     * preselected next target from the resulting seed lot.
     */
    public CrossingSchemeDescriptor extendBoundsUponSelfingWithSelectedTarget(CrossingSchemeDescriptor curBounds,
                                                                                       CrossingScheme scheme,
                                                                                       PlantDescriptor target){
        return curBounds;
    }
    
    @Override
    public boolean boundCurrentScheme(CrossingScheme scheme) {
        return false;
    }
    
    /**
     * Heuristically extend computed bounds for current scheme.
     */
    public CrossingSchemeDescriptor extendBoundsForCurrentScheme(CrossingSchemeDescriptor curBounds, CrossingScheme scheme){
        return curBounds;
    }
    
    @Override
    public boolean boundGrowPlantFromAncestors(Set<PlantDescriptor> ancestors, PlantDescriptor p) {
        return false;
    }
    
    @Override
    public boolean boundGrowPlantInGeneration(Plant p, int generation){
        return false;
    }
    
    @Override
    public boolean boundQueueScheme(CrossingScheme s){
        return false;
    }
    
    @Override
    public boolean boundDequeueScheme(CrossingScheme s){
        return false;
    }
    
}
