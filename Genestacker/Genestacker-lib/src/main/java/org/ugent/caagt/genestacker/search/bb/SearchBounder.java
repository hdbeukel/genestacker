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

package org.ugent.caagt.genestacker.search.bb;

import java.util.Set;
import org.ugent.caagt.genestacker.Plant;
import org.ugent.caagt.genestacker.search.CrossingScheme;

/**
 * Interface for bounding criteria of branch and bound engine and additional heuristics.
 * 
 * @author Herman De Beukelaer <herman.debeukelaer@ugent.be>
 */
public interface SearchBounder {
    
    /**
     * Check whether we should try crossing a specific partial scheme with previous
     * schemes or not (in general, *not* for crossing with a specific other scheme).
     * Method might be called several times on the same scheme during search.
     */
    public abstract boolean boundCrossCurrentScheme(CrossingScheme scheme);
    
    /**
     * Check whether we should try crossing a scheme with a specific, given other scheme.
     * Method might be called several times on the same scheme(s) during search.
     */
    public abstract boolean boundCrossCurrentSchemeWithSpecificOther(CrossingScheme scheme, CrossingScheme other);
    
    /**
     * Check whether we should try crossing a scheme with a specific, given other scheme, and a preselected target
     * among the offspring that will be attached to the aligned schemes. Method might be called several times on the same
     * scheme(s) during search.
     */
    public abstract boolean boundCrossCurrentSchemeWithSpecificOtherWithSelectedTarget(CrossingScheme scheme, CrossingScheme other, PlantDescriptor target);
    
    /**
     * Check whether we should try selfing a specific scheme or not.
     * Method might be called several times on the same scheme during search.
     */
    public abstract boolean boundSelfCurrentScheme(CrossingScheme scheme);
    
    /**
     * Check whether we should self a specific scheme, given a preselected target
     * among the offspring that will be attached as child of the performed selfing.
     * Method might be called several times on the same scheme during search.
     */
    public abstract boolean boundSelfCurrentSchemeWithSelectedTarget(CrossingScheme scheme, PlantDescriptor target);
    
    /**
     * Check whether we should bound the given current scheme. Method might be called several
     * times on the same scheme during search, even when it is still under construction. Therefore
     * the presence of possible dangling and/or dummy plant nodes must be carefully handled.
     */
    public abstract boolean boundCurrentScheme(CrossingScheme scheme);
    
    /**
     * Check whether we should bound the search when considering to grow a specific plant
     * with given ancestors.
     */
    public abstract boolean boundGrowPlantFromAncestors(Set<PlantDescriptor> ancestors, PlantDescriptor p);
    
    /**
     * Check whether we should bound the search when considering to grow a given
     * plant in a generation >= the given generation
     */
    public abstract boolean boundGrowPlantInGeneration(Plant p, int generation);
    
    /**
     * Check whether we should put a crossing scheme in the queue or not. This method is
     * particularly useful because it is guaranteed to be called only once on each scheme,
     * just before it is registered in the BFS queue. Use this method when problems could
     * be encountered in case of multiple calls on the same scheme, e.g. if the computations
     * have some side effects. Furthermore, it is also assured that this method will never be
     * called on schemes that are under construction (e.g. containing dummy and/or dangling
     * plant nodes).
     */
    public abstract boolean boundQueueScheme(CrossingScheme s);
    
    /**
     * Check whether we should bound a crossing scheme after taking it from the queue. This method is
     * particularly useful because it is guaranteed to be called only once on each scheme,
     * just after it is taken from the BFS queue. It can be used to remove schemes that seemed
     * interesting when they were queued but are no longer interesting at the time of being
     * dequeued.
     */
    public abstract boolean boundDequeueScheme(CrossingScheme s);


}
