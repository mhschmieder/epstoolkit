# EpsToolkit
The EpsToolkit library is an open source project for Java 8 that redirects screen output to a single page in EPS format using class inheritance from AWT's Graphics2D canvas-based "paintComponent()" screen refresh renderer.

This is a brand new library that takes a different approach from previous ones, but some of the low level coding details will look quite similar to existing solutions as many of the core graphics functions of AWT's Graphics2D class are quite elementary and limited in scope.

Nevertheless, the architecture of this library is designed to be more flexible towards different usage contexts, while also allowing for partial vs. complete use should someone need to package the core EPS Operator functionality in a very different way. The core of the library is really the EPS Operator utility classes, which encapsulate and decouple the details of the EPS format from the specifics of AWT's Graphics2D structure.

This library makes use of quite a few of the newer Java language features up through Java 8, for safer I/O handling and better decoupling of DSC vs. AWT vs. EPS concepts, amongst other things (including defensive programming, and code coverage techniques). A Java 14 (or Java 15, depending on the release timeframe) module-based version is forthcoming, and possibly a Kotlin translation as well.

The original goal a few years ago was to try to hybridize all existing forks of the original free Version 0.9.0 (sometimes incorrectly referred to as version 1.43 or even version 1.4 or version 1.9, due to inconsistencies within the source code header comments for each class vs. the library version coded as a static constant identifier), of the EpsGraphics2D library from James Paul James Mutton (2004), but there are so many of them and they all have incompatible I/O models that were too hard to reconcile safely, as well as being based on a very old version of Java that makes for less reliability when dealing with huge data sets in charts (for example), so it proved to be a thankless task that probably wouldn't even have helped many people.

Nevertheless, I have a "parked" version of some of that abandoned work, that can be pulled from in order to help improve some of the older libraries, if there is any interest from those developers. The best-known forks are probably EPS Graphics version 1.4 from Thomas Abeel (2009), and jlibeps version 0.1 from Arnaud Blouin (2007), later revised to version 1.5.0 (2017) after merging some work that I did for my previous employer (though not as up-to-date as the code I "parked" last year). Those forks differ only slightly from James Paul James Mutton's original library, except for some minor I/O style differences and code formatting preferences and the like, along with a few Java 1.4 and 1.5 features (such as enums and switch statements).

A strong effort was made to minimize the differences between using EPS and using SVG or PDF in a GUI application's export functions. Specifically, much time was put into trying to remove the y-axis flips in the EPS handling code, and to have instead a negative y-axis scale factor that is applied to all graphics in the file. The goal was to remove the PostScript Translate and Scale operators from the beginning of each EPS file and to treat the screen-topafe transform the same as in SVG and PDF libraries, where the transform is set on the Graphic2D implementation class for that library and then applied to each graphics operation. The problem is that we can only get this to work for vector graphics and bitmap images; if Accurate Text Mode is turned off, all font-rendered text displays upside down, with no recourse.

For this new library, I chose to use the newer StringBuilder class as the primary target for writing EPS, with a final extraction of a String to write to disc once done (or to a servlet stream if that is the ultimate target). This has proven to be the most reliable way of incrementally writing and compositing files compared to some of the older techniques, and is able to handle the unique demands of data sets that might contain 30 million or more data points. It effectively eliminates the need for the programmer to deal with buffering and other issues during the content generation phase of document production. It also takes the file operations off the GUI thread, and eliminates the risk of I/O Exceptions during EPS content generation.

Many other file-based libraries now use this more modern StringBuilder approach; it is becoming an industry standard. It also significantly frees up architectural choices and code organization and structure decisions. Prior to StringBuilder being added in Java 1.5, other String-based approaches to content production were neither as robust nor as scalable to large sets of string append operations. As a result, it was more effective at that time to use buffered file-based I/O.

Unlike some of the older libraries, this toolkit does not support multiple instances of the core EpsGraphics2D class, pointing to the same document. I have yet to see a real-world need for this feature; I instead provide sample code for how to composite multiple screen layout regions into a single set of vectorizations that have the same target. But as there may be use cases that I haven't foreseen, an option for allowing downstream clients more flexibility woould be to let them construct several EpsGraphics2D instances and then composite the extracted content themselves once done. This would involve exposing some private functions.

There are many different purposes that people have in exporting vector graphics from a GUI application. In some cases, they may want screenshots that are vectorized vs. rasterized, so that they scale better. Sometimes the EPS might be part of report generation. The focus of this particular library is more on the specific needs of scientific visualization applications, which can have huge data sets that involve many geometric entities and bitmap images being written to the EPS file.

Many will ask, why bother with EPS vs. more modern formats? Though it is true that EPS can be more limiting, only supporting a single page of output and not including support for color gradients along with other missing features, there are still some workflows and applications that are able to make better use of EPS as the initial input format for editing and compositing, than working with SVG and/or PDF. This may change over time, and EPS is long deprecated as an embedded format inside PDF documents, but in my own industry experience there was far more demand for EPS than any other format due to the specific tools and workflows involved.

There is a slight delay in posting this library, as I decided it is best to extract the non EPS specific stuff into separate libraries called GraphicsToolkit and GuiToolkit (the latter is quite small so far). I also extracted some of the GUI samples into a GraphicsConverter project that also supports PDF and SVG output targets. Due to difference license models for the toolkits that support those formats, it was important to separate the code from EpsToolkit so that I can provide a more liberal license with this library that nevertheless is compatible with downstream use in mnore restricve license models.

Although many people prefer to have zero dependencies in projects they adopt, when I do the modularized versions for Java 14 it will be possible to decouple the parts that aren't needed for every project, such as if you are doing server-based EPS handling. The important thing is to enforce as much decoupling as possible; I have been preparing for Java modularization starting with the Java 7 release.
