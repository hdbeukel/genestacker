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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.ugent.caagt.genestacker.GeneticMap;
import org.ugent.caagt.genestacker.Genotype;
import org.ugent.caagt.genestacker.Plant;
import org.ugent.caagt.genestacker.SeedLot;
import org.ugent.caagt.genestacker.exceptions.CrossingSchemeException;
import org.ugent.caagt.genestacker.exceptions.GenotypeException;
import org.ugent.caagt.genestacker.search.CrossingNode;
import org.ugent.caagt.genestacker.search.CrossingScheme;
import org.ugent.caagt.genestacker.search.CrossingSchemeAlternatives;
import org.ugent.caagt.genestacker.search.DummyPlantNode;
import org.ugent.caagt.genestacker.search.PlantNode;
import org.ugent.caagt.genestacker.search.SeedLotNode;

/**
 * First merges the history of the parent schemes and then attaches
 * each of the possible new plants to the merged history.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class MergeFirstSchemeMerger extends SchemeMerger {

    public MergeFirstSchemeMerger(CrossingSchemeAlternatives scheme1, CrossingSchemeAlternatives scheme2, GeneticMap map,
                                                    BranchAndBoundSolutionManager solManager, SeedLot seedLot,
                                                    Set<PlantDescriptor> ancestors, boolean[][] pruneCross,
                                                    Set<Genotype> prunedGenotypes){
        super(scheme1, scheme2, map, solManager, seedLot, ancestors, pruneCross, prunedGenotypes);
    }
    
    @Override
    public List<CrossingSchemeAlternatives> combineSchemes() throws GenotypeException, CrossingSchemeException {
        
        List<CrossingSchemeAlternatives> newSchemes = new ArrayList<>(); 
        
        // try avoiding construction of alignment(s) if we can predict that they will all
        // be pruned after attaching any of the plants grown from the new seed lot
        
        boolean skip = true;
        Iterator<Genotype> it = seedLot.getGenotypes().iterator();
        while(cont && skip && it.hasNext()){
            Genotype g = it.next();
            if(!prunedGenotypes.contains(g)){
                Plant p = new Plant(g);
                PlantDescriptor pdesc = new PlantDescriptor(
                    p,
                    seedLot.getGenotypeGroup(g.getAllelicFrequencies()).getProbabilityOfPhaseKnownGenotype(g),
                    seedLot.getGenotypeGroup(g.getAllelicFrequencies()).getLinkagePhaseAmbiguity(g),
                    seedLot.isUniform()
                );
                for(int alt1i=0; alt1i<scheme1.nrOfAlternatives(); alt1i++){
                    CrossingScheme alt1 = scheme1.getAlternatives().get(alt1i);
                    for(int alt2i=0; alt2i<scheme2.nrOfAlternatives(); alt2i++){
                        CrossingScheme alt2 = scheme2.getAlternatives().get(alt2i);
                        skip = pruneCross[alt1i][alt2i] || solManager.pruneCrossCurrentSchemeWithSpecificOtherWithSelectedTarget(alt1, alt2, pdesc);
                    }
                }
            }
        }

        if(!skip){
            
            // construct different ways of merging the history, considering all
            // pairs of alternatives of the parent schemes
            
            MergedSchemes merged = new MergedSchemes();
            
            for(int alt1i=0; alt1i<scheme1.nrOfAlternatives(); alt1i++){
                CrossingScheme alt1 = scheme1.getAlternatives().get(alt1i);
                for(int alt2i=0; alt2i<scheme2.nrOfAlternatives(); alt2i++){
                    CrossingScheme alt2 = scheme2.getAlternatives().get(alt2i);
                    
                    // check pruning
                    if(!pruneCross[alt1i][alt2i]){
                        
                        /****************/
                        /* CROSS PLANTS */
                        /****************/

                        // map: unique ID of dangling plant node in new scheme
                        // --> corresponding plant node in original scheme
                        Map<String, PlantNode> danglingPlantNodes1 = new HashMap<>();
                        Map<String, PlantNode> danglingPlantNodes2 = new HashMap<>();

                        // create dangling copy of final plant nodes of both schemes
                        PlantNode finalPlant1 = new PlantNode(alt1.getFinalPlantNode().getPlant(), 0,
                                                            null, alt1.getFinalPlantNode().getID(), 0,
                                                            alt1.getFinalPlantNode().getNumDuplicates());
                        danglingPlantNodes1.put(finalPlant1.getUniqueID(), alt1.getFinalPlantNode());
                        PlantNode finalPlant2 = new PlantNode(alt2.getFinalPlantNode().getPlant(), 0,
                                                            null, alt2.getFinalPlantNode().getID(), 0,
                                                            alt2.getFinalPlantNode().getNumDuplicates());
                        danglingPlantNodes2.put(finalPlant2.getUniqueID(), alt2.getFinalPlantNode());
                        // cross plant nodes
                        CrossingNode crossing = new CrossingNode(finalPlant1, finalPlant2);
                        // create new seedlot node resulting from crossing
                        SeedLotNode sln = new SeedLotNode(seedLot, 1, crossing, -1, 0); // note: real ID will be set when merging is complete
                        // grow dummy plant node from new seedlot, to be replaced with the possible
                        // real new plant nodes after the Pareto optimal alignments have been computed
                        PlantNode dummy = new DummyPlantNode(sln.getGeneration(), sln);
                        // create partial scheme
                        CrossingScheme curScheme = new CrossingScheme(alt1.getPopulationSizeTools(), dummy);
                                                
                        /*****************/
                        /* MERGE HISTORY */
                        /*****************/

                        merge(merged, curScheme, alt1, danglingPlantNodes1, alt1.getNumGenerations(),
                                                    alt2, danglingPlantNodes2, alt2.getNumGenerations(), solManager);
                    
                    }
                }
            }
            
            /********************/
            /* SET SEED LOT IDS */
            /********************/

            // set unique ID for all seed lots resulting from  different history merges
            
            for(CrossingScheme scheme : merged.getMergedSchemes()){
                scheme.getFinalPlantNode().getParent().assignNextID();
            }
            
            /****************************/
            /* CREATE RESULTING SCHEMES */
            /****************************/

            // now we will replace the dummy with each possible real plant grown from the new seedlot, attached
            // to the computed Pareto optimal alternatives resulting from the merging procedure

            it = seedLot.getGenotypes().iterator();
            while(cont && it.hasNext()){
                Genotype g = it.next();
                if(!prunedGenotypes.contains(g)){
                    
                    Plant p = new Plant(g);

                    List<CrossingScheme> newAlts = new ArrayList<>();

                    // consider alternatives resulting from the merging procedure
                    Iterator<CrossingScheme> schemeIt = merged.getMergedSchemes().iterator();
                    while(cont && schemeIt.hasNext()){
                        CrossingScheme scheme = schemeIt.next();
                        // check pruning
                        if(!solManager.pruneGrowPlantInGeneration(p, scheme.getNumGenerations())){
                            // create deep upwards copy, and final plant node and its parent
                            PlantNode finalPn = scheme.getFinalPlantNode().deepUpwardsCopy();
                            SeedLotNode finalSln = finalPn.getParent();
                            // remove final plant node (the dummy)
                            finalSln.removeChild(finalPn);
                            // create new plant node as child of final seedlot, replacing the dummy
                            PlantNode newFinalPlantNode = new PlantNode(p, finalPn.getGeneration(), finalSln);
                            // create final scheme with new final plant
                            CrossingScheme finalScheme = new CrossingScheme(scheme.getPopulationSizeTools(), newFinalPlantNode);
                            // register scheme if:
                            //   - not pruned
                            //   - depleted seedlots successfully resolved, in case final
                            //     seedlot became depleted after replacing the dummy
                            if(!solManager.pruneCurrentScheme(finalScheme)
                                    && finalScheme.resolveDepletedSeedLots(solManager)){
                                newAlts.add(finalScheme);
                            }
                        }
                    }

                    // register new alternatives
                    if(!newAlts.isEmpty()){
                        newSchemes.add(new CrossingSchemeAlternatives(newAlts));
                    }
                }

            }
            return newSchemes;
            
        } else {
            
            List<CrossingSchemeAlternatives> empty = Collections.emptyList();
            return empty;
        
        }
    }

}
