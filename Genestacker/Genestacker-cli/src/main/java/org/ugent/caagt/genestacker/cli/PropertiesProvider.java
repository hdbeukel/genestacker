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

import java.util.ResourceBundle;

/**
 * Provides Gene Stacker CLI properties.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class PropertiesProvider {

    // singleton
    private static final PropertiesProvider INSTANCE = new PropertiesProvider();
    
    // resource bundle
    private final ResourceBundle rb;
    
    // private constructor
    private PropertiesProvider(){
        // read resource bundle
        rb = ResourceBundle.getBundle("genestacker-cli");
    }
    
    /**
     * Get Gene Stacker CLI version.
     * @return version
     */
    public static String getVersion(){
        return INSTANCE.rb.getString("genestacker.cli.version");
    }
    
}
