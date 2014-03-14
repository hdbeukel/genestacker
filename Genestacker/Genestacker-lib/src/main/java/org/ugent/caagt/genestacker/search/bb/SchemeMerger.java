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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import org.ugent.caagt.genestacker.GeneticMap;
import org.ugent.caagt.genestacker.Genotype;
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
 *
 * @author Herman De Beukelaer <herman.debeukelaer@ugent.be>
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
    
    // ancestor plants from parent schemes
    protected Set<PlantDescriptor> ancestors;
    
    // bounding matrix for pairs of alternatives of parent schemes
    protected boolean[][] boundCross;
    // set of bounded genotypes
    protected Set<Genotype> boundedGenotypes;
    
    public SchemeMerger(CrossingSchemeAlternatives scheme1, CrossingSchemeAlternatives scheme2, GeneticMap map,
                                        BranchAndBoundSolutionManager solManager, SeedLot seedLot, Set<PlantDescriptor> ancestors,
                                        boolean[][] boundCross, Set<Genotype> boundedGenotypes){
        this.scheme1 = scheme1;
        this.scheme2 = scheme2;
        ancestors = new HashSet<>();
        ancestors.addAll(scheme1.getAncestorDescriptors());
        ancestors.addAll(scheme2.getAncestorDescriptors());
        this.map = map;
        this.solManager = solManager;
        this.seedLot = seedLot;
        this.ancestors = ancestors;
        this.boundCross = boundCross;
        this.boundedGenotypes = boundedGenotypes;
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
     * Cross the final plants of two crossing schemes, and merge their history in a Pareto
     * optimal way. Different alignments of the generations of both schemes are considered, where
     * generations can be put next to each other (parallel) or in an alternating way (sequential).
     * The latter option may be useful, e.g. to satisfy a possible constraint on the population size
     * per generation. 
     */
    public abstract List<CrossingSchemeAlternatives> combineSchemes() throws GenotypeException, CrossingSchemeException;
    
    protected void mergeHistory(MergedSchemes mergedSchemes, CrossingScheme curScheme, CrossingScheme alt1, Map<String, PlantNode> danglingPlantNodes1,
                                int nextGen1, CrossingScheme alt2, Map<String, PlantNode> danglingPlantNodes2, int nextGen2, BranchAndBoundSolutionManager solManager)
                                                                                                            throws CrossingSchemeException {
               
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
                    PlantNode plant = curScheme.getPlantNodeWithUniqueID(plantID);
                    PlantNode origPlant = remDanglingPlantNodes.get(plantID);
                    SeedLotNode origParentLot = origPlant.getParent();
                    // check if initial seedlot already present
                    SeedLotNode newParentLot = curScheme.getSeedLotNodeFromGenerationWithID(0, origParentLot.getID());
                    if(newParentLot == null){
                        newParentLot = new SeedLotNode(origParentLot.getSeedLot(), 0, origParentLot.getID(), 0);
                    }
                    // attach dangling plant node to parent seed lot
                    plant.setParent(newParentLot);
                    newParentLot.addChild(plant);
                    // reinit scheme
                    curScheme.reinitScheme();
                }

                // register scheme
                if(!solManager.boundCurrentScheme(curScheme)
                        && !solManager.boundGrowPlantInGeneration(
                                    curScheme.getFinalPlantNode().getPlant(),
                                    curScheme.getNumGenerations()
                           )
                        && curScheme.resolveDepletedSeedLots(solManager)){
                    
                    mergedSchemes.registerMergedScheme(curScheme);
                    
                }     

            } else {

                /****************************/
                /* CONTINUE MERGING HISTORY */
                /****************************/

                CrossingScheme extended;
                Map<String, PlantNode> newDanglingPlantNodes1, newDanglingPlantNodes2;

                // OPTION 1: attach next generation of both schemes
                if(nextGen1 > 0 && nextGen2 > 0){
                    extended = new CrossingScheme(curScheme.getPopulationSizeTools(), curScheme.getFinalPlantNode().deepShiftedUpwardsCopy());
                    newDanglingPlantNodes1 = new HashMap<>(danglingPlantNodes1);
                    newDanglingPlantNodes2 = new HashMap<>(danglingPlantNodes2);
                    // scheme 1
                    mergeGeneration(extended, nextGen1, newDanglingPlantNodes1, solManager);
                    // scheme 2
                    mergeGeneration(extended, nextGen2, newDanglingPlantNodes2, solManager);
                    // recursion
                    if(!solManager.boundCurrentScheme(extended) 
                            && !mergedSchemes.boundScheme(extended, alt1, newDanglingPlantNodes1.values(),
                                    nextGen1-1, alt2, newDanglingPlantNodes2.values(), nextGen2-1)){
                        mergeHistory(mergedSchemes, extended, alt1, newDanglingPlantNodes1, nextGen1-1, alt2, newDanglingPlantNodes2, nextGen2-1, solManager);
                    }
                }

                // OPTION 2: attach next generation of scheme 1 only
                if(nextGen1 > 0){
                    extended = new CrossingScheme(curScheme.getPopulationSizeTools(), curScheme.getFinalPlantNode().deepShiftedUpwardsCopy());
                    newDanglingPlantNodes1 = new HashMap<>(danglingPlantNodes1);
                    newDanglingPlantNodes2 = new HashMap<>(danglingPlantNodes2);
                    // scheme 1
                    mergeGeneration(extended, nextGen1, newDanglingPlantNodes1, solManager);
                    // recursion
                    if(!solManager.boundCurrentScheme(extended)
                            && !mergedSchemes.boundScheme(extended, alt1, newDanglingPlantNodes1.values(),
                                    nextGen1-1, alt2, newDanglingPlantNodes2.values(), nextGen2)){
                        mergeHistory(mergedSchemes, extended, alt1, newDanglingPlantNodes1, nextGen1-1, alt2, newDanglingPlantNodes2, nextGen2, solManager);
                    }
                }

                // OPTION 3: attach next generation of scheme 2 only
                if(nextGen2 > 0){
                    extended = new CrossingScheme(curScheme.getPopulationSizeTools(), curScheme.getFinalPlantNode().deepShiftedUpwardsCopy());
                    newDanglingPlantNodes1 = new HashMap<>(danglingPlantNodes1);
                    newDanglingPlantNodes2 = new HashMap<>(danglingPlantNodes2);
                    // scheme 2
                    mergeGeneration(extended, nextGen2, newDanglingPlantNodes2, solManager);
                    // recursion
                    if(!solManager.boundCurrentScheme(extended)
                            && !mergedSchemes.boundScheme(extended, alt1, newDanglingPlantNodes1.values(),
                                    nextGen1, alt2, newDanglingPlantNodes2.values(), nextGen2-1)){
                        mergeHistory(mergedSchemes, extended, alt1, newDanglingPlantNodes1, nextGen1, alt2, newDanglingPlantNodes2, nextGen2-1, solManager);
                    }
                }

            }
        }

    }
    
    // directly updates crossing scheme, and map with dangling plant nodes (attached nodes are removed; new dangling
    // nodes are added)
    protected void mergeGeneration(CrossingScheme curScheme, int mergedGen, Map<String, PlantNode> danglingPlantNodes,
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
                // remove dangling plant ID from map
                it.remove();
                
                // attach dangling plant in current scheme
                
                // check for seed lot with required ID in newly added generation
                SeedLotNode newParentLot = curScheme.getSeedLotNodeFromGenerationWithID(1, origParentLot.getID());
                if(newParentLot == null){
                    // no seed lot with required ID present in generation:
                    // reconstruct history and create new seed lot node
                    CrossingNode origCrossing = origParentLot.getParentCrossings().get(0);
                    CrossingNode newCrossing;
                    if(origCrossing.isSelfing()){
                        // selfing
                        SelfingNode origSelfing = (SelfingNode) origCrossing;
                        PlantNode origParentPlant = origSelfing.getParent();
                        // get a plant with required ID from the corresponding generation
                        // which is still reusable for an additional selfing
                        PlantNode newParentPlant = solManager.getReusablePlantNode(origParentPlant.getID(), 0, curScheme, true);
                        if(newParentPlant == null){
                            // no reusable parent found: grow new dangling parent
                            long ID = origParentPlant.getID();
                            int duplication = curScheme.getNumPlantNodesWithID(ID);
                            newParentPlant = new PlantNode(origParentPlant.getPlant(), 0, null, ID, duplication);
                            newDanglingPlantNodes.put(newParentPlant.getUniqueID(), origParentPlant);
                        }
                        // create new selfing, using new parent
                        newCrossing = new SelfingNode(newParentPlant);
                    } else {
                        // non-selfing
                        PlantNode origParentPlant1 = origCrossing.getParent1();
                        PlantNode origParentPlant2 = origCrossing.getParent2();
                        // search for reusable parents or create new ones
                        PlantNode newParentPlant1 = solManager.getReusablePlantNode(origParentPlant1.getID(), 0, curScheme, false);
                        if(newParentPlant1 == null){
                            // no reusable parent found: grow new dangling parent
                            long ID = origParentPlant1.getID();
                            int duplication = curScheme.getNumPlantNodesWithID(ID);
                            newParentPlant1 = new PlantNode(origParentPlant1.getPlant(), 0, null, ID, duplication);
                            newDanglingPlantNodes.put(newParentPlant1.getUniqueID(), origParentPlant1);
                        }
                        PlantNode newParentPlant2 = solManager.getReusablePlantNode(origParentPlant2.getID(), 0, curScheme, false);
                        if(newParentPlant2 == null){
                            // no reusable parent found: grow new dangling parent
                            long ID = origParentPlant2.getID();
                            int duplication = curScheme.getNumPlantNodesWithID(ID);
                            newParentPlant2 = new PlantNode(origParentPlant2.getPlant(), 0, null, ID, duplication);
                            newDanglingPlantNodes.put(newParentPlant2.getUniqueID(), origParentPlant2);
                        }
                        // create new crossing, using new parents
                        newCrossing = new CrossingNode(newParentPlant1, newParentPlant2);
                    }
                    // create new seed lot with seeds from the new crossing
                    List<CrossingNode> newCrossings = new ArrayList<>();
                    newCrossings.add(newCrossing);
                    long ID = origParentLot.getID();
                    int duplication = curScheme.getNumSeedLotNodesWithID(ID);
                    newParentLot = new SeedLotNode(origParentLot.getSeedLot(), 1, newCrossings, ID, duplication);
                }
                // attach dangling plant to its parent lot
                plant.setParent(newParentLot);
                newParentLot.addChild(plant); // manual attachment of child required, because node was originally created without parent
                // reinit scheme (to update seed/plant indices, pop sizes, etc.)
                curScheme.reinitScheme();
            }
        }
        // add new dangling plant nodes to map
        danglingPlantNodes.putAll(newDanglingPlantNodes);

    }   
    
}
