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

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.ugent.caagt.genestacker.GeneticMap;
import org.ugent.caagt.genestacker.Genotype;
import org.ugent.caagt.genestacker.Plant;
import org.ugent.caagt.genestacker.exceptions.GenestackerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class GenestackerInputXMLWriter extends XMLWriter {

    // location of xml schema
    private final String XML_SCHEMA;
    
    public GenestackerInputXMLWriter() throws GenestackerException{
        XML_SCHEMA = GenestackerResourceBundle.getConfig("genestackerinput.xml.schema");
    }
    
    public void write(Plant[] initialPlants, Genotype ideotype, GeneticMap map, File outputFile) throws IOException {
        try {
            // create document
            Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            
            // create root element genestacker_input
            Element schemeEl = xml.createElement("genestacker_input");
            schemeEl.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            schemeEl.setAttribute("xsi:noNamespaceSchemaLocation", XML_SCHEMA);
            xml.appendChild(schemeEl);
            
            /******************/
            /* INITIAL PLANTS */
            /******************/

            Element initialPlantsEl = xml.createElement("initial_plants");
            for(Plant plant : initialPlants){
                Element plantEl = xml.createElement("plant");
                Element genotypeEl = createGenotypeElement(plant.getGenotype(), xml);
                plantEl.appendChild(genotypeEl);
                initialPlantsEl.appendChild(plantEl);
            }
            schemeEl.appendChild(initialPlantsEl);
            
            /************/
            /* IDEOTYPE */
            /************/
            
            Element ideotypeEl = xml.createElement("ideotype");
            Element genotypeEl = createGenotypeElement(ideotype, xml);
            ideotypeEl.appendChild(genotypeEl);
            schemeEl.appendChild(ideotypeEl);
            
            /***************/
            /* GENETIC MAP */
            /***************/
            
            Element geneticMapEl = xml.createElement("genetic_map");
            double[][] dist = map.getDistances();
            for(int c=0; c<dist.length; c++){
                Element distOnChromEl = xml.createElement("distances_on_chromosome");
                for(int d=0; d<dist[c].length; d++){
                    Element distEl = xml.createElement("dist");
                    distEl.setAttribute("cM", dist[c][d] + "");
                    distOnChromEl.appendChild(distEl);
                }
                geneticMapEl.appendChild(distOnChromEl);
            }
            schemeEl.appendChild(geneticMapEl);
            
            /*********************/
            /* WRITE XML TO FILE */
            /*********************/
            
            writeXML(xml, outputFile);
            
        } catch (ParserConfigurationException | TransformerException ex) {
           throw new IOException("Failed to write genestacker input xml file.", ex);
        }
    }
    
}
