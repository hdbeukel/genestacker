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

/**
 * Represents a generic dominates relation.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public abstract class DominatesRelation<E> {
    
    // double comparator
    protected final DoubleComparatorWithPrecision dComp;
    
    public DominatesRelation(){
        // double comparator with default precision of 0.0001
        dComp = new DoubleComparatorWithPrecision();
    }
    
    public DominatesRelation(double doubleComparingPrecision){
        dComp = new DoubleComparatorWithPrecision(doubleComparingPrecision);
    }
    
    /**
     * Check whether object o1 dominates object o2.
     * 
     * @param o1 object 1
     * @param o2 object 2
     * @return <code>true</code> if object 1 dominates object 2
     */
    public abstract boolean dominates(E o1, E o2);
        
}
