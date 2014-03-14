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

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.ugent.caagt.genestacker.DiploidChromosome;
import org.ugent.caagt.genestacker.Genotype;
import org.ugent.caagt.genestacker.Haplotype;
import org.ugent.caagt.genestacker.ObservableDiploidTargetState;
import org.ugent.caagt.genestacker.ObservableGenotypeState;
import org.ugent.caagt.genestacker.Plant;
import org.ugent.caagt.genestacker.SeedLot;
import org.ugent.caagt.genestacker.exceptions.DuplicateConstraintException;
import org.ugent.caagt.genestacker.search.CrossingScheme;
import org.ugent.caagt.genestacker.search.CrossingSchemeDescriptor;
import org.ugent.caagt.genestacker.search.DefaultDominatesRelation;
import org.ugent.caagt.genestacker.search.DominatesRelation;
import org.ugent.caagt.genestacker.search.FuturePlantNode;
import org.ugent.caagt.genestacker.search.ParetoFrontier;
import org.ugent.caagt.genestacker.search.PlantNode;
import org.ugent.caagt.genestacker.search.PopulationSizeTools;
import org.ugent.caagt.genestacker.search.SeedLotNode;
import org.ugent.caagt.genestacker.search.bb.heuristics.Heuristic;
import org.ugent.caagt.genestacker.search.bb.heuristics.Heuristics;
import org.ugent.caagt.genestacker.search.bb.heuristics.SeedLotFilter;
import org.ugent.caagt.genestacker.search.constraints.Constraint;
import org.ugent.caagt.genestacker.search.constraints.MaxCrossingsWithPlant;
import org.ugent.caagt.genestacker.search.constraints.MaxLinkagePhaseAmbiguity;
import org.ugent.caagt.genestacker.search.constraints.MaxNumGenerations;
import org.ugent.caagt.genestacker.search.constraints.MaxPopulationSizePerGeneration;
import org.ugent.caagt.genestacker.search.constraints.NumberOfSeedsPerCrossing;

/**
 * Responsible for managing solutions during branch and bound search: Pareto frontier,
 * bounding criteria, etc.
 * 
 * @author Herman De Beukelaer <herman.debeukelaer@ugent.be>
 */
public class BranchAndBoundSolutionManager implements SearchBounder {
    
    // desired ideotype
    private Genotype ideotype;
    
    // parents of ideotype forced to be homozygous?
    private boolean homozygousIdeotypeParents;
    
    // population size tools
    private PopulationSizeTools popSizeTools;
    
    // constraints
    private List<Constraint> constraints;
    
    // heuristics
    private Heuristics heuristics;
    
    // seed lot filters to be applied in given order (null, or empty list
    // if no filtering desired)
    private List<SeedLotFilter> heuristicSeedLotFilters;
    
    // number of seeds obtained from one crossing
    private NumberOfSeedsPerCrossing numSeedsPerCrossing;
    
    // constraint: max number of crossings with same plant
    private MaxCrossingsWithPlant maxCrossingsWithPlant;
    
    // constraint: max generations
    private MaxNumGenerations maxNumGen;
    
    // constraint: max linkage phase ambiguity
    private MaxLinkagePhaseAmbiguity maxLinkagePhaseAmbiguity;
    
    // constraint: max pop size per gen
    private MaxPopulationSizePerGeneration maxPopSizePerGen;
    
    // Pareto frontier
    private ParetoFrontier frontier;
    
    /**
     * Create a B&B solution manager the default dominates relation.
     */
    public BranchAndBoundSolutionManager(Genotype ideotype, PopulationSizeTools popSizeTools, NumberOfSeedsPerCrossing numSeedsPerCrossing,
                                            List<Constraint> constraints, Heuristics heuristics, List<SeedLotFilter> seedLotFilters)
                                                            throws  DuplicateConstraintException{
        this(new DefaultDominatesRelation(), ideotype, popSizeTools, numSeedsPerCrossing, constraints, heuristics, seedLotFilters, false);
    }
    
    /**
     * Create a B&B solution manager with given dominates relation.
     */
    public BranchAndBoundSolutionManager(DominatesRelation<CrossingSchemeDescriptor> dominatesRelation, Genotype ideotype, PopulationSizeTools popSizeTools,
                                            NumberOfSeedsPerCrossing numSeedsPerCrossing, List<Constraint> constraints,
                                            Heuristics heuristics, List<SeedLotFilter> seedLotFilters, boolean homozygousIdeotypeParents)
                                                throws DuplicateConstraintException{
        // check for duplicate constraints and presence of some specific constraints
        if(constraints != null && !validateConstraints(constraints)){
            throw new DuplicateConstraintException("Duplicate constraints.");
        }
        this.numSeedsPerCrossing = numSeedsPerCrossing;
        this.constraints = constraints;
        this.heuristics = heuristics;
        this.heuristicSeedLotFilters = seedLotFilters;
        this.ideotype = ideotype;
        this.homozygousIdeotypeParents = homozygousIdeotypeParents;
        this.popSizeTools = popSizeTools;
        // create Pareto frontier
        frontier = new ParetoFrontier(dominatesRelation);
        
        // set empty heuristics if null (to simplify pruning and bounding in case no heuristics are included)
        if(this.heuristics == null){
            List<Heuristic> emptyHeur = Collections.emptyList();
            this.heuristics = new Heuristics(emptyHeur);
        }
    }
    
    private boolean validateConstraints(List<Constraint> constraints){
        maxCrossingsWithPlant = null;
        maxNumGen = null;
        maxLinkagePhaseAmbiguity = null;
        maxPopSizePerGen = null;
        Set<String> ids = new HashSet<>();
        boolean valid = true;
        int i=0;
        while(valid && i < constraints.size()){
            Constraint c = constraints.get(i);
            // register id
            String id = c.getID();
            if(ids.add(id)){
                // check for specific constraints
                switch (id) {
                    case "MaxCrossingsWithPlant":
                        maxCrossingsWithPlant = (MaxCrossingsWithPlant) c;
                        break;
                    case "MaxGenerations":
                        maxNumGen = (MaxNumGenerations) c;
                        break;
                    case "MaxLinkagePhaseAmbiguity":
                        maxLinkagePhaseAmbiguity = (MaxLinkagePhaseAmbiguity) c;
                        break;
                    case "MaxPopSizePerGen":
                        maxPopSizePerGen = (MaxPopulationSizePerGeneration) c;
                        break;
                }
            } else {
                // id already present --> duplicate constraint
                valid = false;
            }
            i++;
        }
        return valid;
    }
    
    public ParetoFrontier getFrontier(){
        return frontier;
    }
    
    public void setFrontier(ParetoFrontier frontier){
        this.frontier = frontier;
    }
    
    /**
     * Check whether a given scheme is a solution to our problem, i.e. whether
     * the desired ideotype is reached in the final generation.
     * 
     * @param scheme
     */
    public boolean isSolution(CrossingScheme scheme){
        return scheme.getFinalPlantNode().getPlant().getGenotype().equals(ideotype);
    }
    
    /**
     * Check whether all constraints are satisfied for a given scheme.
     * 
     * @param scheme
     */
    private boolean areConstraintsSatisfied(CrossingSchemeDescriptor scheme){
        boolean satisfied = true;
        if(constraints != null){
            int i=0;
            while(satisfied && i<constraints.size()){
                Constraint c = constraints.get(i);
                satisfied = c.isSatisfied(scheme);
                i++;
            }
        }
        return satisfied;
    }
    
    /**
     * Checks whether the parents of the ideotype are homozygous. Assumes that
     * the given scheme has been completed, i.e that the ideotype is indeed obtained
     * in the final generation.
     */
    private boolean checkHomozygousIdeotypeParents(CrossingScheme scheme){
        // go through the plants of the penultimate generation
        boolean allHom = true;
        Iterator<PlantNode> it = scheme.getPlantNodesFromGeneration(scheme.getNumGenerations()-1).iterator();
        while(allHom && it.hasNext()){
            allHom = it.next().getPlant().getGenotype().isHomozygousAtAllTargetLoci();
        }
        return allHom;
    }
    
    /**
     * Returns a list of depleted seed lots, i.e. seed lots from which more seeds
     * are taken than the total number of available seeds in this lot. Returns
     * an empty list if no constraint on number of seeds per crossing.
     * 
     * @param scheme 
     */
    public List<SeedLotNode> getDepletedSeedLots(CrossingScheme scheme){
        if(numSeedsPerCrossing != null){
            return numSeedsPerCrossing.getDepletedSeedLots(scheme);
        } else {
            // no constr aint on number of seeds, so lots can never be depleted
            List<SeedLotNode> empty = Collections.emptyList();
            return empty;
        }
    }
    
    /**
     * Returns a plant node with the required ID, from the required generation,
     * which can still be reused as parent in an additional crossing. In case of a
     * selfing, the plant can only be reused if it can still be used at least two
     * times as parent.
     * 
     * @param plantNodeID
     * @param generation
     * @param scheme
     * @param selfing
     */
    public PlantNode getReusablePlantNode(long plantNodeID, int generation, CrossingScheme scheme, boolean selfing){
        if(maxCrossingsWithPlant != null){
            return maxCrossingsWithPlant.getReusablePlantNode(plantNodeID, generation, scheme, selfing);
        } else {
            // no constraint on number of crossings, so any plant with required ID from
            // required generation can be reused
            List<PlantNode> plants = scheme.getPlantNodesFromGenerationWithID(generation, plantNodeID);
            if(plants != null && !plants.isEmpty()){
                return plants.get(0);
            } else {
                return null;
            }
        }
    }
    
    /**
     * Filter the given seed lot. First, some basic filters are applied based on the constraints
     * on maximum linkage phase ambiguity and maximum population size per generation to remove all genotypes
     * that are certain to violate these constraints. Then, if any, all heuristic filters from the seedLotFilters
     * list are applied, in the order in which they occur in this list.
     * <br /><br />
     * Note: this modifies and returns the original seed lot object.
     */
    public SeedLot filterSeedLot(SeedLot seedlot){
        // apply basic filters (non-heuristic)
        for(Genotype g : seedlot.getGenotypes()){
            ObservableGenotypeState state = g.getObservableState();
            if(maxLinkagePhaseAmbiguity != null
                    && seedlot.getGenotypeGroup(state).getLinkagePhaseAmbiguity(g)
                            > maxLinkagePhaseAmbiguity.getMaxLinkagePhaseAmbiguity()){
                // linkage phase ambiguity is definitely too high
                seedlot.filterGenotype(g);
            } else if (maxPopSizePerGen != null
                        && seedlot.getGenotypeGroup(state).getProbabilityOfPhaseKnownGenotype(g)
                            < popSizeTools.computeTargetProbLowerBound(seedlot, maxPopSizePerGen.getMaxPopSizePerGen())){
                // probability of genotype is so small that it would definitely violate the maximum population size per generation
                seedlot.filterGenotype(g);
            }
        }
        // apply heuristic filters
        if(heuristicSeedLotFilters != null){
            for(SeedLotFilter filter : heuristicSeedLotFilters){
                seedlot = filter.filterSeedLot(seedlot);
            }
        }
        // return modified seed lot
        return seedlot;
    }
    
    /**
     * Register a new solution in the Pareto frontier. Returns true if the presented
     * solution is now part of the new Pareto frontier, else false (i.e. if the solution
     * does not satisfy all constraints or if it is strictly dominated by another solution
     * that is already contained in the frontier). Any other solution that is now dominated
     * by this new solution is removed from the frontier.
     */
    public boolean registerSolution(CrossingScheme newScheme){
        // check constraints (if enabled, also check for homozygous ideotype parents)
        if(isSolution(newScheme) && areConstraintsSatisfied(newScheme.getDescriptor())
                && (!homozygousIdeotypeParents || checkHomozygousIdeotypeParents(newScheme))){
            return frontier.register(newScheme);
        } else {
            return false; // is no solution, or does not satisy all constraints
        }
    }
    
    @Override
    public boolean boundCrossCurrentScheme(CrossingScheme scheme){
        if(heuristics.boundCrossCurrentScheme(scheme)){
            return true;
        } else {
            // create descriptor of abstract 'best' case result when continuing 
            // to cross the current scheme with an arbitrary previous scheme
            CrossingSchemeDescriptor desc = scheme.getDescriptor();
            desc.setNumGenerations(desc.getNumGenerations()+1); // at least 1 extra generation
            desc.setNumCrossings(desc.getNumCrossings()+1); // at least 1 extra crossing
            
            // apply any heuristic bound extensions
            desc = heuristics.extendBoundsUponCrossing(desc, scheme);
            
            // check constraints for abstract 'best' extended scheme
            if(!areConstraintsSatisfied(desc)){
                return true;
            } else {
                // check if dominated
                return frontier.dominatedByRegisteredObject(desc);
            }
        }
    }

    @Override
    public boolean boundCrossCurrentSchemeWithSpecificOther(CrossingScheme scheme, CrossingScheme other){
        if(penultimateGenerationReached(Math.max(scheme.getNumGenerations(), other.getNumGenerations()))
                && !ideotypeObtainableInNextGeneration(scheme.getFinalPlantNode().getPlant().getGenotype(), other.getFinalPlantNode().getPlant().getGenotype())){
            // only one generation left but desired ideotype cannot be obtained by crossing the given schemes, so bound!
            return true;
        } else if(heuristics.boundCrossCurrentSchemeWithSpecificOther(scheme, other)){
            return true;
        } else {
            // create descriptor of abstract 'best' case result when continuing 
            // to cross the current scheme with the given other scheme
            CrossingSchemeDescriptor desc = scheme.getDescriptor();
            
            // at least 1 extra generation
            desc.setNumGenerations(Math.max(scheme.getNumGenerations(), other.getNumGenerations()) + 1);
            // at least 1 extra crossing
            desc.setNumCrossings(Math.max(scheme.getNumCrossings(), other.getNumCrossings()) + 1);
            
            // compute minimum LPA and targets grown from non uniform seed lots after merging
            MergedPlantNodesLowerBounds pnBounds = computeLowerBoundsAfterMergingPlantNodes(scheme, other);
            // compute minimum population size after merging
            MergedSeedLotNodesLowerBounds slnBounds = computeLowerBoundsAfterMergingSeedLotNodes(scheme, other);
            
            // set min LPA
            desc.setLinkagePhaseAmbiguity(pnBounds.getMinLPA());
            
            // set min pop size after merging
            desc.setTotalPopSize(slnBounds.getMinPopSize());
            
            // set minimum pop size per generation
            desc.setMaxPopSizePerGeneration(Math.max(scheme.getMaxPopulationSizePerGeneration(), other.getMaxPopulationSizePerGeneration()));
            
            // apply any heuristic bound extensions
            desc = heuristics.extendBoundsUponCrossingWithSpecificOther(desc, scheme, other);
            
            // check constraints for abstract 'best' extended scheme
            if(!areConstraintsSatisfied(desc)){
                return true;
            } else {
                // check if dominated
                return frontier.dominatedByRegisteredObject(desc);
            }
        }
    }
    
    @Override
    public boolean boundCrossCurrentSchemeWithSpecificOtherWithSelectedTarget(CrossingScheme scheme, CrossingScheme other, PlantDescriptor target){
        if(boundGrowPlantInGeneration(target.getPlant(), Math.max(scheme.getNumGenerations(), other.getNumGenerations())+1)){
            // selected target should not be grown in the newly attached generation
            return true;
        } else if(heuristics.boundCrossCurrentSchemeWithSpecificOtherWithSelectedTarget(scheme, other, target)){
            return true;
        } else {
            // create descriptor of abstract 'best' case result when continuing 
            // to cross the current scheme with the given other scheme
            CrossingSchemeDescriptor desc = scheme.getDescriptor();
            
            // at least 1 extra generation
            desc.setNumGenerations(Math.max(scheme.getNumGenerations(), other.getNumGenerations()) + 1);
            // at least 1 extra crossing
            desc.setNumCrossings(Math.max(scheme.getNumCrossings(), other.getNumCrossings()) + 1);
            
            // compute minimum LPA and targets grown from non uniform seed lots after merging
            MergedPlantNodesLowerBounds pnBounds = computeLowerBoundsAfterMergingPlantNodes(scheme, other);
            // compute minimum population size after merging
            MergedSeedLotNodesLowerBounds slnBounds = computeLowerBoundsAfterMergingSeedLotNodes(scheme, other);
            
            // set minimum new LPA after extension with target
            double minLPA = 1.0 - (1.0-pnBounds.getMinLPA())*(1.0-target.getLinkagePhaseAmbiguity());
            desc.setLinkagePhaseAmbiguity(minLPA);
            
            // set minimum number of targets from non uniform seed lots nodes after extension
            int minNonUniform = pnBounds.getMinNrFromNonUniform();
            if(!target.grownFromUniformSeedLot()){
                minNonUniform++;
            }
            desc.setNumTargetsFromNonUniformSeedLots(minNonUniform);
            
            // set minimum pop size after extension with target
            
            // create future plant node
            PlantNode fpn = new FuturePlantNode(minNonUniform, target.getProb());            
            // compute min population size required for new target
            long newTargetPopSize = popSizeTools.computeRequiredSeedsForTargetPlant(fpn);
            
            // set lower bound for new total pop size
            desc.setTotalPopSize(slnBounds.getMinPopSize() + newTargetPopSize);
            // update max pop size per generation
            desc.setMaxPopSizePerGeneration(Math.max(Math.max(scheme.getMaxPopulationSizePerGeneration(), other.getMaxPopulationSizePerGeneration()), newTargetPopSize));
            
            // apply any heuristic bound extensions
            desc = heuristics.extendBoundsUponCrossingWithSpecificOtherWithSelectedTarget(desc, scheme, other, target);
            
            // check constraints for abstract 'best' extended scheme
            if(!areConstraintsSatisfied(desc)){
                return true;
            } else {
                // check if dominated
                return frontier.dominatedByRegisteredObject(desc);
            }
        }
    }
    
    private MergedPlantNodesLowerBounds computeLowerBoundsAfterMergingPlantNodes(CrossingScheme scheme1, CrossingScheme scheme2){        
        // 1) full scheme 1 + non reused plant nodes of scheme 2
        MergedPlantNodesLowerBounds bounds1 = computeLowerBoundsAfterMergingPlantNodesOneWay(scheme1, scheme2);
        // 1) full scheme 2 + non reused plant nodes of scheme 1
        MergedPlantNodesLowerBounds bounds2 = computeLowerBoundsAfterMergingPlantNodesOneWay(scheme2, scheme1);
        // return maximum (both computations always hold)
        double lpa = Math.max(bounds1.getMinLPA(),bounds2.getMinLPA());
        int numNonUniform = Math.max(bounds1.getMinNrFromNonUniform(),bounds2.getMinNrFromNonUniform());
        return new MergedPlantNodesLowerBounds(lpa, numNonUniform);
    }
    
    /**
     * Account for entire scheme 'full' + final plant node of other scheme (never reused) + remaining non overlapping plant nodes of other scheme.
     */
    private MergedPlantNodesLowerBounds computeLowerBoundsAfterMergingPlantNodesOneWay(CrossingScheme full, CrossingScheme other){
        // full scheme + final plant node of other scheme
        double lpa = 1.0 - (1.0-full.getLinkagePhaseAmbiguity())*(1.0-other.getFinalPlantNode().getLinkagePhaseAmbiguity());
        int numNonUniform = full.getNumTargetsFromNonUniformSeedLots();
        if(!other.getFinalPlantNode().grownFromUniformLot()){
            numNonUniform++;
        }
        // remaining non overlapping plant nodes of other scheme
        for(PlantNode pn : other.getPlantNodes()){
            if(!pn.equals(other.getFinalPlantNode())                // final plant node already accounted for
                && !full.containsPlantNodesWithID(pn.getID())){     // no plant nodes with same ID present in scheme 'full'
                    // node can impossibly be reused/merged
                    // --> account for LPA
                    lpa = 1.0 - (1.0-lpa)*(1.0-pn.getLinkagePhaseAmbiguity());
                    // --> account for num targets grown from non uniform seed lots
                    if(!pn.grownFromUniformLot()){
                        numNonUniform++;
                    }
            }
        }
        return new MergedPlantNodesLowerBounds(lpa, numNonUniform);
    }
    
    private MergedSeedLotNodesLowerBounds computeLowerBoundsAfterMergingSeedLotNodes(CrossingScheme scheme1, CrossingScheme scheme2){
        // 1) full scheme 1 + non reused seed lot nodes of scheme 2
        MergedSeedLotNodesLowerBounds bounds1 = computeLowerBoundsAfterMergingSeedLotNodesOneWay(scheme1, scheme2);
        // 1) full scheme 2 + non reused seed lot nodes of scheme 1
        MergedSeedLotNodesLowerBounds bounds2 = computeLowerBoundsAfterMergingSeedLotNodesOneWay(scheme2, scheme1);
        // return maximum (both computations always hold)
        long pop = Math.max(bounds1.getMinPopSize(), bounds2.getMinPopSize());
        return new MergedSeedLotNodesLowerBounds(pop);
    }
    
    /**
     * Account for entire scheme 'full' + non overlapping seed lot nodes of other scheme.
     */
    private MergedSeedLotNodesLowerBounds computeLowerBoundsAfterMergingSeedLotNodesOneWay(CrossingScheme full, CrossingScheme other){
        // full scheme
        long pop = full.getTotalPopulationSize();
        // non overlapping seed lot nodes of other scheme
        for(SeedLotNode sln : other.getSeedLotNodes()){
            if(!full.containsSeedLotNodesWithID(sln.getID())){  // no seed lot nodes with same ID present in scheme 'full'
                // node can impossibly be reused/merged --> account for pop size of all targets grown from this seed lot
                pop += sln.getSeedsTakenFromSeedLot();
            }
        }
        return new MergedSeedLotNodesLowerBounds(pop);
    }
    
    @Override
    public boolean boundSelfCurrentScheme(CrossingScheme scheme){
        if(scheme.getNumGenerations() == 0 && scheme.getFinalPlantNode().getPlant().isHomozygousAtAllTargetLoci()){
            // no point in selfing homozygous initial plants, because these are supposed never to be depleted
            return true;
        } else if(penultimateGenerationReached(scheme.getNumGenerations())
                    && !ideotypeObtainableInNextGeneration(scheme.getFinalPlantNode().getPlant().getGenotype(), scheme.getFinalPlantNode().getPlant().getGenotype())){
            // only one generation left but desired ideotype cannot be obtained by selfing the given parent scheme, so bound!
            return true;
        } else if(heuristics.boundSelfCurrentScheme(scheme)){
            return true;
        } else {
            // create descriptor of abstract 'best' case result when 
            // selfing the current scheme
            CrossingSchemeDescriptor desc = scheme.getDescriptor();
            desc.setNumGenerations(desc.getNumGenerations()+1); // 1 extra generation
            desc.setNumCrossings(desc.getNumCrossings()+1); // at least 1 extra crossing
            
            // apply any heuristic bound extensions
            desc = heuristics.extendBoundsUponSelfing(desc, scheme);

            // check constraints for abstract 'best' extended scheme
            if(!areConstraintsSatisfied(desc)){
                return true;
            } else {
                // check if dominated
                return frontier.dominatedByRegisteredObject(desc);
            }
        }
    }
    
    @Override
    public boolean boundSelfCurrentSchemeWithSelectedTarget(CrossingScheme scheme, PlantDescriptor target) {
        if(scheme.getNumGenerations() == 0 && scheme.getFinalPlantNode().getPlant().isHomozygousAtAllTargetLoci()){
            // no point in selfing homozygous initial plants, because these are supposed never to be depleted
            return true;
        } else if(boundGrowPlantInGeneration(target.getPlant(), scheme.getNumGenerations()+1)){
            // selected target should not be grown in newly attached generation
            return true;
        } else if(heuristics.boundSelfCurrentSchemeWithSelectedTarget(scheme, target)){
            return true;
        } else {
            // create descriptor of abstract 'best' case result when 
            // selfing the current scheme and attaching the selected target
            CrossingSchemeDescriptor desc = scheme.getDescriptor();
            
            desc.setNumGenerations(desc.getNumGenerations()+1); // 1 extra generation
            desc.setNumCrossings(desc.getNumCrossings()+1); // at least 1 extra crossing
            
            // compute new number of targets grown from non-uniform seed lots
            int numNonUniform = scheme.getNumTargetsFromNonUniformSeedLots();
            if(!target.grownFromUniformSeedLot()){
                numNonUniform++;
            }
            desc.setNumTargetsFromNonUniformSeedLots(numNonUniform);
            
            // create future plant node
            PlantNode fpn = new FuturePlantNode(numNonUniform, target.getProb());
            // take into account minimum extra pop size of selected target
            long minExtraPopSize = popSizeTools.computeRequiredSeedsForTargetPlant(fpn);
            // register increased total pop size
            desc.setTotalPopSize(desc.getTotalPopSize()+minExtraPopSize);
            // update maximum pop size per generation
            desc.setMaxPopSizePerGeneration(Math.max(desc.getMaxPopSizePerGeneration(), minExtraPopSize));
            
            // update LPA
            desc.setLinkagePhaseAmbiguity(1.0 - (1.0-desc.getLinkagePhaseAmbiguity())*(1.0-target.getLinkagePhaseAmbiguity()));
            
            // apply any heuristic bound extensions
            desc = heuristics.extendBoundsUponSelfingWithSelectedTarget(desc, scheme, target);

            // check constraints for abstract 'best' extended scheme
            if(!areConstraintsSatisfied(desc)){
                return true;
            } else {
                // check if dominated
                return frontier.dominatedByRegisteredObject(desc);
            }
        }
    }
    
    @Override
    public boolean boundCurrentScheme(CrossingScheme scheme){
        if(heuristics.boundCurrentScheme(scheme)){
            return true;
        } else {
            
            // create scheme descriptor
            CrossingSchemeDescriptor desc = scheme.getDescriptor();
            
            // apply any heuristic bound extensions
            desc = heuristics.extendBoundsForCurrentScheme(desc, scheme);
            
            // check constraints
            if(!areConstraintsSatisfied(desc)){
                return true;
            } else {
                // check if dominated
                return frontier.dominatedByRegisteredObject(desc);
            }
        }
    }
    
    @Override
    public boolean boundQueueScheme(CrossingScheme scheme){
        return heuristics.boundQueueScheme(scheme);
    }
    
    @Override
    public boolean boundDequeueScheme(CrossingScheme scheme){
        return heuristics.boundDequeueScheme(scheme);
    }
    
    @Override
    public boolean boundGrowPlantFromAncestors(Set<PlantDescriptor> ancestors, PlantDescriptor p){
        return heuristics.boundGrowPlantFromAncestors(ancestors, p);
    }
    
    @Override
    public boolean boundGrowPlantInGeneration(Plant p, int generation){
        // never bound dummy plants
        if(p.isDummyPlant()){
            return false;
        }
        // check heuristics
        if(heuristics.boundGrowPlantInGeneration(p, generation)){
            return true;
        } else {
            // bound if one of the following conditions holds:
            //  - plant grown in final generation and g is not the ideotype
            //  - plant grown in penultimate generation and genotype can impossibly
            //    be created from this genotype
            //  - homozygous ideotype parents required, but plant is grown in
            //    penultimate generation + is not yet the ideotype + is not homozygous
            return finalGenerationReached(generation) && !p.getGenotype().equals(ideotype)
               ||  penultimateGenerationReached(generation) && !ideotypeObtainableInNextGeneration(p.getGenotype())
               ||  homozygousIdeotypeParents && penultimateGenerationReached(generation)
                   && !p.getGenotype().equals(ideotype) && !p.getGenotype().isHomozygousAtAllTargetLoci();
        }
    }
    
    /**
     * Check whether it is possible to obtain the ideotype in the next generation,
     * by crossing given parent with any other plant. It is checked whether for every
     * chromosome the parent can yield either the upper or lower target haplotype.
     * 
     * @param parent
     */
    private boolean ideotypeObtainableInNextGeneration(Genotype parent){
        boolean obtainable = true;
        // go through chromosomes
        int c = 0;
        while(obtainable && c < parent.nrOfChromosomes()){
            obtainable = haplotypeObtainable(ideotype.getChromosomes().get(c).getHaplotypes()[0], parent.getChromosomes().get(c))
                      || haplotypeObtainable(ideotype.getChromosomes().get(c).getHaplotypes()[1], parent.getChromosomes().get(c));
            c++;
        }
        return obtainable;
    }
    
    /**
     * Check whether it is possible to obtain the ideotype in the next generation,
     * by crossing the given parents. It is checked whether for each chromosome,
     * parent 1 may yield the upper haplotype and parent 2 may yield the lower haplotype,
     * or vice versa.
     * 
     * @param parent1
     * @param parent2
     */
    private boolean ideotypeObtainableInNextGeneration(Genotype parent1, Genotype parent2){
        boolean obtainable = true;
        // go through chromosomes
        int c = 0;
        while(obtainable && c < parent1.nrOfChromosomes()){
            obtainable = haplotypeObtainable(ideotype.getChromosomes().get(c).getHaplotypes()[0], parent1.getChromosomes().get(c))
                         && haplotypeObtainable(ideotype.getChromosomes().get(c).getHaplotypes()[1], parent2.getChromosomes().get(c))
                      || haplotypeObtainable(ideotype.getChromosomes().get(c).getHaplotypes()[1], parent1.getChromosomes().get(c))
                         && haplotypeObtainable(ideotype.getChromosomes().get(c).getHaplotypes()[0], parent2.getChromosomes().get(c));
            c++;
        }
        return obtainable;
    }
    
    private boolean haplotypeObtainable(Haplotype hap, DiploidChromosome chrom){
        boolean obtainable = true;
        int l = 0;
        // go through loci in chromosome
        ObservableDiploidTargetState[] targetStates = chrom.getObservableState().getTargetStates();
        while(obtainable && l < chrom.nrOfLoci()){
            // check locus l
            obtainable = (hap.targetPresent(l) && (targetStates[l] == ObservableDiploidTargetState.ONCE || targetStates[l] == ObservableDiploidTargetState.TWICE))
                      || (!hap.targetPresent(l) && (targetStates[l] == ObservableDiploidTargetState.NONE || targetStates[l] == ObservableDiploidTargetState.ONCE));
            l++;
        }
        return obtainable;
    }
    
    /**
     * Check whether we have reached the maximum generation when going to generation 'gen'.
     */
    public boolean finalGenerationReached(int gen){
        if(maxNumGen == null){
            return false;
        } else {
            return gen >= maxNumGen.getMaxNumGenerations();
        }
    }
    
    /**
     * Check whether we have reached the penultimate generation when going to generation 'gen'.
     */
    public boolean penultimateGenerationReached(int gen){
        if(maxNumGen == null){
            return false;
        } else {
            return gen >= maxNumGen.getMaxNumGenerations()-1;
        }
    }
    
    public Genotype getIdeotype(){
        return ideotype;
    }
    
    
    
    /*********************************************************/
    /* SOME PRIVATE UTILITY CLASSES WRAPPING MULTIPLE SCORES */
    /*********************************************************/
    
    private class MergedPlantNodesLowerBounds{
        
        // min LPA
        private double minLPA;
        // min number of plant nodes grown from non uniform seed lots
        private int minNrFromNonUniform;

        public MergedPlantNodesLowerBounds(double minLPA, int minNrFromNonUniform) {
            this.minLPA = minLPA;
            this.minNrFromNonUniform = minNrFromNonUniform;
        }

        public double getMinLPA() {
            return minLPA;
        }

        public int getMinNrFromNonUniform() {
            return minNrFromNonUniform;
        }
        
    }
    
    private class MergedSeedLotNodesLowerBounds{
        
        // min pop size
        private long minPopSize;

        public MergedSeedLotNodesLowerBounds(long minPopSize) {
            this.minPopSize = minPopSize;
        }

        public long getMinPopSize() {
            return minPopSize;
        }
        
    }
        
}
