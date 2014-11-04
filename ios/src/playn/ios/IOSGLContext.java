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

import cli.System.Drawing.RectangleF;
import cli.System.IntPtr;
import cli.System.Runtime.InteropServices.Marshal;

import cli.MonoTouch.CoreGraphics.CGBitmapContext;
import cli.MonoTouch.CoreGraphics.CGImage;
import cli.MonoTouch.CoreGraphics.CGImageAlphaInfo;
import cli.MonoTouch.UIKit.UIDeviceOrientation;
import cli.OpenTK.Graphics.ES20.*; // a zillion little types

import pythagoras.f.FloatMath;
import pythagoras.i.Rectangle;

import playn.core.InternalTransform;
import playn.core.StockInternalTransform;
import playn.core.Tint;
import playn.core.gl.GL20;
import playn.core.gl.GLBuffer;
import playn.core.gl.GLContext;
import playn.core.gl.GLProgram;
import playn.core.gl.GLShader;
import playn.core.gl.GroupLayerGL;
import playn.core.gl.IndexedTrisShader;

public class IOSGLContext extends GLContext {

  public static final boolean CHECK_ERRORS = false;

  public final GL20 gl;

  private final InternalTransform rootTransform = new StockInternalTransform();
  private int orient;
  private int minFilter = All.Linear, magFilter = All.Linear;
  private int defaultFramebuffer = -1; // configured in init()
  private GLShader quadShader, trisShader;

  public IOSGLContext(IOSPlatform platform, IOSGL20 gl,
                      float scaleFactor, int screenWidth, int screenHeight) {
    super(platform, scaleFactor);
    this.gl = gl;
    rootTransform.uniformScale(scaleFactor);
    setSize(screenWidth, screenHeight);
  }

  void viewDidInit(int defaultFramebuffer) {
    this.defaultFramebuffer = defaultFramebuffer;
    GL.Disable(EnableCap.wrap(EnableCap.CullFace));
    GL.Enable(EnableCap.wrap(EnableCap.Blend));
    GL.BlendFunc(BlendingFactorSrc.wrap(BlendingFactorSrc.One),
                 BlendingFactorDest.wrap(BlendingFactorDest.OneMinusSrcAlpha));
    GL.ClearColor(0, 0, 0, 1);
    quadShader = createQuadShader();
    trisShader = new IndexedTrisShader(this);
  }

  boolean setOrientation(UIDeviceOrientation orientation) {
    orient = orientation.Value;
    rootTransform.setTransform(scale.factor, 0, 0, scale.factor, 0, 0);
    switch (orientation.Value) {
    default:
    case UIDeviceOrientation.Portrait:
      return false;
    case UIDeviceOrientation.PortraitUpsideDown:
      rootTransform.translate(viewWidth, viewHeight);
      rootTransform.scale(-1, -1);
      return false;
    case UIDeviceOrientation.LandscapeLeft:
      rootTransform.rotate(FloatMath.PI/2);
      rootTransform.translate(0, -viewWidth);
      return true;
    case UIDeviceOrientation.LandscapeRight:
      rootTransform.rotate(-FloatMath.PI/2);
      rootTransform.translate(-viewHeight, 0);
      return true;
    }
  }

  @Override
  public void setTextureFilter (Filter minFilter, Filter magFilter) {
    this.minFilter = toGL(minFilter);
    this.magFilter = toGL(magFilter);
  }

  @Override
  public String getString(int param) {
    return GL.GetString(StringName.wrap(param));
  }

  @Override
  public int getInteger(int param) {
    int[] out = new int[1];
    GL.GetInteger(GetPName.wrap(param), out);
    return out[0];
  }

  @Override
  public float getFloat(int param) {
    float[] out = new float[1];
    GL.GetFloat(GetPName.wrap(param), out);
    return out[0];
  }

  @Override
  public boolean getBoolean(int param) {
    boolean[] out = new boolean[1];
    GL.GetBoolean(GetPName.wrap(param), out);
    return out[0];
  }

  @Override
  public GLProgram createProgram(String vertShader, String fragShader) {
    if (STATS_ENABLED) stats.shaderCreates++;
    return new IOSGLProgram(this, vertShader, fragShader);
  }

  @Override
  public GLBuffer.Float createFloatBuffer(int capacity) {
    return new IOSGLBuffer.FloatImpl(capacity);
  }

  @Override
  public GLBuffer.Short createShortBuffer(int capacity) {
    return new IOSGLBuffer.ShortImpl(capacity);
  }

  @Override
  public void deleteFramebuffer(int fbuf) {
    GL.DeleteFramebuffers(1, new int[] { fbuf });
  }

  @Override
  public int createTexture(boolean repeatX, boolean repeatY, boolean mipmaps) {
    int[] texw = new int[1];
    GL.GenTextures(1, texw);
    int tex = texw[0];
    if (tex == 0) {
      throw new IllegalStateException(NO_SURF_IN_INIT_ERR);
    }

    TextureTarget tt = TextureTarget.wrap(TextureTarget.Texture2D);
    GL.BindTexture(tt, tex);
    GL.TexParameter(tt, TextureParameterName.wrap(TextureParameterName.TextureMinFilter),
                    mipmapify(minFilter, mipmaps));
    GL.TexParameter(tt, TextureParameterName.wrap(TextureParameterName.TextureMagFilter), magFilter);
    GL.TexParameter(tt, TextureParameterName.wrap(TextureParameterName.TextureWrapS),
                    repeatX ? All.Repeat : All.ClampToEdge);
    GL.TexParameter(tt, TextureParameterName.wrap(TextureParameterName.TextureWrapT),
                    repeatY ? All.Repeat : All.ClampToEdge);
    if (STATS_ENABLED) stats.texCreates++;
    return tex;
  }

  @Override
  public int createTexture(int width, int height, boolean repeatX, boolean repeatY, boolean mm) {
    int tex = createTexture(repeatX, repeatY, mm);
    GL.TexImage2D(TextureTarget.wrap(TextureTarget.Texture2D), 0,
                  PixelInternalFormat.wrap(PixelInternalFormat.Rgba), width, height, 0,
                  PixelFormat.wrap(PixelFormat.Rgba), PixelType.wrap(PixelType.UnsignedByte), null);
    return tex;
  }

  @Override
  public void generateMipmap(int tex) {
    TextureTarget tt = TextureTarget.wrap(TextureTarget.Texture2D);
    GL.BindTexture(tt, tex);
    GL.GenerateMipmap(tt);
  }

  @Override
  public void activeTexture(int glTextureN) {
    GL.ActiveTexture(TextureUnit.wrap(glTextureN));
  }

  @Override
  public void bindTexture(int tex) {
    GL.BindTexture(TextureTarget.wrap(TextureTarget.Texture2D), tex);
    if (STATS_ENABLED) stats.texBinds++;
  }

  @Override
  public void destroyTexture(int texObj) {
    flush(); // flush in case this texture is queued up to be drawn
    GL.DeleteTextures(1, new int[] { texObj });
  }

  @Override
  public boolean startClipped(int x, int y, int width, int height) {
    flush(); // flush any pending unclipped calls
    Rectangle r;
    switch (orient) {
    default:
    case UIDeviceOrientation.Portrait:
      r = pushScissorState(x, curFbufHeight-y-height, width, height);
      break;
    case UIDeviceOrientation.PortraitUpsideDown:
      r = pushScissorState(x-width, curFbufHeight-y, width, height);
      break;
    case UIDeviceOrientation.LandscapeLeft:
      r = pushScissorState(x-width, curFbufHeight-y-height, width, height);
      break;
    case UIDeviceOrientation.LandscapeRight:
      r = pushScissorState(x, curFbufHeight-y, width, height);
      break;
    }
    GL.Scissor(r.x, r.y, r.width, r.height);
    checkGLError("GL.Scissor");
    if (getScissorDepth() == 1) GL.Enable(EnableCap.wrap(EnableCap.ScissorTest));
    return !r.isEmpty();
  }

  @Override
  public void endClipped() {
    flush(); // flush our clipped calls with SCISSOR_TEST still enabled
    Rectangle r = popScissorState();
    if (r == null) GL.Disable(EnableCap.wrap(EnableCap.ScissorTest));
    else {
        GL.Scissor(r.x, r.y, r.width, r.height);
        checkGLError("GL.Scissor");
    }
  }

  @Override
  public void clear(float r, float g, float b, float a) {
    GL.ClearColor(r, g, b, a);
    GL.Clear(ClearBufferMask.wrap(ClearBufferMask.ColorBufferBit));
  }

  @Override
  public void checkGLError(String op) {
    if (CHECK_ERRORS) {
      ErrorCode error;
      while (!(error = GL.GetError()).Equals(ErrorCode.wrap(ErrorCode.NoError))) {
        platform.log().error(op + ": glError " + error);
      }
    }
  }

  @Override
  public InternalTransform rootTransform() {
    return rootTransform;
  }

  @Override
  protected int defaultFramebuffer() {
    return defaultFramebuffer;
  }

  @Override
  protected int createFramebufferImpl(int tex) {
    int[] fbufw = new int[1];
    GL.GenFramebuffers(1, fbufw);

    int fbuf = fbufw[0];
    if (fbuf == 0) {
      throw new IllegalStateException(NO_SURF_IN_INIT_ERR);
    }

    GL.BindFramebuffer(FramebufferTarget.wrap(FramebufferTarget.Framebuffer), fbuf);
    GL.FramebufferTexture2D(FramebufferTarget.wrap(FramebufferTarget.Framebuffer),
                            FramebufferSlot.wrap(FramebufferSlot.ColorAttachment0),
                            TextureTarget.wrap(TextureTarget.Texture2D), tex, 0);
    if (STATS_ENABLED) stats.frameBufferCreates++;
    return fbuf;
  }

  @Override
  protected void bindFramebufferImpl(int frameBuffer, int width, int height) {
    // this is called during early initialization before we know our default frame buffer id, but
    // we can just skip binding in that case because our default frame buffer is already bound
    if (frameBuffer != -1)
      GL.BindFramebuffer(FramebufferTarget.wrap(FramebufferTarget.Framebuffer), frameBuffer);
    GL.Viewport(0, 0, width, height);
    if (STATS_ENABLED) stats.frameBufferBinds++;
  }

  @Override
  protected GLShader quadShader() {
    return quadShader;
  }
  @Override
  protected GLShader trisShader() {
    return trisShader;
  }

  void updateTexture(int tex, CGImage image) {
    int width = image.get_Width(), height = image.get_Height();
    if (width == 0 || height == 0) {
      platform.log().warn("Ignoring texture update for empty image (" + width + "x" + height + ").");
      return;
    }

    IntPtr data = Marshal.AllocHGlobal(width * height * 4);
    CGBitmapContext bctx = new CGBitmapContext(
      data, width, height, 8, 4 * width, IOSGraphics.colorSpace,
      CGImageAlphaInfo.wrap(CGImageAlphaInfo.PremultipliedLast));

    bctx.ClearRect(new RectangleF(0, 0, width, height));
    // bctx.TranslateCTM(0, height - imageSize.Height);
    bctx.DrawImage(new RectangleF(0, 0, width, height), image);

    updateTexture(tex, width, height, data);

    bctx.Dispose();
    Marshal.FreeHGlobal(data);
  }

  void updateTexture(int tex, int width, int height, IntPtr data) {
    GL.BindTexture(TextureTarget.wrap(TextureTarget.Texture2D), tex);
    GL.TexImage2D(TextureTarget.wrap(TextureTarget.Texture2D), 0,
                  PixelInternalFormat.wrap(PixelInternalFormat.Rgba), width, height, 0,
                  PixelFormat.wrap(PixelFormat.Rgba), PixelType.wrap(PixelType.UnsignedByte), data);
  }

  void paint(GroupLayerGL rootLayer) {
    if (rootLayer.size() > 0) {
      checkGLError("paint");
      bindFramebuffer();
      GL.Clear(ClearBufferMask.wrap(ClearBufferMask.ColorBufferBit | // clear to transparent
                                    ClearBufferMask.DepthBufferBit));
      rootLayer.paint(rootTransform, Tint.NOOP_TINT, null); // paint all the layers
      useShader(null); // guarantee a shader flush
    }
    if (STATS_ENABLED) stats.frames++;
  }

  private static int toGL(Filter filter) {
    switch (filter) {
    default:
    case  LINEAR: return All.Linear;
    case NEAREST: return All.Nearest;
    }
  }

  private static int mipmapify (int filter, boolean mipmaps) {
    if (!mipmaps)
      return filter;
    // we don't do trilinear filtering (i.e. GL_LINEAR_MIPMAP_LINEAR);
    // it's expensive and not super useful when only rendering in 2D
    switch (filter) {
    case All.Nearest: return All.NearestMipmapNearest;
    case All.Linear: return All.LinearMipmapNearest;
    default: return filter;
    }
  }

  protected static final String NO_SURF_IN_INIT_ERR =
    "Attempted to generate texture before GL was initialized. Unfortunately, " +
    "you cannot create and render to a SurfaceImage in Game.init() on iOS.";
}
