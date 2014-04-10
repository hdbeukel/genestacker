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

    /**
     * <p>
     * Returns a plant node with the required ID, from the required generation,
     * which can still be reused as parent in an additional crossing. In case of a
     * selfing, the plant can only be reused if it can still be used at least two
     * times as parent. In case of a crossing with distinct plants the method returns
     * a plant node with an odd number of crossings, if any. This ensures that there
     * are never multiple plants with an odd number of crossings, to avoid trouble
     * with selfings, which require *two* usages of the same parent plant.
     * </p>
     * <p>
     * Returns <code>null</code> if no appropriate reusable plant was found.
     * </p>
     * 
     * @param plantNodeID required ID of the reusable plant
     * @param generation generation in which the plant has to be found
     * @param scheme crossing scheme in which to search for the desired reusable plant
     * @param selfing indicates whether a parent for a selfing is required (counts as two usages)
     * @return an appropriate reusable plant, if any, else <code>null</code>
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
