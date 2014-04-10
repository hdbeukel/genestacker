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
 * With this relation, scheme s1 dominates scheme s2 when s1 is at least as good
 * as s2 w.r.t to all objectives and better than s2 w.r.t at least one objective.
 * This is the default dominates relation used in Pareto optimization.
 * 
 * Uses a double comparator with precision of 0.0001 (by default) to compare
 * linkage phase ambiguity, as these are presented up to four significant digits
 * in the output.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class DefaultDominatesRelation extends DominatesRelation<CrossingSchemeDescriptor> {

    /**
     * Default dominates relation.
     */
    @Override
    public boolean dominates(CrossingSchemeDescriptor s1, CrossingSchemeDescriptor s2) {
        boolean noWorse = (s1.getNumGenerations() <= s2.getNumGenerations()
                                && s1.getTotalPopSize() <= s2.getTotalPopSize()
                                && dComp.compare(s1.getLinkagePhaseAmbiguity(), s2.getLinkagePhaseAmbiguity()) <= 0);
        boolean atLeastOneBetter = (s1.getNumGenerations() < s2.getNumGenerations()
                                || s1.getTotalPopSize() < s2.getTotalPopSize()
                                || dComp.compare(s1.getLinkagePhaseAmbiguity(), s2.getLinkagePhaseAmbiguity()) < 0);
        return noWorse && atLeastOneBetter;
    }

}
