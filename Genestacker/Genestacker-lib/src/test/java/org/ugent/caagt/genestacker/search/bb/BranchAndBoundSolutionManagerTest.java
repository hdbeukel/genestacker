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

import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.junit.Test;
import org.ugent.caagt.genestacker.DiploidChromosome;
import org.ugent.caagt.genestacker.Genotype;
import org.ugent.caagt.genestacker.Haplotype;
import org.ugent.caagt.genestacker.exceptions.DuplicateConstraintException;
import org.ugent.caagt.genestacker.exceptions.GenestackerException;
import org.ugent.caagt.genestacker.search.constraints.Constraint;
import org.ugent.caagt.genestacker.search.constraints.MaxCrossingsWithPlant;

/**
 *
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class BranchAndBoundSolutionManagerTest extends TestCase {

     @Test
     public void testExceptions() throws GenestackerException{
         // test max crossings with plant exception
         // test duplicate constraint exception
         List<Constraint> constraints = new ArrayList<>();
         constraints.add(new MaxCrossingsWithPlant(4));
         constraints.add(new MaxCrossingsWithPlant(6));
         Haplotype hom = new Haplotype(new boolean[]{true, true, true});
         DiploidChromosome chrom = new DiploidChromosome(hom, hom);
         List<DiploidChromosome> chroms = new ArrayList<>();
         chroms.add(chrom);
         Genotype ideotype = new Genotype(chroms);
         boolean thrown = false;
         try{
             BranchAndBoundSolutionManager f = new BranchAndBoundSolutionManager(null, ideotype, null, null,
                                                                                    constraints, null, null, false);
         } catch(DuplicateConstraintException ex){
             thrown = true;
         }
         assertTrue(thrown);
     }

}