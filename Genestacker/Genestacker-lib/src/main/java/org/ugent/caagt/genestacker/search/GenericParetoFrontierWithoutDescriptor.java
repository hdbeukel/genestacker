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
 * Generic Pareto frontier that does not use descriptors for comparison of the registered
 * objects, but the objects themselves.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class GenericParetoFrontierWithoutDescriptor<T> extends GenericParetoFrontier<T, T> {

    public GenericParetoFrontierWithoutDescriptor(DominatesRelation<T> rel){
        super(rel);
    }
    
    @Override
    public T inferDescriptor(T object) {
        return object;
    }

}
