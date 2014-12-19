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
package org.ugent.caagt.genestacker.io;

/**
 * Color scheme used when creating output graphs with Graphviz.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public enum GraphColorScheme {
    
    GREYSCALE("black", "#aaaaaa", "#dddddd", "black"),
    COLORED("#fafafa", "#fb8335", "#399090", "red");

    private final String alleleFontColor;
    private final String bgColorOneAllele;
    private final String bgColorZeroAllele;
    private final String lpaTextColor;

    private GraphColorScheme(String alleleFontColor, String bgColorOneAllele, String bgColorZeroAllele, String lpaTextColor) {
        this.alleleFontColor = alleleFontColor;
        this.bgColorOneAllele = bgColorOneAllele;
        this.bgColorZeroAllele = bgColorZeroAllele;
        this.lpaTextColor = lpaTextColor;
    }

    public String getAlleleFontColor() {
        return alleleFontColor;
    }

    public String getBgColorOneAllele() {
        return bgColorOneAllele;
    }

    public String getBgColorZeroAllele() {
        return bgColorZeroAllele;
    }

    public String getLpaTextColor() {
        return lpaTextColor;
    }
    
    

}
