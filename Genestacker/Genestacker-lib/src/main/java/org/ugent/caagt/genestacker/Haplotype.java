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

import java.util.ArrayList;
import java.util.List;
import org.ugent.caagt.genestacker.exceptions.EmptyHaplotypeException;

/**
 * Represents a haplotype.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class Haplotype implements Comparable<Haplotype> {

    // targets (true = present, false = not present)
    private List<Boolean> targets;
    
    public Haplotype(List<Boolean> targets) throws EmptyHaplotypeException{
        setTargets(targets);
    }
    
    public Haplotype(boolean[] targets) throws EmptyHaplotypeException{
        List<Boolean> targetList = new ArrayList<>(targets.length);
        for(int i=0; i<targets.length; i++){
            targetList.add(targets[i]);
        }
        setTargets(targetList);
    }
    
    /**
     * Copy constructor (deep copy).
     * 
     * @param h haplotype to copy (deep copy)
     * @throws EmptyHaplotypeException if the given haplotype is empty, i.e. has 0 loci
     */
    public Haplotype(Haplotype h) throws EmptyHaplotypeException{
        List<Boolean> targetList = new ArrayList<>(h.nrOfLoci());
        for(int i=0; i<h.nrOfLoci(); i++){
            targetList.add(h.targetPresent(i));
        }
        setTargets(targetList);
    }
    
    private void setTargets(List<Boolean> targets) throws EmptyHaplotypeException{
        // check for empty targets
        if(targets == null || targets.isEmpty()){
            throw(new EmptyHaplotypeException("Attempted to construct an empty haplotype with 0 loci"));
        }
        this.targets = targets;
    }
    
    public int nrOfLoci(){
        return targets.size();
    }
    
    public int nrOfTargetsPresent(){
        int numTargets = 0;
        for(Boolean t : targets){
            if(t){
                numTargets++;
            }
        }
        return numTargets;
    }
    
    /**
     * Check for presence of target at a specific locus in this haplotype.
     * 
     * @param locus locus at which presence of the target allele is checked
     * @return <code>true</code> if the target allele is present at this locus
     */
    public boolean targetPresent(int locus){
        return targets.get(locus);
    }
    
    public void setTargetPresent(int locus, boolean present){
        targets.set(locus, present);
    }
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder("[");
        for(int i=0; i<targets.size(); i++){
            str.append(targets.get(i) ? 1:0);
            if(i<targets.size()-1){
                str.append(" ");
            } else {
                str.append("]");
            }
        }
        return str.toString();
    }
    
    @Override
    public boolean equals(Object h){
        boolean equal = false;
        if(h instanceof Haplotype){
            Haplotype hh = (Haplotype) h;
            equal = targets.equals(hh.targets);
        }
        return equal;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + (targets != null ? targets.hashCode() : 0);
        return hash;
    }
    
    /**
     * Haplotype h1 is smaller than h2 if and only if h1 has fewer targets or h1
     * has a 0 at the first locus where h1 and h2 differ (h2 has a 1 at this locus).
     * If no such locus exists h1 and h2 are equal.
     * 
     * @param h haplotype to which this haplotype should be compared
     * @return a strictly negative integer if this haplotype is smaller than h, 0 if they are equal,
     *         and a strictly positive integer if h is larger.
     */
    @Override
    public int compareTo(Haplotype h) {
        if(nrOfLoci() < h.nrOfLoci()){
            return -1;
        } else if(nrOfLoci() > h.nrOfLoci()){
            return 1;
        } else {
            // equal nr of targets, compare them
            for(int i=0; i<nrOfLoci(); i++){
                if(!targets.get(i) && h.targets.get(i)){
                    return -1;
                } else if(targets.get(i) && !h.targets.get(i)){
                    return 1;
                }
            }
            // all targets equal
            return 0;
        }
    }
    
}
