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
 * Exception indicating that duplicate constraints are given as input to the
 * search algorithm.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class DuplicateConstraintException extends SearchException {

    /**
     * Creates a new instance of <code>DuplicateConstraintException</code> without detail message.
     */
    public DuplicateConstraintException() {
    }


    /**
     * Constructs an instance of <code>DuplicateConstraintException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public DuplicateConstraintException(String msg) {
        super(msg);
    }
}
