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
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.ugent.caagt.genestacker.exceptions.GenestackerException;
import org.ugent.caagt.genestacker.search.CrossingNode;
import org.ugent.caagt.genestacker.search.CrossingScheme;
import org.ugent.caagt.genestacker.search.PlantNode;
import org.ugent.caagt.genestacker.search.SeedLotNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Responsible for writing crossing schemes to XML files.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class CrossingSchemeXMLWriter extends XMLWriter {

    // location of xml schema
    private final String XML_SCHEMA;
    
    public CrossingSchemeXMLWriter() throws GenestackerException{
        XML_SCHEMA = GenestackerResourceBundle.getConfig("crossingscheme.xml.schema");
    }
    
    public void write(CrossingScheme scheme, File outputFile) throws IOException {
        try {
            // create document
            Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            
            // create root element crossing_scheme
            Element schemeEl = xml.createElement("crossing_scheme");
            schemeEl.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            schemeEl.setAttribute("xsi:noNamespaceSchemaLocation", XML_SCHEMA);
            schemeEl.setAttribute("gamma", "" + scheme.getPopulationSizeTools().getGlobalSuccessRate());
            schemeEl.setAttribute("gammaPrime", "" + scheme.getPopulationSizeTools().computeDesiredSuccessProbPerTarget(scheme.getNumTargetsFromNonUniformSeedLots()));
            schemeEl.setAttribute("numGen", "" + scheme.getNumGenerations());
            schemeEl.setAttribute("totalPopSize", "" + scheme.getTotalPopulationSize());
            schemeEl.setAttribute("lpa", "" + scheme.getLinkagePhaseAmbiguity());
            xml.appendChild(schemeEl);
            
            /************/
            /* SEEDLOTS */
            /************/
            
            Element seedlotsEl = xml.createElement("seedlots");
            schemeEl.appendChild(seedlotsEl);
            // create seedlot elements
            for(SeedLotNode sln : scheme.getSeedLotNodes()){
                Element seedlotEl = xml.createElement("seedlot");
                seedlotEl.setAttribute("id", sln.getUniqueID());
                seedlotEl.setAttribute("generation", sln.getGeneration() + "");
                seedlotsEl.appendChild(seedlotEl);
                // create "used_seeds" child elements for this seed lot
                Map<Integer, Long> usedSeeds = sln.getSeedsTakenFromSeedLotPerGeneration();
                for(int g : usedSeeds.keySet()){
                    Element usedSeedsEl = xml.createElement("used_seeds");
                    usedSeedsEl.setAttribute("generation", "" + g);
                    usedSeedsEl.setAttribute("amount", "" + usedSeeds.get(g));
                    seedlotEl.appendChild(usedSeedsEl);
                }
            }
            
            /**********/
            /* PLANTS */
            /**********/
            
            Element plantsEl = xml.createElement("plants");
            schemeEl.appendChild(plantsEl);
            // create plant elements
            for(PlantNode pn : scheme.getPlantNodes()){
                Element plantEl = xml.createElement("plant");
                plantEl.setAttribute("id", pn.getUniqueID());
                plantEl.setAttribute("seedlot", pn.getParent().getUniqueID());
                plantEl.setAttribute("generation", pn.getGeneration() + "");
                plantEl.setAttribute("obsProb", "" + pn.getProbabilityOfPhaseKnownGenotype());
                plantEl.setAttribute("lpa", "" + pn.getLinkagePhaseAmbiguity());
                plantEl.setAttribute("duplicates", "" + pn.getNumDuplicates());
                plantsEl.appendChild(plantEl);
                // add genotype to plant
                Element genotypeEl = createGenotypeElement(pn.getPlant().getGenotype(), xml);
                plantEl.appendChild(genotypeEl);
            }
            
            /*************/
            /* CROSSINGS */
            /*************/
            
            Element crossingsEl = xml.createElement("crossings");
            schemeEl.appendChild(crossingsEl);
            // create crossing elements
            for(CrossingNode crossing : scheme.getCrossingNodes()){
                Element crossingEl;
                if(crossing.isSelfing()){
                    crossingEl = xml.createElement("selfing");
                    crossingEl.setAttribute("plant", crossing.getParent1().getUniqueID());
                } else {
                    crossingEl = xml.createElement("crossing");
                    crossingEl.setAttribute("plant1", crossing.getParent1().getUniqueID());
                    crossingEl.setAttribute("plant2", crossing.getParent2().getUniqueID());
                }
                crossingEl.setAttribute("seedlot", crossing.getChild().getUniqueID());
                crossingEl.setAttribute("id", crossing.getUniqueID());
                crossingEl.setAttribute("duplicates", "" + crossing.getNumDuplicates());
                crossingsEl.appendChild(crossingEl);
            }
            
            /*********************/
            /* WRITE XML TO FILE */
            /*********************/
            
            writeXML(xml, outputFile);
            
        } catch (ParserConfigurationException | TransformerException ex) {
           throw new IOException("Failed to write crossing scheme xml representation.", ex);
        }
    }
}
