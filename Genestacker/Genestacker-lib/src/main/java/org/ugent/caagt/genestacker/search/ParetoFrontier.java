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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents a Pareto frontier containing all Pareto optimal schemes during construction.
 * 
 * @author Herman De Beukelaer <herman.debeukelaer@ugent.be>
 */
public class ParetoFrontier extends GenericParetoFrontier<CrossingScheme,CrossingSchemeDescriptor> {
    
    /**
     * Create a new Pareto frontier with the default dominates relation.
     */
    public ParetoFrontier(){
        this(new DefaultDominatesRelation());
    }
    
    /**
     * Create a new Pareto frontier with given dominates relation.
     * 
     * @param dominatesRelation 
     */
    public ParetoFrontier(DominatesRelation<CrossingSchemeDescriptor> dominatesRelation){
        super(dominatesRelation);
    }
    
    /**
     * Return the current solutions contained in the Pareto frontier, grouped
     * by the number of generations of the schemes.
     * 
     */
    public Map<Integer, Set<CrossingScheme>> getSchemes(){
        Map<Integer, Set<CrossingScheme>> grouped = new HashMap<>();
        for(CrossingScheme s : getFrontier()){
            int g = s.getNumGenerations();
            Set<CrossingScheme> gen = grouped.get(g);
            if(gen == null){
                gen = new HashSet<>();
                grouped.put(g, gen);
            }
            gen.add(s);
        }
        return grouped;
    }

    @Override
    public CrossingSchemeDescriptor inferDescriptor(CrossingScheme scheme) {
        return scheme.getDescriptor();
    }
    
}
