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
package playn.core;

/**
 * Represents a layer that maintains an off-screen texture into which one can render by calling
 * methods on {@link #surface}. Adding the surface layer to the scene graph results in the
 * off-screen texture being rendered according to the surface layer's current transform.
 *
 * <p><b>Custom shader note:</b> Configuring a custom shader on a surface layer only affects the
 * drawing of the texture to the main framebuffer. If you wish to use a custom shader when drawing
 * into the surface's off-screen buffer, use {@link Surface#setShader}.</p>
 */
public interface SurfaceLayer extends Layer.HasSize {

  /**
   * Returns a surface instance that can be used to render into the off-screen texture associated
   * with this surface layer.
   */
  Surface surface();
}
