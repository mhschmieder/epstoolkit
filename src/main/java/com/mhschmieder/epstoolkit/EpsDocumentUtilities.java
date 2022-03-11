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

import com.mhschmieder.epstoolkit.dsc.EpsDscUtilities;
import com.mhschmieder.epstoolkit.operators.EpsPageOperators;
import com.mhschmieder.graphicstoolkit.graphics.GraphicsUtilities;

/**
 * {@code EpsDocumentUtilities} is a utility class for methods related to EPS
 * Document handling; particularly the details of starting and finishing an EPS
 * Document according to the formal specifications from Adobe Systems.
 * <p>
 * Numbers that are written directly to PostScript must be single-precision
 * floating-point, to be compatible with the largest number of EPS clients, as
 * the EPS format generally requires single-precision precision and integers.
 *
 * @version 1.0
 *
 * @author Mark Schmieder
 */
public final class EpsDocumentUtilities {

    /**
     * The default constructor is disabled, as this is a static utilities class.
     */
    private EpsDocumentUtilities() {}

    /**
     * Starts the EPS Document by writing its DSC-compliant EPS Header, and then
     * starting the current page content. We must make sure the header is at the
     * top of the EPS Document, as not all EPS clients are reliable otherwise.
     * <p>
     * The min/max pairs are unit-agnostic, but the units must be consistent.
     * They are used to scale the graphics operations to avoid clipping, and are
     * specified from the point of view of page-addressing, with the minimum
     * coordinates at top of page and the maximum coordinates at bottom of page.
     *
     * @param stringBuilder
     *            The {@link StringBuilder} for appending the EPS content
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
     * @since 1.0
     */
    public static void startDocument( final StringBuilder stringBuilder,
                                      final String title,
                                      final String creator,
                                      final float pageWidth,
                                      final float pageHeight,
                                      final float minX,
                                      final float minY,
                                      final float maxX,
                                      final float maxY ) {
        // Always write a DSC-compliant header when starting an EPS Document.
        //
        // Margins are usually just for text-only output vs. graphics, so we
        // don't need to add the page margins to the x and y min/max pairs.
        //
        // As this wrapper is for vectorizing screen layouts to page layouts vs.
        // more generic tasks like embedding an image in an existing document,
        // it is safest to start at the origin (that is, the lower left corner
        // of the page) and provide the full page width and height in points
        // (1/72 inch) as the EPS Bounding Box, to avoid surprises with negative
        // offsets or clipping.
        //
        // The original author's assumption that the DSC Bounding Box avoids
        // clipping and ensures proper margins, is entirely incorrect.
        //
        // DSC headers only support integers, so we round up the bounds.
        final int boundingBoxMinX = 0;
        final int boundingBoxMinY = 0;
        final int boundingBoxMaxX = ( int ) Math.ceil( pageWidth );
        final int boundingBoxMaxY = ( int ) Math.ceil( pageHeight );
        EpsDscUtilities.writeDscHeader( stringBuilder,
                                        title,
                                        creator,
                                        boundingBoxMinX,
                                        boundingBoxMinY,
                                        boundingBoxMaxX,
                                        boundingBoxMaxY );

        // Calculate the source dimensions from the original mix/max values.
        final float sourceWidth = Math.abs( maxX - minX );
        final float sourceHeight = Math.abs( maxY - minY );

        // Calculate the scale factor of the source layout to the target page,
        // in such a way that neither dimension is clipped nor distorted. This
        // is fairly specific to the needs of Adobe Distiller and/or importing
        // into Adobe Illustrator, but the resulting file can easily be rescaled
        // without loss of resolution.
        final float scaleFactor = GraphicsUtilities
                .calculateSourceToDestinationScaleFactor( sourceWidth,
                                                          sourceHeight,
                                                          pageWidth,
                                                          pageHeight );

        // Start the current page's content flow, adjusting (if necessary) to
        // avoid negative offsets that will be clipped.
        //
        // The full page bounds are used for the translation factors, as this
        // reduces clipping issues and effectively puts all source-to-page
        // mapping responsibilities on the scaling algorithm.
        //
        // Note that any non-zero minimum (x, y) for the bounds has to be offset
        // in translation, so that the graphics output starts at zero and does
        // not go beyond page width or page height. For instance, if minX = 65,
        // we subtract 65 from all coordinates for page output purposes; whereas
        // if minX = -65, we effectively add 65 to all coordinates during page
        // output. As we effectively flip the y-axis to go from bottom of page
        // to top of page vs. the top-to-bottom coordinate system of Java2D, we
        // add vs. subtract the minY offset.
        //
        // The EPS specifications require the global translate operator to come
        // before the global scale operator, so the translation factor's offsets
        // are specified in page units (that is, pre-scaled).
        final float upperLeftX = -( minX * scaleFactor );
        final float upperLeftY = pageHeight + ( minY * scaleFactor );
        EpsPageOperators.epsStartPage( stringBuilder, upperLeftX, upperLeftY, scaleFactor );
    }

    /**
     * Finishes the EPS Document by showing the current page, and then writing
     * the DSC-compliant EPS Footer.
     *
     * @param stringBuilder
     *            The {@link StringBuilder} for appending the EPS content
     *
     * @since 1.0
     */
    public static void finishDocument( final StringBuilder stringBuilder ) {
        // End the current page's content flow.
        EpsPageOperators.epsEndPage( stringBuilder );

        // Write the DSC-compliant EPS footer (i.e., with Adobe PostScript
        // Document Structuring Comments); this is mostly just an "EOF" Comment,
        // as the EPS format supports single-page output only.
        EpsDscUtilities.writeDscFooter( stringBuilder );
    }

}
