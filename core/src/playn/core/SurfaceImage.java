/**
 * Copyright 2012 The PlayN Authors
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
 * An image that provides a {@link Surface} into which you can render. Surface images are backed by
 * textures in the GL backends and rendering goes directly to the texture.
 *
 * <p><em>NOTE:</em> currently it is not possible to draw a surface image into a {@link Canvas} on
 * the GL backends, even though the API makes it possible. At some point we can implement the code
 * to download the texture image from the GPU and draw it into the CPU-memory bitmap that underlies
 * a {@link Canvas} and this will become supported.</p>
 */
public interface SurfaceImage extends Image {

  /**
   * Returns an interface that can be used to draw into this image.
   */
  Surface surface();

  /**
   * Destroys the texture underlying this surface image and frees up the associated GPU memory.
   * This will happen automatically when this image is garbage collected, but this can ensure that
   * the memory is freed at a predictable time. <em>Warning:</em> if you destroy a surface image
   * that is currently being displayed by an image layer, bad things will happen. Don't do that.
   */
  void destroy();
}
