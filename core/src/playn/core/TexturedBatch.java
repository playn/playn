/**
 * Copyright 2010-2015 The PlayN Authors
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
package playn.core;

/**
 * A batch that renders textured primitives.
 */
public class TexturedBatch extends GLBatch {

  public final GL20 gl;
  protected int curTexId;

  /** Prepares this batch to render using the supplied texture. If pending operations have been
    * added to this batch for a different texture, this call will trigger a {@link #flush}.
    * <p>Note: if you call {@code add} methods that take a texture, you do not need to call this
    * method manually. Only if you're adding bare primitives is it needed. */
  public void setTexture (Texture texture) {
    if (curTexId != 0 && curTexId != texture.id) flush();
    this.curTexId = texture.id;
  }

  protected TexturedBatch (GL20 gl) {
    this.gl = gl;
  }

  /** Binds our current texture. Subclasses need to call this in {@link #flush}. */
  protected void bindTexture () {
    gl.glBindTexture(GL20.GL_TEXTURE_2D, curTexId);
    gl.checkError("QuadBatch glBindTexture");
  }
}
