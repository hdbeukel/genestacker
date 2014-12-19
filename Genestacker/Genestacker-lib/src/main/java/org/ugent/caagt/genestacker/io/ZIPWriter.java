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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Map;
import java.util.Set;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.utils.IOUtils;
import org.ugent.caagt.genestacker.exceptions.GenestackerException;
import org.ugent.caagt.genestacker.search.CrossingScheme;
import org.ugent.caagt.genestacker.search.ParetoFrontier;

/**
 * Creates ZIP packages containing output files generated for schedules contained in a given Pareto frontier.
 *
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class ZIPWriter {

    /**
     * Creates a ZIP package containing 3 files for every schedule in the given Pareto frontier:
     * <ul>
     *  <li>a graph visualisation using the requested graph file format</li>
     *  <li>a dot source file used to generate the visualisation</li>
     *  <li>an XML file describing the schedule</li>
     * </ul>
     *
     * @param pf Pareto frontier
     * @param format graph file format (e.g. PDF)
     * @param colorScheme graph color scheme
     * @param outputFile output file (extension ".zip" is appended if not already contained in the file name)
     * 
     * @throws IOException if any IO errors occur
     * @throws ArchiveException if the ZIP file can not be created
     * @throws GenestackerException if any problems occur with the Gene Stacker config file
     */
    public void createZIP(ParetoFrontier pf, GraphFileFormat format, GraphColorScheme colorScheme, String outputFile)
            throws IOException, ArchiveException, GenestackerException {
        // format outputFile string
        if (!outputFile.endsWith(".zip")) {
            outputFile += ".zip";
        }
        // name of folder inside ZIP (same as ZIP without extension)
        String inZIPFolder = outputFile.substring(0, outputFile.lastIndexOf('.'));
        if (inZIPFolder.indexOf('/') != -1) {
            inZIPFolder = inZIPFolder.substring(outputFile.lastIndexOf('/') + 1);
        }
        // create ZIP archive
        CrossingSchemeGraphWriter graphWriter = new CrossingSchemeGraphWriter(format, colorScheme);
        CrossingSchemeXMLWriter xmlWriter = new CrossingSchemeXMLWriter();
        try (OutputStream out = new FileOutputStream(new File(outputFile));
                ArchiveOutputStream os = new ArchiveStreamFactory().createArchiveOutputStream("zip", out);) {
            // add files
            int numScheme = 0;
            for (Map.Entry<Integer, Set<CrossingScheme>> gen : pf.getSchemes().entrySet()) {
                Set<CrossingScheme> schemes = gen.getValue();
                for (CrossingScheme s : schemes) {
                    numScheme++;
                    // create xml
                    File xml = Files.createTempFile("scheme-", ".xml").toFile();
                    xmlWriter.write(s, xml);
                    // create graph
                    File graph = Files.createTempFile("scheme-", "." + format).toFile();
                    File graphvizSource = graphWriter.write(s, graph);
                    // copy xml, diagram and graphviz source to zip file
                    os.putArchiveEntry(new ZipArchiveEntry(inZIPFolder + "/scheme" + numScheme + ".xml"));
                    IOUtils.copy(new FileInputStream(xml), os);
                    os.closeArchiveEntry();
                    // only copy graph if successfully created!
                    if (graph.exists()) {
                        os.putArchiveEntry(new ZipArchiveEntry(inZIPFolder + "/scheme" + numScheme + "." + format));
                        IOUtils.copy(new FileInputStream(graph), os);
                        os.closeArchiveEntry();
                    }
                    os.putArchiveEntry(new ZipArchiveEntry(inZIPFolder + "/scheme" + numScheme + ".graphviz"));
                    IOUtils.copy(new FileInputStream(graphvizSource), os);
                    os.closeArchiveEntry();
                    // delete temp files
                    xml.delete();
                    graph.delete();
                }
            }
        }
    }
}
