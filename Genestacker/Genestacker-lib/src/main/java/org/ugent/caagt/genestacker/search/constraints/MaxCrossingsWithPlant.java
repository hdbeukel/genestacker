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
import org.ugent.caagt.genestacker.search.PlantNode;

/**
 * Constraint on the maximum number of times that a plant can be crossed (in the
 * generation in which it lives). Selfings count twice because here the same
 * plant is used both as father and mother.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class MaxCrossingsWithPlant implements Constraint {

    // ID
    private static final String ID = "MaxCrossingsWithPlant";
    
    // maximum number of crossings with plant
    private int maxCrossingsWithPlant;
    
    /**
     * Create a constraint on the maximum number of crossings with any plant.
     * 
     * @param maxCrossingsWithPlant maximum number of crossings per plant
     */
    public MaxCrossingsWithPlant(int maxCrossingsWithPlant){
        this.maxCrossingsWithPlant = maxCrossingsWithPlant;
    }
    
    @Override
    public boolean isSatisfied(CrossingSchemeDescriptor scheme) {
        return scheme.getMaxCrossingsWithPlant() <= maxCrossingsWithPlant;
    }

    @Override
    public String getID() {
        return ID;
    }
    
    public int getMaxCrossingsWithPlant(){
        return maxCrossingsWithPlant;
    }
    
    
    
    /**
     * Checks whether the maximum number of crossings with the given plant has
     * been exceeded, in which case the plant should be duplicated.
     * 
     * @param plantNode given plant node
     * @return <code>true</code> if the maximum number of crossings has been exceeded for the given plant
     */
    public boolean maxCrossingsWithPlantExceeded(PlantNode plantNode){
        return plantNode.getNumberOfTimesCrossed() > maxCrossingsWithPlant * plantNode.getNumDuplicates();
    }
    
    /**
     * Get the required number of duplicates of the given plant to perform all scheduled crossings.
     * 
     * @param plantNode given plant
     * @return number of required duplicates to perform all scheduled crossings
     */
    public int getRequiredPlantDuplicatesForCrossings(PlantNode plantNode){
        return (int) Math.ceil(((double) plantNode.getNumberOfTimesCrossed()) / maxCrossingsWithPlant);
    }
    
}
