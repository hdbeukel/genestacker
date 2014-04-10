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
 * With this relation, scheme s1 dominates scheme s2 when it has a lower population size,
 * i.e. with this definition the linkage phase ambiguity and number of generations are ignored.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class PopulationSizeOnlyDominatesRelation extends DominatesRelation<CrossingSchemeDescriptor> {

    @Override
    public boolean dominates(CrossingSchemeDescriptor s1, CrossingSchemeDescriptor s2) {
        return s1.getTotalPopSize() < s2.getTotalPopSize();
    }

}
