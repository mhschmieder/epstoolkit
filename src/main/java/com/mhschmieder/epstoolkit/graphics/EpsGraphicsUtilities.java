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
package com.mhschmieder.epstoolkit.graphics;

import java.awt.BasicStroke;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import com.mhschmieder.graphicstoolkit.graphics.DrawMode;

/**
 * {@code EpsGraphicsUtilities} is a utility class for EPS-specific AWT based
 * graphics methods, usable in either the AWT or Swing GUI toolkits.
 *
 * @version 1.0
 *
 * @author Mark Schmieder
 */
public final class EpsGraphicsUtilities {

    /**
     * This method serves merely as a sanity check that the Maven integration
     * and builds work properly and also behave correctly inside Eclipse IDE. It
     * will likely get removed once I gain more confidence that I have solved
     * the well-known issues with Maven inside Eclipse as I move on to more
     * complex projects with dependencies (this project is quite simple and has
     * no dependencies at this time, until more functionality is added).
     *
     * @param args
     *            The command-line arguments for executing this class as the
     *            main entry point for an application
     *
     * @since 1.0
     */
    public static void main( final String[] args ) {
        System.out.println( "Hello Maven from EpsToolkit!" ); //$NON-NLS-1$
    }

    /**
     * The default constructor is disabled, as this is a static utilities class.
     */
    private EpsGraphicsUtilities() {}

    /**
     * Returns a point (as two coordinates), after it has been transformed by
     * the supplied matrix for scaling and translation.
     * <p>
     * The logic in this method is specific to transforming between y-up and
     * y-down coordinate system bases, as this is necessary for mapping screen
     * coordinates to paper when following the EPS "y-up" specifications.
     *
     * @param matrix
     *            The Affine Transform to apply to the supplied point
     * @param x
     *            The x-coordinate of the point to transform to the page layout
     * @param y
     *            The y-coordinate of the point to transform to the page layout
     * @return The transformed point, or the original point if the
     *         {@link AffineTransform} is {@code null}
     *
     * @since 1.0
     */
    public static Point2D getEpsTransformedPoint( final AffineTransform matrix,
                                                  final float x,
                                                  final float y ) {
        Point2D result = new Point2D.Float( x, y );

        // It is safer to return the point untransformed than to return null.
        if ( matrix == null ) {
            return result;
        }

        // Apply the supplied scale factor, translation factor, etc.
        result = matrix.transform( result, result );
        final float x1 = ( float ) result.getX();
        final float y1 = ( float ) result.getY();

        // All y-coordinates must be flipped from y-down to y-up for EPS.
        result.setLocation( x1, -y1 );

        return result;
    }

    /**
     * Returns the lower-case EPS operator corresponding to the {@link DrawMode}
     * candidate, after adjusting for EPS stroke restrictions.
     * <p>
     * As Encapsulated PostScript can't handle much in the way of advanced
     * stroke attributes, any client class that isn't derived from the AWT
     * {@link BasicStroke} class isn't possible to support, so must be converted
     * to a "thin" outline-fill using the EPS "fill" operator vs. "stroke".
     *
     * @param drawModeCandidate
     *            The {@link DrawMode} candidate (fill, stroke, fill) to
     *            evaluate alongside the stroke to determine the EPS draw mode
     * @param stroke
     *            The {@link Stroke} to evaluate for whether to force the Draw
     *            Mode to "fill" (such as when it is not a {@link BasicStroke}
     * @return The potentially adjusted draw mode as represented in EPS operator
     *         form as a lower-case string
     */
    public static String getEpsDrawMode( final DrawMode drawModeCandidate, final Stroke stroke ) {
        // Switch to Fill Mode if the stroke is a custom class that isn't
        // derived from the BasicStroke class, unless we are in Clip Mode.
        final String epsDrawMode = DrawMode.STROKE.equals( drawModeCandidate )
                && !( stroke instanceof BasicStroke )
                    ? DrawMode.FILL.toCanonicalString()
                    : drawModeCandidate.toCanonicalString();

        return epsDrawMode;
    }

}
