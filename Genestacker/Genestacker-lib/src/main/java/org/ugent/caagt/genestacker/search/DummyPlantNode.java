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

package org.ugent.caagt.genestacker.search;

import org.ugent.caagt.genestacker.Plant;

/**
 * Dummy plant node used during scheme construction, before it is replaced with
 * a real plant node.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class DummyPlantNode extends PlantNode {
    
    public DummyPlantNode(int generation, SeedLotNode parent){
        super(null, generation, parent);
        setPlant(new DummyPlant());
    }
    
    @Override
    public boolean isDummy(){
        return true;
    }
    
    @Override
    public double getProbabilityOfPhaseKnownGenotype(){
        return 1.0;
    }
    
    @Override
    public double getLinkagePhaseAmbiguity(){
        return 0.0;
    }
    
    @Override
    protected PlantNode createCopy(int gen, SeedLotNode parentCopy){
        return new DummyPlantNode(gen, parentCopy);
    }
    
    // dummy plant class
    private class DummyPlant extends Plant {
        
        public DummyPlant(){
            super(null);
        }
        
        @Override
        public boolean isDummyPlant(){
            return true;
        }
        
        // all dummy plants are considered equal
        @Override
        public boolean equals(Object o){
            return (o instanceof DummyPlant);
        }

        // all dummy plants are equal -> same, arbitrary hash
        @Override
        public int hashCode() {
            int hash = 5;
            return hash;
        }
        
        @Override
        public String toString(){
            return "dummy plant";
        }
        
        @Override
        public boolean isHomozygousAtAllTargetLoci(){
            return false;
        }
        
    }
    
}
