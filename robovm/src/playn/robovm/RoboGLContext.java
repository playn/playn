/**
 * Copyright 2014 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playn.robovm;

import org.robovm.apple.coregraphics.CGBitmapContext;
import org.robovm.apple.coregraphics.CGImage;
import org.robovm.apple.coregraphics.CGRect;
import org.robovm.apple.uikit.UIDeviceOrientation;

import org.robovm.rt.bro.ptr.VoidPtr;
import pythagoras.f.FloatMath;
import pythagoras.i.Rectangle;

import playn.core.InternalTransform;
import playn.core.StockInternalTransform;
import playn.core.Tint;
import playn.core.gl.GL20;
import playn.core.gl.GL20Context;
import playn.core.gl.GLBuffer;
import playn.core.gl.GLContext;
import playn.core.gl.GLProgram;
import playn.core.gl.GLShader;
import playn.core.gl.GroupLayerGL;
import playn.core.gl.IndexedTrisShader;
import static playn.core.gl.GL20.*;

public class RoboGLContext extends GL20Context {

  public static final boolean CHECK_ERRORS = false;

  private final RoboPlatform platform;
  private int defaultFramebuffer = -1; // initted in viewDidInit
  // private UIDeviceOrientation orient;

  public RoboGLContext(RoboPlatform platform, RoboGL20 gl, float scaleFactor) {
    super(platform, gl, scaleFactor, CHECK_ERRORS);
    this.platform = platform;
    init();
  }

  void viewDidInit(int width, int height) {
    // Apple re-disables GL_BLEND &c between our call to init() and the eventual call to
    // viewDidInit(); why why why? I want my six hours spend on bullshit debugging back!
    gl.glDisable(GL_CULL_FACE);
    gl.glEnable(GL_BLEND);
    gl.glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
    gl.glClearColor(0, 0, 0, 1);

    defaultFramebuffer = gl.glGetInteger(GL_FRAMEBUFFER_BINDING);
    setSize(width, height);
  }

  // boolean setOrientation(UIDeviceOrientation orientation) {
  //   orient = orientation.Value;
  //   rootTransform.setTransform(scale.factor, 0, 0, scale.factor, 0, 0);
  //   switch (orientation) {
  //   default:
  //   case UIDeviceOrientation.Portrait:
  //     return false;
  //   case UIDeviceOrientation.PortraitUpsideDown:
  //     rootTransform.translate(viewWidth, viewHeight);
  //     rootTransform.scale(-1, -1);
  //     return false;
  //   case UIDeviceOrientation.LandscapeLeft:
  //     rootTransform.rotate(FloatMath.PI/2);
  //     rootTransform.translate(0, -viewWidth);
  //     return true;
  //   case UIDeviceOrientation.LandscapeRight:
  //     rootTransform.rotate(-FloatMath.PI/2);
  //     rootTransform.translate(-viewHeight, 0);
  //     return true;
  //   }
  // }

  // @Override
  // public void startClipped(int x, int y, int width, int height) {
  //   flush(); // flush any pending unclipped calls
  //   Rectangle r;
  //   // switch (orient) {
  //   // default:
  //   // case UIDeviceOrientation.Portrait:
  //     r = pushScissorState(x, curFbufHeight-y-height, width, height);
  //   //   break;
  //   // case UIDeviceOrientation.PortraitUpsideDown:
  //   //   r = pushScissorState(x-width, curFbufHeight-y, width, height);
  //   //   break;
  //   // case UIDeviceOrientation.LandscapeLeft:
  //   //   r = pushScissorState(x-width, curFbufHeight-y-height, width, height);
  //   //   break;
  //   // case UIDeviceOrientation.LandscapeRight:
  //   //   r = pushScissorState(x, curFbufHeight-y, width, height);
  //   //   break;
  //   // }
  //   gl.glScissor(r.x, r.y, r.width, r.height);
  //   checkGLError("GL.Scissor");
  //   if (getScissorDepth() == 1) gl.glEnable(GL_SCISSOR_TEST);
  // }

  @Override
  protected int defaultFramebuffer() {
    return defaultFramebuffer;
  }

  void updateTexture(int tex, CGImage image) {
    int width = (int)image.getWidth(), height = (int)image.getHeight();
    if (width == 0 || height == 0) {
      platform.log().warn(
        "Ignoring texture update for empty image (" + width + "x" + height + ").");
      return;
    }

    CGBitmapContext bctx = RoboGraphics.createCGBitmap(width, height);
    CGRect rect = new CGRect(0, 0, width, height);
    bctx.clearRect(rect);
    bctx.drawImage(rect, image);
    updateTexture(tex, width, height, bctx.getData());
    bctx.dispose();
  }

  void updateTexture(int tex, int width, int height, VoidPtr data) {
    gl.glBindTexture(GL_TEXTURE_2D, tex);
    gl.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
    OpenGLES.glTexImage2Dp(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0,
                           GL_RGBA, GL_UNSIGNED_BYTE, data);
  }
}
