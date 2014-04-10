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
import java.util.List;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.ugent.caagt.genestacker.Chromosome;
import org.ugent.caagt.genestacker.DiploidChromosome;
import org.ugent.caagt.genestacker.Genotype;
import org.ugent.caagt.genestacker.Haplotype;
import org.ugent.caagt.genestacker.Plant;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Abstract class with some general XML formatting tools, e.g. for genotypes,
 * chromosomes, haplotypes, etc. Also provides methods for writing an XML source
 * to a file.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public abstract class XMLWriter {

    protected Element createPlantElement(Plant plant, Document xml){
        Element plantEl = xml.createElement("plant");
        Element genotypeEl = createGenotypeElement(plant.getGenotype(), xml);
        plantEl.appendChild(genotypeEl);
        return plantEl;
    }    
    
    protected Element createGenotypeElement(Genotype genotype, Document xml){
        Element genotypeEl = xml.createElement("genotype");
        List<DiploidChromosome> chroms = genotype.getChromosomes();
        for(int c=0; c<chroms.size(); c++){
            DiploidChromosome chrom = chroms.get(c);
            Element chromEl = createChromosomeElement(chrom, xml);
            genotypeEl.appendChild(chromEl);
        }
        return genotypeEl;
    }
    
    protected Element createChromosomeElement(Chromosome chrom, Document xml){
        Element chromEl = xml.createElement("chromosome");
        Haplotype[] haps = chrom.getHaplotypes();
        for(int h=0; h<haps.length; h++){
            Haplotype hap = haps[h];
            Element hapEl = xml.createElement("haplotype");
            hapEl.setAttribute("targets", formatHaplotype(hap));
            chromEl.appendChild(hapEl);
        }
        return chromEl;
    }    
    
    protected String formatHaplotype(Haplotype hap){
        StringBuilder str = new StringBuilder();
        for(int i=0; i<hap.nrOfLoci(); i++){
            str.append(hap.targetPresent(i) ? '1' : '0');
        }
        return str.toString();
    }
     
    protected void writeXML(Document xml, File outputFile) throws TransformerException {
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer xmlWriter = factory.newTransformer();
        xmlWriter.setOutputProperty(OutputKeys.INDENT, "yes");
        xmlWriter.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        DOMSource source = new DOMSource(xml);
        StreamResult output = new StreamResult(outputFile);
        xmlWriter.transform(source, output);
    }
}
