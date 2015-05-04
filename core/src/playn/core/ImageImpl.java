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

  protected final String source;
  protected Scale scale;
  protected int pixelWidth, pixelHeight;

  /** Notifies this image that its implementation bitmap is available.
    * This can be called from any thread. */
  public synchronized void succeed (Data data) {
    scale = data.scale;
    pixelWidth = data.pixelWidth;
    assert pixelWidth > 0;
    pixelHeight = data.pixelHeight;
    assert pixelHeight > 0;
    setBitmap(data.bitmap);
    ((RPromise<Image>)state).succeed(this); // state is a deferred promise
  }

  /** Notifies this image that its implementation bitmap failed to load.
    * This can be called from any thread. */
  public synchronized void fail (Throwable error) {
    if (pixelWidth == 0) pixelWidth = 50;
    if (pixelHeight == 0) pixelHeight = 50;
    setBitmap(createErrorBitmap(pixelWidth, pixelHeight));
    ((RPromise<Image>)state).fail(error); // state is a deferred promise
  }

  @Override public Scale scale () { return scale; }
  @Override public int pixelWidth () { return pixelWidth; }
  @Override public int pixelHeight () { return pixelHeight; }

  protected ImageImpl (Graphics gfx, Scale scale, int pixelWidth, int pixelHeight, String source,
                       Object bitmap) {
    super(gfx);
    if (pixelWidth == 0 || pixelHeight == 0) throw new IllegalArgumentException(
      "Invalid size for ready image: " + pixelWidth + "x" + pixelHeight + " bitmap: " + bitmap);
    this.source = source;
    this.scale = scale;
    this.pixelWidth = pixelWidth;
    this.pixelHeight = pixelHeight;
    setBitmap(bitmap);
  }

  protected ImageImpl (Graphics gfx, RFuture<Image> state, Scale preScale,
                       int preWidth, int preHeight, String source) {
    super(gfx, state);
    this.source = source;
    this.scale = preScale;
    this.pixelWidth = preWidth;
    this.pixelHeight = preHeight;
  }

  protected ImageImpl (Platform plat, boolean async, Scale preScale, int preWidth, int preHeight,
                       String source) {
    this(plat.graphics(), async ? plat.exec().<Image>deferredPromise() : RPromise.<Image>create(),
         preScale, preWidth, preHeight, source);
  }

  protected abstract void setBitmap (Object bitmap);
  protected abstract Object createErrorBitmap (int pixelWidth, int pixelHeight);
}
