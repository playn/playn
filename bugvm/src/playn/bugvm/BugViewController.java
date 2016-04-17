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
package playn.bugvm;

import com.bugvm.apple.coregraphics.CGRect;
import com.bugvm.apple.foundation.NSSet;
import com.bugvm.apple.glkit.GLKView;
import com.bugvm.apple.glkit.GLKViewController;
import com.bugvm.apple.glkit.GLKViewControllerDelegate;
import com.bugvm.apple.opengles.EAGLContext;
import com.bugvm.apple.opengles.EAGLRenderingAPI;
import com.bugvm.apple.uikit.UIEvent;
import com.bugvm.apple.uikit.UIInterfaceOrientation;
import com.bugvm.apple.uikit.UIInterfaceOrientationMask;
import com.bugvm.apple.uikit.UITouch;
import com.bugvm.objc.Selector;
import com.bugvm.objc.annotation.BindSelector;
import com.bugvm.objc.annotation.Method;
import com.bugvm.rt.bro.annotation.Callback;

/**
 * Manages the main PlayN view as well as all iOS and GL callbacks and lifecycle. This is the root
 * of the iOS backend. If you want to customize things or embed PlayN, you'll want to understand
 * what this class does.
 */
public class BugViewController extends GLKViewController implements GLKViewControllerDelegate {

  private final GLKView view;

  /** The platform managed by this view controller. */
  public final BugPlatform plat;

  /** Creates a game view controller with the given bounds and configuration **/
  public BugViewController(CGRect bounds, BugPlatform.Config config) {
    EAGLContext ctx = new EAGLContext(EAGLRenderingAPI.OpenGLES2);
    EAGLContext.setCurrentContext(ctx);
    plat = new BugPlatform(config, bounds);
    view = new GLKView(bounds, ctx) {
      @Method(selector = "touchesBegan:withEvent:")
      public void touchesBegan(NSSet<UITouch> touches, UIEvent event) {
        plat.input().onTouchesBegan(touches, event);
      }
      @Method(selector = "touchesCancelled:withEvent:")
      public void touchesCancelled(NSSet<UITouch> touches, UIEvent event) {
        plat.input().onTouchesCancelled(touches, event);
      }
      @Method(selector = "touchesEnded:withEvent:")
      public void touchesEnded(NSSet<UITouch> touches, UIEvent event) {
        plat.input().onTouchesEnded(touches, event);
      }
      @Method(selector = "touchesMoved:withEvent:")
      public void touchesMoved(NSSet<UITouch> touches, UIEvent event) {
        plat.input().onTouchesMoved(touches, event);
      }
    };
    view.setMultipleTouchEnabled(true);
    view.setDrawableColorFormat(plat.config.glBufferFormat);
    // view.setDrawableDepthFormat(GLKViewDrawableDepthFormat._16);
    // view.setDrawableStencilFormat(GLKViewDrawableStencilFormat.None);
    setView(view);
    setDelegate(this);
    setPreferredFramesPerSecond(config.targetFPS);
    addStrongRef(plat);
  }

  @Override // from GLKViewControllerDelegate
  public void update(GLKViewController self) {
    plat.processFrame();
  }

  @Override // from GLKViewControllerDelegate
  public void willPause(GLKViewController self, boolean paused) {
    // plat.log().debug("willPause(" + paused + ")");
    if (paused) plat.didEnterBackground();
    else {
      view.bindDrawable();
      plat.willEnterForeground();
    }
  }

  @Override // from ViewController
  public void viewDidAppear(boolean animated) {
    super.viewDidAppear(animated);
    // plat.log().debug("viewDidAppear(" + animated + ")");
    view.bindDrawable();
    plat.graphics().viewDidInit(getView().getBounds());
  }

  @Override // from ViewController
  public void viewDidDisappear(boolean animated) {
    super.viewDidDisappear(animated);
    EAGLContext.setCurrentContext(null);
  }

  @Override // from ViewController
  public void willRotate(UIInterfaceOrientation toOrient, double duration) {
    super.willRotate(toOrient, duration);
    plat.orient.emit(new BugOrientEvent.WillRotate(toOrient, duration));
  }

  @Override // from ViewController
  public void didRotate(UIInterfaceOrientation fromOrient) {
    super.didRotate(fromOrient);
    // plat.log().debug("didRotate(" + fromOrient + "): " + bounds);
    plat.graphics().boundsChanged(getView().getBounds());
    plat.orient.emit(new BugOrientEvent.DidRotate(fromOrient));
  }

  @Override // from ViewController
  public UIInterfaceOrientationMask getSupportedInterfaceOrientations() {
    return plat.config.orients;
  }

  @Override // from ViewController
  public boolean shouldAutorotate() {
    return true;
  }

  public boolean shouldAutorotateToInterfaceOrientation(UIInterfaceOrientation orientation) {
    return true; // TODO
  }

  @Override protected void doDispose() {
    // shutdown the platform
    plat.willTerminate();
    removeStrongRef(plat);
    super.doDispose();
  }

  @Callback @BindSelector("shouldAutorotateToInterfaceOrientation:")
  private static boolean shouldAutorotateToInterfaceOrientation(
    BugViewController self, Selector sel, UIInterfaceOrientation orientation) {
    return self.shouldAutorotateToInterfaceOrientation(orientation);
  }
}
