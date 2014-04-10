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
 * Constraint on the total number of crossings in the scheme.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class MaxTotalCrossings implements Constraint {

     // ID
    private static final String ID = "MaxTotalCrossings";
    
    // maximum total number of crossings
    private int maxCrossings;
    
    public MaxTotalCrossings(int maxCrossings){
        this.maxCrossings = maxCrossings;
    }
    
    public int getMaxCrossings(){
        return maxCrossings;
    }
    
    @Override
    public boolean isSatisfied(CrossingSchemeDescriptor scheme) {
        return scheme.getNumCrossings() <= maxCrossings;
    }

    @Override
    public String getID() {
        return ID;
    }
    
}
