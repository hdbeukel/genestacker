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
 * Represents a seed lot node in a crossing scheme. Each seed lot has a number of crossings as parents
 * (none in case of the initial seed lots). A seed lot also has a list of children (plant nodes)
 * representing the plants that are grown from this seed lot in subsequent generations.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
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
    // sub ID: used for duplicated seed lot nodes over different generations
    private int subID;
    
    // seed lot
    private SeedLot seedLot;
    
    // parent crossings
    private List<CrossingNode> parentCrossings;
    
    // generation in which seed lot is created
    private int generation;
    
    // children (plant nodes) -- grouped per generation
    private Map<Integer, Set<PlantNode>> children;
    
    /**
     * Create an initial seed lot node, without parents.
     * 
     * @param seedLot contained seed lot
     * @param generation generation in which seed lot is obtained
     */
    public SeedLotNode(SeedLot seedLot, int generation){
        this(seedLot, generation, null);
    }
    
    /**
     * Create an initial seed lot node, without parents, with given ID and sub ID.
     * 
     * @param seedLot contained seed lot
     * @param generation generation in which seed lot is obtained
     * @param ID given ID
     * @param subID given sub ID
     */
    public SeedLotNode(SeedLot seedLot, int generation, long ID, int subID){
        this(seedLot, generation, null, ID, subID);
    }
    
    /**
     * Create a new seed lot node with given parents. A new unique ID is generated for this node,
     * and the sub ID is set to 0. This seed lot node is automatically registered with its parent
     * crossing nodes (if any).
     * 
     * @param seedLot contained seed lot
     * @param generation generation in which seed lot is obtained
     * @param parentCrossings crossings providing seeds for the contained seed lot node
     */
    public SeedLotNode(SeedLot seedLot, int generation, List<CrossingNode> parentCrossings) {
        this(seedLot, generation, parentCrossings, genNextID(), 0);
    }
    
    /**
     * Create a new seed lot node with given parents, ID and sub ID. This seed lot node is automatically
     * registered with its parent crossing nodes (if any).
     * 
     * @param seedLot contained seed lot
     * @param generation generation in which seed lot is obtained
     * @param parentCrossings crossings providing seeds for the contained seed lot
     * @param ID given ID
     * @param subID given sub ID
     */
    public SeedLotNode(SeedLot seedLot, int generation, List<CrossingNode> parentCrossings, long ID, int subID){
        this.seeds = null;
        this.seedLot = seedLot;
        this.generation = generation;
        this.ID = ID;
        this.subID = subID;
        // set parents
        if(parentCrossings != null){
            this.parentCrossings = parentCrossings;
        } else {
            this.parentCrossings = new ArrayList<>();
        }
        // set children
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
     * Allows access to seed counts without having to recompute them -- as long as they have not
     * changed. When computing new seed counts, e.g. when extending a scheme, the seed lot should be updated
     * too using this method.
     * 
     * @param seeds steeds taken from this seed lot in every generation
     */
    public void setSeedsTaken(Map<Integer, Long> seeds){
        this.seeds = seeds;
    }
    
    /**
     * Get the previously computed map containing the number of seeds
     * taken from this seed lot in each generation. Should only be called
     * after calling {@link #setSeedsTaken(Map)}.
     * 
     * @return seeds taken from this seed lot, per generation
     */
    public Map<Integer, Long> getSeedsTakenFromSeedLotPerGeneration() {
        return seeds;
    }
    
    /**
     * Get the previously computed total number of seeds taken from this
     * seed lot, over all generations. Should only be called after calling
     * {@link #setSeedsTaken(Map)}.
     * 
     * @return total number of seeds taken from this seed lot across all generations
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
     * Get the previously computed number of seeds taken from this seed
     * lot in a specific generation. Should only be called after calling
     * {@link #setSeedsTaken(Map)}.
     * 
     * @param generation given generation
     * @return number of seeds taken from this seed lot in the given generation
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
     * @return plants nodes representing plants grown from this seed lot, grouped per generation
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
     * Check whether this seed lot is an initial seed lot without any parental crossings
     * (in case it is part of the input).
     * 
     * @return <code>true</code> if this seed lot does not have any parental crossings
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
     * Assign the next available ID to this seed lot node.
     */
    public void assignNextID(){
        ID = genNextID();
    }
    
    public void setID(long ID){
        this.ID = ID;
    }
    
    public int getSubID(){
        return subID;
    }
    
    /**
     * Returns a unique (string) ID containing both the main and sub ID of this seed lot node.
     * 
     * @return unique ID
     */
    public String getUniqueID(){
        return "s" + ID + "x" + subID;
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
     * @param shiftGen if <code>true</code> all generations are shifted (+1)
     * @param curCopiedSeedLots currently already copied seed lot nodes
     * @param curCopiedPlants currently already copied plant nodes
     * @return deep copy of this seed lot node and its ancestor structure, possibly with shifted generations (+1)
     * @throws CrossingSchemeException if anything goes wrong when copying this or related nodes
     */
    public SeedLotNode deepUpwardsCopy(boolean shiftGen, Map<String, SeedLotNode> curCopiedSeedLots,
                                                Map<String, PlantNode> curCopiedPlants)
                                                                    throws CrossingSchemeException{
        // copy parents (crossings)
        List<CrossingNode> parentsCopy = new ArrayList<>();
        for (CrossingNode parent : parentCrossings) {
            CrossingNode parentCopy = parent.deepUpwardsCopy(shiftGen, curCopiedSeedLots, curCopiedPlants);
            parentsCopy.add(parentCopy);
        }
        // copy seedlot node
        int gen = generation;
        if(shiftGen){
            gen++;
        }
        SeedLotNode copy = new SeedLotNode(seedLot, gen, parentsCopy, ID, subID);
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
