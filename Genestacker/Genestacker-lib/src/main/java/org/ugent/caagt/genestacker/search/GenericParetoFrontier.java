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

package org.ugent.caagt.genestacker.search;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Represents a generic, abstract Pareto frontier. Objects are compared based on
 * special descriptor objects that are inferred from these objects.
 * 
 * @param <T> type of objects to be stored in the Pareto frontier
 * @param <D> type of inferred descriptor objects to be used for comparison
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public abstract class GenericParetoFrontier<T,D> {

    // dominates relation
    private DominatesRelation<D> dominatesRelation;
    
    // objects currently in Pareto frontier
    private Set<T> frontier;
    
    /**
     * Create a new Pareto frontier with given dominates relation.
     * 
     * @param dominatesRelation given dominates relation
     */
    public GenericParetoFrontier(DominatesRelation<D> dominatesRelation){
        this.dominatesRelation = dominatesRelation;
        frontier = new HashSet<>();
    }
    
    /**
     * Return the current objects contained in the Pareto frontier.
     * 
     * @return set of objects in the current Pareto frontier
     */
    public Set<T> getFrontier(){
        return frontier;
    }
    
    /**
     * Get the current size of the Pareto frontier.
     * 
     * @return current Pareto frontier size
     */
    public int getNumSchemes(){
        return frontier.size();
    }
    
    /**
     * Verify whether the given object is contained in the current Pareto frontier.
     * 
     * @param obj given object
     * @return <code>true</code> if the given object is currently contained in the Pareto frontier
     */
    public boolean contains(T obj){
        return frontier.contains(obj);
    }
    
    /**
     * <p>
     * Register a new object in the Pareto frontier. Returns <code>true</code> if the newly
     * presented object is included in the updated Pareto frontier, else <code>false</code>
     * (i.e. if the object is dominated by another object or is already contained in the frontier).
     * Any other object that is dominated by the newly inserted object is removed from the frontier.
     * </p>
     * <p>
     * Note: it is not possible that the new object is both dominated by an already registered object
     * and yet also dominates other registered objects!
     * </p>
     * 
     * @param newObject object to include in the Pareto frontier, if not already included and not dominated
     *                  by any other object from the Pareto frontier
     * @return <code>true</code> if the newly presented object is included in the Pareto frontier
     */
    public synchronized boolean register(T newObject){
        D newDescriptor = inferDescriptor(newObject);
        boolean dominated = false;
        Iterator<T> it = frontier.iterator();
        while(!dominated && it.hasNext()){
            D otherDescriptor = inferDescriptor(it.next());
            // check if dominated by other
            dominated = dominatesRelation.dominates(otherDescriptor, newDescriptor);
            // conversely: if new object dominates other, remove other
            if(dominatesRelation.dominates(newDescriptor, otherDescriptor)){
                it.remove();
            }
        }
        if(!dominated){
            // register new object (if not already present)
            return frontier.add(newObject);
        } else {
            return false; // dominated by existing solution, not added
        }
    }
    
    /**
     * Register all objects in the given collection. Returns <code>true</code> if the Pareto frontier
     * has changed after presented each of the given objects.
     * 
     * @param newObjects new objects to register in the Pareto frontier
     * @return <code>true</code> if the Pareto frontier has changed after this operation
     */
    public synchronized boolean registerAll(Collection<T> newObjects){
        boolean changed = false;
        for(T obj : newObjects){
            changed = register(obj) || changed;
        }
        return changed;
    }
    
    /**
     * Get the descriptor corresponding to the given object, to be used for comparison with
     * the dominates relation used for this Pareto frontier.
     * 
     * @param object object for which a descriptor is to be inferred
     * @return inferred descriptor
     */
    public abstract D inferDescriptor(T object);
    
    /**
     * Check whether a given object is already dominated by a registered object, based
     * on its inferred descriptor.
     * 
     * @param desc descriptor of object
     * @return <code>true</code> if the object with the given descriptor is dominated by
     *         another object currently contained in the Pareto frontier
     */
    public synchronized boolean dominatedByRegisteredObject(D desc){
        boolean dominated = false;
        Iterator<T> it = frontier.iterator();
        while(!dominated && it.hasNext()){
            D otherDescriptor = inferDescriptor(it.next());
            dominated = dominatesRelation.dominates(otherDescriptor, desc);
        }
        return dominated;
    }
    
}
