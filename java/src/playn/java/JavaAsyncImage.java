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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import pythagoras.f.MathUtil;

import playn.core.AsyncImage;
import playn.core.Image;
import playn.core.gl.GLContext;
import playn.core.gl.Scale;
import playn.core.util.Callback;
import playn.core.util.Callbacks;

public class JavaAsyncImage extends JavaImage implements AsyncImage<BufferedImage> {

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
    return (img == null) ? preHeight : super.height();
  }

  @Override
  public void addCallback(Callback<? super Image> callback) {
    if (error != null)
      callback.onFailure(error);
    else if (img != null)
      callback.onSuccess(this);
    else
      callbacks = Callbacks.createAdd(callbacks, callback);
  }

  @Override
  public void setImage(BufferedImage img, Scale scale) {
    this.img = img;
    this.scale = scale;
    callbacks = Callbacks.dispatchSuccessClear(callbacks, this);
  }

  @Override
  public void setError(Throwable error) {
    this.error = error;
    this.img = createErrorImage(preWidth == 0 ? 50 : preWidth, preHeight == 0 ? 50 : preHeight);
    callbacks = Callbacks.dispatchFailureClear(callbacks, error);
  }

  private static BufferedImage createErrorImage(float width, float height) {
    BufferedImage img = new BufferedImage(MathUtil.iceil(width), MathUtil.iceil(height),
                                          BufferedImage.TYPE_INT_ARGB_PRE);
    Graphics2D g = img.createGraphics();
    try {
      g.setColor(Color.red);
      for (int yy = 0; yy <= height/15; yy++) {
        for (int xx = 0; xx <= width/45; xx++) {
          g.drawString("ERROR", xx*45, yy*15);
        }
      }
    } finally {
      g.dispose();
    }
    return img;
  }
}
