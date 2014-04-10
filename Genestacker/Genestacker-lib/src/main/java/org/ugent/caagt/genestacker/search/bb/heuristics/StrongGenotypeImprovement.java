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

import org.ugent.caagt.genestacker.DiploidChromosome;
import org.ugent.caagt.genestacker.GeneticMap;
import org.ugent.caagt.genestacker.Genotype;
import org.ugent.caagt.genestacker.Haplotype;

/**
 *
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class StrongGenotypeImprovement extends GenotypeImprovement {
    
    // genetic map; required to compute probability of obtaining matching stretch
    private GeneticMap map;
    
    public StrongGenotypeImprovement(Genotype ideotype, GeneticMap map){
        super(ideotype);
        this.map = map;
    }
    
    /**
     * Checks for improvement in a given chromosome, w.r.t a target haplotype.
     */
    @Override
    protected boolean improvementInChromosome(int chromIndex, DiploidChromosome chrom, DiploidChromosome otherChrom, Haplotype ideotypeHap){
        
        // get max length and corresponding prob of matching stretches after at most 1 crossover
        
        // compute exact values for ancestor chrom
        double[] ancestorStretch = computeLongestMatchingStretch(chromIndex, otherChrom, ideotypeHap);
        // compute lower bounds for plant chrom, taking into account values of ancestor chrom
        double[] plantStretch = computeLongestMatchingStretch(chromIndex, chrom, ideotypeHap, ancestorStretch);
        // first compare length; break ties on prob
        if(plantStretch[0] > ancestorStretch[0]){
            // length increased (improvement)
            return true;
        } else if (plantStretch[0] == ancestorStretch[0]){
            // equal length: compare on prob
            return plantStretch[1] > ancestorStretch[1];
        } else {
            // length decreased (no improvement)
            return false;
        }
    }

    public double[] computeLongestMatchingStretch(int chromIndex, DiploidChromosome chrom, Haplotype target){
        return computeLongestMatchingStretch(chromIndex, chrom, target, null);
    }
    /**
     * Compute length and probability of the longest matching stretch after at most 1 crossover.
     * If several stretches of maximal length are found, the one with the highest probability is
     * picked. Returns an array containing two doubles: first the length of the longest stretch
     * (which is actually always an integer), followed by its probability. To speedup computations,
     * an already computed (l, p) pair of an other chromosome can be given, in such case
     * computation is interrupted as soon as a stretch is found with length > l, or with
     * length == l and prob > p. Then, the computed (l, p) pair of this chromosome is not
     * exact, but it is already guaranteed to be an improvement over the other (l, p) pair,
     * so we do not need to compute the exact values. Computation is also aborted as soon as
     * it is no longer possible to find a stretch with length >= l, because in this case we
     * already know that we can never have an improvement over the given (l, p) pair.
    */
    private double[] computeLongestMatchingStretch(int chromIndex, DiploidChromosome chrom,
                                                  Haplotype target, double[] bounds){

        // compute length and prob
        int maxLength = 0;
        double maxProb = 0.0;
        boolean cont = true;
        // consider both chromosome sides
        int h=0;
        while(cont && h<2){
            Haplotype hap = chrom.getHaplotypes()[h];
            Haplotype otherHap = chrom.getHaplotypes()[1-h];
            // scan for stretches
            int l=0;
            int prevStretchEnd = -1;
            while(cont && l < chrom.nrOfLoci()){
                // skip nonmatching loci
                while(l < chrom.nrOfLoci() && hap.targetPresent(l) != target.targetPresent(l)){
                    l++;
                }
                // check if current maxLength can still be reached for the next stretch
                // (also check if end of chromosome not yet reached)
                if(l <= chrom.nrOfLoci() - (bounds != null ? Math.max(Math.max(1,maxLength),(int)bounds[0]) : Math.max(1,maxLength))){
                    // start of new stretch
                    double prob = 1.0;
                    int length = 0;
                    int prevHeterozygousLocus = -1;
                    // scan matches at current side
                    int s = l;
                    while(cont && s < chrom.nrOfLoci() && hap.targetPresent(s) == target.targetPresent(s)){
                        if(otherHap.targetPresent(s) != hap.targetPresent(s)){
                            // heterozygous locus --> update probability
                            if(prevHeterozygousLocus == -1){
                                prob = 0.5;
                            } else {
                                // no crossover
                                prob *= (1-map.getRecombinationProbability(chromIndex, prevHeterozygousLocus, s));
                            }
                            prevHeterozygousLocus = s;
                        }
                        s++;
                        // update length
                        length++;
                        // check bounds
                        if(bounds != null && (length > (int) bounds[0] || length == (int) bounds[0] && prob > bounds[1])){
                            cont = false;
                        }
                    }
                    // update starting position to search for next stretch
                    l = s;
                    // make crossover and continue stretch at other side
                    while(cont && s < chrom.nrOfLoci()
                            && otherHap.targetPresent(s) == target.targetPresent(s)
                            // if s <= prevStretchEnd, the previous stetch was certainly larger
                            && s > prevStretchEnd){
                        if(hap.targetPresent(s) != otherHap.targetPresent(s)){
                            // heterozygous locus --> update probability
                            if(prevHeterozygousLocus == -1){
                                prob = 0.5;
                            } else if (s == l){
                                // this is the single crossover
                                prob *= map.getRecombinationProbability(chromIndex, prevHeterozygousLocus, s);
                            } else {
                                // no crossover
                                prob *= (1-map.getRecombinationProbability(chromIndex, prevHeterozygousLocus, s));
                            }
                            prevHeterozygousLocus = s;
                        }
                        s++;
                        // update length
                        length++;
                        // check bounds
                        if(bounds != null && (length > (int) bounds[0] || length == (int) bounds[0] && prob > bounds[1])){
                            cont = false;
                        }
                    }
                    // update previous stretch end
                    prevStretchEnd = (s-1);
                    // check if new best stretch found
                    if(length > maxLength){
                        maxLength = length;
                        maxProb = prob;
                    } else if (length == maxLength && prob > maxProb){
                        maxProb = prob;
                    }
                } else {
                    // skip rest of current haplotype (impossible to reach current maxLength anymore)
                    l = chrom.nrOfLoci();
                }               
            }
            h++;
        }
        // return array containing max length and corresponding max prob
        return new double[]{maxLength, maxProb};
    }

}
