# EpsToolkit
The EpsToolkit library is an open source project for Java 8 that redirects screen output to a single page in EPS format using class inheritance from AWT's Graphics2D canvas-based "paintComponent()" screen refresh renderer.

There are many different purposes that people have in exporting vector graphics from a GUI application. In some cases, they may want screenshots that are vectorized vs. rasterized, so that they scale better. Sometimes the EPS might be part of report generation. The focus of this particular library is more on the specific needs of scientific visualization applications, which can have huge data sets that involve many geometric entities and bitmap images being written to the EPS file.

Many will ask, why bother with EPS vs. more modern formats? Though it is true that EPS can be more limiting, only supporting a single page of output and not including support for color gradients along with other missing features, there are still some workflows and applications that are able to make better use of EPS as the initial input format for editing and compositing, than working with SVG and/or PDF. This may change over time, and EPS is long deprecated as an embedded format inside PDF documents, but in my own industry experience there was far more demand for EPS than any other format due to the specific tools and workflows involved.

This is a brand new library that takes a significantly different approach from previous solutions. The core of the library is the EPS Operator utility, which encapsulate and decouple the details of the EPS format from the specifics of AWT's Graphics2D structure. The EpsDocument class drives the client code interaction.

This library makes use of quite a few of the newer Java language features up through Java 8, for safer I/O handling and better decoupling of DSC vs. AWT vs. EPS concepts, amongst other things (including defensive programming, and code coverage techniques). A Java 14 (or Java 15, depending on the release timeframe) module-based version is forthcoming, and possibly a Kotlin translation as well.

The StringBuilder class is the primary target for writing EPS, with a final extraction of a String to write to disc once done (or to a servlet stream if that is the ultimate target). This is the most reliable way of incrementally writing and compositing files of unknown size, compared to some of the older techniques, and is able to handle the unique demands of large data sets that might contain 30 million or more data points. It effectively eliminates the need for the programmer to deal with buffering and other issues during the content generation phase of document production, it takes the file operations off the GUI thread, and eliminates the risk of I/O Exceptions during EPS content generation, as well as deferring file write operations until the EPS generation is 100% complete.

Unlike some of the older EPS libraries, this toolkit does not support multiple instances of the core EpsGraphics2D class pointing to the same EpsDocument. I have yet to see a real-world need for this feature; I instead provide sample code (in the ConverterToolkit Library, so that additional dependency libraries are not required for the EpsToolkit itself), for how to composite multiple AWT/Swing screen layout regions into a single set of vectorizations that have the same target.

The original goal a few years ago was to try to hybridize all existing forks of the original free EpsGraphics2D Version 0.9.0 from James Paul James Mutton (2004), but there are so many forks and they all have incompatible I/O models that are too hard to reconcile safely, as well as being based on a very old version of Java that makes for less reliability when dealing with huge data sets (in time series charts, for example), so that project was abandoned and would have helped no one.

Nevertheless, I have a "parked" version of some of that mothballed work from 2019, if any of those developers wish to borrow some hints for incrementally improving their own libraries. The best-known forks are probably EPS Graphics version 1.4 from Thomas Abeel (2009), and jlibeps version 0.1 from Arnaud Blouin (2007), later revised to version 1.5.0 (2017) after merging some older work that I did for my previous employer. Those forks differ only slightly from James Paul James Mutton's original library, except for some differences in I/O handling along with a few Java 1.4 and 1.5 features (such as enums and switch statements); whereas this new library is a complete ground-up re-think and a fresh code base that more closely follows the style of newer vector graphics export libraries such as jFreePDF.

A strong effort was made to minimize the differences between using EPS vs. SVG vs. PDF in a GUI application's export functions. Specifically, much time was put into trying to remove the y-axis flips in the EPS handling code, and to have instead a negative y-axis scale factor that is applied to all graphics in the file. The goal was to remove the PostScript Translate and Scale operators from the beginning of each EPS file and to treat the screen-to-page transform the same as in SVG and PDF libraries, where the transform is set on the Graphic2D implementation class for that library and then applied to each graphics operation. The problem is that we can only get this to work for vector graphics and bitmap images; if Accurate Text Mode is turned off, all font-rendered text displays upside down, with no recourse.

Although many people prefer to have zero dependencies in projects that they adopt, when I do the modularized versions for Java 14 it will be possible to decouple the parts that aren't needed for every project, such as if you are doing server-based EPS handling. The important thing is to enforce as much decoupling as possible; I have been preparing for Java modularization starting with the Java 7 release.

This project, being one of my simplest (at the moment), represents some steep simultaneous learning curves on my own (for GitHub, but mostly for Maven and integration of the two with Eclipse IDE), and hopefully will be quickly followed by my other libraries once I understand how to specify project dependencies.

The one thing I wasn't certain about was whether to post Eclipse and NetBeans specific files, but as they are generic and are agnostic to the OS or the user's system details and file system structure, it seems helpful to post them in order to accelerate the integration of this library into a user's normal IDE project workflow and build cycle.

The Javadocs are 100% compliant and complete, but I am still learning how to publish those at the hosting site that I think is part of Maven Central, as it is a bad idea to bloat a GitHub project with such files and to complicate repository changes (just as with binary files and archices). Hopefully later tonight!

As a confidence boost at both ends, EpsGraphicsUtilities has a main() function that prints "Hello Maven from EpsToolkit" to the console (e.g. the one in Eclipse IDE). By running Maven's clean task, then the install task, you can quickly gain confidence that everything is integrated properly, by then running the main class and seeing the console and confirming that this library was the source of the validation message.

This projects depends (lightly) on my GraphicsToolkit, so I need to do some additional testing to make sure that dependency is properly set in the Maven and GitHub support files and not just in the Eclipse files.
