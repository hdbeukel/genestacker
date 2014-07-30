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
import java.util.HashSet;
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
                                                    BranchAndBoundSolutionManager solManager, SeedLot seedLot){
        super(scheme1, scheme2, map, solManager, seedLot);
    }
    
    @Override
    public List<CrossingSchemeAlternatives> combineSchemes() throws GenotypeException, CrossingSchemeException {
                
        // gather descriptors of ancestors occurring in both schemes
        Set<PlantDescriptor> ancestors = new HashSet<>();
        ancestors.addAll(scheme1.getAncestorDescriptors());
        ancestors.addAll(scheme2.getAncestorDescriptors());
        // compose set of genotypes which are allowed to be grown from with these ancestors
        Set<Genotype> candidateGenotypes = new HashSet<>();
        for(Genotype g : seedLot.getGenotypes()){
            if(!solManager.pruneGrowPlantFromAncestors(ancestors, new PlantDescriptor(
                        new Plant(g),
                        seedLot.getGenotypeGroup(g.getAllelicFrequencies()).getProbabilityOfPhaseKnownGenotype(g),
                        seedLot.getGenotypeGroup(g.getAllelicFrequencies()).getLinkagePhaseAmbiguity(g),
                        seedLot.isUniform()
                    ))){
                candidateGenotypes.add(g);
            }
        }
        
        // check which combinations of scheme alternatives are pruned, and especially if any are not pruned
        boolean[][] pruneCross = new boolean[scheme1.nrOfAlternatives()][scheme2.nrOfAlternatives()];
        boolean pruneAll = true;
        int alt1i, alt2i;
        Iterator<Genotype> it;
        alt1i = 0;
        while(alt1i<scheme1.nrOfAlternatives()){
            CrossingScheme alt1 = scheme1.getAlternatives().get(alt1i);
            alt2i=0;
            while(alt2i<scheme2.nrOfAlternatives()){
                CrossingScheme alt2 = scheme2.getAlternatives().get(alt2i);
                // check general pruning
                boolean prune = solManager.pruneCrossCurrentSchemeWithSpecificOther(alt1, alt2);
                if(prune){
                    // all extensions are definitely pruned
                    pruneCross[alt1i][alt2i] = true;
                } else {
                    // not pruned yet, now check if there is any genotype in the offspring that does not
                    // cause the extension to be pruned anyway when being attached as next target
                    prune = true;
                    it = candidateGenotypes.iterator();
                    while(prune && it.hasNext()){
                        Genotype g = it.next();
                        PlantDescriptor pdesc = new PlantDescriptor(
                            new Plant(g),
                            seedLot.getGenotypeGroup(g.getAllelicFrequencies()).getProbabilityOfPhaseKnownGenotype(g),
                            seedLot.getGenotypeGroup(g.getAllelicFrequencies()).getLinkagePhaseAmbiguity(g),
                            seedLot.isUniform()
                        );
                        prune = solManager.pruneCrossCurrentSchemeWithSpecificOtherWithSelectedTarget(alt1, alt2, pdesc);
                    }
                    // all candidate genotypes pruned?
                    pruneCross[alt1i][alt2i] = prune;
                }
                // all combinations pruned until now?
                pruneAll = pruneAll && prune;
                alt2i++;
            }
            alt1i++;
        }    
        
        if(!pruneAll){
            
            List<CrossingSchemeAlternatives> newSchemes = new ArrayList<>();
            
            // construct different ways of merging the history, considering all
            // pairs of alternatives of the parent schemes
            
            MergedSchemes merged = new MergedSchemes();
            
            for(alt1i=0; alt1i<scheme1.nrOfAlternatives(); alt1i++){
                CrossingScheme alt1 = scheme1.getAlternatives().get(alt1i);
                for(alt2i=0; alt2i<scheme2.nrOfAlternatives(); alt2i++){
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

            it = candidateGenotypes.iterator();
            while(cont && it.hasNext()){
                Genotype g = it.next();
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
            return newSchemes;
            
        } else {
            
            List<CrossingSchemeAlternatives> empty = Collections.emptyList();
            return empty;
        
        }
    }

}
