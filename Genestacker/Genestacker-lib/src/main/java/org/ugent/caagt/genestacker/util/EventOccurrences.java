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
 * Used to generate the different options for the number of occurrences of a given
 * series of events, with a specific maximum number of occurrences for each event.
 * 
 * @author Herman De Beukelaer <herman.debeukelaer@ugent.be>
 */
public class EventOccurrences {
    
    // max occurences (inclusive)
    private int[] maxOcc;
    
    public EventOccurrences(int[] maxOcc){
        this.maxOcc = maxOcc;
    }

    /**
     * Returns the first option in which each event occurs 0 times.
     */
    public int[] getFirst(){
        return new int[maxOcc.length];
    }
    
    /**
     * Transforms the current option into the next option for the number
     * of occurrences respecting the maxima. Modifies the input array.
     */
    public int[] successor(int[] cur){
        // go from right to left and look for first position which can be increased
        int i = cur.length-1;
        boolean inc = false;
        while(!inc && i>=0){
            inc = cur[i] < maxOcc[i];
            i--; // (*)
        }
        if(!inc){
            return null; // no successor remains
        } else {
            i++; // undo (*)
            // increase pos i
            cur[i]++;
            // set trailing numbers to 0
            i++;
            while(i < cur.length){
                cur[i] = 0;
                i++;
            }
        }
        // return transformed array
        return cur;
    }
    
}
