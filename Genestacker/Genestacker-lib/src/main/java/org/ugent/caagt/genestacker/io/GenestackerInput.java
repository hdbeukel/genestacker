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

package org.ugent.caagt.genestacker.io;

import java.util.List;
import org.ugent.caagt.genestacker.GeneticMap;
import org.ugent.caagt.genestacker.Genotype;
import org.ugent.caagt.genestacker.Plant;

/**
 * Represents the input for Gene Stacker, containing:
 *  
 *  - initial plants
 *  - desired ideotype
 *  - genetic map
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class GenestackerInput {

    // initial plants
    List<Plant> initialPlants;
    
    // desired ideotype's genotype
    Genotype ideotype;
    
    // genetic map
    GeneticMap map;
    
    public GenestackerInput(List<Plant> initialPlants, Genotype ideotype, GeneticMap map){
        this.initialPlants = initialPlants;
        this.ideotype = ideotype;
        this.map = map;
    }

    public List<Plant> getInitialPlants() {
        return initialPlants;
    }

    public Genotype getIdeotype() {
        return ideotype;
    }
    
    public GeneticMap getGeneticMap(){
        return map;
    }

}
