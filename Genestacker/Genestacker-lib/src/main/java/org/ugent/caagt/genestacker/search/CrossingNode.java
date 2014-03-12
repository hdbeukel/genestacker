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
 * Represents a crossing of two plants in a crossing scheme.
 * 
 * Each crossing has two parents (plant nodes), possibly identical in case of
 * selfing. A crossing also has one single child, representing the seedlot
 * that contains the seeds resulting from this crossing.
 * 
 * @author Herman De Beukelaer <herman.debeukelaer@ugent.be>
 */
public class CrossingNode {
    
    // backpointer to crossing scheme in which node occurs
    private CrossingScheme scheme;
    
    // last ID assigned
    private static long lastID = 0;
    
    // ID
    protected long ID;
    
    // generation
    protected int gen;
    
    // parent 1 (plant)
    protected PlantNode parent1;
    
    // parent 2 (plant)
    protected PlantNode parent2;
    
    // child (seedlot)
    protected SeedLotNode child;
    
    /**
     * Create a new crossing node with automatically generated ID. This crossing is automatically registered
     * with its parent plant nodes.
     * 
     * @param parent1
     * @param parent2
     * @throws ImpossibleCrossingException  
     */
    public CrossingNode(PlantNode parent1, PlantNode parent2) throws ImpossibleCrossingException{
        this(genNextID(), parent1, parent2);
    }
    
    /**
     * Create a new crossing node with given ID. This crossing is automatically registered
     * with its parent plant nodes.
     * 
     * @param ID
     * @param parent1
     * @param parent2
     * @throws ImpossibleCrossingException  
     */
    public CrossingNode(long ID, PlantNode parent1, PlantNode parent2) throws ImpossibleCrossingException{
        // check if parents from same generation
        if(parent1.getGeneration() == parent2.getGeneration()){
            this.gen = parent1.getGeneration();
            this.ID = ID;
            this.parent1 = parent1;
            this.parent2 = parent2;
            child = null;
            // register crossing with parent plant nodes
            registerWithParents();            
        } else {
            // impossible crossing!
            throw new ImpossibleCrossingException("Impossible crossing: both plants need to be from the same generation");
        }
    }
    
    /**
     * Register the crossing with its parents.
     */
    protected void registerWithParents(){
        parent1.addCrossing(this);
        parent2.addCrossing(this);
    }
    
    public static void resetIDs(){
        lastID = 0;
    }
    
    public synchronized static long genNextID(){
        return lastID++;
    }
    
    public CrossingScheme getScheme(){
        return scheme;
    }
    
    public void setScheme(CrossingScheme scheme){
        this.scheme = scheme;
    }
    
    public String getUniqueID(){
        return "c" + ID;
    }
    
    public int getGeneration(){
        return gen;
    }
    
    public void setGeneration(int gen){
        this.gen = gen;
    }
    
    public PlantNode getParent1() {
        return parent1;
    }

    public void setParent1(PlantNode parent1) {
        this.parent1 = parent1;
    }

    public PlantNode getParent2() {
        return parent2;
    }

    public void setParent2(PlantNode parent2) {
        this.parent2 = parent2;
    }

    public SeedLotNode getChild() {
        return child;
    }

    public void setChild(SeedLotNode child) {
        this.child = child;
    }
    
    public boolean isSelfing(){
        return false;
    }
    
    /**
     * Create a deep copy of this crossing node node and its ancestor structure.
     * 
     * @param curCopiedSeedLots 
     * @param curCopiedPlants 
     * @throws CrossingSchemeException  
     */
    public CrossingNode deepUpwardsCopy(boolean shiftGen, Map<String, SeedLotNode> curCopiedSeedLots,
                                                Map<String, PlantNode> curCopiedPlants)
                                                        throws CrossingSchemeException {
        // copy parent plants
        PlantNode parent1Copy, parent2Copy;
        // check if first parent plant was already copied (in case of multiple crossings with same plant)
        if(curCopiedPlants.containsKey(parent1.getUniqueID())){
            // take present copy
            parent1Copy = curCopiedPlants.get(parent1.getUniqueID());
        } else {
            // create new copy
            parent1Copy = parent1.deepUpwardsCopy(shiftGen, curCopiedSeedLots, curCopiedPlants);
            curCopiedPlants.put(parent1.getUniqueID(), parent1Copy);
        }
        // repeat copying second parent
        if(curCopiedPlants.containsKey(parent2.getUniqueID())){
            // take present copy
            parent2Copy = curCopiedPlants.get(parent2.getUniqueID());
        } else {
            // create new copy
            parent2Copy = parent2.deepUpwardsCopy(shiftGen, curCopiedSeedLots, curCopiedPlants);
            curCopiedPlants.put(parent2.getUniqueID(), parent2Copy);
        }
        // copy crossing node
        CrossingNode copy = new CrossingNode(ID, parent1Copy, parent2Copy);
        return copy;
    }
    
}
