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

Releases
========

Stable releases are available for download at the [Gene Stacker website][1]. Changes
in new releases are listed in the [CHANGES][2] file.

Building from source code
=========================

Assuming the availability of [Maven][3], Java and a JDK installation, building Gene Stacker
from source code should be as easy as running

```
mvn package
```

from inside the `Genestacker` root directory

```
|-- Genestacker
  |-- Genestacker-cli
  |-- Genestacker-lib
  |-- pom.xml
  |-- ...
```

This will create a jar package `genestacker.jar`, including all dependencies, in a `bin` subdirectory.

R Interface
===========

A simple R interface `genestacker.R` is included in the root directory

```
|-- Genestacker
  |-- genestacker.R
  |-- ...
```

that can be used to run Gene Stacker from R. This script depends on `genestacker.jar` which should be available
in the same directory or in a `bin` subdirectory, where it is created after running `mvn package`.

Documentation
=============

For documentation and examples of how to use Gene Stacker, see http://genestacker.ugent.be. Running

```
java -jar bin/genestacker.jar -help
```

prints brief usage information including an overview of all possible parameters.

License and copyright
=====================

Gene Stacker is licensed under the Apache License, Version 2.0, see LICENSE file or
http://www.apache.org/licenses/LICENSE-2.0. Copyright information is stated in the NOTICE file.

Contact
=======

Gene Stacker is developed and maintained by

 - Herman De Beukelaer (Herman.DeBeukelaer@UGent.be)




[1]: http://genestacker.ugent.be/downloads.php
[2]: CHANGES.md
[3]: http://maven.apache.org/download.cgi
