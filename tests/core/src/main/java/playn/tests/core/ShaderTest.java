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

import pythagoras.f.FloatMath;

import playn.core.CanvasImage;
import playn.core.Image;
import playn.core.ImageLayer;
import playn.core.ResourceCallback;
import playn.core.gl.GLContext;
import playn.core.gl.IndexedTrisShader;
import static playn.core.PlayN.*;

/**
 * Tests custom shader support.
 */
public class ShaderTest extends Test {

  private float elapsed;

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
    float dx = orange.width() + 25;
    graphics().rootLayer().addAt(graphics().createImageLayer(orange), 25, 25);

    // add a sepia toned orange
    ImageLayer olayer = graphics().createImageLayer(orange);
    olayer.setShader(new IndexedTrisShader(graphics().ctx()) {
      @Override protected String textureFragmentShader() {
        return "#ifdef GL_ES\n" +
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
    });
    graphics().rootLayer().addAt(olayer, 25+dx, 25);

    // add an image that is rotated around the (3D) y axis
    CanvasImage image = graphics().createImage(orange.width(), orange.height());
    image.canvas().setFillColor(0xFF99CCFF);
    image.canvas().fillRect(0, 0, image.width(), image.height());
    image.canvas().drawImage(orange, 0, 0);
    final ImageLayer rotlayer = graphics().createImageLayer(image);
    rotlayer.setOrigin(0, image.height()/2);
    rotlayer.setShader(new IndexedTrisShader(graphics().ctx()) {
      @Override protected String vertexShader() {
        return "uniform vec2 u_ScreenSize;\n" +
          "uniform float u_Angle;\n" +
          "uniform vec2 u_Eye;\n" +
          "attribute vec4 a_Matrix;\n" +
          "attribute vec2 a_Translation;\n" +
          "attribute vec2 a_Position;\n" +
          "attribute vec2 a_TexCoord;\n" +
          "varying vec2 v_TexCoord;\n" +

          "void main(void) {\n" +
          // Rotate the vertex per our 3D rotation
          "  float cosa = cos(u_Angle);\n" +
          "  float sina = sin(u_Angle);\n" +
          "  mat4 rotmat = mat4(\n" +
          "    cosa, 0, sina, 0,\n" +
          "    0,    1, 0,    0,\n" +
          "   -sina, 0, cosa, 0,\n" +
          "    0,    0, 0,    1);\n" +
          "  vec4 pos = rotmat * vec4(a_Position - u_Eye, 0, 1);\n" +

          // Perspective project the vertex back into the plane
          "  mat4 persp = mat4(\n" +
          "    1, 0, 0, 0,\n" +
          "    0, 1, 0, 0,\n" +
          "    0, 0, 1, -1.0/200.0,\n" +
          "    0, 0, 0, 1);\n" +
          "  pos = persp * pos;\n" +
          "  pos /= pos.w;\n" +
          "  pos += vec4(u_Eye, -pos.z, 0);\n;" +

          // Transform the vertex per the normal screen transform
          "  mat4 transform = mat4(\n" +
          "    a_Matrix[0],      a_Matrix[1],      0, 0,\n" +
          "    a_Matrix[2],      a_Matrix[3],      0, 0,\n" +
          "    0,                0,                1, 0,\n" +
          "    a_Translation[0], a_Translation[1], 0, 1);\n" +
          "  pos = transform * pos;\n" +
          "  pos.x /= (u_ScreenSize.x / 2.0);\n" +
          "  pos.y /= (u_ScreenSize.y / 2.0);\n" +
          "  pos.x -= 1.0;\n" +
          "  pos.y = 1.0 - pos.y;\n" +
          "  gl_Position = pos;\n" +

          "  v_TexCoord = a_TexCoord;\n" +
          "}";
      }

      @Override
      protected Core createTextureCore(GLContext ctx) {
        return new ITCore(ctx, vertexShader(), textureFragmentShader()) {
          private final Uniform1f uAngle = prog.getUniform1f("u_Angle");
          private final Uniform2f uEye = prog.getUniform2f("u_Eye");

          @Override
          public void prepare(int fbufWidth, int fbufHeight) {
            super.prepare(fbufWidth, fbufHeight);
            uAngle.bind(elapsed * FloatMath.PI);
            uEye.bind(rotlayer.originX(), rotlayer.originY());
          }
        };
      }
    });
    graphics().rootLayer().addAt(rotlayer, 25 + 2*dx + rotlayer.width(), 25 + rotlayer.originY());
  }

  @Override
  public void update(float delta) {
    elapsed += delta/1000;
  }
}
