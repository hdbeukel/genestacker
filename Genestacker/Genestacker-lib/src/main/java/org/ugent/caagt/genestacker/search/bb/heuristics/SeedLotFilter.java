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

import org.ugent.caagt.genestacker.SeedLot;

/**
 *
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public interface SeedLotFilter {

    /**
     * Filter the given seed lot. Directly modifies and returns the original seed lot object, after filtering.
     * 
     * @param seedLot seed lot to be filtered
     * @return filtered seed lot (in place)
     */
    public SeedLot filterSeedLot(SeedLot seedLot);
    
}
