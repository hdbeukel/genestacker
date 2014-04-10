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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.ugent.caagt.genestacker.DiploidChromosome;
import org.ugent.caagt.genestacker.GeneticMap;
import org.ugent.caagt.genestacker.Genotype;
import org.ugent.caagt.genestacker.Haplotype;
import org.ugent.caagt.genestacker.KosambiMapFunction;
import org.ugent.caagt.genestacker.Plant;
import org.ugent.caagt.genestacker.exceptions.GenotypeException;
import org.ugent.caagt.genestacker.exceptions.XMLFormatException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Responsible of parsing XML files containing the input for Gene Stacker.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class GenestackerInputParser {

    public GenestackerInput parse(File xmlFile) throws IOException, XMLFormatException {
        return parse(xmlFile, false);
    }
    
    /**
     * Parse the given XML file and return an instance of GenestackerInput.
     * 
     * @param xmlFile XML file containing the input
     * @param useKosambiMap indicates whether to use the Kosambi mapping function
     *                      instead of the default Haldane map to convert distances
     *                      to crossover rates
     * @return parsed Gene Stacker input
     * 
     * @throws IOException if any IO errors occur
     * @throws XMLFormatException if the input file is not formatted correctly
     */
    public GenestackerInput parse(File xmlFile, boolean useKosambiMap) throws IOException, XMLFormatException {
        
        try {
            
            // validate XML against schema
            SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
            URL schemaURL = GenestackerInputParser.class.getResource("/genestacker_input.xsd");
            Schema schema = factory.newSchema(schemaURL);
            Validator validator = schema.newValidator();
            Source source = new StreamSource(xmlFile);
            validator.validate(source);
            
            // create DOM document
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(xmlFile);
            
            // initialize initial plant list
            List<Plant> plants = new ArrayList<>();
            
            // parse initial plants
            NodeList plantEls = doc.getDocumentElement().getElementsByTagName("plant");
            if(plantEls != null && plantEls.getLength() > 0){
                for(int i=0; i<plantEls.getLength(); i++){
                    Element plantEl = (Element) plantEls.item(i);
                    Genotype g = parsePlantGenotype(plantEl);
                    Plant p = new Plant(g);
                    plants.add(p);
                }
            } else {
                throw new XMLFormatException();
            }
            
            // parse ideotype
            Genotype ideotype;
            NodeList ideotypeEls = doc.getDocumentElement().getElementsByTagName("ideotype");
            if(ideotypeEls != null && ideotypeEls.getLength() == 1){
                Element ideotypeEl = (Element) ideotypeEls.item(0);
                ideotype = parsePlantGenotype(ideotypeEl);
            } else {
                throw new XMLFormatException();
            }
            
            // parse genetic map
            GeneticMap map;
            NodeList geneticMapEls = doc.getDocumentElement().getElementsByTagName("genetic_map");
            if(geneticMapEls != null && geneticMapEls.getLength() == 1){
                Element geneticMapEl = (Element) geneticMapEls.item(0);
                map = parseGeneticMap(geneticMapEl, useKosambiMap);
            } else {
                throw new XMLFormatException();
            }
            
            // return GenestackerInput instance
            return new GenestackerInput(plants, ideotype, map);
            
        } catch (ParserConfigurationException | SAXException | XMLFormatException | GenotypeException ex) {
            throw new XMLFormatException("XML file '" + xmlFile.getName() + "' contains invalid input:\n" + ex.getMessage());
        }
        
    }
    
    private Genotype parsePlantGenotype(Element plantEl) throws XMLFormatException, GenotypeException {
        // parse genotype
        NodeList genotypeEls = plantEl.getElementsByTagName("genotype");
        if(genotypeEls != null && genotypeEls.getLength() == 1){
            Element genotypeEl = (Element) genotypeEls.item(0);
            // init chrom list
            List<DiploidChromosome> chroms = new ArrayList<>();
            // parse chromosomes
            NodeList chromEls = genotypeEl.getElementsByTagName("chromosome");
            if(chromEls != null && chromEls.getLength() > 0){
                for(int i=0; i<chromEls.getLength(); i++){
                    Element chromEl = (Element) chromEls.item(i);
                    // get haplotypes
                    NodeList homEls = chromEl.getElementsByTagName("haplotype");
                    if(homEls != null && homEls.getLength() == 2){
                        Element homEl1 = (Element) homEls.item(0);
                        Element homEl2 = (Element) homEls.item(1);
                        Haplotype hom1 = parseHaplotype(homEl1);
                        Haplotype hom2 = parseHaplotype(homEl2);
                        // create diploid chromosomes
                        DiploidChromosome chrom = new DiploidChromosome(hom1, hom2);
                        chroms.add(chrom);
                    } else {
                        throw new XMLFormatException();
                    }
                }
            } else {
                throw new XMLFormatException();
            }
            // create and return genotype
            Genotype g = new Genotype(chroms);
            return g;
        } else {
            throw new XMLFormatException();
        }
    }
    
    private Haplotype parseHaplotype(Element hapEl) throws XMLFormatException, GenotypeException {
        // get targets attribute
        String targetString = hapEl.getAttribute("targets");
        boolean[] targets = new boolean[targetString.length()];
        // parse targets
        if(targetString != null && targetString.length()>0){
            for(int i=0; i<targetString.length(); i++){
                char c = targetString.charAt(i);
                boolean targetPresent;
                switch(c){
                    case '0':   targetPresent = false;
                                break;
                    case '1':   targetPresent = true;
                                break;
                    default:    throw new XMLFormatException();
                }
                targets[i] = targetPresent;
            }
        } else {
            throw new XMLFormatException();
        }
        // create and return haplotype
        Haplotype hom = new Haplotype(targets);
        return hom;
    }
    
    private GeneticMap parseGeneticMap(Element geneticMapEl, boolean useKosambiMap) throws XMLFormatException {
        List<List<Double>> distancesPerChrom = new ArrayList<>();
        // get distances per chromosome
        NodeList distOnChromEls = geneticMapEl.getElementsByTagName("distances_on_chromosome");
        if(distOnChromEls != null && distOnChromEls.getLength() > 0){
            for(int i=0; i<distOnChromEls.getLength(); i++){
                Element distOnChromEl = (Element) distOnChromEls.item(i);
                // parse distances
                NodeList distEls = distOnChromEl.getElementsByTagName("dist");
                if(distEls != null && distEls.getLength() > 0){
                    List<Double> distances = new ArrayList<>();
                    for(int j=0; j<distEls.getLength(); j++){
                        Element distEl = (Element) distEls.item(j);
                        double dist = Double.parseDouble(distEl.getAttribute("cM"));
                        distances.add(dist);
                    }
                    distancesPerChrom.add(distances);
                } else {
                    // add empty dist list (pnly one target on this chromosome)
                    distancesPerChrom.add(new ArrayList<Double>());
                }
            }
        } else {
            throw new XMLFormatException();
        }
        // convert to 2D array
        double[][] distArray = new double[distancesPerChrom.size()][];
        for(int i=0; i<distancesPerChrom.size(); i++){
            List<Double> distancesOnChrom = distancesPerChrom.get(i);
            distArray[i]  = new double[distancesOnChrom.size()];
            for(int j=0; j<distancesOnChrom.size(); j++){
                distArray[i][j] = distancesOnChrom.get(j);
            }
        }
        
        if(useKosambiMap){
            // Kosambi map function
            return new GeneticMap(distArray, new KosambiMapFunction());
        } else {
            // default map function (Haldane)
            return new GeneticMap(distArray);
        }
    }
    
}
