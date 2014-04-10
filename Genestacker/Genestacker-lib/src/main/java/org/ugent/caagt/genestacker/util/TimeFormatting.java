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
 *
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class TimeFormatting {
    
    public static String formatTime(long millis){
        long hours = millis/3600000;
        millis -= 3600000*hours;
        long min = millis/ 60000;
        millis -= 60000*min;
        long sec = millis/1000;
        millis -= 1000*sec;
        return hours + "h " + min + "m " + sec + "s " + millis + "ms";
    }

}
