Gene Stacker
============

Gene Stacker is a flexible tool for marker-assisted gene pyramiding. It can be used
to construct efficient crossing schedules that gather desired alleles residing in
multiple individuals into a single, specific target genotype (the so-called ideotype).

Given a set of initial (phase-known) genotypes, the desired ideotype and the genetic map
of the crop, Gene Stacker approximates the Pareto frontier of crossing schedules with
minimal number of generations, population size and linkage phase ambiguity, that create
the ideotype starting from the available parental genotypes.

Several heuristics are provided, which yield different tradeoffs between solution quality
and speed and allow to solve more complex gene stacking problems within reasonable time.

Documentation
=============

For documentation and examples of how to use Gene Stacker, see http://genestacker.ugent.be.
Running

```
java -jar genestacker.jar -help
```

prints brief usage information including an overview of all possible parameters.

R Interface
===========

A simple R interface `genestacker.R` is included that can be used to run Gene Stacker from R.
This script depends on the jar file `genestacker.jar` and should be kept in the same directory
as this jar file. Documentation and examples are available at the Gene Stacker website (see above).

Changes
========

An overview of changes in new releases is stated in CHANGES.md.

License and copyright
=====================

Gene Stacker is licensed under the Apache License, Version 2.0, see LICENSE file or
http://www.apache.org/licenses/LICENSE-2.0. Copyright information is included in the
NOTICE file.

Contact
=======

Gene Stacker is developed and maintained by

 - Herman De Beukelaer (Herman.DeBeukelaer@UGent.be)



