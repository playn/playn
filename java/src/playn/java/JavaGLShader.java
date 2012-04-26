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
package playn.java;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

import playn.core.InternalTransform;
import playn.core.gl.GLShader;
import playn.core.gl.IndexedTrisShader;
import static playn.core.PlayN.*;

/**
 * Implements shaders for Java.
 */
public class JavaGLShader extends IndexedTrisShader {

  private static final int SHADER_INFO_LOG_LEN = 4096;

  static class Texture extends JavaGLShader implements GLShader.Texture {
    private int uTexture, uAlpha, lastTex;
    private float lastAlpha;

    Texture(JavaGLContext ctx) {
      super(ctx, TEX_FRAG_SHADER);
      uTexture = glGetUniformLocation(program, "u_Texture");
      uAlpha = glGetUniformLocation(program, "u_Alpha");
    }

    @Override
    public void flush() {
      glBindTexture(GL_TEXTURE_2D, lastTex);
      super.flush();
    }

    @Override
    public void prepare(Object texObj, float alpha, int fbufWidth, int fbufHeight) {
      ctx.checkGLError("textureShader.prepare start");
      boolean wasntAlreadyActive = super.prepare(fbufWidth, fbufHeight);
      if (wasntAlreadyActive) {
        glActiveTexture(GL_TEXTURE0);
        glUniform1i(uTexture, 0);
      }

      int tex = (Integer) texObj;
      if (wasntAlreadyActive || tex != lastTex || alpha != lastAlpha) {
        flush();
        glUniform1f(uAlpha, alpha);
        lastAlpha = alpha;
        lastTex = tex;
        ctx.checkGLError("textureShader.prepare end");
      }
    }
  }

  static class Color extends JavaGLShader implements GLShader.Color {
    private int uColor, uAlpha, lastColor;
    private float lastAlpha;

    Color(JavaGLContext ctx) {
      super(ctx, COLOR_FRAG_SHADER);
      uColor = glGetUniformLocation(program, "u_Color");
      uAlpha = glGetUniformLocation(program, "u_Alpha");
    }

    @Override
    public void prepare(int color, float alpha, int fbufWidth, int fbufHeight) {
      ctx.checkGLError("colorShader.prepare start");
      boolean wasntAlreadyActive = super.prepare(fbufWidth, fbufHeight);
      if (wasntAlreadyActive || color != lastColor || alpha != lastAlpha) {
        flush();
        glUniform1f(uAlpha, alpha);
        lastAlpha = alpha;
        float a = (float) ((color >> 24) & 0xff) / 255;
        float r = (float) ((color >> 16) & 0xff) / 255;
        float g = (float) ((color >> 8) & 0xff) / 255;
        float b = (float) ((color >> 0) & 0xff) / 255;
        glUniform4f(uColor, r, g, b, a);
        lastColor = color;
        ctx.checkGLError("colorShader.prepare end");
      }
    }
  }

  private static final int VERTEX_SIZE = 10; // 10 floats per vertex
  private static final int START_VERTS = 16*4;
  private static final int EXPAND_VERTS = 16*4;
  private static final int START_ELEMS = 6*START_VERTS/4;
  private static final int EXPAND_ELEMS = 6*EXPAND_VERTS/4;
  private static final int FLOAT_SIZE_BYTES = 4;
  private static final int SHORT_SIZE_BYTES = 2;
  private static final int VERTEX_STRIDE = VERTEX_SIZE * FLOAT_SIZE_BYTES;

  protected final JavaGLContext ctx;
  protected final int program, uScreenSizeLoc, aMatrix, aTranslation, aPosition, aTexture;

  protected FloatBuffer vertexData;
  protected ShortBuffer elementData;
  protected final int vertexBuffer, elementBuffer;
  protected int vertexOffset, elementOffset;

  protected JavaGLShader(JavaGLContext ctx, String fragShader) {
    this.ctx = ctx;
    program = createProgram(VERTEX_SHADER, fragShader);

    // glGet*() calls are slow; determine locations once.
    uScreenSizeLoc = glGetUniformLocation(program, "u_ScreenSize");
    aMatrix = glGetAttribLocation(program, "a_Matrix");
    aTranslation = glGetAttribLocation(program, "a_Translation");
    aPosition = glGetAttribLocation(program, "a_Position");
    aTexture = glGetAttribLocation(program, "a_Texture");

    // Create the vertex and index buffers
    vertexBuffer = glGenBuffers();
    elementBuffer = glGenBuffers();
    expandVerts(START_VERTS);
    expandElems(START_ELEMS);
  }

  protected boolean prepare(int fbufWidth, int fbufHeight) {
    if (ctx.useShader(this) && glIsProgram(program)) {
      glUseProgram(program);
      ctx.checkGLError("Shader.prepare useProgram");

      glUniform2f(uScreenSizeLoc, fbufWidth, fbufHeight);
      // ctx.checkGLError("Shader.prepare uScreenSizeLoc set to " + fbufWidth + " " + fbufHeight);

      glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer);
      glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, elementBuffer);
      ctx.checkGLError("Shader.prepare BindBuffer");

      glEnableVertexAttribArray(aMatrix);
      glEnableVertexAttribArray(aTranslation);
      glEnableVertexAttribArray(aPosition);
      if (aTexture != -1)
        glEnableVertexAttribArray(aTexture);
      ctx.checkGLError("Shader.prepare AttribArrays enabled");

      glVertexAttribPointer(aMatrix, 4, GL_FLOAT, false, VERTEX_STRIDE, 0);
      glVertexAttribPointer(aTranslation, 2, GL_FLOAT, false, VERTEX_STRIDE, 16);
      glVertexAttribPointer(aPosition, 2, GL_FLOAT, false, VERTEX_STRIDE, 24);
      if (aTexture != -1)
        glVertexAttribPointer(aTexture, 2, GL_FLOAT, false, VERTEX_STRIDE, 32);
      ctx.checkGLError("Shader.prepare AttribPointer");
      return true;
    }
    return false;
  }

  @Override
  public void flush() {
    if (vertexOffset == 0) {
      return;
    }
    ctx.checkGLError("Shader.flush");

    vertexData.position(0);
    glBufferData(GL_ARRAY_BUFFER, vertexData, GL_STREAM_DRAW);
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementData, GL_STREAM_DRAW);
    ctx.checkGLError("Shader.flush BufferData");

    glDrawElements(GL_TRIANGLES, elementOffset, GL_UNSIGNED_SHORT, 0);
    vertexOffset = elementOffset = 0;
    ctx.checkGLError("Shader.flush DrawElements");
  }

  @Override
  protected int beginPrimitive(int vertexCount, int elemCount) {
    int vertIdx = vertexOffset / VERTEX_SIZE;
    int verts = vertIdx + vertexCount, elems = elementOffset + elemCount;
    int availVerts = vertexData.capacity() / VERTEX_SIZE, availElems = elementData.capacity();
    if ((verts > availVerts) || (elems > availElems)) {
      flush();
      if (vertexCount > availVerts)
        expandVerts(vertexCount);
      if (elemCount > availElems)
        expandElems(elemCount);
      return 0;
    }
    return vertIdx;
  }

  @Override
  protected void addVertex(float m00, float m01, float m10, float m11, float tx, float ty,
                           float dx, float dy, float sx, float sy) {
    vertexData.position(vertexOffset);
    vertexData.put(m00);
    vertexData.put(m01);
    vertexData.put(m10);
    vertexData.put(m11);
    vertexData.put(tx);
    vertexData.put(ty);
    vertexData.put(dx);
    vertexData.put(dy);
    vertexData.put(sx);
    vertexData.put(sy);
    vertexOffset += VERTEX_SIZE;
  }

  @Override
  protected void addElement(int index) {
    elementData.position(elementOffset);
    elementData.put((short) index);
    elementOffset++;
    elementData.position(0);
  }

  // Creates program object, attaches shaders, and links into pipeline
  protected int createProgram(String vertexSource, String fragmentSource) {
    // Load the vertex and fragment shaders
    int vertexShader = loadShader(GL_VERTEX_SHADER, vertexSource);
    int fragmentShader = loadShader(GL_FRAGMENT_SHADER, fragmentSource);
    // Create the program object
    int program = glCreateProgram();
    if (vertexShader == 0 || fragmentShader == 0 || program == 0)
      return 0;

    if (program != 0) {
      glAttachShader(program, vertexShader);
      ctx.checkGLError("createProgram Attaching vertex shader");
      glAttachShader(program, fragmentShader);
      ctx.checkGLError("createProgram Attaching fragment shader");
      glLinkProgram(program);

      int linkStatus = glGetProgram(program, GL_LINK_STATUS);
      if (linkStatus != GL_TRUE) {
        log().error("Could not link program: ");
        log().error(glGetProgramInfoLog(program, SHADER_INFO_LOG_LEN));
        glDeleteProgram(program);
        program = 0;
      }
    }
    return program;
  }

  private int loadShader(int type, final String shaderSource) {
    // Create the shader object
    int shader = glCreateShader(type);
    if (shader == 0)
      return 0;

    // Load the shader source
    glShaderSource(shader, shaderSource);

    // Compile the shader
    glCompileShader(shader);

    int compiled = glGetShader(shader, GL_COMPILE_STATUS);
    if (compiled == 0) { // Same as gfx.GL_FALSE
      log().error("Could not compile shader " + type + ":");
      log().error(glGetShaderInfoLog(shader, SHADER_INFO_LOG_LEN));
      glDeleteShader(shader);
      shader = 0;
    }

    return shader;
  }

  private void expandVerts(int vertCount) {
    int newVerts = vertexData == null ? 0 : vertexData.capacity() / VERTEX_SIZE;
    while (newVerts < vertCount)
      newVerts += EXPAND_VERTS;
    vertexData = ByteBuffer.allocateDirect(newVerts * VERTEX_STRIDE).order(
      ByteOrder.nativeOrder()).asFloatBuffer();
  }

  private void expandElems(int elemCount) {
    int newElems = elementData == null ? 0 : elementData.capacity();
    while (newElems < elemCount)
      newElems += EXPAND_ELEMS;
    elementData = ByteBuffer.allocateDirect(newElems * SHORT_SIZE_BYTES).order(
      ByteOrder.nativeOrder()).asShortBuffer();
  }
}
