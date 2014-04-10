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
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.ugent.caagt.genestacker.exceptions.GenestackerException;

/**
 *
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class ComputeMatchingLoci {

    @Test
    public void computeMatchingLoci() throws GenestackerException {

        System.out.println("");
        System.out.println("### Computing average matching loci ###");
        System.out.println("");
        
        /***************************************************/
        
        // test6a & test7a
        
        List<String[]> parents = new ArrayList<>();
        
        parents.add(new String[]{"11 0 1 11 00 111 1 10", "00 0 1 11 00 000 1 10"});
        parents.add(new String[]{"00 1 1 11 00 100 0 10", "00 0 0 00 00 000 0 00"});
        parents.add(new String[]{"00 0 1 11 00 100 0 10", "00 0 1 11 00 100 0 10"});
        parents.add(new String[]{"00 0 1 11 00 000 1 10", "00 0 1 11 00 000 1 10"});
        parents.add(new String[]{"00 1 1 11 00 000 0 10", "00 0 0 00 00 000 0 00"});
        parents.add(new String[]{"00 0 1 11 00 000 0 10", "00 0 1 11 00 000 0 10"});
        parents.add(new String[]{"00 0 1 11 11 100 0 01", "00 0 1 11 11 100 0 01"});
        parents.add(new String[]{"00 1 0 00 00 000 0 00", "00 1 0 00 00 000 0 00"});
        
        System.out.println("---");
        String[] homIdeotype = new String[]{"11 1 1 11 11 111 1 11", "11 1 1 11 11 111 1 11"};
        printAvgMatchingLoci(parents, homIdeotype);
        String[] hetIdeotype = new String[]{"10 1 1 11 01 000 1 00", "11 1 1 11 11 111 1 11"};
        printAvgMatchingLoci(parents, hetIdeotype);
        System.out.println("---");
        
        /***************************************************/
        
        // test6b & test7b
        
        parents = new ArrayList<>();
        
        parents.add(new String[]{"0 1 11 00 111 1", "0 1 11 00 000 1"});
        parents.add(new String[]{"1 1 11 00 100 0", "0 0 00 00 000 0"});
        parents.add(new String[]{"0 1 11 00 100 0", "0 1 11 00 100 0"});
        parents.add(new String[]{"0 1 11 00 000 1", "0 1 11 00 000 1"});
        parents.add(new String[]{"1 1 11 00 000 0", "0 0 00 00 000 0"});
        parents.add(new String[]{"0 1 11 00 000 0", "0 1 11 00 000 0"});
        parents.add(new String[]{"0 1 11 11 100 0", "0 1 11 11 100 0"});
        parents.add(new String[]{"1 0 00 00 000 0", "1 0 00 00 000 0"});
        
        System.out.println("---");
        homIdeotype = new String[]{"1 1 11 11 111 1", "1 1 11 11 111 1"};
        printAvgMatchingLoci(parents, homIdeotype);
        hetIdeotype = new String[]{"1 1 11 01 000 1", "1 1 11 11 111 1"};
        printAvgMatchingLoci(parents, hetIdeotype);
        System.out.println("---");
        
        /***************************************************/
        
        // test6c & test7c
        
        parents = new ArrayList<>();
        
        parents.add(new String[]{"0 1 00 111 1", "0 1 00 000 1"});
        parents.add(new String[]{"1 1 00 100 0", "0 0 00 000 0"});
        parents.add(new String[]{"0 1 00 100 0", "0 1 00 100 0"});
        parents.add(new String[]{"0 1 00 000 1", "0 1 00 000 1"});
        parents.add(new String[]{"1 1 00 000 0", "0 0 00 000 0"});
        parents.add(new String[]{"0 1 00 000 0", "0 1 00 000 0"});
        parents.add(new String[]{"0 1 11 100 0", "0 1 11 100 0"});
        parents.add(new String[]{"1 0 00 000 0", "1 0 00 000 0"});
        
        System.out.println("---");
        homIdeotype = new String[]{"1 1 11 111 1", "1 1 11 111 1"};
        printAvgMatchingLoci(parents, homIdeotype);
        hetIdeotype = new String[]{"1 1 01 000 1", "1 1 11 111 1"};
        printAvgMatchingLoci(parents, hetIdeotype);
        System.out.println("---");
        
        /***************************************************/
        
        // test6d & test7d
        
        parents = new ArrayList<>();
        
        parents.add(new String[]{"0 00 111", "0 00 000"});
        parents.add(new String[]{"1 00 100", "0 00 000"});
        parents.add(new String[]{"0 00 100", "0 00 100"});
        parents.add(new String[]{"0 00 000", "0 00 000"});
        parents.add(new String[]{"1 00 000", "0 00 000"});
        parents.add(new String[]{"0 00 000", "0 00 000"});
        parents.add(new String[]{"0 11 100", "0 11 100"});
        parents.add(new String[]{"1 00 000", "1 00 000"});
        
        System.out.println("---");
        homIdeotype = new String[]{"1 11 111", "1 11 111"};
        printAvgMatchingLoci(parents, homIdeotype);
        hetIdeotype = new String[]{"1 01 000", "1 11 111"};
        printAvgMatchingLoci(parents, hetIdeotype);
        System.out.println("---");
        
        /***************************************************/
        
        // test6e & test7e
        
        parents = new ArrayList<>();
        
        parents.add(new String[]{"0 111", "0 000"});
        parents.add(new String[]{"1 100", "0 000"});
        parents.add(new String[]{"0 100", "0 100"});
        parents.add(new String[]{"0 000", "0 000"});
        parents.add(new String[]{"1 000", "0 000"});
        parents.add(new String[]{"0 000", "0 000"});
        parents.add(new String[]{"0 100", "0 100"});
        parents.add(new String[]{"1 000", "1 000"});
        
        System.out.println("---");
        homIdeotype = new String[]{"1 111", "1 111"};
        printAvgMatchingLoci(parents, homIdeotype);
        hetIdeotype = new String[]{"1 000", "1 111"};
        printAvgMatchingLoci(parents, hetIdeotype);
        System.out.println("---");
        
        /***************************************************/
        
        // test8a
        
        parents = new ArrayList<>();
        
        parents.add(new String[]{"11 0 1 00 00 001 1 10", "11 0 1 00 00 001 1 10"});
        parents.add(new String[]{"00 1 0 11 00 001 0 10", "00 1 0 11 00 000 0 10"});
        parents.add(new String[]{"01 0 1 00 00 010 0 10", "01 0 1 10 00 010 0 10"});
        parents.add(new String[]{"00 0 1 01 00 000 1 10", "00 0 1 01 00 000 1 10"});
        parents.add(new String[]{"00 1 1 10 00 010 0 10", "00 1 1 00 00 010 0 10"});
        parents.add(new String[]{"00 0 0 01 01 000 0 11", "00 0 0 11 01 000 0 11"});
        parents.add(new String[]{"00 0 1 00 11 001 0 01", "00 0 1 00 11 001 0 01"});
        parents.add(new String[]{"10 1 0 00 01 100 0 01", "00 1 0 00 01 000 0 01"});
        
        System.out.println("---");
        homIdeotype = new String[]{"11 1 1 11 11 111 1 11", "11 1 1 11 11 111 1 11"};
        printAvgMatchingLoci(parents, homIdeotype);
        System.out.println("---");
        
        /***************************************************/
        
        // test8b
        
        parents = new ArrayList<>();
        
        parents.add(new String[]{"0 1 00 00 001 1", "0 1 00 00 001 1"});
        parents.add(new String[]{"1 0 11 00 001 0", "1 0 11 00 000 0"});
        parents.add(new String[]{"0 1 00 00 010 0", "0 1 10 00 010 0"});
        parents.add(new String[]{"0 1 01 00 000 1", "0 1 01 00 000 1"});
        parents.add(new String[]{"1 1 10 00 010 0", "1 1 00 00 010 0"});
        parents.add(new String[]{"0 0 01 01 000 0", "0 0 11 01 000 0"});
        parents.add(new String[]{"0 1 00 11 001 0", "0 1 00 11 001 0"});
        parents.add(new String[]{"1 0 00 01 100 0", "1 0 00 01 000 0"});
        
        System.out.println("---");
        homIdeotype = new String[]{"1 1 11 11 111 1", "1 1 11 11 111 1"};
        printAvgMatchingLoci(parents, homIdeotype);
        System.out.println("---");
        
        /***************************************************/
        
        // test8c
        
        parents = new ArrayList<>();
        
        parents.add(new String[]{"0 1 00 001 1", "0 1 00 001 1"});
        parents.add(new String[]{"1 0 00 001 0", "1 0 00 000 0"});
        parents.add(new String[]{"0 1 00 010 0", "0 1 00 010 0"});
        parents.add(new String[]{"0 1 00 000 1", "0 1 00 000 1"});
        parents.add(new String[]{"1 1 00 010 0", "1 1 00 010 0"});
        parents.add(new String[]{"0 0 01 000 0", "0 0 01 000 0"});
        parents.add(new String[]{"0 1 11 001 0", "0 1 11 001 0"});
        parents.add(new String[]{"1 0 01 100 0", "1 0 01 000 0"});
        
        System.out.println("---");
        homIdeotype = new String[]{"1 1 11 111 1", "1 1 11 111 1"};
        printAvgMatchingLoci(parents, homIdeotype);
        System.out.println("---");
        
        /***************************************************/
        
        System.out.println("");
        
    }
    
    public void printAvgMatchingLoci(List<String[]> parents, String[] ideotype) throws GenestackerException {
        // convert ideotype string to genotype
        Genotype gIdeotype = ComputeLPA.convertToGenotype(ideotype);
        // compute and sum number of matching loci for each parent genotype
        int matching = 0;
        for(String[] parent : parents){
            // convert parent to genotype
            Genotype gParent = ComputeLPA.convertToGenotype(parent);
            // compute number of matching loci
            matching += computeNumMatchingLoci(gParent, gIdeotype);
        }
        // divide by number of parents
        double avg = (double) matching / parents.size();
        // convert to percentage w.r.t. number of loci (times two -- to account for both haplotypes)
        avg = avg / (2*gIdeotype.nrOfLoci()) * 100;
        // print results
        DecimalFormat df = new DecimalFormat("#.##");
        System.out.println("Avg. matching loci: " + df.format(avg) + "%");
    }
    
    public int computeNumMatchingLoci(Genotype genotype, Genotype ideotype){
        int matching = 0;
        // go through chromosomes
        for(int c=0; c<genotype.nrOfChromosomes(); c++){
            DiploidChromosome gChrom = genotype.getChromosomes().get(c);
            DiploidChromosome iChrom = ideotype.getChromosomes().get(c);
            // go through loci
            for(int l=0; l<gChrom.nrOfLoci(); l++){
                // check for matching locus at both haplotypes
                for(int h=0; h<2; h++){
                    boolean target = gChrom.getHaplotypes()[h].targetPresent(l);
                    if(iChrom.getHaplotypes()[0].targetPresent(l) == target
                            || iChrom.getHaplotypes()[1].targetPresent(l) == target){
                        // match!
                        matching++;
                    }
                }
            }
        }
        return matching;
    }

}