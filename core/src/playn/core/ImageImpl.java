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

import react.RFuture;
import react.RPromise;

/**
 * An implementation detail. Not part of the public API.
 */
public abstract class ImageImpl extends Image {

  /** Used to provide bitmap data to the abstract image once it's ready. */
  public static class Data {
    public final Scale scale;
    public final Object bitmap;
    public final int pixelWidth, pixelHeight;
    public Data (Scale scale, Object bitmap, int pixelWidth, int pixelHeight) {
      this.bitmap = bitmap;
      this.scale = scale;
      this.pixelWidth = pixelWidth;
      this.pixelHeight = pixelHeight;
    }
  }

  protected Scale scale;
  protected int pixelWidth, pixelHeight;

  /** Notifies this image that its implementation image is available.
    * This can be called from any thread. */
  public synchronized void succeed (Data data) {
    scale = data.scale;
    pixelWidth = data.pixelWidth;
    pixelHeight = data.pixelHeight;
    setBitmap(data.bitmap);
    ((RPromise<Image>)state).succeed(this); // state is a deferred promise
  }

  /** Notifies this image that its implementation image failed to load.
    * This can be called from any thread. */
  public synchronized void fail (Throwable error) {
    int errWidth = (pixelWidth == 0) ? 50 : pixelWidth;
    int errHeight = (pixelHeight == 0) ? 50 : pixelHeight;
    setBitmap(createErrorBitmap(errWidth, errHeight));
    ((RPromise<Image>)state).fail(error); // state is a deferred promise
  }

  /** Renders this image into a platform-specific drawing context. */
  public abstract void draw (Object ctx, float dx, float dy, float dw, float dh);

  /** Renders this image into a platform-specific drawing context. */
  public abstract void draw (Object ctx, float dx, float dy, float dw, float dh,
                             float sx, float sy, float sw, float sh);

  @Override public Scale scale () { return scale; }
  @Override public int pixelWidth () { return pixelWidth; }
  @Override public int pixelHeight () { return pixelHeight; }

  protected ImageImpl (Scale scale, int pixelWidth, int pixelHeight, Object bitmap) {
    if (pixelWidth == 0 || pixelHeight == 0) throw new IllegalArgumentException(
      "Invalid size for ready image: " + pixelWidth + "x" + pixelHeight + " bitmap: " + bitmap);
    this.scale = scale;
    this.pixelWidth = pixelWidth;
    this.pixelHeight = pixelHeight;
    setBitmap(bitmap);
  }

  protected ImageImpl (RFuture<Image> state, Scale preScale, int preWidth, int preHeight) {
    super(state);
    scale = preScale;
    pixelWidth = preWidth;
    pixelHeight = preHeight;
  }

  protected ImageImpl (Platform plat, Scale preScale, int preWidth, int preHeight) {
    this(plat.<Image>deferredPromise(), preScale, preWidth, preHeight);
  }

  protected abstract void setBitmap (Object bitmap);
  protected abstract Object createErrorBitmap (int pixelWidth, int pixelHeight);
}
