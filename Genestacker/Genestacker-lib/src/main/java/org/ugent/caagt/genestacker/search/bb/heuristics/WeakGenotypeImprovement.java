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
import java.util.List;
import org.ugent.caagt.genestacker.DiploidChromosome;
import org.ugent.caagt.genestacker.Genotype;
import org.ugent.caagt.genestacker.Haplotype;

/**
 *
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class WeakGenotypeImprovement extends GenotypeImprovement {

    public WeakGenotypeImprovement(Genotype ideotype){
        super(ideotype);
    }
    
    /**
     * A chromosome improves w.r.t this target if it contains a matching stretch that did
     * not yet occur in the parent chromosome, or if at least one target inside an already
     * occurring stretch has been stabilized (heterozygous in the parent, but homozygous in
     * the new chromosome).
     */
    @Override
    protected boolean improvementInChromosome(int chromIndex, DiploidChromosome chrom, DiploidChromosome otherChrom, Haplotype ideotypeHap){
        // check for improvement in one of the haplotypes of the new chromosome
        return improvementInHaplotype(0, chrom, otherChrom, ideotypeHap) || improvementInHaplotype(1, chrom, otherChrom, ideotypeHap);
    }
    
    private boolean improvementInHaplotype(int hapIndex, DiploidChromosome chrom, DiploidChromosome otherChrom, Haplotype ideotypeHap){
        boolean impr = false;
        // go through haplotype and look for an improved stretch (extended or stabilized)
        int l = 0;
        Haplotype hap = chrom.getHaplotypes()[hapIndex];
        Haplotype background = chrom.getHaplotypes()[1-hapIndex];
        while(l < hap.nrOfLoci() && !impr){
            // scan for start of next stretch in the considered haplotype that matches with the target
            while(l < hap.nrOfLoci() && hap.targetPresent(l) != ideotypeHap.targetPresent(l)){
                l++;
            }
            if(l < hap.nrOfLoci()){
                // get the matching stretch starting at locus l
                List<Boolean> match = getMatchingStretchStartingAtLocus(l, hap, ideotypeHap);
                // first scan the upper haplotype of the ancestor chromosome for presence of this stretch
                if(hasSubstretchStartingAtLocus(l, match, otherChrom.getHaplotypes()[0])){
                    // stretch present in upper haplotype of ancestor chromosome: check stability
                    impr = stabilityImprovementOfStretchAtLocus(l, match, background, otherChrom.getHaplotypes()[1]);
                } else {
                    // stretch NOT present in upper haplotype of ancestor chromosome: scan lower haplotype
                    if(hasSubstretchStartingAtLocus(l, match, otherChrom.getHaplotypes()[1])){
                        // stretch present in lower haplotype of ancestor chromosome: check stability
                        impr = stabilityImprovementOfStretchAtLocus(l, match, background, otherChrom.getHaplotypes()[0]);
                    } else {
                        // stretch also NOT present in lower haplotype of ancestor chromosome: improvement!
                        impr = true;
                    }
                }
                // skip matched stretch
                l += match.size();
            }
        }
        return impr;
    }
    
    private List<Boolean> getMatchingStretchStartingAtLocus(int startLocus, Haplotype hap, Haplotype target){
        List<Boolean> match = new ArrayList<>();
        int l = startLocus;
        while(l < hap.nrOfLoci() && hap.targetPresent(l) == target.targetPresent(l)){
            match.add(hap.targetPresent(l));
            l++;
        }
        return match;
    }
    
    private boolean hasSubstretchStartingAtLocus(int locus, List<Boolean> substretch, Haplotype hap){
        boolean substretchPresent = true;
        int l = 0;
        // check if the haplotype contains the given substretch starting from the indicated locus
        while(l < substretch.size() && substretchPresent){
            substretchPresent = (hap.targetPresent(locus+l) == substretch.get(l));
            l++;
        }
        return substretchPresent;
    }
    
    private boolean stabilityImprovementOfStretchAtLocus(int locus, List<Boolean> stretch, Haplotype background, Haplotype ancestorBackground){
        boolean stabilityImprovement = false;
        // scan for a stability improvement (a background locus that matches with the stretch,
        // whereas the corresponding ancestor background locus does not match)
        int l = 0;
        while(l < stretch.size() && !stabilityImprovement){
            stabilityImprovement = (background.targetPresent(locus+l) == stretch.get(l)
                                        && ancestorBackground.targetPresent(locus+l) != stretch.get(l));
            l++;
        }
        return stabilityImprovement;
    }
    
}
