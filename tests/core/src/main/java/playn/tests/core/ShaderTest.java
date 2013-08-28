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
import playn.core.ImmediateLayer;
import playn.core.Surface;
import playn.core.gl.IndexedTrisShader;
import playn.core.util.Callback;
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
    orange.addCallback(new Callback<Image>() {
      public void onSuccess(Image orange) {
        init(orange);
      }
      public void onFailure(Throwable err) {
        log().warn("Failed to load orange image", err);
      }
    });
  }

  protected void init (final Image orange) {
    // add the normal orange
    float dx = orange.width() + 25;
    graphics().rootLayer().addAt(graphics().createImageLayer(orange), 25, 25);

    // add a sepia toned orange
    ImageLayer olayer = graphics().createImageLayer(orange);
    olayer.setShader(createSepiaShader());
    graphics().rootLayer().addAt(olayer, 25+dx, 25);

    // create a shader that rotates things around the (3D) y axis
    IndexedTrisShader rotShader = new IndexedTrisShader(graphics().ctx()) {
      @Override protected String vertexShader() {
        return VERT_UNIFS +
          "uniform float u_Angle;\n" +
          "uniform vec2 u_Eye;\n" +
          VERT_ATTRS +
          PER_VERT_ATTRS +
          VERT_VARS +

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
          "  pos += vec4(u_Eye, 0, 0);\n;" +

          // Transform the vertex per the normal screen transform
          "  mat4 transform = mat4(\n" +
          "    a_Matrix[0],      a_Matrix[1],      0, 0,\n" +
          "    a_Matrix[2],      a_Matrix[3],      0, 0,\n" +
          "    0,                0,                1, 0,\n" +
          "    a_Translation[0], a_Translation[1], 0, 1);\n" +
          "  pos = transform * pos;\n" +
          "  pos.x /= (u_ScreenSize.x / 2.0);\n" +
          "  pos.y /= (u_ScreenSize.y / 2.0);\n" +
          "  pos.z /= (u_ScreenSize.y / 2.0);\n" +
          "  pos.x -= 1.0;\n" +
          "  pos.y = 1.0 - pos.y;\n" +
          "  gl_Position = pos;\n" +

          VERT_SETTEX +
          VERT_SETCOLOR +
          "}";
      }

      @Override
      protected Core createTextureCore() {
        return new RotCore(vertexShader(), textureFragmentShader());
      }

      @Override
      protected Core createColorCore() {
        return new RotCore(vertexShader(), colorFragmentShader());
      }

      class RotCore extends ITCore {
        private final Uniform1f uAngle = prog.getUniform1f("u_Angle");
        private final Uniform2f uEye = prog.getUniform2f("u_Eye");

        public RotCore (String vertShader, String fragShader) {
          super(vertShader, fragShader);
        }

        @Override
        public void activate(int fbufWidth, int fbufHeight) {
          super.activate(fbufWidth, fbufHeight);
          uAngle.bind(elapsed * FloatMath.PI);
          uEye.bind(0, orange.height()/2);
        }
      }
    };

    // add an image that is rotated around the (3D) y axis
    CanvasImage image = graphics().createImage(orange.width(), orange.height());
    image.canvas().setFillColor(0xFF99CCFF);
    image.canvas().fillRect(0, 0, image.width(), image.height());
    image.canvas().drawImage(orange, 0, 0);
    ImageLayer rotlayer = graphics().createImageLayer(image);
    rotlayer.setShader(rotShader);
    graphics().rootLayer().addAt(rotlayer, 25 + 2*dx + orange.width(), 25);

    // add an immediate layer that draws a quad and an image (which should rotate)
    ImmediateLayer irotlayer = graphics().createImmediateLayer(new ImmediateLayer.Renderer() {
      public void render (Surface surf) {
        surf.setFillColor(0xFFCC99FF);
        surf.fillRect(0, 0, orange.width(), orange.height());
        surf.drawImage(orange, 0, 0);
      }
    });
    irotlayer.setShader(rotShader);
    graphics().rootLayer().addAt(irotlayer, 25 + 3*dx + orange.width(), 25);
  }

  @Override
  public void update(int delta) {
    elapsed += delta/1000f;
  }
}
