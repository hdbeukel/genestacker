Gene Stacker Changes
====================

Version 1.5 (under development)
------------------------------

_**Note**: v1.5 is currently still under development_

 - Extended command line messages (including an explicit alert if the runtime limit has been exceeded)
 - ...

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
 
 
 	