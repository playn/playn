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

import playn.core.*;
import playn.scene.*;
import react.Slot;

/**
 * Tests custom shader support.
 */
public class ShaderTest extends Test {

  public ShaderTest (TestsGame game) {
    super(game, "Shader", "Tests custom shader support.");
  }

  @Override public void init () {
    game.assets.getImage("images/orange.png").state.onSuccess(new Slot<Image>() {
      public void onEmit (Image orange) {
        final Texture otex = orange.texture();

        // add the normal orange
        float dx = orange.width() + 25;
        game.rootLayer.addAt(new ImageLayer(otex), 25, 25);

        // add a sepia toned orange
        ImageLayer olayer = new ImageLayer(otex);
        olayer.setBatch(createSepiaBatch());
        game.rootLayer.addAt(olayer, 25+dx, 25);

        final RotYBatch rotBatch = createRotBatch();
        rotBatch.eyeX = 0;
        rotBatch.eyeY = orange.height()/2;

        // add an image that is rotated around the (3D) y axis
        Canvas canvas = game.graphics.createCanvas(orange.width(), orange.height());
        canvas.setFillColor(0xFF99CCFF).fillRect(0, 0, canvas.width, canvas.height);
        canvas.draw(orange, 0, 0);
        ImageLayer rotlayer = new ImageLayer(canvas.toTexture());
        rotlayer.setBatch(rotBatch);
        game.rootLayer.addAt(rotlayer, 25 + 2*dx + orange.width(), 25);

        // add an immediate layer that draws a quad and an image (which should rotate)
        Layer irotlayer = new Layer() {
          protected void paintImpl (Surface surf) {
            surf.setFillColor(0xFFCC99FF).fillRect(0, 0, otex.displayWidth, otex.displayHeight);
            surf.draw(otex, 0, 0);
          }
        };
        irotlayer.setBatch(rotBatch);
        game.rootLayer.addAt(irotlayer, 25 + 3*dx + orange.width(), 25);

        conns.add(game.paint.connect(new Slot<Clock>() {
          public void onEmit (Clock clock) {
            rotBatch.elapsed = clock.tick/1000f;
          }
        }));
      }
    });
  }

  protected RotYBatch createRotBatch () {
    return new RotYBatch(game.graphics.gl);
  }

  // a batch with a shader that rotates things around the (3D) y axis
  protected static class RotYBatch extends TriangleBatch {
    public static class Source extends TriangleBatch.Source {
      @Override public String vertex () {
      return (VERT_UNIFS +
              "uniform float u_Angle;\n" +
              "uniform vec2 u_Eye;\n" +
              VERT_ATTRS +
              PER_VERT_ATTRS +
              VERT_VARS +
              "void main(void) {\n" +
              VERT_ROTSETPOS +
              VERT_SETTEX +
              VERT_SETCOLOR +
              "}");
      }

      protected static final String VERT_ROTSETPOS =
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
        "  pos.xy /= u_HScreenSize.xy;\n" +
        "  pos.z  /= u_HScreenSize.y;\n" +
        "  pos.xy -= 1.0;\n" +
        "  pos.y *= u_Flip;\n" +
        "  gl_Position = pos;\n";
    }

    public float elapsed;
    public float eyeX, eyeY;

    public final int uAngle;
    public final int uEye;

    public RotYBatch (GL20 gl) {
      super(gl, new Source());
      uAngle = program.getUniformLocation("u_Angle");
      uEye = program.getUniformLocation("u_Eye");
    }

    @Override public void begin (float fbufWidth, float fbufHeight, boolean flip) {
      super.begin(fbufWidth, fbufHeight, flip);
      gl.glUniform1f(uAngle, elapsed * FloatMath.PI);
      gl.glUniform2f(uEye, eyeX, eyeY);
    }
  }
}
