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

package org.ugent.caagt.genestacker.cli;

import java.util.Scanner;
import org.ugent.caagt.genestacker.exceptions.GenestackerException;
import org.ugent.caagt.genestacker.search.AbstractSearchListener;
import org.ugent.caagt.genestacker.search.ParetoFrontier;
import org.ugent.caagt.genestacker.search.SearchEngine;
import org.ugent.caagt.genestacker.search.SearchMessageType;
import org.ugent.caagt.genestacker.util.TimeFormatting;

/**
 *
 * @author Herman De Beukelaer <herman.debeukelaer@ugent.be>
 */
public class SearchRunner extends AbstractSearchListener {
    
    // search engine
    private SearchEngine engine;
    
    // verbose
    private boolean verbose;
    
    public SearchRunner(boolean verbose){
        this.verbose = verbose;
    }
    
    /**
     * Run search engine.
     */
    public ParetoFrontier run(SearchEngine engine, long runTimeLimit, int numThreads) throws GenestackerException{
        
        this.engine = engine;
        // register as search listener
        engine.addSearchListener(this);
        // run search
        ParetoFrontier pf = engine.search(runTimeLimit, numThreads);
        // stop listening when search is complete
        engine.removeSearchListener(this);
        // return solutions
        return pf;
    }
    
    @Override
    public void searchMessage(String message, SearchMessageType type){
        // append time in case of progress/verbose message
        if(type == SearchMessageType.PROGRESS || type == SearchMessageType.VERBOSE){
            long time = (System.currentTimeMillis() - engine.getStart());
            message += " -- T = " + TimeFormatting.formatTime(time);
        }
        // output message (check verbose option for VERBOSE messages)
        if(type != SearchMessageType.VERBOSE || verbose){
            System.out.println("[" + type + "] " + message);        
        }
        // in case of a debug message: wait for user to press enter
        if(type == SearchMessageType.DEBUG){
            Scanner keyIn = new Scanner(System.in);
            System.out.print("[Press enter to continue]");
            keyIn.nextLine();
        }
    }
    
    @Override
    public void searchStarted(){
        String s = "# Search started #";
        for(int i=0; i<s.length(); i++){
            System.out.print("#");
        }
        System.out.println("");
        System.out.println(s);
        for(int i=0; i<s.length(); i++){
            System.out.print("#");
        }
        System.out.println("");
    }
    
    @Override
    public void searchStopped(){
        long time = (engine.getStop() - engine.getStart());
        String s = "# Search stopped (runtime = " + TimeFormatting.formatTime(time) + ") #";
        for(int i=0; i<s.length(); i++){
            System.out.print("#");
        }
        System.out.println("");
        System.out.println(s);
        for(int i=0; i<s.length(); i++){
            System.out.print("#");
        }
        System.out.println("");
        if(engine.debugMode()){
            // wait for enter to finish program
            Scanner keyIn = new Scanner(System.in);
            System.out.print("[Press enter to finish debugging]");
            keyIn.nextLine();
        }
    }
    
}
