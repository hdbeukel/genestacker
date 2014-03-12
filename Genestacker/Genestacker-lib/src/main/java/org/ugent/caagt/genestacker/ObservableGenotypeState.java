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

/**
 *
 * @author Herman De Beukelaer <herman.debeukelaer@ugent.be>
 */
public class ObservableGenotypeState {
    
    // observable chromosome states
    private List<ObservableDiploidChromosomeState> observableChromosomeStates;

    public ObservableGenotypeState(Genotype genotype){
        observableChromosomeStates = new ArrayList<>(genotype.nrOfChromosomes());
        for(int i=0; i<genotype.nrOfChromosomes(); i++){
            observableChromosomeStates.add(genotype.getChromosomes().get(i).getObservableState());
        }
    }
    
    public List<ObservableDiploidChromosomeState> getObservableChromosomeStates(){
        return observableChromosomeStates;
    }
    
    public int nrOfChromosomes(){
        return observableChromosomeStates.size();
    }
    
    @Override
    public boolean equals(Object s){
        boolean equal = false;
        if(s instanceof ObservableGenotypeState){
            ObservableGenotypeState ss = (ObservableGenotypeState) s;
            equal = (observableChromosomeStates.equals(ss.getObservableChromosomeStates()));
        }
        return equal;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.observableChromosomeStates != null ? this.observableChromosomeStates.hashCode() : 0);
        return hash;
    }
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        for(int i=0; i<observableChromosomeStates.size(); i++){
            str.append(observableChromosomeStates.get(i));
        }
        return str.toString();
    }

}
