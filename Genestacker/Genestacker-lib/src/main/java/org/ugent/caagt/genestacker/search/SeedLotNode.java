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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.ugent.caagt.genestacker.SeedLot;
import org.ugent.caagt.genestacker.exceptions.CrossingSchemeException;

/**
 * Represents a seedlot node in a crossing scheme.
 * 
 * Each seedlot has a number of crossings as parents (none in case of the initial
 * seedlots). A seedlot also has a list of children (plant nodes) representing
 * the plants that are grown from this seedlot in subsequent generations.
 * 
 * @author Herman De Beukelaer <herman.debeukelaer@ugent.be>
 */
public class SeedLotNode {
    
    // backpointer to crossing scheme
    private CrossingScheme scheme;
    
    // number of seeds taken from this seed lot (per generation)
    private Map<Integer, Long> seeds;
    
    // last ID assigned
    private static long lastID = 0;
    
    // ID
    private long ID;
    // duplication number: used to differ between distinct seedlot nodes that
    // contain the same seedlot (duplicated seedlots)
    private int duplication;
    
    // seedlot
    private SeedLot seedLot;
    
    // parent crossings
    private List<CrossingNode> parentCrossings;
    
    // generation in which seedlot is created
    private int generation;
    
    // children (plants) -- grouped per generation
    private Map<Integer, Set<PlantNode>> children;
    
    /**
     * Create an initial seed lot node, without parents.
     */
    public SeedLotNode(SeedLot seedLot, int generation){
        this(seedLot, generation, null);
    }
    
    /**
     * Create an initial seed lot node, without parents, with given ID and duplication number.
     */
    public SeedLotNode(SeedLot seedLot, int generation, long ID, int duplication){
        this(seedLot, generation, null, ID, duplication);
    }
    
    /**
     * Create a new seed lot node with given parents (at least one parent crossing should be specified).
     * A new unique ID is generated for this node. This seed lot node is automatically registered with
     * its parent crossing nodes.
     */
    public SeedLotNode(SeedLot seedLot, int generation, List<CrossingNode> parentCrossings) {
        this(seedLot, generation, parentCrossings, genNextID(), 0);
    }
    
    /**
     * Create a new seed lot node with given parents and specified ID and duplication number.
     * This seed lot node is automatically registered with its parent crossing nodes (if any).
     * 
     * @param seedLot
     * @param generation
     * @param parentCrossings
     * @param ID
     * @param duplication 
     */
    public SeedLotNode(SeedLot seedLot, int generation, List<CrossingNode> parentCrossings, long ID, int duplication){
        this.seeds = null;
        this.seedLot = seedLot;
        this.generation = generation;
        this.ID = ID;
        this.duplication = duplication;
        // set parents and children
        if(parentCrossings != null){
            this.parentCrossings = parentCrossings;
        } else {
            this.parentCrossings = new ArrayList<>();
        }
        children = new HashMap<>();
        // register with parent crossings
        for(CrossingNode c : this.parentCrossings){
            c.setChild(this);
        }
    }
        
    public static void resetIDs(){
        lastID = 0;
    }
    
    public synchronized static long genNextID(){
        return lastID++;
    }
    
    /**
     * Set the number of seeds taken from this seed lot per generation, after it has been computed.
     * Allows access to number of seeds without having to recompute numbers -- as long as they have not
     * changed. Upon computing new numbers, e.g. when extending a scheme, the seed lot should be updated
     * too using this method.
     */
    public void setSeedsTaken(Map<Integer, Long> seeds){
        this.seeds = seeds;
    }
    
    /**
     * Get the previously computed map containing the number of seeds
     * taken from the given seed lot in each generation.
     */
    public Map<Integer, Long> getSeedsTakenFromSeedLotPerGeneration() {
        return seeds;
    }
    
    /**
     * Get the previously computed total number of seeds taken from the given
     * seed lot, over all generations.
     */
    public long getSeedsTakenFromSeedLot() {
        // compute sum over generations
        long totSeeds = 0;
        for(int g : seeds.keySet()){
            totSeeds += seeds.get(g);
        }
        return totSeeds;
    }
    
    /**
     * Get the previously computed number of seeds taken from the given seed
     * lot in a specific generation.
     */
    public long getSeedsTakenFromSeedLotInGeneration(int generation) {
        // return number of seeds taken in generation
        return seeds.get(generation);
    }
    
    public CrossingScheme getScheme(){
        return scheme;
    }
    
    public void setScheme(CrossingScheme scheme){
        this.scheme = scheme;
    }
    
    public int getGeneration(){
        return generation;
    }
    
    public void setGeneration(int gen){
        generation = gen;
    }
    
    public void addChild(PlantNode child){
        Set<PlantNode> gen = children.get(child.getGeneration());
        if(gen == null){
            // create new generation
            gen = new HashSet<>();
            gen.add(child);
            children.put(child.getGeneration(), gen);
        } else {
            // add to existing generation
            gen.add(child);
        }
    }
    
    public void removeChild(PlantNode child){
        Set<PlantNode> gen = children.get(child.getGeneration());
        if(gen != null){
            gen.remove(child);
        }
    }
    
    public int nrOfChildren(){
        int numChildren = 0;
        for(Set<PlantNode> gen : children.values()){
            numChildren += gen.size();
        }
        return numChildren;
    }
    
    /**
     * Get the children (plant nodes) of this seed lot, grouped by generation.
     * 
     * @return 
     */
    public Map<Integer, Set<PlantNode>> getChildren(){
        return children;
    }

    public List<CrossingNode> getParentCrossings(){
        return parentCrossings;
    }
    
    public int nrOfParentCrossings(){
        return parentCrossings.size();
    }
    
    public void addParentCrossing(CrossingNode crossing){
        parentCrossings.add(crossing);
    }
    
    /**
     * Check whether this seed lot is an initial seed lot given as input
     * (in this case it has no parents).
     * 
     */
    public boolean isInitialSeedLot(){
        return nrOfParentCrossings() == 0;
    }
    
    public SeedLot getSeedLot(){
        return seedLot;
    }
    
    public long getID(){
        return ID;
    }
    
    /**
     * Assign the next available ID to this seed lot node
     */
    public void assignNextID(){
        ID = genNextID();
    }
    
    public void setID(long ID){
        this.ID = ID;
    }
    
    public int getDuplication(){
        return duplication;
    }
    
    public String getUniqueID(){
        return "s" + ID + "x" + duplication;
    }
    
    public boolean isUniform(){
        return seedLot.isUniform();
    }
        
    @Override
    public String toString(){
        return getUniqueID();
    }
    
    /**
     * Create a deep copy of this seed lot node node and its ancestor structure.
     * 
     * @param curCopiedSeedLots 
     * @param curCopiedPlants 
     * @throws CrossingSchemeException  
     */
    public SeedLotNode deepUpwardsCopy(boolean shiftGen, Map<String, SeedLotNode> curCopiedSeedLots,
                                                Map<String, PlantNode> curCopiedPlants)
                                                                    throws CrossingSchemeException{
        // copy parents (crossings)
        List<CrossingNode> parentsCopy = new ArrayList<>();
        for(int i=0; i<parentCrossings.size(); i++){
            CrossingNode parent = parentCrossings.get(i);
            CrossingNode parentCopy = parent.deepUpwardsCopy(shiftGen, curCopiedSeedLots, curCopiedPlants);
            parentsCopy.add(parentCopy);
        }
        // copy seedlot node
        int gen = generation;
        if(shiftGen){
            gen++;
        }
        SeedLotNode copy = new SeedLotNode(seedLot, gen, parentsCopy, ID, duplication);
        return copy;
    }
    
    @Override
    public boolean equals(Object o){
        boolean equal = false;
        if(o instanceof SeedLotNode){
            SeedLotNode osln = (SeedLotNode) o;
            equal = getUniqueID().equals(osln.getUniqueID());
        }
        return equal;
    }

    @Override
    public int hashCode() {
        return getUniqueID().hashCode();
    }
    
}
