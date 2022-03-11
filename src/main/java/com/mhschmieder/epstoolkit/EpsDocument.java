/**
 * MIT License
 *
 * Copyright (c) 2020, 2022 Mark Schmieder
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * This file is part of the EpsToolkit Library
 *
 * You should have received a copy of the MIT License along with the
 * EpsToolkit Library. If not, see <https://opensource.org/licenses/MIT>.
 *
 * Project: https://github.com/mhschmieder/epstoolkit
 */
package com.mhschmieder.epstoolkit;

import com.mhschmieder.epstoolkit.graphics.EpsGraphics2D;

/**
 * {@code EpsDocument} is a class that represents an EPS Document. Its methods
 * adhere to Adobe's Encapsulated PostScript (EPS) File Format Specification:
 * <p>
 * https://www.adobe.com/content/dam/acom/en/devnet/actionscript/articles/5002.EPSF_Spec.pdf
 * <p>
 * EPS is a specialized form of PostScript that complies with the Document
 * Structuring Conventions (DSC) and is intended to be embedded inside another
 * PostScript file. An EPS file must contain a special first line that
 * identifies that it as an EPS file (e.g. %!PS-Adobe-3.0 EPSF-3.0), and it must
 * contain a %%BoundingBox: line. The EPS file only draws within the rectangle
 * defined by the Bounding Box. The PostScript code must avoid using PostScript
 * operators that would interfere with the embedding. These include operators
 * with global effects such as changing the Page Size, and changing the half
 * tone screen. Note that DSC continues to evolve to support PDF, and that only
 * the oldest tags can be assumed to be supported by EPS clients and readers.
 * <p>
 * All numeric values that are to be written as strings to PostScript, must be
 * specified as single-precision. Only numbers that interact directly with AWT
 * Transforms should be double-precision.
 * <p>
 * The {@link EpsDocument} class is the primary way for clients to interact with
 * this library, and is also where one fetches an instance of the specialized
 * {@link EpsGraphics2D} Graphics Context for redirecting normal screen output
 * to an Encapsulated PostScript file.
 * <p>
 * As Encapsulated PostScript files are always limited to just one page, there
 * is no need to separate functionality between this class and what would have
 * been a rather sparse and redundant EpsPage class. The Page is the Document.
 * <p>
 * Example usage:
 *
 * <pre>
 * // Create a new EPS Document.
 * EpsDocument epsDocument = new EpsDocument();
 *
 * // Get the Graphics Context for drawing the EPS content.
 * Graphics2D epsGraphics = epsDocument.getGraphics2D();
 *
 * // Line thickness of 1.5.
 * epsGraphics.setStroke( new BasicStroke( 1.5f ) );
 *
 * // Draw a single line.
 * epsGraphics.drawLine( 5, 5, 15, 5 );
 *
 * // Get the full EPS Document (header, content, footer, etc.).
 * String epsContent = epsDocument.getEpsDocument( title,
 *                                                 creator
 *                                                 pageWidth,
 *                                                 pageHeight,
 *                                                 minX,
 *                                                 minY,
 *                                                 maxX,
 *                                                 maxY );
 * </pre>
 *
 * @version 1.0
 *
 * @author Mark Schmieder
 */
public class EpsDocument implements EpsWriter {

    /**
     * This is a cache of the {@link EpsGraphics2D} instance to use with this
     * {@link EpsDocument} for redirecting normal screen output to EPS output.
     */
    private EpsGraphics2D       graphicsContext;

    /**
     * The {@link StringBuilder} that incrementally accumulates the EPS content.
     */
    private final StringBuilder contentStringBuilder;

    //////////////////////////// Constructors ////////////////////////////////

    /**
     * Fully qualified constructor.
     * <p>
     * This is the only provided constructor; it is preferable for clients to
     * introduce their own defaulting if they don't like what is provided in the
     * associated ConverterToolkit Library for incomplete constructor parameter
     * list specification, rather than provide a variety of partially qualified
     * constructors here.
     * <p>
     * The ConverterToolkit Library is highly recommended to help you quickly
     * make appropriate use of this library, either through direct invocation of
     * its utility methods or as coding examples for your own custom code. The
     * library aims to normalize the differences between several vector graphics
     * output formats, and is decoupled from this library due to having
     * additional resource dependencies that not every downstream client needs.
     * <p>
     * This constructor makes an empty {@link EpsDocument} instance that writes
     * directly to a {@link StringBuilder}. It is premature to write the actual
     * content at this point, as it may need to be constructed algorithmically
     * or otherwise incrementally, with interim page offsets between each layout
     * component or between incremental invocations.
     * <p>
     * Basic parameters for the DXC Header, such as Title, Creator, Bounding
     * Box,
     * Page Width, and Page Height, are deferred until {@link #getEpsDocument}
     * is
     * invoked, so that the client code has complete flexibility in when to
     * query
     * the user for input, as well as how it determines the final bounding box.
     *
     * @since 1.0
     */
    public EpsDocument() {
        // Wait to construct the Graphics Context on-demand, as it is expensive.
        graphicsContext = null;

        // Create a String Builder to use for the content; not the header or
        // footer. This is because the content is written indirectly by the
        // Graphics Context; whereas the header and footer are written by this
        // EPS Document container class.
        contentStringBuilder = new StringBuilder();
    }

    ////////////////// Accessor methods for private data /////////////////////

    /**
     * Returns the {@link EpsGraphics2D} instance for drawing to the page.
     *
     * @return The {@code EpsGraphics2D} instance for drawing to the page.
     *
     * @since 1.0
     */
    public final EpsGraphics2D getGraphics2D() {
        // We lazily construct the Graphics Context as it is expensive to do.
        if ( graphicsContext == null ) {
            graphicsContext = new EpsGraphics2D( this );
        }
        return graphicsContext;
    }

    ////////////////////// EpsWriter method overrides ////////////////////////

    /**
     * Appends a line to the EPS Document.
     * <p>
     * A new line character is added to the end of the line that is appended.
     * <p>
     * This is not done in a platform-specific way via a call to the
     * {@code System.lineSeparator} method, as the downstream consumer of the
     * EPS Document matters more than does the producer of the EPS Document. As
     * files may be sent back and forth between users on different OS's, the
     * burden is on the EPS Viewer application to handle the line separators.
     *
     * @param contentLine
     *            The {@link String} to append to the end of the EPS Document as
     *            a new content line
     *
     * @since 1.0
     */
    @Override
    @SuppressWarnings("nls")
    public void append( final String contentLine ) {
        // Append the content line, along with a new line character, to the end
        // of the content-specific String Builder.
        contentStringBuilder.append( contentLine + "\n" );
    }

    /**
     * Returns the full EPS Document, including header, contents, and footer.
     * <p>
     * This method compiles the full EPS Document by writing its header,
     * followed by the cached pre-converted content, and then the footer.
     * <p>
     * The min/max pairs for the source content are unit-agnostic, but the units
     * must be self-consistent. They are used to scale the graphics operations
     * to avoid clipping, and are specified from the point of view of paper
     * oriented actions such as typing and printing, with the minimum (x, y)
     * coordinates at top of page and the maximum coordinates at bottom of page.
     *
     * @param title
     *            The {@link String} to use as the EPS Document's title
     * @param creator
     *            The {@link String} to use as the EPS Document's creator
     * @param pageWidth
     *            The target page width, in points (1/72 inch)
     * @param pageHeight
     *            The target page height, in points (1/72 inch)
     * @param minX
     *            The x-coordinate of the EPS content top left corner
     *            (unit-agnostic)
     * @param minY
     *            The y-coordinate of the EPS content top left corner
     *            (unit-agnostic)
     * @param maxX
     *            The x-coordinate of the EPS content bottom right corner
     *            (unit-agnostic)
     * @param maxY
     *            The y-coordinate of the EPS content bottom right corner
     *            (unit-agnostic)
     *
     * @return The String that represents the entire EPS Document
     *
     * @since 1.0
     */
    @Override
    public final String getEpsDocument( final String title,
                                        final String creator,
                                        final float pageWidth,
                                        final float pageHeight,
                                        final float minX,
                                        final float minY,
                                        final float maxX,
                                        final float maxY ) {
        // Write the DSC Header and the EPS Start Page and global transforms.
        final StringBuilder documentBuilder = new StringBuilder();
        EpsDocumentUtilities.startDocument( documentBuilder,
                                            title,
                                            creator,
                                            pageWidth,
                                            pageHeight,
                                            minX,
                                            minY,
                                            maxX,
                                            maxY );

        // If we had set a clipping rectangle, and it had its own PostScript
        // encapsulation that needs its graphics context to be taken off the
        // stack via the PostScript "grestore" command, then do so now.
        //
        // We force this behavior by setting a null invalid clip area. This is
        // safer than popping the stack here, in case the user reuses the
        // Graphics Context and thus starts out with incorrect clipping state.
        graphicsContext.setClip( null );

        // Append the main EPS content to the document.
        documentBuilder.append( contentStringBuilder );

        // Write the DSC Footer to finish the content, taking care of any
        // graphics state encapsulation of clipping areas by popping the
        // graphics state if necessary.
        EpsDocumentUtilities.finishDocument( documentBuilder );

        // Get the entire EPS Document as a single String.
        final String document = documentBuilder.toString();

        return document;
    }

}
