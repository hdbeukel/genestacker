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
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public interface Constraint {
    
    /**
     * Check whether the constraint is satisfied for a given scheme.
     * 
     * @param scheme given crossing scheme
     * @return <code>true</code> if the constraint is satisfied for the given scheme
     */
    public boolean isSatisfied(CrossingSchemeDescriptor scheme);
    
    /**
     * Unique string ID used to identify the different types of constraints.
     * 
     * @return unique string ID for this type of constraint
     */
    public String getID();
    
}
