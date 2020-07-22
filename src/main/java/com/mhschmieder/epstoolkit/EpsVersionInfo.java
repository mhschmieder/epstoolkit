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
 * {@code EpsVersionInfo} is a container class for EpsToolkit Library naming and
 * versioning.
 *
 * @version 1.0
 *
 * @author Mark Schmieder
 */
public final class EpsVersionInfo {

    /**
     * The default constructor is disabled, as this is a static constants class.
     */
    private EpsVersionInfo() {}

    /**
     * Static declaration of the official name of this library, to avoid
     * programmer error leading to confusion in clients that might use more than
     * one EPS writer library.
     */
    @SuppressWarnings("nls") public static final String LIBRARY_NAME                = "EpsToolkit";

    /**
     * Static declaration of the current version of this library, for use mostly
     * in the DSC-compliant header of the EPS output file.
     */
    @SuppressWarnings("nls") public static final String LIBRARY_VERSION             = "1.0";

    /**
     * Static declaration of the fully qualified name of this library with its
     * release version as part of the identifier.
     */
    @SuppressWarnings("nls") public static final String LIBRARY_RELEASE_NAME        = LIBRARY_NAME
            + " Version " + LIBRARY_VERSION;

    /**
     * Static declaration of the current web host for this open source library.
     * This repository location is included as part of the Document Creator
     * Comment in the DSC-compliant header for the EPS output file.
     */
    @SuppressWarnings("nls") public static final String LIBRARY_REPOSITORY_LOCATION =
                                                                                    "https://github.com/mhschmieder/epstoolkit";

}
