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

import java.util.HashMap;
import java.util.Map;

import playn.core.Asserts;
import playn.core.CanvasImage;
import playn.core.CanvasLayer;
import playn.core.Font;
import playn.core.Gradient;
import playn.core.Graphics;
import playn.core.GroupLayer;
import playn.core.Image;
import playn.core.ImageLayer;
import playn.core.ImmediateLayer;
import playn.core.Path;
import playn.core.Pattern;
import playn.core.SurfaceLayer;
import playn.core.TextFormat;
import playn.core.TextLayout;
import playn.core.gl.GL20;
import static playn.core.PlayN.*;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JFrame;

public class JavaGraphics implements Graphics {

  private final Component component;
  private final JavaGroupLayer rootLayer;
  private final JFrame frame;

  JavaGraphics(JFrame frame, Component component) {
    this.frame = frame;
    this.component = component;
    this.rootLayer = new JavaGroupLayer();
  }

  /**
   * Registers a font with the graphics system.
   *
   * @param name the name under which to register the font.
   * @param path the path to the font resource (relative to the asset manager's path prefix).
   * Currently only TrueType ({@code .ttf}) fonts are supported.
   */
  public void registerFont(String name, String path) {
    try {
      java.awt.Font font = java.awt.Font.createFont(
        java.awt.Font.TRUETYPE_FONT, ((JavaAssets) assets()).getAssetStream(path));
      _fonts.put(name, font);
    } catch (Exception e) {
      log().warn("Failed to load font [name=" + name + ", path=" + path + "]", e);
    }
  }

  @Override @Deprecated
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
  public ImmediateLayer.Clipped createImmediateLayer(
      int width, int height, ImmediateLayer.Renderer renderer) {
    return new JavaImmediateLayer.Clipped(width, height, renderer);
  }

  @Override
  public ImmediateLayer createImmediateLayer(ImmediateLayer.Renderer renderer) {
    return new JavaImmediateLayer(renderer);
  }

  @Override
  public JavaGroupLayer rootLayer() {
    return rootLayer;
  }

  @Override
  public CanvasImage createImage(int w, int h) {
    return new JavaCanvasImage(w, h);
  }

  @Override
  public Gradient createLinearGradient(float x0, float y0, float x1, float y1,
      int[] colors, float[] positions) {
    return JavaGradient.createLinear(x0, y0, x1, y1, positions, colors);
  }

  @Override @Deprecated
  public Path createPath() {
    return new JavaPath();
  }

  @Override
  public Pattern createPattern(Image img) {
    return new JavaPattern((JavaImage) img);
  }

  @Override
  public Gradient createRadialGradient(float x, float y, float r, int[] colors, float[] positions) {
    return JavaGradient.createRadial(x, y, r, positions, colors);
  }

  @Override
  public Font createFont(String name, Font.Style style, float size) {
    java.awt.Font jfont = _fonts.get(name);
    // if we don't have a custom font registered for this name, assume it's a platform font
    if (jfont == null) {
      jfont = new java.awt.Font(name, java.awt.Font.PLAIN, 12);
    }
    return new JavaFont(name, style, size, jfont);
  }

  @Override
  public TextLayout layoutText(String text, TextFormat format) {
    return new JavaTextLayout(frame, text, format);
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

  @Override
  public GL20 gl20() {
    throw new UnsupportedOperationException();
  }

  protected Map<String,java.awt.Font> _fonts = new HashMap<String,java.awt.Font>();
}
