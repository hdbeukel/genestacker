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

package org.ugent.caagt.genestacker.search.bb.heuristics;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.ugent.caagt.genestacker.DiploidChromosome;
import org.ugent.caagt.genestacker.GeneticMap;
import org.ugent.caagt.genestacker.Genotype;
import org.ugent.caagt.genestacker.Haplotype;
import org.ugent.caagt.genestacker.ObservableGenotypeState;
import org.ugent.caagt.genestacker.SeedLot;
import org.ugent.caagt.genestacker.exceptions.GenotypeException;
import org.ugent.caagt.genestacker.search.bb.DefaultSeedLotConstructor;
import org.ugent.caagt.genestacker.search.bb.SeedLotConstructor;
import org.ugent.caagt.genestacker.util.GenestackerConstants;

/** 
 * Heuristic seed lot constructor that bounds construction of a gamete as soon as
 * a stretch has been created between two partialCross-overs which does not improve on the
 * alternative stretch at any contained locus, w.r.t one of both target haplotypes.
 * Furthermore, it is enforced that all stretches between two consecutive crossovers
 * are consistent improvements w.r.t the same target stretch (in case of two different targets).
 * 
 * @author Herman De Beukelaer <herman.debeukelaer@ugent.be>
 */
public class HeuristicSeedLotConstructor extends DefaultSeedLotConstructor {
    
    // maximum number of cross-overs
    private int maxNumCrossovers;
    
    // consistent improvement towards one of both targets required?
    private boolean consistent;
    
    // target genotype
    protected Genotype ideotype;
    
    // default constructor used for finalization
    protected SeedLotConstructor defaultConstructor;
    
    public HeuristicSeedLotConstructor(GeneticMap map, Genotype ideotype){
        this(map, ideotype, GenestackerConstants.UNLIMITED_CROSSOVERS, false);
    }
    
    public HeuristicSeedLotConstructor(GeneticMap map, Genotype ideotype, int maxNumCrossovers, boolean consistent){
        super(map);
        this.ideotype = ideotype;
        this.defaultConstructor = new DefaultSeedLotConstructor(map);
        this.maxNumCrossovers = maxNumCrossovers;
        this.consistent = consistent;
    }
    
    @Override
    protected void genChromosomeGametes(Genotype parent, Genotype otherParent, ObservableGenotypeState desiredObservation,
                                        int chromIndex, Map<Haplotype, Double> gametes, LinkedList<Boolean> curTargets,
                                        double curP, int locus, int previousHeterozygousLocus, int previousHaplotypePicked)
                                            throws GenotypeException{
        // before the first heterozygous loci is traversed, usefulCrossover is set to true
        genChromosomeGametes(parent, otherParent, desiredObservation, chromIndex, gametes, curTargets, curP, 
                locus, previousHeterozygousLocus, previousHaplotypePicked, true, true, true, true, 0);
    }
    /**
     * Construct heuristic set of 'interesting' gametes that can be obtained from a given chromosome.
     * 
     * @param curImprovementWrtUpperTarget Indicates whether the current stretch is already an improvement w.r.t the upper target
     * @param consistentImprovementWrtUpperTarget Indicates whether the previous completed stretches were consistent improvements w.r.t the upper target
     * @param curImprovementWrtLowerTarget Indicates whether the current stretch is already an improvement w.r.t the lower target
     * @param consistentImprovementWrtLowerTarget Indicates whether the previous completed stretches were consistent improvements w.r.t the lower target
     * @throws GenotypeException 
     */
    protected void genChromosomeGametes(Genotype parent, Genotype otherParent, ObservableGenotypeState desiredObservation, int chromIndex,
                                        Map<Haplotype, Double> gametes, LinkedList<Boolean> curTargets, double curP, int locus,
                                        int previousHeterozygousLocus, int previousHaplotypePicked, boolean curImprovementWrtUpperTarget,
                                        boolean consistentImprovementWrtUpperTarget, boolean curImprovementWrtLowerTarget,
                                        boolean consistentImprovementWrtLowerTarget, int curNumCrossovers) throws GenotypeException{
        
        DiploidChromosome chrom = parent.getChromosomes().get(chromIndex);
        DiploidChromosome ichrom = ideotype.getChromosomes().get(chromIndex);
        
        // check if construction complete
        if(locus >= chrom.nrOfLoci()){
            // update consistent improvement indicators based on improvement of final completed stretch
            consistentImprovementWrtUpperTarget = consistentImprovementWrtUpperTarget && curImprovementWrtUpperTarget;
            consistentImprovementWrtLowerTarget = consistentImprovementWrtLowerTarget && curImprovementWrtLowerTarget;
            // check for (consistent) iprovement of final stretch
            boolean store;
            if(consistent){
                // store if final stretch is consistent improvement
                store = consistentImprovementWrtUpperTarget || consistentImprovementWrtLowerTarget;
            } else {
                // store if final stretch improved w.r.t. any target
                store = curImprovementWrtUpperTarget || curImprovementWrtLowerTarget;
            }
            if(store){
                // store completed gamete (copy target list !!)
                List<Boolean> targetsCopy = new ArrayList<>(curTargets);
                gametes.put(new Haplotype(targetsCopy), curP);
            }
            return;
        }
        
        // continue construction: extend currently constructed part of gamete in all possible ways
        
        if(chrom.isHomozygousAtLocus(locus)){
            
            // homozygous target is immune for recombination!

            // extend current targets with the only possible option
            curTargets.add(chrom.getHaplotypes()[0].targetPresent(locus));
            // recursion (probability & usefulness of current/previous crossovers do not change)
            genChromosomeGametes(parent, otherParent, desiredObservation, chromIndex, gametes, curTargets, curP, locus+1,
                                    previousHeterozygousLocus, previousHaplotypePicked, curImprovementWrtUpperTarget,
                                    consistentImprovementWrtUpperTarget, curImprovementWrtLowerTarget,
                                    consistentImprovementWrtLowerTarget, curNumCrossovers);
            // backtracking: remove target
            curTargets.removeLast();
        } else {
                        
            // heterozygous target: create both possible extended gametes, but in case of a crossover
            // we check if the stretch created between this crossover and the previous one is useful
            // compared to the alternative stretch obtained without this pair of crossovers -- if not
            // useful, further construction is bounded
            
            for(int haplotypePicked=0; haplotypePicked <= 1; haplotypePicked++){
                
                // check if currently considered option can lead to the desired observation, if any
                if(desiredObservation == null || canYieldDesiredObservation(desiredObservation, parent, otherParent,
                                                    chromIndex, locus, haplotypePicked)){
                
                    // extend current targets with the selected option
                    curTargets.add(chrom.getHaplotypes()[haplotypePicked].targetPresent(locus));

                    // update probability: depends on recombination factors
                    double r, newP;
                    // update number of crossovers
                    int newNumCrossovers = (previousHaplotypePicked != -1 && previousHaplotypePicked != haplotypePicked) ? curNumCrossovers+1 : curNumCrossovers;

                    if(previousHeterozygousLocus == -1){
                        // first heterozygous locus in chromosome
                        r = 0.5;
                    } else {
                        r = map.getRecombinationProbability(chromIndex, previousHeterozygousLocus, locus);
                    }

                    boolean bound;
                    boolean newImprovementWrtUpperTarget, newImprovementWrtLowerTarget;
                    boolean newConsistentImprovementWrtUpperTarget, newConsistentImprovementWrtLowerTarget;
                    if(previousHaplotypePicked == haplotypePicked){
                        // previous haplotype same as current choice (no crossover in between)
                        newP = curP * (1-r);
                        // never bound a non-crossover
                        bound = false;
                        // consistent improvements of previous stretches stay unchanged (current stretch is not yet complete)
                        newConsistentImprovementWrtUpperTarget = consistentImprovementWrtUpperTarget;
                        newConsistentImprovementWrtLowerTarget = consistentImprovementWrtLowerTarget;
                        // update usefulness of current, extended stretch
                        newImprovementWrtUpperTarget = curImprovementWrtUpperTarget || improvementWrtUpperTarget(chrom, ichrom, locus, haplotypePicked);
                        newImprovementWrtLowerTarget = curImprovementWrtLowerTarget || improvementWrtLowerTarget(chrom, ichrom, locus, haplotypePicked);
                    } else {
                        // other haplotype picked (cross-over)
                        newP = curP * r;
                        // current stretch complete: update previous consistent improvement indicators
                        // to include the currently completed stretch
                        newConsistentImprovementWrtUpperTarget = consistentImprovementWrtUpperTarget && curImprovementWrtUpperTarget;
                        newConsistentImprovementWrtLowerTarget = consistentImprovementWrtLowerTarget && curImprovementWrtLowerTarget;
                        // bounding: first check if max number of crossovers exceeded
                        bound = (maxNumCrossovers != GenestackerConstants.UNLIMITED_CROSSOVERS && newNumCrossovers > maxNumCrossovers);
                        // if not exceeded: check bounding on improvement
                        if(!bound){
                            if(consistent){
                                // check for consistent improvement on all stretches towards one of both target haplotypes
                                bound = !(newConsistentImprovementWrtUpperTarget || newConsistentImprovementWrtLowerTarget);
                            } else {
                                // check for improvement of current stretch towards one of the targets
                                bound = !(curImprovementWrtUpperTarget || curImprovementWrtLowerTarget);
                            }
                        }
                        // set initial usefulness of new stretch started at this locus
                        // (checks if improvement w.r.t same target as previous stretches,
                        // including the one which was just completed in this step)
                        newImprovementWrtUpperTarget = improvementWrtUpperTarget(chrom, ichrom, locus, haplotypePicked);
                        newImprovementWrtLowerTarget = improvementWrtLowerTarget(chrom, ichrom, locus, haplotypePicked);
                    }

                    if(!bound){
                        // recursion
                        genChromosomeGametes(parent, otherParent, desiredObservation, chromIndex, gametes, curTargets, newP, locus+1, locus, haplotypePicked,
                                newImprovementWrtUpperTarget, newConsistentImprovementWrtUpperTarget, newImprovementWrtLowerTarget, newConsistentImprovementWrtLowerTarget,
                                newNumCrossovers);
                    }

                    // backtracking: remove last target
                    curTargets.removeLast();
                    
                }
                
            }
            
        }
        
    }
    
    /**
     * Returns true if and only if the picked haplotype of the current chromosome offers an improvement
     * at the current locus w.r.t the upper target haplotype at this chromosome.
     * 
     * @param chrom current chromosome
     * @param ichrom respective chromosome of target genotype (ideotype)
     * @param locus current locus
     * @param haplotypePicked current picked haplotype (0/1)
     */
    private boolean improvementWrtUpperTarget(DiploidChromosome chrom, DiploidChromosome ichrom, int locus, int haplotypePicked){
        return (chrom.getHaplotypes()[haplotypePicked].targetPresent(locus) == ichrom.getHaplotypes()[0].targetPresent(locus)
                 && chrom.getHaplotypes()[1-haplotypePicked].targetPresent(locus) != ichrom.getHaplotypes()[0].targetPresent(locus));
    }
    
    /**
     * Returns true if and only if the picked haplotype of the current chromosome offers an improvement
     * at the current locus w.r.t the lower target haplotype at this chromosome.
     * 
     * @param chrom current chromosome
     * @param ichrom respective chromosome of target genotype (ideotype)
     * @param locus current locus
     * @param haplotypePicked current picked haplotype (0/1)
     */
    private boolean improvementWrtLowerTarget(DiploidChromosome chrom, DiploidChromosome ichrom, int locus, int haplotypePicked){
        return (chrom.getHaplotypes()[haplotypePicked].targetPresent(locus) == ichrom.getHaplotypes()[1].targetPresent(locus)
                 && chrom.getHaplotypes()[1-haplotypePicked].targetPresent(locus) != ichrom.getHaplotypes()[1].targetPresent(locus));
    }
    
    @Override
    protected SeedLot genSeedLotFromGenotypes(Genotype parent1, Genotype parent2, Map<Genotype, Double> genotypeProbs){
        try {
            // after the interesting genotypes have been heuristically generated, apply
            // the default seed lot construction to construct the full genotype groups of only
            // those genotypes sharing their observation with one of the interesting genotypes,
            // to allow for an accurate computation of group probabilities and especially linkage phase ambiguity
            SeedLot sl = defaultConstructor.partialCross(parent1, parent2, genotypeProbs.keySet());
            // now that computation of LPA/probabilities is complete, again filter the undesired genotypes
            for(Genotype g : sl.getGenotypes()){
                if(!genotypeProbs.containsKey(g)){
                    // not desired
                    sl.filterGenotype(g);
                }
            }
            // return final seed lot
            return sl;
        } catch (GenotypeException shouldNotHappen) {
            // should never happen as the same parents have already been successfully crossed
            throw new RuntimeException("[SHOULD NOT HAPPEN] Error in heuristic seed lot generator (should never happen, this is a bug!)", shouldNotHappen);            
        }
    }

}
