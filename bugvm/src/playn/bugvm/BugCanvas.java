/**
 * Copyright 2014 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playn.bugvm;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.bugvm.apple.coregraphics.CGAffineTransform;
import com.bugvm.apple.coregraphics.CGBitmapContext;
import com.bugvm.apple.coregraphics.CGBlendMode;
import com.bugvm.apple.coregraphics.CGImage;
import com.bugvm.apple.coregraphics.CGLineCap;
import com.bugvm.apple.coregraphics.CGLineJoin;
import com.bugvm.apple.coregraphics.CGMutablePath;
import com.bugvm.apple.coregraphics.CGRect;
import com.bugvm.apple.coregraphics.CGTextEncoding;
import com.bugvm.apple.coretext.CTFont;

import playn.core.*;

/**
 * Implements {@link Canvas}.
 */
public class BugCanvas extends Canvas {

  private float strokeWidth = 1;
  private int strokeColor = 0xFF000000;
  private int fillColor = 0xFF000000;
  private CGBitmapContext bctx;

  private LinkedList<BugCanvasState> states = new LinkedList<BugCanvasState>();

  public BugCanvas(Graphics gfx, BugCanvasImage image) {
    super(gfx, image);

    // if our size is invalid, we'll fail below at CGBitmapContext, so fail here more usefully
    if (width <= 0 || height <= 0) throw new IllegalArgumentException(
      "Invalid size " + width + "x" + height);
    states.addFirst(new BugCanvasState());

    bctx = image.bctx;
    // clear the canvas before we scale our bitmap context to avoid artifacts
    bctx.clearRect(new CGRect(0, 0, texWidth(), texHeight()));

    // CG coordinate system is OpenGL-style (0,0 in lower left); so we flip it
    Scale scale = image.scale();
    bctx.translateCTM(0, scale.scaled(height));
    bctx.scaleCTM(scale.factor, -scale.factor);
  }

  public int texWidth() { return image.pixelWidth(); }
  public int texHeight() { return image.pixelHeight(); }

  // todo: make sure the image created by this call doesn't require any manual resource
  // releasing, other than being eventually garbage collected
  public CGImage cgimage() { return bctx.toImage(); }

  @Override public Canvas clear() {
    bctx.clearRect(new CGRect(0, 0, texWidth(), texHeight()));
    isDirty = true;
    return this;
  }

  @Override public Canvas clearRect(float x, float y, float width, float height) {
    bctx.clearRect(new CGRect(x, y, width, height));
    isDirty = true;
    return this;
  }

  @Override public Canvas clip(Path clipPath) {
    bctx.addPath(((BugPath) clipPath).cgPath);
    bctx.clip();
    return this;
  }

  @Override public Canvas clipRect(float x, float y, float width, float height) {
    bctx.clipToRect(new CGRect(x, y, width, height));
    return this;
  }

  @Override public Path createPath() {
    return new BugPath();
  }

  @Override public Gradient createGradient(Gradient.Config cfg) {
    if (cfg instanceof Gradient.Linear) return new BugGradient.Linear((Gradient.Linear)cfg);
    else if (cfg instanceof Gradient.Radial) return new BugGradient.Radial((Gradient.Radial)cfg);
    else throw new IllegalArgumentException("Unknown config: " + cfg);
  }

  @Override public void close () {
    ((BugCanvasImage)image).dispose();
  }

  @Override public Canvas drawLine(float x0, float y0, float x1, float y1) {
    bctx.beginPath();
    bctx.moveToPoint(x0, y0);
    bctx.addLineToPoint(x1, y1);
    bctx.strokePath();
    isDirty = true;
    return this;
  }

  @Override public Canvas drawPoint(float x, float y) {
    save();
    setStrokeWidth(0.5f);
    strokeRect(x + 0.25f, y + 0.25f, 0.5f, 0.5f);
    restore();
    return this;
  }

  @Override public Canvas drawText(String text, float x, float y) {
    CTFont font = BugFont.resolveFont(null); // default font
    bctx.saveGState();
    bctx.translateCTM(x, y + font.getDescent());
    bctx.scaleCTM(1, -1);
    bctx.selectFont(font.getPostScriptName(), font.getSize(), CGTextEncoding.MacRoman);
    bctx.showTextAtPoint(0, 0, text);
    bctx.restoreGState();
    isDirty = true;
    return this;
  }

  @Override public Canvas fillCircle(float x, float y, float radius) {
    BugGradient gradient = currentState().gradient;
    if (gradient == null) {
      bctx.fillEllipseInRect(new CGRect(x-radius, y-radius, 2*radius, 2*radius));
    } else {
      CGMutablePath cgPath = CGMutablePath.createMutable();
      cgPath.addArc(null, x, y, radius, 0, 2*Math.PI, false);
      bctx.addPath(cgPath);
      bctx.clip();
      gradient.fill(bctx);
    }
    isDirty = true;
    return this;
  }

  @Override public Canvas fillPath(Path path) {
    bctx.addPath(((BugPath) path).cgPath);
    BugGradient gradient = currentState().gradient;
    if (gradient == null) {
      bctx.fillPath();
    } else {
      bctx.clip();
      gradient.fill(bctx);
    }
    isDirty = true;
    return this;
  }

  @Override public Canvas fillRect(float x, float y, float width, float height) {
    BugGradient gradient = currentState().gradient;
    if (gradient == null) {
      bctx.fillRect(new CGRect(x, y, width, height));
    } else {
      bctx.saveGState();
      bctx.clipToRect(new CGRect(x, y, width, height));
      gradient.fill(bctx);
      bctx.restoreGState();
    }
    isDirty = true;
    return this;
  }

  @Override public Canvas fillRoundRect(float x, float y, float width, float height, float radius) {
    addRoundRectPath(x, y, width, height, radius);
    BugGradient gradient = currentState().gradient;
    if (gradient == null) {
      bctx.fillPath();
    } else {
      bctx.clip();
      gradient.fill(bctx);
    }
    isDirty = true;
    return this;
  }

  @Override public Canvas fillText(TextLayout layout, float x, float y) {
    BugGradient gradient = currentState().gradient;
    BugTextLayout ilayout = (BugTextLayout) layout;
    if (gradient == null) {
      ilayout.fill(bctx, x, y, fillColor);

    } else {
      // draw our text into a fresh context so we can use it as a mask for the gradient
      CGBitmapContext maskContext = BugGraphics.createCGBitmap(texWidth(), texHeight());
      maskContext.clearRect(new CGRect(0, 0, texWidth(), texHeight()));
      // scale the context based on our scale factor
      float scale = image.scale().factor;
      maskContext.scaleCTM(scale, scale);
      // fill the text into this temp context in white for use as a mask
      setFillColor(maskContext, 0xFFFFFFFF);
      ilayout.fill(maskContext, 0, 0, fillColor);

      // now fill the gradient, using our temp context as a mask
      bctx.saveGState();
      bctx.clipToMask(new CGRect(x, y, width, height), maskContext.toImage());
      gradient.fill(bctx);
      bctx.restoreGState();

      // finally free the temp context
      maskContext.dispose();
    }

    isDirty = true;
    return this;
  }

  @Override public Canvas restore() {
    states.removeFirst();
    bctx.restoreGState();
    return this;
  }

  @Override public Canvas rotate(float radians) {
    bctx.rotateCTM(radians);
    return this;
  }

  @Override public Canvas save() {
    states.addFirst(new BugCanvasState(currentState()));
    bctx.saveGState();
    return this;
  }

  @Override public Canvas scale(float x, float y) {
    bctx.scaleCTM(x, y);
    return this;
  }

  @Override public Canvas setAlpha(float alpha) {
    bctx.setAlpha(alpha);
    return this;
  }

  @Override public Canvas setCompositeOperation(Composite composite) {
    bctx.setBlendMode(compToBlend.get(composite));
    return this;
  }

  @Override public Canvas setFillColor(int color) {
    this.fillColor = color;
    currentState().gradient = null;
    setFillColor(bctx, color);
    return this;
  }

  @Override public Canvas setFillGradient(Gradient gradient) {
    currentState().gradient = (BugGradient) gradient;
    return this;
  }

  @Override public Canvas setFillPattern(Pattern pattern) {
    currentState().gradient = null;
    // TODO: this anchors the fill pattern in the lower left; sigh
    bctx.setFillColor(((BugPattern) pattern).colorWithPattern);
    return this;
  }

  @Override public Canvas setLineCap(LineCap cap) {
    bctx.setLineCap(decodeCap.get(cap));
    return this;
  }

  @Override public Canvas setLineJoin(LineJoin join) {
    bctx.setLineJoin(decodeJoin.get(join));
    return this;
  }

  @Override public Canvas setMiterLimit(float miter) {
    bctx.setMiterLimit(miter);
    return this;
  }

  @Override public Canvas setStrokeColor(int color) {
    this.strokeColor = color;
    setStrokeColor(bctx, color);
    return this;
  }

  @Override public Canvas setStrokeWidth(float strokeWidth) {
    this.strokeWidth = strokeWidth;
    bctx.setLineWidth(strokeWidth);
    return this;
  }

  @Override public Image snapshot() {
    return new BugImage(gfx, image.scale(), ((BugImage)image).cgImage(), "<canvas>");
  }

  @Override public Canvas strokeCircle(float x, float y, float radius) {
    bctx.strokeEllipseInRect(new CGRect(x-radius, y-radius, 2*radius, 2*radius));
    isDirty = true;
    return this;
  }

  @Override
  public Canvas strokePath(Path path) {
    bctx.addPath(((BugPath) path).cgPath);
    bctx.strokePath();
    isDirty = true;
    return this;
  }

  @Override public Canvas strokeRect(float x, float y, float width, float height) {
    bctx.strokeRect(new CGRect(x, y, width, height));
    isDirty = true;
    return this;
  }

  @Override public Canvas strokeRoundRect(float x, float y, float width, float height,
                                          float radius) {
    addRoundRectPath(x, y, width, height, radius);
    bctx.strokePath();
    isDirty = true;
    return this;
  }

  @Override public Canvas strokeText(TextLayout layout, float x, float y) {
    ((BugTextLayout) layout).stroke(bctx, x, y, strokeWidth, strokeColor);
    isDirty = true;
    return this;
  }

  @Override public Canvas transform(float m11, float m12, float m21, float m22,
                                    float dx, float dy) {
    bctx.concatCTM(new CGAffineTransform(m11, m12, m21, m22, dx, dy));
    return this;
  }

  @Override public Canvas translate(float x, float y) {
    bctx.translateCTM(x, y);
    return this;
  }

  @Override protected CGBitmapContext gc() {
    return bctx;
  }

  private void addRoundRectPath(float x, float y, float width, float height, float radius) {
    float midx = x + width/2, midy = y + height/2, maxx = x + width, maxy = y + height;
    bctx.beginPath();
    bctx.moveToPoint(x, midy);
    bctx.addArcToPoint(x, y, midx, y, radius);
    bctx.addArcToPoint(maxx, y, maxx, midy, radius);
    bctx.addArcToPoint(maxx, maxy, midx, maxy, radius);
    bctx.addArcToPoint(x, maxy, x, midy, radius);
    bctx.closePath();
  }

  private BugCanvasState currentState() {
    return states.peek();
  }

  static void setStrokeColor(CGBitmapContext bctx, int color) {
    float blue = (color & 0xFF) / 255f;
    color >>= 8;
    float green = (color & 0xFF) / 255f;
    color >>= 8;
    float red = (color & 0xFF) / 255f;
    color >>= 8;
    float alpha = (color & 0xFF) / 255f;
    bctx.setRGBStrokeColor(red, green, blue, alpha);
  }

  static void setFillColor(CGBitmapContext bctx, int color) {
    float blue = (color & 0xFF) / 255f;
    color >>= 8;
    float green = (color & 0xFF) / 255f;
    color >>= 8;
    float red = (color & 0xFF) / 255f;
    color >>= 8;
    float alpha = (color & 0xFF) / 255f;
    bctx.setRGBFillColor(red, green, blue, alpha);
  }

  private static Map<Composite,CGBlendMode> compToBlend = new HashMap<Composite,CGBlendMode>();
  static {
    compToBlend.put(Composite.SRC, CGBlendMode.Copy);
    compToBlend.put(Composite.DST_ATOP, CGBlendMode.DestinationAtop);
    compToBlend.put(Composite.SRC_OVER, CGBlendMode.Normal);
    compToBlend.put(Composite.DST_OVER, CGBlendMode.DestinationOver);
    compToBlend.put(Composite.SRC_IN, CGBlendMode.SourceIn);
    compToBlend.put(Composite.DST_IN, CGBlendMode.DestinationIn);
    compToBlend.put(Composite.SRC_OUT, CGBlendMode.SourceOut);
    compToBlend.put(Composite.DST_OUT, CGBlendMode.DestinationOut);
    compToBlend.put(Composite.SRC_ATOP, CGBlendMode.SourceAtop);
    compToBlend.put(Composite.XOR, CGBlendMode.XOR);
    compToBlend.put(Composite.MULTIPLY, CGBlendMode.Multiply);
  }

  private static Map<LineCap,CGLineCap> decodeCap = new HashMap<LineCap,CGLineCap>();
  static {
    decodeCap.put(LineCap.BUTT, CGLineCap.Butt);
    decodeCap.put(LineCap.ROUND, CGLineCap.Round);
    decodeCap.put(LineCap.SQUARE, CGLineCap.Square);
  }

  private static Map<LineJoin,CGLineJoin> decodeJoin = new HashMap<LineJoin,CGLineJoin>();
  static {
    decodeJoin.put(LineJoin.BEVEL, CGLineJoin.Bevel);
    decodeJoin.put(LineJoin.MITER, CGLineJoin.Miter);
    decodeJoin.put(LineJoin.ROUND, CGLineJoin.Round);
  }
}
