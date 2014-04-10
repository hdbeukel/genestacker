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
 * Utility used to generate all options for the number of occurrences of a given series of events, where a
 * maximum number of occurrences is specified for each event. For example, in case of three events with maximum
 * occurrences [1,0,2], all possibilities within these bounds are: [0,0,0], [0,0,1], [0,0,2], [1,0,0], [1,0,1],
 * and [1,0,2].
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class EventOccurrences {
    
    // max occurences (inclusive)
    private int[] maxOcc;
    
    /**
     * Create a new instance, specifying an array of maximum occurrences (inclusive), one per event.
     * 
     * @param maxOcc maximum occurrences of events
     */
    public EventOccurrences(int[] maxOcc){
        this.maxOcc = maxOcc;
    }

    /**
     * Returns the first option in which each event occurs 0 times.
     * 
     * @return array containing only zeros, of appropriate length (number of events)
     */
    public int[] getFirst(){
        return new int[maxOcc.length];
    }
    
    /**
     * Transforms the current option into the next option. If all options have been generated,
     * <code>null</code> is returned. Modifies the input array.
     * 
     * @param cur current option
     * @return next option, <code>null</code> if none
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
