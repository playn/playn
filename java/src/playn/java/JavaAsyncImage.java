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
package playn.java;

import java.awt.image.BufferedImage;
import java.util.List;

import playn.core.Image;
import playn.core.gl.GLContext;
import playn.core.gl.Scale;
import playn.core.util.Callback;
import playn.core.util.Callbacks;

public class JavaAsyncImage extends JavaImage {

  private List<Callback<? super Image>> callbacks;
  private Throwable error;
  private float preWidth, preHeight;

  public JavaAsyncImage(GLContext ctx, float preWidth, float preHeight) {
    super(ctx, null, Scale.ONE);
    this.preWidth = preWidth;
    this.preHeight = preHeight;
  }

  @Override
  public float width() {
    return (img == null) ? preWidth : super.width();
  }

  @Override
  public float height() {
    return (img == null) ? preHeight : super.width();
  }

  @Override
  public void addCallback(Callback<? super Image> callback) {
    if (img != null)
      callback.onSuccess(this);
    else if (error != null)
      callback.onFailure(error);
    else
      callbacks = Callbacks.createAdd(callbacks, callback);
  }

  void setImage(BufferedImage img) {
    this.img = img;
    callbacks = Callbacks.dispatchSuccessClear(callbacks, this);
  }

  void setError(Throwable error) {
    this.error = error;
    callbacks = Callbacks.dispatchFailureClear(callbacks, error);
  }
}
