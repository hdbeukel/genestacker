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

package org.ugent.caagt.genestacker.log;

import org.apache.logging.log4j.message.Message;
import org.ugent.caagt.genestacker.util.TimeFormatting;

/**
 * Message logged when the search has stopped (includes runtime).
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class SearchStoppedMessage implements Message {

    // runtime in milliseconds
    private long runtime;
    
    public SearchStoppedMessage(long runtime){
        this.runtime = runtime;
    }
    
    @Override
    public String getFormattedMessage() {
        StringBuilder s = new StringBuilder();
        s.append("# Search stopped (runtime = ").append(TimeFormatting.formatTime(runtime)).append(") #");
        StringBuilder sep = new StringBuilder();
        for(int i=0; i<s.length(); i++){
            sep.append("#");
        }
        StringBuilder message = new StringBuilder();
        return message.append("\n")
                      .append(sep).append("\n")
                      .append(s).append("\n")
                      .append(sep).toString();
    }

    @Override
    public String getFormat() {
        return "";
    }

    @Override
    public Object[] getParameters() {
        return null;
    }

    @Override
    public Throwable getThrowable() {
        return null;
    }

}
