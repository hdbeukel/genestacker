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

/**
 * Output file formats for generated graph visualisations using Graphviz.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public enum GraphFileFormat {
    
    PDF("pdf"),
    EPS("eps"),
    PS("ps"),
    SVG("svg"),
    PNG("png"),
    BMP("bmp"),
    GIF("gif"),
    JPG("jpg");

    private String formatString;

    private GraphFileFormat(String formatString){
        this.formatString = formatString;
    }

    public String getFormatString(){
        return formatString;
    }

    @Override
    public String toString(){
        return formatString;
    }
    
}
