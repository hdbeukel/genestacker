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

import java.util.ListIterator;
import org.ugent.caagt.genestacker.DiploidChromosome;
import org.ugent.caagt.genestacker.Genotype;
import org.ugent.caagt.genestacker.Haplotype;

/**
 * Abstract concept of improvement of genotypes as compared to a given ideotype.
 * 
 * @author Herman De Beukelaer <herman.debeukelaer@ugent.be>
 */
public abstract class GenotypeImprovement {
    
    // desired ideotype
    protected Genotype ideotype;
    
    public GenotypeImprovement(Genotype ideotype){
        this.ideotype = ideotype;
    }
    
    /**
     * Check whether the current genotype g "improves" (heuristically) on a given other genotype.
     * This is the case if there is an improvement in at least one chromosome, as compared to at least
     * one of the target haplotypes of the respective chromosome of the ideotype.
     * 
     * @param g
     * @param other
     * @return 
     */
    public boolean improvesOnOtherGenotype(Genotype g, Genotype other){
        // go through chromosomes
        boolean impr = false;
        ListIterator<DiploidChromosome> itChromOther = other.getChromosomes().listIterator();   // other genotype
        ListIterator<DiploidChromosome> itChrom = g.getChromosomes().listIterator();           // this genotype
        ListIterator<DiploidChromosome> itChromi = ideotype.getChromosomes().listIterator();    // ideotype
        while(!impr && itChromOther.hasNext()){
            // check improvement in chromosome
            int chromIndex = itChromOther.nextIndex();
            DiploidChromosome otherChrom = itChromOther.next();
            DiploidChromosome chrom = itChrom.next();
            DiploidChromosome ideotypeChrom = itChromi.next();
            impr = improvementInChromosome(chromIndex, chrom, otherChrom, ideotypeChrom.getHaplotypes()[0])
                    || improvementInChromosome(chromIndex, chrom, otherChrom, ideotypeChrom.getHaplotypes()[1]);
        }
        // return result
        return impr;
    }
    
    /**
     * Check whether chromosome 'chrom' improves over 'otherChrom', as compared to a given haplotype from
     * the corresponding chromosome in the ideotype.
     */
    protected abstract boolean improvementInChromosome(int chromIndex, DiploidChromosome chrom, DiploidChromosome otherChrom, Haplotype ideotypeHap);
    
    public Genotype getIdeotype(){
        return ideotype;
    }

}
