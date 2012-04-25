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
import playn.core.ResourceCallback;

public class JavaStaticImage extends JavaImage {

  private List<ResourceCallback<? super Image>> callbacks;

  public JavaStaticImage(JavaGLContext ctx, final BufferedImage img) {
    super(ctx, null);

    JavaAssets.doResourceAction(new Runnable() {
      public void run () {
        JavaStaticImage.this.img = img;
        if (callbacks != null) {
          for (ResourceCallback<? super Image> callback : callbacks)
            callback.done(JavaStaticImage.this);
          callbacks = null;
        }
      }
    });
  }

  @Override
  public void addCallback(ResourceCallback<? super Image> callback) {
    if (img != null)
      callback.done(this);
    else {
      if (callbacks == null)
        callbacks = new ArrayList<ResourceCallback<? super Image>>();
      callbacks.add(callback);
    }
  }

  @Override
  public Image transform(BitmapTransformer xform) {
    return new JavaStaticImage(ctx, ((JavaBitmapTransformer) xform).transform(img));
  }
}
