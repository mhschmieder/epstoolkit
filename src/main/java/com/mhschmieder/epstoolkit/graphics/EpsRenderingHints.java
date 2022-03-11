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
package com.mhschmieder.epstoolkit.graphics;

/**
 * {@code EpsRenderingHints} defines the Rendering Hints that can be used with
 * the {@link EpsGraphics2D} class. There is just one Rendering Hint available
 * at present:<br>
 * <ul>
 * <li>{@link #KEY_TEXT_RENDERING_MODE} is a Rendering Hint that controls
 * whether strings are rendered as regular text or as vector graphics</li>
 * </ul>
 *
 * @version 1.0
 *
 * @author Mark Schmieder
 */
public final class EpsRenderingHints {

    /**
     * The default constructor is disabled, as this is a static constants class.
     */
    private EpsRenderingHints() {}

    /**
     * The key for the Rendering Hint that controls whether strings are rendered
     * as regular text or as vector graphics.
     * <p>
     * Vectorizing the text results in larger output files, but allows for more
     * precise rendering to match the on-screen appearance, as well as better
     * handling of rotated text (such as for vertical axis labels in charts). It
     * also handles the rendering of Unicode characters outside the normal
     * US-ASCII 7-bit character set, without having to resort to embedded fonts.
     * <p>
     * The resulting output has text that is much sharper and scalable, and
     * eliminates downstream issues with unavailable fonts on different systems.
     * <p>
     * Affine Transforms can only affect the starting point of text using Basic
     * Text Rendering Mode - all text will be oriented horizontally regardless.
     * <p>
     * With Vectorized Text Mode enabled, it is not necessary for the EPS Viewer
     * to have the required fonts installed.
     * <p>
     * Turning off Vectorized Text Mode requires the EPS Viewer to have the
     * necessary fonts installed. If there is a lot of text, this significantly
     * reduces the file size of your EPS Documents.
     * <p>
     * As most modern design software is able to recognize vectorized text that
     * was rendered from common fonts, EPS Viewers can still provide selectable
     * and editable text if this mode is set to vector graphics (the default).
     * <p>
     * Current valid hint values are: {@link #VALUE_TEXT_RENDERING_MODE_TEXT}
     * and {@link #VALUE_TEXT_RENDERING_MODE_VECTOR}.
     */
    public static final EpsRenderingHintsKey            KEY_TEXT_RENDERING_MODE          =
                                                                                new EpsRenderingHintsKey( 0 );

    /**
     * Hint value for <code>KEY_TEXT_RENDERING_MODE</code> to specify that
     * strings should be written to the output using the standard EPS "show"
     * operator with the original text (string).
     */
    @SuppressWarnings("nls") public static final Object VALUE_TEXT_RENDERING_MODE_TEXT   =
                                                                                       "VALUE_TEXT_RENDERING_MODE_TEXT";

    /**
     * Hint value for <code>KEY_TEXT_RENDERING_MODE</code> to specify that
     * strings should be written to the output using vectorization to lines and
     * curves for EPS paths.
     */
    @SuppressWarnings("nls") public static final Object VALUE_TEXT_RENDERING_MODE_VECTOR =
                                                                                         "VALUE_TEXT_RENDERING_MODE_VECTOR";

}
