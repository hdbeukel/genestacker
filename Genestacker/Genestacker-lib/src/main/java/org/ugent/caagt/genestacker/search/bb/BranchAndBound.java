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
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.ugent.caagt.genestacker.GeneticMap;
import org.ugent.caagt.genestacker.Genotype;
import org.ugent.caagt.genestacker.Plant;
import org.ugent.caagt.genestacker.SeedLot;
import org.ugent.caagt.genestacker.exceptions.*;
import org.ugent.caagt.genestacker.io.*;
import org.ugent.caagt.genestacker.search.*;
import org.ugent.caagt.genestacker.search.bb.heuristics.*;
import org.ugent.caagt.genestacker.search.constraints.*;
import org.ugent.caagt.genestacker.util.DebugUtils;
import org.ugent.caagt.genestacker.util.TimeFormatting;

/**
 * Branch and bound generation algorithm.
 * 
 * @author <a href="mailto:herman.debeukelaer@ugent.be">Herman De Beukelaer</a>
 */
public class BranchAndBound extends SearchEngine {
    
    // logger
    private Logger logger = LogManager.getLogger(BranchAndBound.class);
    // log markers
    private static final Marker VERBOSE = MarkerManager.getMarker("VERBOSE");
    private static final Marker VERY_VERBOSE = MarkerManager.getMarker("VERY_VERBOSE");
    static {
        VERY_VERBOSE.setParents(VERBOSE);
    }
    
    // previously considered schemes (grouped by alternatives of the same scheme)
    private List<CrossingSchemeAlternatives> previousSchemes;
    // previously considered schemes (all alternatives individually contained)
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
    
    // write intermediate output files ? (default: false)
    private boolean writeIntermediateOutput = false;
    // file name of intermediate output
    private String intermediatOutputFileName = null;
    
    public BranchAndBound(GenestackerInput input, PopulationSizeTools popSizeTools, List<Constraint> constraints, NumberOfSeedsPerCrossing maxNumSeedsPerCrossing,
                            Heuristics heuristics, List<SeedLotFilter> seedLotFilters, PlantCollectionFilter initialPlantFilter, SeedLotConstructor seedLotConstructor){
        super(input);
        init(popSizeTools, constraints, maxNumSeedsPerCrossing, heuristics, seedLotFilters, initialPlantFilter,
                null, seedLotConstructor, new DefaultDominatesRelation(), false);
    }
    
    public BranchAndBound(GenestackerInput input, GraphFileFormat graphFileFormat, GraphColorScheme colorScheme,
                                                PopulationSizeTools popSizeTools,  List<Constraint> constraints,
                                                NumberOfSeedsPerCrossing maxNumSeedsPerCrossing,
                                                Heuristics heuristics, List<SeedLotFilter> seedLotFilters,
                                                PlantCollectionFilter initialPlantFilter, SeedLotConstructor seedLotConstructor,
                                                DominatesRelation<CrossingSchemeDescriptor> dominatesRelation,
                                                boolean homozygousIdeotypeParents){
        super(input.getInitialPlants(), input.getIdeotype(), input.getGeneticMap(), graphFileFormat, colorScheme);
        init(popSizeTools, constraints, maxNumSeedsPerCrossing, heuristics, seedLotFilters,
                initialPlantFilter, null, seedLotConstructor, dominatesRelation, homozygousIdeotypeParents);
    }
    
    public BranchAndBound(GenestackerInput input, GraphFileFormat graphFileFormat, GraphColorScheme colorScheme,
                                List<Constraint> constraints, PopulationSizeTools popSizeTools,
                                NumberOfSeedsPerCrossing maxNumSeedsPerCrossing, Heuristics heuristics,
                                List<SeedLotFilter> seedLotFilters, PlantCollectionFilter initialPlantFilter,
                                ParetoFrontier initialFrontier, SeedLotConstructor seedLotConstructor,
                                DominatesRelation<CrossingSchemeDescriptor> dominatesRelation,
                                boolean homozygousIdeotypeParents){
        super(input.getInitialPlants(), input.getIdeotype(), input.getGeneticMap(), graphFileFormat, colorScheme);
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
    
    /**
     * Write intermediate output whenever the Pareto frontier is updated.
     * 
     * @param intermediateOutputFileName file name used for intermediate output file
     */
    public void enableIntermediateOutput(String intermediateOutputFileName){
        writeIntermediateOutput = true;
        this.intermediatOutputFileName = intermediateOutputFileName;
    }
    
    /**
     * Disable intermediate output, as is the default setting.
     */
    public void disableIntermediateOutput(){
        writeIntermediateOutput = false;
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
        
        // create thread pool and completion service for scheme extension
        
        // inform user about number of cross workers used (verbose)
        logger.info(VERBOSE, "Number of threads used for extending partial schemes: {}", numThreads);
        ExecutorService extPool = Executors.newFixedThreadPool(numThreads);
        CompletionService<List<CrossingSchemeAlternatives>> extCompletionService = new ExecutorCompletionService<>(extPool);
        
        // initialize solution manager
        BranchAndBoundSolutionManager solutionManager = new BranchAndBoundSolutionManager(dominatesRelation, ideotype, popSizeTools,
                                                                maxNumSeedsPerCrossing, constraints, heuristics, seedLotFilters, homozygousIdeotypeParents);
        // set initial Pareto frontier, if any
        if(initialFrontier != null){
            solutionManager.setFrontier(initialFrontier);
        }
        
        // apply initial plant filter, if any
        if(initialPlantFilter != null){

            // verbose
            logger.info(VERBOSE, "Filtering initial plants ...");
            
            initialPlants = initialPlantFilter.filter(initialPlants);
            
            //verbose
            logger.info(VERBOSE, "Retained {} initial plants (see below)", initialPlants.size());
            for(Plant p : initialPlants){
                logger.info(VERBOSE, "\n{}", p);
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
        // until all solutions have been inspected or pruned
        while(!runtimeLimitExceeded() && !schemeQueue.isEmpty()){
            
            // get next scheme from queue
            CrossingSchemeAlternatives cur = schemeQueue.poll();
            
            // fire progression message (verbose)
            logger.info(VERBOSE, "num solutions: {} ### prog: {} ({}) ### cur scheme: {} - T = {}",
                                 solutionManager.getFrontier().getNumSchemes(),
                                 previousSchemes.size(),
                                 schemeQueue.size(),
                                 cur,
                                 TimeFormatting.formatTime(System.currentTimeMillis()-getStart()));
            // debug: create diagram of current scheme (all alternatives)
            if(logger.isDebugEnabled()){
                for(int i=0; i<cur.nrOfAlternatives(); i++){
                    logger.debug("Cur scheme (alternative {}): {}", i+1, writeDiagram(cur.getAlternatives().get(i)));
                }
                // wait for enter
                DebugUtils.waitForEnter();
            }
            
            // delete possible pruned alternatives
            Iterator<CrossingScheme> it = cur.iterator();
            int numForCrossing = 0;
            int numForSelfing = 0;
            while(it.hasNext()){
                CrossingScheme alt = it.next();
                // check if alternative should be removed
                if(previousSchemeAlternatives.contains(alt)){
                    // equivalent scheme alternative generated before, delete current alternative
                    it.remove();
                } else if (solutionManager.pruneDequeueScheme(alt)){
                    // prune dequeued scheme (e.g. by the optimal subscheme heuristic)
                    it.remove();
                } else {
                    // check pruning for crossing/selfing
                    boolean pruneCross = solutionManager.pruneCrossCurrentScheme(alt);
                    boolean pruneSelf = solutionManager.pruneSelfCurrentScheme(alt);
                    if(pruneCross && pruneSelf){
                        // alternative not useful anymore
                        it.remove();
                    } else {
                        // count nr of alternatives useful for crossing or selfing
                        if(!pruneCross){
                            numForCrossing++;
                        }
                        if(!pruneSelf){
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
                    // launch workers to combine with previous schemes
                    Iterator<CrossingSchemeAlternatives> previousSchemesIterator = previousSchemes.iterator();
                    for(int w=0; w<numThreads; w++){
                        // submit worker
                        extCompletionService.submit(new CrossWorker(previousSchemesIterator, cur, solutionManager, map));
                        // very verbose
                        logger.info(VERY_VERBOSE, "Launched cross worker {} of {}", w+1, numThreads);
                    }
                    // handle results of completed workers in the order in which they complete
                    for(int w=0; w<numThreads; w++){
                        try {
                            // wait for next worker to complete and register its solutions
                            registerNewSchemes(extCompletionService.take().get(), solutionManager);
                            // very verbose
                            logger.info(VERY_VERBOSE, "{}/{} cross workers finished", w+1, numThreads);
                        } catch (InterruptedException | ExecutionException ex) {
                            // something went wrong with the cross workers
                            throw new SearchException("An error occured while extending the current scheme.", ex);
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
            // info
            logger.info("Runtime limit exceeded");
        }
        
        // shutdown thread pool
        extPool.shutdownNow();
        
        return solutionManager.getFrontier();
    }
    
    /**
     * Register new schemes in the Pareto frontier.
     * 
     * @param newSchemes newly created crossing schemes
     * @param solManager solution manager
     * @throws GenestackerException if anything goes wrong while creating (intermediate or debug) output
     */
    protected void registerNewSchemes(List<CrossingSchemeAlternatives> newSchemes, BranchAndBoundSolutionManager solManager)
                                                                                    throws GenestackerException {
        
        if(!newSchemes.isEmpty()){
            for(CrossingSchemeAlternatives scheme : newSchemes){
                // go through alternatives to:
                //  1) register possible solutions
                //  2) remove pruned alternatives
                Iterator<CrossingScheme> it = scheme.iterator();
                while(it.hasNext()){
                    CrossingScheme alt = it.next();
                    // check if alternative is a valid solution
                    if(solManager.isSolution(alt)){
                        // register new solution
                        boolean frontierUpdated = solManager.registerSolution(alt);
                        if(frontierUpdated){
                            // info
                            logger.info("Pareto frontier updated ({} solution(s)) - T = {}",
                                            solManager.getFrontier().getNumSchemes(),
                                            TimeFormatting.formatTime(System.currentTimeMillis()-getStart()));
                            // update intermediate output file, if enabled
                            if(writeIntermediateOutput){
                                try {
                                    new ZIPWriter().createZIP(solManager.getFrontier(), graphFileFormat,
                                                              graphColorScheme, intermediatOutputFileName);
                                    logger.info("Updated intermediate output file {}.", intermediatOutputFileName);
                                } catch (IOException | ArchiveException ex) {
                                    throw new SearchException("Failed to write intermediate output file." , ex);
                                }
                            }
                        }
                        // debug: create diagram of new solution
                        if(logger.isDebugEnabled()){
                            logger.debug("New solution: {}", writeDiagram(alt));
                            // wait for enter
                            DebugUtils.waitForEnter();
                        }
                    }
                    // check if further extension of alternative should be pruned
                    if(solManager.pruneCrossCurrentScheme(alt) && solManager.pruneSelfCurrentScheme(alt)){
                        // alternative not useful for further crossing or selfing, so delete it
                        it.remove();
                    } else if(solManager.pruneQueueScheme(alt)){
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
    
    protected String writeDiagram(CrossingScheme scheme) throws GenestackerException {
        CrossingSchemeGraphWriter epsWriter = new CrossingSchemeGraphWriter(graphFileFormat, graphColorScheme);
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
            throw new SearchException("Failed to create scheme diagram while debugging branch and bound search engine.");
        }
    }
    
    protected SeedLot constructSeedLot(int generation, Plant p1, Plant p2, GeneticMap map, BranchAndBoundSolutionManager solManager) throws GenotypeException{
        SeedLot sl;
        if(solManager.finalGenerationReached(generation)){
            // create partial seed lot containing the ideotype only
            Set<Genotype> s = new HashSet<>();
            s.add(solManager.getIdeotype());
            sl = seedLotConstructor.partialCross(p1.getGenotype(), p2.getGenotype(), s);
            // very verbose
            logger.info(VERY_VERBOSE, "|-- Generated new partial seed lot (ideotype only)");
            // note: do not cache this partial seed lot (not general)
        }  else {
            // lookup full seed lot in cache
            sl = seedLotCache.getCachedSeedLot(p1.getGenotype(), p2.getGenotype());
            if(sl == null){
                // not yet present in cache, create full seed lot
                sl = seedLotConstructor.cross(p1.getGenotype(), p2.getGenotype());
                int unfiltered = sl.nrOfGenotypes();
                // very verbose
                logger.info(VERY_VERBOSE, "|-- Generated new seed lot: {}", unfiltered);
                // apply seed lot filters
                sl = solManager.filterSeedLot(sl);
                // very verbose
                logger.info(VERY_VERBOSE, "|-- Filtered seed lot: {} --> {}", unfiltered, sl.nrOfGenotypes());
                // store in cache
                seedLotCache.cache(p1.getGenotype(), p2.getGenotype(), sl);
            } else {
                // found seed lot in cache
                // very verbose
                logger.info(VERY_VERBOSE, "|-- Cached seed lot size: {}", sl.nrOfGenotypes());
            }
        }
        return sl;
    }
    
    /**
     * Create all possible crossing schemes obtained by selfing the final plant of the given scheme.
     * 
     * @param scheme crossing scheme to be extended by a selfing of its final plant
     * @param map genetic map
     * @param solManager solution manager
     * @return list of crossing scheme alternatives resulting from the extension
     * @throws GenotypeException if something goes wrong when creating the seed lot
     *         obtained from the performed selfing
     * @throws CrossingSchemeException if anything goes wrong while extending the crossing
     *         schedule with new nodes
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
                        sl.getGenotypeGroup(g.getAllelicFrequencies()).getProbabilityOfPhaseKnownGenotype(g),
                        sl.getGenotypeGroup(g.getAllelicFrequencies()).getLinkagePhaseAmbiguity(g),
                        sl.isUniform()
                    );
            if(!solManager.pruneGrowPlantFromAncestors(scheme.getAncestorDescriptors(), pdesc)){
                // list containing alternatives of new scheme
                List<CrossingScheme> newAlts = new ArrayList<>();
                // go through alternatives
                for(int i=0; i<scheme.nrOfAlternatives(); i++){
                    CrossingScheme alt = scheme.getAlternatives().get(i);
                    // check pruning
                    if(!solManager.pruneSelfCurrentSchemeWithSelectedTarget(alt, pdesc)){                    
                            // deep copy current node structure
                            PlantNode selfed = alt.getFinalPlantNode().deepUpwardsCopy();
                            // create new selfing node and connect with parent plant
                            SelfingNode selfing = new SelfingNode(selfed);
                            // create new seedlot node and connect it with the selfing node
                            SeedLotNode sln = new SeedLotNode(sl, alt.getNumGenerations()+1, selfing, seedLotNodeIDs[i], 0);
                            // create new plant, connect it with parent seedlot
                            PlantNode newFinalPlantNode = new PlantNode(p, alt.getNumGenerations()+1, sln);
                            // create new crossing scheme and resolve possible depleted seed lots
                            CrossingScheme newScheme = new CrossingScheme(alt.getPopulationSizeTools(), newFinalPlantNode);
                            if(!solManager.pruneCurrentScheme(newScheme)
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
    
    /**
     * Create all possible crossing schemes obtained by crossing the final plants of the given schemes, and merging
     * these schemes.
     * 
     * @param scheme1 first crossing scheme
     * @param scheme2 second crossing scheme
     * @param map genetic map
     * @param solManager solution manager
     * @return list of crossing scheme alternatives resulting from the extension
     * @throws GenotypeException if something goes wrong when creating the seed lot
     *         obtained from the performed crossing
     * @throws CrossingSchemeException if anything goes wrong while extending the crossing
     *         schedule with new nodes
     */
    public List<CrossingSchemeAlternatives> combineSchemes(CrossingSchemeAlternatives scheme1, CrossingSchemeAlternatives scheme2,
                                                           GeneticMap map, BranchAndBoundSolutionManager solManager)
                                                                                throws  GenotypeException,
                                                                                        CrossingSchemeException{
        
        // construct seed lot obtained from crossing (retrieved from cache if constructed before)
        SeedLot sl = constructSeedLot(Math.max(scheme1.getMinNumGen(), scheme2.getMinNumGen())+1,
                                        scheme1.getFinalPlant(), scheme2.getFinalPlant(), map, solManager);
        
        // run scheme merger
        return new MergeFirstSchemeMerger(scheme1, scheme2, map, solManager, sl).combineSchemes();
        
    }
    
    /**
     * Private implementation of a cross worker which is responsible for combining the currently considered
     * scheme with previously considered schemes, through an additional crossing. All cross workers operate
     * and synchronize on a shared iterator that iterates over the previously considered schemes.
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
                    // check pruning (if all combinations are pruned, we can
                    // save some time by not constructing the obtained seed lot)
                    boolean prune = true;
                    Iterator<CrossingScheme> it1 = curScheme.iterator();
                    while(prune && it1.hasNext()){
                        CrossingScheme alt1 = it1.next();
                        Iterator<CrossingScheme> it2 = toExtend.iterator();
                        while(prune && it2.hasNext()){
                            CrossingScheme alt2 = it2.next();
                            prune = solManager.pruneCrossCurrentSchemeWithSpecificOther(alt1, alt2);
                        }
                    }
                    // create new schemes
                    if(!prune){
                        newSchemes.addAll(combineSchemes(curScheme, toExtend, map, solManager));
                    }
                }
            }
            return newSchemes;
        }
        
    }

}
