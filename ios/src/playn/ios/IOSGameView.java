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
import cli.System.EventArgs;

import cli.OpenTK.FrameEventArgs;
import cli.OpenTK.Graphics.ES20.GL;
import cli.OpenTK.Graphics.ES20.FramebufferTarget;
import cli.OpenTK.Platform.iPhoneOS.iPhoneOSGameView;

import cli.MonoTouch.CoreAnimation.CAEAGLLayer;
import cli.MonoTouch.Foundation.ExportAttribute;
import cli.MonoTouch.Foundation.NSNotification;
import cli.MonoTouch.Foundation.NSNotificationCenter;
import cli.MonoTouch.OpenGLES.EAGLColorFormat;
import cli.MonoTouch.OpenGLES.EAGLRenderingAPI;
import cli.MonoTouch.UIKit.UIDevice;
import cli.MonoTouch.UIKit.UIWindow;

public class IOSGameView extends iPhoneOSGameView {

  private final IOSPlatform platform;
  private boolean activated;
  private boolean createdCtx;

  public IOSGameView(IOSPlatform platform, RectangleF bounds, float scale) {
    super(bounds);
    this.platform = platform;

    // TODO: I assume we want to manually manage loss of EGL context
    set_LayerRetainsBacking(false);
    set_ContentScaleFactor(scale);
    set_MultipleTouchEnabled(true);
    set_AutoResize(false);
    set_LayerColorFormat(EAGLColorFormat.RGBA8);
    // TODO: support OpenGL ES 1.1?
    set_ContextRenderingApi(EAGLRenderingAPI.wrap(EAGLRenderingAPI.OpenGLES2));

    NSNotificationCenter.get_DefaultCenter().AddObserver(
      UIDevice.get_OrientationDidChangeNotification(),
      new cli.System.Action$$00601_$$$_Lcli__MonoTouch__Foundation__NSNotification_$$$$_(new cli.System.Action$$00601_$$$_Lcli__MonoTouch__Foundation__NSNotification_$$$$_.Method() {
        @Override
        public void Invoke(NSNotification nf) {
          IOSGameView.this.platform.onOrientationChange(
            UIDevice.get_CurrentDevice().get_Orientation());
        }}));
  }

  // called when our app is activated and when we resign activation; per iOS policy we stop our
  // renderer when we're not activated; the app may still be visible in the background (probably
  // blurred out in iOS 7) while the user decides whether to answer a phone call or whatever
  synchronized void onActivated() {
    activated = true;
  }
  synchronized void onResignActivation() {
    activated = false;
  }

  @Override
  public void WillMoveToWindow(UIWindow window) {
    // this is a temporary workaround to deal with iPhoneOSGameView (unnecessarily) destroying and
    // recreating our GL context if we're hidden and reshown; the proper solution is to not use
    // iPhoneOSGameView and do all the same things ourselves directly atop UIView, but that's not
    // something I'm going to do while fixing an 11th hour crashing bug for the build I should be
    // submitting to Apple today
    if (!createdCtx) {
      super.WillMoveToWindow(window);
      createdCtx = true;
    }
  }

  @Override
  protected void ConfigureLayer(CAEAGLLayer eaglLayer) {
    eaglLayer.set_Opaque(true);
    super.ConfigureLayer(eaglLayer);
  }

  @Override
  protected void CreateFrameBuffer() {
    super.CreateFrameBuffer();
    // now that we're loaded, initialize the GL subsystem
    platform.viewDidInit(get_Framebuffer());
  }

  // @Override
  // protected void OnClosed(EventArgs e) {
  //   super.OnClosed(e);
  // }

  // @Override
  // protected void OnDisposed(EventArgs e) {
  //   super.OnDisposed(e);
  // }

  @Override
  protected void OnLoad(EventArgs e) {
    super.OnLoad(e);
    UIDevice.get_CurrentDevice().BeginGeneratingDeviceOrientationNotifications();

    // run a single frame so that we have something in our framebuffer when iOS stops displaying
    // our splash screen and starts displaying our app
    platform.update();
    GL.BindFramebuffer(FramebufferTarget.wrap(FramebufferTarget.Framebuffer), get_Framebuffer());
    MakeCurrent();
    platform.paint();
    SwapBuffers();
  }

  @Override
  protected void OnUnload(EventArgs e) {
    super.OnUnload(e);
    UIDevice.get_CurrentDevice().EndGeneratingDeviceOrientationNotifications();
  }

  // @Override
  // protected void OnResize(EventArgs e) {
  //   super.OnResize(e);
  // }

  // @Override
  // protected void OnTitleChanged(EventArgs e) {
  //   super.OnTitleChanged(e);
  // }

  @Override
  protected void OnUpdateFrame(FrameEventArgs e) {
    super.OnUpdateFrame(e);
    platform.update();
  }

  @Override
  protected void OnRenderFrame(FrameEventArgs e) {
    super.OnRenderFrame(e);

    if (activated) {
      MakeCurrent();
      platform.paint();
      SwapBuffers();
    }
  }

  @ExportAttribute.Annotation("layerClass")
  static cli.MonoTouch.ObjCRuntime.Class LayerClass() {
    return iPhoneOSGameView.GetLayerClass();
  }
}
