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
 * Interface of an abstract pruning criterion used for branch and bound (including heuristics).
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public interface PruningCriterion {
    
    /**
     * Check whether we should consider to combine a specific partial scheme with previous
     * schemes, through an additional crossing, or not (in general, <b>not</b> for crossings
     * with a specific other scheme). Method might be called several times on the same scheme
     * during search.
     * 
     * @param scheme partial crossing scheme
     * @return <code>true</code> if the given partial crossing scheme should <b>not</b> be extended
     *         through any additional crossing with a plant obtained by a previously constructed scheme
     */
    public abstract boolean pruneCrossCurrentScheme(CrossingScheme scheme);
    
    /**
     * Check whether we should consider to combine a scheme with a specific, given other scheme,
     * through an additional crossing. Method might be called several times on the same scheme(s)
     * during search.
     * 
     * @param scheme given partial crossing scheme
     * @param other other partial crossing scheme
     * @return <code>true</code> if the given partial scheme should <b>not</b> be extended through
     *         an additional crossing with the final plant obtained by the given other partial scheme
     */
    public abstract boolean pruneCrossCurrentSchemeWithSpecificOther(CrossingScheme scheme, CrossingScheme other);
    
    /**
     * Check whether we should consider to combine a scheme with a specific, given other scheme, through an additional
     * crossing, where a preselected target genotype has already been fixed to be aimed for among the offspring.
     * Method might be called several times on the same scheme(s) during search.
     * 
     * @param scheme given partial crossing scheme
     * @param other other partial crossing scheme
     * @param target already fixed target aimed for among the offspring of the newly performed crossing, also
     *               indicates the probability to obtain this target, the linkage phase ambiguity, etc.
     *               (see {@link PlantDescriptor})
     * @return <code>true</code> if the combination of the given crossing schemes and the selected target
     *         should <b>not</b> be considered
     */
    public abstract boolean pruneCrossCurrentSchemeWithSpecificOtherWithSelectedTarget(CrossingScheme scheme, CrossingScheme other, PlantDescriptor target);
    
    /**
     * Check whether we should consider to extend a specific scheme through a selfing or not.
     * Method might be called several times on the same scheme during search.
     * 
     * @param scheme given partial crossing scheme
     * @return <code>true</code> if the given partial scheme should <b>not</b> be extended through
     *         a selfing of its final plant
     */
    public abstract boolean pruneSelfCurrentScheme(CrossingScheme scheme);
    
    /**
     * Check whether we should consider to extend a specific scheme through a selfing, where a preselected target
     * genotype has already been fixed to be aimed for among the offspring. Method might be called several times on
     * the same scheme during search.
     * 
     * @param scheme given partial crossing scheme
     * @param target already fixed target aimed for among the offspring of the newly performed selfing, also
     *               indicates the probability to obtain this target, the linkage phase ambiguity, etc.
     *               (see {@link PlantDescriptor})
     * @return <code>true</code> if the extension of the given partial scheme through a selfing, with the
     *         selected target, should <b>not</b> be considered
     */
    public abstract boolean pruneSelfCurrentSchemeWithSelectedTarget(CrossingScheme scheme, PlantDescriptor target);
    
    /**
     * Check whether the given current scheme should be discarded. Method might be called several
     * times on the same scheme during search, even when it is still under construction. Therefore
     * the presence of possible dangling and/or dummy plant nodes must be carefully handled.
     * 
     * @param scheme given partial scheme
     * @return <code>true</code> if the given partial scheme should be discarded
     */
    public abstract boolean pruneCurrentScheme(CrossingScheme scheme);
    
    /**
     * Check whether we should consider to grow the given plant, knowing that it has a specific set of ancestors.
     * 
     * @param ancestors set of plant descriptors of all ancestors of the given plant
     * @param p plant descriptor of the given plant
     * @return <code>true</code> if a plant with the given descriptor should <b>not</b> be grown, considering
     *         the set of ancestors
     */
    public abstract boolean pruneGrowPlantFromAncestors(Set<PlantDescriptor> ancestors, PlantDescriptor p);
    
    /**
     * Check whether the given plant can be grown in the given generation or any subsequent generation.
     * 
     * @param p considered plant
     * @param generation generation
     * @return <code>true</code> if the given plant should <b>not</b> be grown in the given generation,
     *         nor in any subsequent generation
     */
    public abstract boolean pruneGrowPlantInGeneration(Plant p, int generation);
    
    /**
     * Check whether a crossing scheme should be discarded when it is offered to the BFS queue.
     * This method is particularly useful because it is guaranteed to be called only once on
     * each scheme, just before it is registered in the BFS queue. Use this method when problems
     * could be encountered in case of multiple calls on the same scheme, e.g. if the computations
     * have some side effects. Furthermore, it is also assured that this method will never be
     * called on schemes that are under construction (e.g. containing dummy and/or dangling
     * plant nodes).
     * 
     * @param s given partial crossing scheme
     * @return <code>true</code> if the given partial scheme should be discarded when it is offered
     *         to the BFS queue
     */
    public abstract boolean pruneQueueScheme(CrossingScheme s);
    
    /**
     * Check whether a crossing scheme should be discarded when it is taken from the BFS queue.
     * This method is particularly useful because it is guaranteed to be called only once on each
     * scheme, just after it is taken from the BFS queue. It can be used to remove schemes that
     * seemed interesting when they were queued but are no longer interesting at the time of being
     * dequeued.
     * 
     * @param s given partial crossing scheme
     * @return <code>true</code> if the given partial scheme should be discarded when it is taken
     *         from the BFS queue
     */
    public abstract boolean pruneDequeueScheme(CrossingScheme s);


}
