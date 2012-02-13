/**
 * Copyright 2010 The PlayN Authors
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
package playn.html;

import com.google.gwt.canvas.dom.client.Context2d;

import playn.core.AbstractLayer;
import playn.core.StockInternalTransform;

abstract class HtmlLayerCanvas extends AbstractLayer {

  abstract void paint(Context2d ctx, float parentAlpha);

  protected HtmlLayerCanvas() {
    super(HtmlPlatform.hasTypedArraySupport ?
          new HtmlInternalTransform() : new StockInternalTransform());
  }

  void transform(Context2d ctx) {
    ctx.translate(originX, originY);
    ctx.transform(transform.m00(), transform.m01(), transform.m10(),
        transform.m11(), transform.tx() - originX, transform.ty() - originY);
    ctx.translate(-originX, -originY);
  }
}
