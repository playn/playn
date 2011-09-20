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

import static android.opengl.GLUtils.texImage2D;
import static playn.core.PlayN.log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.HashSet;
import java.util.Set;

import playn.core.Asserts;
import playn.core.CanvasImage;
import playn.core.CanvasLayer;
import playn.core.Font;
import playn.core.Gradient;
import playn.core.Graphics;
import playn.core.GroupLayer;
import playn.core.Image;
import playn.core.ImageLayer;
import playn.core.InternalTransform;
import playn.core.Path;
import playn.core.Pattern;
import playn.core.StockInternalTransform;
import playn.core.Surface;
import playn.core.SurfaceLayer;
import playn.core.TextFormat;
import playn.core.TextLayout;
import playn.core.gl.GL20;
import android.graphics.Bitmap;
import android.graphics.LinearGradient;
import android.graphics.RadialGradient;
import android.graphics.Shader.TileMode;
import android.util.Log;
import android.view.View;

class AndroidGraphics implements Graphics {
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

  private static int startingScreenWidth;
  private static int startingScreenHeight;

  protected final AndroidGL20 gl20;
  final AndroidGroupLayer rootLayer;
  private final GameViewGL gameView;
  private int viewWidth, viewHeight, lastFrameBuffer, screenWidth, screenHeight, fbufWidth,
      fbufHeight;
  private boolean sizeSetManually = false;
  private Set<Surface> surfaces = new HashSet<Surface>();

  // Debug
  private int texCount;

  // Shaders & Meshes
  private Shader curShader;
  private TextureShader texShader;
  private ColorShader colorShader;

  public AndroidGraphics(AndroidGL20 gfx) {
    this.gl20 = gfx;
    gameView = AndroidPlatform.instance.activity.gameView();
    rootLayer = new AndroidGroupLayer(this);
    if (startingScreenWidth != 0)
      fbufWidth = viewWidth = screenWidth = startingScreenWidth;
    if (startingScreenHeight != 0)
      fbufHeight = viewHeight = screenHeight = startingScreenHeight;
    initGL();

    shaderAssetManager.setPathPrefix(Shaders.pathPrefix);
    generateShaders();
  }

  private void generateShaders() {
    texShader = new TextureShader();
    colorShader = new ColorShader();
  }

  @Override
  public CanvasImage createImage(int w, int h) {
    return new AndroidImage(w, h, true);
  }

  public CanvasImage createImage(int w, int h, boolean alpha) {
    return new AndroidImage(w, h, alpha);
  }

  @Override
  public Gradient createLinearGradient(float x0, float y0, float x1, float y1, int[] colors,
      float[] positions) {
    LinearGradient gradient = new LinearGradient(x0, y0, x1, y1, colors, positions, TileMode.CLAMP);
    return new AndroidGradient(gradient);
  }

  @Override
  public Path createPath() {
    return new AndroidPath();
  }

  @Override
  public Pattern createPattern(Image img) {
    Asserts.checkArgument(img instanceof AndroidImage);
    return new AndroidPattern(img);
  }

  @Override
  public Gradient createRadialGradient(float x, float y, float r, int[] colors, float[] positions) {
    RadialGradient gradient = new RadialGradient(x, y, r, colors, positions, TileMode.CLAMP);
    return new AndroidGradient(gradient);
  }

  @Override
  public Font createFont(String name, Font.Style style, float size) {
    return new AndroidFont(name, style, size);
  }

  @Override
  public TextLayout layoutText(String text, TextFormat format) {
    return new AndroidTextLayout(text, format);
  }

  /**
   * @return The height of the AndroidLayoutView containing the GameView (generally the
   *         entire display height) in pixels.
   */
  @Override
  public int screenHeight() {
    return screenHeight;
  }

  /**
   * @return The width of the AndroidLayoutView containing the GameView (generally the
   *         entire display width) in pixels.
   */
  @Override
  public int screenWidth() {
    return screenWidth;
  }

  @Override
  public CanvasLayer createCanvasLayer(int width, int height) {
    return new AndroidCanvasLayer(this, width, height, true);
  }

  public CanvasLayer createCanvasLayer(int width, int height, boolean alpha) {
    return new AndroidCanvasLayer(this, width, height, alpha);
  }

  @Override
  public GroupLayer createGroupLayer() {
    return new AndroidGroupLayer(this);
  }

  @Override
  public ImageLayer createImageLayer() {
    return new AndroidImageLayer(this);
  }

  @Override
  public ImageLayer createImageLayer(Image image) {
    return new AndroidImageLayer(this, (AndroidImage) image);
  }

  @Override
  public SurfaceLayer createSurfaceLayer(int width, int height) {
    return new AndroidSurfaceLayer(this, width, height);
  }

  /**
   * @return The height of the GameView itself in pixels. Defaults to the value of
   *         screenHeight(), but can be set using setSize()
   */
  @Override
  public int height() {
    return viewHeight;
  }

  /**
   * @return The width of the GameView itself in pixels. Defaults to the value of
   *         screenWidth(), but can be set using setSize()
   */
  @Override
  public int width() {
    return viewWidth;
  }

  @Override
  public GroupLayer rootLayer() {
    return rootLayer;
  }

  void refreshScreenSize(boolean resize) {
    View viewLayout = AndroidPlatform.instance.activity.viewLayout();
    int oldWidth = screenWidth;
    int oldHeight = screenHeight;
    screenWidth = viewLayout.getWidth();
    screenHeight = viewLayout.getHeight();
    AndroidPlatform.instance.touchEventHandler().calculateOffsets();
    // Change game size to fill the screen if it has never been set manually.
    if (resize && !sizeSetManually && (screenWidth != oldWidth || screenHeight != oldHeight))
      setSize(screenWidth, screenHeight, false);
  }

  public void refreshScreenSize() {
    refreshScreenSize(true);
  }

  /*
   * Public manual setSize function. Once this is called, automatic calls to
   * refreshScreenSize() when something changes the size of the gameView will
   * not force a call to setSize.
   */
  @Override
  public void setSize(int width, int height) {
    setSize(width, height, true);
  }

  private void setSize(int width, int height, boolean manual) {
    if (manual)
      sizeSetManually = true;
    gameView.gameSizeSet();
    this.viewWidth = width;
    this.viewHeight = height;
    AndroidPlatform.instance.touchEventHandler().calculateOffsets();
    // Layout the views again to change the surface size
    AndroidPlatform.instance.activity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        View viewLayout = AndroidPlatform.instance.activity.viewLayout();
        viewLayout.measure(viewLayout.getMeasuredWidth(), viewLayout.getMeasuredHeight());
        viewLayout.requestLayout();
      }
    });
    bindFramebuffer(0, width, height, true);
  }

  /**
   * Called by AndroidViewLayout to make sure that AndroidGraphics is
   * initialized with non-zero screen dimensions.
   *
   * @param width
   * @param height
   */
  static void setStartingScreenSize(int width, int height) {
    startingScreenWidth = width;
    startingScreenHeight = height;
  }

  void bindFramebuffer() {
    bindFramebuffer(0, viewWidth, viewHeight);
  }

  void bindFramebuffer(int frameBuffer, int width, int height) {
    bindFramebuffer(frameBuffer, width, height, false);
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

  int createTexture(boolean repeatX, boolean repeatY) {
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

  void destroyTexture(int texture) {
    //Flush in case this texture is queued up to be drawn
    flush();
    gl20.glDeleteTextures(1, new int[] {texture}, 0);
    --texCount;
    if (AndroidPlatform.DEBUG_LOGS) log().debug(texCount + " textures remain.");
  }

  void paintLayers() {
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

  void refreshGL() {
    generateShaders();
    initGL();
    refreshSurfaces();
  }

  void updateTexture(int texture, Bitmap image) {
    gl20.glBindTexture(GL20.GL_TEXTURE_2D, texture);
    texImage2D(GL20.GL_TEXTURE_2D, 0, image, 0);
    checkGlError("updateTexture end");
  }

  void drawTexture(int texture, float texWidth, float texHeight, InternalTransform local, float dw,
      float dh, boolean repeatX, boolean repeatY, float alpha) {
    drawTexture(texture, texWidth, texHeight, local, 0, 0, dw, dh, repeatX, repeatY, alpha);
  }

  void drawTexture(int texture, float texWidth, float texHeight, InternalTransform local, float dx,
      float dy, float dw, float dh, boolean repeatX, boolean repeatY, float alpha) {
    float sw = repeatX ? dw : texWidth, sh = repeatY ? dh : texHeight;
    drawTexture(texture, texWidth, texHeight, local, dx, dy, dw, dh, 0, 0, sw, sh, alpha);
  }

  void drawTexture(int texture, float texWidth, float texHeight, InternalTransform local, float dx,
      float dy, float dw, float dh, float sx, float sy, float sw, float sh, float alpha) {
    checkGlError("drawTexture start");
    texShader.prepare(texture, alpha);
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

  void fillRect(InternalTransform local, float dx, float dy, float dw, float dh, float texWidth,
      float texHeight, int texture, float alpha) {
    texShader.prepare(texture, alpha);

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

  void fillRect(InternalTransform local, float dx, float dy, float dw, float dh, int color,
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

  /*
   * Currently only used by AndroidSurface.drawLine()
   */
  void fillPoly(InternalTransform local, float[] positions, int color, float alpha) {
    colorShader.prepare(color, alpha);

    // FIXME: Rewrite to take advantage of GL_TRIANGLE_STRIP
    int idx = colorShader.beginPrimitive(4, 6); // FIXME: This won't scale for
                                                // non-line polys.
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

  void flush() {
    if (curShader != null) {
      checkGlError("flush()");
      curShader.flush();
      curShader = null;
    }
  }

  private void initGL() {
    gl20.glDisable(GL20.GL_CULL_FACE);
    gl20.glEnable(GL20.GL_BLEND);
    gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    gl20.glClearColor(0, 0, 0, 1);
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

  /*
   * Methods to store and refresh surfaces when the GL context
   * is destroyed and refreshed.
   */
  void addSurface(Surface surface) {
    if (surface != null) surfaces.add(surface);
  }

  void removeSurface(Surface surface) {
    surfaces.remove(surface);
  }

  void refreshSurfaces() {
    for (Surface surface : surfaces) {
      Asserts.check(surface instanceof AndroidSurface);
      AndroidSurface asurf = (AndroidSurface) surface;
      asurf.checkRefreshGL();
    }
  }

  void storeSurfaces() {
    for (Surface surface : surfaces) {
      Asserts.check(surface instanceof AndroidSurface);
      AndroidSurface asurf = (AndroidSurface) surface;
      asurf.storePixels();
    }
  }
}
