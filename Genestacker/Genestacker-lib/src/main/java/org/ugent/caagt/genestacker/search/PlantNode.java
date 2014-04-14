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
 * Represents a plant node in a crossing scheme. Each plant has one parent (seed lot node) denoting
 * the seed lot from which it was grown. Furthermore each plant node (except the final plant node)
 * has a number of children, representing the crossings of this plant with other plants from the
 * same generation (or itself, in case of a selfing).
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class PlantNode {

    // backpointer to crossing scheme
    private CrossingScheme scheme;
    
    // last ID assigned
    private static long lastID = 0;
    
    // ID
    private long ID;
    // sub ID used when regrowing the same plant (same ID) in a different generation
    private int subID;
    
    // number of duplicates
    private int numDuplicates;
    
    // plant
    private Plant plant;
    
    // parent seed lot
    private SeedLotNode parent;
    
    // crossings with other plants
    private List<CrossingNode> crossings;
    // selfings
    private List<SelfingNode> selfings;
    
    // generation in which this plant was grown
    private int generation;
    
    /**
     * Create new plant node. A new plant node ID is automatically generated,
     * and the sub ID is set to 0. The number of duplicates is set to 1, and
     * the plant node is automatically registered with its parent seed lot node.
     * 
     * @param plant plant corresponding to this plant node
     * @param generation generation in which the plant is grown
     * @param parent parental seed lot node
     */
    public PlantNode(Plant plant, int generation, SeedLotNode parent){
        this(plant, generation, parent, genNextID(), 0, 1);
    }
    
    /**
     * Create a new plant node with given ID, sub ID and number of duplicates.
     * This plant node is automatically registered with its parent seed lot node.
     * 
     * @param plant plant corresponding to this plant node
     * @param generation generation in which the plant is grown
     * @param parent parental seed lot node
     * @param ID given ID
     * @param subID given subID
     * @param numDuplicates number of required duplicates of this plant
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public PlantNode(Plant plant, int generation, SeedLotNode parent, long ID, int subID, int numDuplicates){
        this.plant = plant;
        this.generation = generation;
        this.parent = parent;
        this.ID = ID;
        this.subID = subID;
        this.numDuplicates = numDuplicates;
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
    
    /**
     * Link the crossing scheme in which this plant node occurs.
     * 
     * @param scheme crossing scheme in which this plant node occurs
     */
    public void setScheme(CrossingScheme scheme){
        this.scheme = scheme;
    }
    
    /**
     * Adds a crossing using the plant represented by this plant node.
     * This method should <b>only</b> be used for crossings with <b>distinct</b>
     * plants (for selfings, use {@link #addSelfing(SelfingNode)}).
     * 
     * @param crossing crossing with this plant
     */
    public void addCrossing(CrossingNode crossing){
        crossings.add(crossing);
    }
    
    public List<CrossingNode> getCrossings(){
        return crossings;
    }
    
    /**
     * Adds a selfing, i.e a crossing of the represented plant with itself.
     * 
     * @param selfing selfing of this plant
     */
    public void addSelfing(SelfingNode selfing){
        selfings.add(selfing);
    }
    
    public List<SelfingNode> getSelfings(){
        return selfings;
    }
    
    /**
     * Returns the number of times that this plant is used for crossings, taking into account
     * the number of times that each crossing is performed. Selfings are counted double because
     * then the plant acts both as mother and father.
     * 
     * @return number of crossings with this plant
     */
    public int getNumberOfTimesCrossed(){
        int totalCrossings = 0;
        // account for crossings
        for(CrossingNode c : crossings){
            totalCrossings += c.getNumDuplicates();
        }
        // account for selfings
        for(SelfingNode s : selfings){
            totalCrossings += 2*s.getNumDuplicates();
        }
        return totalCrossings;
    }
    
    /**
     * Get the probability with which the phase-known genotype of the represented plant is produced
     * when growing offspring using seeds from the parental seed lot.
     * 
     * @return probability of obtaining the desired phase-known genotype
     */
    public double getProbabilityOfPhaseKnownGenotype(){
        SeedLot sl = parent.getSeedLot();
        Genotype g = plant.getGenotype();
        return sl.getGenotypeGroup(g.getAllelicFrequencies()).getProbabilityOfPhaseKnownGenotype(g);
    }

    /**
     * Get the linkage phase ambiguity of the represented plant.
     * 
     * @return linkage phase ambiguity
     */
    public double getLinkagePhaseAmbiguity(){
        SeedLot sl = parent.getSeedLot();
        Genotype g = plant.getGenotype();
        return sl.getGenotypeGroup(g.getAllelicFrequencies()).getLinkagePhaseAmbiguity(g);
    }
    
    /**
     * Get the number of targets grown from nonuniform seed lot nodes in the crossing scheme in which this plant node occurs.
     * Note that this method should only be called if the plant node has been linked with its crossing schedule using
     * {@link #setScheme(CrossingScheme)}, else, a NullPointerException will be thrown.
     * 
     * @return number of targets grown from nonuniform seed lot nodes in the crossing scheme in which this plant node occurs
     */
    public int getNumTargetsFromNonUniformSeedLotsInScheme(){
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
    
    public int getSubID(){
        return subID;
    }
    
    /**
     * Get the number of required duplicates of this plant (to be able to perform all desired crossings).
     * 
     * @return number of duplicates
     */
    public int getNumDuplicates(){
        return numDuplicates;
    }
    
    /**
     * Increase the number of duplicates of this plant.
     */
    public void incNumDuplicates(){
        numDuplicates++;
    }
    
    /**
     * Set the number of required duplicates of this plant (to be able to perform all desired crossings).
     * 
     * @param dup number of duplicates
     */
    public void setNumDuplicates(int dup){
        numDuplicates = dup;
    }
    
    /**
     * Returns a unique ID, consisting of both the main ID and sub ID.
     * 
     * @return unique ID
     */
    public String getUniqueID(){
        return "p" + ID + "n" + subID;
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
        String s = getUniqueID();
        if(numDuplicates > 1){
            s += "(x" + numDuplicates + ")";
        }
        return s;
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
    
    /**
     * Creates a deep copy of this plant node and its ancestor structure.
     * 
     * @param shiftGen if <code>true</code>, all generations are increased by 1
     * @param curCopiedSeedLots currently already copied seed lot nodes
     * @param curCopiedPlants currently already copied plant nodes
     * @return deep copy, possibly with shifted generations (+1)
     * @throws CrossingSchemeException if anything goes wrong when copying this or related nodes
     */
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
    
    protected PlantNode createCopy(int gen, SeedLotNode parentCopy){
        return new PlantNode(plant, gen, parentCopy, ID, subID, numDuplicates);
    }
    
    /**
     * Create a deep copy of this plant node and its ancestor structure.
     * 
     * @return deep copy
     * @throws CrossingSchemeException if anything goes wrong when copying this or related nodes
     */
    public PlantNode deepUpwardsCopy() throws CrossingSchemeException{
        return deepUpwardsCopy(false, new HashMap<String, SeedLotNode>(), new HashMap<String, PlantNode>());
    }
    
    /**
     * Create a deep copy of this plant node and its ancestor structure, and shift the generation
     * of each node (+1) to create an empty 0th generation. This is used while merging
     * schedules as it makes place for an additional generation to be inserted at the top level.
     * 
     * @return deep copy with shifted generations (+1)
     * @throws CrossingSchemeException if anything goes wrong when copying this or related nodes
     */
    public PlantNode deepShiftedUpwardsCopy() throws CrossingSchemeException{
        return deepUpwardsCopy(true, new HashMap<String, SeedLotNode>(), new HashMap<String, PlantNode>());
    }
    
    /**
     * Check whether this is a dummy plant node. Always returns <code>false</code> here, but may be overridden
     * (for example, see {@link DummyPlantNode}).
     * 
     * @return <code>false</code>
     */
    public boolean isDummy(){
        return false;
    }
    
}
