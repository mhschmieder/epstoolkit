# EpsGraphics2D
The EpsGraphics2D library is an open source project for Java 8 that redirects screen output to a single page in EPS format using class inheritance from AWT's Graphics2D canvas-based "paintComponent()" screen refresh renderer.

This library is more or less a fork of the original EpsGraphics2D library from James Paul James Mutton (2004), but is a significant enhancement of the final GPL-licensed free version (v1.9) of that library (sometimes incorrectly referred to as version 0.9, due to inconsistencies within the source code). This version makes use of quite a few of the newer Java language features up through Java 8, for safer I/O handling and better decoupling of DSC vs. AWT vs. EPS concepts, amongst other things. A Java 14 module version is forthcoming, and possibly a Kotlin translation as well.

There are two other forks from the original epsgraphics v1.9, but they barely differ at all from the original, except for some minor I/O style differences and code formatting preferences and the like. This version however, is a significant rewrite and also extends the functionality beyond that of the original library. As a derivative product, it is bound by the same GPL licensing clauses.

There is a very different focus to this library than is found in the original version (not to mention that it's a complete rewrite from scratch, but it owes attribution solely for the majority of EPS mappings having been worked out by the original author). The original EpsGraphics2D stores documents in memory by default. This version deprecates that approach and will eventually only support direct writing of the EPS output, as it is focused more on exporting vectorzed screenshots of GUI components and subcomponents than on "composing" an EPS document via report generation, wizards, etc.

This version of EpsGraphics2D has scientific applications in mind, where there may be charts with millions of data points (as for a typical IFFT). It is beneficial to write directly to disk, which means the bounding box of the document must be known before the drawing begins. As the DSC header requires a fixed bounding box, one otherwise has to store everything in memory (which is expensive) until the final bounding box is known, as the DSC header can only be safely written as the first output, even though some EPS readers may be capable of dealing with the DSC header being at some random location in the EPS output file.

Use Version 2.0 if you need a simple JAR swap with the old version (or even with the jlibeps fork which is almost identical to the original, or the also similar fork for epsgraphics which differs a bit in its I/O handling), without having to change your existing code or class derivations (other than for changing the package provider of the main EpsGraphics2D and EpsDocument classes).

Use Version 2.5 is you'd prefer a more spartan API that removes the clutter of the old-style (and sometimes dangerous) I/O handling in favor of the modern preference towards try-with-resources. This version removes all deprecated legacy functions and constructors, which also frees us up to further improve the code architecture and library structure.

Use Version 3.0 if you need a module-ready version for Java 9 and beyond. This version will ve targeted for Java 14.

A Kotlin port is planned, if it ends up being performant. Kotlin can work with AWT; I am not yet sure whether Scala can, but will also provide a Scala API if so.
