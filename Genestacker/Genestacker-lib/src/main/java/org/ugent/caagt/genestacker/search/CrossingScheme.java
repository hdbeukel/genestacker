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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.ugent.caagt.genestacker.Plant;
import org.ugent.caagt.genestacker.exceptions.CrossingSchemeException;
import org.ugent.caagt.genestacker.search.bb.BranchAndBoundSolutionManager;

/**
 * Represents a crossing scheme. The entire scheme can be reconstructed by using
 * backpointers starting from the final plant node.
 * 
 * @author Herman De Beukelaer <herman.debeukelaer@ugent.be>
 */
public class CrossingScheme {
    
    // population size tools: used to compute population sizes
    private PopulationSizeTools popSizeTools;
    
    // linkage phase ambiguity = 
    // risk of selecting at least one genotype with undesired linkage phase
    private double linkagePhaseAmbiguity;
    
    // number of generations
    private int numGenerations;
    
    // number of plant nodes in scheme that were grown from a non-uniform seedlot
    private int numTargetsFromNonUniformSeedLots;
    
    // total number of plants
    private long totalPopulationSize;
    
    // population size per generation
    private long[] popSizePerGeneration;
    
    // index for quick access of seedlots grouped by generation
    private List<List<SeedLotNode>> seedLotsPerGeneration;
    
    // index for quick access of seedlots grouped per ID
    private Map<Long, List<SeedLotNode>> seedLotsPerID;
    
    // index for quick access to seedlot nodes with a specific unique ID
    private Map<String, SeedLotNode> seedLotIndex;
    
    // index for quick access of plants grouped by generation
    private List<List<PlantNode>> plantsPerGeneration;
    
    // index for quick access of plants grouped per ID
    private Map<Long, List<PlantNode>> plantsPerID;
    
    // index for quick access to plant nodes with a specific unique ID
    private Map<String, PlantNode> plantIndex;
    
    // index for quick access to crossing nodes grouped by generation
    private List<List<CrossingNode>> crossingsPerGeneration;
    
    // index for quick access to crossing nodes with a specific unique ID
    private Map<String, CrossingNode> crossingIndex;
    
    // final plant node
    private PlantNode finalPlantNode;
        
    /**
     * Create a new crossing scheme with given final plant node, from which the
     * entire scheme can be reconstructed by following back-pointers. The desired
     * success rate is stated.
     */
    public CrossingScheme(PopulationSizeTools popSizeTools, PlantNode finalPlantNode){
        this.popSizeTools = popSizeTools;
        this.numGenerations = finalPlantNode.getGeneration();
        this.finalPlantNode = finalPlantNode;
        seedLotIndex = new HashMap<>();
        seedLotsPerID = new HashMap<>();
        plantIndex = new HashMap<>();
        plantsPerID = new HashMap<>();
        crossingIndex = new HashMap<>();
        seedLotsPerGeneration = new ArrayList<>();
        plantsPerGeneration = new ArrayList<>();
        crossingsPerGeneration = new ArrayList<>();
        for(int g=0; g<=numGenerations; g++){
            seedLotsPerGeneration.add(new ArrayList<SeedLotNode>());
            plantsPerGeneration.add(new ArrayList<PlantNode>());
            crossingsPerGeneration.add(new ArrayList<CrossingNode>());
        }
        reinitScheme();
    }
    
    /**
     * Compute the required seeds at each seed lot node, given the overall success
     * probability. If the crossing scheme contains n plant nodes grown from 
     * genetically non-uniform seedlots, the probability of success for each
     * plant is set to be the n-th root of successProbability.
     * 
     * Also computes the linkage phase ambiguity of the scheme and creates indices for quick access
     * to specific seed lots and plant nodes.
     */
    final public void reinitScheme(){        
        seedLotIndex.clear();
        seedLotsPerID.clear();
        plantIndex.clear();
        plantsPerID.clear();
        crossingIndex.clear();
        for(int g=0; g<=numGenerations; g++){
            seedLotsPerGeneration.get(g).clear();
            plantsPerGeneration.get(g).clear();
            crossingsPerGeneration.get(g).clear();
        }
        linkagePhaseAmbiguity = 0.0;
        numTargetsFromNonUniformSeedLots = 0;
        
        // go through all generations (backwards)
        
        List<LinkedList<PlantNode>> plantQueues = new ArrayList<>();
        List<LinkedList<SeedLotNode>> seedLotQueues = new ArrayList<>();
        for(int i=0; i<numGenerations+1; i++){
            plantQueues.add(new LinkedList<PlantNode>());
            seedLotQueues.add(new LinkedList<SeedLotNode>());
        }
        
        Map<String, Integer> slChildCounter = new HashMap<>();
        
        plantQueues.get(numGenerations).add(finalPlantNode);
        indexPlantNode(finalPlantNode);
        
        for(int gen=numGenerations; gen >= 0; gen--){
            // process all plants of this generation
            while(!plantQueues.get(gen).isEmpty()){
                PlantNode plant = plantQueues.get(gen).poll();                
                if(!plant.isDanglingPlantNode()){
                    // update nr of non uniform plant nodes
                    if(!plant.grownFromUniformLot()){
                        numTargetsFromNonUniformSeedLots++;
                    }
                    // update LPA
                    linkagePhaseAmbiguity = 1 - ((1-linkagePhaseAmbiguity)*(1-plant.getLinkagePhaseAmbiguity()));
                    // get parent seed lot
                    SeedLotNode sl = plant.getParent();
                    // update seedlot child counter
                    if(slChildCounter.containsKey(sl.getUniqueID())){
                        slChildCounter.put(sl.getUniqueID(), slChildCounter.get(sl.getUniqueID())+1);
                    } else {
                        slChildCounter.put(sl.getUniqueID(), 1);
                    }
                    // update seedlot index and put seedlot in queue, only if
                    // all children of the seedlot have already been processed
                    // (assures that seedlots with multiple children are only
                    // considered once in the recursion)
                    if(slChildCounter.get(sl.getUniqueID()) == sl.nrOfChildren()){
                        // update seedlot index
                        indexSeedLotNode(sl);
                        // put seedlot in the queue
                        if(!sl.isInitialSeedLot()){
                            seedLotQueues.get(sl.getGeneration()).addLast(sl);
                        }
                    }                    
                }
            }
            // process all seedlots of this generation
            Set<String> consideredPlantNodes = new HashSet<>();
            while(!seedLotQueues.get(gen).isEmpty()){
                SeedLotNode sln = seedLotQueues.get(gen).poll();
                // for each crossing, add parent plants to queue of previous generation
                List<CrossingNode> crossings = sln.getParentCrossings();
                for(int i=0; i<crossings.size(); i++){
                    CrossingNode c = crossings.get(i);
                    // index crossing node
                    indexCrossingNode(c);
                    // process parent plants
                    PlantNode parent1 = c.getParent1();
                    PlantNode parent2 = c.getParent2();
                    // index parents and add to queue if not already added
                    if(!consideredPlantNodes.contains(parent1.getUniqueID())){
                        plantQueues.get(gen-1).addLast(parent1);
                        indexPlantNode(parent1);
                        consideredPlantNodes.add(parent1.getUniqueID());
                    }
                    if(!consideredPlantNodes.contains(parent2.getUniqueID())){
                        plantQueues.get(gen-1).addLast(parent2);
                        indexPlantNode(parent2);
                        consideredPlantNodes.add(parent2.getUniqueID());
                    }                    
                }
            }
        }
        
        // compute and index population sizes for all seed lots
        
        totalPopulationSize = 0;
        popSizePerGeneration = new long[numGenerations+1];
        
        // go through seed lots
        for(SeedLotNode sln : getSeedLotNodes()){
            // compute required seeds from seed lot (per generation)
            Map<Integer, Long> numSeeds = popSizeTools.computeSeedsTakenFromSeedLotPerGeneration(sln);
            // update seed lot with computed amount of seeds
            sln.setSeedsTaken(numSeeds);
            // update pop size sums
            for(int g : numSeeds.keySet()){
                totalPopulationSize += numSeeds.get(g);
                popSizePerGeneration[g] += numSeeds.get(g);
            }
        }
    }
    
    private void indexSeedLotNode(SeedLotNode sln){
        // put seed lot in seed lot index
        seedLotIndex.put(sln.getUniqueID(), sln);
        // register seed lot in its generation
        seedLotsPerGeneration.get(sln.getGeneration()).add(sln);
        // register seed lot at its ID
        if(seedLotsPerID.containsKey(sln.getID())){
            seedLotsPerID.get(sln.getID()).add(sln);
        } else {
            List<SeedLotNode> list = new ArrayList<>();
            list.add(sln);
            seedLotsPerID.put(sln.getID(), list);
        }
        // register crossing scheme backpointer
        sln.setScheme(this);
    }
    
    private void indexPlantNode(PlantNode pn){
        // put plant node in plant index
        plantIndex.put(pn.getUniqueID(), pn);
        // register plant node in its generation
        plantsPerGeneration.get(pn.getGeneration()).add(pn);
        // register plant node at its ID
        if(plantsPerID.containsKey(pn.getID())){
            plantsPerID.get(pn.getID()).add(pn);
        } else {
            List<PlantNode> list = new ArrayList<>();
            list.add(pn);
            plantsPerID.put(pn.getID(), list);
        }
        // register crossing scheme backpointer
        pn.setScheme(this);
    }
    
    private void indexCrossingNode(CrossingNode c){
        // put crossing node in node index
        crossingIndex.put(c.getUniqueID(), c);
        // register crossing node in its generation
        crossingsPerGeneration.get(c.getGeneration()).add(c);
        // register crossing scheme backpointer
        c.setScheme(this);
    }
    
    public PopulationSizeTools getPopulationSizeTools(){
        return popSizeTools;
    }
    
    /**
     * Get the total population size in the entire scheme.
     */
    public long getTotalPopulationSize(){
        return totalPopulationSize;
    }
    
    public long getPopSizeOfGeneration(int gen){
        return popSizePerGeneration[gen];
    }
    
    public long[] getPopSizePerGeneration(){
        return popSizePerGeneration;
    }
    
    /**
     * Get the maximum population size in one generation across the scheme.
     * 
     */
    public long getMaxPopulationSizePerGeneration(){
        long max = popSizePerGeneration[0];
        for(int i=1; i<popSizePerGeneration.length; i++){
            if(popSizePerGeneration[i] > max){
                max = popSizePerGeneration[i];
            }
        }
        return max;
    }
    
    /**
     * Get the maximum number of times that a specific plant is used for crossings.
     * Selfings are counted twice because here the same plant is used as both
     * mother and father in the crossing.
     * 
     */
    public int getMaxCrossingsWithPlant(){
        int max = 0;
        for(String id : plantIndex.keySet()){
            PlantNode pn = plantIndex.get(id);
            int numTimesCrossed = pn.getNumberOfTimesCrossed();
            if(numTimesCrossed > max){
                max = numTimesCrossed;
            }
        }
        return max;
    }
    
    /**
     * Get the total number of crossings.
     */
    public int getNumCrossings(){
        return crossingIndex.size();
    }
    
    public PlantNode getFinalPlantNode(){
        return finalPlantNode;
    }
    
    public int getNumGenerations(){
        return numGenerations;
    }
    
    public int getNumTargetsFromNonUniformSeedLots(){
        return numTargetsFromNonUniformSeedLots;
    }
    
    public double getLinkagePhaseAmbiguity(){
        return linkagePhaseAmbiguity;
    }
    
    public int getNumNodes(){
        return plantIndex.size() + seedLotIndex.size() + crossingIndex.size();
    }
    
    public SeedLotNode getSeedLotNodeWithUniqueID(String ID){
        return seedLotIndex.get(ID);
    }
    
    public Collection<SeedLotNode> getSeedLotNodes(){
        return seedLotIndex.values();
    }
    
    public List<SeedLotNode> getSeedLotNodesFromGeneration(int generation){
        return seedLotsPerGeneration.get(generation);
    }
    
    public List<SeedLotNode> getSeedLotNodesWithID(long ID){
        return seedLotsPerID.get(ID);
    }
    
    /**
     * Get the UNIQUE seed lot node with given ID in a given generation, or null
     * in case no such seed lot is present.
     * 
     * @param generation
     * @param ID
     */
    public SeedLotNode getSeedLotNodeFromGenerationWithID(int generation, long ID){
        List<SeedLotNode> seedLots = seedLotsPerGeneration.get(generation);
        boolean found = false;
        int i=0;
        while(!found && seedLots != null && i<seedLots.size()){
            found = (seedLots.get(i).getID() == ID);
            i++;
        }
        if(found){
            return seedLots.get(i-1);
        } else {
            return null;
        }
    }
    
    public boolean containsSeedLotNodesWithID(long ID){
        return seedLotsPerID.containsKey(ID);
    }
    
    public int getNumSeedLotNodesWithID(long ID){
        if(!containsSeedLotNodesWithID(ID)){
            return 0;
        } else {
            return getSeedLotNodesWithID(ID).size();
        }
    }

    public PlantNode getPlantNodeWithUniqueID(String ID){
        return plantIndex.get(ID);
    }
    
    public Collection<PlantNode> getPlantNodes(){
        return plantIndex.values();
    }
    
    /**
     * Get the set IDs of all initial seed lots that are used in this scheme.
     */
    public Set<Long> getInitialSeedLotNodeIDs(){
        Set<Long> ids = new HashSet<>();
        for(SeedLotNode sln : seedLotsPerGeneration.get(0)){
            ids.add(sln.getID());
        }
        return ids;
    }
    
    public List<PlantNode> getPlantNodesFromGeneration(int generation){
        return plantsPerGeneration.get(generation);
    }
    
    public List<PlantNode> getPlantNodesWithID(long ID){
        return plantsPerID.get(ID);
    }
    
    public List<PlantNode> getPlantNodesFromGenerationWithID(int generation, long ID){
        List<PlantNode> plants = new ArrayList<>();
        for(PlantNode p : plantsPerGeneration.get(generation)){
            if(p.getID() == ID){
                plants.add(p);
            }
        }
        return plants;
    }
    
    public boolean containsPlantNodesWithID(long ID){
        return plantsPerID.containsKey(ID);
    }
    
    public int getNumPlantNodesWithID(long ID){
        if(!containsPlantNodesWithID(ID)){
            return 0;
        } else {
            return getPlantNodesWithID(ID).size();
        }
    }
    
    public Collection<CrossingNode> getCrossingNodes(){
        return crossingIndex.values();
    }
    
    public List<CrossingNode> getCrossingNodesFromGeneration(int generation){
        return crossingsPerGeneration.get(generation);
    }
    
    public CrossingSchemeDescriptor getDescriptor(){
        return new CrossingSchemeDescriptor(
                numGenerations,
                getNumCrossings(),
                getMaxCrossingsWithPlant(),
                getMaxPopulationSizePerGeneration(),
                totalPopulationSize,
                linkagePhaseAmbiguity,
                numTargetsFromNonUniformSeedLots
        );
    }
    
    /**
     * Checks for any depleted seed lots in this scheme and, if any, resolves
     * them by creating new parent crossings for the depleted lots. Returns true
     * if all depleted seed lots have been refilled and the extended scheme still
     * satisfies all constraints and is not yet dominated by an existing solution.
     * If seed lots can not be refilled without violating constraints or if the
     * extended scheme is dominated by an existing solution, false is returned.
     * 
     * @param solManager 
     * @throws CrossingSchemeException  
    */
    public boolean resolveDepletedSeedLots(BranchAndBoundSolutionManager solManager) throws CrossingSchemeException{
        // get list of depleted seed lots, depending on the constraints that
        // are given in the Pareto Frontier
        List<SeedLotNode> depleted = solManager.getDepletedSeedLots(this);
        boolean bounded = false;
        while(!bounded && depleted != null && !depleted.isEmpty()){
            // iteratively fix all depleted seed lots
            int i=0;
            while(!bounded && i < depleted.size()){
                SeedLotNode depletedLot = depleted.get(i);
                // get one of the parent crossings of the seed lot (arbitrary,
                // as they are all duplicates of the same crossing anyway)
                CrossingNode crossing = depletedLot.getParentCrossings().get(0);
                if(crossing.isSelfing()){
                    // selfing
                    SelfingNode selfing = (SelfingNode) crossing;
                    PlantNode parentPlant = selfing.getParent();
                    // get a plant with required ID from the corresponding generation
                    // which is still reusable for an additional selfing
                    PlantNode newParentPlant = solManager.getReusablePlantNode(parentPlant.getID(), parentPlant.getGeneration(), this, true);
                    if(newParentPlant == null){
                        // no reusable parent found: grow new parent from corresponding seed lot
                        long ID = parentPlant.getID();
                        int duplication = getNumPlantNodesWithID(ID);
                        newParentPlant = new PlantNode(parentPlant.getPlant(), parentPlant.getGeneration(), parentPlant.getParent(), ID, duplication);
                    }
                    // create new selfing, using new parent
                    SelfingNode newSelfing = new SelfingNode(newParentPlant);
                    // add the new selfing as incoming crossing for the depleted seed lot
                    depletedLot.addParentCrossing(newSelfing);
                    newSelfing.setChild(depletedLot);
                } else {
                    // crossing is not a selfing
                    PlantNode parentPlant1 = crossing.getParent1();
                    PlantNode parentPlant2 = crossing.getParent2();
                    // get reusable plants, if any
                    PlantNode newParentPlant1 = solManager.getReusablePlantNode(parentPlant1.getID(), parentPlant1.getGeneration(), this, false);
                    PlantNode newParentPlant2 = solManager.getReusablePlantNode(parentPlant2.getID(), parentPlant2.getGeneration(), this, false);
                    // create new plant(s) if not reusable
                    if(newParentPlant1 == null){
                        long ID = parentPlant1.getID();
                        int duplication = getNumPlantNodesWithID(ID);
                        newParentPlant1 = new PlantNode(parentPlant1.getPlant(), parentPlant1.getGeneration(), parentPlant1.getParent(), ID, duplication);
                    }
                    if(newParentPlant2 == null){
                        long ID = parentPlant2.getID();
                        int duplication = getNumPlantNodesWithID(ID);
                        newParentPlant2 = new PlantNode(parentPlant2.getPlant(), parentPlant2.getGeneration(), parentPlant2.getParent(), ID, duplication);
                    }
                    // create new crossing, using new parents
                    CrossingNode newCrossing = new CrossingNode(newParentPlant1, newParentPlant2);
                    // add the new crossing as a new incoming parent of the depleted seed lot
                    depletedLot.addParentCrossing(newCrossing);
                    newCrossing.setChild(depletedLot);
                }               
                // reinit scheme (to recompute population sizes etc. with the updated scheme structure)
                reinitScheme();
                // check for bounding
                bounded = solManager.boundCurrentScheme(this);
                i++;
            }
            // check for any new depleted seed lots, resulting from extending the scheme
            depleted = solManager.getDepletedSeedLots(this);
        }
        return !bounded;
    }
    
    /**
     * Print a representation of the crossing scheme to standard output.
     */
    public void print(){
        // print crossing scheme
        System.out.println("\n### Crossing scheme ###\n");
        for(int gen=0; gen<=numGenerations; gen++){
            // print ith generation
            System.out.println("--- Generation " + gen + " (" + popSizePerGeneration[gen] + " plants) ---");
            // plants grown from seedlots
            List<PlantNode> plants = plantsPerGeneration.get(gen);
            for(int i=0; i<plants.size(); i++){
                PlantNode plant = plants.get(i);
                SeedLotNode parent = plant.getParent();
                System.out.print(parent + " => " + plant);
                if(i<plants.size()-1){
                    System.out.print(", ");
                }
            }
            System.out.println("");
            // crossings leading to new seed lots (available in next generation)
            if(gen < numGenerations){
                List<SeedLotNode> seedlots = seedLotsPerGeneration.get(gen+1);
                for(int i=0; i<seedlots.size(); i++){
                    SeedLotNode seedlot = seedlots.get(i);
                    List<CrossingNode> crossings = seedlot.getParentCrossings();
                    for(int j=0; j<crossings.size(); j++){
                        CrossingNode crossing = crossings.get(j);
                        System.out.print(crossing.getParent1() + " x " + crossing.getParent2() + " => " + seedlot);
                        if(j<crossings.size()-1){
                            System.out.print(", ");
                        }
                    }
                    if(i<seedlots.size()-1){
                        System.out.print(", ");
                    }
                }
                System.out.println("");
            }
        }
        
        System.out.println("");
        System.out.println("Final plant node " + finalPlantNode + " reached with:");
        System.out.println(" - " + numGenerations + " generation(s)");
        System.out.println(" - " + totalPopulationSize + " plants");
        System.out.println(" - max. " + getMaxPopulationSizePerGeneration() + " plants per generation");
        
        // print legend
        System.out.println("\n### Legend ###\n");
        for(long ID : plantsPerID.keySet()){
            List<PlantNode> pns = plantsPerID.get(ID);
            for(int i=0; i<pns.size(); i++){
                PlantNode pn = pns.get(i);
                System.out.print(pn.getUniqueID());
                if(i < pns.size()-1){
                    System.out.print(", ");
                }
            }
            // print LPA
            System.out.print(" (LPA: " + pns.get(0).getLinkagePhaseAmbiguity() + ")");
            System.out.println(":");
            System.out.println(pns.get(0).getPlant());
        }
        
        System.out.println("\n### OTHER VARIABLES ###\n");
        System.out.println("LPA: " + linkagePhaseAmbiguity);
        System.out.println("Grown from non-uniform seed lot: " + numTargetsFromNonUniformSeedLots);
        System.out.println("Gamma: " + popSizeTools.getSuccessProbability());
        System.out.println("Gamma':" + popSizeTools.computeDesiredSuccessProbPerTarget(numTargetsFromNonUniformSeedLots));
    }
    
    /**
     * String showing only most basic scheme info.
     */
    @Override
    public String toString(){
        DecimalFormat df = new DecimalFormat("#.##");
        return "{gen: " + numGenerations + ", pop: " + totalPopulationSize + ", lpa: " + df.format(linkagePhaseAmbiguity) + "}";
    }
    
    /**
     * Check if 2 crossing schemes are equivalent based on a comparison of seed lots
     * in each generation. Takes into account how the seed lots were created (as the
     * result of a crossing of which plants) and also which new plants are grown from it
     * in which subsequent generation(s).
     */
    @Override
    public boolean equals(Object o){
        boolean equal = false;
        if(o instanceof CrossingScheme){
            CrossingScheme s = (CrossingScheme) o;
            // first check some properties (quick), if not equal then schemes are surely not equivalent
            /*System.out.println("# Gen: " + numGenerations + " - " + s.getNumGenerations());
            System.out.println("Pop size: " + totalPopulationSize + " - " + s.getTotalPopulationSize());
            System.out.println("Non uniform: " + nrOfNonUniformPlantNodes + " - " + s.getNumTargetsFromNonUniformSeedLots());
            System.out.println("Pop size per gen: " + Arrays.toString(popSizePerGeneration) + " - " + Arrays.toString(s.getPopSizePerGeneration()));
            System.out.println("Final plant: " + finalPlantNode.getPlant() + " - " + s.getFinalPlantNode().getPlant());*/
            if(numGenerations == s.getNumGenerations()
                    && totalPopulationSize == s.getTotalPopulationSize()
                    && numTargetsFromNonUniformSeedLots == s.getNumTargetsFromNonUniformSeedLots()
                    && Arrays.equals(popSizePerGeneration, s.getPopSizePerGeneration())
                    && finalPlantNode.getPlant().equals(s.getFinalPlantNode().getPlant())){
                // schemes might be equivalent: compare seed lots
                int checkGen = 0;
                equal = true;
                while(equal && checkGen <= numGenerations){
                    Iterator<SeedLotNode> myGenIt = getSeedLotNodesFromGeneration(checkGen).iterator();
                    Set<SeedLotNode> otherMatched = new HashSet<>(); // already matched seed lots from other scheme's generation
                    while(equal && myGenIt.hasNext()){
                        SeedLotNode mySln = myGenIt.next();
                        Plant myParent1 = null, myParent2 = null;
                        if(!mySln.isInitialSeedLot()){
                            myParent1 = mySln.getParentCrossings().get(0).getParent1().getPlant();
                            myParent2 = mySln.getParentCrossings().get(0).getParent2().getPlant();
                        }
                        Map<Integer, Map<Plant, Integer>> myChildren = getChildPlants(mySln);
                        // look for matching seed lot node in other scheme
                        boolean matched = false;
                        Iterator<SeedLotNode> otherGenIt = s.getSeedLotNodesFromGeneration(checkGen).iterator();
                        while(!matched && otherGenIt.hasNext()){
                            SeedLotNode otherSln = otherGenIt.next();
                            if(!otherMatched.contains(otherSln)){
                                // not yet matched before
                                Plant otherParent1 = null, otherParent2 = null;
                                if(!otherSln.isInitialSeedLot()){
                                    otherParent1 = otherSln.getParentCrossings().get(0).getParent1().getPlant();
                                    otherParent2 = otherSln.getParentCrossings().get(0).getParent2().getPlant();
                                }
                                Map<Integer, Map<Plant, Integer>> otherChildren = getChildPlants(otherSln);
                                // check parents and children
                                matched = (equalPlants(myParent1, otherParent1) && equalPlants(myParent2, otherParent2)
                                            || equalPlants(myParent1, otherParent2) && equalPlants(myParent2, otherParent1))
                                        && myChildren.equals(otherChildren);
                                if(matched){
                                    otherMatched.add(otherSln);
                                }
                            }
                        }
                        equal = matched;
                    }
                    // check if all seed lots from other scheme's generation were successfully matched
                    if(otherMatched.size() != s.getSeedLotNodesFromGeneration(checkGen).size()){
                        equal = false;
                    }
                    checkGen++;
                }
            }
        }
        return equal;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        for(SeedLotNode sln : getSeedLotNodes()){
            if(!sln.isInitialSeedLot()){
                Plant parent1 = sln.getParentCrossings().get(0).getParent1().getPlant();
                Plant parent2 = sln.getParentCrossings().get(0).getParent2().getPlant();
                hash += parent1.hashCode(); 
                hash += parent2.hashCode();
            }
            Map<Integer, Map<Plant, Integer>> children = getChildPlants(sln);
            hash += children.hashCode();
        }
        return hash;
    }
    
    /**
     * Get the plants (not plant nodes!) that are grown from a seed lot node, grouped
     * per generation. Each generation number is mapped on a second map that contains
     * all plants grown from the seed lot in that generation, mapped on the number of
     * grown duplicates.
     */
    private Map<Integer, Map<Plant, Integer>> getChildPlants(SeedLotNode sln){
        Map<Integer,Map<Plant, Integer>> plants = new HashMap<>();
        for(int gen : sln.getChildren().keySet()){
            Map<Plant, Integer> genMap = new HashMap<>();
            for(PlantNode pn : sln.getChildren().get(gen)){
                Plant p = pn.getPlant();
                if(genMap.containsKey(p)){
                    genMap.put(p, genMap.get(p)+1);
                } else {
                    genMap.put(p, 1);
                }
            }
            plants.put(gen, genMap);
        }
        return plants;
    }
    
    private boolean equalPlants(Plant p1, Plant p2){
        return (p1 == null && p2 == null)
                || (p1 != null && p2 != null && p1.equals(p2));
    }
    
}
