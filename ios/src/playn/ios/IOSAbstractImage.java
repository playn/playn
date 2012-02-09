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
package playn.ios;

import cli.MonoTouch.CoreGraphics.CGImage;

import playn.core.Image;
import playn.core.ResourceCallback;
import playn.core.gl.ImageGL;

/**
 * Provides some shared bits for {@link IOSImage} and {@link IOSCanvasImage}.
 */
abstract class IOSAbstractImage extends ImageGL implements Image
{
  protected final IOSGLContext ctx;

  /**
   * Returns a core graphics image that can be used to paint this image into a canvas.
   */
  abstract CGImage cgImage();

  @Override
  public boolean isReady() {
    return true; // we're always ready
  }

  @Override
  public void addCallback(ResourceCallback<Image> callback) {
    callback.done(this); // we're always ready
  }

  @Override
  protected void finalize() {
    if (tex != null)
      ctx.queueDestroyTexture(tex);
    if (reptex != null)
      ctx.queueDeleteFramebuffer(reptex);
  }

  protected IOSAbstractImage(IOSGLContext ctx) {
    this.ctx = ctx;
  }
}
