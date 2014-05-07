//  Copyright 2013 Herman De Beukelaer
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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.ugent.caagt.genestacker.DiploidChromosome;
import org.ugent.caagt.genestacker.GeneticMap;
import org.ugent.caagt.genestacker.Genotype;
import org.ugent.caagt.genestacker.Plant;
import org.ugent.caagt.genestacker.search.CrossingScheme;
import org.ugent.caagt.genestacker.search.CrossingSchemeDescriptor;
import org.ugent.caagt.genestacker.search.FuturePlantNode;
import org.ugent.caagt.genestacker.search.PlantNode;
import org.ugent.caagt.genestacker.search.PopulationSizeTools;
import org.ugent.caagt.genestacker.search.bb.PlantDescriptor;

/**
 * Computes a heuristic lower bound on the population size of any extension of a given partial scheme, based on
 * the probabilities of those crossovers that are necessarily still required to obtain the ideotype. More precisely,
 * the following bound is computed:
 * <ol>
 *  <li>
 *      For every pair of consecutive loci on the same chromosome, the desired "two allele stretches" are inferred from
 *      the ideotype. In case of a homozygous ideotype, there will be one such desired stretch for every two consecutive
 *      loci. In case of a heterozygous ideotype, there might be two desired stretches, if the ideotype is heterozygous
 *      at at least one of the two consecutive loci.
 *  </li>
 * <li>
 *      The set of initial parents is inspected to retain all stretches, from those identified in the previous step,
 *      which do <b>not</b> occur in any initial parent. For each such stretch, a crossover between the two corresponding
 *      loci is necessarily required to obtain the ideotype.
 * </li>
 * <li>
 *      During search, given a currently constructed partial scheme, it is checked which of those stretches have not
 *      yet been obtained in any plant throughout the scheme, and the population size required to obtain each of the
 *      corresponding crossovers is accounted for.
 * </li>
 * </ol>
 * This bound is not exact as it might be possible to obtain multiple target genotypes from the same seed lot in the
 * same generation, in which case the joint population size is accounted for, which might be lower than the sum of the
 * individual population sizes. The heuristic bound does not take this option into account, which makes it an inexact
 * bound. However, it will hold in many cases, so it is expected to be a good heuristic in general.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class HeuristicPopulationSizeBound extends Heuristic {
    
    // desired two allele stretches, consisting of two consecutive alleles,
    // which do not occur in any initial parent; data structure:
    // chromosome index --> 1st locus --> stretches (1 or 2)
    private List<List<Set<TwoAlleleStretch>>> desiredStretchesNotOccurringInInitialParents;
    
    // genetic map
    private GeneticMap map;
    
    // population size tools
    private PopulationSizeTools popSizeTools;
    
    /**
     * Create a new instance.
     * 
     * @param initialPlants collection of initial plants
     * @param ideotype desired ideotype
     * @param map genetic map
     * @param popSizeTools population size tools used to compute population sizes
     */
    public HeuristicPopulationSizeBound(Collection<Plant> initialPlants, Genotype ideotype, GeneticMap map, PopulationSizeTools popSizeTools){
        this.map = map;
        this.popSizeTools = popSizeTools;
        // inspect ideotype and initial plants to identify necessary crossovers
        identifyNecessaryCrossovers(initialPlants, ideotype);
    }

    /**
     * Private method that infers the collection of stretches consisting of two consecutive alleles that are desired
     * (i.e occur in the ideotype) but which are not yet contained in any initial plant. The result is store in the
     * private field <code>desiredStretchesNotOccurringInInitialParents</code>.
     * 
     * @param initialPlants collection of initial plants
     * @param ideotype desired ideotype
     */
    private void identifyNecessaryCrossovers(Collection<Plant> initialPlants, Genotype ideotype){
        
        // infer all two allele stretches from ideotype
        desiredStretchesNotOccurringInInitialParents = new ArrayList<>();
        // go through chromosomes
        for(int c=0; c<ideotype.nrOfChromosomes(); c++){
            DiploidChromosome chrom = ideotype.getChromosomes().get(c);
            List<Set<TwoAlleleStretch>> chromStretches = new ArrayList<>();
            // go through pairs of consecutive loci
            for(int l=0; l<chrom.nrOfLoci()-1; l++){
                Set<TwoAlleleStretch> locusStretches = new HashSet<>();
                // consider both haplotypes
                for(int h=0; h<2; h++){
                    TwoAlleleStretch stretch = new TwoAlleleStretch(
                                    chrom.getHaplotypes()[h].targetPresent(l),
                                    chrom.getHaplotypes()[h].targetPresent(l+1)
                                );
                    locusStretches.add(stretch);
                }
                chromStretches.add(locusStretches);
            }
            desiredStretchesNotOccurringInInitialParents.add(chromStretches);
        }
        
        // go through initial parents and remove all stretches that are already present in at least one parent

        // go through chromosomes
        for(int c=0; c<desiredStretchesNotOccurringInInitialParents.size(); c++){
            // go through pairs of consecutive loci
            for(int l=0; l<desiredStretchesNotOccurringInInitialParents.get(c).size(); l++){
                // filter stretches (continue as long as not all present in some initial parent)
                Set<TwoAlleleStretch> locusStretches = desiredStretchesNotOccurringInInitialParents.get(c).get(l);
                Iterator<Plant> it = initialPlants.iterator();
                while(!locusStretches.isEmpty() && it.hasNext()){
                    // check next parent (both haplotypes)
                    Genotype g = it.next().getGenotype();
                    for(int h=0; h<2; h++){
                        TwoAlleleStretch stretch = new TwoAlleleStretch(
                                        g.getChromosomes().get(c).getHaplotypes()[h].targetPresent(l),
                                        g.getChromosomes().get(c).getHaplotypes()[h].targetPresent(l+1)
                                    );
                        // remove stretch
                        locusStretches.remove(stretch);
                    }
                }
            }
        }
        
    }
    
    /**
     * Compute minimum additional population size, when a certain collection of genotypes has already been obtained.
     * 
     * @param genotypes already obtained genotypes
     * @param minNumTargetsFromNonUniformSeedLots minimum number of targets grown from nonuniform seed lots in the
     *                                            respective crossing schedule (used for population size computations)
     * @return the (heuristic) minimum increase in population size to obtain all desired stretches consisting of two
     *         consecutive alleles, which have not yet been obtained so far
     */
    public long computeMinAdditionalPopSize(Collection<Genotype> genotypes, int minNumTargetsFromNonUniformSeedLots){
        long popsize = 0;
        
        // go through genotypes, check which stretches have already been created, account for pop size of remaining crossovers
        
        Set<TwoAlleleStretch> found = new HashSet<>();
        // go through chromosomes
        for(int c=0; c<desiredStretchesNotOccurringInInitialParents.size(); c++){
            // go through pairs of consecutive loci
            for(int l=0; l<desiredStretchesNotOccurringInInitialParents.get(c).size(); l++){
                // filter stretches (continue as long as not all present in some genotype)
                Set<TwoAlleleStretch> locusStretches = desiredStretchesNotOccurringInInitialParents.get(c).get(l);
                Iterator<Genotype> it = genotypes.iterator();
                found.clear();
                while(found.size() < locusStretches.size() && it.hasNext()){
                    // check next genotype (both haplotypes)
                    Genotype g = it.next();
                    for(int h=0; h<2; h++){
                        TwoAlleleStretch stretch = new TwoAlleleStretch(
                                            g.getChromosomes().get(c).getHaplotypes()[h].targetPresent(l),
                                            g.getChromosomes().get(c).getHaplotypes()[h].targetPresent(l+1)
                                        );
                        // missing stretch found?
                        if(locusStretches.contains(stretch)){
                            found.add(stretch);
                        }
                    }
                }
                // take into account population size for remaining necessary crossover(s)
                if(found.size() < locusStretches.size()){
                    // number of distinct crossovers still required betweeen corresponding loci
                    int n = locusStretches.size() - found.size();
                    // recombination rate (divided by two; not interested in complement)
                    double prob = 0.5 * map.getRecombinationProbability(c, l, l+1);
                    // compute corresponding population size
                    PlantNode fpn = new FuturePlantNode(minNumTargetsFromNonUniformSeedLots, prob);
                    long pop = popSizeTools.computeRequiredSeedsForTargetPlant(fpn);
                    // increase lower bound
                    popsize += n * pop;
                }
            }
        }
        
        return popsize;
    }
    
    @Override
    public CrossingSchemeDescriptor extendBoundsUponCrossing(CrossingSchemeDescriptor curBounds, CrossingScheme scheme){
        Set<Genotype> genotypes = new HashSet<>();
        addGenotypesOccurringInScheme(genotypes, scheme);
        // account for additional population size
        return increasePopSizeBound(genotypes, curBounds);
    }
    
    @Override
    public CrossingSchemeDescriptor extendBoundsUponCrossingWithSpecificOther(CrossingSchemeDescriptor curBounds,
                                                                              CrossingScheme scheme,
                                                                              CrossingScheme other){
        Set<Genotype> genotypes = new HashSet<>();
        addGenotypesOccurringInScheme(genotypes, scheme);
        addGenotypesOccurringInScheme(genotypes, other);
        // account for additional population size
        return increasePopSizeBound(genotypes, curBounds);
    }
    
    @Override
    public CrossingSchemeDescriptor extendBoundsUponCrossingWithSpecificOtherWithSelectedTarget(CrossingSchemeDescriptor curBounds,
                                                                                                CrossingScheme scheme,
                                                                                                CrossingScheme other,
                                                                                                PlantDescriptor target){
        Set<Genotype> genotypes = new HashSet<>();
        addGenotypesOccurringInScheme(genotypes, scheme);
        addGenotypesOccurringInScheme(genotypes, other);
        genotypes.add(target.getPlant().getGenotype());
        // account for additional population size
        return increasePopSizeBound(genotypes, curBounds);
    }
    
    @Override
    public CrossingSchemeDescriptor extendBoundsUponSelfing(CrossingSchemeDescriptor curBounds, CrossingScheme scheme){
        Set<Genotype> genotypes = new HashSet<>();
        addGenotypesOccurringInScheme(genotypes, scheme);
        // account for additional population size
        return increasePopSizeBound(genotypes, curBounds);
    }
    
    @Override
    public CrossingSchemeDescriptor extendBoundsUponSelfingWithSelectedTarget(CrossingSchemeDescriptor curBounds,
                                                                              CrossingScheme scheme,
                                                                              PlantDescriptor target){
        Set<Genotype> genotypes = new HashSet<>();
        addGenotypesOccurringInScheme(genotypes, scheme);
        genotypes.add(target.getPlant().getGenotype());
        // account for additional population size
        return increasePopSizeBound(genotypes, curBounds);
    }
    
    private void addGenotypesOccurringInScheme(Collection<Genotype> genotypes, CrossingScheme scheme){
        // register genotypes occurring in scheme
        for(PlantNode pn : scheme.getPlantNodes()){
            // skip dummy plant nodes
            if(!pn.isDummy()){
                genotypes.add(pn.getPlant().getGenotype());
            }
        }
    }
    
    private CrossingSchemeDescriptor increasePopSizeBound(Collection<Genotype> genotypes, CrossingSchemeDescriptor curBounds){
        // compute additional pop size lower bound
        long extraPopSize = computeMinAdditionalPopSize(genotypes, curBounds.getNumTargetsFromNonUniformSeedLots());
        // update bounds
        curBounds.setTotalPopSize(curBounds.getTotalPopSize()+extraPopSize);
        // return updated bounds
        return curBounds;
    }
    
    /**
     * Private class representing a two allele stretch.
     */
    
    private class TwoAlleleStretch {
        
        // alleles
        boolean a1, a2;

        public TwoAlleleStretch(boolean a1, boolean a2) {
            this.a1 = a1;
            this.a2 = a2;
        }

        public boolean isA1() {
            return a1;
        }

        public boolean isA2() {
            return a2;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 89 * hash + (this.a1 ? 1 : 0);
            hash = 89 * hash + (this.a2 ? 1 : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TwoAlleleStretch other = (TwoAlleleStretch) obj;
            if (this.a1 != other.a1) {
                return false;
            }
            if (this.a2 != other.a2) {
                return false;
            }
            return true;
        }
        
        @Override
        public String toString(){
            StringBuilder str = new StringBuilder();
            if(a1){
                str.append("1 ");
            } else {
                str.append("0 ");
            }
            if(a2){
                str.append("1");
            } else {
                str.append("0");
            }
            return str.toString();
        }
        
    }
    
}
