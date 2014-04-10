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
 * Generates all k-subset, using the revolving door algorithm from "Combinatorial Algorithms: Generation,
 * Enumeration and Search", Donald Kreher and Douglas Stinson, CRC Press, 1999 (chapter 2, p. 43-52).
 * This algorithm generates k-subsets in a specific minimal change ordering called the revolving door
 * ordering.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class KSubsetGenerator {

    // nr of elements in subset
    private int k;
    // nr of elements in entire set
    private int n;

    /**
     * Create a k-subset generator that generates all subsets of k out of n elements.
     * 
     * @param k subset size
     * @param n full set size
     */
    public KSubsetGenerator(int k, int n){
        this.k = k;
        this.n = n;
    }

    /**
     * Create the first k-subset containing elements {1,2,...,k}.
     * 
     * @return first k-subset: [1,2,...,k]
     */
    public int[] first(){
        // Generate first k-subset
        int[] first = new int[k];
        for(int i=0; i<k; i++){
            first[i] = i+1;
        }
        return first;
    }

    /**
     * Transforms the current k-subset into its successor. In case of a rollover,
     * <code>null</code> is returned to indicate that all k-subsets have been generated.
     * 
     * @param curKSubset current k-subset
     * @return next k-subset
     */
    public int[] successor(int[] curKSubset){
        
        // special case: k = n --> only one ksubset (entire set) so no successors
        if(k == n){
            return null;
        }
        
        // normal case:
        
        int[] S = new int[k+2];
        S[0] = 0; // t_0
        for(int i = 1; i < k+1; i++) {
            S[i] = curKSubset[i-1];
        }
        S[k+1] = n+1; // t_{k+1}

        int j = 1;
        while(j <= k && S[j] == j) {
            j++;
        }
        if(k % 2 != j % 2) {
            if(j == 1) {
                S[1]--;
            } else {
                S[j-1] = j;
                S[j-2] = j-1;
            }
        } else {
            if(S[j+1] != S[j]+1) {
                S[j-1] = S[j];
                S[j] = S[j]+1;
            } else {
                S[j+1] = S[j];
                S[j] = j;
            }
        }

        for(int i = 1; i < k+1; i++) {
            curKSubset[i-1] = S[i];
        }
        
        // check for roll over
        if(curKSubset[k-1] == k){
            // roll over --> give termination signal
            return null;
        } else {
            // return successor
            return curKSubset;
        }
    }
}
