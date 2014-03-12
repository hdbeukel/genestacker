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

import java.util.Map;
import org.ugent.caagt.genestacker.exceptions.CrossingSchemeException;
import org.ugent.caagt.genestacker.exceptions.ImpossibleCrossingException;

/**
 * Special extension of a crossing node to model selfings.
 * 
 * @author Herman De Beukelaer <herman.debeukelaer@ugent.be>
 */
public class SelfingNode extends CrossingNode {

    /**
     * Creates a new selfing node with random ID, which is automatically registered with the parent plant node.
     */
    public SelfingNode(PlantNode parent) throws ImpossibleCrossingException{
        super(parent, parent);
    }
    
    /**
     * Creates a new selfing node with given ID, which is automatically registered with the parent plant node.
     */
    public SelfingNode(long ID, PlantNode parent) throws ImpossibleCrossingException{
        super(ID, parent, parent);
    }
    
    /**
     * Override to register as selfing instead of plain crossing with parent plant.
     */
    @Override
    protected void registerWithParents(){
        parent1.addSelfing(this);
    }
    
    @Override
    public boolean isSelfing(){
        return true;
    }
    
    public PlantNode getParent(){
        return parent1;
    }
    
    public void setParent(PlantNode parent){
        parent1 = parent;
        parent2 = parent;
    }
    
    /**
     * Overrides method to ensure both parents are always the same plant.
     * 
     * @param parent 
     */
    @Override
    public void setParent1(PlantNode parent){
        setParent(parent);
    }
    
    /**
     * Overrides method to ensure both parents are always the same plant.
     * 
     * @param parent 
     */
    @Override
    public void setParent2(PlantNode parent){
        setParent(parent);
    }

    /**
     * Create a deep copy of this crossing node node and its ancestor structure.
     * 
     */
    @Override
    public CrossingNode deepUpwardsCopy(boolean shiftGen, Map<String, SeedLotNode> curCopiedSeedLots,
                                                Map<String, PlantNode> curCopiedPlants)
                                                            throws CrossingSchemeException {
        // copy parent
        PlantNode parentCopy;
        // check if parent plant was already copied (in case of multiple crossings with same plant)
        if(curCopiedPlants.containsKey(parent1.getUniqueID())){
            // take present copy
            parentCopy = curCopiedPlants.get(parent1.getUniqueID());
        } else {
            // create new copy
            parentCopy = parent1.deepUpwardsCopy(shiftGen, curCopiedSeedLots, curCopiedPlants);
            curCopiedPlants.put(parent1.getUniqueID(), parentCopy);
        }
        // copy crossing node
        SelfingNode copy = new SelfingNode(ID, parentCopy);
        return copy;
    }
}
