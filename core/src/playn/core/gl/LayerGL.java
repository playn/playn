/**
 * Copyright 2011 The PlayN Authors
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
package playn.core.gl;

import playn.core.AbstractLayer;
import playn.core.InternalTransform;
import playn.core.Layer;
import playn.core.gl.GLShader;

public abstract class LayerGL extends AbstractLayer {

  private final InternalTransform savedLocal;

  protected final GLContext ctx;
  protected GLShader shader;

  @Override
  public Layer setShader(GLShader shader) {
    if (this.shader != null) {
      this.shader.release();
    }
    this.shader = shader;
    if (this.shader != null) {
      this.shader.reference();
    }
    return this;
  }

  @Override
  public void destroy() {
    super.destroy();
    setShader(null);
  }

  protected LayerGL(GLContext ctx) {
    super(ctx.createTransform());
    this.ctx = ctx;
    this.savedLocal = ctx.createTransform();
  }

  protected InternalTransform localTransform(InternalTransform parentTransform) {
    savedLocal.set(parentTransform);
    return savedLocal.concatenate(transform(), originX, originY);
  }

  public abstract void paint(InternalTransform curTransform, float curAlpha, GLShader curShader);
}
