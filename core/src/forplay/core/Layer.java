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
 * Layer is the base element for all rendering in ForPlay
 * <p>
 * Each layer has a transformation matrix {@link #transform()} and several other associated
 * properties which can be manipulated directly (changes take effect automatically on the next
 * rendered frame).
 * <p>
 * The root of the layer hierarchy is the {@link Graphics#rootLayer() rootLayer} . All coordinates
 * in a layer are transformed by the layer's transformation matrix, and each child layer is
 * positioned by the transformation matrix of it's parent.
 * <p>
 * TODO: z-index, clipping (?), visibility TODO: transform-origin: allow explicit
 * "center, top-left, bottom-right" like CSS transform-origin?
 */
public interface Layer {

  /**
   * Destroys this layer, removing it from its parent layer. Any resources associated with this
   * layer are freed, and it cannot be reused after being destroyed. Destroying a layer that has
   * children will destroy them as well.
   */
  void destroy();

  /**
   * Whether this layer has been destroyed. If true, the layer can no longer be used.
   */
  boolean destroyed();

  /**
   * Returns the parent that contains this layer, or {@code null}.
   */
  GroupLayer parent();

  /**
   * Returns the layer's transformation matrix.
   */
  Transform transform();

  /**
   * Returns true if this layer is visible (i.e. it is being rendered).
   */
  boolean visible();

  /**
   * Configures this layer's visibility: if true, it will be rendered as normal, if false it and
   * its children will not be rendered.
   */
  void setVisible(boolean visible);

  /**
   * Return the global alpha value for this layer.
   * <p>
   * The global alpha value for a layer controls the opacity of the layer but does not affect the
   * current drawing operation. I.e., when {@link Game#paint(float)} is called and the {@link Layer}
   * is drawn, this alpha value is applied to the alpha channel of the Layer.
   * <p>
   * By default, the alpha for a Layer is 1.0 (not transparent).
   * 
   * @return alpha in range [0,1] where 0 is transparent and 1 is opaque
   */
  float alpha();

  /**
   * Set the global alpha value for this layer.
   * <p>
   * The global alpha value for a layer controls the opacity of the layer but does not affect the
   * current drawing operation. I.e., when {@link Game#paint(float)} is called and the {@link Layer}
   * is drawn, this alpha value is applied to the alpha channel of the Layer.
   * <p>
   * Values outside the range [0,1] will be clamped to the range [0,1].
   * 
   * @param alpha alpha value in range [0,1] where 0 is transparent and 1 is opaque
   */
  void setAlpha(float alpha);

  /**
   * Sets the origin of the layer.
   * <p>
   * This sets the origin of the layer's transformation matrix.
   * 
   * @param x origin on x axis
   * @param y origin on y axis
   */
  void setOrigin(float x, float y);

  /**
   * Sets the translation of the layer.
   * <p>
   * This sets the translation of the layer's transformation matrix so coordinates in the layer will
   * be translated by this amount.
   * 
   * @param x translation on x axis
   * @param y translation on y axis
   */
  void setTranslation(float x, float y);

  /**
   * Sets the scale of the layer.
   * <p>
   * This sets the scale of the layer's transformation matrix so coordinates in the layer will be
   * multiplied by this scale.
   * <p>
   * Note that a scale of {@code 1} is equivalent to no scale.
   * 
   * @param x non-zero scale value
   */
  void setScale(float x);

  /**
   * Sets the scale of the layer.
   * <p>
   * This sets the scale of the layer's transformation matrix so coordinates in the layer will be
   * multiplied by this scale.
   * <p>
   * Note that a scale of {@code 1} is equivalent to no scale.
   * 
   * @param x non-zero scale value on the x axis
   * @param y non-zero scale value on the y axis
   */
  void setScale(float x, float y);

  /**
   * Sets the rotation of the layer.
   * <p>
   * This sets the rotation of the layer's transformation matrix so coordinates in the layer will be
   * rotated by this angle.
   * <p>
   * 
   * @param angle angle to rotate, in radians
   */
  void setRotation(float angle);
}
