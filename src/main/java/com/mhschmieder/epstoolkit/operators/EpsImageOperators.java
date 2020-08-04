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

import java.awt.Color;

import com.mhschmieder.epstoolkit.EpsWriter;
import com.mhschmieder.graphicstoolkit.color.ColorConstants;
import com.mhschmieder.graphicstoolkit.color.ColorMode;
import com.mhschmieder.graphicstoolkit.color.ColorUtilities;

/**
 * {@code EpsImageOperators} is a wrapper class for EPS image operators, and for
 * methods related to EPS image handling, with only minor differences (such as
 * "bind", which seems to be specific to EPS) if this code needs to be adapted
 * to work for standard PostScript or related formats.
 *
 * @version 1.0
 *
 * @author Mark Schmieder
 */
public final class EpsImageOperators {

    /**
     * The default constructor is disabled, as this is a static utilities class.
     */
    private EpsImageOperators() {}

    /////////////////// EPS image operator writer methods ////////////////////

    /**
     * Writes the preamble to the image operator; the parts before the pixels.
     * <p>
     * This is a helper method that combines the writing of the image
     * dimensions and the image transformation matrix to the EPS File, as a
     * combined operation that preserves the necessary write order and also
     * takes care of the image operator preamble.
     * <p>
     * It is still necessary to break apart the overall writing of the image
     * operator, as the legacy code interrupts the parameters with a background
     * fill operation that also includes a "setcolor" operation.
     *
     * @param epsWriter
     *            The {@link EpsWriter} for writing the EPS content
     * @param width
     *            The source image width, in samples
     * @param height
     *            The source image height, in samples
     * @param bitsPerSample
     *            The number of bits per image sample (1, 2, 4, 8, etc.)
     * @param imageMatrix
     *            The user space to image space transformation matrix
     *
     * @since 1.0
     */
    public static void writeEpsImageOperatorPreamble( final EpsWriter epsWriter,
                                                      final int width,
                                                      final int height,
                                                      final int bitsPerSample,
                                                      final double[] imageMatrix ) {
        // Write the dimensions of the source image, in samples.
        writeEpsImageDimensions( epsWriter, width, height, bitsPerSample );

        // Write the user space to image space transformation matrix.
        writeEpsImageMatrix( epsWriter, imageMatrix );
    }

    /**
     * Writes the image dimensions, as part of the image operator preamble.
     * <p>
     * This is a helper method that writes the image dimensions to the EPS
     * File. It must be followed by the image transformation matrix. This
     * method should remain private and final because it has specific order
     * dependencies and thus is of no use to downstream clients or derived
     * classes. It should generally only be accessed by internal code.
     *
     * @param epsWriter
     *            The {@link EpsWriter} for writing the EPS content
     * @param width
     *            The source image width, in samples
     * @param height
     *            The source image height, in samples
     * @param bitsPerSample
     *            The number of bits per image sample (1, 2, 4, 8, etc.)
     *
     * @since 1.0
     */
    @SuppressWarnings("nls")
    private static void writeEpsImageDimensions( final EpsWriter epsWriter,
                                                 final int width,
                                                 final int height,
                                                 final int bitsPerSample ) {
        // Write the dimensions of the source image, in samples.
        final StringBuilder dimensions = new StringBuilder();
        dimensions.append( width );
        dimensions.append( " " );
        dimensions.append( height );
        dimensions.append( " " );
        dimensions.append( bitsPerSample );
        epsWriter.append( dimensions.toString() );
    }

    /**
     * Writes the image matrix, as part of the image operator preamble.
     * <p>
     * This is a helper method that writes the image matrix to the EPS File.
     * It must be followed by the image procedure and the image operator. This
     * method should remain private and final because it has specific order
     * dependencies and thus is of no use to downstream clients or derived
     * classes. It should generally only be accessed by internal code.
     *
     * @param epsWriter
     *            The {@link EpsWriter} for writing the EPS content
     * @param imageMatrix
     *            The user space to image space transformation matrix
     *
     * @since 1.0
     */
    @SuppressWarnings("nls")
    private static void writeEpsImageMatrix( final EpsWriter epsWriter,
                                             final double[] imageMatrix ) {
        // Write the user space to image space transformation matrix.
        final StringBuilder mapping = new StringBuilder();
        mapping.append( " [" );
        mapping.append( imageMatrix[ 0 ] );
        mapping.append( " " );
        mapping.append( imageMatrix[ 1 ] );
        mapping.append( " " );
        mapping.append( imageMatrix[ 2 ] );
        mapping.append( " " );
        mapping.append( imageMatrix[ 3 ] );
        mapping.append( " " );
        mapping.append( imageMatrix[ 4 ] );
        mapping.append( " " );
        mapping.append( imageMatrix[ 5 ] );
        mapping.append( "]" );

        epsWriter.append( mapping.toString() );
    }

    /**
     * Writes the Image Procedure, followed by the actual Image Contents.
     * <p>
     * This is a helper method that combines the writing of the image
     * procedure and the image contents to the EPS File, as a combined operation
     * that preserves the necessary write order and also takes care of the main
     * image operator parameters.
     * <p>
     * It is still necessary to break apart the overall writing of the image
     * operator, as the legacy code interrupts the parameters with a background
     * fill operation that also includes a "setcolor" operation.
     *
     * @param epsWriter
     *            The {@link EpsWriter} for writing the EPS content
     * @param width
     *            The source image width, in samples
     * @param height
     *            The source image height, in samples
     * @param pixels
     *            The pixel array to use for the EPS image
     * @param colorMode
     *            The current active Color Mode for EPS operators
     *
     * @since 1.0
     */
    public static void writeEpsImageOperatorParameters( final EpsWriter epsWriter,
                                                        final int width,
                                                        final int height,
                                                        final int[] pixels,
                                                        final ColorMode colorMode ) {
        // Write the image procedure and command, ahead of the image data.
        EpsImageOperators.writeEpsImageProcedure( epsWriter, width, colorMode );

        // Draw the image contents (namely, the rectangular block of pixels).
        EpsImageOperators.writeEpsImageContents( epsWriter, width, height, pixels, colorMode );
    }

    /**
     * Writes the Image Procedure to the image operator, in EPS format.
     * <p>
     * Note that there are slight differences here, compared to standard
     * PostScript Image Procedure syntax. See the EPS reference manual for
     * details, as linked in various parts of this library's documentation.
     * <p>
     * This is a helper method that sets the image procedure and the image
     * operator, in preparation for then immediately writing the image contents
     * to the EPS File. This method should remain private and final because it
     * has specific order dependencies and thus is of no use to downstream
     * clients or derived classes.
     *
     * @param epsWriter
     *            The {@link EpsWriter} for writing the EPS content
     * @param width
     *            The source image width, in samples
     * @param colorMode
     *            The current active Color Mode for EPS operators
     *
     * @since 1.0
     */
    public static void writeEpsImageProcedure( final EpsWriter epsWriter,
                                               final int width,
                                               final ColorMode colorMode ) {
        // Write the procedure for reading the image data, per Color Mode.
        switch ( colorMode ) {
        case BITMAP:
            // Bitmap images use the PostScript "image" operator.
            //
            // This could alternately use the "colorimage" operator with just
            // one
            // color component specified, but some viewers and editors may not
            // handle such images correctly so it is safer to leave as-is.
            epsImage( epsWriter, width );

            break;
        case GRAYSCALE:
            // Grayscale images use the PostScript "image" operator.
            //
            // This could alternately use the "colorimage" operator with just
            // one
            // color component specified, but some viewers and editors may not
            // handle such images correctly so it is safer to leave as-is.
            epsImage( epsWriter, width );

            break;
        case RGB:
            // RGB images use the PostScript "colorimage" operator.
            epsColorimage( epsWriter, width, 3 );

            break;
        case CMYK:
            // CMYK images use the PostScript "colorimage" operator.
            epsColorimage( epsWriter, width, 4 );

            break;
        default:
            break;
        }
    }

    /**
     * Writes the actual Image Contents, which is a rectangular blob of pixels.
     * <p>
     * This is a helper method that loops on the pixel array in order to write
     * the image contents to the EPS File. It must be preceded by the relevant
     * image operator and other parameters. This method should remain private
     * and final because it has specific order dependencies and thus is of no
     * use to downstream clients or derived classes.
     *
     * @param epsWriter
     *            The {@link EpsWriter} for writing the EPS content
     * @param width
     *            The source image width, in samples
     * @param height
     *            The source image height, in samples
     * @param pixels
     *            The pixel array to use for the EPS image
     * @param colorMode
     *            The current active Color Mode for EPS operators
     *
     * @since 1.0
     */
    public static void writeEpsImageContents( final EpsWriter epsWriter,
                                              final int width,
                                              final int height,
                                              final int[] pixels,
                                              final ColorMode colorMode ) {
        // Loop on the actual image content and write as successive two-digit
        // hexadecimal strings. Insert newlines after every 64 characters.
        StringBuilder contentLine = new StringBuilder();
        for ( int y = 0; y < height; y++ ) {
            for ( int x = 0; x < width; x++ ) {
                final Color color = new Color( pixels[ x + ( width * y ) ] );

                switch ( colorMode ) {
                case BITMAP:
                    final String bitmapHexValue = ColorUtilities.rgbToBitmapHex( color );
                    contentLine.append( bitmapHexValue );

                    break;
                case GRAYSCALE:
                    final String grayHexValue = ColorUtilities.rgbToGrayHex( color );
                    contentLine.append( grayHexValue );

                    break;
                case RGB:
                    final String[] rgbHexValues = ColorUtilities.rgbToRgbHex( color );
                    final String redHexValue = rgbHexValues[ ColorConstants.RGB_RED_INDEX ];
                    final String greenHexValue = rgbHexValues[ ColorConstants.RGB_GREEN_INDEX ];
                    final String blueHexValue = rgbHexValues[ ColorConstants.RGB_BLUE_INDEX ];

                    contentLine.append( redHexValue );
                    contentLine.append( greenHexValue );
                    contentLine.append( blueHexValue );

                    break;
                case CMYK:
                    final String[] cmykHexValues = ColorUtilities.rgbToCmykHex( color );
                    final String cyanHexValue = cmykHexValues[ ColorConstants.CMYK_CYAN_INDEX ];
                    final String magentaHexValue =
                                                 cmykHexValues[ ColorConstants.CMYK_MAGENTA_INDEX ];
                    final String yellowHexValue = cmykHexValues[ ColorConstants.CMYK_YELLOW_INDEX ];
                    final String blackHexValue = cmykHexValues[ ColorConstants.CMYK_BLACK_INDEX ];

                    contentLine.append( cyanHexValue );
                    contentLine.append( magentaHexValue );
                    contentLine.append( yellowHexValue );
                    contentLine.append( blackHexValue );

                    break;
                default:
                    break;
                }

                // Try to keep the resulting EPS file human-readable by limiting
                // to 64 characters per line (although 255 characters per line
                // is the official limit for EPS).
                if ( contentLine.length() > 64 ) {
                    epsWriter.append( contentLine.toString() );

                    contentLine = new StringBuilder();
                }
            }
        }

        // Write the remaining hexadecimal characters that overflowed from the
        // final line written inside the 64-character loop above.
        if ( contentLine.length() > 0 ) {
            epsWriter.append( contentLine.toString() );
        }
    }

    /**
     * Writes the monochrome image operator, along with immediately preceding
     * parameters as specified in the PostScript Language Reference Manual.
     * <p>
     * This is a wrapper for the PostScript "image" operator syntax, to avoid
     * copy/paste errors in duplicative code. It must be preceded by the
     * relevant image preamble and other parameters. This method should remain
     * private because it has specific order dependencies and thus is of no use
     * to downstream clients or derived classes.
     *
     * @param epsWriter
     *            The {@link EpsWriter} for writing the EPS content
     * @param width
     *            The source image width, in samples
     *
     * @since 1.0
     */
    @SuppressWarnings("nls")
    private static void epsImage( final EpsWriter epsWriter, final int width ) {
        final StringBuilder procedure = new StringBuilder();
        procedure.append( "{currentfile " );
        procedure.append( width );
        procedure.append( " string readhexstring pop} bind" );
        epsWriter.append( procedure.toString() );

        // Monochrome images use the PostScript "image" operator. Although the
        // "colorimage" can also be used, with just one color component
        // specified, it is a bit more intuitive to use the dedicated grayscale
        // "image" operator, and it also has more capabilities to tap into.
        epsWriter.append( "image" );
    }

    /**
     * Writes the color image operator, along with immediately preceding
     * parameters as specified in the PostScript Language Reference Manual.
     * <p>
     * This is a wrapper for the PostScript "colorimage" operator, to avoid
     * copy/paste errors in duplicative code. It must be preceded by the
     * relevant image operator and other parameters. This method should remain
     * private because it has specific order dependencies and thus is of no use
     * to downstream clients or derived classes.
     *
     * @param epsWriter
     *            The {@link EpsWriter} for writing the EPS content
     * @param width
     *            The source image width, in samples
     * @param numberOfColorComponents
     *            The number of color components for the image, which implicitly
     *            sets the Color Space
     *
     * @since 1.0
     */
    @SuppressWarnings("nls")
    private static void epsColorimage( final EpsWriter epsWriter,
                                       final int width,
                                       final int numberOfColorComponents ) {
        final StringBuilder procedure = new StringBuilder();
        procedure.append( "{currentfile " );
        procedure.append( numberOfColorComponents );
        procedure.append( " " );
        procedure.append( width );
        procedure.append( " mul string readhexstring pop} bind" );
        epsWriter.append( procedure.toString() );

        // Color images use a single data source, with 1, 3, or 4 color
        // components. The number of color components implies the Color Space.
        final StringBuilder dataSource = new StringBuilder();
        dataSource.append( "false " );
        dataSource.append( numberOfColorComponents );
        epsWriter.append( dataSource.toString() );

        // Color images use the PostScript "colorimage" operator. Although the
        // "image" can also be used, this is only true if switching to
        // PostScript Level 3 or Level 4, which not all EPS viewers and editors
        // can handle. It is safer to mostly stick to PostScript Level 1 and 2.
        epsWriter.append( "colorimage" );
    }

}
