/**
 * Copyright 2010 The PlayN Authors
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
package playn.html;

import com.google.gwt.webgl.client.WebGLRenderingContext;

import playn.core.AbstractLayer;
import playn.core.InternalTransform;

abstract class HtmlLayerGL extends AbstractLayer {

  protected final HtmlGraphicsGL gfx;
  private final InternalTransform savedLocal = createTransform();

  protected HtmlLayerGL(HtmlGraphicsGL gfx) {
    super();
    this.gfx = gfx;
  }

  protected InternalTransform localTransform(InternalTransform parentTransform) {
    savedLocal.set(parentTransform);
    return savedLocal.concatenate(transform, originX, originY);
  }

  abstract void paint(WebGLRenderingContext gl, InternalTransform parentTransform,
                      float parentAlpha);

  @Override protected InternalTransform createTransform() {
    return new HtmlInternalTransform();
  }
}
