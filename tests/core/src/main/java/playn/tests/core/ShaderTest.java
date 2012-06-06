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
package playn.tests.core;

import playn.core.Image;
import playn.core.ImageLayer;
import playn.core.ResourceCallback;
import playn.core.gl.IndexedTrisShader;
import static playn.core.PlayN.*;

/**
 * Tests custom shader support.
 */
public class ShaderTest extends Test {

  @Override
  public String getName() {
    return "ShaderTest";
  }

  @Override
  public String getDescription() {
    return "Tests custom shader support.";
  }

  @Override
  public void init() {
    // TODO: display some text saying shaders aren't supported
    if (graphics().ctx() == null) return;

    Image orange = assets().getImage("images/orange.png");
    orange.addCallback(new ResourceCallback<Image>() {
      public void done(Image orange) {
        init(orange);
      }
      public void error(Throwable err) {
        log().warn("Failed to load orange image", err);
      }
    });
  }

  protected void init (Image orange) {
    // add the normal orange
    graphics().rootLayer().addAt(graphics().createImageLayer(orange), 25, 25);

    // add a sepia toned orange
    ImageLayer olayer = graphics().createImageLayer(orange);
    olayer.setShaders(new IndexedTrisShader.Texture(
                        graphics().ctx(), IndexedTrisShader.VERTEX_SHADER, SEPIA_FRAG_SHADER), null);
    graphics().rootLayer().addAt(olayer, 75, 25);
  }

  protected static final String SEPIA_FRAG_SHADER =
    "#ifdef GL_ES\n" +
    "precision highp float;\n" +
    "#endif\n" +

    "uniform sampler2D u_Texture;\n" +
    "varying vec2 v_TexCoord;\n" +
    "uniform float u_Alpha;\n" +

    "void main(void) {\n" +
    "  vec4 textureColor = texture2D(u_Texture, v_TexCoord);\n" +
    "  float grey = dot(textureColor.rgb, vec3(0.299, 0.587, 0.114));\n" +
    "  gl_FragColor = vec4(grey * vec3(1.2, 1.0, 0.8), textureColor.a) * u_Alpha;\n" +
    "}";
}
