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

package org.ugent.caagt.genestacker.exceptions;

/**
 *
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class GenestackerException extends Exception {

    /**
     * Creates a new instance of <code>GenestackerException</code> without detail message.
     */
    public GenestackerException() {
    }


    /**
     * Constructs an instance of <code>GenestackerException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public GenestackerException(String msg) {
        super(msg);
    }
    
    /**
     * Constructs an instance of <code>GenestackerException</code> with the specified detail message,
     * and throwable that caused this exception.
     * @param msg the detail message.
     * @param t cause of this exception
     */
    public GenestackerException(String msg, Throwable t){
        super(msg, t);
    }
    
}
