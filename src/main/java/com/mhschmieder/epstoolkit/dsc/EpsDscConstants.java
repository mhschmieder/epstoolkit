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
package com.mhschmieder.epstoolkit.dsc;

/**
 * {@code EpsDscConstants} is a container class for constants needed by the DSC
 * Header in order to comply with its specifications.
 *
 * @version 1.0
 *
 * @author Mark Schmieder
 */
public class EpsDscConstants {

    /**
     * The default constructor is disabled, as this is a static constants class.
     */
    private EpsDscConstants() {}

    /**
     * Static declaration of the DSC Conformance Level for EPS files produced by
     * this library, as determined by a canvas of current applications and their
     * disappointingly low conformance levels. Settings this higher,
     * unfortunately crashes well-known widely-used professional applications.
     */
    @SuppressWarnings("nls") public static final String DSC_CONFORMANCE_LEVEL  = "3.0";

    /**
     * Static declaration of the EPSF Conformance Level for EPS files produced
     * by this library, as determined by a canvas of current applications and
     * their disappointingly low conformance levels. Settings this higher,
     * unfortunately crashes well-known widely-used professional applications.
     */
    @SuppressWarnings("nls") public static final String EPSF_CONFORMANCE_LEVEL = "3.0";

}
