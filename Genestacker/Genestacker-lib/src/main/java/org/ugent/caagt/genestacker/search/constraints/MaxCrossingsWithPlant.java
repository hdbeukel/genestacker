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

import java.util.List;
import org.ugent.caagt.genestacker.search.CrossingScheme;
import org.ugent.caagt.genestacker.search.CrossingSchemeDescriptor;
import org.ugent.caagt.genestacker.search.PlantNode;

/**
 * Constraint on the maximum number of times that a plant can be crossed, during
 * the generation in which it lives. Selfings count twice because here the same
 * plant is used both as father and mother.
 * 
 * @author Herman De Beukelaer <herman.debeukelaer@ugent.be>
 */
public class MaxCrossingsWithPlant implements Constraint {

    // ID
    private static final String ID = "MaxCrossingsWithPlant";
    
    // maximum number of crossings with plant
    private int maxCrossingsWithPlant;
    
    /**
     * Create a constraint on the maximum number of crossings with any plant.
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

    /**
     * Returns a plant node with the required ID, from the required generation,
     * which can still be reused as parent in an additional crossing. In case of a
     * selfing, the plant can only be reused if it can still be used at least two
     * times as parent. In case of a crossing with distinct plants the method returns
     * a plant node with an odd number of crossings, if any. This ensures that there
     * are never multiple plants with an odd number of crossings, to avoid trouble
     * with selfings, which require *two* usages of the same parent plant.
     * 
     * Returns null if no reusable plant was found.
     * 
     * @param plantNodeID
     * @param generation
     * @param scheme
     * @param selfing
     */
    public PlantNode getReusablePlantNode(long plantNodeID, int generation, CrossingScheme scheme, boolean selfing){
        if(selfing){
            return getReusablePlantNodeForSelfing(plantNodeID, generation, scheme);
        } else {
            return getReusablePlantNodeForCrossing(plantNodeID, generation, scheme);
        }
    }
    
    private PlantNode getReusablePlantNodeForCrossing(long plantNodeID, int generation, CrossingScheme scheme){
        // get plants with required ID, from given generation
        List<PlantNode> plants = scheme.getPlantNodesFromGenerationWithID(generation, plantNodeID);
        // look for reusable plant (with odd number of crossings, if any)
        int i=0;
        PlantNode pn, reusable = null, oddReusable = null;
        while(oddReusable == null && i<plants.size()){
            pn = plants.get(i);
            if(pn.getNumberOfTimesCrossed() + 1 <= maxCrossingsWithPlant){
                reusable = pn;
                if(reusable.getNumberOfTimesCrossed()%2 == 1){
                    // reusable plant with odd number of crossings
                    oddReusable = reusable;
                }
            }
            i++;
        }
        if(oddReusable != null){
            return oddReusable;
        } else {
            return reusable;
        }
    }
    
    private PlantNode getReusablePlantNodeForSelfing(long plantNodeID, int generation, CrossingScheme scheme){
        // get plants with required ID, from given generation
        List<PlantNode> plants = scheme.getPlantNodesFromGenerationWithID(generation, plantNodeID);
        // look for reusable plant (any will do)
        int i=0;
        PlantNode pn, reusable = null;
        while(reusable == null && i<plants.size()){
            pn = plants.get(i);
            if(pn.getNumberOfTimesCrossed() + 2 <= maxCrossingsWithPlant){
                reusable = pn;
            }
            i++;
        }
        return reusable;
    }
    
}
