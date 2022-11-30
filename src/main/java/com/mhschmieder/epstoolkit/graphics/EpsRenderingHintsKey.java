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

import java.awt.RenderingHints;

/**
 * {@code EpsRenderingHintsKey} is a key for Rendering Hints used by the
 * {@link EpsGraphics2D} class, as defined by {@link EpsRenderingHints}.
 */
public class EpsRenderingHintsKey extends RenderingHints.Key {

    /**
     * Construct a key using the indicated private key.
     * <p>
     * Each subclass of Key maintains its own unique domain of integer keys. No
     * two objects with the same integer key and of the same specific subclass
     * can be constructed.
     * <p>
     * An exception is thrown if an attempt is made to construct another object
     * of a given class with the same integer key as a pre-existing instance of
     * that subclass of {@link EpsRenderingHintsKey}.
     *
     * @param privateKey
     *            the specified key
     */
    public EpsRenderingHintsKey( final int privateKey ) {
        super( privateKey );
    }

    /**
     * Returns {@code true} if the key value is compatible with this key, and
     * {@code false} otherwise.
     *
     * @param val
     *            the key value.
     *
     * @return A flag for whether the.
     */
    @Override
    public boolean isCompatibleValue( final Object val ) {
        boolean compatibleValue = false;
        
        switch ( intKey() ) {
        case 0:
            compatibleValue = ( val == null )
                || EpsRenderingHints.VALUE_TEXT_RENDERING_MODE_TEXT.equals( val )
                || EpsRenderingHints.VALUE_TEXT_RENDERING_MODE_VECTOR.equals( val );
            break;
        default:
            throw new RuntimeException( "Unsupported Rendering Hint Key" ); //$NON-NLS-1$
        }
        
        return compatibleValue;
    }

}
