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

import java.text.DecimalFormat;
import java.util.Arrays;
import junit.framework.TestCase;
import org.junit.Test;

/**
 *
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class GeneticMapTest extends TestCase {
    
    public GeneticMapTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testGeneticMap(){
        
        System.out.println("\n### GENETIC MAP TEST ###\n");
        
        double[][] d = new double[][]{new double[]{20, 20}, new double[]{}, new double[]{1}};
        
        System.out.println("Distances:\n");
        System.out.println(Arrays.deepToString(d));
        
        GeneticMap map = new GeneticMap(d, new HaldaneMapFunction());
        
        System.out.println("\nRecombination factors:\n");
        double[][][] r = map.getRecombinationProbabilities();
        for(int i=0; i<r.length; i++){
            System.out.println("Chromosome " + (i+1) + ":");
            for(int j=0; j<r[i].length; j++){
                for(int k=0; k<r[i][j].length; k++){
                    DecimalFormat df = new DecimalFormat("#.#####");
                    System.out.print(df.format(r[i][j][k]) + " ");
                }
                System.out.println(""); 
           }
        } 
       
        // tests
        
        // there should be 3 chromosomes
        assertEquals(3, r.length);
        // chromosomes have 3, 1 and 2 targets respectively,
        // i.e. 2, 0 and 1 inter marker distances/probs
        assertEquals(2, r[0].length);
        assertEquals(0, r[1].length);
        assertEquals(1, r[2].length);
        // check recombination probabilities
        double precision = 0.001;

        // chrom 1
        
        assertEquals(0.165, r[0][0][0], precision);
        assertEquals(0.275, r[0][1][0], precision);
        assertEquals(0.165, r[0][1][1], precision);
        
        assertEquals(0.165, map.getRecombinationProbability(0, 0, 1), precision);
        assertEquals(0.165, map.getRecombinationProbability(0, 1, 0), precision);
        assertEquals(0.165, map.getRecombinationProbability(0, 1, 2), precision);
        assertEquals(0.165, map.getRecombinationProbability(0, 2, 1), precision);
        assertEquals(0.275, map.getRecombinationProbability(0, 0, 2), precision);
        assertEquals(0.275, map.getRecombinationProbability(0, 2, 0), precision);
        assertEquals(0.0, map.getRecombinationProbability(0, 0, 0), precision);
        assertEquals(0.0, map.getRecombinationProbability(0, 1, 1), precision);
        assertEquals(0.0, map.getRecombinationProbability(0, 2, 2), precision);

        // chrom 2
        // --> only one target, no inter marker distances/probs
        
        // chrom3
        assertEquals(0.01, r[2][0][0], precision);
        assertEquals(0.01, map.getRecombinationProbability(2, 0, 1), precision);
        assertEquals(0.01, map.getRecombinationProbability(2, 1, 0), precision);
        assertEquals(0.0, map.getRecombinationProbability(2, 0, 0), precision);
        assertEquals(0.0, map.getRecombinationProbability(2, 1, 1), precision);
        
    }
}
