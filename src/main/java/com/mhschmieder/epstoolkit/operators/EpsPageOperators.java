/**
 * MIT License
 *
 * Copyright (c) 2020 Mark Schmieder
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
package com.mhschmieder.epstoolkit.operators;

/**
 * {@code EpsPageOperators} is a utility class for methods related to EPS page
 * scaling and positioning, starting and ending a page, and writing a content
 * line to the page. It works directly with a {@link StringBuilder} instead of
 * the primary {@code EpsWriter}, as the start and end of the EPS Document often
 * are written using a different {@link StringBuilder} than the one that the
 * {@code EpsWriter} uses to write the main EPS content, due to the order of
 * content handling (the main EPS content is generally gathered ahead of time).
 * <p>
 * Numbers that are written directly to PostScript must be single-precision
 * floating-point, to be compatible with the largest number of EPS clients, as
 * the EPS format generally requires single-precision precision and integers.
 *
 * @version 1.0
 *
 * @author Mark Schmieder
 */
public final class EpsPageOperators {

    /**
     * The default constructor is disabled, as this is a static utilities class.
     */
    private EpsPageOperators() {}

    /**
     * Start the current page's content flow and set the PostScript graphics
     * state encapsulation, including the basic overall transforms.
     * <p>
     * We can't flip the y-axis scale factor, or graphics and text will be
     * inverted, so we instead compensate using max-y as y-axis translation.
     * Mostly, any compensating set of values for the y-axis here and in the
     * implementation code (removing the y-axis flips for images, strings, and
     * paths/shapes), is all for nothing, as fonts still render upside-down in
     * such cases, so we would have to force Vectorized Text Mode in all cases.
     *
     * @param stringBuilder
     *            The {@link StringBuilder} for appending the EPS start page
     * @param upperLeftX
     *            The upper left x-coordinate of the writable area, in EPS
     *            page units (points, 1/72 inch)
     * @param upperLeftY
     *            The upper left y-coordinate of the writable area, in EPS
     *            page units (points, 1/72 inch)
     * @param scaleFactor
     *            The scale factor for going from the source to the page
     *
     * @since 1.0
     */
    @SuppressWarnings("nls")
    public static void epsStartPage( final StringBuilder stringBuilder,
                                     final float upperLeftX,
                                     final float upperLeftY,
                                     final float scaleFactor ) {
        // Set up a global PostScript graphics context to wrap the overall
        // document contents. We do this here, as this is also where we need to
        // set up the global page scaling and translation factors.
        stringBuilder.append( "gsave\n" );

        // Write the scale factor and translation offsets for the overall page.
        //
        // The EPS specifications require the global translate operator to come
        // before the global scale operator, so the translation factor's offsets
        // are specified in page units (that is, pre-scaled).
        stringBuilder.append( Float.toString( upperLeftX ) + " " + Float.toString( upperLeftY )
                + " translate\n" );
        stringBuilder.append( Float.toString( scaleFactor ) + " " + Float.toString( scaleFactor )
                + " scale\n" );
    }

    /**
     * End the current page's content flow and close the PostScript graphics
     * state encapsulation (accounting for clipping, when present).
     *
     * @param stringBuilder
     *            The {@link StringBuilder} for appending the EPS end page
     *
     * @since 1.0
     */
    @SuppressWarnings("nls")
    public static void epsEndPage( final StringBuilder stringBuilder ) {
        // Close the PostScript encapsulation of the main content before writing
        // the EPS footer and "EOF", using the PostScript "grestore" command.
        stringBuilder.append( "grestore\n" );

        // Each PostScript page is ended using exactly one "showpage" command.
        stringBuilder.append( "showpage\n" );
    }

}
