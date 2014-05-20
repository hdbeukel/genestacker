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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.ugent.caagt.genestacker.search.CrossingScheme;
import org.ugent.caagt.genestacker.search.CrossingSchemeDescriptor;
import org.ugent.caagt.genestacker.search.ParetoFrontier;
import org.ugent.caagt.genestacker.search.PlantNode;
import org.ugent.caagt.genestacker.search.SeedLotNode;

/**
 * Represents possible ways to merge two schemes with minimum
 * number of generations and optimal reuse of material.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class MergedSchemes{

   ParetoFrontier pareto;

   public MergedSchemes(){
       pareto = new ParetoFrontier();
   }

   /**
    * Register the scheme resulting from merging two smaller scheme with a specific
    * alignment of generations. The scheme is discarded if better merges have already
    * been found.
    * 
    * @param scheme crossing scheme resulting from a merge with a specific alignment of generations
    */
   public void registerMergedScheme(CrossingScheme scheme){
       pareto.register(scheme);
   }

   public Set<CrossingScheme> getMergedSchemes(){
       return pareto.getFrontier();
   }

   /**
    * Check whether further construction of the current alignment of generations should be pruned, because any
    * extension of this alignment is known to be dominated by an already completed other alignment. Takes into
    * account which plant nodes and seed lot nodes have necessarily still to be inserted, and the corresponding
    * minimum increase in LPA and population size. Also computes the minimum number of additional generations
    * required to finish the alignment.
    * 
    * @param curAlignment current combined scheme containing already aligned generations of the smaller schemes
    * @param scheme1 smaller scheme 1
    * @param danglingPlantNodes1 dangling plant nodes in current combined scheme originating from scheme 1
    * @param nextGen1 next generation of scheme 1 to be inserted into the combined scheme (bottom up)
    * @param scheme2 smaller scheme 2
    * @param danglingPlantNodes2 dangling plant nodes in current combined scheme originating from scheme 2
    * @param nextGen2 next generation of scheme 2 to be inserted into the combined scheme (bottom up)
    * @return <code>true</code> if further extension of this alignment should be aborted
    */
   public boolean pruneAlignment(CrossingScheme curAlignment, CrossingScheme scheme1, Collection<PlantNode> danglingPlantNodes1,
                                int nextGen1, CrossingScheme scheme2, Collection<PlantNode> danglingPlantNodes2, int nextGen2){

       // get scheme descriptor
       CrossingSchemeDescriptor desc = curAlignment.getDescriptor();

       // min increase in generations

       int minGenIncrease = Math.max(nextGen1, nextGen2);
       desc.setNumGenerations(desc.getNumGenerations()+minGenIncrease);

       // min increase in pop size and linkage phase ambiguity

       // scheme 1
       Collection<PlantNode> plantNodes1 = getNodesWithAncestors(scheme1, danglingPlantNodes1, nextGen1);
       double bestCaseLPAInc1 = 0.0;
       for(PlantNode pn : plantNodes1){
           bestCaseLPAInc1 = 1.0 - (1.0-bestCaseLPAInc1)*(1.0-pn.getLinkagePhaseAmbiguity());
       }
       Collection<SeedLotNode> seedLots1 = getAncestorSeedLots(scheme1, nextGen1);
       int minPopSizeInc1 = 0;
       for(SeedLotNode sln : seedLots1){
           minPopSizeInc1 += sln.getSeedsTakenFromSeedLot();
       }
       // scheme 2
       Collection<PlantNode> plantNodes2 = getNodesWithAncestors(scheme2, danglingPlantNodes2, nextGen2);
       double bestCaseLPAInc2 = 0.0;
       for(PlantNode pn : plantNodes2){
           bestCaseLPAInc2 = 1.0 - (1.0-bestCaseLPAInc2)*(1.0-pn.getLinkagePhaseAmbiguity());
       }
       Collection<SeedLotNode> seedLots2 = getAncestorSeedLots(scheme2, nextGen2);
       int minPopSizeInc2 = 0;
       for(SeedLotNode sln : seedLots2){
           minPopSizeInc2 += sln.getSeedsTakenFromSeedLot();
       }

       // get max of min pop size / LPA increase of both branches
       // --> merged scheme will have at least this increase!

       desc.setTotalPopSize(desc.getTotalPopSize() + Math.max(minPopSizeInc1, minPopSizeInc2));
       desc.setLinkagePhaseAmbiguity(1.0 - (1.0-desc.getLinkagePhaseAmbiguity())*(1.0-Math.max(bestCaseLPAInc1, bestCaseLPAInc2)));

       // check if dominated
       return pareto.dominatedByRegisteredObject(desc);
   }

   private Collection<PlantNode> getNodesWithAncestors(CrossingScheme branch, Collection<PlantNode> danglingPlantNodes, int nextGen){
       // add all distinct ancestor plant nodes, starting at nodes which have not yet been assigend a parent
       // in the merged scheme (including these nodes themselves)
       Set<PlantNode> ancestors = new HashSet<>();
       // add dangling plant nodes themselves
       ancestors.addAll(danglingPlantNodes);
       // add ancestors
       for(int gen = nextGen-1; gen >= 0; gen--){
           ancestors.addAll(branch.getPlantNodesFromGeneration(gen));
       }
       return ancestors;
   }
   
   private Collection<SeedLotNode> getAncestorSeedLots(CrossingScheme branch, int nextGen){
       // add all distinct ancestor seed lot nodes, starting from nextGen (up to generation 0)
       Set<SeedLotNode> ancestors = new HashSet<>();
       // add ancestors
       for(int gen = nextGen; gen >= 0; gen--){
           ancestors.addAll(branch.getSeedLotNodesFromGeneration(gen));
       }
       return ancestors;
   }

}
