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

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;

import com.mhschmieder.epstoolkit.EpsDocument;
import com.mhschmieder.epstoolkit.operators.EpsGraphicsOperators;
import com.mhschmieder.epstoolkit.operators.EpsImageOperators;
import com.mhschmieder.graphicstoolkit.color.ColorMode;
import com.mhschmieder.graphicstoolkit.font.FontUtilities;
import com.mhschmieder.graphicstoolkit.geometry.GeometryUtilities;
import com.mhschmieder.graphicstoolkit.graphics.DrawMode;
import com.mhschmieder.graphicstoolkit.graphics.GraphicsUtilities;
import com.mhschmieder.graphicstoolkit.image.ImageConversionUtilities;
import com.mhschmieder.graphicstoolkit.text.TextUtilities;

/**
 * {@code EpsGraphics2D} is a graphics wrapper class that is suitable for
 * creating high quality scalable EPS vector graphics for use in downstream
 * documents and reports, and can be used just like a standard AWT screen
 * oriented {@link Graphics2D} canvas.
 * <p>
 * The methods of this class all adhere to Adobe's Encapsulated PostScript (EPS)
 * File Format Specification:
 * <p>
 * https://www.adobe.com/content/dam/acom/en/devnet/actionscript/articles/5002.EPSF_Spec.pdf
 * <p>
 * This class simplifies the EPS generation by redirecting existing screen
 * oriented methods found on the {@link Graphics2D} class in AWT, thus allowing
 * the client code to simply redirect normal screen repaint loops to use this
 * special version of that class in place of the normal screen canvas instance.
 * <p>
 * What this means is that the client code can completely ignore the logic and
 * format of Encapsulated PostScript altogether; it is instead this library's
 * responsibility to properly map the {@link Graphics2D} methods to appropriate
 * Encapsulated postScript operators that follow the proper EPS syntax rules.
 * <p>
 * Fortunately, there is a very close match between the EPS spec and Java2D
 * (the 2D Graphics subsystem with Java's AWT toolkit). There are in fact a few
 * methods defined on {@link Graphics2D} that are not relevant to EPS due to
 * the lack of support for transparency, alpha compositing, and rendering hints.
 * Those are implemented to do nothing, and to not punish the invoker at all.
 * <p>
 * There are a few existing Java libraries that directly write EPS from scratch,
 * which can get a bit complex but may be best applied to a Report Designer tool
 * that may be composing content algorithmically or via some sort of wizard.
 * <p>
 * All numeric values that are to be written as strings to PostScript, must be
 * specified as single-precision. Only numbers that interact directly with AWT
 * Transforms should be double-precision.
 * <p>
 * Although EPS can handle single-precision, and consumers of this library will
 * often be producing output from non-integer source data, using
 * {@link Graphics2D} overloading as the output strategy means we are stuck with
 * its low precision methods as any additional floating-point methods for
 * graphics primitives would never get called by the AWT/Swing screen rendering
 * code when it redirects to this EPS specialization of the {@link Graphics2D}
 * class. This is where other strategies, such as direct EPS output and XSLT
 * approaches, have an advantage in rendering sharpness, but at the cost of far
 * more complex code and architecture. Several such EPS libraries exist.
 * <p>
 * Vectorized Text Mode is the default, as substitute font mapping is generally
 * problematic and can lead to files not loading in some applications, along
 * with the usual problems with rotated text; especially common as y-axis
 * labels in charting software but not that uncommon in other contexts either.
 * The selection of Vectorized Text Mode is done via custom Rendering Hints.
 * <p>
 * The following AWT functions for {@link Graphics} and {@link Graphics2D} are
 * not supported, as they have no application in the EPS context, but all of the
 * related methods are implemented in a way that causes no harm in any context:
 * <p>
 * {@link #setComposite}
 * <p>
 * {@link #getComposite}
 * <p>
 * {@link #copyArea}
 * <p>
 * {@link #dispose}
 * <p>
 * {@link #setPaintMode}
 * <p>
 * {@link #setXORMode}
 *
 * @version 1.0
 *
 * @author Mark Schmieder
 */
public final class EpsGraphics2D extends Graphics2D {

    /**
     * The default background color for pre-clearing regions of the EPS Page
     * before drawing, filling, etc., is white, as EPS is usually targeted for
     * black-on-white reports, documents, and other print-designated output.
     */
    public static final Color     DEFAULT_BACKGROUND_COLOR = Color.WHITE;

    /**
     * The default foreground color for drawing, filling, etc., is black, as EPS
     * is usually targeted for black-on-white reports, documents, and other
     * print-designated output.
     */
    public static final Color     DEFAULT_FOREGROUND_COLOR = Color.BLACK;

    /**
     * The default {@link ColorMode} is set to RGB as that is the most common
     * mode that covers all downstream clients and contexts (screen display,
     * printing, etc.) when the client doesn't specify a mode explicitly.
     */
    public static final ColorMode DEFAULT_COLOR_MODE       = ColorMode.RGB;

    /**
     * The background color for pre-clearing regions of the EPS Page.
     */
    private Color                 backgroundColor;

    /**
     * The foreground color for painting and drawing on the EPS Page.
     * <p>
     * This color should always be in sync with the paint color; it is part of
     * the older {@link Graphics} class so does not allow for gradients.
     */
    private Color                 foregroundColor;

    /**
     * The current color for overwriting existing content on the EPS Page.
     * <p>
     * This color should always be in sync with the foreground color; it is part
     * of {@link Graphics2D} but not {@link Graphics}, and allows for gradients
     * (though the Encapsulated PostScript specification does not).
     */
    private Paint                 paint;

    /**
     * The current {@link Composite} for alpha transparency; not used by EPS.
     */
    private Composite             composite;

    /**
     * The current {@link Stroke} for drawing on the EPS Page.
     */
    private Stroke                stroke;

    /**
     * The current font for writing text to the EPS Page.
     */
    private Font                  font;

    /**
     * The Rendering Hints for this Graphics Context; mostly for text handling.
     */
    private final RenderingHints  renderingHints;

    /**
     * Flag for whether a clipping rectangle is set or not. {@code true} if set.
     * If set, there is a Graphics State wrapping an active Clip Area.
     */
    private boolean               clipActive;

    /**
     * If set, this Clip Area is intersected with shapes that are about to be
     * drawn to the EPS Page, to make sure they fit within the defined borders.
     */
    private Area                  clipArea;

    /**
     * This Transform is applied to the Clip Area to help determine its bounds.
     */
    private AffineTransform       clipTransform;

    /**
     * This is the Transform that is applied to the overall EPS Page
     * positioning for each output operation.
     */
    private AffineTransform       epsTransform;

    /**
     * The current Color Mode for painting and drawing to the EPS Page.
     */
    private ColorMode             epsColorMode;

    /**
     * The {@link EpsDocument} used for actually writing to the EPS Document.
     * <p>
     * Note that the {@link EpsDocument} is never used directly by the client;
     * it is always indirectly used via this {@link EpsGraphics2D} instance.
     */
    private final EpsDocument     epsDocument;

    /**
     * A {@link Line2D} instance that is lazily instantiated in drawLine(), and
     * then subsequently reused in order to avoid excessive object creation on
     * the stack and frequent triggers of the garbage collector.
     */
    private Line2D                line;

    /**
     * A {@link Rectangle2D} instance that is lazily instantiated in drawRect()
     * and fillRect(), and then subsequently reused in order to avoid excessive
     * object creation on the stack and frequent triggers of the garbage
     * collector.
     */
    private Rectangle2D           rect;

    /**
     * A {@link RoundRectangle2D} instance that is lazily instantiated in
     * drawRoundRect() and fillRoundRect(), and then subsequently reused in
     * order to avoid excessive object creation on the stack and frequent
     * triggers of the garbage collector.
     */
    private RoundRectangle2D      roundRect;

    /**
     * An {@link Ellipse2D} instance that is lazily instantiated in drawOval()
     * and fillOval(), and then subsequently reused in order to avoid excessive
     * object creation on the stack and frequent triggers of the garbage
     * collector.
     */
    private Ellipse2D             oval;

    /**
     * An {@link Arc2D} instance that is lazily instantiated in drawArc() and
     * fillArc(), and then subsequently reused in order to avoid excessive
     * object creation on the stack and frequent triggers of the garbage
     * collector.
     */
    private Arc2D                 arc;

    //////////////////////////// Constructors ////////////////////////////////

    /**
     * Default constructor. All parameters are defaulted initially, but can be
     * changed vis setter functions before or during output generation begins.
     * <p>
     * Constructs a new Graphics Context that is initially empty and can be
     * drawn on like a regular {@link Graphics2D} canvas. The EPS Document is
     * written to {@link StringBuilder} as it goes, which reduces memory usage.
     * <p>
     * By default, Vectorized Text Mode is turned on (via Rendering Hints).
     *
     * @param documentOwner
     *            The {@link EpsDocument} that owns this Graphics Context
     *
     * @since 1.0
     */
    public EpsGraphics2D( final EpsDocument documentOwner ) {
        epsDocument = documentOwner;

        renderingHints = new RenderingHints( EpsRenderingHints.KEY_TEXT_RENDERING_MODE,
                                             EpsRenderingHints.VALUE_TEXT_RENDERING_MODE_VECTOR );

        epsColorMode = ColorMode.defaultValue();

        backgroundColor = DEFAULT_BACKGROUND_COLOR;
        foregroundColor = DEFAULT_FOREGROUND_COLOR;

        clipActive = false;
        clipArea = null;
        clipTransform = new AffineTransform();
        epsTransform = new AffineTransform();

        // Use setter methods vs. assignment statements, for the main
        // attributes that are part of the Graphics2D parent class, so that
        // these values get written to the EPS Document right away.
        setPaint( DEFAULT_FOREGROUND_COLOR );
        setComposite( AlphaComposite.getInstance( AlphaComposite.CLEAR ) );
        setFont( Font.decode( null ) );
        setStroke( new BasicStroke() );
    }

    /**
     * Copy constructor.
     * <p>
     * Constructs a new {@link EpsGraphics2D} instance that is a copy of the
     * supplied {@link EpsGraphics2D} Graphics Context and points to the same
     * {@link EpsDocument} instance.
     *
     * @param epsGraphics
     *            The {@link EpsGraphics2D} Graphics Context used to redirect
     *            graphics drawing command to the EPS document
     *
     * @since 1.0
     */
    protected EpsGraphics2D( final EpsGraphics2D epsGraphics ) {
        epsDocument = epsGraphics.epsDocument;

        // For the Rendering Hints, we are careful to make a new cloned copy.
        renderingHints = epsGraphics.getRenderingHints();

        epsColorMode = epsGraphics.epsColorMode;

        backgroundColor = epsGraphics.backgroundColor;
        foregroundColor = epsGraphics.foregroundColor;

        clipActive = epsGraphics.clipActive;
        clipArea = epsGraphics.clipArea;
        clipTransform = ( AffineTransform ) epsGraphics.clipTransform.clone();
        epsTransform = ( AffineTransform ) epsGraphics.epsTransform.clone();

        // Use setter methods vs. assignment statements, for the main
        // attributes that are part of the Graphics2D parent class, so that
        // these values get written to the EPS Document right away.
        setPaint( epsGraphics.paint );
        setComposite( epsGraphics.composite );
        setFont( epsGraphics.font );
        setStroke( epsGraphics.stroke );
    }

    ////////////////// Accessor methods for private data /////////////////////

    /**
     * Returns a flag for whether or not a clipping rectangle is active
     * ({@code true} if so; {@code false} if not).
     *
     * @return {@code true} if a clipping rectangle is active; {@code false}
     *         otherwise
     *
     * @since 1.0
     *
     * @see #setClipActive(boolean)
     */
    public boolean isClipActive() {
        return clipActive;
    }

    /**
     * Sets a flag for whether or not a clipping rectangle is active
     * ({@code true} if so; {@code false} if not).
     *
     * @param clippingRectangleActive
     *            Set to {@code true} if a clipping rectangle is active;
     *            {@code false} otherwise
     *
     * @since 1.0
     *
     * @see #isClipActive()
     */
    public void setClipActive( final boolean clippingRectangleActive ) {
        clipActive = clippingRectangleActive;
    }

    /**
     * Returns the Color Mode used for all drawing operations.
     *
     * @return The {@link ColorMode} to use for the EPS Document
     *
     * @since 1.0
     *
     * @see #setColorMode(ColorMode)
     */
    public ColorMode getColorMode() {
        return epsColorMode;
    }

    /**
     * Sets the EPS Color Mode to use when drawing on the document.
     *
     * @param colorMode
     *            The {@link ColorMode} to use for the EPS Document
     *
     * @since 1.0
     *
     * @see #getColorMode()
     */
    public void setColorMode( final ColorMode colorMode ) {
        epsColorMode = colorMode;
    }

    /////////////// Efficient floating-point polyline methods ////////////////

    /**
     * Draws a polyline using single-precision coordinates.
     * <p>
     * This method makes up for a deficiency in the available methods for
     * the AWT {@link Graphics} class, as the downstream path-handling code is
     * perfectly capable of using single-precision floating-point coordinates.
     * Of course, being a unique method that isn't overridden from
     * {@link Graphics2D} or {@link Graphics} means that this method can only
     * be invoked directly by clients vs. indirectly in rendering code where the
     * client might not have full control. Nevertheless, some client code will
     * go for massive lists of single-precision polylines for efficiency.
     *
     * @param xPoints
     *            A single-precision array of x coordinates for the polyline
     * @param yPoints
     *            A single-precision array of x coordinates for the polyline
     * @param numberOfPoints
     *            The number of single-precision points to make for this
     *            polyline
     *
     * @since 1.0
     */
    public void drawPolyline( final float[] xPoints,
                              final float[] yPoints,
                              final int numberOfPoints ) {
        final GeneralPath path = GeometryUtilities.makePolyline( xPoints, yPoints, numberOfPoints );
        draw( path );
    }

    /**
     * Draws a polyline using double-precision coordinates.
     * <p>
     * This method makes up for a deficiency in the available methods for
     * the AWT {@link Graphics} class, as the downstream path-handling code is
     * perfectly capable of using double-precision floating-point coordinates.
     * Of course, being a unique method that isn't overridden from
     * {@link Graphics2D} or {@link Graphics} means that this method can only
     * be invoked directly by clients vs. indirectly in rendering code where the
     * client might not have full control. Nevertheless, some client code will
     * go for massive lists of double-precision polylines for efficiency.
     *
     * @param xPoints
     *            A double-precision array of x coordinates for the polyline
     * @param yPoints
     *            A double-precision array of x coordinates for the polyline
     * @param numberOfPoints
     *            The number of double-precision points to make for this
     *            polyline
     *
     * @since 1.0
     */
    public void drawPolyline( final double[] xPoints,
                              final double[] yPoints,
                              final int numberOfPoints ) {
        final GeneralPath path = GeometryUtilities.makePolyline( xPoints, yPoints, numberOfPoints );
        draw( path );
    }

    //////////// Efficient setter methods for geometry primitives ////////////

    /**
     * Sets the attributes of the reusable {@link Line2D} object that is used by
     * the {@link #drawLine(int, int, int, int)} method.
     *
     * @param x1
     *            The X coordinate of the start point.
     * @param y1
     *            The Y coordinate of the start point.
     * @param x2
     *            The X coordinate of the end point.
     * @param y2
     *            The Y coordinate of the end point.
     *
     * @since 1.0
     */
    private void setLine( final double x1, final double y1, final double x2, final double y2 ) {
        if ( line == null ) {
            line = new Line2D.Double( x1, y1, x2, y2 );
        }
        else {
            line.setLine( x1, y1, x2, y2 );
        }
    }

    /**
     * Sets the attributes of the reusable {@link Rectangle2D} object that is
     * used by the {@link #drawRect(int, int, int, int)} and
     * {@link #fillRect(int, int, int, int)} methods.
     *
     * @param x
     *            The X coordinate of the upper-left corner of the newly
     *            constructed {@link Rectangle2D}
     * @param y
     *            The Y coordinate of the upper-left corner of the newly
     *            constructed {@link Rectangle2D}
     * @param width
     *            The width of the newly constructed {@link Rectangle2D}
     * @param height
     *            The height of the newly constructed {@link Rectangle2D}
     *
     * @since 1.0
     */
    private void setRect( final int x, final int y, final int width, final int height ) {
        if ( rect == null ) {
            rect = new Rectangle2D.Double( x, y, width, height );
        }
        else {
            rect.setRect( x, y, width, height );
        }
    }

    /**
     * Sets the attributes of the reusable {@link RoundRectangle2D} object that
     * is used by the {@link #drawRoundRect(int, int, int, int, int, int)} and
     * {@link #fillRoundRect(int, int, int, int, int, int)} methods.
     *
     * @param x
     *            The X coordinate of the newly constructed
     *            {@link RoundRectangle2D}
     * @param y
     *            The Y coordinate of the newly constructed
     *            {@link RoundRectangle2D}
     * @param width
     *            The width to which to set the newly constructed
     *            {@link RoundRectangle2D}
     * @param height
     *            The height to which to set the newly constructed
     *            {@link RoundRectangle2D}
     * @param arcWidth
     *            The width of the arc to use to round off the corners of the
     *            newly constructed {@link RoundRectangle2D}
     * @param arcHeight
     *            The height of the arc to use to round off the corners of the
     *            newly constructed {@link RoundRectangle2D}
     *
     * @since 1.0
     */
    private void setRoundRect( final int x,
                               final int y,
                               final int width,
                               final int height,
                               final int arcWidth,
                               final int arcHeight ) {
        if ( roundRect == null ) {
            roundRect = new RoundRectangle2D.Double( x, y, width, height, arcWidth, arcHeight );
        }
        else {
            roundRect.setRoundRect( x, y, width, height, arcWidth, arcHeight );
        }
    }

    /**
     * Sets the attributes of the reusable {@link Arc2D} object that is used by
     * {@link #drawArc(int, int, int, int, int, int)} and
     * {@link #fillArc(int, int, int, int, int, int)} methods.
     *
     * @param x
     *            The X coordinate of the upper-left corner of the arc's framing
     *            rectangle
     * @param y
     *            The Y coordinate of the upper-left corner of the arc' framing
     *            rectangle
     * @param width
     *            The overall width of the full ellipse of which this arc is a
     *            partial section
     * @param height
     *            The overall height of the full ellipse of which this arc is a
     *            partial section
     * @param startAngle
     *            The starting angle of the arc in degrees (0 = 3 o'clock)
     * @param arcAngle
     *            The angular extent of the arc in degrees (anti-clockwise)
     * @param closureType
     *            The closure type for the arc: OPEN, CHORD, or PIE
     *
     * @since 1.0
     */
    private void setArc( final int x,
                         final int y,
                         final int width,
                         final int height,
                         final int startAngle,
                         final int arcAngle,
                         final int closureType ) {
        if ( arc == null ) {
            arc = new Arc2D.Double( x, y, width, height, startAngle, arcAngle, closureType );
        }
        else {
            arc.setArc( x, y, width, height, startAngle, arcAngle, closureType );
        }
    }

    /**
     * Sets the attributes of the reusable {@link Ellipse2D} object that is
     * used by the {@link #drawOval(int, int, int, int)} and
     * {@link #fillOval(int, int, int, int)} methods.
     *
     * @param x
     *            The X coordinate of the upper-left corner of the framing
     *            rectangle
     * @param y
     *            the Y coordinate of the upper-left corner of the framing
     *            rectangle
     * @param width
     *            The width of the framing rectangle
     * @param height
     *            The height of the framing rectangle
     *
     * @since 1.0
     */
    private void setOval( final int x, final int y, final int width, final int height ) {
        if ( oval == null ) {
            oval = new Ellipse2D.Double( x, y, width, height );
        }
        else {
            oval.setFrame( x, y, width, height );
        }
    }

    ///////////////////// Graphics2D method overrides ////////////////////////

    /**
     * Returns the device configuration associated with the local graphics
     * environment, which is dependent on machine architecture and screen
     * configurations, unique per user.
     *
     * @since 1.0
     */
    @Override
    public GraphicsConfiguration getDeviceConfiguration() {
        final GraphicsConfiguration graphicsConfiguration = GraphicsUtilities
                .getDeviceConfiguration();
        return graphicsConfiguration;
    }

    /**
     * Returns the current {@link Paint} attributes of this
     * {@link EpsGraphics2D} instance.
     *
     * @return The {@link Paint} object to be used to generate color during the
     *         rendering process (never {@code null})
     *
     * @since 1.0
     *
     * @see #setPaint(java.awt.Paint)
     */
    @Override
    public Paint getPaint() {
        return paint;
    }

    /**
     * Sets the {@link Paint} attribute for the {@link EpsGraphics2D} context.
     * Calling this method with a {@code null} {@link Paint} object does not
     * have any effect on the current {@link Paint} attribute of this {@link
     * EpsGraphics2D}.
     * <p>
     * Only {@link Paint} objects of type {@link Color} are respected by
     * {@link EpsGraphics2D} at this time; gradients are cached but may have no
     * impact as they could get ignored downstream due to "instanceof" checks
     * against the {@link Color} class not matching.
     *
     * @param newPaint
     *            The {@link Paint} object to be used to generate color during
     *            the rendering process, or {@code null}
     *
     * @since 1.0
     *
     * @see #getPaint()
     */
    @Override
    public void setPaint( final Paint newPaint ) {
        if ( newPaint == null ) {
            return;
        }

        paint = newPaint;

        if ( newPaint instanceof Color ) {
            setColor( ( Color ) newPaint );
        }
        else if ( newPaint instanceof GradientPaint ) {
            // Not supported by the Encapsulated PostScript specification.
        }
        else if ( newPaint instanceof LinearGradientPaint ) {
            // Not supported by the Encapsulated PostScript specification.
        }
        else if ( newPaint instanceof RadialGradientPaint ) {
            // Not supported by the Encapsulated PostScript specification.
        }
    }

    /**
     * Returns the current foreground color. This will be a default value
     * (black) until it is changed using the {@link #setColor} method.
     * <p>
     * It is not recommended to call this method as it is supplied solely for
     * backward compatibility with the older {@link Graphics} class; the newer
     * {@link #getPaint} method should be used instead, as it supports various
     * gradients that were added to {@link Graphics2D}. The cached foreground
     * color and paint are guaranteed to stay in sync, so this method is safe.
     *
     * @return The current foreground color used for all graphics operations
     *
     * @since 1.0
     *
     * @see #setColor(java.awt.Color)
     */
    @Override
    public Color getColor() {
        return foregroundColor;
    }

    /**
     * Sets the Color to be used when drawing all future shapes, text, etc.
     * <p>
     * It is not recommended to call this method as it is supplied solely for
     * backward compatibility with the older {@link Graphics} class; the newer
     * {@link #setPaint} method should be used instead, as it supports various
     * gradients that were added to {@link Graphics2D}. This method guarantees
     * that the cached foreground color and paint stay in sync, so is safe.
     * <p>
     * There is no need to match or contrast the supplied color with any other
     * cached color to avoid masking of graphics, as the background color for
     * {@link Graphics2D} objects is specialized and doesn't interact with the
     * foreground color, and as the paint color is kept in sync with foreground.
     * <p>
     * This function writes the new color to the EPS output even if it is the
     * same as the current cached color, as the Graphics State might be
     * different or there might have been a temporary color written previously.
     *
     * @param color
     *            The new foreground color used for all graphics operations
     *
     * @since 1.0
     *
     * @see #setPaint(java.awt.Paint)
     */
    @Override
    public void setColor( final Color color ) {
        if ( color == null ) {
            return;
        }

        foregroundColor = color;
        paint = color;

        final ColorMode colorMode = getColorMode();
        EpsGraphicsOperators.epsSetColor( epsDocument, foregroundColor, colorMode );
    }

    /**
     * Returns the background color that is used by the {@link #clearRect}
     * method.
     *
     * @return color
     *         The {@link Color} to use for pre-filling background regions;
     *         potentially {@code null}, which is treated as a no-op
     *
     * @since 1.0
     *
     * @see #setBackground(java.awt.Color)
     */
    @Override
    public Color getBackground() {
        return backgroundColor;
    }

    /**
     * Sets the background color to be used by the {@link #clearRect} method;
     * {@code null} is permitted in order to be consistent with the reference
     * implementation's documentation, and is simply treated as a no-op.
     * <p>
     * There is no need to match or contrast the supplied color with any other
     * cached color to avoid masking of graphics, as this is a specialized
     * background object and isn't the same as that of the parent component.
     *
     * @param color
     *            The {@link Color} to use for pre-filling background regions;
     *            potentially {@code null}, which is treated as a no-op
     *
     * @since 1.0
     *
     * @see #getBackground()
     */
    @Override
    public void setBackground( final Color color ) {
        backgroundColor = color;
    }

    /**
     * Returns the current {@link Composite} value for alpha transparency; not
     * supported by EPS or PostScript.
     *
     * @since 1.0
     *
     * @see #setComposite(java.awt.Composite)
     */
    @Override
    public Composite getComposite() {
        return composite;
    }

    /**
     * Sets the new {@link Composite} value for alpha transparency; not
     * supported by EPS or PostScript.
     *
     * @since 1.0
     *
     * @see #getComposite()
     */
    @Override
    public void setComposite( final Composite comp ) {
        // Ignore invalid composite values as they aren't used by EPS.
        if ( comp == null ) {
            return;
        }

        composite = comp;
    }

    /**
     * Returns the {@link Stroke} currently in use. Guaranteed to be an instance
     * of {@link BasicStroke}.
     *
     * @since 1.0
     *
     * @see #setStroke(java.awt.Stroke)
     */
    @Override
    public Stroke getStroke() {
        return stroke;
    }

    /**
     * Sets the current stroke.
     * <p>
     * This method now accepts all stroke types, but anything not derived from
     * {@link BasicStroke} is treated downstream as a fill command, as EPS only
     * supports the equivalent of the AWT {@link BasicStroke} class.
     * <p>
     * It is not advisable to exit early when the incoming {@link Stroke}
     * matches the current cached {@link Stroke}, as that has been shown to have
     * edge cases where the stroke was never set in the EPS file, and also it
     * assumes that no other command or operators temporarily set the stroke.
     *
     * @throws IllegalArgumentException
     *             If the newStroke argument is null
     *
     * @since 1.0
     *
     * @see #getStroke()
     */
    @Override
    public void setStroke( final Stroke newStroke ) {
        if ( newStroke == null ) {
            throw new IllegalArgumentException( "Null 'newStroke' argument." ); //$NON-NLS-1$
        }

        // Cache the new stroke, as long as it isn't null.
        stroke = newStroke;

        // If this is not a BasicStroke, meaning it is some third-party custom
        // Stroke class that is derived from the original Stroke interface vs.
        // derived from the BasicStroke parent implementation class, we treat it
        // downstream as a "fill" (if it would otherwise be a "stroke"), as EPS
        // only supports the equivalent of AWT's BasicStroke class.
        if ( !( newStroke instanceof BasicStroke ) ) {
            return;
        }

        // Write the PostScript basic stroke attributes and dash pattern.
        final BasicStroke basicStroke = ( BasicStroke ) newStroke;
        EpsGraphicsOperators.epsSetStroke( epsDocument, basicStroke );
    }

    /**
     * Returns the current value for the specified Rendering Hint.
     * <p>
     * See the {@link EpsRenderingHints} class for details on supported hints.
     *
     * @param hintKey
     *            The Rendering Hint key
     * @return The current value for the specified Rendering Hint (or
     *         {@code null} if not found)
     *
     * @since 1.0
     *
     * @see #setRenderingHint(java.awt.RenderingHints.Key, java.lang.Object)
     */
    @Override
    public Object getRenderingHint( final RenderingHints.Key hintKey ) {
        return renderingHints.get( hintKey );
    }

    /**
     * Sets the value for the specified Rendering Hint.
     *
     * @param hintKey
     *            The Rendering Hint key
     * @param hintValue
     *            The Rendering Hint value
     *
     * @since 1.0
     *
     * @see #getRenderingHint(java.awt.RenderingHints.Key)
     */
    @Override
    public void setRenderingHint( final RenderingHints.Key hintKey, final Object hintValue ) {
        renderingHints.put( hintKey, hintValue );
    }

    /**
     * Returns a copy of the Rendering Hints. Modifying the returned copy has no
     * impact on the state of this {@link EpsGraphics2D} instance.
     *
     * @return The Rendering Hints (never {@code null}).
     *
     * @since 1.0
     *
     * @see #setRenderingHints(java.util.Map)
     */
    @Override
    public RenderingHints getRenderingHints() {
        final RenderingHints renderingHintsCopy = ( RenderingHints ) renderingHints.clone();
        return renderingHintsCopy;
    }

    /**
     * Sets (replaces) the Rendering Hints to the specified collection.
     *
     * @param hints
     *            The new Rendering Hints ({@code null} not permitted)
     *
     * @since 1.0
     *
     * @see #getRenderingHints()
     */
    @Override
    public void setRenderingHints( final Map< ?, ? > hints ) {
        renderingHints.clear();
        addRenderingHints( hints );
    }

    /**
     * Adds all of the supplied Rendering Hints to the existing collection.
     *
     * @param hints
     *            The Rendering Hints to add ({@code null} not permitted).
     *
     * @since 1.0
     */
    @Override
    public void addRenderingHints( final Map< ?, ? > hints ) {
        renderingHints.putAll( hints );
    }

    /**
     * Returns the default {@link FontRenderContext} used for measuring text for
     * Vectorized Text Mode.
     *
     * @since 1.0
     */
    @Override
    public FontRenderContext getFontRenderContext() {
        return FontUtilities.getFontRenderContextForVectorization();
    }

    /**
     * Draws a Shape on the EPS Document.
     *
     * @since 1.0
     *
     * @see #fill(java.awt.Shape)
     */
    @Override
    public void draw( final Shape shape ) {
        EpsGraphicsOperators
                .epsDrawShape( epsDocument,
                               shape,
                               epsTransform,
                               EpsGraphicsUtilities.getEpsDrawMode( DrawMode.STROKE, stroke ) );
    }

    /**
     * Fills a Shape on the EPS document.
     *
     * @since 1.0
     *
     * @see #draw(java.awt.Shape)
     */
    @Override
    public void fill( final Shape shape ) {
        EpsGraphicsOperators
                .epsDrawShape( epsDocument,
                               shape,
                               epsTransform,
                               EpsGraphicsUtilities.getEpsDrawMode( DrawMode.FILL, stroke ) );
    }

    /**
     * Draws a 3D rectangle outline. If it is raised, light appears to come from
     * the top left.
     * <p>
     * This override may get removed from future library updates, as it mostly
     * matches the implementation in AWT's {@link Graphics2D} parent class.
     * <p>
     * The one exception is that we go one level brighter and darker, but that
     * is probably no longer necessary as this was just a reaction to poor user
     * feedback from the previous implementation in the legacy version of this
     * library, which for some reason did not match the AWT algorithm.
     * <p>
     * Also, the {@link Graphics2D} implementation does not restore the color
     * afterwards; it only restores the paint. And though most overrides of
     * {@link #setPaint} will also reset the color, it is with the paint value
     * and so any differences between the color and paint settings are lost.
     *
     * @since 1.0
     */
    @Override
    public void draw3DRect( final int x,
                            final int y,
                            final int width,
                            final int height,
                            final boolean raised ) {
        final Color oldColor = getColor();
        final Paint oldPaint = getPaint();

        final Color brighter = oldColor.brighter().brighter();
        final Color darker = oldColor.darker().darker();

        // Paint the "raised" or "recessed" left and top sides.
        setColor( raised ? brighter : darker );
        fillRect( x, y, 1, height + 1 );
        fillRect( x + 1, y, width - 1, 1 );

        // Conversely, paint the "recessed" or "raised" bottom and right sides
        // to complement the choices for the left and top sides and therefore
        // give the illusion of 3D.
        setColor( raised ? darker : brighter );
        fillRect( x + 1, y + height, width, 1 );
        fillRect( x + width, y, 1, height );

        setPaint( oldPaint );
        setColor( oldColor );
    }

    /**
     * Fills a 3D rectangle. If raised, it has bright fill and light appears to
     * come from the top left.
     * <p>
     * This override may get removed from future library updates, as it mostly
     * matches the implementation in AWT's {@link Graphics2D} parent class.
     * <p>
     * The one exception is that we go one level brighter and darker, but that
     * is probably no longer necessary as this was just a reaction to poor user
     * feedback from the previous implementation in the legacy version of this
     * library, which for some reason did not match the AWT algorithm.
     * <p>
     * Also, the {@link Graphics2D} implementation does not restore the color
     * afterwards; it only restores the paint. And though most overrides of
     * {@link #setPaint} will also reset the color, it is with the paint value
     * and so any differences between the color and paint settings are lost.
     *
     * @since 1.0
     */
    @Override
    public void fill3DRect( final int x,
                            final int y,
                            final int width,
                            final int height,
                            final boolean raised ) {
        final Color oldColor = getColor();
        final Paint oldPaint = getPaint();

        final Color brighter = oldColor.brighter().brighter();
        final Color darker = oldColor.darker().darker();

        // Paint the "recessed" interior, or a "transparent" interior.
        if ( !raised ) {
            setColor( darker );
        }
        else if ( !oldPaint.equals( oldColor ) ) {
            setColor( oldColor );
        }
        fillRect( x + 1, y + 1, width - 2, height - 2 );

        // Paint the "raised" or "recessed" left and top sides.
        setColor( raised ? brighter : darker );
        fillRect( x, y, 1, height );
        fillRect( x + 1, y, width - 2, 1 );

        // Conversely, paint the "recessed" or "raised" bottom and right sides
        // to complement the choices for the left and top sides and therefore
        // give the illusion of 3D.
        setColor( raised ? darker : brighter );
        fillRect( x + 1, ( y + height ) - 1, width - 1, 1 );
        fillRect( ( x + width ) - 1, y, 1, height - 1 );

        setPaint( oldPaint );
        setColor( oldColor );
    }

    /**
     * Returns a status indicator for whether the image was fully loaded and
     * rendered ({@code true} if so; {@code false} if not).
     * <p>
     * Draws an Image on the EPS document.
     *
     * @since 1.0
     */
    @Override
    public boolean drawImage( final Image img,
                              final AffineTransform xform,
                              final ImageObserver obs ) {
        // Avoid side effects if image is null.
        if ( img == null ) {
            return false;
        }

        final AffineTransform at = getTransform();
        transform( xform );
        final boolean status = drawImage( img, 0, 0, obs );
        setTransform( at );
        return status;
    }

    /**
     * Draws a BufferedImage on the EPS document.
     *
     * @since 1.0
     */
    @Override
    public void drawImage( final BufferedImage img,
                           final BufferedImageOp op,
                           final int translateX,
                           final int translateY ) {
        if ( img == null ) {
            return;
        }

        // Allow calling "drawImage" with null "op", like the JDK does.
        final BufferedImage imageToDraw = ( op != null ) ? op.filter( img, null ) : img;

        final AffineTransform translation = AffineTransform.getTranslateInstance( translateX,
                                                                                  translateY );
        drawImage( imageToDraw, translation, null );
    }

    /**
     * Draws a RenderedImage on the EPS document.
     *
     * @since 1.0
     */
    @Override
    public void drawRenderedImage( final RenderedImage renderedImage,
                                   final AffineTransform xform ) {
        final BufferedImage bufferedImage = ImageConversionUtilities
                .convertRenderedImage( renderedImage );
        drawImage( bufferedImage, xform, null );
    }

    /**
     * Draws a RenderableImage by invoking its createDefaultRendering method.
     *
     * @since 1.0
     */
    @Override
    public void drawRenderableImage( final RenderableImage img, final AffineTransform xform ) {
        if ( img == null ) {
            return;
        }

        final RenderedImage defaultRenderedImage = img.createDefaultRendering();
        drawRenderedImage( defaultRenderedImage, xform );
    }

    /**
     * Draws a string at (x,y).
     *
     * @since 1.0
     *
     * @see #drawString(java.lang.String, float, float)
     */
    @Override
    public void drawString( final String str, final int x, final int y ) {
        drawString( str, ( float ) x, ( float ) y );
    }

    /**
     * Draws a string at (x,y).
     *
     * @throws NullPointerException
     *             If the string argument is null
     *
     * @since 1.0
     */
    @Override
    public void drawString( final String string, final float x, final float y ) {
        if ( string == null ) {
            throw new NullPointerException( "Null 'string' argument." ); //$NON-NLS-1$
        }

        if ( string.isEmpty() ) {
            return;
        }

        final Font currentFont = getFont();
        final AttributedCharacterIterator attributedCharacterIterator = FontUtilities
                .getAttributeCharacterIterator( string, currentFont );

        final Object textRenderingMode =
                                       getRenderingHint( EpsRenderingHints.KEY_TEXT_RENDERING_MODE );
        if ( EpsRenderingHints.VALUE_TEXT_RENDERING_MODE_VECTOR.equals( textRenderingMode ) ) {
            drawString( attributedCharacterIterator, x, y );
        }
        else if ( EpsRenderingHints.VALUE_TEXT_RENDERING_MODE_TEXT.equals( textRenderingMode ) ) {
            // Transform the point from screen coordinates to paper coordinates.
            final Point2D location = EpsGraphicsUtilities
                    .getEpsTransformedPoint( epsTransform, x, y );
            final float x1 = ( float ) location.getX();
            final float y1 = ( float ) location.getY();

            // Draw the attributed character string to the EPS Document.
            EpsGraphicsOperators.epsDrawText( epsDocument, attributedCharacterIterator, x1, y1 );
        }
    }

    /**
     * Draws the characters of an {@link AttributedCharacterIterator}, starting
     * from (x,y), as vectored text.
     *
     * @since 1.0
     */
    @Override
    public void drawString( final AttributedCharacterIterator iterator, final int x, final int y ) {
        drawString( iterator, ( float ) x, ( float ) y );
    }

    /**
     * Draws the characters of an {@link AttributedCharacterIterator}, starting
     * from (x,y), as vectored text.
     *
     * @since 1.0
     */
    @Override
    public void drawString( final AttributedCharacterIterator attributedCharacterIterator,
                            final float x,
                            final float y ) {
        final FontRenderContext fontRenderContext = getFontRenderContext();
        final Shape shape = TextUtilities
                .getRenderedText( attributedCharacterIterator, fontRenderContext, x, y );
        fill( shape );
    }

    /**
     * Draws a GlyphVector at (x,y).
     *
     * @since 1.0
     */
    @Override
    public void drawGlyphVector( final GlyphVector glyphVector, final float x, final float y ) {
        final Shape shape = glyphVector.getOutline( x, y );
        fill( shape );
    }

    /**
     * Translates the origin of the EpsGraphics2D Graphics Context to the point
     * (x,y) in the current coordinate system.
     *
     * @since 1.0
     *
     * @see #translate(double, double)
     */
    @Override
    public void translate( final int translateX, final int translateY ) {
        translate( ( double ) translateX, ( double ) translateY );
    }

    /**
     * Concatenates the current EpsGraphics2D Transform with a translation
     * transform.
     *
     * @since 1.0
     */
    @Override
    public void translate( final double translateX, final double translateY ) {
        final AffineTransform translation = AffineTransform.getTranslateInstance( translateX,
                                                                                  translateY );
        transform( translation );
    }

    /**
     * Concatenates the current EpsGraphics2D Transform with a rotation
     * transform.
     *
     * @since 1.0
     */
    @Override
    public void rotate( final double theta ) {
        rotate( theta, 0d, 0d );
    }

    /**
     * Concatenates the current EpsGraphics2D Transform with a translated
     * rotation transform.
     *
     * @since 1.0
     */
    @Override
    public void rotate( final double theta, final double rotateX, final double rotateY ) {
        final AffineTransform rotation =
                                       AffineTransform.getRotateInstance( theta, rotateX, rotateY );
        transform( rotation );
    }

    /**
     * Concatenates the current EpsGraphics2D Transform with a scaling
     * transformation.
     *
     * @since 1.0
     */
    @Override
    public void scale( final double scaleX, final double scaleY ) {
        final AffineTransform scale = AffineTransform.getScaleInstance( scaleX, scaleY );
        transform( scale );
    }

    /**
     * Concatenates the current EpsGraphics2D Transform with a shearing
     * transform.
     *
     * @since 1.0
     */
    @Override
    public void shear( final double shearX, final double shearY ) {
        transform( AffineTransform.getShearInstance( shearX, shearY ) );
    }

    /**
     * Composes an AffineTransform object with the Transform in this
     * EpsGraphics2D according to the rule last-specified-first-applied.
     *
     * @since 1.0
     */
    @Override
    public void transform( final AffineTransform transform ) {
        final AffineTransform affine = getTransform();
        affine.concatenate( transform );
        setTransform( affine );
    }

    /**
     * Returns a copy of the {@link AffineTransform} used by this
     * {@link EpsGraphics2D} instance.
     *
     * @since 1.0
     *
     * @see #setTransform(java.awt.geom.AffineTransform)
     */
    @Override
    public AffineTransform getTransform() {
        return new AffineTransform( epsTransform );
    }

    /**
     * Sets the AffineTransform to be used by this EpsGraphics2D, or resets to
     * the identity transform if {@code null} is passed as the new transform.
     *
     * @since 1.0
     *
     * @see #getTransform()
     */
    @Override
    public void setTransform( final AffineTransform transform ) {
        if ( transform == null ) {
            epsTransform = new AffineTransform();
        }
        else {
            epsTransform = new AffineTransform( transform );
        }
    }

    /**
     * Intersects the current clip with the interior of the specified Shape and
     * sets the clip to the resulting intersection.
     *
     * @since 1.0
     */
    @Override
    public void clip( final Shape shape ) {
        if ( shape == null ) {
            setClip( null );
            return;
        }

        if ( clipArea == null ) {
            setClip( shape );
            return;
        }

        final Area area = new Area( clipArea );
        area.intersect( new Area( shape ) );
        setClip( area );
    }

    /**
     * Returns a status indicator of whether there is a hit ({@code true} if so;
     * {@code false} otherwise).
     * <p>
     * Checks whether or not the specified {@link Shape} intersects the
     * specified {@link Rectangle}, which is in device space.
     *
     * @since 1.0
     */
    @Override
    public boolean hit( final Rectangle rectangle, final Shape shape, final boolean onStroke ) {
        Shape transformedShape = onStroke ? stroke.createStrokedShape( shape ) : shape;
        transformedShape = epsTransform.createTransformedShape( transformedShape );
        final Area area = new Area( transformedShape );
        if ( clipArea != null ) {
            area.intersect( clipArea );
        }
        return area.intersects( rectangle );
    }

    ////////////////////// Graphics method overrides /////////////////////////

    /**
     * Returns a new {@link Graphics} object that is identical to this
     * {@link EpsGraphics2D} instance, with some internal cloning involved.
     *
     * @since 1.0
     */
    @Override
    public Graphics create() {
        return new EpsGraphics2D( this );
    }

    /**
     * Returns the Font currently being used.
     *
     * @since 1.0
     *
     * @see #setFont(java.awt.Font)
     */
    @Override
    public Font getFont() {
        return font;
    }

    /**
     * Sets this graphics context's {@link Font} to the specified {@link Font}.
     * All subsequent text operations using this graphics context use this
     * {@link Font}. A null argument triggers the setting of the default font.
     *
     * @param fontCandidate
     *            The new font to use; set to default font if {@code null}
     *
     * @since 1.0
     *
     * @see #getFont()
     */
    @Override
    public void setFont( final Font fontCandidate ) {
        font = ( fontCandidate != null ) ? fontCandidate : Font.decode( null );

        // Set the Font in the EPS Document, but only if not in Vectorized Text
        // Mode, as we vectorize the text vs. writing strings in that case, and
        // there's no point in potentially triggering document load errors if
        // the unused fonts aren't found or have inadequate substitute mappings.
        final Object textRenderingMode =
                                       getRenderingHint( EpsRenderingHints.KEY_TEXT_RENDERING_MODE );
        if ( EpsRenderingHints.VALUE_TEXT_RENDERING_MODE_TEXT.equals( textRenderingMode ) ) {
            EpsGraphicsOperators.epsSetfont( epsDocument, font );
        }
    }

    /**
     * Returns the {@link FontMetrics} for the specified {@link Font}.
     * <p>
     * This override may be removed from future library updates, as it exactly
     * matches the implementation in AWT's {@link Graphics} base class.
     *
     * @since 1.0
     *
     * @see #getFontMetrics(Font)
     */
    @Override
    public FontMetrics getFontMetrics() {
        final Font currentFont = getFont();
        return getFontMetrics( currentFont );
    }

    /**
     * Returns the {@link FontMetrics} for the specified {@link Font}.
     * <p>
     * This method computes the font metrics when we don't already have a
     * graphics context at hand for measuring fonts.
     *
     * @since 1.0
     */
    @Override
    public FontMetrics getFontMetrics( final Font fontToMeasure ) {
        final FontMetrics fontMetrics = FontUtilities.getFontMetrics( fontToMeasure );

        return fontMetrics;
    }

    /**
     * Returns the current clipping area as a transformed {@link Shape}.
     *
     * @return A Shape object representing the current clipping area, or
     *         {@code null} if no clip is set or if the current transform is
     *         either {@code null} or non-invertible.
     *
     * @since 1.0
     *
     * @see #setClip(java.awt.Shape)
     */
    @Override
    public Shape getClip() {
        if ( clipArea == null ) {
            return null;
        }

        final AffineTransform inverseTransform = GraphicsUtilities.getInverseMatrix( epsTransform );
        if ( inverseTransform == null ) {
            return null;
        }

        if ( clipTransform != null ) {
            inverseTransform.concatenate( clipTransform );
        }

        final Shape clipShape = inverseTransform.createTransformedShape( clipArea );

        return clipShape;
    }

    /**
     * Sets the current clipping area to an clipping shape of arbitrary shape.
     * <p>
     * Although this is a legacy method and the only one to access the {@code
     * EpsDocument.setClipSet} method, the copy constructor on this class
     * allows for several instances of {@link EpsGraphics2D} to point to the
     * same instance of {@link EpsDocument}, and thus the clipping might accrue
     * and modify from different sources at different times, meaning that it is
     * probably still best to have the {@link EpsDocument} class manage the clip
     * set flag than to move that flag and its setter/getter methods here.
     *
     * @since 1.0
     *
     * @see #getClip()
     */
    @Override
    public void setClip( final Shape clipShape ) {
        // Regardless of what we do with the new clip shape, we must pop the
        // graphics state to restore it to the previous clip.
        if ( isClipActive() ) {
            EpsGraphicsOperators.epsGrestore( epsDocument );
        }

        // If the new clip shape is invalid, turn off clip active, nullify the
        // cached clip area, and return.
        if ( clipShape == null ) {
            setClipActive( false );
            clipArea = null;
            return;
        }

        // Turn on clip active, cache the new clip area, and adjust the clip
        // transform.
        setClipActive( true );
        clipArea = new Area( clipShape );
        clipTransform = ( AffineTransform ) epsTransform.clone();

        // Push the current graphics state to encapsulate the new clip.
        EpsGraphicsOperators.epsGsave( epsDocument );

        // Draw the new clip shape, or do whatever we do in Clip Mode.
        EpsGraphicsOperators
                .epsDrawShape( epsDocument,
                               clipShape,
                               epsTransform,
                               EpsGraphicsUtilities.getEpsDrawMode( DrawMode.CLIP, stroke ) );
    }

    /**
     * Returns the bounding rectangle of the current clipping area.
     *
     * @since 1.0
     *
     * @see #getClip()
     */
    @Override
    public Rectangle getClipBounds() {
        final Shape clip = getClip();
        if ( clip == null ) {
            return null;
        }

        return clip.getBounds();
    }

    /**
     * Returns the bounding rectangle of the current clipping area.
     *
     * @since 1.0
     */
    @Override
    public Rectangle getClipBounds( final Rectangle rectangle ) {
        if ( clipArea == null ) {
            return rectangle;
        }

        // Although the Rectangle should be integer based, it is safer to assume
        // otherwise and to round to the nearest integer just in case.
        final Rectangle clipBounds = getClipBounds();
        rectangle.setLocation( ( int ) Math.round( clipBounds.getX() ),
                               ( int ) Math.round( clipBounds.getY() ) );
        rectangle.setSize( ( int ) Math.round( clipBounds.getWidth() ),
                           ( int ) Math.round( clipBounds.getHeight() ) );

        return rectangle;
    }

    /**
     * Intersects the current clip with the specified rectangle.
     *
     * @since 1.0
     */
    @Override
    public void clipRect( final int x, final int y, final int width, final int height ) {
        clip( new Rectangle( x, y, width, height ) );
    }

    /**
     * Sets the current clip to the rectangle specified by the given
     * coordinates.
     *
     * @since 1.0
     *
     * @see #setClip(java.awt.Shape)
     */
    @Override
    public void setClip( final int x, final int y, final int width, final int height ) {
        setClip( new Rectangle( x, y, width, height ) );
    }

    /**
     * Returns {@code true} if the specified rectangular area might intersect
     * the current clipping area; {@code false} otherwise.
     *
     * @since 1.0
     */
    @Override
    public boolean hitClip( final int x, final int y, final int width, final int height ) {
        if ( clipArea == null ) {
            return true;
        }

        final Rectangle rectangle = new Rectangle( x, y, width, height );
        return hit( rectangle, clipArea, true );
    }

    /**
     * Draws a straight line from (x1,y1) to (x2,y2), using integers.
     *
     * @since 1.0
     */
    @Override
    public void drawLine( final int x1, final int y1, final int x2, final int y2 ) {
        setLine( x1, y1, x2, y2 );
        draw( line );
    }

    /**
     * Fills a rectangle with top-left corner placed at (x,y).
     *
     * @since 1.0
     */
    @Override
    public void fillRect( final int x, final int y, final int width, final int height ) {
        // Avoid potentially problematic cases of inverted geometry.
        if ( ( width < 0 ) || ( height < 0 ) ) {
            return;
        }

        // Avoid potentially problematic degenerate cases.
        if ( ( width == 0 ) || ( height == 0 ) ) {
            drawLine( x, y, x + width, y + height );
        }
        else {
            setRect( x, y, width, height );
            fill( rect );
        }
    }

    /**
     * Draws a rectangle with top-left corner placed at (x,y).
     *
     * @since 1.0
     */
    @Override
    public void drawRect( final int x, final int y, final int width, final int height ) {
        // Avoid potentially problematic cases of inverted geometry.
        if ( ( width < 0 ) || ( height < 0 ) ) {
            return;
        }

        // Avoid potentially problematic degenerate cases.
        if ( ( width == 0 ) || ( height == 0 ) ) {
            drawLine( x, y, x + width, y + height );
        }
        else {
            setRect( x, y, width, height );
            draw( rect );
        }
    }

    /**
     * Clears the specified rectangular region using the current background
     * color. Has no effect if the current background color is {@code null}.
     *
     * @since 1.0
     *
     * @see #getBackground()
     */
    @Override
    public void clearRect( final int x, final int y, final int width, final int height ) {
        final Color background = getBackground();
        if ( background == null ) {
            return;
        }

        final Paint previousColor = getPaint();

        setPaint( background );
        fillRect( x, y, width, height );

        setPaint( previousColor );
    }

    /**
     * Draws a rounded rectangle.
     *
     * @since 1.0
     *
     * @see #fillRoundRect(int, int, int, int, int, int)
     */
    @Override
    public void drawRoundRect( final int x,
                               final int y,
                               final int width,
                               final int height,
                               final int arcWidth,
                               final int arcHeight ) {
        // Avoid potentially problematic cases of inverted geometry.
        if ( ( width < 0 ) || ( height < 0 ) ) {
            return;
        }

        // Avoid potentially problematic degenerate cases.
        if ( ( width == 0 ) || ( height == 0 ) ) {
            drawLine( x, y, x + width, y + height );
        }
        else {
            setRoundRect( x, y, width, height, arcWidth, arcHeight );
            draw( roundRect );
        }
    }

    /**
     * Fills a rounded rectangle.
     *
     * @since 1.0
     *
     * @see #drawRoundRect(int, int, int, int, int, int)
     */
    @Override
    public void fillRoundRect( final int x,
                               final int y,
                               final int width,
                               final int height,
                               final int arcWidth,
                               final int arcHeight ) {
        // Avoid potentially problematic cases of inverted geometry.
        if ( ( width < 0 ) || ( height < 0 ) ) {
            return;
        }

        // Avoid potentially problematic degenerate cases.
        if ( ( width == 0 ) || ( height == 0 ) ) {
            drawLine( x, y, x + width, y + height );
        }
        else {
            setRoundRect( x, y, width, height, arcWidth, arcHeight );
            fill( roundRect );
        }
    }

    /**
     * Draws an oval.
     *
     * @since 1.0
     *
     * @see #fillOval(int, int, int, int)
     */
    @Override
    public void drawOval( final int x, final int y, final int width, final int height ) {
        setOval( x, y, width, height );
        draw( oval );
    }

    /**
     * Fills an oval.
     *
     * @since 1.0
     *
     * @see #drawOval(int, int, int, int)
     */
    @Override
    public void fillOval( final int x, final int y, final int width, final int height ) {
        setOval( x, y, width, height );
        fill( oval );
    }

    /**
     * Draws an arc.
     *
     * @since 1.0
     *
     * @see #fillArc(int, int, int, int, int, int)
     */
    @Override
    public void drawArc( final int x,
                         final int y,
                         final int width,
                         final int height,
                         final int startAngle,
                         final int arcAngle ) {
        setArc( x, y, width, height, startAngle, arcAngle, Arc2D.OPEN );
        draw( arc );
    }

    /**
     * Fills an arc.
     *
     * @since 1.0
     *
     * @see #drawArc(int, int, int, int, int, int)
     */
    @Override
    public void fillArc( final int x,
                         final int y,
                         final int width,
                         final int height,
                         final int startAngle,
                         final int arcAngle ) {
        setArc( x, y, width, height, startAngle, arcAngle, Arc2D.PIE );
        fill( arc );
    }

    /**
     * Draws a polyline using integer coordinates.
     *
     * @param xPoints
     *            An integer array of x coordinates for the polyline
     * @param yPoints
     *            An integer array of x coordinates for the polyline
     * @param numberOfPoints
     *            The number of integer-based points to make for this polyline
     *
     * @since 1.0
     */
    @Override
    public void drawPolyline( final int[] xPoints, final int[] yPoints, final int numberOfPoints ) {
        final GeneralPath path = GeometryUtilities.makePolyline( xPoints, yPoints, numberOfPoints );
        draw( path );
    }

    /**
     * Draws a polygon made with the specified points.
     *
     * @since 1.0
     *
     * @see #fillPolygon(int[], int[], int)
     */
    @Override
    public void drawPolygon( final int[] xPoints, final int[] yPoints, final int nPoints ) {
        final Polygon polygon = new Polygon( xPoints, yPoints, nPoints );
        draw( polygon );
    }

    /**
     * Draws a polygon.
     *
     * @since 1.0
     *
     * @see #drawPolygon(int[], int[], int)
     */
    @Override
    public void drawPolygon( final Polygon polygon ) {
        draw( polygon );
    }

    /**
     * Fills a polygon made with the specified points.
     *
     * @since 1.0
     */
    @Override
    public void fillPolygon( final int[] xPoints, final int[] yPoints, final int nPoints ) {
        final Polygon polygon = new Polygon( xPoints, yPoints, nPoints );
        fill( polygon );
    }

    /**
     * Fills a polygon.
     *
     * @since 1.0
     */
    @Override
    public void fillPolygon( final Polygon polygon ) {
        fill( polygon );
    }

    /**
     * Draws the specified characters, starting from (x,y).
     * <p>
     * This override will be removed from future library updates, as it exactly
     * matches the implementation in AWT's {@link Graphics} base class.
     *
     * @since 1.0
     *
     * @see #drawString(java.lang.String, int, int)
     */
    @Override
    public void drawChars( final char[] data,
                           final int offset,
                           final int length,
                           final int x,
                           final int y ) {
        final String string = new String( data, offset, length );
        drawString( string, x, y );
    }

    /**
     * Draws the specified bytes, starting from (x,y).
     *
     * @since 1.0
     *
     * @see #drawString(java.lang.String, int, int)
     */
    @Override
    public void drawBytes( final byte[] data,
                           final int offset,
                           final int length,
                           final int x,
                           final int y ) {
        final String string = new String( data, offset, length );
        drawString( string, x, y );
    }

    /**
     * Draws an image.
     *
     * @since 1.0
     */
    @Override
    public boolean drawImage( final Image img,
                              final int x,
                              final int y,
                              final ImageObserver observer ) {
        return drawImage( img, x, y, Color.WHITE, observer );
    }

    /**
     * Draws an image.
     *
     * @since 1.0
     */
    @Override
    public boolean drawImage( final Image img,
                              final int x,
                              final int y,
                              final int width,
                              final int height,
                              final ImageObserver observer ) {
        return drawImage( img, x, y, width, height, Color.WHITE, observer );
    }

    /**
     * Draws an image.
     *
     * @since 1.0
     */
    @Override
    public boolean drawImage( final Image img,
                              final int x,
                              final int y,
                              final Color bgcolor,
                              final ImageObserver observer ) {
        final int width = img.getWidth( null );
        final int height = img.getHeight( null );
        return drawImage( img, x, y, width, height, bgcolor, observer );
    }

    /**
     * Draws an image.
     *
     * @since 1.0
     */
    @Override
    public boolean drawImage( final Image img,
                              final int x,
                              final int y,
                              final int width,
                              final int height,
                              final Color bgcolor,
                              final ImageObserver observer ) {
        return drawImage( img,
                          x,
                          y,
                          x + width,
                          y + height,
                          0,
                          0,
                          width,
                          height,
                          bgcolor,
                          observer );
    }

    /**
     * Draws an image.
     *
     * @since 1.0
     */
    @Override
    public boolean drawImage( final Image img,
                              final int dx1,
                              final int dy1,
                              final int dx2,
                              final int dy2,
                              final int sx1,
                              final int sy1,
                              final int sx2,
                              final int sy2,
                              final ImageObserver observer ) {
        return drawImage( img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, Color.WHITE, observer );
    }

    /**
     * Draws an image.
     *
     * @since 1.0
     */
    @Override
    public boolean drawImage( final Image img,
                              final int dx1,
                              final int dy1,
                              final int dx2,
                              final int dy2,
                              final int sx1,
                              final int sy1,
                              final int sx2,
                              final int sy2,
                              final Color bgcolor,
                              final ImageObserver observer ) {
        final int width = sx2 - sx1;
        if ( width <= 0 ) {
            return false;
        }

        final int height = sy2 - sy1;
        if ( height <= 0 ) {
            return false;
        }

        final int destWidth = dx2 - dx1;
        if ( destWidth <= 0 ) {
            return false;
        }

        final int destHeight = dy2 - dy1;
        if ( destHeight <= 0 ) {
            return false;
        }

        // Convert the image to a two-dimensional array of integer-based pixels.
        // Each pixel is considered a "sample" in PostScript terminology.
        final int[] pixels =
                           ImageConversionUtilities.getImagePixels( img, sx1, sy1, width, height );
        if ( ( pixels == null ) || ( pixels.length <= 0 ) ) {
            return false;
        }

        // Compute the user space to image space transformation matrix.
        //
        // This matrix does not have to conform to invertibility rules, as only
        // graphics and font operators can get an "undefinedresult" in such
        // cases. See section 4.3 on p. 189 of the PostScript Reference Manual:
        //
        // https://www-cdf.fnal.gov/offline/PostScript/PLRM3.pdf
        final AffineTransform matrix = new AffineTransform( epsTransform );
        matrix.translate( dx1, dy1 );
        matrix.scale( destWidth / ( double ) width, destHeight / ( double ) height );
        try {
            // Use the self-modifying version of this method added in Java
            // 1.6, as it allows for cleaner error handling due to any run-time
            // errors or exceptions simply leaving the original matrix as-is.
            matrix.invert();
        }
        catch ( final NoninvertibleTransformException nte ) {
            // There is nothing to do here, as the original matrix is unaffected
            // and is still allowed for use in the context of a PostScript
            // "image" or "colorimage" operator. If Logging is added at some
            // point, it might make sense to log this exception.
        }

        // There is also a vertical axis inversion from screen to page output.
        matrix.scale( 1, -1 );

        // PostScript only uses the two left columns of {@link AffineTransform}.
        final double[] imageMatrix = new double[ 6 ];
        matrix.getMatrix( imageMatrix );

        // All image data uses 8 bits per sample, which corresponds to two-digit
        // hexadecimal strings concatenated together
        final int bitsPerSample = 8;

        // Save the Graphics State before writing the image, due to temporary
        // settings that might differ from fonts and strokes.
        EpsGraphicsOperators.epsGsave( epsDocument );

        // Write the image operator preamble (dimensions and transform matrix).
        EpsImageOperators.writeEpsImageOperatorPreamble( epsDocument,
                                                         width,
                                                         height,
                                                         bitsPerSample,
                                                         imageMatrix );

        // Fill the background to update the bounding box.
        //
        // This is here only to match what every other implementation does, but
        // it seems suspicious as it doesn't correspond to any examples for
        // proper PostScript image handling provided by Adobe themselves in
        // their language specifications and user guides. Furthermore, it
        // interrupts the parameter subsets of the PostScript image commands.
        // Without this code in the way, we could wrap the entire EPS image
        // writing code in a library method outside this class.
        //
        // Perhaps the EPS version of this command differs from the standard
        // PostScript version and requires the background filling to occur
        // inside the image command parameters as is done here, after the image
        // matrix and before the image procedure and image contents are written?
        // It is unfortunate that the EPS guide doesn't give more details about
        // its modifications to the image operators; even the "bind" tagged to
        // the image procedure (parameter subset) was implied vs. stated
        // explicitly in the official Encapsulated PostScript specifications.
        //
        // This needs further review, but it is hard to come up with trustworthy
        // test conditions to gain full confidence after a code change, so this
        // has been deferred past the initial Version 1.0 of this library.
        clearRect( dx1, dy1, destWidth, destHeight );

        // Now it is safe to grab the current Color Mode.
        final ColorMode colorMode = getColorMode();

        // Write the main image operator parameters (procedure and contents).
        EpsImageOperators
                .writeEpsImageOperatorParameters( epsDocument, width, height, pixels, colorMode );

        // Restore the Graphics State after writing the image, due to temporary
        // settings that might have differed from fonts and strokes.
        EpsGraphicsOperators.epsGrestore( epsDocument );

        return true;
    }

    /**
     * This method does nothing in this particular implementation.
     * <p>
     * The "copy area" operation assumes that the output is in bitmap form,
     * which is not the case for EPS output, so we silently ignore this method
     * call.
     *
     * @param x
     *            The x-coordinate of the area to be copied
     * @param y
     *            The y-coordinate of the area to be copied
     * @param width
     *            The width of the area to be copied
     * @param height
     *            The height of the area to be copied
     * @param dx
     *            The delta x for the copied data
     * @param dy
     *            The delta y for the copied data
     *
     * @since 1.0
     */
    @Override
    public void copyArea( final int x,
                          final int y,
                          final int width,
                          final int height,
                          final int dx,
                          final int dy ) {
        // Do nothing; this operation is silently ignored in the EPS context.
    }

    /**
     * Sets the Paint Mode of this {@link EpsGraphics2D} object to overwrite the
     * destination {@link EpsDocument} with the current foreground paint color.
     * <p>
     * This method does nothing in the EPS context as EPS doesn't support it.
     *
     * @since 1.0
     */
    @Override
    public void setPaintMode() {
        // Do nothing; paint overwrite mode is the only method supported anyway.
    }

    /**
     * Sets the Paint Mode of this {@link EpsGraphics2D} object to alternate the
     * destination {@link EpsDocument} between the supplied new color and the
     * current foreground paint color.
     * <p>
     * This method does nothing in the EPS context as EPS doesn't support it.
     *
     * @since 1.0
     */
    @Override
    public void setXORMode( final Color color ) {
        // Do nothing; paint overwrite mode is the only method supported anyway.
    }

    /**
     * This method should do nothing, there are no resources to dispose.
     *
     * @since 1.0
     */
    @Override
    public void dispose() {
        // Nothing to do; there are no resources to dispose.
    }

}
