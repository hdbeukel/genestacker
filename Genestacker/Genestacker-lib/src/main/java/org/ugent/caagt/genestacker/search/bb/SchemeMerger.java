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

package org.ugent.caagt.genestacker.search.bb;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.ugent.caagt.genestacker.GeneticMap;
import org.ugent.caagt.genestacker.SeedLot;
import org.ugent.caagt.genestacker.exceptions.CrossingSchemeException;
import org.ugent.caagt.genestacker.exceptions.GenotypeException;
import org.ugent.caagt.genestacker.search.CrossingNode;
import org.ugent.caagt.genestacker.search.CrossingScheme;
import org.ugent.caagt.genestacker.search.CrossingSchemeAlternatives;
import org.ugent.caagt.genestacker.search.PlantNode;
import org.ugent.caagt.genestacker.search.SeedLotNode;
import org.ugent.caagt.genestacker.search.SelfingNode;

/**
 * Responsible for merging two partial crossing schedules when they are combined through
 * an additional crossing. Considers all possible alignments of generations of the partial
 * schedules, but only retains those that are Pareto optimal w.r.t. the number of generations
 * and the amount of reuse.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public abstract class SchemeMerger implements Callable<List<CrossingSchemeAlternatives>>{

    // input
    protected CrossingSchemeAlternatives scheme1, scheme2;
    protected GeneticMap map;
    protected BranchAndBoundSolutionManager solManager;
    
    // continue flag
    protected boolean cont;
    
    // seed lot obtained by crossing final plants of parent schemes
    protected SeedLot seedLot;
    
    public SchemeMerger(CrossingSchemeAlternatives scheme1, CrossingSchemeAlternatives scheme2, GeneticMap map,
                                        BranchAndBoundSolutionManager solManager, SeedLot seedLot){
        this.scheme1 = scheme1;
        this.scheme2 = scheme2;
        this.map = map;
        this.solManager = solManager;
        this.seedLot = seedLot;
        cont = true;
    }
    
    public void stop(){
        cont = false;
    }
    
    @Override
    public List<CrossingSchemeAlternatives> call() throws GenotypeException, CrossingSchemeException{
        return combineSchemes();
    }
    
    /**
     * Cross the final plants of two partial crossing schemes, and merge these schemes into a larger scheme
     * in a Pareto optimal way (input is specified in the constructor). Different alignments of the generations
     * of both schemes are considered, where generations can be put next to each other (parallel) or in an alternating
     * way (sequential). The latter option may e.g. be useful to satisfy a possible constraint on the population size
     * per generation. 
     * 
     * @return list of crossing scheme alternatives resulting from the combination of two smaller schemes through
     *         an additional crossing
     * @throws GenotypeException if incompatible genotypes are crossed, or anything else goes wrong while creating
     *         the seed lot resulting from the performed crossing
     * @throws CrossingSchemeException if anything goes wrong while creating the extended crossing scheme alternatives
     */
    public abstract List<CrossingSchemeAlternatives> combineSchemes() throws GenotypeException, CrossingSchemeException;
    
    /**
     * <p>
     * Merge two partial crossing scheme by recursively aligning their generations. In every step, three options
     * are considered: (1) include one more generation of both schemes (parallel), (2) include an additional generation
     * of scheme 1 only, or (3) include an additional generation of scheme 2 only. When merging additional generations
     * into the combined schedule, material (plant nodes and seed lot nodes) is reused if already available.
     * </p>
     * <p>
     * Only Pareto optimal alignments are retained, and during construction of all possible merges, new alignments
     * are pruned if known to be suboptimal compared to already obtained other alignments, or compared to already
     * obtained full solutions.
     * </p>
     * 
     * @param mergedSchemes already constructed alignments, initially empty and extended during recursion
     * @param curAlignment current combined scheme containing already aligned generations (under construction)
     * @param scheme1 partial crossing scheme 1 to be combined with partial scheme 2
     * @param danglingPlantNodes1 dangling plant nodes (i.e. which have not yet been assigned a parental seed lot)
     *                            in the combined scheme, originating from scheme 1; unique IDs of new plant nodes
     *                            in the merged scheme are mapped on plant nodes from the original scheme 1
     * @param nextGen1 next generation of scheme 1 to be inserted in to combined scheme (bottom up)
     * @param scheme2 partial crossing scheme 2 to be combined with partial scheme 1
     * @param danglingPlantNodes2 dangling plant nodes (i.e. which have not yet been assigned a parental seed lot)
     *                            in the combined scheme, originating from scheme 2; unique IDs of new plant nodes
     *                            in the merged scheme are mapped on plant nodes from the original scheme 2
     * @param nextGen2 next generation of scheme 2 to be inserted in to combined scheme (bottom up)
     * @param solManager branch and bound solution manager, used for pruning etc.
     * @throws CrossingSchemeException if anything goes wrong while creating the extended crossing schedules
     */
    protected void merge(MergedSchemes mergedSchemes, CrossingScheme curAlignment, CrossingScheme scheme1,
                         Map<String, PlantNode> danglingPlantNodes1, int nextGen1, CrossingScheme scheme2,
                         Map<String, PlantNode> danglingPlantNodes2, int nextGen2,
                         BranchAndBoundSolutionManager solManager) throws CrossingSchemeException {
               
        if(cont){
            if(nextGen2 == 0 && nextGen1 == 0){

                /*************/
                /* COMPLETED */
                /*************/

                // add initial seedlots to fix remaining dangling plants of 0th generation

                Map<String, PlantNode> remDanglingPlantNodes = new HashMap<>();
                remDanglingPlantNodes.putAll(danglingPlantNodes2);
                remDanglingPlantNodes.putAll(danglingPlantNodes1);
                for(String plantID : remDanglingPlantNodes.keySet()){
                    PlantNode plant = curAlignment.getPlantNodeWithUniqueID(plantID);
                    PlantNode origPlant = remDanglingPlantNodes.get(plantID);
                    SeedLotNode origParentLot = origPlant.getParent();
                    // check if initial seedlot already present
                    SeedLotNode newParentLot = curAlignment.getSeedLotNodeFromGenerationWithID(0, origParentLot.getID());
                    if(newParentLot == null){
                        newParentLot = new SeedLotNode(origParentLot.getSeedLot(), 0, origParentLot.getID(), 0);
                    }
                    // attach dangling plant node to parent seed lot
                    plant.setParent(newParentLot);
                    newParentLot.addChild(plant);
                    // reinit scheme
                    curAlignment.reinitScheme();
                }

                // register scheme
                if(!solManager.pruneCurrentScheme(curAlignment)
                        && !solManager.pruneGrowPlantInGeneration(
                                    curAlignment.getFinalPlantNode().getPlant(),
                                    curAlignment.getNumGenerations()
                           )
                        && curAlignment.resolveDepletedSeedLots(solManager)){
                    
                    mergedSchemes.registerMergedScheme(curAlignment);
                    
                }     

            } else {

                /****************************/
                /* CONTINUE MERGING HISTORY */
                /****************************/

                CrossingScheme extended;
                Map<String, PlantNode> newDanglingPlantNodes1, newDanglingPlantNodes2;

                // OPTION 1: attach next generation of both schemes
                if(nextGen1 > 0 && nextGen2 > 0){
                    extended = new CrossingScheme(curAlignment.getPopulationSizeTools(), curAlignment.getFinalPlantNode().deepShiftedUpwardsCopy());
                    newDanglingPlantNodes1 = new HashMap<>(danglingPlantNodes1);
                    newDanglingPlantNodes2 = new HashMap<>(danglingPlantNodes2);
                    // scheme 1
                    insertGeneration(extended, nextGen1, newDanglingPlantNodes1, solManager);
                    // scheme 2
                    insertGeneration(extended, nextGen2, newDanglingPlantNodes2, solManager);
                    // recursion
                    if(!solManager.pruneCurrentScheme(extended) 
                            && !mergedSchemes.pruneAlignment(extended, scheme1, newDanglingPlantNodes1.values(),
                                    nextGen1-1, scheme2, newDanglingPlantNodes2.values(), nextGen2-1)){
                        merge(mergedSchemes, extended, scheme1, newDanglingPlantNodes1, nextGen1-1, scheme2, newDanglingPlantNodes2, nextGen2-1, solManager);
                    }
                }

                // OPTION 2: attach next generation of scheme 1 only
                if(nextGen1 > 0){
                    extended = new CrossingScheme(curAlignment.getPopulationSizeTools(), curAlignment.getFinalPlantNode().deepShiftedUpwardsCopy());
                    newDanglingPlantNodes1 = new HashMap<>(danglingPlantNodes1);
                    newDanglingPlantNodes2 = new HashMap<>(danglingPlantNodes2);
                    // scheme 1
                    insertGeneration(extended, nextGen1, newDanglingPlantNodes1, solManager);
                    // recursion
                    if(!solManager.pruneCurrentScheme(extended)
                            && !mergedSchemes.pruneAlignment(extended, scheme1, newDanglingPlantNodes1.values(),
                                    nextGen1-1, scheme2, newDanglingPlantNodes2.values(), nextGen2)){
                        merge(mergedSchemes, extended, scheme1, newDanglingPlantNodes1, nextGen1-1, scheme2, newDanglingPlantNodes2, nextGen2, solManager);
                    }
                }

                // OPTION 3: attach next generation of scheme 2 only
                if(nextGen2 > 0){
                    extended = new CrossingScheme(curAlignment.getPopulationSizeTools(), curAlignment.getFinalPlantNode().deepShiftedUpwardsCopy());
                    newDanglingPlantNodes1 = new HashMap<>(danglingPlantNodes1);
                    newDanglingPlantNodes2 = new HashMap<>(danglingPlantNodes2);
                    // scheme 2
                    insertGeneration(extended, nextGen2, newDanglingPlantNodes2, solManager);
                    // recursion
                    if(!solManager.pruneCurrentScheme(extended)
                            && !mergedSchemes.pruneAlignment(extended, scheme1, newDanglingPlantNodes1.values(),
                                    nextGen1, scheme2, newDanglingPlantNodes2.values(), nextGen2-1)){
                        merge(mergedSchemes, extended, scheme1, newDanglingPlantNodes1, nextGen1, scheme2, newDanglingPlantNodes2, nextGen2-1, solManager);
                    }
                }

            }
        }

    }
    
    /**
     * <p>
     * Insert an additional generation of one of both smaller crossing schemes at the top level (0th generation)
     * of the combined, larger scheme. For every dangling plant node originating from the considered smaller scheme,
     * it is checked whether its parental seed lot node is part of the generation to be merged, and if so, it is
     * inserted in the combined scheme (or reused if already available in the 0th generation). When inserting a new
     * seed lot node, its parental crossing and corresponding plant nodes are also inserted (again, plant nodes are
     * reused if already available in the 0th generation, for example when performing multiple crossing with the same
     * plant). Newly inserted plant nodes are inserted as dangling nodes, i.e. their parental seed lots are to be
     * inserted later.
     * </p>
     * <p>
     * Note that this method directly updates the combined scheme and the map with dangling plant nodes originating
     * from the considered smaller scheme (nodes that are now attached to their parental seed lot node are removed,
     * new dangling plant nodes are added).
     * </p>
     * 
     * @param curScheme current, combined scheme (under construction)
     * @param mergedGen generation of smaller scheme to insert into the combined scheme (at the top level)
     * @param danglingPlantNodes dangling plant nodes originating from the smaller scheme
     * @param solManager branch and bound solution manager
     * @throws CrossingSchemeException if anything goes wrong during extensions of the combined scheme
     */
    protected void insertGeneration(CrossingScheme curScheme, int mergedGen, Map<String, PlantNode> danglingPlantNodes,
                                        BranchAndBoundSolutionManager solManager) throws CrossingSchemeException {
        
        Iterator<Map.Entry<String, PlantNode>> it = danglingPlantNodes.entrySet().iterator();
        Map<String, PlantNode> newDanglingPlantNodes = new HashMap<>();
        
        while(cont && it.hasNext()){
            // inspect dangling plant node
            Map.Entry<String, PlantNode> entry = it.next();
            String plantID = entry.getKey();
            PlantNode plant = curScheme.getPlantNodeWithUniqueID(plantID);
            PlantNode origPlant = entry.getValue();
            SeedLotNode origParentLot = origPlant.getParent();
            // check merged generation
            if(origParentLot.getGeneration() == mergedGen){
                // remove dangling plant node from map
                it.remove();
                
                // attach dangling plant in current scheme
                
                // check for presence of seed lot with required ID (in generation 1)
                SeedLotNode newParentSeedLot = curScheme.getSeedLotNodeFromGenerationWithID(1, origParentLot.getID());
                if(newParentSeedLot == null){
                    // no seed lot with required ID present in generation 1:
                    // add parental crossing in generation 0 and create new seed lot node
                    CrossingNode origCrossing = origParentLot.getParentCrossing();
                    CrossingNode newCrossing;
                    if(origCrossing.isSelfing()){
                        // selfing
                        SelfingNode origSelfing = (SelfingNode) origCrossing;
                        PlantNode origParentPlant = origSelfing.getParent();
                        // look for a plant with the required ID in the current 0th generation
                        PlantNode newParentPlant = curScheme.getPlantNodeFromGenerationWithID(0, origParentPlant.getID());
                        if(newParentPlant == null){
                            // no reusable parent found: grow new dangling parent plant
                            long ID = origParentPlant.getID();
                            int subID = curScheme.getNumPlantNodesWithID(ID);
                            newParentPlant = new PlantNode(origParentPlant.getPlant(), 0, null,
                                                            ID, subID, origParentPlant.getNumDuplicates());
                            newDanglingPlantNodes.put(newParentPlant.getUniqueID(), origParentPlant);
                        }
                        // create new selfing, using new parent
                        newCrossing = new SelfingNode(newParentPlant);
                        // increase parent plant duplicates if necessary to perform all attached crossings
                        increasePlantDuplicatesIfNecessary(newParentPlant, solManager);
                    } else {
                        // normal crossing (no selfing)
                        PlantNode origParentPlant1 = origCrossing.getParent1();
                        PlantNode origParentPlant2 = origCrossing.getParent2();
                        // look for a plant with the required ID in the current 0th generation
                        PlantNode newParentPlant1 = curScheme.getPlantNodeFromGenerationWithID(0, origParentPlant1.getID());
                        if(newParentPlant1 == null){
                            // no reusable parent found: grow new dangling parent plant
                            long ID = origParentPlant1.getID();
                            int subID = curScheme.getNumPlantNodesWithID(ID);
                            newParentPlant1 = new PlantNode(origParentPlant1.getPlant(), 0, null,
                                                                ID, subID, origParentPlant1.getNumDuplicates());
                            newDanglingPlantNodes.put(newParentPlant1.getUniqueID(), origParentPlant1);
                        }
                        // repeat for other parent plant
                        PlantNode newParentPlant2 = curScheme.getPlantNodeFromGenerationWithID(0, origParentPlant2.getID());
                        if(newParentPlant2 == null){
                            // no reusable parent found: grow new dangling parent
                            long ID = origParentPlant2.getID();
                            int subID = curScheme.getNumPlantNodesWithID(ID);
                            newParentPlant2 = new PlantNode(origParentPlant2.getPlant(), 0, null,
                                                                ID, subID, origParentPlant2.getNumDuplicates());
                            newDanglingPlantNodes.put(newParentPlant2.getUniqueID(), origParentPlant2);
                        }
                        // create new crossing, using new parents
                        newCrossing = new CrossingNode(newParentPlant1, newParentPlant2);
                        // increase parent plant duplicates if necessary to perform all attached crossings
                        increasePlantDuplicatesIfNecessary(newParentPlant1, solManager);
                        increasePlantDuplicatesIfNecessary(newParentPlant2, solManager);
                    }
                    // create new seed lot with seeds from the new crossing
                    // (in generation 1, obtained after crossings from 0th generation)
                    long ID = origParentLot.getID();
                    int subID = curScheme.getNumSeedLotNodesWithID(ID);
                    newParentSeedLot = new SeedLotNode(origParentLot.getSeedLot(), 1, newCrossing, ID, subID);
                }
                // attach dangling plant to its parent lot
                plant.setParent(newParentSeedLot);
                newParentSeedLot.addChild(plant);   // manual attachment of child required, because
                                                    // node was originally created without parent
                // reinit scheme (to update node indices etc. to detect reuse while inserting remaining nodes)
                curScheme.reinitScheme();
            }
        }
        // add new dangling plant nodes to map
        danglingPlantNodes.putAll(newDanglingPlantNodes);

    }
    
    /**
     * Duplicates the given plant if necessary to perform all desired crossings.
     * 
     * @param plantNode plant node of which contained plant is duplicated if necessary
     */
    private void increasePlantDuplicatesIfNecessary(PlantNode plantNode, BranchAndBoundSolutionManager solManager){
        if(solManager.maxCrossingsWithPlantExceeded(plantNode)){
            plantNode.setNumDuplicates(solManager.getRequiredPlantDuplicatesForCrossings(plantNode));
        }
    }
    
}
