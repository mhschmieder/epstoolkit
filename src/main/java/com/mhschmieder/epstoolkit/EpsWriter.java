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
 * FxConverterToolkit Library. If not, see
 * <https://opensource.org/licenses/MIT>.
 *
 * Project: https://github.com/mhschmieder/epstoolkit
 */
package com.mhschmieder.epstoolkit;

/**
 * {@code EpsWriter} is an interface that establishes the basic contract for
 * writing lines to an EPS Document.
 * <p>
 * The purpose of providing this interface, is to allow client applications and
 * third-party libraries to provide alternate implementations of the basic
 * methodology of export for EPS, if they find the {@link StringBuilder} based
 * implementations in this library for EpsGraphics2D and/or EpsDocument to be
 * too restrictive for their purposes. This allows everyone to take advantage of
 * the proven EPS operator handling in this library, which is more adherent to
 * specifications than other EPS libraries currently available.
 *
 * @version 1.0
 *
 * @author Mark Schmieder
 */
public interface EpsWriter {

    /**
     * Appends a line to the EPS Document.
     *
     * @param contentLine
     *            The new content line to write to the EPS Document
     *
     * @since 1.0
     */
    void append( final String contentLine );

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
    String getEpsDocument( final String title,
                           final String creator,
                           final float pageWidth,
                           final float pageHeight,
                           final float minX,
                           final float minY,
                           final float maxX,
                           final float maxY );

}
