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

import org.robovm.apple.coregraphics.CGRect;
import org.robovm.apple.foundation.NSSet;
import org.robovm.apple.glkit.GLKView;
import org.robovm.apple.glkit.GLKViewController;
import org.robovm.apple.glkit.GLKViewControllerDelegate;
import org.robovm.apple.opengles.EAGLContext;
import org.robovm.apple.opengles.EAGLRenderingAPI;
import org.robovm.apple.uikit.UIEvent;
import org.robovm.apple.uikit.UIInterfaceOrientation;
import org.robovm.apple.uikit.UIInterfaceOrientationMask;
import org.robovm.apple.uikit.UIScreen;
import org.robovm.apple.uikit.UITouch;
import org.robovm.objc.Selector;
import org.robovm.objc.annotation.BindSelector;
import org.robovm.objc.annotation.Method;
import org.robovm.rt.bro.annotation.Callback;

import playn.core.Game;
import playn.core.PlayN;

/**
 * Manages the main PlayN view as well as all iOS and GL callbacks and lifecycle. This is the root
 * of the iOS backend. If you want to customize things or embed PlayN, you'll want to understand
 * what this class does.
 */
public class RoboViewController extends GLKViewController implements GLKViewControllerDelegate {

  private final GLKView view;
  private final RoboPlatform platform;

  /** Creates a game view controller with the given bounds and configuration **/
  public RoboViewController(CGRect bounds, RoboPlatform.Config config) {
    EAGLContext ctx = new EAGLContext(EAGLRenderingAPI.OpenGLES2);
    platform = new RoboPlatform(bounds, config);
    view = new GLKView(bounds, ctx) {
      @Method(selector = "touchesBegan:withEvent:")
      public void touchesBegan(NSSet<UITouch> touches, UIEvent event) {
        platform.touch().onTouchesBegan(touches, event);
        platform.pointer().onTouchesBegan(touches, event);
      }
      @Method(selector = "touchesCancelled:withEvent:")
      public void touchesCancelled(NSSet<UITouch> touches, UIEvent event) {
        platform.touch().onTouchesCancelled(touches, event);
        platform.pointer().onTouchesCancelled(touches, event);
      }
      @Method(selector = "touchesEnded:withEvent:")
      public void touchesEnded(NSSet<UITouch> touches, UIEvent event) {
        platform.touch().onTouchesEnded(touches, event);
        platform.pointer().onTouchesEnded(touches, event);
      }
      @Method(selector = "touchesMoved:withEvent:")
      public void touchesMoved(NSSet<UITouch> touches, UIEvent event) {
        platform.touch().onTouchesMoved(touches, event);
        platform.pointer().onTouchesMoved(touches, event);
      }
      @Override
      public void draw(CGRect rect) {
        platform.paint();
      }
    };
    view.setMultipleTouchEnabled(true);
    view.setDrawableColorFormat(platform.config.glBufferFormat);
    // view.setDrawableDepthFormat(GLKViewDrawableDepthFormat._16);
    // view.setDrawableStencilFormat(GLKViewDrawableStencilFormat.None);
    setView(view);
    setDelegate(this);
    setPreferredFramesPerSecond(config.targetFPS);
    addStrongRef(platform);
    PlayN.setPlatform(platform);
  }

  /** Returns the platform managed by this view controller. */
  public RoboPlatform platform() {
    return platform;
  }

  @Override // from GLKViewControllerDelegate
  public void update(GLKViewController self) {
    platform.update();
  }

  @Override // from GLKViewControllerDelegate
  public void willPause(GLKViewController self, boolean paused) {
    // platform.log().debug("willPause(" + paused + ")");
    if (paused) platform.didEnterBackground();
    else platform.willEnterForeground();
  }

  @Override // from ViewController
  public void viewDidAppear(boolean animated) {
    super.viewDidAppear(animated);
    CGRect bounds = getView().getBounds();
    // platform.log().debug("viewDidAppear(" + animated + "): " + bounds);
    EAGLContext.setCurrentContext(view.getContext());
    platform.graphics().ctx.viewDidInit((int)bounds.getWidth(), (int)bounds.getHeight());
  }

  @Override // from ViewController
  public void viewDidDisappear(boolean animated) {
    super.viewDidDisappear(animated);
    EAGLContext.setCurrentContext(null);
  }

  @Override // from ViewController
  public void willRotate(UIInterfaceOrientation toOrient, double duration) {
    super.willRotate(toOrient, duration);
    platform.willRotate(toOrient, duration);
  }

  @Override // from ViewController
  public void didRotate(UIInterfaceOrientation fromOrient) {
    super.didRotate(fromOrient);
    CGRect bounds = getView().getBounds();
    // platform.log().debug("didRotate(" + fromOrient + "): " + bounds);
    platform.graphics().setSize((int)bounds.getWidth(), (int)bounds.getHeight());
    platform.didRotate(fromOrient);
  }

  @Override // from ViewController
  public UIInterfaceOrientationMask getSupportedInterfaceOrientations() {
    return platform.config.orients;
  }

  @Override // from ViewController
  public boolean shouldAutorotate() {
    return true;
  }

  public boolean shouldAutorotateToInterfaceOrientation(UIInterfaceOrientation orientation) {
    return true; // TODO
  }

  @Override
  protected void doDispose() {
    //Shutdown the platform
    platform.willTerminate();
    removeStrongRef(platform);
    super.doDispose();
  }

  @Callback @BindSelector("shouldAutorotateToInterfaceOrientation:")
  private static boolean shouldAutorotateToInterfaceOrientation(
    RoboViewController self, Selector sel, UIInterfaceOrientation orientation) {
    return self.shouldAutorotateToInterfaceOrientation(orientation);
  }
}
