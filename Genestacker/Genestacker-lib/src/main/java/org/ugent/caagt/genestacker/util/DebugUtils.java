//  Copyright 2014 Herman De Beukelaer
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

import java.util.Scanner;

/**
 * Utilities for debugging.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class DebugUtils {

    /**
     * Calling this method waits until the user presses enter (scans standard input for next line), writing
     * a default message to standard output: "[Press enter to continue]".
     */
    public static void waitForEnter(){
        waitForEnter("[Press enter to continue]");
    }
    
    /**
     * Calling this method waits until the user presses enter (scans standard input for next line), writing
     * a given message to standard output.
     * 
     * @param message message to write to standard output before waiting for enter
     */
    public static void waitForEnter(String message){
        Scanner in = new Scanner(System.in);
        System.out.print(message);
        in.nextLine();
    }
    
}
