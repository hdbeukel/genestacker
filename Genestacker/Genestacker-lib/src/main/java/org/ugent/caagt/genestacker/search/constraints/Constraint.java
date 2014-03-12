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

package org.ugent.caagt.genestacker.search.constraints;

import org.ugent.caagt.genestacker.search.CrossingSchemeDescriptor;

/**
 * Interface for a constraint on the set of valid crossing schemes.
 * 
 * @author Herman De Beukelaer <herman.debeukelaer@ugent.be>
 */
public interface Constraint {
    
    /**
     * Check whether the constraint is satisfied for a given scheme.
     * 
     * @param scheme
     */
    public boolean isSatisfied(CrossingSchemeDescriptor scheme);
    
    /**
     * ID which is used to distinguish between different types of constraints
     * and to ensure there are no duplicate constraints.
     * 
     */
    public String getID();
    
}
