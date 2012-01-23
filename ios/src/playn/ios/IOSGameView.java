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

import cli.System.DateTime;
import cli.System.Drawing.RectangleF;
import cli.System.EventArgs;

import cli.OpenTK.FrameEventArgs;
import cli.OpenTK.Platform.iPhoneOS.iPhoneOSGameView;

import cli.MonoTouch.CoreAnimation.CAEAGLLayer;
import cli.MonoTouch.Foundation.ExportAttribute;
import cli.MonoTouch.Foundation.NSSet;
import cli.MonoTouch.OpenGLES.EAGLColorFormat;
import cli.MonoTouch.OpenGLES.EAGLRenderingAPI;
import cli.MonoTouch.UIKit.UIEvent;
import cli.MonoTouch.UIKit.UIScreen;

import playn.core.PlayN;

class IOSGameView extends iPhoneOSGameView
{
  private static final float MAX_DELTA = 100;

  private DateTime lastUpdate = DateTime.get_Now();

  public IOSGameView(RectangleF bounds) {
    super(bounds);

    // TODO: I assume we want to manually manage loss of EGL context
    set_LayerRetainsBacking(false);
    // TODO: is this for retina displays?
    set_ContentScaleFactor(UIScreen.get_MainScreen().get_Scale());
    // set_MultipleTouchEnabled(true);
    set_AutoResize(false);
    set_LayerColorFormat(EAGLColorFormat.RGBA8);
    // TODO: support OpenGL ES 1.1?
    set_ContextRenderingApi(EAGLRenderingAPI.wrap(EAGLRenderingAPI.OpenGLES2));
  }

  @Override
  protected void CreateFrameBuffer() {
    super.CreateFrameBuffer();
    // now that we're loaded, initialize the GL subsystem
    IOSPlatform.instance.graphics().ctx.init();
  }

  @Override
  protected void ConfigureLayer (CAEAGLLayer eaglLayer) {
    super.ConfigureLayer(eaglLayer);
    eaglLayer.set_Opaque(true);

    // scale our EAGL layer up if we're on a Retina device; TODO: make this a Platform option?
    eaglLayer.set_ContentsScale(1);
  }

  @Override
  protected void OnClosed(EventArgs e) {
    super.OnClosed(e);
  }

  @Override
  protected void OnDisposed(EventArgs e) {
    super.OnDisposed(e);
  }

  @Override
  protected void OnLoad(EventArgs e)
  {
    super.OnLoad(e);
  }

  @Override
  protected void OnUnload(EventArgs e) {
    super.OnUnload(e);
  }

  @Override
  protected void OnRenderFrame(FrameEventArgs e) {
    super.OnRenderFrame(e);

    MakeCurrent();
    IOSPlatform.instance.paint();
    SwapBuffers();
  }

  @Override
  protected void OnResize(EventArgs e) {
    super.OnResize(e);
  }

  @Override
  protected void OnTitleChanged(EventArgs e) {
    super.OnTitleChanged(e);
  }

  @Override
  protected void OnUpdateFrame(FrameEventArgs e) {
    super.OnUpdateFrame(e);

    DateTime now = DateTime.get_Now();
    float delta = Math.min(MAX_DELTA, (now.get_Ticks() - lastUpdate.get_Ticks())/10000f);
    lastUpdate = now;
    IOSPlatform.instance.update(delta);
  }

  @Override
  protected void OnVisibleChanged(EventArgs e) {
    super.OnVisibleChanged(e);
  }

  @Override
  protected void OnWindowStateChanged(EventArgs e) {
    super.OnWindowStateChanged(e);
  }

  @Override
  public void TouchesBegan(NSSet touches, UIEvent event) {
    super.TouchesBegan(touches, event);
    IOSPlatform.instance.touch().onTouchesBegan(touches, event);
    IOSPlatform.instance.pointer().onTouchesBegan(touches, event);
  }

  @Override
  public void TouchesMoved(NSSet touches, UIEvent event) {
    super.TouchesMoved(touches, event);
    IOSPlatform.instance.touch().onTouchesMoved(touches, event);
    IOSPlatform.instance.pointer().onTouchesMoved(touches, event);
  }

  @Override
  public void TouchesEnded(NSSet touches, UIEvent event) {
    super.TouchesEnded(touches, event);
    IOSPlatform.instance.touch().onTouchesEnded(touches, event);
    IOSPlatform.instance.pointer().onTouchesEnded(touches, event);
  }

  @Override
  public void TouchesCancelled(NSSet touches, UIEvent event) {
    super.TouchesCancelled(touches, event);
    IOSPlatform.instance.touch().onTouchesCancelled(touches, event);
    IOSPlatform.instance.pointer().onTouchesCancelled(touches, event);
  }

  @ExportAttribute.Annotation("layerClass")
  static cli.MonoTouch.ObjCRuntime.Class LayerClass() {
    return iPhoneOSGameView.GetLayerClass();
  }
}
