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
import java.util.List;
import java.util.Map;
import org.ugent.caagt.genestacker.Genotype;
import org.ugent.caagt.genestacker.Plant;
import org.ugent.caagt.genestacker.SeedLot;
import org.ugent.caagt.genestacker.exceptions.CrossingSchemeException;

/**
 * Represents a plant node in a crossing scheme.
 * 
 * Each plant has one parent (seedlot node) denoting the seedlot from which
 * it was grown. Furthermore each plant node (except the final node) has a number of children,
 * representing the crossings of this plant with other plants (or itself, in case of selfing)
 * from the same generation. Crossings are grouped in selfings and crossings with other plants.
 * 
 * @author Herman De Beukelaer <herman.debeukelaer@ugent.be>
 */
public class PlantNode {

    // backpointer to crossing scheme
    private CrossingScheme scheme;
    
    // last ID assigned
    private static long lastID = 0;
    
    // ID
    private long ID;
    
    // duplication number
    private int duplication;
    
    // plant
    private Plant plant;
    
    // parent seedlot
    private SeedLotNode parent;
    
    // crossings with other plants (single children)
    private List<CrossingNode> crossings;
    // selfings (double children)
    private List<SelfingNode> selfings;
    
    // generation in which this plant was grown
    private int generation;
    
    /**
     * Create new plant node. A new plant node ID is automatically generated.
     * The plant node is automatically registered with its parent seed lot node.
     */
    public PlantNode(Plant plant, int generation, SeedLotNode parent){
        this(plant, generation, parent, genNextID(), 0);
    }
    
    /**
     * Create a new plant node with given ID. This plant node is automatically
     * registered with its parent seed lot node.
     */
    public PlantNode(Plant plant, int generation, SeedLotNode parent, long ID, int duplication){
        this.plant = plant;
        this.generation = generation;
        this.parent = parent;
        this.ID = ID;
        this.duplication = duplication;
        crossings = new ArrayList<>();
        selfings = new ArrayList<>();
        // register with parent seed lot node (if no dangling plant node)
        if(parent != null){
            parent.addChild(this);
        }
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
    
    /**
     * Use this method only for crossings of distinct plants (for selfings,
     * see method addSelfing)
     * 
     * @param crossing 
     */
    public void addCrossing(CrossingNode crossing){
        crossings.add(crossing);
    }
    
    public List<CrossingNode> getCrossings(){
        return crossings;
    }
    
    public void addSelfing(SelfingNode selfing){
        selfings.add(selfing);
    }
    
    public List<SelfingNode> getSelfings(){
        return selfings;
    }
    
    /**
     * Returns the number of times that this plant is used for crossings.
     * Selfings are counted twice because here the plant is used as both
     * mother and father in the crossing.
     * 
     */
    public int getNumberOfTimesCrossed(){
        return 2 * selfings.size() + crossings.size();
    }
    
    /**
     * Get the probability of observing the desired phase-known genotype.
     */
    public double getProbabilityOfPhaseKnownGenotype(){
        SeedLot sl = parent.getSeedLot();
        Genotype g = plant.getGenotype();
        return sl.getGenotypeGroup(g.getObservableState()).getProbabilityOfPhaseKnownGenotype(g);
    }

    /**
     * Get the linkage phase ambiguity of this plant.
     */
    public double getLinkagePhaseAmbiguity(){
        SeedLot sl = parent.getSeedLot();
        Genotype g = plant.getGenotype();
        return sl.getGenotypeGroup(g.getObservableState()).getLinkagePhaseAmbiguity(g);
    }
    
    /**
     * Get the number of targets grown from non-uniform seed lot nodes in the crossing scheme in which this plant node occurs.
     */
    public int getNumTargetsFromNonUniformSeedLotsInScheme(){
        // number of plants grown from non uniform seed lots
        return scheme.getNumTargetsFromNonUniformSeedLots();
    }
    
    public boolean isDanglingPlantNode(){
        return parent == null;
    }
    
    public SeedLotNode getParent(){
        return parent;
    }
    
    public void setParent(SeedLotNode sfn){
        parent = sfn;
    }
    
    public int getGeneration(){
        return generation;
    }
    
    public void setGeneration(int gen){
        generation = gen;
    }
    
    public Plant getPlant(){
        return plant;
    }
    
    public void setPlant(Plant plant){
        this.plant = plant;
    }
    
    public long getID(){
        return ID;
    }
    
    public String getUniqueID(){
        return "p" + ID + "x" + duplication;
    }
    
    public boolean grownFromUniformLot(){
        if(isDanglingPlantNode()){
            // dangling plant node
            return false;
        } else {
            // check parent seed lot
            return parent.isUniform();
        }
    }
    
    @Override
    public String toString(){
        return getUniqueID();
    }
    
    @Override
    public boolean equals(Object o){
        boolean equal = false;
        if(o instanceof PlantNode){
            PlantNode opn = (PlantNode) o;
            equal = getUniqueID().equals(opn.getUniqueID());
        }
        return equal;
    }

    @Override
    public int hashCode() {
        return getUniqueID().hashCode();
    }
    
    public PlantNode deepUpwardsCopy(boolean shiftGen, Map<String, SeedLotNode> curCopiedSeedLots,
                                                Map<String, PlantNode> curCopiedPlants)
                                                                throws CrossingSchemeException{
        SeedLotNode parentCopy = null;
        if(parent != null){
            // check if parent seedlot was already copied (in case of multiple plants grown
            // from same seedlot)
            if(curCopiedSeedLots.containsKey(parent.getUniqueID())){
                // take present copy
                parentCopy = curCopiedSeedLots.get(parent.getUniqueID());
            } else {
                // create new copy
                parentCopy = parent.deepUpwardsCopy(shiftGen, curCopiedSeedLots, curCopiedPlants);
                curCopiedSeedLots.put(parent.getUniqueID(), parentCopy);
            }
        }
        int gen = generation;
        if(shiftGen){
            gen++;
        }
        PlantNode copy = createCopy(gen, parentCopy);
        return copy;
    }
    
    /**
    * Create a deep copy of this plant node and its ancestor structure.
    * 
    * @throws CrossingSchemeException  
    */
    public PlantNode deepUpwardsCopy() throws CrossingSchemeException{
        return deepUpwardsCopy(false, new HashMap<String, SeedLotNode>(), new HashMap<String, PlantNode>());
    }
    
    /**
    * Create a deep copy of this plant node and its ancestor structure, and shift the generation
    * of each node (gen -> gen+1) to create an empty 0th generation.
    * 
    * @throws CrossingSchemeException  
    */
    public PlantNode deepShiftedUpwardsCopy() throws CrossingSchemeException{
        return deepUpwardsCopy(true, new HashMap<String, SeedLotNode>(), new HashMap<String, PlantNode>());
    }
    
    public PlantNode createCopy(int gen, SeedLotNode parentCopy){
        return new PlantNode(plant, gen, parentCopy, ID, duplication);
    }
    
    /**
     * Check whether this is a dummy plant node. Returns false by default.
     */
    public boolean isDummy(){
        return false;
    }
    
}
