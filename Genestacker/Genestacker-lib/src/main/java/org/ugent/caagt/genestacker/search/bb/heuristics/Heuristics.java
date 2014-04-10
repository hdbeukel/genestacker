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
 * Combines several heuristics and prunes as soon as any of them prunes.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
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
    public boolean pruneCrossCurrentScheme(CrossingScheme scheme) {
        boolean prune = false;
        int i=0;
        while(!prune && i<heuristics.size()){
            prune = heuristics.get(i).pruneCrossCurrentScheme(scheme);
            i++;
        }
        return prune;
    }

    @Override
    public boolean pruneCrossCurrentSchemeWithSpecificOther(CrossingScheme scheme, CrossingScheme other) {
        boolean prune = false;
        int i=0;
        while(!prune && i<heuristics.size()){
            prune = heuristics.get(i).pruneCrossCurrentSchemeWithSpecificOther(scheme, other);
            i++;
        }
        return prune;
    }
    
    @Override
    public boolean pruneCrossCurrentSchemeWithSpecificOtherWithSelectedTarget(CrossingScheme scheme, CrossingScheme other, PlantDescriptor target) {
        boolean prune = false;
        int i=0;
        while(!prune && i<heuristics.size()){
            prune = heuristics.get(i).pruneCrossCurrentSchemeWithSpecificOtherWithSelectedTarget(scheme, other, target);
            i++;
        }
        return prune;
    }

    @Override
    public boolean pruneSelfCurrentScheme(CrossingScheme scheme) {
        boolean prune = false;
        int i=0;
        while(!prune && i<heuristics.size()){
            prune = heuristics.get(i).pruneSelfCurrentScheme(scheme);
            i++;
        }
        return prune;
    }
    
    @Override
    public boolean pruneSelfCurrentSchemeWithSelectedTarget(CrossingScheme scheme, PlantDescriptor target) {
        boolean prune = false;
        int i=0;
        while(!prune && i<heuristics.size()){
            prune = heuristics.get(i).pruneSelfCurrentSchemeWithSelectedTarget(scheme, target);
            i++;
        }
        return prune;
    }
    
    @Override
    public boolean pruneCurrentScheme(CrossingScheme scheme) {
        boolean prune = false;
        int i=0;
        while(!prune && i<heuristics.size()){
            prune = heuristics.get(i).pruneCurrentScheme(scheme);
            i++;
        }
        return prune;
    }

    @Override
    public boolean pruneGrowPlantFromAncestors(Set<PlantDescriptor> ancestors, PlantDescriptor p) {
        boolean prune = false;
        int i=0;
        while(!prune && i<heuristics.size()){
            prune = heuristics.get(i).pruneGrowPlantFromAncestors(ancestors, p);
            i++;
        }
        return prune;
    }
    
    @Override
    public boolean pruneGrowPlantInGeneration(Plant p, int generation) {
        boolean prune = false;
        int i=0;
        while(!prune && i<heuristics.size()){
            prune = heuristics.get(i).pruneGrowPlantInGeneration(p, generation);
            i++;
        }
        return prune;
    }

    @Override
    public boolean pruneQueueScheme(CrossingScheme s) {
        boolean prune = false;
        int i=0;
        while(!prune && i<heuristics.size()){
            prune = heuristics.get(i).pruneQueueScheme(s);
            i++;
        }
        return prune;
    }
    
    @Override
    public boolean pruneDequeueScheme(CrossingScheme s) {
        boolean prune = false;
        int i=0;
        while(!prune && i<heuristics.size()){
            prune = heuristics.get(i).pruneDequeueScheme(s);
            i++;
        }
        return prune;
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
