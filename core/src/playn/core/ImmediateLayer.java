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
package playn.core;

/**
 * Represents a layer that enables direct rendering into the framebuffer. An immediate layer is
 * constructed with a callback that is invoked every frame to render the layer directly into the
 * framebuffer, appropriately interleaved with the rendering of all of the other layers in the
 * scene graph.
 *
 * <p><b>Custom shader note:</b> An immediate layer defaults to the shader configured by its
 * parent, but it is possible to change that shader by calling {@link Surface#setShader} on the
 * surface passed to {@link Renderer#render}.</p>
 *
 * <p>See also: http://en.wikipedia.org/wiki/Immediate_mode</p>
 */
public interface ImmediateLayer extends Layer {

  /** An immediate layer that is clipped to a specified bounds. */
  public interface Clipped extends ImmediateLayer, Layer.HasSize {
  }

  /** Defines a callback that is invoked every frame to render an immediate layer. */
  public interface Renderer {
    /** Renders the contents of this layer using the supplied {@link Surface}. The graphics context
     * will have already been transformed based on the layer's transform.
     * @param surface a surface object that represents the frame buffer. */
    void render(Surface surface);
  }

  /** Returns the renderer used by this immediate layer. */
  Renderer renderer ();
}
