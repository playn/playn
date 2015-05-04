/**
 * Copyright 2010 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playn.java;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.Deque;
import java.util.LinkedList;

import playn.core.*;
import pythagoras.f.MathUtil;

class JavaCanvas extends Canvas {

  final Graphics2D g2d;
  private Deque<JavaCanvasState> stateStack = new LinkedList<JavaCanvasState>();

  private Ellipse2D.Float ellipse = new Ellipse2D.Float();
  private Line2D.Float line = new Line2D.Float();
  private Rectangle2D.Float rect = new Rectangle2D.Float();
  private RoundRectangle2D.Float roundRect = new RoundRectangle2D.Float();

  public JavaCanvas (Graphics gfx, JavaImage image) {
    super(gfx, image);

    g2d = image.bufferedImage().createGraphics();
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    float scale = image.scale().factor;
    g2d.scale(scale, scale);

    // push default state
    stateStack.push(new JavaCanvasState());

    // All clears go to rgba(0,0,0,0).
    g2d.setBackground(new Color(0, true));
  }

  public float alpha() {
    return currentState().alpha;
  }

  @Override
  public Image snapshot() {
    BufferedImage bmp = ((JavaImage)image).bufferedImage();
    ColorModel cm = bmp.getColorModel();
    boolean isAlphaPremultiplied = bmp.isAlphaPremultiplied();
    WritableRaster raster = bmp.copyData(null);
    BufferedImage snap = new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    return new JavaImage(gfx, image.scale(), snap, "<canvas>");
  }

  @Override
  public Canvas clear() {
    currentState().prepareClear(g2d);
    g2d.clearRect(0, 0, MathUtil.iceil(width), MathUtil.iceil(height));
    isDirty = true;
    return this;
  }

  @Override
  public Canvas clearRect(float x, float y, float width, float height) {
    currentState().prepareClear(g2d);
    g2d.clearRect(MathUtil.ifloor(x), MathUtil.ifloor(y),
                  MathUtil.iceil(width), MathUtil.iceil(height));
    isDirty = true;
    return this;
  }

  @Override
  public Canvas clip(Path path) {
    currentState().clipper = (JavaPath) path;
    return this;
  }

  @Override
  public Canvas clipRect(float x, float y, float width, final float height) {
    final int cx = MathUtil.ifloor(x), cy = MathUtil.ifloor(y);
    final int cwidth = MathUtil.iceil(width), cheight = MathUtil.iceil(height);
    currentState().clipper = new JavaCanvasState.Clipper() {
      public void setClip(Graphics2D g2d) { g2d.setClip(cx, cy, cwidth, cheight); }
    };
    return this;
  }

  @Override
  public Path createPath() {
    return new JavaPath();
  }

  @Override public Gradient createGradient(Gradient.Config cfg) {
    if (cfg instanceof Gradient.Linear) return JavaGradient.create((Gradient.Linear)cfg);
    else if (cfg instanceof Gradient.Radial) return JavaGradient.create((Gradient.Radial)cfg);
    else throw new IllegalArgumentException("Unknown config: " + cfg);
  }

  @Override
  public Canvas drawLine(float x0, float y0, float x1, float y1) {
    currentState().prepareStroke(g2d);
    line.setLine(x0, y0, x1, y1);
    g2d.draw(line);
    isDirty = true;
    return this;
  }

  @Override
  public Canvas drawPoint(float x, float y) {
    currentState().prepareStroke(g2d);
    g2d.drawLine((int) x, (int) y, (int) x, (int) y);
    isDirty = true;
    return this;
  }

  @Override
  public Canvas drawText(String text, float x, float y) {
    currentState().prepareFill(g2d);
    g2d.drawString(text, x, y);
    isDirty = true;
    return this;
  }

  @Override
  public Canvas fillCircle(float x, float y, float radius) {
    currentState().prepareFill(g2d);
    ellipse.setFrame(x - radius, y - radius, 2 * radius, 2 * radius);
    g2d.fill(ellipse);
    isDirty = true;
    return this;
  }

  @Override
  public Canvas fillPath(Path path) {
    currentState().prepareFill(g2d);
    g2d.fill(((JavaPath) path).path);
    isDirty = true;
    return this;
  }

  @Override
  public Canvas fillRect(float x, float y, float width, float height) {
    currentState().prepareFill(g2d);
    rect.setRect(x, y, width, height);
    g2d.fill(rect);
    isDirty = true;
    return this;
  }

  @Override
  public Canvas fillRoundRect(float x, float y, float width, float height, float radius) {
    currentState().prepareFill(g2d);
    roundRect.setRoundRect(x, y, width, height, radius*2, radius*2);
    g2d.fill(roundRect);
    isDirty = true;
    return this;
  }

  @Override
  public Canvas fillText(TextLayout layout, float x, float y) {
    currentState().prepareFill(g2d);
    ((JavaTextLayout)layout).fill(g2d, x, y);
    isDirty = true;
    return this;
  }

  @Override
  public Canvas restore() {
    stateStack.pop();
    g2d.setTransform(currentState().transform);
    return this;
  }

  @Override
  public Canvas rotate(float angle) {
    g2d.rotate(angle);
    return this;
  }

  @Override
  public Canvas save() {
    // update saved transform
    currentState().transform = g2d.getTransform();

    // clone to maintain current state
    stateStack.push(new JavaCanvasState(currentState()));
    return this;
  }

  @Override
  public Canvas scale(float x, float y) {
    g2d.scale(x, y);
    return this;
  }

  public Canvas setAlpha(float alpha) {
    currentState().alpha = alpha;
    return this;
  }

  @Override
  public Canvas setCompositeOperation(Composite composite) {
    currentState().composite = composite;
    return this;
  }

  @Override
  public Canvas setFillColor(int color) {
    currentState().fillColor = color;
    currentState().fillGradient = null;
    currentState().fillPattern = null;
    return this;
  }

  @Override
  public Canvas setFillGradient(Gradient gradient) {
    currentState().fillGradient = (JavaGradient) gradient;
    currentState().fillPattern = null;
    currentState().fillColor = 0;
    return this;
  }

  @Override
  public Canvas setFillPattern(Pattern pattern) {
    currentState().fillPattern = (JavaPattern) pattern;
    currentState().fillGradient = null;
    currentState().fillColor = 0;
    return this;
  }

  @Override
  public Canvas setLineCap(LineCap cap) {
    currentState().lineCap = cap;
    return this;
  }

  @Override
  public Canvas setLineJoin(LineJoin join) {
    currentState().lineJoin = join;
    return this;
  }

  @Override
  public Canvas setMiterLimit(float miter) {
    currentState().miterLimit = miter;
    return this;
  }

  @Override
  public Canvas setStrokeColor(int color) {
    currentState().strokeColor = color;
    return this;
  }

  @Override
  public Canvas setStrokeWidth(float w) {
    currentState().strokeWidth = w;
    return this;
  }

  @Override
  public Canvas strokeCircle(float x, float y, float radius) {
    currentState().prepareStroke(g2d);
    ellipse.setFrame(x - radius, y - radius, 2 * radius, 2 * radius);
    g2d.draw(ellipse);
    isDirty = true;
    return this;
  }

  @Override
  public Canvas strokePath(Path path) {
    currentState().prepareStroke(g2d);
    g2d.setColor(new Color(currentState().strokeColor, false));
    g2d.draw(((JavaPath) path).path);
    isDirty = true;
    return this;
  }

  @Override
  public Canvas strokeRect(float x, float y, float width, float height) {
    currentState().prepareStroke(g2d);
    rect.setRect(x, y, width, height);
    g2d.draw(rect);
    isDirty = true;
    return this;
  }

  @Override
  public Canvas strokeRoundRect(float x, float y, float width, float height, float radius) {
    currentState().prepareStroke(g2d);
    roundRect.setRoundRect(x, y, width, height, radius*2, radius*2);
    g2d.draw(roundRect);
    isDirty = true;
    return this;
  }

  @Override
  public Canvas strokeText(TextLayout layout, float x, float y) {
    currentState().prepareStroke(g2d);
    ((JavaTextLayout)layout).stroke(g2d, x, y);
    isDirty = true;
    return this;
  }

  @Override
  public Canvas transform(float m11, float m12, float m21, float m22, float dx, float dy) {
    g2d.transform(new AffineTransform(m11, m12, m21, m22, dx, dy));
    return this;
  }

  @Override
  public Canvas translate(float x, float y) {
    g2d.translate(x, y);
    return this;
  }

  @Override
  protected Graphics2D gc() {
    currentState().prepareFill(g2d);
    return g2d;
  }

  private JavaCanvasState currentState() {
    return stateStack.getFirst();
  }
}
