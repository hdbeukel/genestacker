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

package org.ugent.caagt.genestacker.cli;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

/**
 * Extends Posix parser to ignore unknown options if desired.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class ExtendedPosixParser extends PosixParser {

    private boolean ignoreUnrecognizedOption;

    public ExtendedPosixParser(final boolean ignoreUnrecognizedOption) {
        this.ignoreUnrecognizedOption = ignoreUnrecognizedOption;
    }

    @Override
    protected String[] flatten(Options options, String[] arguments, boolean stopAtNonOption) {
        String[] flat = super.flatten(options, arguments, stopAtNonOption);
        if(!ignoreUnrecognizedOption){
            // do not ignore unrecognized options: simply return flat options
            return flat;
        } else {
            // only retain known options and their parameters
            List<String> filtered = new ArrayList<>();
            int i=0;
            while(i < flat.length){
                // if known option, add it to the filtered list
                if(getOptions().hasOption(flat[i])){
                    filtered.add(flat[i]);
                    // add parameters of known option
                    for(int j=0; j<getOptions().getOption(flat[i]).getArgs(); j++){
                        i++;
                        filtered.add(flat[i]);
                    }
                }
                i++;
            }
            return filtered.toArray(new String[0]);
        }
    }
    
}
