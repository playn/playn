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

/**
 * A layer that displays a bitmapped image. By default, the layer is the same size as its
 * underlying image, but its size can be changed from that default and the layer will either scale
 * or repeat the image to cause it to fill its bounds. See {@link #setSize}, {@link #setRepeatX},
 * etc.
 */
public interface ImageLayer extends Layer.HasSize {

  /**
   * Returns the image contained by this layer.
   */
  Image image();

  /**
   * Sets the image contained by this layer.
   */
  ImageLayer setImage(Image image);

  /**
   * Configures this layer to repeat its underlying image (instead of scaling it) when this layer's
   * width differs from that of its underlying image. The default is {@code false}.
   */
  void setRepeatX(boolean repeat);

  /**
   * Configures this layer to repeat its underlying image (instead of scaling it) when this layer's
   * height differs from that of its underlying image. The default is {@code false}.
   */
  void setRepeatY(boolean repeat);

  /**
   * Configures the size of this layer. The size defaults to the size of the image contained by the
   * layer, but if it is set manually then the image will be scaled or repeated to fill the
   * specified size.
   */
  void setSize(float width, float height);

  /**
   * Configures the width of this layer. See {@link #setSize}.
   */
  void setWidth(float width);

  /**
   * Configures the height of this layer. See {@link #setSize}.
   */
  void setHeight(float height);

  /**
   * Clears out the configured width for this layer, causing it to return to using the height of
   * its contained image.
   */
  void clearWidth();

  /**
   * Clears out the configured height for this layer, causing it to return to using the height of
   * its contained image.
   */
  void clearHeight();
}
