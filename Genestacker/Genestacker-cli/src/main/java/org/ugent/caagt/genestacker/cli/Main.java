package org.ugent.caagt.genestacker.cli;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.ugent.caagt.genestacker.Haplotype;
import org.ugent.caagt.genestacker.Plant;
import org.ugent.caagt.genestacker.exceptions.GenestackerException;
import org.ugent.caagt.genestacker.io.*;
import org.ugent.caagt.genestacker.search.*;
import org.ugent.caagt.genestacker.search.bb.*;
import org.ugent.caagt.genestacker.search.bb.heuristics.*;
import org.ugent.caagt.genestacker.search.constraints.*;
import org.ugent.caagt.genestacker.util.GenestackerConstants;
import org.ugent.caagt.genestacker.util.TimeFormatting;

/**
 * Command line interface for Gene Stacker software.
 */
public class Main
{
    
    // logger
    private Logger logger = LogManager.getLogger(Main.class);
    
    // options to check before parsing other options
    private Options checkFirstOptions;
    // required parameters
    private Options requiredOptions;
    // constraints
    private Options constraintOptions;
    // heuristic options
    private Options heuristicPresetsOptions;
    private Options individualHeuristicOptions;
    // verbosity options
    private Options verbosityOptions;
    // general options
    private Options miscOptions;
    // groups all options
    private Options allOptions;
    
    // parameter values
    private double successProb;
    private String outputFile;
    private String inputFile;
    // constraints
    private List<Constraint> constraints;
    private NumberOfSeedsPerCrossing numSeeds;
    private boolean homozygousIdeotypeParents;
    // heuristics
    private boolean h0 = false;     // initial plant filtering
    private boolean h1a = false;    // weak improvement heuristic
    private boolean h1b = false;    // strong improvement heuristic
    private boolean h2a = false;    // seed lot filtering (w.r.t. weak improvement)
    private boolean h2b = false;    // seed lot filtering (w.r.t. strong improvement)
    private boolean h3 = false;     // Pareto optimal subschemes
    private boolean h3s1 = false;   // H3 seeded (version 1)
    private boolean h3s2 = false;   // H3 seeded (version 2 -- extra seed lot filtering in second run)
    private boolean h4 = false;     // Pareto optimal seed lots
    private boolean h5 = false;     // heuristic seed lot construction
    private boolean h5c = false;    // heuristic seed lot construction with consistency check
    private boolean h6 = false;     // heuristic population size bound
    // option for heuristic seed lot constructor
    private int maxNumCrossovers = GenestackerConstants.UNLIMITED_CROSSOVERS;
    // misc options
    private GraphFileFormat graphFileFormat;
    private GraphColorScheme graphColorScheme;
    private boolean useKosambiMap;
    private boolean tree;
    private long runtimeLimit = GenestackerConstants.NO_RUNTIME_LIMIT;
    private boolean minimizePopSizeOnly;
    private int numThreads;
    private boolean writeIntermediateOutput;
    
    // total runtime (ms)
    private long totalRuntime;
    
    /**
     * Run Gene Stacker software from command line.
     * 
     * @param args arguments to be parsed using Apache commons CLI
     */
    public static void main( String[] args ){
        Main cli = new Main();
        cli.run(args);
    }
    
    private void run(String args[]){
        // setup options
        setupOptions();
        // parse special options only
        try {
            // create command line parser that ignores unknown options
            CommandLineParser parser = new ExtendedPosixParser(true);
            // parse special options
            CommandLine cmd = parser.parse(checkFirstOptions, args, false);
            parseSpecialOptions(cmd);
        } catch (ParseException ex) {
            logger.error("Invalid usage: {}", ex.getMessage());
            printHelp();
            System.exit(1);
        }
        // go ahead and parse full options
        try {
            // create command line parser
            CommandLineParser parser = new PosixParser();
            // parse options
            CommandLine cmd = parser.parse(allOptions, args);
            parseOptions(cmd);
        } catch (ParseException ex){
            logger.error("Invalid usage: {}", ex.getMessage());
            printHelp();
            System.exit(1);
        }
        // run search engine
        try {
            // check config file
            checkConfigFile();
            // run engine
            search();
        } catch (GenestackerException ex){
            // report genestacker errors
            logger.error("Fatal error occurred while running Gene Stacker", ex);            
            System.exit(1);
        } catch (IOException | ArchiveException ex){
            // report IO errors
            logger.error("Input/output error: {}", ex.getMessage());            
            System.exit(1);
        } catch (Exception ex){
            // unexpected error (catch-all)
            logger.error("Unexpected error", ex);
            System.exit(1);
        }
    }
    
    private void checkConfigFile() throws GenestackerException {
        // try to read config file entries
        GenestackerResourceBundle.getConfig("dot.path");
        GenestackerResourceBundle.getConfig("crossingscheme.xml.schema");
        GenestackerResourceBundle.getConfig("genestackerinput.xml.schema");
    }
    
    private void setupOptions(){
        
        // setup required params
        
        Option successProbOption = OptionBuilder.withLongOpt("success-prob")
                                          .hasArg()
                                          .withArgName("p")
                                          .withDescription("desired total success probability for a crossing schedule")
                                          .create("s");        
        successProbOption.setRequired(true);
        
        Option maxGenOption = OptionBuilder.withLongOpt("max-gen")
                                     .hasArg()
                                     .withArgName("g")
                                     .withDescription("maximum number of generations")
                                     .create("g");
        maxGenOption.setRequired(true);
        
        requiredOptions = new Options();
        requiredOptions.addOption(successProbOption);
        requiredOptions.addOption(maxGenOption);
        
        // setup constraints
        
        Option maxCrossingsOption = OptionBuilder.withLongOpt("max-total-crossings")
                                     .hasArg()
                                     .withArgName("c")
                                     .withDescription("maximum total number of crossings")
                                     .create("tc");
        Option maxLPAOption = OptionBuilder.withLongOpt("max-linkage-phase-ambiguity")
                                      .hasArg()
                                      .withArgName("a")
                                      .withDescription("maximum allowed overall linkage phase ambiguity")
                                      .create("lpa");
        Option maxPlantsPerGenOption = OptionBuilder.withLongOpt("max-plants-per-gen")
                                               .hasArg()
                                               .withArgName("p")
                                               .withDescription("maximum allowed number of plants per generation")
                                               .create("p");
        Option maxCrossingsWithPlantOption = OptionBuilder.withLongOpt("max-crossings-with-plant")
                                                    .hasArg()
                                                    .withArgName("c")
                                                    .withDescription("maximum number of crossings with same plant")
                                                    .create("c");
        Option numSeedsPerCrossingOption = OptionBuilder.withLongOpt("num-seeds")
                                                  .hasArg()
                                                  .withArgName("s")
                                                  .withDescription("number of seeds obtained from one crossing")
                                                  .create("S");
        Option homozygousIdeotypeParentsOption = new Option("hip", "homozygous-ideotype-parents", false, "require the parents of the ideotype, i.e. the plants in the penultimate generation, "
                                                                                                          + " to be homozygous");
                
        constraintOptions = new Options();
        constraintOptions.addOption(maxCrossingsOption);
        constraintOptions.addOption(maxLPAOption);
        constraintOptions.addOption(maxPlantsPerGenOption);
        constraintOptions.addOption(maxCrossingsWithPlantOption);
        constraintOptions.addOption(numSeedsPerCrossingOption);
        constraintOptions.addOption(homozygousIdeotypeParentsOption);
        
        // setup heuristic options
        
        Option filterInitialPlantsOption = new Option("h0", "filter-initial-plants", false, "filter initial plants by removing any plant which is strictly dominated by another"
                                                        + " initial plant, according to the weak improvement heuristic");
        Option weakImprHeuristicOption = new Option("h1a", "weak-improvement", false, "apply weak improvement heuristic to force improvement from ancestors to offspring");
        Option strongImprHeuristicOption = new Option("h1b", "strong-improvement", false, "apply strong improvement heuristic to force improvement from ancestors to offspring"
                                                                                + " (overrides h1a)");
        Option seedLotWeakImprovementFilterOption = new Option("h2a", "filter-seed-lots-weak", false, "heuristically filter the seed lots to remove non promising genotypes"
                                                                                + " (w.r.t weak improvement)");
        Option seedLotStrongImprovementFilterOption = new Option("h2b", "filter-seed-lots-strong", false, "heuristically filter the seed lots to remove non promising genotypes"
                                                                                + " (w.r.t strong improvement, overrides h2a)");
        Option optimalSubschemeOption = new Option("h3", "optimal-subscheme", false, "keep track of a pareto frontier for each intermediary plant and bound"
                                                                                + " partial subschemes which are dominated by previously queued schemes resulting in the same"
                                                                                + " intermediary plant, when considering to add them to the scheme queue");
        Option optimalSubschemeSeeded1Option = new Option("h3s1", "optimal-subscheme-seeded-1", false, "(overrides -h3 option) first run the algorithm with h3, followed by a second run without h3"
                                                                                + "  -- possible other activated heuristics are used in both runs");
        Option optimalSubschemeSeeded2Option = new Option("h3s2", "optimal-subscheme-seeded-2", false, "(overrides -h3 and -h3s1 options) first run the algorithm with h3, followed by a second run without h3"
                                                                                + " but with an extra filtering of seed lots: only allow genotypes of which each chromosome"
                                                                                + " contains two haplotypes that occured in this chromosome in an inner plant node (i.e. not the initial parents)"
                                                                                + " in some solutions found in the first run using h3"
                                                                                + "  -- possible other activated heuristics are used in both runs");
        Option optimalSeedLotOption = new Option("h4", "optimal-seedlot", false, "enforce that the parent seed lot of a plant is always Pareto optimal among those "
                                                                                + "available at the generation in which the plant is grown, w.r.t. the probability "
                                                                                + "of obtaining this plant and the respective linkage phase ambiguity");
        Option heuristicSeedLotConstructionOption = new Option("h5", "heuristic-seedlot-construction", false, "use a heuristic seed lot constructor that only generates 'interesting' genotypes "
                                                                                + "instead of the entire seed lot (before possible filterings) by only allowing crossovers that work towards one "
                                                                                + "of the target haplotypes within each chromosome");
        Option consistentHeuristicSeedLotConstructionOption = new Option("h5c", "consistent-heuristic-seedlot-construction", false, "(overrides -h5) use an extended heuristic seed lot constructor which also requires consistency "
                                                                                + "within a chromosome, i.e. all crossovers within a chromosome work towards the same target haplotype (note: in case of homozygous ideotypes "
                                                                                + "this does not make any difference, as then there is only one target haplotype for each chromosome)");
        Option maxNumCrossoversOption = OptionBuilder.withLongOpt("max-crossovers")
                                                  .hasArg()
                                                  .withArgName("c")
                                                  .withDescription("maximum number of crossovers per chromosome during construction of gametes (positive integer value); only applicable "
                                                                 + "in combination with the heuristic seed lot constructor (h5/h5c) or a preset that uses this constructor")
                                                  .create("mco");
        Option heuristicPopSizeBoundOption = new Option("h6", "heuristic-popsize-bound", false, "apply a heuristic lower bound on the popluation size of any extension of a "
                                                                                              + "given partial scheme, based on the probability of those crossovers that are "
                                                                                              + "necessarily still required to obtain the ideotype");
        
        individualHeuristicOptions = new Options();
        individualHeuristicOptions.addOption(filterInitialPlantsOption);
        individualHeuristicOptions.addOption(weakImprHeuristicOption);
        individualHeuristicOptions.addOption(strongImprHeuristicOption);
        individualHeuristicOptions.addOption(seedLotWeakImprovementFilterOption);
        individualHeuristicOptions.addOption(seedLotStrongImprovementFilterOption);
        individualHeuristicOptions.addOption(optimalSubschemeOption);
        individualHeuristicOptions.addOption(optimalSubschemeSeeded1Option);
        individualHeuristicOptions.addOption(optimalSubschemeSeeded2Option);
        individualHeuristicOptions.addOption(optimalSeedLotOption);
        individualHeuristicOptions.addOption(heuristicSeedLotConstructionOption);
        individualHeuristicOptions.addOption(consistentHeuristicSeedLotConstructionOption);
        individualHeuristicOptions.addOption(maxNumCrossoversOption);
        individualHeuristicOptions.addOption(heuristicPopSizeBoundOption);
        
        Option heuristicsOptionBest = new Option("best", "best", false, "do not apply any heuristics, this results in a slower algorithm but"
                                                            + " may lead to superior solutions (by default h0, h1a, h2a, h3s1, h4, h5 and h6 are applied)");
        Option heuristicsOptionBetter = new Option("better", "better", false, "do not apply heuristic h4, h5 nor h6, but retain heuristics h0, h1a, h2a and h3s1");
        Option heuristicsOptionFaster = new Option("faster", "faster", false, "apply heuristics h0, h4 and h6 as in the default setting, but use"
                                                                              + " h1b and h2b instead of h1a and h2a,"
                                                                              + " h3s2 instead of h3s1 and switch to h5c instead of h5");
        Option heuristicsOptionFastest = new Option("fastest", "fastest", false, "apply heuristics h0, h1b, h2b, h3, h4, h5c and h6");
        
        heuristicPresetsOptions = new Options();
        heuristicPresetsOptions.addOption(heuristicsOptionBest);
        heuristicPresetsOptions.addOption(heuristicsOptionBetter);
        heuristicPresetsOptions.addOption(heuristicsOptionFaster);
        heuristicPresetsOptions.addOption(heuristicsOptionFastest);
        
        // setup verbosity options
        
        Option verboseOption = new Option("v", "verbose", false, "be extra verbose");
        Option veryVerboseOption = new Option("vv", "very-verbose", false, "be ridiculously verbose (overrides -v,--verbose)");
        Option debugOption = new Option("d", "debug", false, "run in debug mode (overrides -vv,--very-verbose), which will create"
                                            + " diagrams for every partial scheme taken from the queue and any newly reported solution");
        
        verbosityOptions = new Options();
        verbosityOptions.addOption(verboseOption);
        verbosityOptions.addOption(veryVerboseOption);
        verbosityOptions.addOption(debugOption);
        
        // setup misc options
        
        Option graphFileFormatOption = OptionBuilder.withLongOpt("graph-file-format")
                                                  .hasArg()
                                                  .withArgName("f")
                                                  .withDescription("output file format used for graphs created by Graphviz (pdf, eps, ps, svg, png, bmp, jpg or gif), defaults to pdf")
                                                  .create("gf");
        Option noColorOption = new Option("nc", "no-color", false, "produce greyscale graphs instead of the default colored graphs");
        Option kosambiOption = new Option("k", "kosambi", false, "use the Kosambi mapping function to translate genetic distances into crossover probabilities, instead of"
                                                               + " the default Haldane mapping function");
        Option treeOption = new Option("tree", "tree", false, "construct crossing schemes with tree structure, i.e. no reuse nor selfings are allowed (except from a "
                                                                + "possible final selfing creating the ideotype); overrides heuristic H0 (initial plant filtering) because "
                                                                + "when reuse is not allowed it might be crucial to use inferior parents as well in the scheme");         
        Option runtimeLimitOption = OptionBuilder.withLongOpt("runtime")
                                                  .hasArg()
                                                  .withArgName("sec")
                                                  .withDescription("runtime limit in seconds (integer value)")
                                                  .create("rt");
        Option minPopSizeOnlyOption = new Option("minp", "min-pop-size-only", false, "only minimize the total population size, i.e. ignore linkage phase ambiguity and number of generations");
        Option numThreadsOption = OptionBuilder.withLongOpt("num-threads")
                                                  .hasArg()
                                                  .withArgName("n")
                                                  .withDescription("specifies the number of threads used for extension of a partial scheme through crossings, by default this value is read "
                                                                    + "from the environment variable OMP_NUM_THREADS, if set, else it defaults to the number of available threads on the machine")
                                                  .create("thr");
        Option versionOption = new Option("version", "version", false, "print Gene Stacker version (ignores other options)");
        Option helpOption = new Option("help", "help", false, "print help (overrides -version, ignores other options)");
        Option intOutputOption = new Option("int", "intermediate-output", false, "create and update intermediate ZIP package whenever the current Pareto frontier has changed,"
                                                                                + " where a suffix \"-int\" is appended to the output file name for this intermediate file; note that this"
                                                                                + " is expected to slow down the application (intermediate output file is deleted if the search completes)");
        
        miscOptions = new Options();
        miscOptions.addOption(graphFileFormatOption);
        miscOptions.addOption(noColorOption);
        miscOptions.addOption(kosambiOption);
        miscOptions.addOption(treeOption);
        miscOptions.addOption(runtimeLimitOption);
        miscOptions.addOption(minPopSizeOnlyOption);
        miscOptions.addOption(numThreadsOption);
        miscOptions.addOption(versionOption);
        miscOptions.addOption(helpOption);
        miscOptions.addOption(intOutputOption);
        // indicate which options have to be checked prior to the other options
        checkFirstOptions = new Options();
        checkFirstOptions.addOption(versionOption);
        checkFirstOptions.addOption(helpOption);
        
        // group all options
        
        allOptions = new Options();
        Iterator i = requiredOptions.getOptions().iterator();
        while(i.hasNext()){
            allOptions.addOption((Option)i.next());
        }
        i = constraintOptions.getOptions().iterator();
        while(i.hasNext()){
            allOptions.addOption((Option)i.next());
        }
        i = individualHeuristicOptions.getOptions().iterator();
        while(i.hasNext()){
            allOptions.addOption((Option)i.next());
        }
        i = heuristicPresetsOptions.getOptions().iterator();
        while(i.hasNext()){
            allOptions.addOption((Option)i.next());
        }
        i = verbosityOptions.getOptions().iterator();
        while(i.hasNext()){
            allOptions.addOption((Option)i.next());
        }
        i = miscOptions.getOptions().iterator();
        while(i.hasNext()){
            allOptions.addOption((Option)i.next());
        }
        
    }
    
    // parse "special" options that ignore all other (possibly required) options
    private void parseSpecialOptions(CommandLine cmd) {
        // check help
        if(cmd.hasOption("help")){
            printHelp();
            System.exit(0);
        }
        // check version
        if(cmd.hasOption("version")){
            printVersion();
            System.exit(0);
        }
    }
    
    private void printVersion(){
        System.out.println("Gene Stacker v" + PropertiesProvider.getVersion());
    }
    
    private void printHelp(){
        
        System.out.println("");
	System.out.println("usage:\tgenestacker [options] <input-file> <output>");
	System.out.println("");
        System.out.println("\texample: The following command will read the initial plants, genetic map" +
                           "\n\tand desired ideotype from the file 'input.xml' and store the optimized" +
                           "\n\tcrossing scheme(s) in a ZIP file 'output.zip'. The maximum number of" +
                           "\n\tgenerations of such crossing scheme is set to 3 and the desired probability" +
                           "\n\tof success is 0.9. In this case, no further optional constraints are given.");
	System.out.println("");
	System.out.println("\t\tgenestacker -g 3 -s 0.9 input.xml output");
	System.out.println("");
        System.out.println("\tor (long version)");
        System.out.println("");
	System.out.println("\t\tgenestacker --max-gen 3 --success-prob 0.9 input.xml output");
        System.out.println("");
        
        HelpFormatter f = new HelpFormatter();
        f.setWidth(100);
        f.setSyntaxPrefix("");
        
        // print required params
        f.printHelp("Required parameters:", requiredOptions);
        System.out.println("");
        
        // print constraints
        f.printHelp("Constraints:", constraintOptions);
        System.out.println("");
        
        // print heuristic options
        f.printHelp("Search heuristic presets:", heuristicPresetsOptions);
        System.out.println("");
        f.printHelp("Individual search heuristics:", individualHeuristicOptions);
        System.out.println("");
        
        // print verbosity options
        f.printHelp("Verbosity Options:", verbosityOptions);
        System.out.println("");
        
        // print misc options
        f.printHelp("Misc Options:", miscOptions);
        System.out.println("");
        
    }
    
    private void parseOptions(CommandLine cmd) throws ParseException {
        
        // check for <input-file> argument
        String[] args = cmd.getArgs();
        if(args != null && args.length == 2){
            inputFile = args[0];
            outputFile = args[1];
        } else {
            throw new ParseException("Two arguments <input-file> and <output> expected.");
        }
        
        // init constraints
        constraints = new ArrayList<>();
        
        // ### parse required params
        
        // get value of -max-gen
        try {
            int maxGen = Integer.parseInt(cmd.getOptionValue("max-gen"));
            if(!(maxGen >= 0)){
                throw new NumberFormatException();
            }
            constraints.add(new MaxNumGenerations(maxGen));
        } catch(NumberFormatException ex){
            throw new ParseException("Parameter -g,--max-gen should be a nonnegative integer.");
        }
        
        // get value of -success-prob
        try {
            successProb = Double.parseDouble(cmd.getOptionValue("success-prob"));
            if(!(successProb >= 0.0 && successProb <= 1.0)){
                throw new NumberFormatException();
            }
        } catch(NumberFormatException ex){
            throw new ParseException("Parameter -s,--success-prob should be a probability between 0.0 and 1.0.");
        }
        
        // ### parse constraints
        
        // check for -max-total-crossings
        if(cmd.hasOption("max-total-crossings")){
            try {
                int maxCrossings = Integer.parseInt(cmd.getOptionValue("max-total-crossings"));
                if(!(maxCrossings > 0)){
                    throw new NumberFormatException();
                }
                constraints.add(new MaxTotalCrossings(maxCrossings));
            } catch(NumberFormatException ex){
                throw new ParseException("Parameter -tc,--max-total-crossings should be a positive integer.");
            }
        }
        // check for -max-crossings-with-plant
        if(cmd.hasOption("max-crossings-with-plant")){
            try {
                int maxCrossingsWithPlant = Integer.parseInt(cmd.getOptionValue("max-crossings-with-plant"));
                if(!(maxCrossingsWithPlant > 0)){
                    throw new NumberFormatException();
                }
                constraints.add(new MaxCrossingsWithPlant(maxCrossingsWithPlant));
            } catch(NumberFormatException ex){
                throw new ParseException("Parameter -c,--max-crossings-with-plant should be a positive integer.");
            }
        }
        
        // check for -max-plants-per-gen
        if(cmd.hasOption("max-plants-per-gen")){
            try {
                int maxPlants = Integer.parseInt(cmd.getOptionValue("max-plants-per-gen"));
                if(!(maxPlants > 0)){
                    throw new NumberFormatException();
                }
                constraints.add(new MaxPopulationSizePerGeneration(maxPlants));
            } catch(NumberFormatException ex){
                throw new ParseException("Parameter -p,--max-plants-per-gen should be a positive integer.");
            }
        }
        
        //check for -max-linkage-phase-ambiguity
        if(cmd.hasOption("max-linkage-phase-ambiguity")){
            try {
                double maxLPA = Double.parseDouble(cmd.getOptionValue("max-linkage-phase-ambiguity"));
                if(!(maxLPA >= 0.0 && maxLPA <= 1.0)){
                    throw new NumberFormatException();
                }
                constraints.add(new MaxLinkagePhaseAmbiguity(maxLPA));
            } catch(NumberFormatException ex){
                throw new ParseException("Parameter -lpa,--max-linkage-phase-ambiguity should be a probability between 0.0 and 1.0.");
            }
        }
        
        // check for -num-seeds
        if(cmd.hasOption("num-seeds")){
            try {
                int nSeeds = Integer.parseInt(cmd.getOptionValue("num-seeds"));
                if(!(nSeeds > 0)){
                    throw new NumberFormatException();
                }
                numSeeds = new NumberOfSeedsPerCrossing(nSeeds);
            } catch(NumberFormatException ex){
                throw new ParseException("Parameter -S,--num-seeds should be a positive integer.");
            }
        }
        
        // check for homozygous-ideotype-parents
        homozygousIdeotypeParents = cmd.hasOption("homozygous-ideotype-parents");
        
        // ### parse heuristics options
        
        // presets
        
        int heuristicPresetsSpecified = 0;
        if(cmd.hasOption("best")){
            // no heuristics
            h0 = false;
            h1a = false;
            h1b = false;
            h2a = false;
            h2b = false;
            h3 = false;
            h3s1 = false;
            h3s2 = false;
            h4 = false;
            h5 = false;
            h5c = false;
            h6 = false;
            heuristicPresetsSpecified++;
        }
        if(cmd.hasOption("better")){
            // h0, h1a, h2a and h3s1
            h0 = true;
            h1a = true;
            h1b = false;
            h2a = true;
            h2b = false;
            h3 = false;
            h3s1 = true;
            h3s2 = false;
            h4 = false;
            h5 = false;
            h5c = false;
            h6 = false;
            heuristicPresetsSpecified++;
        }
        if(cmd.hasOption("faster")){
            // h0, h1b, h2b, h3s2, h4, h5c, h6
            h0 = true;
            h1a = false;
            h1b = true;
            h2a = false;
            h2b = true;
            h3 = false;
            h3s1 = false;
            h3s2 = true;
            h4 = true;
            h5 = false;
            h5c = true;
            h6 = true;
            heuristicPresetsSpecified++;
        }
        if(cmd.hasOption("fastest")){
            // h0, h1b, h2b, h3, h4, h5c, h6
            h0 = true;
            h1a = false;
            h1b = true;
            h2a = false;
            h2b = true;
            h3 = true;
            h3s1 = false;
            h3s2 = false;
            h4 = true;
            h5 = false;
            h5c = true;
            h6 = true;
            heuristicPresetsSpecified++;
        }
        if(heuristicPresetsSpecified > 1){
            throw new ParseException("Heuristic presets (best, better, fast, fastest) cannot be combined; please choose only one of these options.");
        }
        
        // individual
        
        int individualHeuristicsSpecified = 0;
        if(cmd.hasOption("filter-initial-plants")){
            h0 = true;
            individualHeuristicsSpecified++;
        }
        if(cmd.hasOption("weak-improvement")){
            h1a = true;
            individualHeuristicsSpecified++;
        }
        if(cmd.hasOption("strong-improvement")){
            h1b = true;
            individualHeuristicsSpecified++;
            // overrides h1a
            logger.warn("Option -h1b,--strong-improvement overrides -h1a,--weak-improvement");
            h1a = false;
        }
        if(cmd.hasOption("filter-seed-lots-weak")){
            h2a = true;
            individualHeuristicsSpecified++;
        }
        if(cmd.hasOption("filter-seed-lots-strong")){
            h2b = true;
            individualHeuristicsSpecified++;
            // overrides h2a
            logger.warn("Option -h2b,--filter-seed-lots-strong overrides -h2a,--filter-seed-lots-weak");
            h2a = false;
        }
        if(cmd.hasOption("optimal-subscheme")){
            h3 = true;
            individualHeuristicsSpecified++;
        }
        if(cmd.hasOption("optimal-subscheme-seeded-1")){
            h3s1 = true;
            // overrides h3
            logger.warn("Option -h3s1,--optimal-subscheme-seeded-1 overrides -h3,--optimal-subscheme");
            h3 = false;
            individualHeuristicsSpecified++;
        }
        if(cmd.hasOption("optimal-subscheme-seeded-2")){
            h3s2 = true;
            // overrides h3 and h3s1
            logger.warn("Option -h3s2,--optimal-subscheme-seeded-2 overrides -h3,--optimal-subscheme");
            logger.warn("Option -h3s2,--optimal-subscheme-seeded-2 overrides -h3s1,--optimal-subscheme-seeded-1");
            h3s1 = false;
            h3 = false;
            individualHeuristicsSpecified++;
        }
        if(cmd.hasOption("optimal-seedlot")){
            h4 = true;
            individualHeuristicsSpecified++;
        }
        if(cmd.hasOption("heuristic-seedlot-construction")){
            h5 = true;
            individualHeuristicsSpecified++;
        }    
        if(cmd.hasOption("consistent-heuristic-seedlot-construction")){
            h5c = true;
            // overrides h5
            logger.warn("Option -h5c,--consistent-heuristic-seedlot-construction overrides  -h5,--heuristic-seedlot-construction");
            h5 = false;
            individualHeuristicsSpecified++;
        }
        if(cmd.hasOption("heuristic-popsize-bound")){
            h6 = true;
            individualHeuristicsSpecified++;
        }
        if(heuristicPresetsSpecified > 0 && individualHeuristicsSpecified > 0){
            throw new ParseException("Heuristic presets cannot be combined with individual heuristic options."
                                        + " Please specify either one preset or a combination of individual heuristics.");
        }
        
        // default
        
        if(heuristicPresetsSpecified == 0 && individualHeuristicsSpecified == 0){
            // no heuristic options specified; default to h0, h1a, h2a, h3s1, h4, h5 and h6
            h0 = true;
            h1a = true;
            h1b = false;
            h2a = true;
            h2b = false;
            h3 = false;
            h3s1 = true;
            h3s2 = false;
            h4 = true;
            h5 = true;
            h5c = false;
            h6 = true;
        }
        
        // check for -max-crossovers
        if(cmd.hasOption("max-crossovers")){
            // verify that h5 is enabled
            if(!(h5 || h5c)){
                throw new ParseException("Parameter -mco,--max-crossovers can only be specified when using a heuristic seed lot constructor (h5 or h5c).");
            }
            // parse given value and overwrite default
            try {
                maxNumCrossovers = Integer.parseInt(cmd.getOptionValue("max-crossovers"));
                if(!(maxNumCrossovers > 0)){
                    throw new NumberFormatException();
                }
            } catch(NumberFormatException ex){
                throw new ParseException("Parameter -mco,--max-crossovers should be a positive integer.");
            }
        }   
        
        // ### parse verbosity options
        
        // check for -debug, -very-verbose and -verbose in this order
        if(cmd.hasOption("debug")){
            // load debug log settings
            Configurator.initialize("config", null, "log4j2-debug.xml");
        } else if (cmd.hasOption("very-verbose")){
            // load very verbose log settings
            Configurator.initialize("config", null, "log4j2-very-verbose.xml");
        } else if (cmd.hasOption("verbose")){
            // load verbose log settings
            Configurator.initialize("config", null, "log4j2-verbose.xml");
        } else {
            // load default log settings
            Configurator.initialize("config", null, "log4j2.xml");
        }
        
        // ### parse misc options
        
        // check for -graph-file-format
        graphFileFormat = GraphFileFormat.PDF; // defaults to pdf
        if(cmd.hasOption("graph-file-format")){
            String format = cmd.getOptionValue("graph-file-format");
            switch(format){
                case "pdf": graphFileFormat = GraphFileFormat.PDF;
                    break;
                case "eps": graphFileFormat = GraphFileFormat.EPS;
                    break;
                case "ps": graphFileFormat = GraphFileFormat.PS;
                    break;
                case "png": graphFileFormat = GraphFileFormat.PNG;
                    break;
                case "bmp": graphFileFormat = GraphFileFormat.BMP;
                    break;
                case "jpg": graphFileFormat = GraphFileFormat.JPG;
                    break;
                case "gif": graphFileFormat = GraphFileFormat.GIF;
                    break;
                case "svg": graphFileFormat = GraphFileFormat.SVG;
                    break;
                default:
                    graphFileFormat = GraphFileFormat.PDF;
                    logger.warn("[Unknown graph output file format specified, sticking to default (pdf)");
                    break;
            }
        }
        
        // check for -no-color
        graphColorScheme = GraphColorScheme.COLORED;
        if(cmd.hasOption("no-color")){
            graphColorScheme = GraphColorScheme.GREYSCALE;
        }
        
        // check for -kosambi
        useKosambiMap = cmd.hasOption("kosambi");
        
        // check for -tree
        tree = cmd.hasOption("tree");
        if(tree){
            // switch of initial parent filtering in case of tree structure
            logger.warn("Option -tree set: producing tree structures only; overrides -h0,--filter-initial-plants");
            h0 = false;
        }
        
        // check for -runtime
        if(cmd.hasOption("runtime")){
            try {
                int runtimeSecs = Integer.parseInt(cmd.getOptionValue("runtime"));
                if(!(runtimeSecs > 0)){
                    throw new NumberFormatException();
                }
                runtimeLimit = runtimeSecs*1000;
            } catch(NumberFormatException ex){
                throw new ParseException("Parameter -rt,--runtime should be a positive integer.");
            }
        }
        
        // check for min-pop-size-only
        minimizePopSizeOnly = cmd.hasOption("min-pop-size-only");
        
        // set num threads to be used as cross workers
        if(cmd.hasOption("num-threads")){
            try {
                numThreads = Integer.parseInt(cmd.getOptionValue("num-threads"));
                if(!(numThreads > 0)){
                    throw new NumberFormatException();
                }
            } catch(NumberFormatException ex){
                throw new ParseException("Parameter -thr,--num-threads should be a positive integer.");
            }
        } else {
            // CLI option not set, first check OMP_NUM_THREADS
            try {
                numThreads = Integer.parseInt(System.getenv("OMP_NUM_THREADS"));
                if(!(numThreads > 0)){
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex){
                // OMP_NUM_THREADS not set (or no positive integer value)
                // default to num available threads on machine
                numThreads = Runtime.getRuntime().availableProcessors();
            }
        }
        
        // check for intermediate-output
        writeIntermediateOutput = cmd.hasOption("intermediate-output");
        
    }
    
    private void search() throws GenestackerException, IOException, ArchiveException{
        // check if input file exists
        if(!Files.exists(Paths.get(inputFile))){
            throw new FileNotFoundException("Could not find input file '" + inputFile + "'.");
        }
        
        // format outputFile string
        if(!outputFile.endsWith(".zip")){
            outputFile += ".zip";
        }
        
        // check if output file already exists
        if(Files.exists(Paths.get(outputFile))){
            throw new FileAlreadyExistsException("Output file '" + outputFile + "' already exists.");
        }
                
        /********************/
        /* PARSE INPUT FILE */
        /********************/

        logger.info("Parsing input file ...");
        
        GenestackerInputParser inputParser = new GenestackerInputParser();
        GenestackerInput input = inputParser.parse(new File(inputFile), useKosambiMap);
        if(useKosambiMap){
            logger.info("Using Kosambi mapping function (instead of default: Haldane)");
        }
        
        /************************/
        /* RUN BRANCH AND BOUND */
        /************************/
        
        // init total runtime to zero milliseconds
        totalRuntime = 0;
        // run B&B
        ParetoFrontier frontier = runBranchAndBound(input, runtimeLimit);
        
        // print total runtime
        logger.info("Total runtime = {}", TimeFormatting.formatTime(totalRuntime));
        
        // output results
        output(frontier, outputFile);
    }
    
    private ParetoFrontier runBranchAndBound(GenestackerInput input, long timeLimit) throws GenestackerException{
        // print applied heuristics/filters info
        logger.info("Running Branch and Bound engine {} ...", formatActivatedHeuristicsInfo());
        
        // init dominates relation
        DominatesRelation<CrossingSchemeDescriptor> dominatesRelation;
        if(minimizePopSizeOnly){
            // only minimize population size (ignore linkage phase ambiguity and number of generations)
            dominatesRelation = new PopulationSizeOnlyDominatesRelation();
            // print warning
            logger.warn("Option -minp,--min-pop-size-only set: minimizing population size only");
        } else {
            // default dominates relation taking into account pop size, linkage phase ambiguity and number of generations
            dominatesRelation = new DefaultDominatesRelation();
        }
                
        // init seed lot filters
        List<SeedLotFilter> seedLotFilters = new ArrayList<>();
        if(h2a){
            seedLotFilters.add(new ImprovementSeedLotFilter(new WeakGenotypeImprovement(input.getIdeotype())));
        }
        if(h2b){
            seedLotFilters.add(new ImprovementSeedLotFilter(new StrongGenotypeImprovement(input.getIdeotype(), input.getGeneticMap())));
        }
        
        // init initial plant filter
        InitialPlantFilter initialPlantFilter = null;
        if(h0){
            initialPlantFilter = new InitialPlantFilter(new DefaultDuplicatePlantFilter(), new DefaultPlantImprovement(new WeakGenotypeImprovement(input.getIdeotype())));
        }
        
        // init other heuristics
        List<Heuristic> heurList = new ArrayList<>();
        if(h1a){
            heurList.add(new ImprovementOverAncestorsHeuristic(new DefaultPlantImprovement(new WeakGenotypeImprovement(input.getIdeotype()))));
        }
        if(h1b){
            heurList.add(new ImprovementOverAncestorsHeuristic(new DefaultPlantImprovement(new StrongGenotypeImprovement(input.getIdeotype(), input.getGeneticMap()))));
        }
        if(h3){
            heurList.add(new OptimalSubschemeHeuristic(dominatesRelation));
        }
        if(h4){
            heurList.add(new OptimalSeedLotHeuristic(new OptimalSeedLotParetoFrontierFactory()));
        }
        
        // tree structure option set?
        if(tree){
            heurList.add(new TreeHeuristic(input.getIdeotype()));
        }
        
        Heuristics heuristics = new Heuristics(heurList);
        
        SeedLotConstructor seedLotConstructor;
        if(h5 || h5c){
            // heuristic seed lot constructor
            if(h5){
                // h5
                seedLotConstructor = new HeuristicSeedLotConstructor(input.getGeneticMap(), input.getIdeotype(), maxNumCrossovers, false);
            } else {
                // h5c
                seedLotConstructor = new HeuristicSeedLotConstructor(input.getGeneticMap(), input.getIdeotype(), maxNumCrossovers, true);
            }
            if(maxNumCrossovers != GenestackerConstants.UNLIMITED_CROSSOVERS){
                // print warning
                logger.warn("Number of crossovers per chromosome limited to {}", maxNumCrossovers);
            }
        } else {
            // default seed lot constructor
            seedLotConstructor = new DefaultSeedLotConstructor(input.getGeneticMap());
        }
        
        // initialize population size tools
        PopulationSizeTools popSizeTools = new DefaultPopulationSizeTools(successProb);
        
        // initialize heuristic population size bound if applied
        if(h6){
            heuristics.addHeuristic(new HeuristicPopulationSizeBound(input.getInitialPlants(), input.getIdeotype(), input.getGeneticMap(), popSizeTools));
        }

        // create B&B engine
        BranchAndBound engine = new BranchAndBound(input, graphFileFormat, graphColorScheme, popSizeTools, constraints, numSeeds, heuristics,
                                                    seedLotFilters, initialPlantFilter, seedLotConstructor, dominatesRelation, homozygousIdeotypeParents);
        // write intermediate output files ?
        if(writeIntermediateOutput){
            engine.enableIntermediateOutput(getIntermediateOutputFileName());
        }
        // run B&B engine
        ParetoFrontier frontier;
        if(!dualRun()){
            
            // ### single run  ###
            
            frontier = engine.search(timeLimit, numThreads);
            totalRuntime += engine.getStop() - engine.getStart();
            
        } else {
            
            // two consecutive runs: first with h3, second without h3 (two possible versions; h3s1 or h3s2)
                        
            // first run: enable h3 heuristic
            Heuristic h3heur = new OptimalSubschemeHeuristic(dominatesRelation);
            heuristics.addHeuristic(h3heur);
            h3 = true;
            engine.setHeuristics(heuristics);
                                    
            // run search
            logger.info("Run 1 {} ...", formatActivatedHeuristicsInfo(true, ""));
            frontier = engine.search(timeLimit, numThreads);
            long run1time = engine.getStop() - engine.getStart();
            totalRuntime += run1time;

            // check if time left for second run
            boolean timeLeft = true;
            long run2timeLimit = GenestackerConstants.NO_RUNTIME_LIMIT;
            if(timeLimit != GenestackerConstants.NO_RUNTIME_LIMIT){
                run2timeLimit = timeLimit - run1time;
                timeLeft = run2timeLimit > 0;
            }
            if(timeLeft){
                // second run:  disable h3
                heuristics.removeHeuristic(h3heur);
                h3 = false;
                engine.setHeuristics(heuristics);
                // in case of h3s2 only: add extra seed lot filter based on haplotypes occurring in the solutions found in the first run
                if(h3s2){
                    // gather occurring haplotypes
                    List<Set<Haplotype>> haplotypes = gatherHaplotypes(input, frontier);
                    // set additional filter
                    seedLotFilters.add(new RestrictedHaplotypesSeedLotFilter(haplotypes));
                    engine.setSeedLotFilters(seedLotFilters); // note: this will (and should!) clear the engine's seed lot cache as a side effect
                }
                // set initial Pareto frontier
                engine.setInitialFrontier(frontier);
                // second run
                logger.info("Run 2 {} ...", formatActivatedHeuristicsInfo(true, h3s2 ? " + extra seed lot filtering" : ""));
                frontier = engine.search(run2timeLimit, numThreads);
                long run2time = engine.getStop() - engine.getStart();
                totalRuntime += run2time;
            }
        }
        
        return frontier;
    }
    
    /**
     * Generate string with information about the currently activated heuristics and seed lot filters.
     */
    private String formatActivatedHeuristicsInfo(boolean skipH3Seeded, String suffix){
        StringBuilder heuristicsString = new StringBuilder();
        if(!h0 && !h1a && !h1b && !h2a && !h2b && !h3 && !h3s1 && !h3s2 && !h4 && !h5 & !h5c){
            heuristicsString.append("none");
        } else {
            if(h0){
                heuristicsString.append("h0 ");
            }
            if(h1a){
                heuristicsString.append("h1a ");
            }
            if(h1b){
                heuristicsString.append("h1b ");
            }
            if(h2a){
                heuristicsString.append("h2a ");
            }
            if(h2b){
                heuristicsString.append("h2b ");
            }
            if(h3){
                heuristicsString.append("h3 ");
            }
            if(h3s1 && !skipH3Seeded){
                heuristicsString.append("h3s1 ");
            }
            if(h3s2 && !skipH3Seeded){
                heuristicsString.append("h3s2 ");
            }
            if(h4){
                heuristicsString.append("h4 ");
            }
            if(h5){
                heuristicsString.append("h5 ");
            }    
            if(h5c){
                heuristicsString.append("h5c ");
            }
            if(maxNumCrossovers != GenestackerConstants.UNLIMITED_CROSSOVERS){
                heuristicsString.append("mco=").append(maxNumCrossovers).append(" ");
            }
            if(h6){
                heuristicsString.append("h6 ");
            }
            heuristicsString.deleteCharAt(heuristicsString.length()-1);
        }
        return "[heuristics: " + heuristicsString + suffix + "]";
    }
    
    private String formatActivatedHeuristicsInfo(){
        return formatActivatedHeuristicsInfo(false, "");
    }

    /**
     * Returns whether we will perform two consecutive runs of the algorithm, depending on the heuristics that have been set.
     */
    private boolean dualRun(){
        return h3s1 || h3s2;
    }
    
    /**
     * Generate the set of all haplotypes occurring in any solution in the given Pareto frontier,
     * grouped per chromosome (for each chromosome the returned list contains a distinct set, in
     * the same order as the chromosomes).
     */
    private List<Set<Haplotype>> gatherHaplotypes(GenestackerInput input, ParetoFrontier frontier){
        List<Set<Haplotype>> haplotypes = new ArrayList<>();
        // init empty set for each chromosome
        for(int c=0; c<input.getIdeotype().nrOfChromosomes(); c++){
            haplotypes.add(new HashSet<Haplotype>());
        }
        // register haplotypes occuring in solutions
        Map<Integer, Set<CrossingScheme>> schemes = frontier.getSchemes();
        for(int gen : schemes.keySet()){
            if(gen >= 1){ // ignore haplotypes from initial parents
                for(CrossingScheme scheme : schemes.get(gen)){
                    // go through all plants occurring in scheme
                    for(PlantNode pn : scheme.getPlantNodes()){
                        Plant p = pn.getPlant();
                        // go through chromosomes of plant
                        for(int c=0; c<p.getGenotype().nrOfChromosomes(); c++){
                            // register both haplotypes of the chromosome
                            Haplotype[] haps = p.getGenotype().getChromosomes().get(c).getHaplotypes();
                            haplotypes.get(c).add(haps[0]);
                            haplotypes.get(c).add(haps[1]);
                        }
                    }
                }
            }
        }
        return haplotypes;
    }
    
    /**
     * Output the results, both on the terminal as well as in a ZIP file.
     * If the solution is empty, a message is printed and no ZIP file is generated.
     */
    private void output(ParetoFrontier frontier, String outputFile) throws IOException, ArchiveException, GenestackerException {
        System.out.println("");
        System.out.println("# Results:");
        // any solutions found ?
        if(frontier.getNumSchemes() > 0){
            // print to standard output
            int numScheme = 0;
            for(Map.Entry<Integer, Set<CrossingScheme>> gen : frontier.getSchemes().entrySet()){
                int numGen = gen.getKey();
                Set<CrossingScheme> schemes = gen.getValue();
                System.out.println("");
                System.out.println("# Found " + schemes.size() + " scheme(s) with " + numGen + " generations:");
                for(CrossingScheme s : schemes){
                    numScheme++;
                    System.out.println("");
                    System.out.println("===");
                    System.out.println("Scheme " + numScheme + ":");
                    System.out.println("===");
                    s.print();
                }
            }
            System.out.println("");
            // generate ZIP package
            logger.info("Generating output file ...");
            new ZIPWriter().createZIP(frontier, graphFileFormat, graphColorScheme, outputFile);
            // remove intermediate output file if generated
            if(writeIntermediateOutput){
                logger.info("Deleting intermediate output file ...");
                new File(getIntermediateOutputFileName()).delete();
            }
        } else {
            System.out.println("\n!! NO SOLUTIONS FOUND !!\n");
        }
    }
    
    private String getIntermediateOutputFileName(){
        return outputFile.replace(".zip", "-int.zip");
    }
    
}
