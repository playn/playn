/**
 * Copyright 2010-2015 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playn.core;

import react.Function;
import react.RFuture;

import playn.core.Disposable;

/**
 * Combines a {@link Bitmap} and an on-demand created {@link Texture} into a convenient package.
 * When possible, one should avoid retaining references to their bitmaps after they've uploaded
 * them to textures, but sometimes that's too much of a pain. This class allows one to at least
 * ensure that anything that is displaying a bitmap uses the same texture to do so.
 */
public class Image implements Disposable {

  private final Texture.Config config;
  private Texture texture;

  /** The bitmap that backs this image. */
  public final Bitmap bitmap;

  /**
   * Creates an image with {@code bitmap} which uses the default texture configuration when
   * creating its texture.
   */
  public Image (Bitmap bitmap) {
    this(bitmap, Texture.Config.DEFAULT);
  }

  /**
   * Creates an image with {@code bitmap} and the supplied texture {@code config}.
   */
  public Image (Bitmap bitmap, Texture.Config config) {
    this.config = config;
    this.bitmap = bitmap;
  }

  /**
   * Returns the texture for this image, creating and uploading it if necessary. This can only be
   * called if this image's bitmap is fully loaded.
   */
  public Texture texture () {
    if (texture == null || texture.destroyed()) texture = bitmap.toTexture(config);
    return texture;
  }

  /**
   * Returns the texture for this image, once it has finished loading.
   */
  public RFuture<Texture> textureAsync () {
    return bitmap.state.map(new Function<Bitmap,Texture>() {
      public Texture apply (Bitmap bmp) { return texture(); }
    });
  }

  /**
   * Destroys the texture associated with this image, if it has been created. This is not necessary
   * (and could cause problems if the texture is still in use) if one is using a managed texture,
   * but can be useful if one is manually managing their textures.
   */
  @Override public void close () {
    if (texture != null) {
      texture.close();
      texture = null;
    }
  }
}
