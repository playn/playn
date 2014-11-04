/**
 * Copyright 2014 The PlayN Authors
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
package playn.robovm;

import java.util.List;

import org.robovm.apple.uikit.UIImage;

import playn.core.AsyncImage;
import playn.core.Image;
import playn.core.gl.GLContext;
import playn.core.gl.Scale;
import playn.core.util.Callback;
import playn.core.util.Callbacks;

/**
 * Implements {@link Image} based on an asynchronously loaded bitmap.
 */
public class RoboAsyncImage extends RoboImage implements AsyncImage<UIImage> {

  private final float preWidth, preHeight;
  private List<Callback<? super Image>> callbacks;
  private Throwable error;

  public RoboAsyncImage (GLContext ctx, float preWidth, float preHeight) {
    super(ctx, null, Scale.ONE);
    this.preWidth = preWidth;
    this.preHeight = preHeight;
  }

  @Override
  public boolean isReady() {
    return (image != null);
  }

  @Override
  public float width() {
    return (image == null) ? preWidth : super.width();
  }

  @Override
  public float height() {
    return (image == null) ? preHeight : super.height();
  }

  @Override
  public void addCallback(Callback<? super Image> callback) {
    if (error != null)
      callback.onFailure(error);
    else if (image != null)
      callback.onSuccess(this);
    else
      callbacks = Callbacks.createAdd(callbacks, callback);
  }

  @Override
  public void setImage(UIImage uiImage, Scale scale) {
    this.image = uiImage.getCGImage();
    this.scale = scale;
    callbacks = Callbacks.dispatchSuccessClear(callbacks, this);
  }

  @Override
  public void setError(Throwable error) {
    this.error = error;
    this.image = new UIImage().getCGImage(); // TODO: create error image
    callbacks = Callbacks.dispatchFailureClear(callbacks, error);
  }
}
