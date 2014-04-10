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

package org.ugent.caagt.genestacker.io;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import org.ugent.caagt.genestacker.exceptions.GenestackerException;

/**
 * Interface to access config entries from the Gene Stacker config file. Initialises the
 * default config file in the user's home directory if not yet present.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class GenestackerResourceBundle {

    public static String getConfig(String key) throws GenestackerException{
        try{
            // check presence of config file in home directory
            String home = System.getProperty("user.home");
            Path dir = Paths.get(home, "genestacker");
            if(Files.notExists(dir)){
                // create config folder in user home dir
                Files.createDirectory(dir);
            }
            Path config = dir.resolve("config.properties");
            if(Files.notExists(config)){
                // init default config file
                Files.copy(GenestackerResourceBundle.class.getResourceAsStream("/config_template.properties"), config);
            }
            // now read config file
            Properties props = new Properties();
            String value;
            try (FileInputStream fis = new FileInputStream(config.toFile())) {
                props.load(fis);
                value = props.getProperty(key);
            }
            if (value == null){
                // invalid config file structure
                throw new GenestackerException("Genestacker config file ~/genestacker/config.properties is corrupt. To reset default configs: delete config file and restart the software.");
            }
            return value;
        } catch (IOException ex){
            throw new GenestackerException("Problem reading genestacker config file.", ex);
        }
    }

}
