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
package com.mhschmieder.epstoolkit.dsc;

import java.time.LocalDate;

import com.mhschmieder.epstoolkit.EpsVersionInfo;

/**
 * {@code EpsDscUtilities} is a utility class for methods related to Adobe's
 * Document Structuring Conventions (DSC) Specification, which apply not just to
 * EPS but also to PDF and related PostScript-derived formats. As such, these
 * conventions continue to evolve, but have a minimal subset that must be
 * support by every EPS Document. Specifically, the EPS Document Identification
 * Comment, and the (older, low-resolution integer-based) Bounding Box Comment.
 * <p>
 * Visit the web address below, for the PDF version of the DSC specifications:
 * <p>
 * https://www.adobe.com/content/dam/acom/en/devnet/actionscript/articles/5001.DSC_Spec.pdf
 *
 * @version 1.0
 *
 * @author Mark Schmieder
 */
public final class EpsDscUtilities {

    /**
     * The default constructor is disabled, as this is a static utilities class.
     */
    private EpsDscUtilities() {}

    /**
     * Static declaration of the specification-compliant EPS Document
     * Identification Comment for the DSC Header, to avoid programmer error
     * leading to EPS documents that won't load in downstream applications.
     */
    @SuppressWarnings("nls") public static final String EPS_DOCUMENT_IDENTIFICATION_COMMENT =
                                                                                            "%!PS-Adobe-"
                                                                                                    + EpsDscConstants.DSC_CONFORMANCE_LEVEL
                                                                                                    + " "
                                                                                                    + "EPSF-"
                                                                                                    + EpsDscConstants.EPSF_CONFORMANCE_LEVEL;

    /**
     * Static declaration of a default specification-compliant EPS Document
     * Creator Comment for the DSC Header, to avoid programmer error leading to
     * EPS documents that won't load in downstream applications.
     */
    @SuppressWarnings("nls") public static final String DEFAULT_DOCUMENT_CREATOR            =
                                                                                 EpsVersionInfo.LIBRARY_RELEASE_NAME
                                                                                         + ", "
                                                                                         + EpsVersionInfo.LIBRARY_REPOSITORY_LOCATION;

    /**
     * Writes the DSC Header to the specified {@link StringBuilder}.
     * <p>
     * Although DSC is also used for PDF and other formats (with significantly
     * enhanced header options), the only two required fields for EPS are the
     * line confirming that the file conforms to Version 3 of the EPS format,
     * and the Bounding Box comment. It is in fact often safer to leave out many
     * of the other header comments, as not all clients and readers support
     * them, or do so properly. The ones that are deemed safe are included here.
     * <p>
     * We specify 8-Bit Document Data (vs. 7-Bit), because there may be issues
     * otherwise with Titles and other fields specified in languages other than
     * English. This of course does not cover 16-Bit and beyond, for Unicode.
     * <p>
     * It is up to the caller to ensure that the Bounding Box is positive.
     *
     * @param stringBuilder
     *            The {@link StringBuilder} for appending the DSC Header
     * @param title
     *            The {@link String} to use as the EPS Document's title
     * @param creator
     *            The {@link String} to use as the EPS Document's creator
     * @param boundingBoxMinX
     *            The lower left x-coordinate of the bounding box
     * @param boundingBoxMinY
     *            The lower left y-coordinate of the bounding box
     * @param boundingBoxMaxX
     *            The upper right x-coordinate of the bounding box
     * @param boundingBoxMaxY
     *            The upper right y-coordinate of the bounding box
     *
     * @since 1.0
     */
    @SuppressWarnings("nls")
    public static void writeDscHeader( final StringBuilder stringBuilder,
                                       final String title,
                                       final String creator,
                                       final int boundingBoxMinX,
                                       final int boundingBoxMinY,
                                       final int boundingBoxMaxX,
                                       final int boundingBoxMaxY ) {
        // Make a default Title if none was provided, or if empty.
        final String titleVerified = ( ( title == null ) || title.isEmpty() )
            ? "The EPS Document"
            : title;

        // In case no creator was provided, use this library as the default.
        //
        // The Creator comment usually indicates the name of the document
        // composition software, which must be provided by clients of this
        // library, but if none is provided, stating this library as the
        // document's creator is a good fallback vs. leaving it blank.
        final String creatorVerified = ( ( creator == null ) || creator.isEmpty() )
            ? DEFAULT_DOCUMENT_CREATOR
            : creator;

        // Grab the current date and time in ISO format so we can extract an
        // ISO-compatible locale-sensitive date at the level of resolution we
        // care about, which is probably just to the year/month/day.
        final LocalDate localDate = LocalDate.now();
        final String timeStamp = localDate.toString();

        // Convert the Bounding Box to a string representation for the header.
        final StringBuilder boundingBoxDescriptor = new StringBuilder();
        boundingBoxDescriptor.append( Integer.toString( boundingBoxMinX ) );
        boundingBoxDescriptor.append( " " );
        boundingBoxDescriptor.append( Integer.toString( boundingBoxMinY ) );
        boundingBoxDescriptor.append( " " );
        boundingBoxDescriptor.append( Integer.toString( boundingBoxMaxX ) );
        boundingBoxDescriptor.append( " " );
        boundingBoxDescriptor.append( Integer.toString( boundingBoxMaxY ) );
        final String boundingBoxLabel = boundingBoxDescriptor.toString();

        // Compose the DSC-compliant EPS Header (i.e., with Adobe PostScript
        // Document Structuring Comments), set to 8-bit characters for UTF-8.
        //
        // Not every reader, viewer, or editor client application handles the
        // Origin comment, but some require it, so it is safest to include the
        // Origin and to set it to zero vs. allowing the caller to specify a
        // preferred Page Origin for EPS. Due to low adoption rates however, the
        // new High Resolution Bounding Box comment is excluded, as EPS requires
        // the older low resolution integer-based bounding box regardless.
        //
        // Although the specifications say to take line width into account and
        // to adjust the bounding box accordingly, in the context of a generic
        // toolkit this isn't practical so it is instead advised to be careful
        // with GUI elements and graphical components that didn't use any insets
        // or other gaps, as line width won't necessarily be a constant value.
        final StringBuilder header = new StringBuilder();
        header.append( EPS_DOCUMENT_IDENTIFICATION_COMMENT + "\n" );
        header.append( "%%Title: " + titleVerified + "\n" );
        header.append( "%%Creator: " + creatorVerified + "\n" );
        header.append( "%%CreationDate: " + timeStamp + "\n" );
        header.append( "%%DocumentData: Clean8Bit\n" );
        header.append( "%%DocumentProcessColors: Black\n" );
        header.append( "%%ColorUsage: Color\n" );
        header.append( "%%LanguageLevel: 2\n" );
        header.append( "%%Origin: 0 0\n" );
        header.append( "%%Pages: 1\n" );
        header.append( "%%Page: 1 1\n" );
        header.append( "%%BoundingBox: " + boundingBoxLabel + "\n" );
        header.append( "%%EndComments\n" );
        header.append( "\n" );

        // It is more efficient to compose smaller sections independently before
        // appending to the main content wrapper.
        stringBuilder.append( header );
    }

    /**
     * Writes the DSC Footer to the specified {@link StringBuilder}.
     *
     * @param stringBuilder
     *            The {@link StringBuilder} for appending the DSC Footer
     *
     * @since 1.0
     */
    @SuppressWarnings("nls")
    public static void writeDscFooter( final StringBuilder stringBuilder ) {
        // Write the DSC-compliant EPS footer (i.e., with Adobe PostScript
        // Document Structuring Comments); this is mostly just an "EOF" Comment,
        // as the EPS format is single-page only.
        stringBuilder.append( "\n" );
        stringBuilder.append( "%%EOF" );
    }

}
