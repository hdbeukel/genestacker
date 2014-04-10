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

/**
 * Descriptor used to describe important properties of a crossing scheme.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class CrossingSchemeDescriptor {
    
    private int numGenerations;
        
    private int numCrossings;
    
    private int maxCrossingsWithPlant;
    
    private long maxPopSizePerGeneration;
    
    private long totalPopSize;
    
    private double linkagePhaseAmbiguity;
    
    private int numTargetsFromNonUniformSeedLots;
        
    public CrossingSchemeDescriptor(int numGenerations, int numCrossings, int maxCrossingsWithPlant,
                            long maxPopSizePerGeneration, long totalPopSize, double linkagePhaseAmbiguity,
                            int numTargetsFromNonUniformSeedLots) {
        this.numGenerations = numGenerations;
        this.numCrossings = numCrossings;
        this.maxCrossingsWithPlant = maxCrossingsWithPlant;
        this.maxPopSizePerGeneration = maxPopSizePerGeneration;
        this.totalPopSize = totalPopSize;
        this.linkagePhaseAmbiguity = linkagePhaseAmbiguity;
        this.numTargetsFromNonUniformSeedLots = numTargetsFromNonUniformSeedLots;
    }

    public int getNumGenerations() {
        return numGenerations;
    }

    public void setNumGenerations(int numGenerations) {
        this.numGenerations = numGenerations;
    }
    
    public int getNumCrossings(){
        return numCrossings;
    }
    
    public void setNumCrossings(int numCrossings){
        this.numCrossings = numCrossings;
    }

    public long getMaxPopSizePerGeneration() {
        return maxPopSizePerGeneration;
    }

    public void setMaxPopSizePerGeneration(long maxPopSizePerGeneration) {
        this.maxPopSizePerGeneration = maxPopSizePerGeneration;
    }

    public long getTotalPopSize() {
        return totalPopSize;
    }

    public void setTotalPopSize(long totalPopSize) {
        this.totalPopSize = totalPopSize;
    }

    public double getLinkagePhaseAmbiguity() {
        return linkagePhaseAmbiguity;
    }

    public void setLinkagePhaseAmbiguity(double linkagePhaseAmbiguity) {
        this.linkagePhaseAmbiguity = linkagePhaseAmbiguity;
    }

    public int getMaxCrossingsWithPlant() {
        return maxCrossingsWithPlant;
    }

    public void setMaxCrossingsWithPlant(int maxCrossingsWithPlant) {
        this.maxCrossingsWithPlant = maxCrossingsWithPlant;
    }

    public int getNumTargetsFromNonUniformSeedLots() {
        return numTargetsFromNonUniformSeedLots;
    }

    public void setNumTargetsFromNonUniformSeedLots(int numTargetsFromNonUniformSeedLots) {
        this.numTargetsFromNonUniformSeedLots = numTargetsFromNonUniformSeedLots;
    }
    
}
