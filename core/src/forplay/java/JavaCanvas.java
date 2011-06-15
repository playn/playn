/**
 * Copyright 2010 The ForPlay Authors
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
package forplay.java;

import forplay.core.Asserts;
import forplay.core.Canvas;
import forplay.core.Gradient;
import forplay.core.Image;
import forplay.core.Path;
import forplay.core.Pattern;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Deque;
import java.util.LinkedList;

class JavaCanvas implements Canvas {

  final Graphics2D gfx;
  private final int width, height;
  private Deque<JavaCanvasState> stateStack = new LinkedList<JavaCanvasState>();

  private Ellipse2D.Float ellipse = new Ellipse2D.Float();
  private Line2D.Float line = new Line2D.Float();
  private Rectangle2D.Float rect = new Rectangle2D.Float();

  JavaCanvas(Graphics2D graphics, int width, int height) {
    this.gfx = graphics;
    this.width = width;
    this.height = height;

    // push default state
    stateStack.push(new JavaCanvasState());

    // All clears go to rgba(0,0,0,0).
    gfx.setBackground(new Color(0, true));
  }

  public float alpha() {
    return currentState().alpha;
  }

  public void clear() {
    gfx.clearRect(0, 0, width, height);
  }

  @Override
  public void clip(Path path) {
    Asserts.checkArgument(path instanceof JavaPath);
    currentState().clip = (JavaPath) path;
  }

  public void drawImage(Image img, float x, float y) {
    Asserts.checkArgument(img instanceof JavaImage);
    JavaImage jimg = (JavaImage) img;

    currentState().prepareFill(gfx);
    int dx = (int) x, dy = (int) y, w = jimg.width(), h = jimg.height();
    gfx.drawImage(jimg.img, dx, dy, dx + w, dy + h, 0, 0, w, h, null);
  }

  public void drawImageCentered(Image img, float x, float y) {
    drawImage(img, x - img.width()/2, y - img.height()/2);
  }

  public void drawImage(Image img, float x, float y, float w, float h) {
    Asserts.checkArgument(img instanceof JavaImage);
    JavaImage jimg = (JavaImage) img;

    // For non-integer scaling, we have to use AffineTransform.
    AffineTransform tx = new AffineTransform(w / jimg.width(), 0f, 0f, h / jimg.height(), x, y);

    currentState().prepareFill(gfx);
    gfx.drawImage(jimg.img, tx, null);
  }

  @Override
  public void drawImage(Image img, float dx, float dy, float dw, float dh,
                        float sx, float sy, float sw, float sh) {
    Asserts.checkArgument(img instanceof JavaImage);
    JavaImage jimg = (JavaImage) img;

    // TODO: use AffineTransform here as well?

    currentState().prepareFill(gfx);
    gfx.drawImage(jimg.img, (int)dx, (int)dy, (int)(dx + dw), (int)(dy + dh),
                  (int)sx, (int)sy, (int)(sx + sw), (int)(sy + sh), null);
  }

  @Override
  public void drawLine(float x0, float y0, float x1, float y1) {
    currentState().prepareStroke(gfx);
    line.setLine(x0, y0, x1, y1);
    gfx.draw(line);
  }

  @Override
  public void drawPoint(float x, float y) {
    currentState().prepareStroke(gfx);
    gfx.drawLine((int) x, (int) y, (int) x, (int) y);
  }

  @Override
  public void drawText(String text, float x, float y) {
    currentState().prepareFill(gfx);
    gfx.drawString(text, x, y);
  }

  @Override
  public void fillCircle(float x, float y, float radius) {
    currentState().prepareFill(gfx);
    ellipse.setFrame(x - radius, y - radius, 2 * radius, 2 * radius);
    gfx.fill(ellipse);
  }

  @Override
  public void fillPath(Path path) {
    Asserts.checkArgument(path instanceof JavaPath);

    currentState().prepareFill(gfx);
    gfx.fill(((JavaPath) path).path);
  }

  @Override
  public void fillRect(float x, float y, float width, float height) {
    currentState().prepareFill(gfx);
    rect.setRect(x, y, width, height);
    gfx.fill(rect);
  }

  @Override
  public int height() {
    return height;
  }

  @Override
  public void restore() {
    stateStack.pop();
    gfx.setTransform(currentState().transform);
  }

  @Override
  public void rotate(float angle) {
    gfx.rotate(angle);
  }

  @Override
  public void save() {
    // update saved transform
    currentState().transform = gfx.getTransform();

    // clone to maintain current state
    stateStack.push(new JavaCanvasState(currentState()));
  }

  @Override
  public void scale(float x, float y) {
    gfx.scale(x, y);
  }

  public void setAlpha(float alpha) {
    currentState().alpha = alpha;
  }

  @Override
  public void setCompositeOperation(Composite composite) {
    currentState().composite = composite;
  }

  @Override
  public void setFillColor(int color) {
    currentState().fillColor = color;
    currentState().fillGradient = null;
    currentState().fillPattern = null;
  }

  @Override
  public void setFillGradient(Gradient gradient) {
    Asserts.checkArgument(gradient instanceof JavaGradient);

    currentState().fillGradient = (JavaGradient) gradient;
    currentState().fillPattern = null;
    currentState().fillColor = 0;
  }

  @Override
  public void setFillPattern(Pattern pattern) {
    Asserts.checkArgument(pattern instanceof JavaPattern);

    currentState().fillPattern = (JavaPattern) pattern;
    currentState().fillGradient = null;
    currentState().fillColor = 0;
  }

  @Override
  public void setLineCap(LineCap cap) {
    currentState().lineCap = cap;
  }

  @Override
  public void setLineJoin(LineJoin join) {
    currentState().lineJoin = join;
  }

  @Override
  public void setMiterLimit(float miter) {
    currentState().miterLimit = miter;
  }

  @Override
  public void setStrokeColor(int color) {
    currentState().strokeColor = color;
  }

  @Override
  public void setStrokeWidth(float w) {
    currentState().strokeWidth = w;
  }

  @Override
  public void setTransform(float m11, float m12, float m21, float m22, float dx, float dy) {
    gfx.setTransform(new AffineTransform(m11, m12, m21, m22, dx, dy));
  }

  @Override
  public void strokeCircle(float x, float y, float radius) {
    currentState().prepareStroke(gfx);
    ellipse.setFrame(x - radius, y - radius, 2 * radius, 2 * radius);
    gfx.draw(ellipse);
  }

  @Override
  public void strokePath(Path path) {
    currentState().prepareStroke(gfx);
    gfx.setColor(new Color(currentState().strokeColor, false));
    gfx.draw(((JavaPath) path).path);
  }

  @Override
  public void strokeRect(float x, float y, float width, float height) {
    currentState().prepareStroke(gfx);
    rect.setRect(x, y, width, height);
    gfx.draw(rect);
  }

  @Override
  public void transform(float m11, float m12, float m21, float m22, float dx, float dy) {
    gfx.transform(new AffineTransform(m11, m12, m21, m22, dx, dy));
  }

  @Override
  public void translate(float x, float y) {
    gfx.translate(x, y);
  }

  @Override
  public int width() {
    return width;
  }

  private JavaCanvasState currentState() {
    return stateStack.getFirst();
  }
}
