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
import java.io.FileWriter;
import java.io.IOException;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ugent.caagt.genestacker.exceptions.CrossingSchemeException;
import org.ugent.caagt.genestacker.exceptions.GenestackerException;
import org.ugent.caagt.genestacker.search.CrossingNode;
import org.ugent.caagt.genestacker.search.CrossingScheme;
import org.ugent.caagt.genestacker.search.PlantNode;
import org.ugent.caagt.genestacker.search.SeedLotNode;

/**
 * Responsible of creating graphical representations for crossing schemes and writing these
 * to image files (e.g. png, jpeg, pdf, etc.) using the DOT command line tool (graphviz software)
 * 
 * @author Herman De Beukelaer <herman.debeukelaer@ugent.be>
 */
public class CrossingSchemeGraphWriter {
    
    // path to dot command line tool
    private final String DOT;
    
    // double formatter
    private DecimalFormat df;
    
    // output file format
    private GraphFileFormat fileFormat;
    
    /**
     * Create a new CrossingSchemeGraphWriter with the desired output file format.
     * 
     * @param fileFormat 
     */
    public CrossingSchemeGraphWriter(GraphFileFormat fileFormat) throws GenestackerException{
        this.fileFormat = fileFormat;
        df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.UP);
        String dot = GenestackerResourceBundle.getConfig("dot.path");
        // resolve home dir if present
        if(dot.startsWith("~" + File.separator)){
            dot = System.getProperty("user.home") + dot.substring(1);
        }
        DOT = dot;
    }
    
    public void setFileFormat(GraphFileFormat fileFormat){
        this.fileFormat = fileFormat;
    }
    
    /**
     * Write a graphical representation of the crossing scheme to the given output file
     * using the external graphviz software. Returns a reference to the temporary
     * file containing the scheme's structure in the graphviz definition language.
     * This file will be automatically deleted upon exit.
     */
    public File write(CrossingScheme scheme, File outputFile) throws IOException {
        
        /*********************/
        /* CREATE DOT SOURCE */
        /*********************/

        StringBuilder dotSource = new StringBuilder("digraph G{\n");
        dotSource.append("ranksep=0.4;\n");
        // go through generations
        Map<SeedLotNode, List<PlantNode>> groupedPlants = new HashMap<>();
        int clusterCount = 0;
        int seedLotCount = 1;
        for(int gen=0; gen<=scheme.getNumGenerations(); gen++){
            
            // SEEDLOTS
            List<SeedLotNode> seedlots = scheme.getSeedLotNodesFromGeneration(gen);
            // output seedlots
            for(SeedLotNode seedlot : seedlots){
                // create seedlot node
                dotSource.append(seedlot.getUniqueID()).append(" [shape=circle, width=0.4, fixedsize=true, fontsize=12.0, label=\"S").append(seedLotCount).append("\"];\n");
                // connect with parent crossings
                for(CrossingNode c : seedlot.getParentCrossings()){
                    dotSource.append(c.getUniqueID()).append(" -> ").append(seedlot.getUniqueID()).append(";\n");
                }
                // update seed lot counter
                seedLotCount++;
            }
            
            // PLANTS
            List<PlantNode> plants = scheme.getPlantNodesFromGeneration(gen);
            // group plants per parent seed lot
            groupedPlants.clear();
            for(PlantNode plant : plants){
                List<PlantNode> plantGroup = groupedPlants.get(plant.getParent());
                if(plantGroup == null){
                    plantGroup = new ArrayList<>();
                    plantGroup.add(plant);
                    groupedPlants.put(plant.getParent(), plantGroup);
                } else {
                    plantGroup.add(plant);
                }
            }
            // output plants (per group, same rank)
            for(SeedLotNode parentSln : groupedPlants.keySet()){
                List<PlantNode> plantGroup = groupedPlants.get(parentSln);
                // create cluster
                StringBuilder cluster = new StringBuilder("subgraph cluster" + clusterCount + "{\n");
                StringBuilder seedLotsToPlants = new StringBuilder();
                StringBuilder rank = new StringBuilder("{rank=same; ");
                // add plant nodes to cluster
                for(PlantNode plant : plantGroup){
                    // update rank indicator
                    rank.append(plant.getUniqueID()).append(" ");
                    // set label
                    String labelColor = "black";
                    String label = plant.getPlant().toString().replace("\n", "\\n"); // escape newlines
                    // output linkage phase ambiguity (if not zero)
                    if(plant.getLinkagePhaseAmbiguity() > 0){
                        String lpa = df.format(100*plant.getLinkagePhaseAmbiguity());
                        if(lpa.equals("100")){
                            lpa = "> " + 99.99;
                        }
                        label = label + "\\nLPA: " + lpa + "%";
                        labelColor = "red";
                    }
                    // create plant node in cluster
                    cluster.append(plant.getUniqueID()).append(" [shape=box, label=\"").append(label).append("\", fontcolor=").append(labelColor).append("];\n");   
                    // connect plant with parent seedlot
                    int genDif = plant.getGeneration() - parentSln.getGeneration();
                    int len = 3*genDif + 1; // force length of edge from seed lot to plant
                                            // to be of maximum length
                    seedLotsToPlants.append(parentSln.getUniqueID()).append(" -> ").append(plant.getUniqueID()).append(" [style=dashed, minlen=").append(len).append("];\n");
                }
                // close rank indicator
                rank.append("};");
                // add rank indicator to cluster
                cluster.append(rank).append("\n");
                // set rounded border style
                cluster.append("style = rounded;\n");
                // hide border in case of only one plant
                if(plantGroup.size() == 1){
                    cluster.append("color = invis;\n");
                }
                // set label (showing num seeds)
                cluster.append("label = \"").append(parentSln.getSeedsTakenFromSeedLotInGeneration(gen)).append("\";\n");
                // right align label
                cluster.append("labeljust = \"r\";\n");
                // close cluster
                cluster.append("}\n");
                // add cluster to main graph
                dotSource.append(cluster);
                // increase cluster count
                clusterCount++;
                // OUTSIDE cluster: connect seed lots with plants (to prevent seed lots ending up inside clusters)
                dotSource.append(seedLotsToPlants);
            }
            
            // CROSSINGS
            List<CrossingNode> crossings = scheme.getCrossingNodesFromGeneration(gen);
            // output crossings
            for(CrossingNode c : crossings){
                // create crossing node
                dotSource.append(c.getUniqueID()).append(" [shape=diamond, label=\"\", fixedsize=true, width=0.15, height=0.15, style=filled];\n");
                // connect with parent plants
                dotSource.append(c.getParent1().getUniqueID()).append(" -> ").append(c.getUniqueID()).append(";\n"); 
                dotSource.append(c.getParent2().getUniqueID()).append(" -> ").append(c.getUniqueID()).append(";\n");
            }
            
        }
        // add graph title
        String lpa = df.format(100*scheme.getLinkagePhaseAmbiguity());
        if(lpa.equals("100")){
            lpa = "> " + 99.99;
        }
        dotSource.append("label=\"\\nOverall LPA: ").append(lpa).append("%\\n# Plants: ").append(scheme.getTotalPopulationSize()).append("\"\n");
        dotSource.append("labelloc=b\n");
        // set transparent background
        dotSource.append("bgcolor=transparent\n");
        // finish DOT source string
        dotSource.append("}");

        /**********************************/
        /* OUTPUT DOT SOURCE TO TEMP FILE */
        /**********************************/
        
        File dotSourceFile = Files.createTempFile("graph_", ".graphviz").toFile();
        dotSourceFile.deleteOnExit();
        try(FileWriter fout = new FileWriter(dotSourceFile)){
            fout.write(dotSource.toString());
        }
        
        /*****************************/
        /* RUN DOT TO CREATE DIAGRAM */
        /*****************************/
        
        String[] args = {DOT, "-T"+fileFormat, dotSourceFile.getAbsolutePath(), "-o", outputFile.getAbsolutePath()};
        Runtime rt = Runtime.getRuntime();
        try {
            // run dot program
            Process p = rt.exec(args);
            // wait for completion
            p.waitFor();
        } catch (IOException ex){
            // could not run dot program, issue warning
            System.err.println("[WARNING] Could not run external graphviz software -- skipping graph creation (check config file: ~/genestacker/config.properties)");
            // delete file
            outputFile.delete();
        } catch (InterruptedException shouldNotHappen) {
            throw new RuntimeException("[SHOULD NOT HAPPEN] Main thread was interrupted while waiting for DOT to complete writing a diagram file", shouldNotHappen);
        }
        
        // return reference to temporary dot source file
        return dotSourceFile;
    }

}
