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
import org.ugent.caagt.genestacker.GenotypeGroupWithSameAllelicFrequencies;
import org.ugent.caagt.genestacker.ChromosomeAllelicFrequencies;
import org.ugent.caagt.genestacker.AllelicFrequency;
import org.ugent.caagt.genestacker.GenotypeAllelicFrequencies;
import org.ugent.caagt.genestacker.SeedLot;
import org.ugent.caagt.genestacker.exceptions.GenotypeException;
import org.ugent.caagt.genestacker.exceptions.IncompatibleGeneticMapException;
import org.ugent.caagt.genestacker.exceptions.IncompatibleGenotypesException;

/**
 * Default seed lot constructor that creates the seed lot obtained from
 * a given crossing or selfing, containing all possible child genotypes
 * with their respective probability and linkage phase ambiguity.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
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
     * Recursively generate all haplotypes that can be produced by a single chromosome of a given genotype.
     * 
     * @param parent parental genotype
     * @param chromIndex index of chromosome for which the possible gametes have to be computed
     * @param haplotypes currently constructed haplotypes, extended during recursion
     * @param curHaplotype current haplotype under construction, extended during recursion
     * @param curP current probability of gamete under construction, updated during recursion
     * @param locus index of current considered locus in chromosome
     * @param previousHeterozygousLocus index of last heterozygous locus before the current locus, -1 if none
     * @param previousHaplotypePicked index of haplotype (0/1) of the considered chromosome that passed on its
     *                                allele to the constructed gamete at the last heterozygous locus before the
     *                                current locus, -1 if none
     * @param desiredAllelicFreqs optional: only construct gametes that may yield the desired allelic frequencies
     *                            when crossing <code>parent</code> with <code>otherParent</code>; may be null
     * @param otherParent optional: other genotype with which <code>parent</code> will be crossed; should
     *                    only be provided if <code>desiredAllelicFreqs</code> is stated, and will then be
     *                    used to skip options that cannot yield the desired allelic frequencies when crossing
     *                    with this other genotype
     * @throws GenotypeException if anything goes wrong when construction the haplotypes of the generated gametes
     */
    protected void genChromosomeGametes(Genotype parent, Genotype otherParent, GenotypeAllelicFrequencies desiredAllelicFreqs,
                                        int chromIndex, Map<Haplotype, Double> haplotypes, LinkedList<Boolean> curHaplotype,
                                        double curP, int locus, int previousHeterozygousLocus, int previousHaplotypePicked)
                                            throws GenotypeException{
        
        DiploidChromosome chrom = parent.getChromosomes().get(chromIndex);
        
        // check if construction complete
        if(locus >= chrom.nrOfLoci()){
            // store completed gamete (copy haplotype !!)
            List<Boolean> hapCopy = new ArrayList<>(curHaplotype);
            haplotypes.put(new Haplotype(hapCopy), curP);
            return;
        }
        
        // continue construction: extend currently constructed part of gamete in all possible ways
        
        if(chrom.isHomozygousAtLocus(locus)){
            
            // homozygous target is immune for recombination!

            // extend current targets with the only possible option
            curHaplotype.add(chrom.getHaplotypes()[0].targetPresent(locus));
            // recursion (probability does not change)
            genChromosomeGametes(parent, otherParent, desiredAllelicFreqs, chromIndex, haplotypes, curHaplotype, curP, locus+1,
                                    previousHeterozygousLocus, previousHaplotypePicked);
            // backtracking: remove target
            curHaplotype.removeLast();
        } else {
                        
            // heterozygous target: create both possible extended gametes
            // (if desired observation is given, one option might be ignored)
            
            for(int haplotypePicked=0; haplotypePicked <= 1; haplotypePicked++){
                
                // check if currently considered option can lead to the desired observation, if any,
                // through a crossing of the considered genotype with the given other genotype
                if(desiredAllelicFreqs == null
                        || canYieldDesiredObservation(
                                desiredAllelicFreqs.getChromosomeAllelicFrequencies().get(chromIndex)
                                                   .getAllelicFrequencies()[locus],
                                otherParent.getAllelicFrequencies().getChromosomeAllelicFrequencies()
                                           .get(chromIndex).getAllelicFrequencies()[locus],
                                chrom.getHaplotypes()[haplotypePicked].targetPresent(locus)
                        )){
                
                    // extend current targets with the selected option
                    curHaplotype.add(chrom.getHaplotypes()[haplotypePicked].targetPresent(locus));

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
                    genChromosomeGametes(parent, otherParent, desiredAllelicFreqs, chromIndex, haplotypes,
                                                        curHaplotype, newP, locus+1, locus, haplotypePicked);
                    // backtracking: remove last target
                    curHaplotype.removeLast();
                
                }
                
            }
            
        }
        
    }
    
    /**
     * Check if the selected allele at a specific locus of the considered chromosome (either the allele of the top or
     * bottom haplotype) may yield the desired allelic frequencies, when the parental genotype is crossed with a fixed
     * other genotype. Will return <code>true</code> if and only if one of the following cases holds:
     * <ul>
     *  <li>Frequency TWICE is desired at this locus, and the selected allele is 1.</li>
     *  <li>Frequency NONE is desired at this locus, and the selected allele is 0.</li>
     *  <li>Frequency ONCE is desired at this locus, and one of these sub-cases holds:
     *      <ul>
     *          <li>
     *              The other parent has frequency TWICE at this locus and the selected allele is 0.
     *          </li>
     *          <li>
     *              The other parent has frequency NONE at this locus and the selected allele is 1.
     *          </li>
     *          <li>
     *              The other parent has frequency ONCE at this locus, regardless of the selected allele
     *              (both 0 and 1 are allowed here, which will cause branching during recursive construction
     *              of gametes).
     *          </li>
     *      </ul>
     *  </li>
     * </ul>
     * 
     * @param desiredAllelicFrequency desired allelic frequency at the considered locus
     * @param otherParentAllelicFrequency allelic frequency of other fixed parental genotype
     * @param selectedAllele currently selected allele (allele of either top or bottom haplotype
     *                       at the considered locus of the considered chromosome of the parental
     *                       genotype for which gametes are being constructed)
     * @return <code>true</code> if the selected allele may yield the desired frequency, taking into
     *         account the allelic frequency of the other parent at the considered locus
     */
    protected boolean canYieldDesiredObservation(AllelicFrequency desiredAllelicFrequency,
                                                 AllelicFrequency otherParentAllelicFrequency,
                                                 boolean selectedAllele){
                
        return   ( desiredAllelicFrequency == AllelicFrequency.TWICE && selectedAllele
                || desiredAllelicFrequency == AllelicFrequency.NONE && !selectedAllele
                || desiredAllelicFrequency == AllelicFrequency.ONCE &&
                    (
                           otherParentAllelicFrequency == AllelicFrequency.TWICE && !selectedAllele
                        || otherParentAllelicFrequency == AllelicFrequency.NONE && selectedAllele
                        || otherParentAllelicFrequency == AllelicFrequency.ONCE
                    ));
        
    }
        
    /**
     * Recursively combine possible chromosomes to create the set of all possible genotypes, with their respective
     * probability. If the map with complete genotypes already contains some entries upon calling this method, these
     * will be retained and new genotypes will be added to the map.
     * 
     * @param possibleChromosomes list containing possible outcomes (and the respective probability) per chromosome
     * @param chromIndex index of chromosome for which each option is considered through recursion
     * @param curP current probability of the complete genotype, updated during recursion
     * @param curGenotype currently constructed complete genotype, extended during recursion by adding more chromosomes
     * @param completeGenotypes set of complete genotypes resulting from the combination of possible chromosomes,
     *                          extended during recursion
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
     * Creates the seed lot containing all generated genotypes by grouping them according to their allelic
     * frequencies, and computes all probabilities and linkage phase ambiguities (per group). This assumes
     * that for any created genotype, the entire group of genotypes sharing  the same allelic frequencies
     * has been generated. This is assured in the default implementations of both {@link #cross(Genotype, Genotype)}
     * and {@link #partialCross(Genotype, Genotype, Set)}.
     * 
     * @param parent1 parental genotype 1
     * @param parent2 parental genotype 2
     * @param genotypes all genotypes (and corresponding probabilities) that may be produced by crossing
     *                  the given parental genotypes
     * @return a seed lot modelling all possible genotypes, grouped according to shared allelic frequencies,
     *         including all probabilities and linkage phase ambiguities
     */
    protected SeedLot genSeedLotFromGenotypes(Genotype parent1, Genotype parent2, Map<Genotype, Double> genotypes){
        
        // group genotypes by observable state and compute probability of each genotype group:
        Map<GenotypeAllelicFrequencies, Map<Genotype, Double>> groups = new HashMap<>();
        Map<GenotypeAllelicFrequencies, Double> observableStateProbs = new HashMap<>();
        for(Genotype g : genotypes.keySet()){
            GenotypeAllelicFrequencies state = g.getAllelicFrequencies();
            if(groups.containsKey(state)){
                // group already registered
                groups.get(state).put(g, genotypes.get(g));
                observableStateProbs.put(state, observableStateProbs.get(state) + genotypes.get(g));
            } else {
                // first genotype from group
                Map<Genotype, Double> genotypeMap = new HashMap<>();
                genotypeMap.put(g, genotypes.get(g));
                groups.put(state, genotypeMap);
                observableStateProbs.put(state, genotypes.get(g));
            }
        }
        
        // create seed lot
        Map<GenotypeAllelicFrequencies, GenotypeGroupWithSameAllelicFrequencies> genotypeGroups = new HashMap<>();
        for(GenotypeAllelicFrequencies state : groups.keySet()){
            // pack genotype group for registration in seed lot
            genotypeGroups.put(state, new GenotypeGroupWithSameAllelicFrequencies(observableStateProbs.get(state), state, groups.get(state)));
        }
        // uniform seed lot if both parents are fully homozygous
        boolean uniform = parent1.isHomozygousAtAllContainedLoci() && parent2.isHomozygousAtAllContainedLoci();
        return new SeedLot(uniform, genotypeGroups);
        
    }
    
    /**
     * Generate the possible gametes that may be produced from each chromosome of the given parental genotype.
     * 
     * @param parent parental genotype
     * @return collection of possible gametes that may be produced from each chromosome of the given
     *         parental genotype, with their probabilities
     * @throws GenotypeException if anything goes wrong while creating the possible gametes
     */
    public List<Map<Haplotype, Double>> genGametesPerChromosome(Genotype parent) throws GenotypeException{
        return genGametesPerChromosome(parent, null, null);
    }
    
    /**
     * Generate the possible gametes that may be produced from each chromosome of the given parental genotype,
     * and that can yield the desired allelic frequencies when being combined with a gamete produced by the given
     * other genotype. This is used to obtain partial seed lots containing information about a predefined subset
     * of the possible offspring of a crossing with these two genotypes.
     * 
     * @param parent parental genotype
     * @param otherParent other genotype
     * @param desiredAllelicFreqs desired allelic frequencies
     * @return subset of possible gametes (and corresponding probabilities) produced per chromosome of
     *         <code>parent</code>, so that the desired allelic frequencies may be obtained when combining
     *         the constructed gametes with a gamete produced by the respective chromosome of <code>otherParent</code>
     * @throws GenotypeException if anything goes wrong while creating the possible gametes
     */
    protected List<Map<Haplotype, Double>> genGametesPerChromosome(Genotype parent, Genotype otherParent,
                                                                   GenotypeAllelicFrequencies desiredAllelicFreqs)
                                                                                throws GenotypeException{
        List<Map<Haplotype, Double>> gametesPerChromosome = new ArrayList<>();
        // generate chromosome gametes
        for(int c=0; c<parent.nrOfChromosomes(); c++){
            // generate gametes of chromosome c of g1
            LinkedList<Boolean> curTargets = new LinkedList<>();
            Map<Haplotype, Double> chromGametes = new HashMap<>();
            // recursive backtracking
            genChromosomeGametes(parent, otherParent, desiredAllelicFreqs, c, chromGametes, curTargets, 1.0, 0, -1, -1);
            // store chromosome gametes
            gametesPerChromosome.add(chromGametes);
        }
        // return chromosome gametes
        return gametesPerChromosome;
    }
        
    /**
     * Generate entire seed lot obtained by crossing two given genotypes.
     * 
     * @param g1 genotype 1
     * @param g2 genotype 2
     * @return entire seed lot obtained by crossing the two given genotypes
     * @throws GenotypeException  if anything goes wrong while creating the seed lot
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
     * Generate PART of the seed lot obtained by crossing two given genotypes, confined to a predefined
     * set of genotypes among the offspring for which properties (LPA, probability) are to be inferred.
     * For every genotype in <code>childGenotypes</code> the entire group of genotypes among the offspring
     * with the same allelic frequencies as this genotype will be generated, so that linkage phase ambiguities
     * can be inferred. Other genotype groups are not generated.
     * 
     * @param g1 genotype 1
     * @param g2 genotype 2
     * @param childGenotypes genotypes among the offspring, obtained by crossing the two given genotypes,
     *                       for which properties (LPA, probability) are to be inferred
     * @throws GenotypeException if anything goes wrong while creating the seed lot
     */
    @Override
    public SeedLot partialCross(Genotype g1, Genotype g2, Set<Genotype> childGenotypes) throws GenotypeException {
        
        // NOTE: do not use the cache here! (not general)

        checkCompatibility(g1, g2);

        // convert to set of genotype observations
        Set<GenotypeAllelicFrequencies> observations = new HashSet<>();
        for(Genotype g : childGenotypes){
            observations.add(g.getAllelicFrequencies());
        }
        
        // generate all genotypes for each observation (mutually exclusive)
        Map<Genotype, Double> offspring = new HashMap<>();
        LinkedList<DiploidChromosome> curDipChromComb = new LinkedList<>();
        for(GenotypeAllelicFrequencies obs : observations){
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
                    double h2p = createComplementaryHaplotype(c, h1.getKey(), obs.getChromosomeAllelicFrequencies().get(c), g2, complementaryHaplotype);
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
     * Create a new haplotype from the indicated chromosome of the given parent genotype, which is complementary to
     * the given haplotype (produced by the other parent), in the sense that the combination of both haplotypes gives
     * the desired allelic frequencies. The list <code>complementaryHaplotype</code> is filled during execution of
     * this method and the probability of obtaining this complementary haplotype is returned. It is assumed that
     * the desired observation can indeed be created from a combination of the already constructed haplotype and
     * a complementary haplotype produced by this parent; else, the behaviour of this method is undefined.
     * 
     * @param parent parent genotype
     * @param chromIndex index of considered chromosome of parent genotype, from which a haplotype is produced
     * @param curHap already obtained haplotype, produced by the other parent
     * @param desiredAllelicFreqs desired allelic frequencies when combining <code>curHap</code> with the haplotype
     *                            produced by this method, from the considered chromosome of the given genotype
     * @param complementaryHaplotype produced complementary haplotype, filled during execution of this method
     * @return probability with which the constructed complementary haplotype is produced
     */
    private double createComplementaryHaplotype(int chromIndex, Haplotype curHap, ChromosomeAllelicFrequencies desiredAllelicFreqs,
                                                            Genotype parent, List<Boolean> complementaryHaplotype){
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
                boolean complementaryTarget = (!curHap.targetPresent(l) && desiredAllelicFreqs.getAllelicFrequencies()[l] == AllelicFrequency.ONCE
                                            || curHap.targetPresent(l) && desiredAllelicFreqs.getAllelicFrequencies()[l] == AllelicFrequency.TWICE);
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
