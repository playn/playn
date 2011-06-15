/**
 * Copyright 2010 The ForPlay Authors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package forplay.html;

import com.google.gwt.webgl.client.WebGLRenderingContext;

import forplay.core.AbstractLayer;
import forplay.core.Transform;

abstract class HtmlLayerGL extends AbstractLayer {

  protected final HtmlGraphicsGL gfx;
  private final Transform savedLocal = new Transform();

  protected HtmlLayerGL(HtmlGraphicsGL gfx) {
    super();
    this.gfx = gfx;
  }

  protected Transform localTransform(Transform parentTransform) {
    savedLocal.copy(parentTransform);
    savedLocal.translate(originX, originY);
    savedLocal.transform(transform.m00(), transform.m01(), transform.m10(),
        transform.m11(), transform.tx() - originX, transform.ty() - originY);
    savedLocal.translate(-originX, -originY);
    return savedLocal;
  }

  abstract void paint(WebGLRenderingContext gl, Transform parentTransform, float parentAlpha);
}
