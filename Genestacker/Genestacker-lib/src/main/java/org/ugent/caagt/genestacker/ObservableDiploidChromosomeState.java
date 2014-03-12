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

import java.util.Arrays;

/**
 * Represents the observable state of a diploid chromosome. For each target locus, the observable
 * state indicates whether the target gene is present once, twice or not at all.
 * 
 * @author Herman De Beukelaer <herman.debeukelaer@ugent.be>
 */
public class ObservableDiploidChromosomeState {
    
    // state
    private ObservableDiploidTargetState[] targetStates;
    
    /**
     * Construct a new observable diploid chromosome state.
     * 
     * @param targetStates 
     */
    public ObservableDiploidChromosomeState(ObservableDiploidTargetState[] targetStates){
        this.targetStates = targetStates;
    }
    
    public ObservableDiploidTargetState[] getTargetStates(){
        return targetStates;
    }
    
    public int nrOfLoci(){
        return targetStates.length;
    }
    
    @Override
    public boolean equals(Object s){
        boolean equal = false;
        if(s instanceof ObservableDiploidChromosomeState){
            ObservableDiploidChromosomeState ss = (ObservableDiploidChromosomeState) s;
            equal = (Arrays.equals(targetStates, ss.targetStates));
        }
        return equal;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Arrays.deepHashCode(targetStates);
        return hash;
    }
    
    @Override
    public String toString(){
        return Arrays.toString(targetStates);
    }
    
}
