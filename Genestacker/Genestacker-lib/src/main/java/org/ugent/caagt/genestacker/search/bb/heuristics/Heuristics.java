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

import java.util.List;
import java.util.Set;
import org.ugent.caagt.genestacker.Plant;
import org.ugent.caagt.genestacker.search.CrossingScheme;
import org.ugent.caagt.genestacker.search.CrossingSchemeDescriptor;
import org.ugent.caagt.genestacker.search.bb.PlantDescriptor;

/**
 * Combines several heuristics and bounds the search as soon as any of them bounds.
 * 
 * @author Herman De Beukelaer <herman.debeukelaer@ugent.be>
 */
public class Heuristics extends Heuristic {

    private List<Heuristic> heuristics;
    
    public Heuristics(List<Heuristic> heuristics){
        this.heuristics = heuristics;
    }
    
    public void addHeuristic(Heuristic heuristic){
        heuristics.add(heuristic);
    }
    
    public void removeHeuristic(Heuristic heuristic){
        heuristics.remove(heuristic);
    }

    @Override
    public boolean boundCrossCurrentScheme(CrossingScheme scheme) {
        boolean bound = false;
        int i=0;
        while(!bound && i<heuristics.size()){
            bound = heuristics.get(i).boundCrossCurrentScheme(scheme);
            i++;
        }
        return bound;
    }

    @Override
    public boolean boundCrossCurrentSchemeWithSpecificOther(CrossingScheme scheme, CrossingScheme other) {
        boolean bound = false;
        int i=0;
        while(!bound && i<heuristics.size()){
            bound = heuristics.get(i).boundCrossCurrentSchemeWithSpecificOther(scheme, other);
            i++;
        }
        return bound;
    }
    
    @Override
    public boolean boundCrossCurrentSchemeWithSpecificOtherWithSelectedTarget(CrossingScheme scheme, CrossingScheme other, PlantDescriptor target) {
        boolean bound = false;
        int i=0;
        while(!bound && i<heuristics.size()){
            bound = heuristics.get(i).boundCrossCurrentSchemeWithSpecificOtherWithSelectedTarget(scheme, other, target);
            i++;
        }
        return bound;
    }

    @Override
    public boolean boundSelfCurrentScheme(CrossingScheme scheme) {
        boolean bound = false;
        int i=0;
        while(!bound && i<heuristics.size()){
            bound = heuristics.get(i).boundSelfCurrentScheme(scheme);
            i++;
        }
        return bound;
    }
    
    @Override
    public boolean boundSelfCurrentSchemeWithSelectedTarget(CrossingScheme scheme, PlantDescriptor target) {
        boolean bound = false;
        int i=0;
        while(!bound && i<heuristics.size()){
            bound = heuristics.get(i).boundSelfCurrentSchemeWithSelectedTarget(scheme, target);
            i++;
        }
        return bound;
    }
    
    @Override
    public boolean boundCurrentScheme(CrossingScheme scheme) {
        boolean bound = false;
        int i=0;
        while(!bound && i<heuristics.size()){
            bound = heuristics.get(i).boundCurrentScheme(scheme);
            i++;
        }
        return bound;
    }

    @Override
    public boolean boundGrowPlantFromAncestors(Set<PlantDescriptor> ancestors, PlantDescriptor p) {
        boolean bound = false;
        int i=0;
        while(!bound && i<heuristics.size()){
            bound = heuristics.get(i).boundGrowPlantFromAncestors(ancestors, p);
            i++;
        }
        return bound;
    }
    
    @Override
    public boolean boundGrowPlantInGeneration(Plant p, int generation) {
        boolean bound = false;
        int i=0;
        while(!bound && i<heuristics.size()){
            bound = heuristics.get(i).boundGrowPlantInGeneration(p, generation);
            i++;
        }
        return bound;
    }

    @Override
    public boolean boundQueueScheme(CrossingScheme s) {
        boolean bound = false;
        int i=0;
        while(!bound && i<heuristics.size()){
            bound = heuristics.get(i).boundQueueScheme(s);
            i++;
        }
        return bound;
    }
    
    @Override
    public boolean boundDequeueScheme(CrossingScheme s) {
        boolean bound = false;
        int i=0;
        while(!bound && i<heuristics.size()){
            bound = heuristics.get(i).boundDequeueScheme(s);
            i++;
        }
        return bound;
    }

    @Override
    public CrossingSchemeDescriptor extendBoundsUponCrossing(CrossingSchemeDescriptor curBounds, CrossingScheme scheme) {
        // iteratively apply all bound extensions
        for(Heuristic h : heuristics){
            curBounds = h.extendBoundsUponCrossing(curBounds, scheme);
        }
        return curBounds;
    }

    @Override
    public CrossingSchemeDescriptor extendBoundsUponCrossingWithSpecificOther(CrossingSchemeDescriptor curBounds, CrossingScheme scheme, CrossingScheme other) {
        // iteratively apply all bound extensions
        for(Heuristic h : heuristics){
            curBounds = h.extendBoundsUponCrossingWithSpecificOther(curBounds, scheme, other);
        }
        return curBounds;
    }

    @Override
    public CrossingSchemeDescriptor extendBoundsUponCrossingWithSpecificOtherWithSelectedTarget(CrossingSchemeDescriptor curBounds, CrossingScheme scheme, CrossingScheme other, PlantDescriptor target) {
        // iteratively apply all bound extensions
        for(Heuristic h : heuristics){
            curBounds = h.extendBoundsUponCrossingWithSpecificOtherWithSelectedTarget(curBounds, scheme, other, target);
        }
        return curBounds;
    }

    @Override
    public CrossingSchemeDescriptor extendBoundsUponSelfing(CrossingSchemeDescriptor curBounds, CrossingScheme scheme) {
        // iteratively apply all bound extensions
        for(Heuristic h : heuristics){
            curBounds = h.extendBoundsUponSelfing(curBounds, scheme);
        }
        return curBounds;
    }

    @Override
    public CrossingSchemeDescriptor extendBoundsUponSelfingWithSelectedTarget(CrossingSchemeDescriptor curBounds, CrossingScheme scheme, PlantDescriptor target) {
        // iteratively apply all bound extensions
        for(Heuristic h : heuristics){
            curBounds = h.extendBoundsUponSelfingWithSelectedTarget(curBounds, scheme, target);
        }
        return curBounds;
    }

    @Override
    public CrossingSchemeDescriptor extendBoundsForCurrentScheme(CrossingSchemeDescriptor curBounds, CrossingScheme scheme) {
        // iteratively apply all bound extensions
        for(Heuristic h : heuristics){
            curBounds = h.extendBoundsForCurrentScheme(curBounds, scheme);
        }
        return curBounds;
    }
    
}
