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
package playn.android;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import android.util.Log;

import playn.core.InternalTransform;
import playn.core.gl.GL20;
import playn.core.gl.GLShader;
import playn.core.gl.IndexedTrisShader;

/**
 * Implements shaders for Android.
 */
public class AndroidGLShader extends IndexedTrisShader
{
  static class Texture extends AndroidGLShader implements GLShader.Texture {
    private int uTexture, uAlpha, lastTex;
    private float lastAlpha;

    Texture(AndroidGLContext ctx) {
      super(ctx, TEX_FRAG_SHADER);
      uTexture = gl20.glGetUniformLocation(program, "u_Texture");
      uAlpha = gl20.glGetUniformLocation(program, "u_Alpha");
    }

    @Override
    public void flush() {
      gl20.glBindTexture(GL20.GL_TEXTURE_2D, lastTex);
      super.flush();
    }

    @Override
    public void prepare(Object texObj, float alpha, int fbufWidth, int fbufHeight) {
      ctx.checkGLError("textureShader.prepare start");
      boolean wasntAlreadyActive = super.prepare(fbufWidth, fbufHeight);
      if (wasntAlreadyActive) {
        gl20.glActiveTexture(GL20.GL_TEXTURE0);
        gl20.glUniform1i(uTexture, 0);
      }

      int tex = (Integer) texObj;
      if (wasntAlreadyActive || tex != lastTex || alpha != lastAlpha) {
        flush();
        gl20.glUniform1f(uAlpha, alpha);
        lastAlpha = alpha;
        lastTex = tex;
        ctx.checkGLError("textureShader.prepare end");
      }
    }
  }

  static class Color extends AndroidGLShader implements GLShader.Color {
    private int uColor, uAlpha, lastColor;
    private FloatBuffer colors = FloatBuffer.allocate(4);
    private float lastAlpha;

    Color(AndroidGLContext ctx) {
      super(ctx, COLOR_FRAG_SHADER);
      uColor = gl20.glGetUniformLocation(program, "u_Color");
      uAlpha = gl20.glGetUniformLocation(program, "u_Alpha");
    }

    @Override
    public void prepare(int color, float alpha, int fbufWidth, int fbufHeight) {
      ctx.checkGLError("colorShader.prepare start");
      boolean wasntAlreadyActive = super.prepare(fbufWidth, fbufHeight);
      if (wasntAlreadyActive || color != lastColor || alpha != lastAlpha) {
        flush();
        gl20.glUniform1f(uAlpha, alpha);
        lastAlpha = alpha;
        float a = (float) ((color >> 24) & 0xff) / 255;
        float r = (float) ((color >> 16) & 0xff) / 255;
        float g = (float) ((color >> 8) & 0xff) / 255;
        float b = (float) ((color >> 0) & 0xff) / 255;
        gl20.glUniform4f(uColor, r, g, b, a);
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

  protected final AndroidGLContext ctx;
  protected final AndroidGL20 gl20;
  protected final int program, uScreenSizeLoc, aMatrix, aTranslation, aPosition, aTexture;

  protected FloatBuffer vertexData;
  protected ShortBuffer elementData;
  protected final int vertexBuffer, elementBuffer;
  protected int vertexOffset, elementOffset;

  protected AndroidGLShader(AndroidGLContext ctx, String fragShader) {
    this.ctx = ctx;
    this.gl20 = ctx.gl20;
    program = createProgram(VERTEX_SHADER, fragShader);

    // glGet*() calls are slow; determine locations once.
    uScreenSizeLoc = gl20.glGetUniformLocation(program, "u_ScreenSize");
    aMatrix = gl20.glGetAttribLocation(program, "a_Matrix");
    aTranslation = gl20.glGetAttribLocation(program, "a_Translation");
    aPosition = gl20.glGetAttribLocation(program, "a_Position");
    aTexture = gl20.glGetAttribLocation(program, "a_Texture");

    // create the vertex and index buffer handles and buffers
    int[] buffers = new int[2];
    gl20.glGenBuffers(2, buffers, 0);
    vertexBuffer = buffers[0];
    elementBuffer = buffers[1];
    expandVerts(START_VERTS);
    expandElems(START_ELEMS);
  }

  protected boolean prepare(int fbufWidth, int fbufHeight) {
    if (ctx.useShader(this) && gl20.glIsProgram(program)) {
      gl20.glUseProgram(program);
      ctx.checkGLError("Shader.prepare useProgram");
      // Couldn't get glUniform2fv to work for whatever reason.
      gl20.glUniform2f(uScreenSizeLoc, fbufWidth, fbufHeight);

      ctx.checkGLError("Shader.prepare uScreenSizeLoc vector set to " + fbufWidth + " " + fbufHeight);

      gl20.glBindBuffer(GL20.GL_ARRAY_BUFFER, vertexBuffer);
      gl20.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, elementBuffer);

      ctx.checkGLError("Shader.prepare BindBuffer");

      gl20.glEnableVertexAttribArray(aMatrix);
      gl20.glEnableVertexAttribArray(aTranslation);
      gl20.glEnableVertexAttribArray(aPosition);
      if (aTexture != -1)
        gl20.glEnableVertexAttribArray(aTexture);

      ctx.checkGLError("Shader.prepare AttribArrays enabled");

      gl20.glVertexAttribPointer(aMatrix, 4, GL20.GL_FLOAT, false, VERTEX_STRIDE, 0);
      gl20.glVertexAttribPointer(aTranslation, 2, GL20.GL_FLOAT, false, VERTEX_STRIDE, 16);
      gl20.glVertexAttribPointer(aPosition, 2, GL20.GL_FLOAT, false, VERTEX_STRIDE, 24);
      if (aTexture != -1)
        gl20.glVertexAttribPointer(aTexture, 2, GL20.GL_FLOAT, false, VERTEX_STRIDE, 32);
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
    gl20.glBufferData(GL20.GL_ARRAY_BUFFER, vertexOffset * FLOAT_SIZE_BYTES, vertexData,
                      GL20.GL_STREAM_DRAW);
    gl20.glBufferData(GL20.GL_ELEMENT_ARRAY_BUFFER, elementOffset * SHORT_SIZE_BYTES,
                      elementData, GL20.GL_STREAM_DRAW);
    ctx.checkGLError("Shader.flush BufferData");
    gl20.glDrawElements(GL20.GL_TRIANGLES, elementOffset, GL20.GL_UNSIGNED_SHORT, 0);
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

  private int loadShader(int type, final String shaderSource) {
    int shader;

    // Create the shader object
    shader = gl20.glCreateShader(type);
    if (shader == 0)
      return 0;

    // Load the shader source
    gl20.glShaderSource(shader, shaderSource);

    // Compile the shader
    gl20.glCompileShader(shader);

    IntBuffer compiled = IntBuffer.allocate(1);
    gl20.glGetShaderiv(shader, GL20.GL_COMPILE_STATUS, compiled);

    if (compiled.array()[0] == 0) { // Same as gfx.GL_FALSE
      Log.e(this.getClass().getName(), "Could not compile shader " + type + ":");
      Log.e(this.getClass().getName(), gl20.glGetShaderInfoLog(shader));
      gl20.glDeleteShader(shader);
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

  // Creates program object, attaches shaders, and links into pipeline
  protected int createProgram(String vertexSource, String fragmentSource) {
    // Load the vertex and fragment shaders
    int vertexShader = loadShader(GL20.GL_VERTEX_SHADER, vertexSource);
    int fragmentShader = loadShader(GL20.GL_FRAGMENT_SHADER, fragmentSource);
    // Create the program object
    int program = gl20.glCreateProgram();
    if (vertexShader == 0 || fragmentShader == 0 || program == 0)
      return 0;

    if (program != 0) {
      gl20.glAttachShader(program, vertexShader);
      ctx.checkGLError("createProgram Attaching vertex shader");
      gl20.glAttachShader(program, fragmentShader);
      ctx.checkGLError("createProgram Attaching fragment shader");
      gl20.glLinkProgram(program);
      IntBuffer linkStatus = IntBuffer.allocate(1);
      gl20.glGetProgramiv(program, GL20.GL_LINK_STATUS, linkStatus);
      if (linkStatus.array()[0] != GL20.GL_TRUE) {
        Log.e(this.getClass().getName(), "Could not link program: ");
        Log.e(this.getClass().getName(), gl20.glGetProgramInfoLog(program));
        gl20.glDeleteProgram(program);
        program = 0;
      }
    }
    return program;
  }
}
