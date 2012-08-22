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

import playn.core.Image;
import playn.core.gl.GLContext;
import playn.core.gl.Scale;
import playn.core.util.Callback;

import cli.MonoTouch.UIKit.UIImage;

public class IOSErrorImage extends IOSImage {

  private final Throwable error;
  private final float width, height;

  public IOSErrorImage (GLContext ctx, Throwable cause, float width, float height) {
    super(ctx, new UIImage(), Scale.ONE);
    this.error = cause;
    this.width = width;
    this.height = height;
  }

  @Override
  public float width() {
    return width;
  }

  @Override
  public float height() {
    return height;
  }

  @Override
  public void addCallback(Callback<? super Image> callback) {
    callback.onFailure(error);
  }
}
