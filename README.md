# EpsGraphics2D
Library for redirecting screen output to a single page in EPS format using inheritance from AWT's Graphics2D.

This is a significant enhancement of the old epsgraphics2d freeware library written by James Paul James Mutton in 2004 and forked from the GPL-licensed version 1.9 (sometimes incorrect referred to as version 0.9, due to inconsistencies within the source code). It makes use of newer Java language features up through Java 8, for safer I/O handling and better decoupling of DSC vs. AWT vs. EPS concepts. A Java 14 module version is forthcoming, and possibly a Kotlin translation as well.

There are two other forks from the original epsgraphics v1.9, but they barely differ at all from the original, except for some minor I/O style differences and code formatting preferences and the like. This version however, is a significant rewrite and also extends the functionality beyond that of the original library. As a derivative product, it is bound by the same GPL licensing clauses.

Use Version 2.0 if you need a simple JAR swap without having to change your existing code or class derivations (other than for the package provider of the main EpsGraphics2D and EpsDocument classes).

Use Version 2.5 is you'd prefer a more spartan API that removes the clutter of the old-style (and sometimes dangerous) I/O handling in favor of the modern preference towards try-with-resources. This version removes all deprecated legacy functions and constructors, which also frees us up to further improve the code architecture and library structure.

Use Version 3.0 if you need a module-ready version for Java 9 and beyond. This version will ve targeted for Java 14.

A Kotlin port is planned, if it ends up being performant. Kotlin can work with AWT; I am not yet sure whether Scala can, but will also provide a Scala API if so.
