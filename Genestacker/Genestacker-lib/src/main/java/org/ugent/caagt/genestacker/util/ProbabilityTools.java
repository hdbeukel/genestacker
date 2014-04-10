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

package org.ugent.caagt.genestacker.util;

/**
 * Utilities for computing advanced probabilities.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class ProbabilityTools {

    /**
     * Computes the probability of obtaining each of the events i at most <code>maxOcc[i]</code> times (inclusive),
     * in a total of n Bernoulli trials, where the probability of obtaining event i is equal to <code>probs[i]</code>.
     * 
     * @param probs probabilities of the events
     * @param maxOcc maximum number of occurrences (inclusive) of each event
     * @param n number of Bernoulli trials
     * @return probability that each event will be obtained at most the desired maximum number of times, in n trials
     */
    public double computeProbMaxOcc(double[] probs, int[] maxOcc, long n){
        // check input
        verifyInput(probs, maxOcc, n);
        
        // compute probability
        
        double probSum = 0.0;
        for(Double p : probs){
            probSum += p;
        }
        EventOccurrences ev = new EventOccurrences(maxOcc);
        int[] curOcc = ev.getFirst();
        // sum over probability of success for all distinct assignments of number
        // of occurrences of each event, respecting the maxima
        double prob = 0.0;
        while(curOcc != null){
            // compute term of success prob
            double term = 1;
            int occSum = 0;
            int k=0;
            for(int i=0; i<curOcc.length; i++){
                occSum += curOcc[i];
                for(int j=0; j<curOcc[i]; j++){
                    term = term * (n-k) * probs[i] / (curOcc[i]-j);
                    k++;
                }
            }
            term = term * Math.pow(1-probSum, n-occSum);
            // add term
            prob += term;
            // compute successor
            curOcc = ev.successor(curOcc);
        }
        return prob;
    }
 
    /**
     * Computes the probability of obtaining each of the events i at least <code>minOcc[i]</code> times (inclusive),
     * in a total of n Bernoulli trials, where the probability of obtaining event i is equal to <code>probs[i]</code>.
     * 
     * @param probs probabilities of the events
     * @param minOcc minimum number of occurrences (inclusive) of each event
     * @param n number of Bernoulli trials
     * @return probability that each event will be obtained at least the desired minimum number of times, in n trials
     */
    public double computeProbMinOcc(double[] probs, int[] minOcc, long n){
         // check input
        verifyInput(probs, minOcc, n);
        
        // compute probability
        
        double prob = 1.0;
        int numEvents = probs.length;
        int sign = -1;
        for(int k=1; k<=numEvents; k++){
            double term = 0.0;
            double[] ksubProbs = new double[k];
            int[] ksubMaxOcc = new int[k];
            // sum over negated prob of all ksubsets
            KSubsetGenerator ksubgen = new KSubsetGenerator(k, numEvents);
            int[] ksub = ksubgen.first();
            while(ksub != null){
                // select ksubset
                for(int i=0; i<k; i++){
                    ksubProbs[i] = probs[ksub[i]-1];
                    ksubMaxOcc[i] = minOcc[ksub[i]-1] - 1;
                }
                // add to term
                term += computeProbMaxOcc(ksubProbs, ksubMaxOcc, n);
                // compute next ksubset
                ksub = ksubgen.successor(ksub);
            }
            // set correct sign
            term = term * sign;
            // add/subtract (depending on sign) term to prob
            prob += term;
            // update sign
            sign = -sign;
        }
        
        return prob;
    }
    
    
    private void verifyInput(double[] probs, int[] occ, long n){
        if(probs.length != occ.length){
            throw new IllegalArgumentException("Illegal use of ProbabilityTools: the given number of probabilities does not match the given number of desired occurrences of events");
        }
        // check probs and occ
        double probSum = 0.0;
        int maxOccSum = 0;
        for(int i=0; i<probs.length; i++){
            if(probs[i] < 0.0 || probs[i] > 1.0){
                throw new IllegalArgumentException("Illegal use of ProbabilityTools: probabilities should be real numbers between 0.0 and 1.0");
            }
            probSum += probs[i];
            if(occ[i] < 0 || occ[i] > n){
                throw new IllegalArgumentException("Illegal use of ProbabilityTools: number of occurrences should be integers between 0 and n");
            }
            maxOccSum += occ[i];
        }
        // verify sums
        if(probSum > 1.0){
            throw new IllegalArgumentException("Illegal use of ProbabilityTools: the sum of the probabilities of all desired events should not exceed 1.0");
        }
        if(maxOccSum > n){
            throw new IllegalArgumentException("Illegal use of ProbabilityTools: total number of trials n too low for desired number of occurences of events");
        }
    }
    
}
