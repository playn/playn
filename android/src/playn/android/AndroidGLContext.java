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
package playn.android;

import static playn.core.PlayN.log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.*;

import android.graphics.Bitmap;
import android.opengl.GLUtils;
import android.util.Log;

import playn.core.Asserts;
import playn.core.InternalTransform;
import playn.core.StockInternalTransform;
import playn.core.gl.GL20;
import playn.core.gl.GLContext;
import playn.core.gl.GroupLayerGL;

/**
 * Implements the GL context via Android OpenGL bindings.
 */
public class AndroidGLContext extends GLContext
{
  /** An interface implemented by entities that need to store things when our GL context is lost
   * and restore them when we are given a new context. */
  public interface Refreshable {
    /** Called when our GL context is about to go away. */
    void onSurfaceLost();
    /** Called when we have been given a new GL context. */
    void onSurfaceCreated();
  }

  public static final boolean CHECK_ERRORS = true;

  private static final int VERTEX_SIZE = 10; // 10 floats per vertex
  private static final int MAX_VERTS = 4;
  private static final int MAX_ELEMS = 6;
  private static final int FLOAT_SIZE_BYTES = 4;
  private static final int SHORT_SIZE_BYTES = 2;
  private static final int VERTEX_STRIDE = VERTEX_SIZE * FLOAT_SIZE_BYTES;

  private static AndroidAssetManager shaderAssetManager = new AndroidAssetManager();
  private static ShaderCallback shaderCallback = new ShaderCallback();

  private class Shader {
    int program, vertexBuffer, elementBuffer, vertexOffset, elementOffset, uScreenSizeLoc, aMatrix,
        aTranslation, aPosition, aTexture;
    FloatBuffer vertexData = ByteBuffer.allocateDirect(VERTEX_STRIDE * MAX_VERTS).order(
        ByteOrder.nativeOrder()).asFloatBuffer();
    ShortBuffer elementData = ByteBuffer.allocateDirect(MAX_ELEMS * SHORT_SIZE_BYTES).order(
        ByteOrder.nativeOrder()).asShortBuffer();

    Shader(String fragShaderName) {
      shaderAssetManager.getText(Shaders.vertexShader, shaderCallback);
      String vertexShader = shaderCallback.shader();
      shaderAssetManager.getText(fragShaderName, shaderCallback);
      String fragShader = shaderCallback.shader();
      program = createProgram(vertexShader, fragShader);

      // glGet*() calls are slow; determine locations once.
      uScreenSizeLoc = gl20.glGetUniformLocation(program, "u_ScreenSize");
      aMatrix = gl20.glGetAttribLocation(program, "a_Matrix");
      aTranslation = gl20.glGetAttribLocation(program, "a_Translation");
      aPosition = gl20.glGetAttribLocation(program, "a_Position");
      aTexture = gl20.glGetAttribLocation(program, "a_Texture");

      // Create the vertex and index buffers
      int[] buffers = new int[2];
      gl20.glGenBuffers(2, buffers, 0);
      vertexBuffer = buffers[0];
      elementBuffer = buffers[1];
    }

    boolean prepare() {
      if (useShader(this) && gl20.glIsProgram(program)) {
        gl20.glUseProgram(program);
        checkGlError("Shader.prepare useProgram");
        // Couldn't get glUniform2fv to work for whatever reason.
        gl20.glUniform2f(uScreenSizeLoc, fbufWidth, fbufHeight);

        checkGlError("Shader.prepare uScreenSizeLoc vector set to "
          + viewWidth + " " + viewHeight);

        gl20.glBindBuffer(GL20.GL_ARRAY_BUFFER, vertexBuffer);
        gl20.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, elementBuffer);

        checkGlError("Shader.prepare BindBuffer");

        gl20.glEnableVertexAttribArray(aMatrix);
        gl20.glEnableVertexAttribArray(aTranslation);
        gl20.glEnableVertexAttribArray(aPosition);
        if (aTexture != -1)
          gl20.glEnableVertexAttribArray(aTexture);

        checkGlError("Shader.prepare AttribArrays enabled");

        gl20.glVertexAttribPointer(aMatrix, 4, GL20.GL_FLOAT, false, VERTEX_STRIDE, 0);
        gl20.glVertexAttribPointer(aTranslation, 2, GL20.GL_FLOAT, false, VERTEX_STRIDE, 16);
        gl20.glVertexAttribPointer(aPosition, 2, GL20.GL_FLOAT, false, VERTEX_STRIDE, 24);
        if (aTexture != -1)
          gl20.glVertexAttribPointer(aTexture, 2, GL20.GL_FLOAT, false, VERTEX_STRIDE, 32);
        checkGlError("Shader.prepare AttribPointer");
        return true;
      }
      return false;
    }

    void flush() {
      if (vertexOffset == 0) {
        return;
      }
      checkGlError("Shader.flush");
      gl20.glBufferData(GL20.GL_ARRAY_BUFFER, vertexOffset * FLOAT_SIZE_BYTES, vertexData,
          GL20.GL_STREAM_DRAW);
      gl20.glBufferData(GL20.GL_ELEMENT_ARRAY_BUFFER, elementOffset * SHORT_SIZE_BYTES,
          elementData, GL20.GL_STREAM_DRAW);
      checkGlError("Shader.flush BufferData");
      gl20.glDrawElements(GL20.GL_TRIANGLE_STRIP, elementOffset, GL20.GL_UNSIGNED_SHORT, 0);
      vertexOffset = elementOffset = 0;
      checkGlError("Shader.flush DrawElements");
    }

    int beginPrimitive(int vertexCount, int elemCount) {
      int vertIdx = vertexOffset / VERTEX_SIZE;
      if ((vertIdx + vertexCount > MAX_VERTS) || (elementOffset + elemCount > MAX_ELEMS)) {
        flush();
        return 0;
      }
      return vertIdx;
    }

    void buildVertex(InternalTransform local, float dx, float dy) {
      buildVertex(local, dx, dy, 0, 0);
    }

    void buildVertex(InternalTransform local, float dx, float dy, float sx, float sy) {
      vertexData.position(vertexOffset);
      vertexData.put(local.m00());
      vertexData.put(local.m01());
      vertexData.put(local.m10());
      vertexData.put(local.m11());
      vertexData.put(local.tx());
      vertexData.put(local.ty());
      vertexData.put(dx);
      vertexData.put(dy);
      vertexData.put(sx);
      vertexData.put(sy);
      vertexData.position(0);

      vertexOffset += VERTEX_SIZE;

    }

    void addElement(int index) {
      elementData.position(elementOffset);
      elementData.put((short) index);
      elementOffset++;
      elementData.position(0);
    }

    /**
     * Methods for building shaders and programs
     */

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
        checkGlError("createProgram Attaching vertex shader");
        gl20.glAttachShader(program, fragmentShader);
        checkGlError("createProgram Attaching fragment shader");
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

  private class TextureShader extends Shader {
    int uTexture, uAlpha, lastTex;
    float lastAlpha;

    TextureShader() {
      super(Shaders.texFragmentShader);
      uTexture = gl20.glGetUniformLocation(program, "u_Texture");
      uAlpha = gl20.glGetUniformLocation(program, "u_Alpha");
    }

    @Override
    void flush() {
      gl20.glBindTexture(GL20.GL_TEXTURE_2D, lastTex);
      super.flush();
    }

    void prepare(int tex, float alpha) {
      checkGlError("textureShader.prepare start");
      if (super.prepare()) {
        gl20.glActiveTexture(GL20.GL_TEXTURE0);
        gl20.glUniform1i(uTexture, 0);
      }

      if (tex == lastTex && alpha == lastAlpha)
        return;
      flush();

      gl20.glUniform1f(uAlpha, alpha);
      lastAlpha = alpha;
      lastTex = tex;
      checkGlError("textureShader.prepare end");
    }

  }

  private class ColorShader extends Shader {
    int uColor, uAlpha, lastColor;
    FloatBuffer colors = FloatBuffer.allocate(4);
    float lastAlpha;

    ColorShader() {
      super(Shaders.colorFragmentShader);
      uColor = gl20.glGetUniformLocation(program, "u_Color");
      uAlpha = gl20.glGetUniformLocation(program, "u_Alpha");
    }

    void prepare(int color, float alpha) {
      checkGlError("colorShader.prepare start");
      super.prepare();

      checkGlError("colorShader.prepare super called");

      if (color == lastColor && alpha == lastAlpha)
        return;
      flush();

      checkGlError("colorShader.prepare flushed");

      gl20.glUniform1f(uAlpha, alpha);
      lastAlpha = alpha;
      setColor(color);
      checkGlError("colorShader.prepare end");
    }

    private void setColor(int color) {
      float[] colorsArray = colors.array();
      colorsArray[3] = (float) ((color >> 24) & 0xff) / 255;
      colorsArray[0] = (float) ((color >> 16) & 0xff) / 255;
      colorsArray[1] = (float) ((color >> 8) & 0xff) / 255;
      colorsArray[2] = (float) ((color >> 0) & 0xff) / 255;
      // Still can't work out how to use glUniform4fv without generating a
      // glError, so passing the array through as individual floats
      gl20.glUniform4f(uColor, colorsArray[0], colorsArray[1], colorsArray[2], colorsArray[3]);

      lastColor = color;
    }
  }

  public int viewWidth, viewHeight;
  private int fbufWidth, fbufHeight;
  private int lastFrameBuffer;

  final AndroidGL20 gl20;

  private Map<Refreshable, Void> refreshables =
    Collections.synchronizedMap(new WeakHashMap<Refreshable, Void>());

  // Debug
  private int texCount;

  // Shaders & Meshes
  private Shader curShader;
  private TextureShader texShader;
  private ColorShader colorShader;

  AndroidGLContext(AndroidGL20 gfx, int screenWidth, int screenHeight) {
    gl20 = gfx;
    fbufWidth = viewWidth = screenWidth;
    fbufHeight = viewHeight = screenHeight;
    shaderAssetManager.setPathPrefix(Shaders.pathPrefix);
    reinitGL();
  }

  void setSize(int width, int height) {
    viewWidth = width;
    viewHeight = height;
    bindFramebuffer(0, width, height, true);
  }

  void onSurfaceCreated() {
    reinitGL();
    for (Refreshable ref : refreshables.keySet()) {
      ref.onSurfaceCreated();
    }
  }

  void onSurfaceLost() {
    for (Refreshable ref : refreshables.keySet()) {
      ref.onSurfaceLost();
    }
  }

  void paintLayers(GroupLayerGL rootLayer) {
    // Bind the default frameBuffer (the SurfaceView's Surface)
    checkGlError("updateLayers Start");

    bindFramebuffer();

    // Clear to transparent
    gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
    // Paint all the layers
    rootLayer.paint(StockInternalTransform.IDENTITY, 1);
    checkGlError("updateLayers");

    // Guarantee a flush
    useShader(null);
  }

  void updateTexture(int texture, Bitmap image) {
    gl20.glBindTexture(GL20.GL_TEXTURE_2D, texture);
    GLUtils.texImage2D(GL20.GL_TEXTURE_2D, 0, image, 0);
    checkGlError("updateTexture end");
  }

  @Override
  public Integer createFramebuffer(Object tex) {
    // Generate the framebuffer and attach the texture
    int[] fbufBuffer = new int[1];
    gl20.glGenFramebuffers(1, fbufBuffer, 0);

    int fbuf = fbufBuffer[0];
    gl20.glBindFramebuffer(GL20.GL_FRAMEBUFFER, fbuf);
    gl20.glFramebufferTexture2D(GL20.GL_FRAMEBUFFER, GL20.GL_COLOR_ATTACHMENT0,
                                GL20.GL_TEXTURE_2D, (Integer) tex, 0);

    return fbuf;
  }

  @Override
  public void deleteFramebuffer(Object fbuf) {
    gl20.glDeleteFramebuffers(1, new int[] {(Integer) fbuf}, 0);
  }

  @Override
  public void bindFramebuffer(Object fbuf, int width, int height) {
    bindFramebuffer((Integer)fbuf, width, height, false);
  }

  void bindFramebuffer() {
    bindFramebuffer(0, viewWidth, viewHeight, false);
  }

  void bindFramebuffer(int frameBuffer, int width, int height, boolean force) {
    if (force || lastFrameBuffer != frameBuffer) {
      checkGlError("bindFramebuffer");
      flush();

      lastFrameBuffer = frameBuffer;
      gl20.glBindFramebuffer(GL20.GL_FRAMEBUFFER, frameBuffer);
      gl20.glViewport(0, 0, width, height);
      fbufWidth = width;
      fbufHeight = height;
    }
  }

  @Override
  public Integer createTexture(boolean repeatX, boolean repeatY) {
    int[] texId = new int[1];
    gl20.glGenTextures(1, texId, 0);
    int texture = texId[0];
    gl20.glBindTexture(GL20.GL_TEXTURE_2D, texture);
    gl20.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAG_FILTER, GL20.GL_LINEAR);
    gl20.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MIN_FILTER, GL20.GL_LINEAR);
    gl20.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_S, repeatX ? GL20.GL_REPEAT
        : GL20.GL_CLAMP_TO_EDGE);
    gl20.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_T, repeatY ? GL20.GL_REPEAT
        : GL20.GL_CLAMP_TO_EDGE);
    ++texCount;
    if (AndroidPlatform.DEBUG_LOGS) log().debug(texCount + " textures created.");
    return texture;
  }

  @Override
  public Integer createTexture(int width, int height, boolean repeatX, boolean repeatY) {
    int tex = createTexture(repeatX, repeatY);
    gl20.glTexImage2D(GL20.GL_TEXTURE_2D, 0, GL20.GL_RGBA, width, height, 0, GL20.GL_RGBA,
                      GL20.GL_UNSIGNED_BYTE, null);
    return tex;
  }

  @Override
  public void destroyTexture(Object tex) {
    //Flush in case this texture is queued up to be drawn
    flush();
    gl20.glDeleteTextures(1, new int[] {(Integer) tex}, 0);
    --texCount;
    if (AndroidPlatform.DEBUG_LOGS) log().debug(texCount + " textures remain.");
  }

  @Override
  public void drawTexture(
      Object tex, float texWidth, float texHeight, InternalTransform local,
      float dx, float dy, float dw, float dh, float sx, float sy, float sw, float sh, float alpha) {
    checkGlError("drawTexture start");
    texShader.prepare((Integer) tex, alpha);
    sx /= texWidth;
    sw /= texWidth;
    sy /= texHeight;
    sh /= texHeight;

    int idx = texShader.beginPrimitive(4, 4);
    texShader.buildVertex(local, dx, dy, sx, sy);
    texShader.buildVertex(local, dx + dw, dy, sx + sw, sy);
    texShader.buildVertex(local, dx, dy + dh, sx, sy + sh);
    texShader.buildVertex(local, dx + dw, dy + dh, sx + sw, sy + sh);

    texShader.addElement(idx + 0);
    texShader.addElement(idx + 1);
    texShader.addElement(idx + 2);
    texShader.addElement(idx + 3);
    checkGlError("drawTexture end");
  }

  @Override
  public void fillRect(InternalTransform local, float dx, float dy, float dw, float dh,
                       float texWidth, float texHeight, Object tex, float alpha) {
    texShader.prepare((Integer) tex, alpha);

    float sx = dx / texWidth, sy = dy / texHeight;
    float sw = dw / texWidth, sh = dh / texHeight;

    int idx = texShader.beginPrimitive(4, 4);
    texShader.buildVertex(local, dx, dy, sx, sy);
    texShader.buildVertex(local, dx + dw, dy, sx + sw, sy);
    texShader.buildVertex(local, dx, dy + dh, sx, sy + sh);
    texShader.buildVertex(local, dx + dw, dy + sy, sx + sw, sy + sh);

    texShader.addElement(idx + 0);
    texShader.addElement(idx + 1);
    texShader.addElement(idx + 2);
    texShader.addElement(idx + 3);
  }

  @Override
  public void fillRect(InternalTransform local, float dx, float dy, float dw, float dh, int color,
                       float alpha) {
    colorShader.prepare(color, alpha);
    checkGlError("fillRect shader prepared");

    int idx = colorShader.beginPrimitive(4, 4);
    colorShader.buildVertex(local, dx, dy);
    colorShader.buildVertex(local, dx + dw, dy);
    colorShader.buildVertex(local, dx, dy + dh);
    colorShader.buildVertex(local, dx + dw, dy + dh);

    colorShader.addElement(idx + 0);
    colorShader.addElement(idx + 1);
    colorShader.addElement(idx + 2);
    colorShader.addElement(idx + 3);
    checkGlError("fillRect done");
  }

  @Override // Currently only used by SurfaceGL.drawLine()
  public void fillPoly(InternalTransform local, float[] positions, int color, float alpha) {
    colorShader.prepare(color, alpha);

    // FIXME: Rewrite to take advantage of GL_TRIANGLE_STRIP
    int idx = colorShader.beginPrimitive(4, 6); // FIXME: This won't work for non-line polys.
    int points = positions.length / 2;
    for (int i = 0; i < points; ++i) {
      float dx = positions[i * 2];
      float dy = positions[i * 2 + 1];
      colorShader.buildVertex(local, dx, dy);
    }

    int a = idx + 0, b = idx + 1, c = idx + 2;
    int tris = points - 2;
    for (int i = 0; i < tris; i++) {
      colorShader.addElement(a);
      colorShader.addElement(b);
      colorShader.addElement(c);
      a = c;
      b = a + 1;
      c = (i == tris - 2) ? idx : b + 1;
    }
  }

  @Override
  public void clear(float r, float g, float b, float a) {
    gl20.glClearColor(r, g, b, a);
    gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
  }

  @Override
  public void flush() {
    if (curShader != null) {
      checkGlError("flush()");
      curShader.flush();
      curShader = null;
    }
  }

  private boolean useShader(Shader shader) {
    if (curShader != shader) {
      checkGlError("useShader");
      flush();
      curShader = shader;
      return true;

    }
    return false;
  }

  void checkGlError(String op) {
    if (CHECK_ERRORS) {
      int error;
      while ((error = gl20.glGetError()) != GL20.GL_NO_ERROR) {
        log().error(this.getClass().getName() + " -- " + op + ": glError " + error);
      }
    }
  }

  void addRefreshable(Refreshable ref) {
    refreshables.put(Asserts.checkNotNull(ref), null);
  }

  void removeRefreshable(Refreshable ref) {
    refreshables.remove(Asserts.checkNotNull(ref));
  }

  private void reinitGL() {
    gl20.glDisable(GL20.GL_CULL_FACE);
    gl20.glEnable(GL20.GL_BLEND);
    gl20.glBlendFunc(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
    gl20.glClearColor(0, 0, 0, 1);
    texShader = new TextureShader();
    colorShader = new ColorShader();
  }
}
