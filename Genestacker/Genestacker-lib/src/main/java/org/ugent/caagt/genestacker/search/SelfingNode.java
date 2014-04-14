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
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class SelfingNode extends CrossingNode {

    /**
     * Creates a new selfing node with automatically assigned ID.
     * The number of performed duplicates is set to 1 and the selfing
     * is automatically registered with its parent plant node.
     * 
     * @param parent parent of the selfing
     * @throws ImpossibleCrossingException should not be thrown for
     *         a selfing, parent is always compatible with itself
     */
    public SelfingNode(PlantNode parent) throws ImpossibleCrossingException{
        super(parent, parent);
    }
    
    /**
     * Creates a new selfing node with given ID and number of performed duplicates.
     * The selfing is automatically registered with its parent plant node.
     * 
     * @param ID given ID
     * @param numDuplicates number of performed duplicates (to generate sufficient seeds)
     * @param parent parent of the selfing
     * @throws ImpossibleCrossingException should not be thrown for
     *         a selfing, parent is always compatible with itself
     */
    public SelfingNode(long ID, int numDuplicates, PlantNode parent) throws ImpossibleCrossingException{
        super(ID, numDuplicates, parent, parent);
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
    
    /**
     * Set the parent of this selfing.
     * 
     * @param parent parent plant node
     */
    public void setParent(PlantNode parent){
        parent1 = parent;
        parent2 = parent;
    }
    
    /**
     * Overrides this method to ensure that both parents always refer to the same plant.
     * 
     * @param parent parent plant node
     */
    @Override
    public void setParent1(PlantNode parent){
        setParent(parent);
    }
    
    /**
     * Overrides this method to ensure that both parents always refer to the same plant.
     * 
     * @param parent parent plant node
     */
    @Override
    public void setParent2(PlantNode parent){
        setParent(parent);
    }

    /**
     * Create a deep copy of this selfing node node and its ancestor structure.
     * 
     * @param shiftGen if <code>true</code> all generations are shifted (+1)
     * @param curCopiedSeedLots currently already copied seed lot nodes
     * @param curCopiedPlants currently already copied plant nodes
     * @return deep copy of this selfing node and its ancestor structure, possibly with shifted generations (+1)
     * @throws CrossingSchemeException if anything goes wrong while copying this or related nodes
     */
    @Override
    public CrossingNode deepUpwardsCopy(boolean shiftGen, Map<String, SeedLotNode> curCopiedSeedLots,
                                                Map<String, PlantNode> curCopiedPlants)
                                                            throws CrossingSchemeException {
        // copy parent
        PlantNode parentCopy;
        // check if parent plant was already copied (in case of multiple crossings with same plant)
        if(curCopiedPlants.containsKey(getParent().getUniqueID())){
            // take present copy
            parentCopy = curCopiedPlants.get(getParent().getUniqueID());
        } else {
            // create new copy
            parentCopy = getParent().deepUpwardsCopy(shiftGen, curCopiedSeedLots, curCopiedPlants);
            curCopiedPlants.put(getParent().getUniqueID(), parentCopy);
        }
        // copy crossing node
        SelfingNode copy = new SelfingNode(getID(), getNumDuplicates(), parentCopy);
        return copy;
    }
}
