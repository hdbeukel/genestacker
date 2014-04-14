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
import org.ugent.caagt.genestacker.AllelicFrequency;
import org.ugent.caagt.genestacker.GenotypeAllelicFrequencies;
import org.ugent.caagt.genestacker.Plant;
import org.ugent.caagt.genestacker.SeedLot;
import org.ugent.caagt.genestacker.exceptions.DuplicateConstraintException;
import org.ugent.caagt.genestacker.search.CrossingScheme;
import org.ugent.caagt.genestacker.search.CrossingSchemeDescriptor;
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
 * pruning criteria, etc.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class BranchAndBoundSolutionManager implements PruningCriterion {
    
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
     * Create a branch and bound solution manager.
     * 
     * @param dominatesRelation given dominates relation
     * @param ideotype targeted ideotype
     * @param popSizeTools population size tools used to compute population sizes and related bounds
     * @param numSeedsPerCrossing number of seeds obtained from a single crossing
     * @param constraints list of constraints
     * @param heuristics list of applied heuristics
     * @param seedLotFilters list of applied seed lot filters
     * @param homozygousIdeotypeParents if <code>true</code>, it is required that the parents of the
     *                                  ideotype are both homozygous at all considered loci
     * @throws DuplicateConstraintException if multiple instances of the same constraint are included
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
        
        // set empty heuristics if null
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
     * Check whether a given scheme is a solution to the problem, i.e whether
     * the desired ideotype is reached in the final generation.
     * 
     * @param scheme crossing scheme
     * @return <code>true</code> if the ideotype is obtained in the final generation
     *         of the given crossing scheme
     */
    public boolean isSolution(CrossingScheme scheme){
        return scheme.getFinalPlantNode().getPlant().getGenotype().equals(ideotype);
    }
    
    /**
     * Check whether all constraints are satisfied for a given scheme.
     * 
     * @param scheme crossing scheme for which the constraints are to be checked
     * @return <code>true</code> if all constraints are satisfied
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
     * Checks whether both parents of the ideotype are homozygous. Assumes that
     * the given scheme has been completed, i.e that the ideotype is indeed obtained
     * in the final generation.
     * 
     * @param scheme a crossing scheme that obtains the ideotype in the final generation
     * @return <code>true</code> if both parents of the ideotype are homozygous at all considered loci
     */
    private boolean checkHomozygousIdeotypeParents(CrossingScheme scheme){
        // go through the plants of the penultimate generation
        boolean allHom = true;
        Iterator<PlantNode> it = scheme.getPlantNodesFromGeneration(scheme.getNumGenerations()-1).iterator();
        while(allHom && it.hasNext()){
            allHom = it.next().getPlant().getGenotype().isHomozygousAtAllContainedLoci();
        }
        return allHom;
    }
    
    /**
     * Returns a list of depleted seed lots, i.e seed lots from which more seeds
     * are taken than the total number of available seeds. Returns an empty list
     * if the number of seeds produced per crossing has not been specified or if
     * no seed lots are depleted.
     * 
     * @param scheme crossing scheme in which depleted seed lots are to be found
     * @return list of depleted seed lots in the given scheme
     */
    public List<SeedLotNode> getDepletedSeedLots(CrossingScheme scheme){
        if(numSeedsPerCrossing != null){
            return numSeedsPerCrossing.getDepletedSeedLots(scheme);
        } else {
            // no constraint on number of seeds, so lots can never be depleted
            return Collections.emptyList();
        }
    }
    
    /**
     * Verify whether a given seed lot is depleted, taking into account the number of
     * seeds produced by a crossing. If the produced number of seeds has not been specified,
     * this method always returns <code>false</code>.
     * 
     * @param seedLotNode given seed lot node
     * @return <code>true</code> if the given seed lot is depleted
     */
    public boolean isDepleted(SeedLotNode seedLotNode){
        if(numSeedsPerCrossing != null){
            return numSeedsPerCrossing.isDepeleted(seedLotNode);
        } else {
            // number of seeds per crossing not specified
            return false;
        }
    }
    
    /**
     * Compute the required number of crossings to provide sufficient seeds for this seed lot,
     * so that all target plants can be obtained. Takes into account the number of seeds produced
     * per crossing, if set; else, this method always returns 1.
     * 
     * @param seedLotNode given seed lot node
     * @return number of crossings required to produce sufficient seeds to obtain all targets
     *         grown from this seed lot
     */
    public int getRequiredCrossingsForSufficientSeeds(SeedLotNode seedLotNode){
        if(numSeedsPerCrossing != null){
            return numSeedsPerCrossing.getRequiredCrossingsForSufficientSeeds(seedLotNode);
        } else {
            // number of seeds per crossing not specified
            return 1;
        }
    }
    
    /**
     * Checks whether the maximum number of crossings with the plant contained in the given plant node has
     * been exceeded, in which case the plant should be duplicated. If no constraint on the number of crossings
     * per plant has been set, this method always returns <code>false</code>
     * 
     * @param plantNode given plant node
     * @return <code>true</code> if the maximum number of crossings has been exceeded for the given plant
     */
    public boolean maxCrossingsWithPlantExceeded(PlantNode plantNode){
        if(maxCrossingsWithPlant != null){
            // check constraint
            return maxCrossingsWithPlant.maxCrossingsWithPlantExceeded(plantNode);
        } else {
            // no constraint on number of crossings per plant
            return false;
        }
    }
    
    /**
     * Get the required number of duplicates of the given plant to perform all scheduled crossings.
     * Takes into account the maximum number of crossings per plant, if set; else, this method always
     * returns 1.
     * 
     * @param plantNode given plant
     * @return number of required duplicates to perform all scheduled crossings
     */
    public int getRequiredPlantDuplicatesForCrossings(PlantNode plantNode){
        if(maxCrossingsWithPlant != null){
            return maxCrossingsWithPlant.getRequiredPlantDuplicatesForCrossings(plantNode);
        } else {
            // no constraint on number of crossings per plant
            return 1;
        }
    }
    
    /**
     * <p>
     * Filter the given seed lot. First, some basic filters are applied based on the constraints
     * on maximum linkage phase ambiguity and maximum population size per generation to remove all
     * genotypes that will certain cause these constraints to be violated. Then, all heuristic filters,
     * if any, are applied in the order in which they occur in the seed lot filter list.
     * </p>
     * <p>
     * Note: this modifies and returns the original seed lot object.
     * </p>
     * 
     * @param seedlot seed lot to be filtered
     * @return original seed lot object after applying all filters
     */
    public SeedLot filterSeedLot(SeedLot seedlot){
        // apply basic filters (non-heuristic)
        for(Genotype g : seedlot.getGenotypes()){
            GenotypeAllelicFrequencies state = g.getAllelicFrequencies();
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
     * Register a new solution in the Pareto frontier. Returns <code>true</code> if the presented
     * solution has been added to the Pareto frontier, else <code>false</code> (i.e. if the solution
     * does not satisfy all constraints or if it is dominated by another solution that is already
     * contained in the frontier). If the solution is added to the Pareto frontier, any other solution
     * that is now dominated by this new solution is removed from the frontier.
     * 
     * @param newScheme new solution to be registered in the Pareto frontier
     * @return <code>true</code> if the solution has been added to the frontier
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
    public boolean pruneCrossCurrentScheme(CrossingScheme scheme){
        if(heuristics.pruneCrossCurrentScheme(scheme)){
            return true;
        } else {
            // create descriptor of abstract 'best' case result when continuing 
            // to cross the current scheme with an arbitrary previous scheme
            CrossingSchemeDescriptor desc = scheme.getDescriptor();
            desc.setNumGenerations(desc.getNumGenerations()+1); // at least 1 extra generation
            desc.setNumCrossings(desc.getNumCrossings()+1); // at least 1 extra crossing
            
            // apply any heuristic bound extensions (e.g. heuristic H6)
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
    public boolean pruneCrossCurrentSchemeWithSpecificOther(CrossingScheme scheme, CrossingScheme other){
        if(penultimateGenerationReached(Math.max(scheme.getNumGenerations(), other.getNumGenerations()))
                && !ideotypeObtainableInNextGeneration(scheme.getFinalPlantNode().getPlant().getGenotype(), other.getFinalPlantNode().getPlant().getGenotype())){
            // only one generation left but desired ideotype cannot be obtained by crossing the given schemes; prune!
            return true;
        } else if(heuristics.pruneCrossCurrentSchemeWithSpecificOther(scheme, other)){
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
    public boolean pruneCrossCurrentSchemeWithSpecificOtherWithSelectedTarget(CrossingScheme scheme, CrossingScheme other, PlantDescriptor target){
        if(pruneGrowPlantInGeneration(target.getPlant(), Math.max(scheme.getNumGenerations(), other.getNumGenerations())+1)){
            // selected target should not be grown in the newly attached generation
            return true;
        } else if(heuristics.pruneCrossCurrentSchemeWithSpecificOtherWithSelectedTarget(scheme, other, target)){
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
    
    /**
     * Compute a lower bound for the linkage phase ambiguity and number of targets grown from nonuniform seed lots,
     * when combining the inner plant nodes of two crossing schemes upon performing a new crossing of their final
     * plants. The bounds are based on the fact that only plant nodes with the same ID will every be reused.
     * Non-reused plant nodes will always retain their contribution to the LPA and number of targets grown
     * from nonuniform seed lots.
     * 
     * @param scheme1 partial scheme 1
     * @param scheme2 partial scheme 2
     * @return lower bound for LPA and number of targets grown from nonuniform seed lots in combined scheme,
     *         in which a new crossing with the final plants of both schemes is performed
     */
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
     * Compute "one way" lower bounds for LPA and number of targets grown from nonuniform seed lots after combining
     * the given schemes through an additional crossing. "One way" means that one scheme is fully contained in the
     * combined scheme, whereas nodes from this full scheme may have been reused for the other scheme. Accounts for
     * the entire scheme <code>full</code>, the final plant node of other scheme (never reused), and remaining non
     * overlapping plant nodes from the other scheme.
     * 
     * @param full fully contained crossing scheme
     * @param other other scheme, may partially reuse elements from full scheme
     * @return lower bounds for LPA and number of targets grown from nonuniform seed lots
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
                    // node can impossibly be reused
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
    
    /**
     * Compute a lower bound for the total population size, when combining the inner seed lot nodes of two crossing
     * schemes upon performing a new crossing of their final plants. The bounds are based on the fact that only seed
     * lot nodes with the same ID will every be reused. Non-reused seed lot nodes will always retain their complete
     * contribution to the total population size.
     * 
     * @param scheme1 partial scheme 1
     * @param scheme2 partial scheme 2
     * @return lower bound for total population size in combined scheme, in which a new crossing with the final
     *         plants of both schemes is performed
     */
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
     * Compute "one way" lower bound for the total population size after combining the given schemes through an
     * additional crossing. "One way" means that one scheme is fully contained in the combined scheme, whereas
     * nodes from this full scheme may have been reused for the other scheme. Accounts for the entire scheme
     * <code>full</code> and remaining non overlapping seed lot nodes from the other scheme.
     * 
     * @param full fully contained crossing scheme
     * @param other other scheme, may partially reuse elements from full scheme
     * @return lower bound for total population size
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
    public boolean pruneSelfCurrentScheme(CrossingScheme scheme){
        if(scheme.getNumGenerations() == 0 && scheme.getFinalPlantNode().getPlant().isHomozygousAtAllTargetLoci()){
            // no point in selfing homozygous initial plants, because these are supposed never to be depleted
            return true;
        } else if(penultimateGenerationReached(scheme.getNumGenerations())
                    && !ideotypeObtainableInNextGeneration(scheme.getFinalPlantNode().getPlant().getGenotype(), scheme.getFinalPlantNode().getPlant().getGenotype())){
            // only one generation left but desired ideotype cannot be obtained by selfing the given parent scheme, so bound!
            return true;
        } else if(heuristics.pruneSelfCurrentScheme(scheme)){
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
    public boolean pruneSelfCurrentSchemeWithSelectedTarget(CrossingScheme scheme, PlantDescriptor target) {
        if(scheme.getNumGenerations() == 0 && scheme.getFinalPlantNode().getPlant().isHomozygousAtAllTargetLoci()){
            // no point in selfing homozygous initial plants, because these are supposed never to be depleted
            return true;
        } else if(pruneGrowPlantInGeneration(target.getPlant(), scheme.getNumGenerations()+1)){
            // selected target should not be grown in newly attached generation
            return true;
        } else if(heuristics.pruneSelfCurrentSchemeWithSelectedTarget(scheme, target)){
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
    public boolean pruneCurrentScheme(CrossingScheme scheme){
        if(heuristics.pruneCurrentScheme(scheme)){
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
    public boolean pruneQueueScheme(CrossingScheme scheme){
        return heuristics.pruneQueueScheme(scheme);
    }
    
    @Override
    public boolean pruneDequeueScheme(CrossingScheme scheme){
        return heuristics.pruneDequeueScheme(scheme);
    }
    
    @Override
    public boolean pruneGrowPlantFromAncestors(Set<PlantDescriptor> ancestors, PlantDescriptor p){
        return heuristics.pruneGrowPlantFromAncestors(ancestors, p);
    }
    
    @Override
    public boolean pruneGrowPlantInGeneration(Plant p, int generation){
        // never prune dummy plants
        if(p.isDummyPlant()){
            return false;
        }
        // check heuristics
        if(heuristics.pruneGrowPlantInGeneration(p, generation)){
            return true;
        } else {
            // prune if one of the following conditions holds:
            //  - plant grown in final generation and g is not the ideotype
            //  - plant grown in penultimate generation and genotype can impossibly
            //    be created from this genotype
            //  - homozygous ideotype parents required, but plant is grown in
            //    penultimate generation + is not yet the ideotype + is not homozygous
            return finalGenerationReached(generation) && !p.getGenotype().equals(ideotype)
               ||  penultimateGenerationReached(generation) && !ideotypeObtainableInNextGeneration(p.getGenotype())
               ||  homozygousIdeotypeParents && penultimateGenerationReached(generation)
                   && !p.getGenotype().equals(ideotype) && !p.getGenotype().isHomozygousAtAllContainedLoci();
        }
    }
    
    /**
     * Check whether it is possible to obtain the ideotype in the next generation,
     * by crossing the given parent with any other plant. It is checked whether for
     * every chromosome the parent can yield either the upper or lower target haplotype.
     * 
     * @param parent parental genotype
     * @return <code>true</code> if the given genotype could produce the ideotype in the next
     *         generation, when being crossed with an appropriate other genotype
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
     * parent 1 may yield the upper haplotype and parent 2 may yield the lower
     * haplotype, or vice versa.
     * 
     * @param parent1 parental genotype 1
     * @param parent2 parental genotype 2
     * @return <code>true</code> if crossing the given genotypes may produce the ideotype
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
        AllelicFrequency[] targetStates = chrom.getAllelicFrequencies().getAllelicFrequencies();
        while(obtainable && l < chrom.nrOfLoci()){
            // check locus l
            obtainable = (hap.targetPresent(l) && (targetStates[l] == AllelicFrequency.ONCE || targetStates[l] == AllelicFrequency.TWICE))
                      || (!hap.targetPresent(l) && (targetStates[l] == AllelicFrequency.NONE || targetStates[l] == AllelicFrequency.ONCE));
            l++;
        }
        return obtainable;
    }
    
    /**
     * Check whether we have reached or exceeded the maximum number of generations.
     * 
     * @param gen current generation
     * @return <code>true</code> if <code>gen</code> is greater than or equal to the maximum number of generations
     */
    public boolean finalGenerationReached(int gen){
        if(maxNumGen == null){
            return false;
        } else {
            return gen >= maxNumGen.getMaxNumGenerations();
        }
    }
    
    /**
     * Check whether we have reached or exceeded the penultimate generation.
     *
     * @param gen current generation
     * @return <code>true</code> if <code>gen</code> is greater than or equal to
     *         the maximum number of generations minus 1
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
