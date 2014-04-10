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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.ugent.caagt.genestacker.Plant;
import org.ugent.caagt.genestacker.search.bb.PlantDescriptor;

/**
 * Groups a set of alternative ways to construct a specific crossing scheme.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class CrossingSchemeAlternatives {
    
    // last ID assigned
    private static long lastID = 0;
    
    // this object's ID
    private long ID;

    // schemes
    private List<CrossingScheme> alternatives;
    
    // common final plants of the schemes
    private Plant finalPlant;
    
    // common set of plants present in the scheme alternatives, each represented
    // by a PlantDescriptor object
    private Set<PlantDescriptor> ancestorDescriptors;
    
    // common set of initial parents (those plants grown from an initial seed lot
    // of generation 0)
    private Set<Plant> initialParents;
    
    public CrossingSchemeAlternatives(List<CrossingScheme> alternatives){
        ID = genNextID();
        this.alternatives = alternatives;
        init();
    }
    
    public CrossingSchemeAlternatives(CrossingScheme scheme){
        ID = genNextID();
        alternatives = new ArrayList<>();
        alternatives.add(scheme);
        init();
    }
    
    private void init(){
        
        // register common final plant, the common set of all plants and the common
        // set of initial parents (assumes that all given schemes are indeed alternatives
        // of the same scheme, having the same plants, seedlots, etc. where only the
        // generation shifts and the resulting degree of reuse may differ)
        
        finalPlant = alternatives.get(0).getFinalPlantNode().getPlant();
        
        ancestorDescriptors = new HashSet<>();
        initialParents = new HashSet<>();
        for(PlantNode pn : alternatives.get(0).getPlantNodes()){
            ancestorDescriptors.add(
                new PlantDescriptor(
                    pn.getPlant(),
                    pn.getProbabilityOfPhaseKnownGenotype(),
                    pn.getLinkagePhaseAmbiguity(),
                    pn.grownFromUniformLot()
                )
            );
            if(pn.getParent().isInitialSeedLot()){
                initialParents.add(pn.getPlant());
            }
        }
        
    }
    
    public static void resetIDs(){
        lastID = 0;
    }
    
    public synchronized static long genNextID(){
        return lastID++;
    }
    
    public long getID(){
        return ID;
    }
    
    public List<CrossingScheme> getAlternatives(){
        return alternatives;
    }
    
    public int nrOfAlternatives(){
        return alternatives.size();
    }
    
    public Plant getFinalPlant(){
        return finalPlant;
    }
    
    public Set<PlantDescriptor> getAncestorDescriptors(){
        return ancestorDescriptors;
    }
    
    public Set<Plant> getInitialParents(){
        return initialParents;
    }
    
    public Iterator<CrossingScheme> iterator(){
        return alternatives.iterator();
    }
    
    @Override
    public String toString(){
        return "{ID:" + ID + ", min gen:" + getMinNumGen() + ", max gen: " + getMaxNumGen() + ", alternatives: " + nrOfAlternatives() + "}";
    }
    
    public int getMaxNumGen(){
        int maxGen = 0;
        for(CrossingScheme alt : alternatives){
            if(alt.getNumGenerations() > maxGen){
                maxGen = alt.getNumGenerations();
            }
        }
        return maxGen;
    }
    
    public int getMinNumGen(){
        int minGen = Integer.MAX_VALUE;
        for(CrossingScheme alt : alternatives){
            if(alt.getNumGenerations() < minGen){
                minGen = alt.getNumGenerations();
            }
        }
        return minGen;
    }
    
    public long getMaxPopSize(){
        long maxPop = 0;
        for(CrossingScheme alt : alternatives){
            if(alt.getTotalPopulationSize() > maxPop){
                maxPop = alt.getTotalPopulationSize();
            }
        }
        return maxPop;
    }
    
    public long getMinPopSize(){
        long minPop = Integer.MAX_VALUE;
        for(CrossingScheme alt : alternatives){
            if(alt.getTotalPopulationSize() < minPop){
                minPop = alt.getTotalPopulationSize();
            }
        }
        return minPop;
    }
    
    public double getMaxLinkagePhaseAmbiguity(){
        double maxLPA = 0;
        for(CrossingScheme alt : alternatives){
            if(alt.getLinkagePhaseAmbiguity() > maxLPA){
                maxLPA = alt.getLinkagePhaseAmbiguity();
            }
        }
        return maxLPA;
    }
    
    public double getMinLinkagePhaseAmbiguity(){
        double minLPA = Double.MAX_VALUE;
        for(CrossingScheme alt : alternatives){
            if(alt.getLinkagePhaseAmbiguity() < minLPA){
                minLPA = alt.getLinkagePhaseAmbiguity();
            }
        }
        return minLPA;
    }
    
    @Override
    public boolean equals(Object o){
        boolean equal = false;
        if(o instanceof CrossingSchemeAlternatives){
            CrossingSchemeAlternatives a = (CrossingSchemeAlternatives) o;
            equal = (ID == a.getID());
        }
        return equal;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + (int) (this.ID ^ (this.ID >>> 32));
        return hash;
    }
    
}
