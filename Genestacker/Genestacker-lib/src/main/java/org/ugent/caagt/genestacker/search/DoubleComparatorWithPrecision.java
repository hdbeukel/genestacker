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

import java.util.Comparator;

/**
 * Compares doubles up to a certain precision. By default, the precision is set
 * to 0.0001.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class DoubleComparatorWithPrecision implements Comparator<Double> {

    // default precision
    private static final double DEFAULT_PRECISION = 0.0001;
    
    // precision
    private final double precision;
    
    public DoubleComparatorWithPrecision(){
        this(DEFAULT_PRECISION);
    }
    
    public DoubleComparatorWithPrecision(double precision){
        this.precision = precision;
    }
    
    @Override
    public int compare(Double d1, Double d2) {
        double delta = d1-d2;
        if(delta >= precision){
            // d1 > d2, given precision
            return 1;
        } else if(delta <= -precision){
            // d1 < d2, given precision
            return -1;
        } else {
            // equal, given precision
            return 0;
        }
    }

}
