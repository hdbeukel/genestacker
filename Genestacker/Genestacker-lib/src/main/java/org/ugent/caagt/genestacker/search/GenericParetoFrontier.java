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
 * Represents a generic, abstract Pareto frontier.
 * 
 * @param <T> Type of objects to be stored in the Pareto frontier
 * @param <D> Type of inferred descriptor objects to be used for comparison
 * 
 * @author Herman De Beukelaer <herman.debeukelaer@ugent.be>
 */
public abstract class GenericParetoFrontier<T,D> {

    // dominates relation
    private DominatesRelation<D> dominatesRelation;
    
    // objects currently in Pareto frontier
    private Set<T> frontier;
    
    /**
     * Create a new Pareto frontier with given dominates relation.
     * 
     * @param dominatesRelation 
     */
    public GenericParetoFrontier(DominatesRelation<D> dominatesRelation){
        this.dominatesRelation = dominatesRelation;
        frontier = new HashSet<>();
    }
    
    /**
     * Return the current objects contained in the Pareto frontier.
     */
    public Set<T> getFrontier(){
        return frontier;
    }
    
    /**
     * Get the current size of the Pareto frontier.
     */
    public int getNumSchemes(){
        return frontier.size();
    }
    
    public boolean contains(T obj){
        return frontier.contains(obj);
    }
    
    /**
     * Register a new object in the Pareto frontier. Returns true if the newly presented
     * object is now part of the updated Pareto frontier, else false (i.e. if the object
     * is already dominated by another object or is already contained in the frontier).
     * Any other object that is now dominated by the new object is removed from the frontier.
     * 
     * Note: it is not possible that the new object is both dominated by an already
     * registered object and also dominates other registered objects itself!
     * 
     * @param newObject  
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
     * Register all objects in the given collection. Returns false as soon as one
     * of the contained objects could not be registered, but all objects that can
     * be registered will be.
     * 
     * @param newObjects
     */
    public boolean registerAll(Collection<T> newObjects){
        boolean ok=true;
        for(T obj : newObjects){
            ok = register(obj) && ok;
        }
        return ok;
    }
    
    public abstract D inferDescriptor(T object);
    
    /**
     * Check whether a given object is already dominated by a registered object, based
     * on its inferred descriptor.
     * 
     * @param object 
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
