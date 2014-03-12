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

package org.ugent.caagt.genestacker;

import java.util.List;

/**
 * Represents a diploid plant genotype w.r.t. the target genes.
 * 
 * @author Herman De Beukelaer <herman.debeukelaer@ugent.be>
 */
public class Genotype {
    
    // chromosomes containing target genes
    private List<DiploidChromosome> chromosomes;
    
    // observable state
    private ObservableGenotypeState observableState;
    
    /**
     * Create genotype with given chromosomes.
     * 
     * @param chromosomes 
     */
    public Genotype(List<DiploidChromosome> chromosomes){
        this.chromosomes = chromosomes;
        observableState = new ObservableGenotypeState(this);
    }
    
    /**
     * Get the observable state of the genotype.
     * 
     */
    public ObservableGenotypeState getObservableState(){
        return observableState;
    }
    
    /**
     * Get the number of chromosomes in this genotype.
     * 
     */
    public int nrOfChromosomes(){
        return chromosomes.size();
    }
    
    /**
     * Get the total number of loci across all chromosomes.
     * 
     */
    public int nrOfLoci(){
        int nr = 0;
        for(Chromosome chr : chromosomes){
            nr += chr.nrOfLoci();
        }
        return nr;
    }
    
    public List<DiploidChromosome> getChromosomes(){
        return chromosomes;
    }
    
    public int nrOfTargetsPresent(){
        int numTargets = 0;
        for(DiploidChromosome c : chromosomes){
            numTargets += c.nrOfTargetsPresent();
        }
        return numTargets;
    }
    
    /**
     * Check whether all chromosomes of the genotype contain only homozygous target loci.
     * 
     */
    public boolean isHomozygousAtAllTargetLoci(){
        boolean homozygous = true;
        int c=0;
        while(homozygous && c<nrOfChromosomes()){
            homozygous = chromosomes.get(c).isHomozygousAtAllTargetLoci();
            c++;
        }
        return homozygous;
    }
    
    /**
     * Check if this genotype is compatible for crossing with genotype 'other'.
     * Compatible means that both genotypes have the same number of chromosomes
     * and each respective pair of chromosomes has the same number of targets.
     * 
     * @param other
     */
    public boolean compatibleWith(Genotype other){
        boolean compatible = (chromosomes.size() == other.chromosomes.size());
        int i=0;
        while(compatible && i<chromosomes.size()){
            compatible = (chromosomes.get(i).nrOfLoci() == other.chromosomes.get(i).nrOfLoci());
            i++;
        }
        return compatible;
    }
    
    
    @Override
    public String toString(){
        StringBuilder line1 = new StringBuilder();
        StringBuilder line2 = new StringBuilder();
        for(int i=0; i<chromosomes.size(); i++){
            String chr = chromosomes.get(i).toString();
            String[] chrLines = chr.split("\n");
            line1.append(chrLines[0]);
            line2.append(chrLines[1]);
        }
        return line1 + "\n" + line2;
    }
    
    @Override
    public boolean equals(Object g){
        boolean equal = false;
        if(g instanceof Genotype){
            Genotype gg = (Genotype) g;
            equal = chromosomes.equals(gg.chromosomes);
        }
        return equal;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + (chromosomes != null ? chromosomes.hashCode() : 0);
        return hash;
    }

}
