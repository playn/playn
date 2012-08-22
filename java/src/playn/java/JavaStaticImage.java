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
import java.util.ArrayList;
import java.util.List;

import playn.core.Image;
import playn.core.gl.GLContext;
import playn.core.gl.Scale;
import playn.core.util.Callback;

public class JavaStaticImage extends JavaImage {

  private List<Callback<? super Image>> callbacks;

  public JavaStaticImage(GLContext ctx, final BufferedImage img, Scale scale) {
    super(ctx, null, scale);

    JavaAssets.doResourceAction(new Runnable() {
      public void run () {
        JavaStaticImage.this.img = img;
        if (callbacks != null) {
          for (Callback<? super Image> callback : callbacks)
            callback.onSuccess(JavaStaticImage.this);
          callbacks = null;
        }
      }
    });
  }

  @Override
  public void addCallback(Callback<? super Image> callback) {
    if (img != null)
      callback.onSuccess(this);
    else {
      if (callbacks == null)
        callbacks = new ArrayList<Callback<? super Image>>();
      callbacks.add(callback);
    }
  }
}
