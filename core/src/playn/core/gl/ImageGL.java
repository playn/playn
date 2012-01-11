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
package playn.core.gl;

import playn.core.Asserts;
import playn.core.Image;

public abstract class ImageGL implements Image {

  /** The current count of references to this image. */
  protected int refs;

  /**
   * Creates a texture for this image (if one does not already exist) and returns it. May return
   * null if the underlying image data is not yet ready.
   */
  public abstract Object ensureTexture(GLContext ctx, boolean repeatX, boolean repeatY);

  /**
   * Releases this image's texture memory.
   */
  public abstract void clearTexture(GLContext ctx);

  /**
   * Increments this image's reference count. Called by {@link ImageLayerGL} to let the image know
   * that it's part of the scene graph. Note that this reference counting mechanism only exists to
   * make more efficient use of texture memory. Images are also used by things like {@link Pattern}
   * which does not support reference counting, thus images must also provide some fallback
   * mechanism for releasing their texture when no longer needed (like in their finalizer).
   */
  public void reference(GLContext ctx) {
    refs++; // we still create our texture on demand
  }

  /**
   * Decrements this image's reference count. Called by {@link ImageLayerGL} to let the image know
   * that may no longer be part of the scene graph.
   */
  public void release(GLContext ctx) {
    Asserts.checkState(refs > 0, "Released an image with no references!");
    if (--refs == 0) {
      clearTexture(ctx);
    }
  }
}
