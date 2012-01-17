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
package playn.ios;

import cli.System.Console;

import cli.System.Drawing.RectangleF;
import cli.System.IntPtr;
import cli.System.Runtime.InteropServices.GCHandle;
import cli.System.Runtime.InteropServices.GCHandleType;
import cli.System.Runtime.InteropServices.Marshal;

import cli.MonoTouch.CoreGraphics.CGBitmapContext;
import cli.MonoTouch.CoreGraphics.CGColorSpace;
import cli.MonoTouch.CoreGraphics.CGImage;
import cli.MonoTouch.CoreGraphics.CGImageAlphaInfo;
import cli.MonoTouch.UIKit.UIImage;
import cli.OpenTK.Graphics.ES20.All;
import cli.OpenTK.Graphics.ES20.GL;

import playn.core.InternalTransform;
import playn.core.PlayN;
import playn.core.StockInternalTransform;
import playn.core.gl.GLContext;
import playn.core.gl.GroupLayerGL;

class IOSGLContext extends GLContext
{
  public static final boolean CHECK_ERRORS = true;

  public int viewWidth, viewHeight;

  private int fbufWidth, fbufHeight;
  private int lastFrameBuffer;

  private Shader curShader;
  private TextureShader texShader;
  private ColorShader colorShader;

  private static final int VERTEX_SIZE = 10; // 10 floats per vertex
  private static final int MAX_VERTS = 4;
  private static final int MAX_ELEMS = 6;
  private static final int FLOAT_SIZE_BYTES = 4;
  private static final int SHORT_SIZE_BYTES = 2;
  private static final int VERTEX_STRIDE = VERTEX_SIZE * FLOAT_SIZE_BYTES;

  private static final String VERTEX_SHADER =
    "uniform vec2 u_ScreenSize;\n" +
    "attribute vec4 a_Matrix;\n" +
    "attribute vec2 a_Translation;\n" +
    "attribute vec2 a_Position;\n" +
    "attribute vec2 a_Texture;\n" +
    "varying vec2 v_TexCoord;\n" +

    "void main(void) {\n" +
    // Transform the vertex.
    "  mat3 transform = mat3(\n" +
    "    a_Matrix[0], a_Matrix[1], 0,\n" +
    "    a_Matrix[2], a_Matrix[3], 0,\n" +
    "    a_Translation[0], a_Translation[1], 1);\n" +
    "  gl_Position = vec4(transform * vec3(a_Position, 1.0), 1);\n" +
    // Scale from screen coordinates to [0, 2].
    "  gl_Position.x /= (u_ScreenSize.x / 2.0);\n" +
    "  gl_Position.y /= (u_ScreenSize.y / 2.0);\n" +
    // Offset to [-1, 1] and flip y axis to put origin at top-left.
    "  gl_Position.x -= 1.0;\n" +
    "  gl_Position.y = 1.0 - gl_Position.y;\n" +

    "  v_TexCoord = a_Texture;\n" +
    "}";

  private static final String TEX_FRAG_SHADER =
    "#ifdef GL_ES\n" +
    "precision highp float;\n" +
    "#endif\n" +

    "uniform sampler2D u_Texture;\n" +
    "varying vec2 v_TexCoord;\n" +
    "uniform float u_Alpha;\n" +

    "void main(void) {\n" +
    "  vec4 textureColor = texture2D(u_Texture, v_TexCoord);\n" +
    "  gl_FragColor = vec4(textureColor.rgb * u_Alpha, textureColor.a * u_Alpha);\n" +
    "}";

  private static final String COLOR_FRAG_SHADER =
    "#ifdef GL_ES\n" +
    "precision highp float;\n" +
    "#endif\n" +

    "uniform vec4 u_Color;\n" +
    "uniform float u_Alpha;\n" +

    "void main(void) {\n" +
    "  gl_FragColor = vec4(u_Color.rgb * u_Alpha, u_Color.a * u_Alpha);\n" +
    "}";

  private class Shader {
    protected final int program, uScreenSizeLoc, aMatrix, aTranslation, aPosition, aTexture;

    protected final float[] vertexData = new float[VERTEX_SIZE * MAX_VERTS];
    protected final GCHandle vertexHandle = GCHandle.Alloc(
      vertexData, GCHandleType.wrap(GCHandleType.Pinned));
    protected final int vertexBuffer;
    protected int vertexOffset;

    protected final short[] elementData = new short[MAX_ELEMS];
    protected final GCHandle elementHandle = GCHandle.Alloc(
      elementData, GCHandleType.wrap(GCHandleType.Pinned));
    protected final int elementBuffer;
    protected int elementOffset;

    Shader(String fragShader) {
      program = createProgram(VERTEX_SHADER, fragShader);

      uScreenSizeLoc = GL.GetUniformLocation(program, "u_ScreenSize");
      aMatrix = GL.GetAttribLocation(program, "a_Matrix");
      aTranslation = GL.GetAttribLocation(program, "a_Translation");
      aPosition = GL.GetAttribLocation(program, "a_Position");
      aTexture = GL.GetAttribLocation(program, "a_Texture");

      int[] buffers = new int[2];
      GL.GenBuffers(2, buffers);
      vertexBuffer = buffers[0];
      elementBuffer = buffers[1];
    }

    boolean prepare() {
      if (!useShader(this))
        return false;

      GL.UseProgram(program);
      checkGlError("Shader.prepare useProgram");

      GL.Uniform2(uScreenSizeLoc, (float)fbufWidth, (float)fbufHeight);
      checkGlError("Shader.prepare uScreenSizeLoc (" + uScreenSizeLoc + ") set to " +
                   viewWidth + " " + viewHeight);

      GL.BindBuffer(All.wrap(All.ArrayBuffer), vertexBuffer);
      GL.BindBuffer(All.wrap(All.ElementArrayBuffer), elementBuffer);
      checkGlError("Shader.prepare BindBuffer");

      GL.EnableVertexAttribArray(aMatrix);
      GL.EnableVertexAttribArray(aTranslation);
      GL.EnableVertexAttribArray(aPosition);
      if (aTexture != -1)
        GL.EnableVertexAttribArray(aTexture);
      checkGlError("Shader.prepare AttribArrays enabled");

      IntPtr vaddr = vertexHandle.AddrOfPinnedObject();
      long baseVAddr = vaddr.ToInt64();
      GL.VertexAttribPointer(aMatrix, 4, All.wrap(All.Float), false, VERTEX_STRIDE, vaddr);
      vaddr = new IntPtr(baseVAddr + 16);
      GL.VertexAttribPointer(aTranslation, 2, All.wrap(All.Float), false, VERTEX_STRIDE, vaddr);
      vaddr = new IntPtr(baseVAddr + 24);
      GL.VertexAttribPointer(aPosition, 2, All.wrap(All.Float), false, VERTEX_STRIDE, vaddr);
      if (aTexture != -1) {
        vaddr = new IntPtr(baseVAddr + 32);
        GL.VertexAttribPointer(aTexture, 2, All.wrap(All.Float), false, VERTEX_STRIDE, vaddr);
      }
      checkGlError("Shader.prepare AttribPointer");
      return true;
    }

    void flush() {
      if (vertexOffset == 0)
        return;

      checkGlError("Shader.flush");
      // GL.BufferData(All.wrap(All.ArrayBuffer), vertexOffset * FLOAT_SIZE_BYTES,
      //               vertexHandle.AddrOfPinnedObject(), All.wrap(All.StreamDraw));
      // GL.BufferData(All.wrap(All.ElementArrayBuffer), elementOffset * SHORT_SIZE_BYTES,
      //               elementHandle.AddrOfPinnedObject(), All.wrap(All.StreamDraw));
      // checkGlError("Shader.flush BufferData");
      GL.DrawElements(All.wrap(All.TriangleStrip), elementOffset, All.wrap(All.UnsignedShort),
                      elementHandle.AddrOfPinnedObject());
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
      int ii = vertexOffset;
      vertexData[ii++] = local.m00();
      vertexData[ii++] = local.m01();
      vertexData[ii++] = local.m10();
      vertexData[ii++] = local.m11();
      vertexData[ii++] = local.tx();
      vertexData[ii++] = local.ty();
      vertexData[ii++] = dx;
      vertexData[ii++] = dy;
      vertexData[ii++] = sx;
      vertexData[ii++] = sy;
      vertexOffset = ii;
    }

    void addElement(int index) {
      elementData[elementOffset++] = (short) index;
    }

    private int loadShader(All type, final String shaderSource) {
      // create the shader object
      int shader = GL.CreateShader(type);
      if (shader == 0) {
        throw new RuntimeException("Unable to create GL shader: " + GL.GetError());
      }

      // load and compile the shader
      GL.ShaderSource(shader, 1, new String[] { shaderSource }, null);
      GL.CompileShader(shader);

      // check that the shader compiled successfully
      int[] compiled = new int[1];
      GL.GetShader(shader, All.wrap(All.CompileStatus), compiled);
      if (compiled[0] != All.False)
        return shader;

      // if not, extract the error log and report it
      int[] llength = new int[1];
      GL.GetShader(shader, All.wrap(All.InfoLogLength), llength);
      cli.System.Text.StringBuilder log = new cli.System.Text.StringBuilder(llength[0]);
      GL.GetShaderInfoLog(shader, llength[0], llength, log);

      PlayN.log().error("Could not compile shader " + type + ":");
      PlayN.log().error(log.ToString());
      GL.DeleteShader(shader);
      return 0;
    }

    // Creates program object, attaches shaders, and links into pipeline
    protected int createProgram(String vertexSource, String fragmentSource) {
      // load the vertex and fragment shaders
      int vertexShader = loadShader(All.wrap(All.VertexShader), vertexSource);
      int fragmentShader = loadShader(All.wrap(All.FragmentShader), fragmentSource);
      int program = GL.CreateProgram();
      if (program == 0) {
        throw new RuntimeException("Unable to create GL program: " + GL.GetError());
      }

      GL.AttachShader(program, vertexShader);
      checkGlError("createProgram Attaching vertex shader");
      GL.AttachShader(program, fragmentShader);
      checkGlError("createProgram Attaching fragment shader");
      GL.LinkProgram(program);

      int[] linkStatus = new int[1];
      GL.GetProgram(program, All.wrap(All.LinkStatus), linkStatus);
      if (linkStatus[0] == All.True)
        return program;

      int[] llength = new int[1];
      GL.GetProgram(program, All.wrap(All.InfoLogLength), llength);
      cli.System.Text.StringBuilder log = new cli.System.Text.StringBuilder(llength[0]);
      GL.GetProgramInfoLog(program, llength[0], llength, log);

      PlayN.log().error("Could not link program: ");
      PlayN.log().error(log.ToString());
      GL.DeleteProgram(program);
      return 0;
    }
  }

  private class TextureShader extends Shader {
    private int uTexture, uAlpha, lastTex;
    private float lastAlpha;

    TextureShader() {
      super(TEX_FRAG_SHADER);
      uTexture = GL.GetUniformLocation(program, "u_Texture");
      uAlpha = GL.GetUniformLocation(program, "u_Alpha");
    }

    @Override
    void flush() {
      GL.BindTexture(All.wrap(All.Texture2D), lastTex);
      super.flush();
    }

    void prepare(int tex, float alpha) {
      checkGlError("textureShader.prepare start");
      if (super.prepare()) {
        GL.ActiveTexture(All.wrap(All.Texture0));
        GL.Uniform1(uTexture, 0);
      }

      if (tex == lastTex && alpha == lastAlpha)
        return;
      flush();

      GL.Uniform1(uAlpha, alpha);
      lastAlpha = alpha;
      lastTex = tex;
      checkGlError("textureShader.prepare end");
    }
  }

  private class ColorShader extends Shader {
    private int uColor, uAlpha, lastColor;
    private float lastAlpha;

    ColorShader() {
      super(COLOR_FRAG_SHADER);
      uColor = GL.GetUniformLocation(program, "u_Color");
      uAlpha = GL.GetUniformLocation(program, "u_Alpha");
    }

    void prepare(int color, float alpha) {
      checkGlError("colorShader.prepare start");
      super.prepare();

      checkGlError("colorShader.prepare super called");

      if (color == lastColor && alpha == lastAlpha)
        return;
      flush();

      checkGlError("colorShader.prepare flushed");

      GL.Uniform1(uAlpha, alpha);
      lastAlpha = alpha;
      setColor(color);
      checkGlError("colorShader.prepare end");
    }

    private void setColor(int color) {
      float a = (float) ((color >> 24) & 0xff) / 255;
      float r = (float) ((color >> 16) & 0xff) / 255;
      float g = (float) ((color >> 8) & 0xff) / 255;
      float b = (float) ((color >> 0) & 0xff) / 255;
      GL.Uniform4(uColor, r, g, b, a);
      lastColor = color;
    }
  }

  IOSGLContext(int screenWidth, int screenHeight) {
    fbufWidth = viewWidth = screenWidth;
    fbufHeight = viewHeight = screenHeight;
  }

  void init() {
    reinitGL();
  }

  @Override
  public Integer createFramebuffer(Object tex) {
    int[] fbufw = new int[1];
    GL.GenFramebuffers(1, fbufw);

    int fbuf = fbufw[0];
    GL.BindFramebuffer(All.wrap(All.Framebuffer), fbuf);
    GL.FramebufferTexture2D(All.wrap(All.Framebuffer), All.wrap(All.ColorAttachment0),
                            All.wrap(All.Texture2D), (Integer) tex, 0);

    return fbuf;
  }

  @Override
  public void deleteFramebuffer(Object fbuf) {
    GL.DeleteFramebuffers(1, new int[] { (Integer) fbuf });
  }

  @Override
  public void bindFramebuffer(Object fbuf, int width, int height) {
    bindFramebuffer((Integer)fbuf, width, height, false);
  }

  @Override
  public Integer createTexture(boolean repeatX, boolean repeatY) {
    int[] texw = new int[1];
    GL.GenTextures(1, texw);
    int tex = texw[0];
    GL.BindTexture(All.wrap(All.Texture2D), tex);
    GL.TexParameter(All.wrap(All.Texture2D), All.wrap(All.TextureMinFilter), All.Linear);
    GL.TexParameter(All.wrap(All.Texture2D), All.wrap(All.TextureMagFilter), All.Linear);
    GL.TexParameter(All.wrap(All.Texture2D), All.wrap(All.TextureWrapS),
                    repeatX ? All.Repeat : All.ClampToEdge);
    GL.TexParameter(All.wrap(All.Texture2D), All.wrap(All.TextureWrapT),
                    repeatY ? All.Repeat : All.ClampToEdge);
    return tex;
  }

  @Override
  public Integer createTexture(int width, int height, boolean repeatX, boolean repeatY) {
    int tex = createTexture(repeatX, repeatY);
    GL.TexImage2D(All.wrap(All.Texture2D), 0, All.Rgba, width, height, 0, All.wrap(All.Rgba),
                  All.wrap(All.UnsignedByte), null);
    return tex;
  }

  @Override
  public void destroyTexture(Object texObj) {
    GL.DeleteTextures(1, new int[] { (Integer)texObj });
  }

  @Override
  public void drawTexture(Object texObj, float texWidth, float texHeight, InternalTransform local,
                          float dx, float dy, float dw, float dh,
                          float sx, float sy, float sw, float sh, float alpha) {
    checkGlError("drawTexture start");
    texShader.prepare((Integer) texObj, alpha);
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
  public void fillRect(InternalTransform local, float dx, float dy, float dw, float dh,
                       int color, float alpha) {
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

  @Override
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
    GL.ClearColor(r, g, b, a);
    GL.Clear(All.ColorBufferBit);
  }

  @Override
  public void flush() {
    if (curShader != null) {
      checkGlError("flush()");
      curShader.flush();
      curShader = null;
    }
  }

  void bindFramebuffer() {
    bindFramebuffer(0, viewWidth, viewHeight, false);
  }

  void bindFramebuffer(int frameBuffer, int width, int height, boolean force) {
    if (force || lastFrameBuffer != frameBuffer) {
      checkGlError("bindFramebuffer");
      flush();

      lastFrameBuffer = frameBuffer;
      GL.BindFramebuffer(All.wrap(All.Framebuffer), frameBuffer);
      GL.Viewport(0, 0, width, height);
      fbufWidth = width;
      fbufHeight = height;
    }
  }

  void updateTexture(int tex, UIImage image) {
    CGImage cimage = image.get_CGImage();
    int width = cimage.get_Width(), height = cimage.get_Height();

    IntPtr data = Marshal.AllocHGlobal(width * height * 4);
    CGColorSpace colorSpace = CGColorSpace.CreateDeviceRGB();
    CGBitmapContext bctx = new CGBitmapContext(
      data, width, height, 8, 4 * width, colorSpace,
      CGImageAlphaInfo.wrap(CGImageAlphaInfo.PremultipliedLast));
    colorSpace.Dispose();

    bctx.ClearRect(new RectangleF(0, 0, width, height));
    // bctx.TranslateCTM(0, height - imageSize.Height);
    bctx.DrawImage(new RectangleF(0, 0, width, height), cimage);

    GL.TexImage2D(All.wrap(All.Texture2D), 0, All.Rgba, width, height, 0, All.wrap(All.Rgba),
                  All.wrap(All.UnsignedByte), data);

    bctx.Dispose();
    Marshal.FreeHGlobal(data);
  }

  void paintLayers(GroupLayerGL rootLayer) {
    checkGlError("updateLayers start");
    bindFramebuffer();
    GL.Clear(All.ColorBufferBit | All.DepthBufferBit); // clear to transparent
    rootLayer.paint(StockInternalTransform.IDENTITY, 1); // paint all the layers
    checkGlError("updateLayers end");
    useShader(null); // guarantee a flush
  }

  private boolean useShader(Shader shader) {
    if (curShader == shader)
      return false;
    checkGlError("useShader");
    flush();
    curShader = shader;
    return true;
  }

  private void reinitGL() {
    GL.Disable(All.wrap(All.CullFace));
    GL.Enable(All.wrap(All.Blend));
    GL.BlendFunc(All.wrap(All.One), All.wrap(All.OneMinusSrcAlpha));
    GL.ClearColor(0, 0, 0, 1);
    texShader = new TextureShader();
    colorShader = new ColorShader();
  }

  private static void checkGlError(String op) {
    if (CHECK_ERRORS) {
      All error;
      while (!(error = GL.GetError()).Equals(All.wrap(All.NoError))) {
        PlayN.log().error(op + ": glError " + error);
      }
    }
  }
}
