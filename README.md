# epsgraphics2d
Library for redirecting screen output to a single page in EPS format using inheritance from AWT's Graphics2D.

This is a significant enhancement of the old epsgraphics2d freeware library written by James Paul James Mutton in 2004 and forked from the GPL-licensed version 1.9 (sometimes incorrect referred to as version 0.9, due to inconsistencies within the source code). It makes use of newer Java language features up through Java 8, for safer I/O handling and better decoupling of DSC vs. AWT vs. EPS concepts. A Java 14 module version is forthcoming, and possibly a Kotlin translation as well.

There are two other forks from the original epsgraphics v1.9, but they barely differ at all from the original, except for one or two minor I/O style differences and code formatting preferences and the like. This version however, is a significant rewrite and also extends the functionality beyond that of the original library. As a derivative product, it is bound by the same GPL licensing clauses.
