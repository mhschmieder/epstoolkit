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
package com.mhschmieder.epstoolkit.operators;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;

import org.apache.commons.math3.util.FastMath;

import com.mhschmieder.epstoolkit.EpsWriter;
import com.mhschmieder.graphicstoolkit.color.ColorConstants;
import com.mhschmieder.graphicstoolkit.color.ColorMode;
import com.mhschmieder.graphicstoolkit.color.ColorUtilities;

/**
 * {@code EpsGraphicsOperators} is a wrapper class for EPS graphics operators,
 * to decouple that code as much as possible from the AWT-specific code in {code
 * EpsGraphics2D}. This also allows the correct EPS implementation code to be
 * used by other implementations of the {@code Graphics2D} part of the library
 * architecture, if a client has needs we didn't anticipate, or to be used for
 * direct-write operations outside the context of AWT and Swing repaint loops.
 *
 * @version 1.0
 *
 * @author Mark Schmieder
 */
public final class EpsGraphicsOperators {

    /**
     * The default constructor is disabled, as this is a static utilities class.
     */
    private EpsGraphicsOperators() {}

    ///////////////// EPS graphics operator writer methods ///////////////////

    /**
     * Saves the current Graphics State.
     * <p>
     * This method is a wrapper for the PostScript "gsave" operator, to avoid
     * copy/paste errors in duplicative code.
     *
     * @param epsWriter
     *            The {@link EpsWriter} for writing the EPS content
     *
     * @since 1.0
     */
    @SuppressWarnings("nls")
    public static void epsGsave( final EpsWriter epsWriter ) {
        epsWriter.append( "gsave" );
    }

    /**
     * Restores the previous Graphics State.
     * <p>
     * This method is a wrapper for the PostScript "grestore" operator, to avoid
     * copy/paste errors in duplicative code.
     *
     * @param epsWriter
     *            The {@link EpsWriter} for writing the EPS content
     *
     * @since 1.0
     */
    @SuppressWarnings("nls")
    public static void epsGrestore( final EpsWriter epsWriter ) {
        epsWriter.append( "grestore" );
    }

    /**
     * Sets grayscale as the current color space. This includes bitmap mode.
     * <p>
     * This method is a wrapper for the PostScript "setgray" operator, to avoid
     * copy/paste errors in duplicative code.
     *
     * @param epsWriter
     *            The {@link EpsWriter} for writing the EPS content
     * @param grayValue
     *            The Gray value, from 0.0 to 1.0
     *
     * @since 1.0
     */
    @SuppressWarnings("nls")
    public static void epsSetgray( final EpsWriter epsWriter, final float grayValue ) {
        final StringBuilder gray = new StringBuilder();
        gray.append( grayValue );
        gray.append( " setgray" );
        epsWriter.append( gray.toString() );
    }

    /**
     * Sets RGB as the current color space.
     * <p>
     * This method is a wrapper for the PostScript "setrgbcolor" operator, to
     * avoid copy/paste errors in duplicative code.
     *
     * @param epsWriter
     *            The {@link EpsWriter} for writing the EPS content
     * @param rgbRed
     *            The Red component of RGB, from 0.0 to 1.0
     * @param rgbGreen
     *            The Green component of RGB, from 0.0 to 1.0
     * @param rgbBlue
     *            The Blue component of RGB, from 0.0 to 1.0
     *
     * @since 1.0
     */
    @SuppressWarnings("nls")
    public static void epsSetrgbcolor( final EpsWriter epsWriter,
                                       final float rgbRed,
                                       final float rgbGreen,
                                       final float rgbBlue ) {
        final StringBuilder rgbColor = new StringBuilder();
        rgbColor.append( rgbRed );
        rgbColor.append( " " );
        rgbColor.append( rgbGreen );
        rgbColor.append( " " );
        rgbColor.append( rgbBlue );
        rgbColor.append( " setrgbcolor" );
        epsWriter.append( rgbColor.toString() );
    }

    /**
     * Sets HSB as a specialized variation of RGB as the current color space.
     * <p>
     * This method is a wrapper for the PostScript "sethsbcolor" operator, to
     * avoid copy/paste errors in duplicative code.
     * <p>
     * This method is not currently used, as there is no point in going back
     * and forth between HSB and RGB (any HSB color is converted by PostScript
     * to RGB). If an application chooses to work natively in HSB vs. AWT's RGB
     * basis, our EpsGraphics2D class can be extended to add a setHsbColor
     * method that then invokes this method before stroke and fill actions.
     *
     * @param epsWriter
     *            The {@link EpsWriter} for writing the EPS content
     * @param hsbHue
     *            The Hue component of HSB, from 0.0 to 1.0
     * @param hsbSaturation
     *            The Saturation component of HSB, from 0.0 to 1.0
     * @param hsbBrightness
     *            The Brightness component of HSB, from 0.0 to 1.0
     *
     * @since 1.0
     */
    @SuppressWarnings("nls")
    public static void epsSethsbcolor( final EpsWriter epsWriter,
                                       final float hsbHue,
                                       final float hsbSaturation,
                                       final float hsbBrightness ) {
        final StringBuilder hsbColor = new StringBuilder();
        hsbColor.append( hsbHue );
        hsbColor.append( " " );
        hsbColor.append( hsbSaturation );
        hsbColor.append( " " );
        hsbColor.append( hsbBrightness );
        hsbColor.append( " sethsbcolor" );
        epsWriter.append( hsbColor.toString() );
    }

    /**
     * Sets CMYK as the current color space.
     * <p>
     * This method is a wrapper for the PostScript "setcmykcolor" operator, to
     * avoid copy/paste errors in duplicative code.
     *
     * @param epsWriter
     *            The {@link EpsWriter} for writing the EPS content
     * @param cmykCyan
     *            The Cyan component of CMYK, from 0.0 to 1.0
     * @param cmykMagenta
     *            The Magenta component of CMYK, from 0.0 to 1.0
     * @param cmykYellow
     *            The Yellow component of CMYK, from 0.0 to 1.0
     * @param cmykBlack
     *            The Black component of CMYK, from 0.0 to 1.0
     *
     * @since 1.0
     */
    @SuppressWarnings("nls")
    public static void epsSetcmykcolor( final EpsWriter epsWriter,
                                        final float cmykCyan,
                                        final float cmykMagenta,
                                        final float cmykYellow,
                                        final float cmykBlack ) {
        final StringBuilder cmykColor = new StringBuilder();
        cmykColor.append( cmykCyan );
        cmykColor.append( " " );
        cmykColor.append( cmykMagenta );
        cmykColor.append( " " );
        cmykColor.append( cmykYellow );
        cmykColor.append( " " );
        cmykColor.append( cmykBlack );
        cmykColor.append( " setcmykcolor" );
        epsWriter.append( cmykColor.toString() );
    }

    /**
     * Sets the dash pattern to use for the next stroke operators.
     * <p>
     * This method is a wrapper for the PostScript "setdash" operator, to avoid
     * copy/paste errors in duplicative code.
     *
     * @param epsWriter
     *            The {@link EpsWriter} for writing the EPS content
     * @param stroke
     *            The current stroke to set for EPS graphics
     *
     * @since 1.0
     */
    @SuppressWarnings("nls")
    public static void epsSetdash( final EpsWriter epsWriter, final BasicStroke stroke ) {
        final StringBuilder dashes = new StringBuilder();
        dashes.append( "[ " );
        final float[] dashArray = stroke.getDashArray();
        if ( dashArray != null ) {
            for ( final float element : dashArray ) {
                dashes.append( element ).append( " " );
            }
        }
        dashes.append( "]" );
        dashes.append( " 0" );
        dashes.append( " setdash" );
        epsWriter.append( dashes.toString() );
    }

    /**
     * Sets the font to use for the next text string show operators.
     * <p>
     * This method is a wrapper for the PostScript "setfont" operator, to avoid
     * copy/paste errors in duplicative code.
     *
     * @param epsWriter
     *            The {@link EpsWriter} for writing the EPS content
     * @param font
     *            The current font to use for text and strings in EPS
     *
     * @since 1.0
     */
    @SuppressWarnings("nls")
    public static void epsSetfont( final EpsWriter epsWriter, final Font font ) {
        final StringBuilder fontOperator = new StringBuilder();
        fontOperator.append( "/" );
        fontOperator.append( font.getPSName() );
        fontOperator.append( " findfont " );
        fontOperator.append( font.getSize() );
        fontOperator.append( " scalefont" );
        fontOperator.append( " setfont" );
        epsWriter.append( fontOperator.toString() );
    }

    /**
     * Sets a new path to initialize the next stroke, fill or clip operators.
     * <p>
     * This method is a wrapper for the PostScript "newpath" operator, to avoid
     * copy/paste errors in duplicative code.
     *
     * @param epsWriter
     *            The {@link EpsWriter} for writing the EPS content
     *
     * @since 1.0
     */
    @SuppressWarnings("nls")
    public static void epsNewpath( final EpsWriter epsWriter ) {
        epsWriter.append( "newpath" );
    }

    /**
     * Sets the coordinates of the start of a new path using moveto operator.
     * <p>
     * This method is a wrapper for the PostScript "moveto" operator, to avoid
     * copy/paste errors in duplicative code.
     *
     * @param epsWriter
     *            The {@link EpsWriter} for writing the EPS content
     * @param x1
     *            The x coordinate of the point to move to
     * @param y1
     *            The y coordinate of the point to move to
     *
     * @since 1.0
     */
    @SuppressWarnings("nls")
    public static void epsMoveto( final EpsWriter epsWriter, final float x1, final float y1 ) {
        final StringBuilder moveTo = new StringBuilder();
        moveTo.append( x1 );
        moveTo.append( " " );
        moveTo.append( y1 );
        moveTo.append( " moveto" );
        epsWriter.append( moveTo.toString() );
    }

    /**
     * Sets the end point of a line from the previous point, as part of the
     * current path.
     * <p>
     * This method is a wrapper for the PostScript "lineto" operator, to avoid
     * copy/paste errors in duplicative code.
     *
     * @param epsWriter
     *            The {@link EpsWriter} for writing the EPS content
     * @param x1
     *            The x coordinate of the point to draw a line to
     * @param y1
     *            The y coordinate of the point to draw a line to
     *
     * @since 1.0
     */
    @SuppressWarnings("nls")
    public static void epsLineto( final EpsWriter epsWriter, final float x1, final float y1 ) {
        final StringBuilder lineTo = new StringBuilder();
        lineTo.append( x1 );
        lineTo.append( " " );
        lineTo.append( y1 );
        lineTo.append( " lineto" );
        epsWriter.append( lineTo.toString() );
    }

    /**
     * Sets the control points for the next point in a curve from the previous
     * moveto or the end point of the last line or curve segment.
     * <p>
     * This method is a wrapper for the PostScript "curveto" operator, to avoid
     * copy/paste errors in duplicative code.
     *
     * @param epsWriter
     *            The {@link EpsWriter} for writing the EPS content
     * @param x1
     *            The x coordinate of the first control point for drawing a
     *            cubic curve
     * @param y1
     *            The y coordinate of the first control point for drawing a
     *            cubic curve
     * @param x2
     *            The x coordinate of the second control point for drawing a
     *            cubic curve
     * @param y2
     *            The y coordinate of the second control point for drawing a
     *            cubic curve
     * @param x3
     *            The x coordinate of the third control point for drawing a
     *            cubic curve
     * @param y3
     *            The y coordinate of the third control point for drawing a
     *            cubic curve
     *
     * @since 1.0
     */
    @SuppressWarnings("nls")
    public static void epsCurveto( final EpsWriter epsWriter,
                                   final float x1,
                                   final float y1,
                                   final float x2,
                                   final float y2,
                                   final float x3,
                                   final float y3 ) {
        final StringBuilder cubicTo = new StringBuilder();
        cubicTo.append( x1 );
        cubicTo.append( " " );
        cubicTo.append( y1 );
        cubicTo.append( " " );
        cubicTo.append( x2 );
        cubicTo.append( " " );
        cubicTo.append( y2 );
        cubicTo.append( " " );
        cubicTo.append( x3 );
        cubicTo.append( " " );
        cubicTo.append( y3 );
        cubicTo.append( " curveto" );
        epsWriter.append( cubicTo.toString() );
    }

    /**
     * Closes the current path's sequence of operators.
     * <p>
     * This is not the same as setting a path's sequence of line or curve
     * segments to form a closed shape; it simply marks the path as finished.
     * <p>
     * This method is a wrapper for the PostScript "closepath" operator, to
     * avoid copy/paste errors in duplicative code.
     *
     * @param epsWriter
     *            The {@link EpsWriter} for writing the EPS content
     *
     * @since 1.0
     */
    @SuppressWarnings("nls")
    public static void epsClosepath( final EpsWriter epsWriter ) {
        epsWriter.append( "closepath" );
    }

    ///////////////// EPS graphics operator utility methods //////////////////

    /**
     * Sets the current color for stroke or fill, along with its color space.
     * <p>
     * This is a wrapper for all of the operations that must be performed when
     * handling PostScript color attributes; unique handling per Color Space.
     *
     * @param epsWriter
     *            The {@link EpsWriter} for writing the EPS content
     * @param color
     *            The current color to use as the basis for all Color Space
     *            mappings
     * @param colorMode
     *            The current active Color Mode for EPS operators
     *
     * @since 1.0
     */
    public static void epsSetColor( final EpsWriter epsWriter,
                                    final Color color,
                                    final ColorMode colorMode ) {
        switch ( colorMode ) {
        case BITMAP:
            // Convert the 0-255 integer based RGB components of the supplied
            // color to a floating-point bitmap value of either 0.0 or 1.0,
            // which also matches PostScript.
            final float bitmapValue = ColorUtilities.rgbToBitmap( color );

            epsSetgray( epsWriter, bitmapValue );

            break;
        case GRAYSCALE:
            // Convert the 0-255 integer based RGB components of the supplied
            // color to a floating-point gray value from 0.0 to 1.0, which also
            // matches PostScript.
            final float grayValue = ColorUtilities.rgbToGray( color );

            epsSetgray( epsWriter, grayValue );

            break;
        case RGB:
            // Grab the raw AWT color components for R, G, and B (ignore Alpha),
            // using AWT's built-in floating-point RGB color converter, which
            // can take advantage of a conditionally pre-cached internal result
            // of an always-synced conversion from integers.
            final float[] rgb = color.getRGBColorComponents( null );
            final float rgbRed = rgb[ ColorConstants.RGB_RED_INDEX ];
            final float rgbGreen = rgb[ ColorConstants.RGB_GREEN_INDEX ];
            final float rgbBlue = rgb[ ColorConstants.RGB_BLUE_INDEX ];

            // Use the Red, Green, and Blue components of the RGB color as-is,
            // as these values already match the PostScript RGB specifications.
            epsSetrgbcolor( epsWriter, rgbRed, rgbGreen, rgbBlue );

            break;
        case CMYK:
            // Convert the 0-255 integer based RGB components of the supplied
            // color to a floating-point CMYK basis from 0.0 to 1.0, which also
            // matches PostScript. Special-case for Absolute Black and Absolute
            // White, to avoid masking with almost-black and almost-white.
            final float[] cmykValues = ColorUtilities.rgbToCmyk( color );
            final float cmykCyan = cmykValues[ ColorConstants.CMYK_CYAN_INDEX ];
            final float cmykMagenta = cmykValues[ ColorConstants.CMYK_MAGENTA_INDEX ];
            final float cmykYellow = cmykValues[ ColorConstants.CMYK_YELLOW_INDEX ];
            final float cmykBlack = cmykValues[ ColorConstants.CMYK_BLACK_INDEX ];

            epsSetcmykcolor( epsWriter, cmykCyan, cmykMagenta, cmykYellow, cmykBlack );

            break;
        default:
            break;
        }
    }

    /**
     * Sets the stroke details for the next stroke operator.
     * <p>
     * This is a wrapper for all of the operations that must be performed when
     * handling PostScript stroke attributes; split between several operators.
     *
     * @param epsWriter
     *            The {@link EpsWriter} for writing the EPS content
     * @param stroke
     *            The current stroke to set for EPS graphics
     *
     * @since 1.0
     */
    @SuppressWarnings("nls")
    public static void epsSetStroke( final EpsWriter epsWriter, final BasicStroke stroke ) {
        final StringBuilder lineWidthOperator = new StringBuilder();
        lineWidthOperator.append( stroke.getLineWidth() );
        lineWidthOperator.append( " setlinewidth" );
        epsWriter.append( lineWidthOperator.toString() );

        final float miterLimit = FastMath.max( 1f, stroke.getMiterLimit() );
        final StringBuilder miterLimitOperator = new StringBuilder();
        miterLimitOperator.append( miterLimit );
        miterLimitOperator.append( " setmiterlimit" );
        epsWriter.append( miterLimitOperator.toString() );

        final StringBuilder lineJoinOperator = new StringBuilder();
        lineJoinOperator.append( stroke.getLineJoin() );
        lineJoinOperator.append( " setlinejoin" );
        epsWriter.append( lineJoinOperator.toString() );

        final StringBuilder lineCapOperator = new StringBuilder();
        lineCapOperator.append( stroke.getEndCap() );
        lineCapOperator.append( " setlinecap" );
        epsWriter.append( lineCapOperator.toString() );

        // Now that the stroke attributes are set, write the dash pattern.
        epsSetdash( epsWriter, stroke );
    }

    /**
     * Draws the supplied {@link Shape}, using one of the supported draw modes
     * (stroke, fill, or clip).
     * <p>
     * This is a wrapper for all of the operations that must be performed when
     * handling a basic PostScript draw operator (whether for stroke, fill, or
     * clip as the current Draw Mode).
     * <p>
     * This is a higher-level version of the draw method, closer to the original
     * AWT {@link Shape}, and tasked with getting closer to the EPS syntax.
     * <p>
     * If the current stroke is anything other than {@link BasicStroke}, we
     * treat this as a fill operator instead of a stroke operator, as EPS only
     * supports the equivalent of the AWT {@link BasicStroke} class.
     *
     * @param epsWriter
     *            The {@link EpsWriter} for writing the EPS content
     * @param shape
     *            The {@link Shape} to draw to the EPS Document
     * @param transform
     *            The {@link AffineTransform} to apply to the {link Shape} to
     *            get its associated {@link PathIterator}
     * @param drawModeDescriptor
     *            The Draw Mode in PostScript-compatible terminology and
     *            case-sensitivity (e.g. "fill", "stroke", "clip")
     *
     * @since 1.0
     */
    public static void epsDrawShape( final EpsWriter epsWriter,
                                     final Shape shape,
                                     final AffineTransform transform,
                                     final String drawModeDescriptor ) {
        // Make sure to avoid unpaired Graphics State save/restore operators by
        // exiting this method immediately if there is nothing to draw.
        if ( shape == null ) {
            return;
        }

        // Avoid creeping numeric inaccuracy if it's the identity transform.
        final AffineTransform pathTransform = transform.isIdentity() ? null : transform;

        // Passing in the transform here, effectively does the same thing as
        // transforming the shape in advance, so is more performant overall.
        final PathIterator pathIterator = shape.getPathIterator( pathTransform );

        // Draw the entire path, encapsulated by "newpath"and "closepath".
        epsDrawPath( epsWriter, pathIterator, drawModeDescriptor );
    }

    /**
     * Draws the path contained in the supplied {@link PathIterator}, using one
     * of the supported draw modes (stroke, fill, or clip).
     * <p>
     * This is a wrapper for all of the operations that must be performed when
     * handling a basic PostScript draw operator (whether for stroke, fill, or
     * clip as the current Draw Mode).
     * <p>
     * This is a lower-level version of the draw method, closer to the actual
     * EPS operators, and further removed from the original AWT {@link Shape}.
     *
     * @param epsWriter
     *            The {@link EpsWriter} for writing the EPS content
     * @param pathIterator
     *            A pre-constructed path iterator for the shape to draw, already
     *            transformed for device space
     * @param drawModeDescriptor
     *            The Draw Mode in PostScript-compatible terminology and
     *            case-sensitivity (e.g. "fill", "stroke", "clip")
     *
     * @since 1.0
     */
    public static void epsDrawPath( final EpsWriter epsWriter,
                                    final PathIterator pathIterator,
                                    final String drawModeDescriptor ) {
        final float[] coords = new float[ 6 ];
        float x0 = 0f;
        float y0 = 0f;

        // Drawing paths always requires a "newpath" to initialize the path.
        epsNewpath( epsWriter );

        while ( !pathIterator.isDone() ) {
            final int segmentType = pathIterator.currentSegment( coords );

            // All y-coordinates must be flipped from y-down to y-up for EPS.
            final float x1 = coords[ 0 ];
            final float y1 = -coords[ 1 ];
            final float x2 = coords[ 2 ];
            final float y2 = -coords[ 3 ];
            final float x3 = coords[ 4 ];
            final float y3 = -coords[ 5 ];

            switch ( segmentType ) {
            case PathIterator.SEG_MOVETO:
                epsMoveto( epsWriter, x1, y1 );

                x0 = x1;
                y0 = y1;

                break;
            case PathIterator.SEG_LINETO:
                epsLineto( epsWriter, x1, y1 );

                x0 = x1;
                y0 = y1;

                break;
            case PathIterator.SEG_CUBICTO:
                epsCurveto( epsWriter, x1, y1, x2, y2, x3, y3 );

                x0 = x3;
                y0 = y3;

                break;
            case PathIterator.SEG_QUADTO:
                // PostScript doesn't support quadratic Bézier curves; it only
                // supports cubic Bézier curves, so we need to perform "degree
                // elevation" to convert the quadratic Bézier curve into a cubic
                // Bézier curve. This is the only way to preserve the original
                // curve's shape accurately for all arbitrary cases.
                //
                // https://pages.mtu.edu/~shene/COURSES/cs3621/NOTES/spline/Bezier/bezier-elev.html
                //
                // https://en.wikipedia.org/wiki/Bézier_curve#Degree_elevation
                //
                // A quadratic curve has degree 2; a cubic curve has degree 3.
                //
                // The code is simplified to improve performance and accuracy
                final float x1cubic = ( x0 + ( 2f * x1 ) ) / 3f;
                final float y1cubic = ( y0 + ( 2f * y1 ) ) / 3f;
                final float x2cubic = ( ( 2f * x1 ) + x2 ) / 3f;
                final float y2cubic = ( ( 2f * y1 ) + y2 ) / 3f;
                final float x3cubic = x2;
                final float y3cubic = y2;

                epsCurveto( epsWriter, x1cubic, y1cubic, x2cubic, y2cubic, x3cubic, y3cubic );

                x0 = x3cubic;
                y0 = y3cubic;

                break;
            case PathIterator.SEG_CLOSE:
                epsClosepath( epsWriter );

                break;
            default:
                break;
            }

            pathIterator.next();
        }

        // Now that the Path is drawn, PostScript needs to know the Draw Mode.
        epsWriter.append( drawModeDescriptor );
    }

    /**
     * Draws the text encapsulated by an {@link AttributedCharacterIterator}.
     * <p>
     * This is a wrapper for all of the operations that must be performed when
     * handling a basic PostScript "show" operator for attributed text.
     *
     * @param epsWriter
     *            The {@link EpsWriter} for writing the EPS content
     * @param attributedCharacterIterator
     *            The {@link AttributedCharacterIterator} that loops through the
     *            character string, with all attributes already in-place and
     *            applied
     * @param x
     *            The x-coordinate where the iterator's text is to be written
     * @param y
     *            The y-coordinate where the iterator's text is to be written
     *
     * @since 1.0
     */
    @SuppressWarnings("nls")
    public static void epsDrawText( final EpsWriter epsWriter,
                                    final AttributedCharacterIterator attributedCharacterIterator,
                                    final float x,
                                    final float y ) {
        // Drawing text always requires a "newpath" to initialize the path.
        epsNewpath( epsWriter );

        // Move to the insert point for the text we are about to iterate.
        epsMoveto( epsWriter, x, y );

        final StringBuilder attributedCharacterBuilder = new StringBuilder();
        attributedCharacterBuilder.append( "(" );
        for ( char ch = attributedCharacterIterator
                .first(); ch != CharacterIterator.DONE; ch = attributedCharacterIterator.next() ) {
            if ( ( ch == '(' ) || ( ch == ')' ) ) {
                attributedCharacterBuilder.append( '\\' );
            }

            attributedCharacterBuilder.append( ch );
        }
        attributedCharacterBuilder.append( ")" );
        attributedCharacterBuilder.append( " show" );
        epsWriter.append( attributedCharacterBuilder.toString() );
    }

}
