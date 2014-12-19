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

package org.ugent.caagt.genestacker.search;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ugent.caagt.genestacker.GeneticMap;
import org.ugent.caagt.genestacker.Genotype;
import org.ugent.caagt.genestacker.Plant;
import org.ugent.caagt.genestacker.exceptions.GenestackerException;
import org.ugent.caagt.genestacker.io.GenestackerInput;
import org.ugent.caagt.genestacker.io.GraphColorScheme;
import org.ugent.caagt.genestacker.io.GraphFileFormat;
import org.ugent.caagt.genestacker.log.SearchStartedMessage;
import org.ugent.caagt.genestacker.log.SearchStoppedMessage;
import org.ugent.caagt.genestacker.util.DebugUtils;
import org.ugent.caagt.genestacker.util.GenestackerConstants;

/**
 * Common interface for search engines.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public abstract class SearchEngine{
    
    // logger
    Logger logger = LogManager.getLogger(SearchEngine.class);
    
    // input
    protected Collection<Plant> initialPlants;
    protected Genotype ideotype;
    protected GeneticMap map;
    
    // search listeners
    protected final List<SearchListener> searchListeners;

    // graph file format for debug and intermediate output
    protected GraphFileFormat graphFileFormat;
    // graph color scheme for debug and intermediate output
    protected GraphColorScheme graphColorScheme;
    
    // start, stop time
    protected long startTime, stopTime;
    
    // runtime limit
    protected long runtimeLimit;
    
    public SearchEngine(GenestackerInput input){
        this(input.getInitialPlants(), input.getIdeotype(), input.getGeneticMap());
    }
    
    public SearchEngine(List<Plant> initialPlants, Genotype ideotype, GeneticMap map){
        this(initialPlants, ideotype, map, GraphFileFormat.PDF, GraphColorScheme.COLORED);
    }
    
    public SearchEngine(List<Plant> initialPlants, Genotype ideotype, GeneticMap map,
                        GraphFileFormat graphFileFormat, GraphColorScheme graphColorScheme){
        this.initialPlants = initialPlants;
        this.ideotype = ideotype;
        this.map = map;
        this.graphFileFormat = graphFileFormat;
        this.graphColorScheme = graphColorScheme;
        searchListeners = new LinkedList<>();
    }
    
    /**
     * Run the search engine with a specific runtime limit and number of threads.
     * 
     * @param runtimeLimit time limit in milliseconds
     * @param numThreads (maximum) number of thread to be created by this search 
     * @return computed (or approximated) Pareto frontier
     * @throws GenestackerException if anything goes wrong during search
     */
    public ParetoFrontier search(long runtimeLimit, int numThreads) throws GenestackerException{
        this.runtimeLimit = runtimeLimit;
        startTime = System.currentTimeMillis();
        logger.info(new SearchStartedMessage());
        fireSearchStarted();
        ParetoFrontier f = runSearch(runtimeLimit, numThreads);
        
        // if debugging, wait for enter before finishing
        if(logger.isDebugEnabled()){
            DebugUtils.waitForEnter("[Press enter to finish search run]");
        }
        
        stopTime = System.currentTimeMillis();
        fireSearchStopped();
        logger.info(new SearchStoppedMessage(stopTime-startTime));
        
        return f;
    }
    
    /**
     * Override this method in each search engine to define its behaviour.
     * 
     * @param runtimeLimit time limit in milliseconds
     * @param numThreads (maximum) number of thread to be created by this search 
     * @return computed (or approximated) Pareto frontier
     * @throws GenestackerException if anything goes wrong during search
     */
    protected abstract ParetoFrontier runSearch(long runtimeLimit, int numThreads) throws GenestackerException;
    
    protected boolean runtimeLimitExceeded(){
        if(runtimeLimit != GenestackerConstants.NO_RUNTIME_LIMIT){
            return System.currentTimeMillis()-startTime > runtimeLimit;
        } else {
            return false; // no limit set
        }
    }
    
    /**
     * Get the start time in milliseconds.
     * 
     * @return start time (milliseconds)
     */
    public long getStart(){
        return startTime;
    }
    
    /**
     * Get the stop time in milliseconds.
     * 
     * @return stop time (milliseconds)
     */
    public long getStop(){
        return stopTime;
    }
    
    public void addSearchListener(SearchListener l){
        synchronized(searchListeners){
            searchListeners.add(l);
        }
    }
    
    public void removeSearchListener(SearchListener l){
        synchronized(searchListeners){
            searchListeners.remove(l);
        }
    }
    
    protected void fireSearchMessage(String message){
        synchronized(searchListeners){
            Iterator<SearchListener> it = searchListeners.iterator();
            while(it.hasNext()){
                it.next().searchMessage(message);
            }
        }
    }
    
    protected void fireSearchStarted(){
        synchronized(searchListeners){
            Iterator<SearchListener> it = searchListeners.iterator();
            while(it.hasNext()){
                it.next().searchStarted();
            }
        }
    }
    
    protected void fireSearchStopped(){
        synchronized(searchListeners){
            Iterator<SearchListener> it = searchListeners.iterator();
            while(it.hasNext()){
                it.next().searchStopped();
            }
        }
    }
    
}
