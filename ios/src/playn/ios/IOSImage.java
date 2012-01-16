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

import cli.MonoTouch.UIKit.UIImage;

import playn.core.Image;
import playn.core.ResourceCallback;
import playn.core.gl.GLContext;
import playn.core.gl.ImageGL;

/**
 * Does something extraordinary.
 */
public class IOSImage extends ImageGL implements Image
{
  public final UIImage image;

  IOSImage (UIImage image) {
    this.image = image;
  }

  @Override
  public int width() {
    return (int)image.get_Size().get_Width();
  }

  @Override
  public int height() {
    return (int)image.get_Size().get_Height();
  }

  @Override
  public boolean isReady() {
    return true; // we're always ready
  }

  @Override
  public void addCallback(ResourceCallback<Image> callback) {
    callback.done(this); // we're always ready
  }

  @Override
  public Object ensureTexture(GLContext ctx, boolean repeatX, boolean repeatY) {
    return null; // TODO
  }

  @Override
  public void clearTexture(GLContext ctx) {
    // TODO
  }
}
