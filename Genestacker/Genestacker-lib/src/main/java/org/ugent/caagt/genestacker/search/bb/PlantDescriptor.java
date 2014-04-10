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

import java.util.Objects;
import org.ugent.caagt.genestacker.Plant;

/**
 * Represents a plant and the corresponding probability and linkage phase ambiguity
 * when growing this plant from its parent seed lot.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class PlantDescriptor {
    
    // plant
    private Plant plant;
    
    // probability of obtaining genotype from the seed lot from which it is grown
    private double prob;
    
    // linkage phase ambiguity
    private double linkagePhaseAmbiguity;
    
    // grown from uniform seed lot?
    private boolean uniformSeedLot;
    
    public PlantDescriptor(Plant plant, double prob, double linkagePhaseAmbiguity, boolean uniformSeedLot){
        this.plant = plant;
        this.prob = prob;
        this.linkagePhaseAmbiguity = linkagePhaseAmbiguity;
        this.uniformSeedLot = uniformSeedLot;
    }

    public Plant getPlant() {
        return plant;
    }

    public double getProb() {
        return prob;
    }

    public double getLinkagePhaseAmbiguity() {
        return linkagePhaseAmbiguity;
    }
    
    public boolean grownFromUniformSeedLot(){
        return uniformSeedLot;
    }
    
    @Override
    public boolean equals(Object d){
        boolean equal = false;
        if(d instanceof PlantDescriptor){
            PlantDescriptor dd = (PlantDescriptor) d;
            equal = plant.equals(dd.getPlant())
                        && prob == dd.getProb()
                        && linkagePhaseAmbiguity == dd.getLinkagePhaseAmbiguity()
                        && uniformSeedLot == dd.grownFromUniformSeedLot();
        }
        return equal;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + Objects.hashCode(plant);
        hash = 43 * hash + (int) (Double.doubleToLongBits(prob) ^ (Double.doubleToLongBits(prob) >>> 32));
        hash = 43 * hash + (int) (Double.doubleToLongBits(linkagePhaseAmbiguity) ^ (Double.doubleToLongBits(linkagePhaseAmbiguity) >>> 32));
        hash = 43 * hash + (uniformSeedLot ? 1 : 0);
        return hash;
    }

    @Override
    public String toString(){
        return plant.toString();
    }

}
