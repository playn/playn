/**
 * Copyright 2010 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package playn.core;

import playn.core.gl.GL20;
import playn.core.gl.GLContext;

/**
 * Main 2D graphics interface. This interface can be used to create and load
 * graphics objects used with {@link Canvas}.
 */
public interface Graphics {

  /**
   * Gets the width of the drawable surface, in pixels.
   */
  int width();

  /**
   * Gets the height of the drawable surface, in pixels.
   */
  int height();

  /**
   * Gets the height of the available screen real-estate, in pixels.
   */
  int screenHeight();

  /**
   * Gets the width of the available screen real-estate, in pixels.
   */
  int screenWidth();

  /**
   * Returns the display scale factor. This will be 1 except on HiDPI devices that have been
   * configured to use HiDPI mode, where it will probably be 2, but could be some other scale
   * depending on how things were configured when initializing the platform.
   */
  float scaleFactor();

  /**
   * Returns the root of the scene graph. When layers are added to this layer, they become visible
   * on the screen.
   */
  GroupLayer rootLayer();

  /**
   * Returns the GL context on platforms that use GL, null otherwise. This is used for creating
   * custom shaders.
   */
  GLContext ctx();

  /**
   * Returns a reference to the GL context. <b>WARNING</b>: this is an experimental, not well
   * tested feature. It works on the Java, HTML and Android backends. It may change completely.
   * Consider yourself warned.
   */
  GL20 gl20();

  /**
   * Creates a group layer.
   */
  GroupLayer createGroupLayer();

  /**
   * Creates a clipped group layer, with the initial clipping size.
   */
  GroupLayer.Clipped createGroupLayer(float width, float height);

  /**
   * Creates an immediate layer that is clipped to the specified rectangular region.
   *
   * @param width the horizontal extent of the layer's drawable region.
   * @param height the vertical extent of the layer's drawable region.
   */
  ImmediateLayer.Clipped createImmediateLayer(
      int width, int height, ImmediateLayer.Renderer renderer);

  /**
   * Creates an unclipped immediate layer. This layer may draw anywhere on the framebuffer, though
   * its rendering operations will be transformed appropriately, based on the layer's current
   * transform.
   */
  ImmediateLayer createImmediateLayer(ImmediateLayer.Renderer renderer);

  /**
   * Creates an image layer with no configured image. Configure the image like so:
   * {@code createImageLayer().setImage(image)}.
   */
  ImageLayer createImageLayer();

  /**
   * Creates an image layer with the supplied image.
   */
  ImageLayer createImageLayer(Image image);

  /**
   * @deprecated Use {@link #createSurface} and {@link #createImageLayer}.
   */
  @Deprecated
  SurfaceLayer createSurfaceLayer(float width, float height);

  /**
   * Creates an image that can be painted using the {@link Canvas} interface.
   */
  CanvasImage createImage(float width, float height);

  /**
   * Creates an image that can be rendered into using the {@link Surface} interface.
   */
  SurfaceImage createSurface(float width, float height);

  /**
   * Creates a linear gradient fill pattern. (x0, y0) and (x1, y1) specify the
   * start and end positions, while (colors, positions) specifies the list of
   * color stops.
   */
  Gradient createLinearGradient(float x0, float y0, float x1, float y1,
      int colors[], float positions[]);

  /**
   * Creates a radial gradient fill pattern. (x0, y0, r) specifies the circle
   * covered by this gradient, while (colors, positions) specifies the list of
   * color stops.
   */
  Gradient createRadialGradient(float x, float y, float r, int colors[],
      float positions[]);

  /**
   * Creates a font with the specified configuration.
   */
  Font createFont(String name, Font.Style style, float size);

  /**
   * Lays out a single line of text using the specified format. The text may subsequently be
   * rendered on a canvas via {@link Canvas#fillText(TextLayout,float,float)}.
   */
  TextLayout layoutText(String text, TextFormat format);

  /**
   * Lays out multiple lines of text using the specified format and wrap configuration. The text
   * may subsequently be rendered on a canvas via {@link Canvas#fillText(TextLayout,float,float)}.
   */
  TextLayout[] layoutText(String text, TextFormat format, TextWrap wrap);
}
