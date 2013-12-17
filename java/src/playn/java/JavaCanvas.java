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

import pythagoras.f.MathUtil;

import playn.core.Asserts;
import playn.core.Canvas;
import playn.core.Gradient;
import playn.core.Path;
import playn.core.Pattern;
import playn.core.TextLayout;
import playn.core.gl.AbstractCanvasGL;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Deque;
import java.util.LinkedList;

class JavaCanvas extends AbstractCanvasGL<Graphics2D> {

  interface Drawable {
    void fill(Graphics2D gfx, float x, float y);
    void stroke(Graphics2D gfx, float x, float y);
  }

  final Graphics2D gfx;
  private Deque<JavaCanvasState> stateStack = new LinkedList<JavaCanvasState>();

  private Ellipse2D.Float ellipse = new Ellipse2D.Float();
  private Line2D.Float line = new Line2D.Float();
  private Rectangle2D.Float rect = new Rectangle2D.Float();
  private RoundRectangle2D.Float roundRect = new RoundRectangle2D.Float();

  JavaCanvas(Graphics2D graphics, float width, float height) {
    super(width, height);
    this.gfx = graphics;

    // push default state
    stateStack.push(new JavaCanvasState());

    // All clears go to rgba(0,0,0,0).
    gfx.setBackground(new Color(0, true));
  }

  public float alpha() {
    return currentState().alpha;
  }

  @Override
  public Canvas clear() {
    gfx.clearRect(0, 0, MathUtil.iceil(width), MathUtil.iceil(height));
    isDirty = true;
    return this;
  }

  @Override
  public Canvas clearRect(float x, float y, float width, float height) {
    gfx.clearRect(MathUtil.ifloor(x), MathUtil.ifloor(y),
                  MathUtil.iceil(width), MathUtil.iceil(height));
    isDirty = true;
    return this;
  }

  @Override
  public Canvas clip(Path path) {
    Asserts.checkArgument(path instanceof JavaPath);
    currentState().clipper = (JavaPath) path;
    return this;
  }

  @Override
  public Canvas clipRect(float x, float y, float width, final float height) {
    final int cx = MathUtil.ifloor(x), cy = MathUtil.ifloor(y);
    final int cwidth = MathUtil.iceil(width), cheight = MathUtil.iceil(height);
    currentState().clipper = new JavaCanvasState.Clipper() {
      public void setClip(Graphics2D gfx) {
        gfx.clipRect(cx, cy, cwidth, cheight);
      }
    };
    return this;
  }

  @Override
  public Path createPath() {
    return new JavaPath();
  }

  @Override
  public Canvas drawLine(float x0, float y0, float x1, float y1) {
    currentState().prepareStroke(gfx);
    line.setLine(x0, y0, x1, y1);
    gfx.draw(line);
    isDirty = true;
    return this;
  }

  @Override
  public Canvas drawPoint(float x, float y) {
    currentState().prepareStroke(gfx);
    gfx.drawLine((int) x, (int) y, (int) x, (int) y);
    isDirty = true;
    return this;
  }

  @Override
  public Canvas drawText(String text, float x, float y) {
    currentState().prepareFill(gfx);
    gfx.drawString(text, x, y);
    isDirty = true;
    return this;
  }

  @Override
  public Canvas fillCircle(float x, float y, float radius) {
    currentState().prepareFill(gfx);
    ellipse.setFrame(x - radius, y - radius, 2 * radius, 2 * radius);
    gfx.fill(ellipse);
    isDirty = true;
    return this;
  }

  @Override
  public Canvas fillPath(Path path) {
    Asserts.checkArgument(path instanceof JavaPath);

    currentState().prepareFill(gfx);
    gfx.fill(((JavaPath) path).path);
    isDirty = true;
    return this;
  }

  @Override
  public Canvas fillRect(float x, float y, float width, float height) {
    currentState().prepareFill(gfx);
    rect.setRect(x, y, width, height);
    gfx.fill(rect);
    isDirty = true;
    return this;
  }

  @Override
  public Canvas fillRoundRect(float x, float y, float width, float height, float radius) {
    currentState().prepareFill(gfx);
    roundRect.setRoundRect(x, y, width, height, radius*2, radius*2);
    gfx.fill(roundRect);
    isDirty = true;
    return this;
  }

  @Override
  public Canvas fillText(TextLayout layout, float x, float y) {
    currentState().prepareFill(gfx);
    ((Drawable)layout).fill(gfx, x, y);
    isDirty = true;
    return this;
  }

  @Override
  public Canvas restore() {
    stateStack.pop();
    gfx.setTransform(currentState().transform);
    return this;
  }

  @Override
  public Canvas rotate(float angle) {
    gfx.rotate(angle);
    return this;
  }

  @Override
  public Canvas save() {
    // update saved transform
    currentState().transform = gfx.getTransform();

    // clone to maintain current state
    stateStack.push(new JavaCanvasState(currentState()));
    return this;
  }

  @Override
  public Canvas scale(float x, float y) {
    gfx.scale(x, y);
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
    Asserts.checkArgument(gradient instanceof JavaGradient);

    currentState().fillGradient = (JavaGradient) gradient;
    currentState().fillPattern = null;
    currentState().fillColor = 0;
    return this;
  }

  @Override
  public Canvas setFillPattern(Pattern pattern) {
    Asserts.checkArgument(pattern instanceof JavaPattern);

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
    currentState().prepareStroke(gfx);
    ellipse.setFrame(x - radius, y - radius, 2 * radius, 2 * radius);
    gfx.draw(ellipse);
    isDirty = true;
    return this;
  }

  @Override
  public Canvas strokePath(Path path) {
    currentState().prepareStroke(gfx);
    gfx.setColor(new Color(currentState().strokeColor, false));
    gfx.draw(((JavaPath) path).path);
    isDirty = true;
    return this;
  }

  @Override
  public Canvas strokeRect(float x, float y, float width, float height) {
    currentState().prepareStroke(gfx);
    rect.setRect(x, y, width, height);
    gfx.draw(rect);
    isDirty = true;
    return this;
  }

  @Override
  public Canvas strokeRoundRect(float x, float y, float width, float height, float radius) {
    currentState().prepareStroke(gfx);
    roundRect.setRoundRect(x, y, width, height, radius*2, radius*2);
    gfx.draw(roundRect);
    isDirty = true;
    return this;
  }

  @Override
  public Canvas strokeText(TextLayout layout, float x, float y) {
    currentState().prepareStroke(gfx);
    ((Drawable)layout).stroke(gfx, x, y);
    isDirty = true;
    return this;
  }

  @Override
  public Canvas transform(float m11, float m12, float m21, float m22, float dx, float dy) {
    gfx.transform(new AffineTransform(m11, m12, m21, m22, dx, dy));
    return this;
  }

  @Override
  public Canvas translate(float x, float y) {
    gfx.translate(x, y);
    return this;
  }

  @Override
  protected Graphics2D gc() {
    currentState().prepareFill(gfx);
    return gfx;
  }

  private JavaCanvasState currentState() {
    return stateStack.getFirst();
  }
}
