Gene Stacker Changes
====================

Version 1.9 (28 August 2015)
----------------------------

 - Various bugfixes.

Version 1.8 (21 January 2015)
-----------------------------

 - Fixed wrong overall LPA when targeting several duplicates of plants with a non-zero LPA.

Version 1.7 (06 January 2015)
-------------------------------

 - Default colored output graphs. A new command line option `-nc,--no-color` is provided to produce greyscale graphs. **Note:** it is advised to install the latest version of Graphviz if colored graphs are not rendered correctly.

Version 1.6 (10 September 2014)
------------------------------

 - Crossings and plants are no longer explicitely duplicated. Instead, if a
   crossing needs to be performed multiple times to generate a sufficient amount of seeds,
   or if a plant needs to be grown several times to be able to perform all scheduled crossings,
   this is indicated in the labels of the corresponding nodes in the crossing schedule. Population
   sizes are still computed so that the required number of duplicates of the target plants are
   expected among the offspring.
   
 - Heuristic H3 now allows selfing of genotypes which are homozygous at all target loci, in order
   to reproduce such genotype in the next generation. This exception has been introduced because
   such selfings allow crossing with the respective genotype over multiple generations at low cost.
   This change may result in better schedules when using presets `default`, `faster` or `fastest`
   and the corresponding increase in runtime is negligibly small.
   
 - Heuristic H6 now computes a tighter population size bound. May result in speedups
   when using presets `default`, `faster` or `fastest`.
   
 - Fixed bug in joint population size computation when simultaneously targeting different
   phase-known genotypes with the same observed allelic frequencies.
   
 - Fixed bug in alignment of partial schedules because of which some alignments
   were sometimes not considered.
   
 - Cleaned up Javadoc in source code.

Version 1.5 (17 March 2014)
------------------------------

 - Added and refactored several command line options:
   - Option `-help` prints brief usage information, including an overview of all parameters.
   - Option `-version` outputs the version of the Gene Stacker software.
   - Options `-v,--verbose`, `-vv,--very-verbose` and `-debug` allow finer control
     of the amount of console output.
   - When `-int,--intermediate-output` is enabled, an intermediate ZIP package will be
     created and updated whenever the current Pareto frontier has changed, containing all
     constructed schedules which are currently not dominated by any other (constructed) schedule.
     This option may be useful to obtain intermediate results, but is expected to slow down the
     application.
   
 - Redesigned command line messages:
   - By default, less output is printed than before.
   - More verbosity options to increase amount of output (see above).
   - An explicit alert is printed when the runtime limit has been exceeded.
   
 - Improved parallel extension of partial schedules.

Version 1.4 (06 December 2013)
-------------------------------

 - Population sizes are now computed based on the probability to obtain
   a specific phase-known genotype, instead of the probability of observing
   the corresponding genotype scores (arbitrary phase), resulting in an increased
   population size for genotypes with linkage phase ambiguity. This ensures that
   the precise phase-known genotype is expected among the offspring, even if its
   linkage phase is ambiguous, in which case standard genotyping techniques can
   not easily identify it. An appropriate method will then have to be applied to
   identify the genotype with the desired phase among all candidates with the
   corresponding genotype scores.
   
 - Added new heuristic (H6) which computes a heuristic lower bound on the population
   size of any extension of a given partial schedule, based on the probabilities of
   crossovers that are necessarily still required to obtain the ideotyope. This new
   heuristic is activated in presets `default`, `faster` and `fastest`.  
   
 - Optimized some code to keep you happy.   
   

Version 1.3 (20 November 2013)
-------------------------------

 - Improved pruning criteria. May result in lower runtimes. 
 
 - Cleaned up command line messages (including warnings and errors).
 

Version 1.2 (23 September 2013)
-------------------------------

 - Improved distribution of work among independent threads.
   May result in lower runtimes on multicore machines.
   

Version 1.1 (20 August 2013)
----------------------------

 - Renamed option `-r, --max-risk <r>` to `-lpa, --max-linkage-phase-ambiguity <a>`,
   used to limit the maximum 'linkage phase ambiguity', which was formerly referred
   to as 'risk'.
   

Version 1.0 (13 August 2013)
----------------------------

 - Initial release of the Gene Stacker software.
 
 
 	
