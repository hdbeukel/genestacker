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

package org.ugent.caagt.genestacker.search.bb;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.ugent.caagt.genestacker.GeneticMap;
import org.ugent.caagt.genestacker.Genotype;
import org.ugent.caagt.genestacker.Plant;
import org.ugent.caagt.genestacker.SeedLot;
import org.ugent.caagt.genestacker.exceptions.CrossingSchemeException;
import org.ugent.caagt.genestacker.exceptions.GenestackerException;
import org.ugent.caagt.genestacker.exceptions.GenotypeException;
import org.ugent.caagt.genestacker.exceptions.SearchException;
import org.ugent.caagt.genestacker.io.CrossingSchemeGraphWriter;
import org.ugent.caagt.genestacker.io.CrossingSchemeXMLWriter;
import org.ugent.caagt.genestacker.io.GenestackerInput;
import org.ugent.caagt.genestacker.io.GraphFileFormat;
import org.ugent.caagt.genestacker.search.CrossingNode;
import org.ugent.caagt.genestacker.search.CrossingScheme;
import org.ugent.caagt.genestacker.search.CrossingSchemeAlternatives;
import org.ugent.caagt.genestacker.search.CrossingSchemeDescriptor;
import org.ugent.caagt.genestacker.search.DefaultDominatesRelation;
import org.ugent.caagt.genestacker.search.DominatesRelation;
import org.ugent.caagt.genestacker.search.ParetoFrontier;
import org.ugent.caagt.genestacker.search.PlantNode;
import org.ugent.caagt.genestacker.search.PopulationSizeTools;
import org.ugent.caagt.genestacker.search.SearchEngine;
import org.ugent.caagt.genestacker.search.SearchMessageType;
import org.ugent.caagt.genestacker.search.SeedLotCache;
import org.ugent.caagt.genestacker.search.SeedLotNode;
import org.ugent.caagt.genestacker.search.SelfingNode;
import org.ugent.caagt.genestacker.search.bb.heuristics.Heuristics;
import org.ugent.caagt.genestacker.search.bb.heuristics.PlantCollectionFilter;
import org.ugent.caagt.genestacker.search.bb.heuristics.SeedLotFilter;
import org.ugent.caagt.genestacker.search.constraints.Constraint;
import org.ugent.caagt.genestacker.search.constraints.NumberOfSeedsPerCrossing;

/**
 * Abstract branch and bound search engine.
 * 
 * @author Herman De Beukelaer <herman.debeukelaer@ugent.be>
 */
public class BranchAndBound extends SearchEngine {
    
    // previously considered schemes
    private List<CrossingSchemeAlternatives> previousSchemes;
    // previously considered scheme alternatives
    private Set<CrossingScheme> previousSchemeAlternatives;
    // queue schemes to be considered later
    private Queue<CrossingSchemeAlternatives> schemeQueue;
    
    // dominates relation used by the Pareto frontier
    private DominatesRelation<CrossingSchemeDescriptor> dominatesRelation;
    
    // seed lot cache
    private SeedLotCache seedLotCache;
    
    // seed lot constructor
    private SeedLotConstructor seedLotConstructor;
    
    // population size tools
    private PopulationSizeTools popSizeTools;
    
    // constraints
    private List<Constraint> constraints;
    
    // special constraint: maximum number of seeds per crossing (can be resolved)
    private NumberOfSeedsPerCrossing maxNumSeedsPerCrossing;
    
    // heuristics
    private Heuristics heuristics;
    
    // seed lot filters
    private List<SeedLotFilter> seedLotFilters;
    
    // initial Pareto frontier already known before starting the search
    private ParetoFrontier initialFrontier;
    
    // initial plant filter
    private PlantCollectionFilter initialPlantFilter;
    
    // homozygous ideotype parents required ?
    private boolean homozygousIdeotypeParents;
    
    public BranchAndBound(GenestackerInput input, PopulationSizeTools popSizeTools, List<Constraint> constraints, NumberOfSeedsPerCrossing maxNumSeedsPerCrossing,
                            Heuristics heuristics, List<SeedLotFilter> seedLotFilters, PlantCollectionFilter initialPlantFilter, SeedLotConstructor seedLotConstructor){
        super(input);
        init(popSizeTools, constraints, maxNumSeedsPerCrossing, heuristics, seedLotFilters, initialPlantFilter,
                null, seedLotConstructor, new DefaultDominatesRelation(), false);
    }
    
    public BranchAndBound(GenestackerInput input, boolean debug, GraphFileFormat graphFileFormat,
                                                PopulationSizeTools popSizeTools,  List<Constraint> constraints,
                                                NumberOfSeedsPerCrossing maxNumSeedsPerCrossing,
                                                Heuristics heuristics, List<SeedLotFilter> seedLotFilters,
                                                PlantCollectionFilter initialPlantFilter, SeedLotConstructor seedLotConstructor,
                                                DominatesRelation<CrossingSchemeDescriptor> dominatesRelation,
                                                boolean homozygousIdeotypeParents){
        super(input.getInitialPlants(), input.getIdeotype(), input.getGeneticMap(), debug, graphFileFormat);
        init(popSizeTools, constraints, maxNumSeedsPerCrossing, heuristics, seedLotFilters,
                initialPlantFilter, null, seedLotConstructor, dominatesRelation, homozygousIdeotypeParents);
    }
    
    public BranchAndBound(GenestackerInput input, boolean debug, GraphFileFormat graphFileFormat, List<Constraint> constraints, PopulationSizeTools popSizeTools,
                                NumberOfSeedsPerCrossing maxNumSeedsPerCrossing, Heuristics heuristics, List<SeedLotFilter> seedLotFilters,
                                PlantCollectionFilter initialPlantFilter, ParetoFrontier initialFrontier, SeedLotConstructor seedLotConstructor,
                                DominatesRelation<CrossingSchemeDescriptor> dominatesRelation, boolean homozygousIdeotypeParents){
        super(input.getInitialPlants(), input.getIdeotype(), input.getGeneticMap(), debug, graphFileFormat);
        init(popSizeTools, constraints, maxNumSeedsPerCrossing, heuristics, seedLotFilters,
                initialPlantFilter, initialFrontier, seedLotConstructor, dominatesRelation, homozygousIdeotypeParents);
    }
    
    private void init(PopulationSizeTools popSizeTools, List<Constraint> constraints, NumberOfSeedsPerCrossing maxNumSeedsPerCrossing,
                            Heuristics heuristics, List<SeedLotFilter> seedLotFilters, PlantCollectionFilter initialPlantFilter,
                            ParetoFrontier initialFrontier, SeedLotConstructor seedLotConstructor,
                            DominatesRelation<CrossingSchemeDescriptor> dominatesRelation, boolean homozygousIdeotypeParents){
        this.popSizeTools = popSizeTools;
        this.constraints = constraints;
        this.maxNumSeedsPerCrossing = maxNumSeedsPerCrossing;
        this.heuristics = heuristics;
        this.seedLotFilters = seedLotFilters;
        this.initialPlantFilter = initialPlantFilter;
        this.initialFrontier = initialFrontier;
        this.seedLotConstructor = seedLotConstructor;
        this.dominatesRelation = dominatesRelation;
        this.homozygousIdeotypeParents = homozygousIdeotypeParents;
        seedLotCache = new SeedLotCache();
    }
    
    public void setHeuristics(Heuristics heur){
        this.heuristics = heur;
    }
    
    public void setSeedLotFilters(List<SeedLotFilter> filters){
        this.seedLotFilters = filters;
        // IMPORTANT: upon changing the seed lot filters, the seed lot cache of this
        //            engine is cleared because it is no longer up to date
        seedLotCache.clear();
    }
    
    public void setConstraints(List<Constraint> constraints){
        this.constraints = constraints;
        // IMPORTANT: upon changing the constraints, the seed lot cache of this
        //            engine is cleared because it is no longer up to date
        //            (basic filtering is based constraints, e.g. max linkage phase ambiguity
        //                                                      & max pop size per gen)
        seedLotCache.clear();
    }
    
    public void setPopulationSizeTools(PopulationSizeTools popSizeTools){
        this.popSizeTools = popSizeTools;
        // IMPORTANT: upon changing the population size tools,
        //            the seed lot cache of this search engine is
        //            cleared because it is no longer up to date
        //            (basic filtering of seed lots is based on the
        //            population size tools)
        seedLotCache.clear();
    }
    
    public void setInitialFrontier(ParetoFrontier frontier){
        this.initialFrontier = frontier;
    }
    
    @Override
    public ParetoFrontier runSearch(long runtimeLimit, int numThreads) throws GenestackerException {

        // create list to store previously generated schemes
        previousSchemes = new ArrayList<>();
        // create set to store previously generated scheme alternatives
        previousSchemeAlternatives = new HashSet<>();
        // create queue for schemes to be considered
        schemeQueue = new LinkedList<>();
        
        // reset ids
        SeedLotNode.resetIDs();
        PlantNode.resetIDs();
        CrossingNode.resetIDs();
        CrossingSchemeAlternatives.resetIDs();
        
        // create thread pool for scheme crossing
        
        // inform user about number of cross workers used
        fireSearchMessage("# Cross workers: " + numThreads, SearchMessageType.INFO);
        ExecutorService crossPool = Executors.newFixedThreadPool(numThreads);
        // cross worker future set
        Set<Future<List<CrossingSchemeAlternatives>>> futures = new HashSet<>();
        
        // initialize solution manager
        BranchAndBoundSolutionManager solutionManager = new BranchAndBoundSolutionManager(dominatesRelation, ideotype, popSizeTools,
                                                                maxNumSeedsPerCrossing, constraints, heuristics, seedLotFilters, homozygousIdeotypeParents);
        // set initial Pareto frontier, if any
        if(initialFrontier != null){
            solutionManager.setFrontier(initialFrontier);
        }
        
        // apply initial plant filter, if any
        if(initialPlantFilter != null){

            fireSearchMessage("Filtering initial plants ...", SearchMessageType.INFO);
            
            initialPlants = initialPlantFilter.filter(initialPlants);
            
            fireSearchMessage("Retained initial plants:", SearchMessageType.INFO);
            for(Plant p : initialPlants){
                fireSearchMessage("\n" + p.toString(), SearchMessageType.INFO);
            }
            
        }
        
        // create initial partial schemes from initial plants
        List<CrossingSchemeAlternatives> initialParentSchemes = new ArrayList<>();
        for(Plant p : initialPlants){
            // create uniform seed lot
            SeedLot sl = new SeedLot(p.getGenotype());
            // create seedlot node
            SeedLotNode sln = new SeedLotNode(sl, 0);
            // create and attach plant node
            PlantNode pn = new PlantNode(p, 0, sln);
            // create partial crossing scheme
            CrossingScheme s = new CrossingScheme(popSizeTools, pn);
            initialParentSchemes.add(new CrossingSchemeAlternatives(s));
        }
        registerNewSchemes(initialParentSchemes, solutionManager);
        
        // now iteratively cross schemes with previous schemes to create larger schemes,
        // until all solutions have been inspected or bounded
        while(!runtimeLimitExceeded() && !schemeQueue.isEmpty()){
            
            // get next scheme from queue
            CrossingSchemeAlternatives cur = schemeQueue.poll();
            
            // fire progression message
            fireSearchMessage("cur frontier: " + solutionManager.getFrontier().getNumSchemes()
                                + " ### prog: " + previousSchemes.size()
                                + " (" + schemeQueue.size() + ")"
                                + " ### cur scheme: " + cur,
                                    SearchMessageType.PROGRESS);
            if(DEBUG){
                for(int i=0; i<cur.nrOfAlternatives(); i++){
                    fireSearchMessage("Cur scheme alt " + i + ": " + writeDiagram(cur.getAlternatives().get(i)), SearchMessageType.DEBUG);
                }
            }
            
            // delete possible bounded alternatives
            Iterator<CrossingScheme> it = cur.iterator();
            int numForCrossing = 0;
            int numForSelfing = 0;
            while(it.hasNext()){
                CrossingScheme alt = it.next();
                // check if alternative should be removed
                if(previousSchemeAlternatives.contains(alt)){
                    // equivalent scheme alternative generated before, delete current alternative
                    it.remove();
                } else if (solutionManager.boundDequeueScheme(alt)){
                    // dequeing of scheme bounded (e.g. by the optimal subscheme heuristic)
                    it.remove();
                } else {
                    // check bounding for crossing/selfing
                    boolean boundCross = solutionManager.boundCrossCurrentScheme(alt);
                    boolean boundSelf = solutionManager.boundSelfCurrentScheme(alt);
                    if(boundCross && boundSelf){
                        // alternative not useful anymore
                        it.remove();
                    } else {
                        // count nr of alternatives useful for crossing or selfing
                        if(!boundCross){
                            numForCrossing++;
                        }
                        if(!boundSelf){
                            numForSelfing++;
                        }
                    }
                }
            }
            
            if(cur.nrOfAlternatives() > 0){
                
                // if useful, self current scheme
                if(numForSelfing > 0){
                    registerNewSchemes(selfScheme(cur, map, solutionManager), solutionManager);
                }

                // if useful, cross with previous schemes
                if(numForCrossing > 0){
                    // launch workers to cross with previous schemes
                    futures.clear();
                    // submit tasks
                    Iterator<CrossingSchemeAlternatives> previousSchemesIterator = previousSchemes.iterator();
                    for(int w=0; w<numThreads; w++){
                        // submit worker
                        futures.add(crossPool.submit(new CrossWorker(previousSchemesIterator, cur, solutionManager, map)));
                        fireSearchMessage("Launched cross worker " + (w+1) + " of " + numThreads, SearchMessageType.VERBOSE);
                    }
                    // wait for completion of workers
                    int w = 1;
                    for(Future<List<CrossingSchemeAlternatives>> fut : futures){
                        try {
                            // wait for next worker to complete and register its solutions
                            registerNewSchemes(fut.get(), solutionManager);
                            fireSearchMessage("Cross worker " + w + " of " + numThreads + " finished!", SearchMessageType.VERBOSE);
                            w++;
                        } catch (InterruptedException | ExecutionException ex) {
                            // something went wrong with the cross workers
                            throw new SearchException("An error occured while crossing the current scheme with all previous schemes. Reason: " + ex);
                        }
                    }
                }
                
                // put the scheme in the sorted set with previously considered schemes (only done if useful for later crossings)
                previousSchemes.add(cur);
                // register scheme alternatives
                previousSchemeAlternatives.addAll(cur.getAlternatives());
            }
        }
        
        if(runtimeLimitExceeded()){
            fireSearchMessage("Runtime limit exceeded", SearchMessageType.INFO);
        }
        
        // shutdown thread pool
        crossPool.shutdownNow();
        
        return solutionManager.getFrontier();
    }
    
    /**
     * Register new schemes.
     * 
     * @param alt
     * @param solManager
     * @throws GenestackerException 
     */
    protected void registerNewSchemes(List<CrossingSchemeAlternatives> newSchemes, BranchAndBoundSolutionManager solManager)
                                                                                    throws GenestackerException {
        
        if(!newSchemes.isEmpty()){
            for(CrossingSchemeAlternatives scheme : newSchemes){
                // go through alternatives to:
                //  1) register possible solutions
                //  2) remove bounded alternatives
                Iterator<CrossingScheme> it = scheme.iterator();
                while(it.hasNext()){
                    CrossingScheme alt = it.next();
                    // check if alternative is a valid solution
                    if(solManager.isSolution(alt)){
                        // register new solution
                        boolean frontierUpdated = solManager.registerSolution(alt);
                        if(frontierUpdated){
                            fireSearchMessage("Pareto frontier updated", SearchMessageType.PROGRESS);
                        }
                        if(DEBUG){
                            fireSearchMessage("New solution: " + writeDiagram(alt), SearchMessageType.DEBUG);
                        }
                    }
                    // check if further extension of alternative should be bounded
                    if(solManager.boundCrossCurrentScheme(alt) && solManager.boundSelfCurrentScheme(alt)){
                        // alternative not useful for further crossing or selfing, so delete it
                        it.remove();
                    } else if(solManager.boundQueueScheme(alt)){
                        // do not queue this scheme alternative (e.g. because some heuristic prevents it)
                        it.remove();
                    }
                }
                // if non-zero number of alternatives remain, add new scheme to the queue
                if(scheme.nrOfAlternatives() > 0){
                    schemeQueue.add(scheme);
                }
            }
        }

    }
    
    protected String writeDiagram(CrossingScheme scheme) throws GenestackerException{
        CrossingSchemeGraphWriter epsWriter = new CrossingSchemeGraphWriter(graphFileFormat);
        CrossingSchemeXMLWriter xmlWriter = new CrossingSchemeXMLWriter();
        try {
            File graph = File.createTempFile("graph_", "." + graphFileFormat);
            graph.deleteOnExit();
            epsWriter.write(scheme, graph);
            String xmlPath = graph.getAbsolutePath();
            xmlPath = xmlPath.substring(0, xmlPath.length()-4);
            xmlPath += ".xml";
            File xml = Files.createFile(Paths.get(xmlPath)).toFile();
            xml.deleteOnExit();
            xmlWriter.write(scheme, xml);
            return graph.getAbsolutePath();
        } catch (IOException ex) {
            throw new SearchException("Failed to create scheme diagram while debugging branch and bound search engine");
        }
    }
    
    protected SeedLot constructSeedLot(int generation, Plant p1, Plant p2, GeneticMap map, BranchAndBoundSolutionManager solManager) throws GenotypeException{
        SeedLot sl;
        if(solManager.finalGenerationReached(generation)){
            // create partial seed lot containing the ideotype only
            Set<Genotype> s = new HashSet<>();
            s.add(solManager.getIdeotype());
            sl = seedLotConstructor.partialCross(p1.getGenotype(), p2.getGenotype(), s);
            fireSearchMessage("|-- Generated new partial seed lot: " + sl.nrOfGenotypes(), SearchMessageType.VERBOSE);
            // note: do not cache this partial seed lot (not general)
        }  else {
            // lookup full seed lot in cache
            sl = seedLotCache.getCachedSeedLot(p1.getGenotype(), p2.getGenotype());
            if(sl == null){
                // not yet present in cache, create full seed lot
                sl = seedLotConstructor.cross(p1.getGenotype(), p2.getGenotype());
                int unfiltered = sl.nrOfGenotypes();
                fireSearchMessage("|-- Generated new seed lot: " + unfiltered, SearchMessageType.VERBOSE);
                // apply seed lot filters
                sl = solManager.filterSeedLot(sl);
                fireSearchMessage("|-- Filtered seed lot: " + unfiltered + " --> " + sl.nrOfGenotypes(), SearchMessageType.VERBOSE);
                // store in cache
                seedLotCache.cache(p1.getGenotype(), p2.getGenotype(), sl);
            } else {
                // found seed lot in cache
                fireSearchMessage("|-- Cached seed lot size: " + sl.nrOfGenotypes(), SearchMessageType.VERBOSE);
            }
        }
        return sl;
    }
    
    /**
     * Create all possible crossing schemes obtained by selfing the final plant of the given scheme.
     * 
     * @param scheme
     * @param map
     * @param solManager
     * @throws GenotypeException
     * @throws CrossingSchemeException  
     */
    public List<CrossingSchemeAlternatives> selfScheme(CrossingSchemeAlternatives scheme, GeneticMap map, BranchAndBoundSolutionManager solManager)
                                                                        throws GenotypeException,
                                                                               CrossingSchemeException {
        
        List<CrossingSchemeAlternatives> newSchemes = new ArrayList<>();
        SeedLot sl = constructSeedLot(scheme.getMinNumGen()+1, scheme.getFinalPlant(), scheme.getFinalPlant(), map, solManager);

        // create new schemes for each possible genotype resulting from seedlot,
        // attached to each alternative of the given scheme
        long[] seedLotNodeIDs = new long[scheme.nrOfAlternatives()];
        for(int i=0; i<seedLotNodeIDs.length; i++){
            seedLotNodeIDs[i] = SeedLotNode.genNextID();
        }
        for(Genotype g : sl.getGenotypes()){
            Plant p = new Plant(g);
            PlantDescriptor pdesc = new PlantDescriptor(
                        p,
                        sl.getGenotypeGroup(g.getObservableState()).getProbabilityOfPhaseKnownGenotype(g),
                        sl.getGenotypeGroup(g.getObservableState()).getLinkagePhaseAmbiguity(g),
                        sl.isUniform()
                    );
            if(!solManager.boundGrowPlantFromAncestors(scheme.getAncestorDescriptors(), pdesc)){
                // list containing alternatives of new scheme
                List<CrossingScheme> newAlts = new ArrayList<>();
                // go through alternatives
                for(int i=0; i<scheme.nrOfAlternatives(); i++){
                    CrossingScheme alt = scheme.getAlternatives().get(i);
                    // check bounding
                    if(!solManager.boundSelfCurrentSchemeWithSelectedTarget(alt, pdesc)){                    
                            // deep copy current node structure
                            PlantNode selfed = alt.getFinalPlantNode().deepUpwardsCopy();
                            // create new selfing node and connect with parent plant
                            SelfingNode selfing = new SelfingNode(selfed);
                            // create new seedlot node and connect it with the selfing node
                            List<CrossingNode> newCrossings = new ArrayList<>();
                            newCrossings.add(selfing);
                            SeedLotNode sln = new SeedLotNode(sl, alt.getNumGenerations()+1, newCrossings, seedLotNodeIDs[i], 0);
                            // create new plant, connect it with parent seedlot
                            PlantNode newFinalPlantNode = new PlantNode(p, alt.getNumGenerations()+1, sln);
                            // create new crossing scheme and resolve possible depleted seed lots
                            CrossingScheme newScheme = new CrossingScheme(alt.getPopulationSizeTools(), newFinalPlantNode);
                            if(!solManager.boundCurrentScheme(newScheme)
                                && newScheme.resolveDepletedSeedLots(solManager)){
                                // depleted seed lots successfully resolved, satisfying constraints
                                newAlts.add(newScheme);
                            }
                    }
                }
                // store alternatives obtaining current genotype
                if(!newAlts.isEmpty()){
                    newSchemes.add(new CrossingSchemeAlternatives(newAlts));
                }
            }
        }
        return newSchemes;
    }
    
    public List<CrossingSchemeAlternatives> combineSchemes(CrossingSchemeAlternatives scheme1, CrossingSchemeAlternatives scheme2,
                                                           GeneticMap map, BranchAndBoundSolutionManager solManager)
                                                                                throws  SearchException,
                                                                                        GenotypeException,
                                                                                        CrossingSchemeException{
        
        /************************/
        /* GENERAL PREPARATIONS */
        /************************/
        
        // get cached seed lot
        SeedLot sl = constructSeedLot(Math.max(scheme1.getMinNumGen(), scheme2.getMinNumGen())+1,
                                        scheme1.getFinalPlant(), scheme2.getFinalPlant(), map, solManager);

        // BOUNDING
        
        // compute bounded genotypes
        Set<PlantDescriptor> ancestors = new HashSet<>();
        ancestors.addAll(scheme1.getAncestorDescriptors());
        ancestors.addAll(scheme2.getAncestorDescriptors());
        Set<Genotype> boundedGenotypes = new HashSet<>();
        Iterator<Genotype> it = sl.getGenotypes().iterator();
        while(it.hasNext()){
            Genotype g = it.next();
            if(solManager.boundGrowPlantFromAncestors(ancestors, new PlantDescriptor(
                        new Plant(g),
                        sl.getGenotypeGroup(g.getObservableState()).getProbabilityOfPhaseKnownGenotype(g),
                        sl.getGenotypeGroup(g.getObservableState()).getLinkagePhaseAmbiguity(g),
                        sl.isUniform()
                    ))){
                boundedGenotypes.add(g);
            }
        }
        
        // compute bounded pairs of parent schemes
        boolean[][] boundCross = new boolean[scheme1.nrOfAlternatives()][scheme2.nrOfAlternatives()];
        for(int alt1i=0; alt1i<scheme1.nrOfAlternatives(); alt1i++){
            CrossingScheme alt1 = scheme1.getAlternatives().get(alt1i);
            for(int alt2i=0; alt2i<scheme2.nrOfAlternatives(); alt2i++){
                CrossingScheme alt2 = scheme2.getAlternatives().get(alt2i);
                boundCross[alt1i][alt2i] = solManager.boundCrossCurrentSchemeWithSpecificOther(alt1, alt2);
            }
        }
        
        /**************/
        /* RUN MERGER */
        /**************/
        
        return new MergeFirstSchemeMerger(scheme1, scheme2, map, solManager, sl, ancestors, boundCross, boundedGenotypes).combineSchemes();
        
    }
    
    /**
     * Private implementation of a cross worker which is responsible of crossing the currently considered
     * scheme with a given subset of the previously considered schemes.
     */
    private final class CrossWorker implements Callable<List<CrossingSchemeAlternatives>>{
        
        // iterator of previous schemes collection
        private final Iterator<CrossingSchemeAlternatives> previousSchemesIterator;
        
        // current scheme
        private CrossingSchemeAlternatives curScheme;
        
        // solution manager
        private BranchAndBoundSolutionManager solManager;
        
        // genetic map
        private GeneticMap map;
        
        public CrossWorker(Iterator<CrossingSchemeAlternatives> previousSchemesIterator, CrossingSchemeAlternatives curScheme,
                            BranchAndBoundSolutionManager solManager, GeneticMap map){
            this.previousSchemesIterator = previousSchemesIterator;
            this.curScheme = curScheme;
            this.solManager = solManager;
            this.map = map;
        }

        @Override
        public List<CrossingSchemeAlternatives> call() throws Exception {
            // cross the current scheme with previous schemes in a synchronized
            // fashion so that each previous scheme will be considered by one
            // cross worker only
            List<CrossingSchemeAlternatives> newSchemes = new ArrayList<>();
            boolean cont = true;
            while(cont){
                // poll next previous scheme (synchronized)
                CrossingSchemeAlternatives toExtend;
                synchronized(previousSchemesIterator){
                    if(previousSchemesIterator.hasNext()){
                        toExtend = previousSchemesIterator.next();
                    } else {
                        toExtend = null;
                        cont = false;
                    }
                }
                // cross with previous scheme
                if(toExtend != null){
                    // check bounding
                    boolean bound = true;
                    Iterator<CrossingScheme> it1 = curScheme.iterator();
                    while(bound && it1.hasNext()){
                        CrossingScheme alt1 = it1.next();
                        Iterator<CrossingScheme> it2 = toExtend.iterator();
                        while(bound && it2.hasNext()){
                            CrossingScheme alt2 = it2.next();
                            bound = solManager.boundCrossCurrentSchemeWithSpecificOther(alt1, alt2);
                        }
                    }
                    // create new schemes
                    if(!bound){
                        newSchemes.addAll(combineSchemes(curScheme, toExtend, map, solManager));
                    }
                }
            }
            return newSchemes;
        }
        
    }

}
