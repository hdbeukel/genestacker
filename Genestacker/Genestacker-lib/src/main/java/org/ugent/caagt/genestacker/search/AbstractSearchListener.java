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

/**
 * Abstract search listener that ignores all events. Extend this class to
 * implement real search listeners, overriding the appropriate method(s).
 *
 * @author Herman De Beukelaer <herman.debeukelaer@ugent.be>
 */
public class AbstractSearchListener implements SearchListener {

    @Override
    public void searchMessage(String message, SearchMessageType type) {
        // ignore event
    }
    
    @Override
    public void searchStarted(){
        // ignore event
    }
    
    @Override
    public void searchStopped(){
        // ignore event
    }

}
