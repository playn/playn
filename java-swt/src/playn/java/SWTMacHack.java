/**
 * Copyright 2010-2015 The PlayN Authors
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
package playn.java;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.cocoa.*;
import org.eclipse.swt.opengl.GLCanvas;

import playn.core.Scale;
import pythagoras.f.FloatMath;

public class SWTMacHack extends SWTGraphics.Hack {

  private static final long sel_setWantsBestResolutionOpenGLSurface_ =
    OS.sel_registerName("setWantsBestResolutionOpenGLSurface:");
  private static final long sel_convertSizeToBacking_ =
    OS.sel_registerName("convertSizeToBacking:");

  private NSSize convertSizeToBacking (NSView view, NSSize size) {
    NSSize result = new NSSize();
    OS.objc_msgSend_stret(result, view.id, sel_convertSizeToBacking_, size);
    return result;
  }

  public final float screenScale;

  public SWTMacHack () {
    NSArray screens = NSScreen.screens();
    NSScreen screen = new NSScreen(screens.objectAtIndex(0));
    this.screenScale = (float)screen.backingScaleFactor();
  }

  @Override public Scale hackScale () { return new Scale(screenScale); }

  @Override public void hackCanvas (GLCanvas canvas) {
    OS.objc_msgSend(canvas.view.id, sel_setWantsBestResolutionOpenGLSurface_, true);
  }

  @Override public void convertToBacking (GLCanvas canvas, Rectangle bounds) {
    // the below is the way we're *supposed* to handle Retina displays, but for whatever unknown
    // reason it just returns a 1x size regardless of the actual backing scale

    // NSSize backingSize = new NSSize();
    // backingSize.width = bounds.width;
    // backingSize.height = bounds.height;
    // convertSizeToBacking(canvas.view, backingSize);

    // so instead we use the deprecated API above (getting the screen backing scale factor) and
    // then forcibly apply that to the bounds
    bounds.width = FloatMath.iceil(bounds.width * screenScale);
    bounds.height = FloatMath.iceil(bounds.height * screenScale);
  }
}
