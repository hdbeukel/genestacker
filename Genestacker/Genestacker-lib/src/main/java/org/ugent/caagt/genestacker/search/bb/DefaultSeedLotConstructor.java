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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.ugent.caagt.genestacker.DiploidChromosome;
import org.ugent.caagt.genestacker.GeneticMap;
import org.ugent.caagt.genestacker.Genotype;
import org.ugent.caagt.genestacker.Haplotype;
import org.ugent.caagt.genestacker.IndistinguishableGenotypeGroup;
import org.ugent.caagt.genestacker.ObservableDiploidChromosomeState;
import org.ugent.caagt.genestacker.ObservableDiploidTargetState;
import org.ugent.caagt.genestacker.ObservableGenotypeState;
import org.ugent.caagt.genestacker.SeedLot;
import org.ugent.caagt.genestacker.exceptions.GenotypeException;
import org.ugent.caagt.genestacker.exceptions.IncompatibleGeneticMapException;
import org.ugent.caagt.genestacker.exceptions.IncompatibleGenotypesException;

/**
 * Default seed lot constructor that creates the entire seed lot obtained from
 * a given crossing or selfing of plants, containing all possible child genotypes
 * with their respective probability and linkage phase ambiguity.
 * 
 * @author Herman De Beukelaer <herman.debeukelaer@ugent.be>
 */
public class DefaultSeedLotConstructor extends SeedLotConstructor {
    
    public DefaultSeedLotConstructor(GeneticMap map){
        super(map);
    }
    
    private void checkCompatibility(Genotype g1, Genotype g2) throws IncompatibleGenotypesException, IncompatibleGeneticMapException{
        // check if genotypes are compatible for crossing
        if(!g1.compatibleWith(g2)){
            throw new IncompatibleGenotypesException("Attempted to cross incompatible genotypes");
        }
        // check compatibility with genetic map
        boolean compatibleWithMap = (g1.nrOfChromosomes() == map.nrOfChromosomes());
        int i=0;
        while(compatibleWithMap && i<g1.nrOfChromosomes()){
            compatibleWithMap = (g1.getChromosomes().get(i).nrOfLoci() == map.nrOfLociOnChromosome(i));
            i++;
        }
        if(!compatibleWithMap){
            throw new IncompatibleGeneticMapException("Given genetic map is not compatible with genotype structure");
        }
    }
    
    /**
     * Generate all gametes that can be produced by a single chromosome of a given genotype.
     */
    protected void genChromosomeGametes(Genotype parent, Genotype otherParent, ObservableGenotypeState desiredObservation,
                                        int chromIndex, Map<Haplotype, Double> gametes, LinkedList<Boolean> curTargets,
                                        double curP, int locus, int previousHeterozygousLocus, int previousHaplotypePicked)
                                            throws GenotypeException{
        
        DiploidChromosome chrom = parent.getChromosomes().get(chromIndex);
        
        // check if construction complete
        if(locus >= chrom.nrOfLoci()){
            // store completed gamete (copy target list !!)
            List<Boolean> targetsCopy = new ArrayList<>(curTargets);
            gametes.put(new Haplotype(targetsCopy), curP);
            return;
        }
        
        // continue construction: extend currently constructed part of gamete in all possible ways
        
        if(chrom.isHomozygousAtLocus(locus)){
            
            // homozygous target is immune for recombination!

            // extend current targets with the only possible option
            curTargets.add(chrom.getHaplotypes()[0].targetPresent(locus));
            // recursion (probability does not change)
            genChromosomeGametes(parent, otherParent, desiredObservation, chromIndex, gametes, curTargets, curP, locus+1,
                                    previousHeterozygousLocus, previousHaplotypePicked);
            // backtracking: remove target
            curTargets.removeLast();
        } else {
                        
            // heterozygous target: create both possible extended gametes
            // (if desired observation is given, one option might be ignored)
            
            for(int haplotypePicked=0; haplotypePicked <= 1; haplotypePicked++){
                
                // check if currently considered option can lead to the desired observation, if any
                if(desiredObservation == null || canYieldDesiredObservation(desiredObservation, parent, otherParent,
                                                    chromIndex, locus, haplotypePicked)){
                
                    // extend current targets with the selected option
                    curTargets.add(chrom.getHaplotypes()[haplotypePicked].targetPresent(locus));

                    // update probability: depends on recombination factors
                    double r, newP;

                    if(previousHeterozygousLocus == -1){
                        // first heterozygous locus in chromosome
                        r = 0.5;
                    } else {
                        r = map.getRecombinationProbability(chromIndex, previousHeterozygousLocus, locus);
                    }

                    if(previousHaplotypePicked == haplotypePicked){
                        // previous haplotype same as current choice (no partialCross-over in between)
                        newP = curP * (1-r);
                    } else {
                        // other haplotype picked (partialCross-over)
                        newP = curP * r;
                    }

                    // recursion
                    genChromosomeGametes(parent, otherParent, desiredObservation, chromIndex, gametes, curTargets, newP, locus+1, locus, haplotypePicked);
                    // backtracking: remove last target
                    curTargets.removeLast();
                
                }
                
            }
            
        }
        
    }
    
    /**
     * Check if the current option may yield the desired observation, taking into account
     * the genotype of both parents. Will return true if and only if one of the following 
     * cases holds:
     * 
     *  - The observation contains the target TWICE at this locus and the current option also contains the target gene
     *  - The observation does not contain the target gene and neither does the current option
     *  - The observation contains the target gene ONCE at this locus, and one of these sub-cases holds:
     *          - The other parent contains the target TWICE and the current option does not contain the target
     *          - The other parent does not contain the target but the current options does
     *          - The other parent contains the target ONCE and the current option either does or does not contain
     *            the target (both are allowed, this is the only cases where there will be a branching during gamete generation)
     * 
     * @param desiredObs
     * @param parent
     * @param otherParent
     * @param chromIndex
     * @param locus
     * @return 
     */
    protected boolean canYieldDesiredObservation(ObservableGenotypeState desiredObs, Genotype parent, Genotype otherParent,
                                                    int chromIndex, int locus, int haplotypePicked){
        
        ObservableDiploidTargetState desiredLocusObs = desiredObs.getObservableChromosomeStates().get(chromIndex).getTargetStates()[locus];
        DiploidChromosome parentChrom = parent.getChromosomes().get(chromIndex);
        ObservableDiploidTargetState otherParentLocusObs = otherParent.getChromosomes().get(chromIndex).getObservableState().getTargetStates()[locus];
        
        return   ( desiredLocusObs == ObservableDiploidTargetState.TWICE && parentChrom.getHaplotypes()[haplotypePicked].targetPresent(locus)
                || desiredLocusObs == ObservableDiploidTargetState.NONE && !parentChrom.getHaplotypes()[haplotypePicked].targetPresent(locus)
                || desiredLocusObs == ObservableDiploidTargetState.ONCE &&
                    (
                           otherParentLocusObs == ObservableDiploidTargetState.TWICE && !parentChrom.getHaplotypes()[haplotypePicked].targetPresent(locus)
                        || otherParentLocusObs == ObservableDiploidTargetState.NONE && parentChrom.getHaplotypes()[haplotypePicked].targetPresent(locus)
                        || otherParentLocusObs == ObservableDiploidTargetState.ONCE
                    ));
        
    }
        
    /**
     * Combine possible chromosomes to create the set of all possible genotypes, with their respective probability.
     * If the map with complete genotypes already contains some entries upon calling this method, these will be retained
     * and new genotypes will be added to the map.
     */
    protected void combineChromosomes(List<Map<DiploidChromosome, Double>> possibleChromosomes, int chromIndex, double curP,
                                        LinkedList<DiploidChromosome> curGenotype, Map<Genotype, Double> completeGenotypes){
        // check if complete
        if(chromIndex >= possibleChromosomes.size()){
            // create genotype
            List<DiploidChromosome> chroms = new ArrayList<>(curGenotype);
            Genotype g = new Genotype(chroms);
            completeGenotypes.put(g, curP);
            return;
        }
        
        // continue construction: consider each of the possible chromosomes for the current chrom index
        Map<DiploidChromosome, Double> chromOptions = possibleChromosomes.get(chromIndex);
        for(DiploidChromosome chrom : chromOptions.keySet()){
            // add chromosome to genotype
            curGenotype.add(chrom);
            // recursive call for further construction (with updated probability)
            combineChromosomes(possibleChromosomes, chromIndex+1, curP * chromOptions.get(chrom), curGenotype, completeGenotypes);
            // backtracking: remove last chromosome
            curGenotype.removeLast();
        }
    }
    
    /**
     * Creates the seed lot containing all generated genotypes by grouping them according
     * to their observable state, and computes the probabilities and ambiguities (of each group).
     * This assumes that for any created genotype, the entire group of genotypes sharing 
     * the same observation has been generated. This is assured in the default implementations
     * of both cross and partialCross. Else, the computations may be (strongly) inexact.
     * 
     * @param parent1
     * @param parent2
     * @param genotypeProbs
     * @return 
     */
    protected SeedLot genSeedLotFromGenotypes(Genotype parent1, Genotype parent2, Map<Genotype, Double> genotypeProbs){
        
        // group genotypes by observable state and compute probability of each genotype group:
        Map<ObservableGenotypeState, Map<Genotype, Double>> groups = new HashMap<>();
        Map<ObservableGenotypeState, Double> observableStateProbs = new HashMap<>();
        for(Genotype g : genotypeProbs.keySet()){
            ObservableGenotypeState state = g.getObservableState();
            if(groups.containsKey(state)){
                // group already registered
                groups.get(state).put(g, genotypeProbs.get(g));
                observableStateProbs.put(state, observableStateProbs.get(state) + genotypeProbs.get(g));
            } else {
                // first genotype from group
                Map<Genotype, Double> genotypeMap = new HashMap<>();
                genotypeMap.put(g, genotypeProbs.get(g));
                groups.put(state, genotypeMap);
                observableStateProbs.put(state, genotypeProbs.get(g));
            }
        }
        
        // create seed lot
        Map<ObservableGenotypeState, IndistinguishableGenotypeGroup> genotypeGroups = new HashMap<>();
        for(ObservableGenotypeState state : groups.keySet()){
            // pack genotype group for registration in seed lot
            genotypeGroups.put(state, new IndistinguishableGenotypeGroup(observableStateProbs.get(state), state, groups.get(state)));
        }
        // uniform seed lot if both parents are fully homozygous
        boolean uniform = parent1.isHomozygousAtAllTargetLoci() && parent2.isHomozygousAtAllTargetLoci();
        return new SeedLot(uniform, genotypeGroups);
        
    }
    
    public List<Map<Haplotype, Double>> genGametesPerChromosome(Genotype parent) throws GenotypeException{
        return genGametesPerChromosome(parent, null, null);
    }
    
    protected List<Map<Haplotype, Double>> genGametesPerChromosome(Genotype parent, Genotype otherParent, ObservableGenotypeState desiredObservation)
                                                                                throws GenotypeException{
        List<Map<Haplotype, Double>> gametesPerChromosome = new ArrayList<>();
        // generate chromosome gametes
        for(int c=0; c<parent.nrOfChromosomes(); c++){
            // generate gametes of chromosome c of g1
            LinkedList<Boolean> curTargets = new LinkedList<>();
            Map<Haplotype, Double> chromGametes = new HashMap<>();
            // recursive backtracking
            genChromosomeGametes(parent, otherParent, desiredObservation, c, chromGametes, curTargets, 1.0, 0, -1, -1);
            // store chromosome gametes
            gametesPerChromosome.add(chromGametes);
        }
        // return chromosome gametes
        return gametesPerChromosome;
    }
        
    /**
     * Generate entire seed lot created by crossing two given genotypes.
     * 
     * @param g1
     * @param g2
     * @throws GenotypeException  
     */
    @Override
    public SeedLot cross(Genotype g1, Genotype g2) throws GenotypeException {
        
        checkCompatibility(g1, g2);
        
        // get possible haplotypes per chromosome of g1
        List<Map<Haplotype, Double>> gametesPerChromosome1 = cachedGametesPerChrom.get(g1);
        if(gametesPerChromosome1 == null){
            gametesPerChromosome1 = genGametesPerChromosome(g1);
            // store in cache
            cachedGametesPerChrom.put(g1, gametesPerChromosome1);
        }
        // repeat for g2
        List<Map<Haplotype, Double>> gametesPerChromosome2 = cachedGametesPerChrom.get(g2);
        if(gametesPerChromosome2 == null){
            gametesPerChromosome2 = genGametesPerChromosome(g2);
            // store in cache
            cachedGametesPerChrom.put(g2, gametesPerChromosome2);
        }
        
        // create possible diploid chromosomes by comining respective haplotypes per chromosome
        // (take into account possible symmetry when both haplotypes of a chromosome may have been
        // produced by both parents)
        
        List<Map<DiploidChromosome, Double>> diploidChromsList = new ArrayList<>();
        for(int c=0; c<g1.nrOfChromosomes(); c++){
            // create all possible combinations for chromosome at index c
            Map<DiploidChromosome, Double> diploidChroms = new HashMap<>();
            for(Map.Entry<Haplotype, Double> h1 : gametesPerChromosome1.get(c).entrySet()){
                for(Map.Entry<Haplotype, Double> h2 : gametesPerChromosome2.get(c).entrySet()){
                    DiploidChromosome dipChrom = new DiploidChromosome(h1.getKey(), h2.getKey());
                    // compute probability of this new combination
                    double newP = h1.getValue() * h2.getValue();
                    if(diploidChroms.containsKey(dipChrom)){
                        // symmetric version already occured: increase prob
                        diploidChroms.put(dipChrom, diploidChroms.get(dipChrom) + newP);
                    } else {
                        // first occurence: set prob
                        diploidChroms.put(dipChrom, newP);
                    }                    
                }
            }
            // store map in list
            diploidChromsList.add(diploidChroms);
        }
        
        // finally combine chromosomes to create possible genotypes
        Map<Genotype, Double> offspring = new HashMap<>();
        combineChromosomes(diploidChromsList, 0, 1.0, new LinkedList<DiploidChromosome>(), offspring);
        
        // generate seed lot from these genotypes
        return genSeedLotFromGenotypes(g1, g2, offspring);
        
    }
    
    /**
     * Generate PART of the seed lot from crossing two given parent genotypes, restricted
     * to the set of desired child genotypes. This will also compute the probability of
     * genotypes with the same observation as one of the desired children to allow computation
     * of the respective linkage phase ambiguities. Note: the cache is not used here because these
     * computations are not general.
     * 
     * @param g1
     * @param g2
     * @param childGenotypes
     * @throws GenotypeException  
     */
    @Override
    public SeedLot partialCross(Genotype g1, Genotype g2, Set<Genotype> childGenotypes) throws GenotypeException {
        
        // NOTE: do not use the cache here! (not general)

        checkCompatibility(g1, g2);

        // convert to set of genotype observations
        Set<ObservableGenotypeState> observations = new HashSet<>();
        for(Genotype g : childGenotypes){
            observations.add(g.getObservableState());
        }
        
        // generate all genotypes for each observation (mutually exclusive)
        Map<Genotype, Double> offspring = new HashMap<>();
        LinkedList<DiploidChromosome> curDipChromComb = new LinkedList<>();
        for(ObservableGenotypeState obs : observations){
            // get possible haplotypes per chromosome of g1 w.r.t desired observation (do not use the cache)
            List<Map<Haplotype, Double>> gametesPerChromosome1 = genGametesPerChromosome(g1, g2, obs);
            // for each chromosome: combine all options with the complementary haplotype from g2,
            // creating a possible diploid chromosome with the desired observation
            List<Map<DiploidChromosome, Double>> diploidChromsList = new ArrayList<>();
            for(int c=0; c<g1.nrOfChromosomes(); c++){
                // create all possible chromosomes at index c
                Map<DiploidChromosome, Double> diploidChroms = new HashMap<>();
                // go through haplotype options of g1 at chrom c and compute complementary haplotype of g2
                for(Map.Entry<Haplotype, Double> h1 : gametesPerChromosome1.get(c).entrySet()){
                    // compute complementary haplotype from g2
                    List<Boolean> complementaryHaplotype = new ArrayList<>();
                    double h2p = createComplementaryHaplotype(c, h1.getKey(), obs.getObservableChromosomeStates().get(c), g2, complementaryHaplotype);
                    // combine haplotypes to create diploid chromosome
                    DiploidChromosome dipChrom = new DiploidChromosome(h1.getKey(), new Haplotype(complementaryHaplotype));
                    // compute probability of this new combination
                    double newP = h1.getValue() * h2p;
                    if(diploidChroms.containsKey(dipChrom)){
                        // symmetric version already occured: increase prob
                        diploidChroms.put(dipChrom, diploidChroms.get(dipChrom) + newP);
                    } else {
                        // first occurence: set prob
                        diploidChroms.put(dipChrom, newP);
                    }
                }
                // store map in list
                diploidChromsList.add(diploidChroms);
            }
            // finally combine chromosomes to create possible genotypes (adding these to already computed genotypes for other observations)
            curDipChromComb.clear();
            combineChromosomes(diploidChromsList, 0, 1.0, curDipChromComb, offspring);
        }
        
        // generate seed lot from these genotypes
        return genSeedLotFromGenotypes(g1, g2, offspring);
        
    }
    
    /**
     * Create a new haplotype from the indicated chromosome of the given parent genotype, which is complementary to the given haplotype
     * (coming from the other parent), meaning that the combination of both haplotypes gives the desired chromosome observation.
     * The list of targets is filled and the probability of obtaining this complementary haplotype is returned. It is assumed that
     * the desired observation can indeed be created from a combination of the already constructed haplotype and a complementary haplotype
     * of this parent, else the behavior of this method is undefined.
     */
    private double createComplementaryHaplotype(int chromIndex, Haplotype curHap, ObservableDiploidChromosomeState desiredObs,
                                                    Genotype parent, List<Boolean> complementaryHaplotype) throws GenotypeException{
        double p = 1.0;
        DiploidChromosome parentChrom = parent.getChromosomes().get(chromIndex);
        // go through loci on chromosome
        int prevHeterozygousLocus = -1;
        int prevHaplotypePicked = -1;
        for(int l=0; l<parentChrom.nrOfLoci(); l++){
            // add next target to gamete chromosome
            if(parentChrom.isHomozygousAtLocus(l)){
                // homozygous: simply add next target
                complementaryHaplotype.add(parentChrom.getHaplotypes()[0].targetPresent(l));
            } else {
                // heterozygous: compute desired complementary target
                boolean complementaryTarget = (!curHap.targetPresent(l) && desiredObs.getTargetStates()[l] == ObservableDiploidTargetState.ONCE
                                            || curHap.targetPresent(l) && desiredObs.getTargetStates()[l] == ObservableDiploidTargetState.TWICE);
                // select the complementary target from the parent's chromosome (and update probability)
                complementaryHaplotype.add(complementaryTarget);
                double r;
                if(prevHeterozygousLocus == -1){
                    // first heterozygous locus in chromosome
                    r = 0.5;
                } else {
                    r = map.getRecombinationProbability(chromIndex, prevHeterozygousLocus, l);
                }
                if(parentChrom.getHaplotypes()[0].targetPresent(l) == complementaryTarget){
                    // complementary target at upper haplotype of parent
                    if(prevHaplotypePicked == 0){
                        // no cross-over
                        p *= (1-r);
                    } else {
                        // cross-over
                        p *= r;
                    }
                    prevHaplotypePicked = 0;
                } else {
                    // complementary target at lower haplotype
                    if(prevHaplotypePicked == 1){
                        // no cross-over
                        p *= (1-r);
                    } else {
                        // cross-over
                        p *= r;
                    }
                    prevHaplotypePicked = 1;
                }
                prevHeterozygousLocus = l;
            }
        }
        // return probability
        return p;
    }
    
}
