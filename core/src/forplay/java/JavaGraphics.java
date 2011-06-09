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
import forplay.core.CanvasLayer;
import forplay.core.Gradient;
import forplay.core.Graphics;
import forplay.core.GroupLayer;
import forplay.core.Image;
import forplay.core.ImageLayer;
import forplay.core.Path;
import forplay.core.Pattern;
import forplay.core.CanvasImage;
import forplay.core.SurfaceLayer;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

class JavaGraphics implements Graphics {

  private final Component component;
  private final JavaGroupLayer rootLayer;
  private final JFrame frame;

  JavaGraphics(JFrame frame, Component component) {
    this.frame = frame;
    this.component = component;
    this.rootLayer = new JavaGroupLayer();
  }

  @Override
  public CanvasLayer createCanvasLayer(int width, int height) {
    return new JavaCanvasLayer(width, height);
  }

  @Override
  public GroupLayer createGroupLayer() {
    return new JavaGroupLayer();
  }

  @Override
  public ImageLayer createImageLayer() {
    return new JavaImageLayer();
  }

  @Override
  public ImageLayer createImageLayer(Image image) {
    Asserts.checkArgument(image instanceof JavaImage);
    return new JavaImageLayer((JavaImage) image);
  }

  @Override
  public SurfaceLayer createSurfaceLayer(int width, int height) {
    return new JavaSurfaceLayer(width, height);
  }

  @Override
  public JavaGroupLayer rootLayer() {
    return rootLayer;
  }

  @Override
  public CanvasImage createImage(int w, int h) {
    return new JavaImage(new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB));
  }

  @Override
  public Gradient createLinearGradient(float x0, float y0, float x1, float y1, int[] colors, float[] positions) {
    return JavaGradient.createLinear(x0, y0, x1, y1, positions, colors);
  }

  @Override
  public Path createPath() {
    return new JavaPath();
  }

  @Override
  public Pattern createPattern(Image img) {
    return JavaPattern.create((JavaImage) img);
  }

  @Override
  public Gradient createRadialGradient(float x, float y, float r, int[] colors, float[] positions) {
    return JavaGradient.createRadial(x, y, r, positions, colors);
  }

  @Override
  public int screenWidth() {
    // TODO: Do we actually want to return the true screen width?
    return component.getWidth();
  }

  @Override
  public int screenHeight() {
    // TODO: Do we actually want to return the true screen height?
    return component.getHeight();
  }

  @Override
  public int width() {
    return component.getWidth();
  }

  @Override
  public int height() {
    return component.getHeight();
  }

  @Override
  public void setSize(int width, int height) {
    component.setPreferredSize(new Dimension(width, height));
    frame.pack();
  }
}
