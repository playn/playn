/**
 * Copyright 2010 The ForPlay Authors
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
package forplay.core;

/**
 * Main 2D graphics interface. This interface can be used to create and load
 * graphics objects used with {@link Canvas}.
 */
public interface Graphics {

  /**
   * TODO
   */
  GroupLayer rootLayer();

  /**
   * TODO
   */
  CanvasLayer createCanvasLayer(int width, int height);

  /**
   * TODO
   */
  GroupLayer createGroupLayer();

  /**
   * TODO
   */
  SurfaceLayer createSurfaceLayer(int width, int height);

  /**
   * TODO
   */
  ImageLayer createImageLayer();

  /**
   * TODO
   */
  ImageLayer createImageLayer(Image image);

  /**
   * Creates an image that can be painted using the {@link Canvas} interface.
   */
  CanvasImage createImage(int width, int height);

  /**
   * Creates a linear gradient fill pattern. (x0, y0) and (x1, y1) specify the
   * start and end positions, while (colors, positions) specifies the list of
   * color stops.
   */
  Gradient createLinearGradient(float x0, float y0, float x1, float y1,
      int colors[], float positions[]);

  /**
   * Creates a path object that may be used with {@link Canvas} drawing
   * methods.
   */
  Path createPath();

  /**
   * Creates a repeated image fill pattern to be used with {@link Canvas}
   * drawing methods.
   */
  Pattern createPattern(Image img);

  /**
   * Creates a radial gradient fill pattern. (x0, y0, r) specifies the circle
   * covered by this gradient, while (colors, positions) specifies the list of
   * color stops.
   */
  Gradient createRadialGradient(float x, float y, float r, int colors[],
      float positions[]);

  /**
   * Gets the height of the available screen real-estate, in pixels.
   */
  int screenHeight();

  /**
   * Gets the width of the available screen real-estate, in pixels.
   */
  int screenWidth();

  /**
   * Gets the width of the drawable surface, in pixels.
   */
  int width();

  /**
   * Gets the height of the drawable surface, in pixels.
   */
  int height();

  /**
   * Sets the size of the drawable surface, in pixels.
   */
  void setSize(int width, int height);
}
