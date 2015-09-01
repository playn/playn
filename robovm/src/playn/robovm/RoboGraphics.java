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
import org.robovm.apple.coregraphics.CGBitmapInfo;
import org.robovm.apple.coregraphics.CGColorSpace;
import org.robovm.apple.coregraphics.CGImageAlphaInfo;
import org.robovm.apple.coregraphics.CGRect;
import org.robovm.apple.uikit.UIDevice;
import org.robovm.apple.uikit.UIScreen;
import org.robovm.apple.uikit.UIUserInterfaceIdiom;

import playn.core.*;
import pythagoras.f.Dimension;
import pythagoras.f.IDimension;
import pythagoras.f.IPoint;
import pythagoras.f.Point;

/**
 * Provides graphics implementation on iOS.
 */
public class RoboGraphics extends Graphics {

  // a shared colorspace instance for use all over the place
  static final CGColorSpace colorSpace = CGColorSpace.createDeviceRGB();

  final Platform plat;
  final private RoboPlatform.Config config;
  private final float touchScale;
  private final Point touchTemp = new Point();
  private final Dimension screenSize = new Dimension();
  private int defaultFramebuffer;

  // a scratch bitmap context used for measuring text
  private static final int S_SIZE = 10;
  final CGBitmapContext scratchCtx = createCGBitmap(S_SIZE, S_SIZE);

  private static boolean useHalfSize (RoboPlatform.Config config) {
    boolean isPad = UIDevice.getCurrentDevice().getUserInterfaceIdiom() == UIUserInterfaceIdiom.Pad;
    return isPad && config.iPadLikePhone;
  }
  private static Scale viewScale (RoboPlatform.Config config) {
    float deviceScale = (float)UIScreen.getMainScreen().getScale();
    boolean useHalfSize = useHalfSize(config);
    return new Scale((useHalfSize ? 2 : 1) * deviceScale);
  }

  public RoboGraphics(Platform plat, RoboPlatform.Config config, CGRect bounds) {
    super(plat, new RoboGL20(), viewScale(config));
    this.plat = plat;
    this.config = config;
    this.touchScale = useHalfSize(config) ? 2 : 1;
    boundsChanged(bounds);
  }

  @Override public IDimension screenSize() {
    // we just recompute this when asked so that we have the right orientation

    // TODO: is this properly resolution independent?
    CGRect screenBounds = UIScreen.getMainScreen().getBounds();
    // TODO: (plat.osVersion < 8) manually divide by scale factor?
    // tODO: (plat.osVersion < 8) manually flip width/height when in landscape?
    screenSize.width = (int)screenBounds.getWidth();
    screenSize.height = (int)screenBounds.getHeight();
    if (useHalfSize(config)) {
      screenSize.width /= 2;
      screenSize.height /= 2;
    }
    return screenSize;
  }

  @Override public TextLayout layoutText(String text, TextFormat format) {
    return RoboTextLayout.layoutText(this, text, format);
  }

  @Override public TextLayout[] layoutText(String text, TextFormat format, TextWrap wrap) {
    return RoboTextLayout.layoutText(this, text, format, wrap);
  }

  @Override protected int defaultFramebuffer () { return defaultFramebuffer; }

  @Override protected Canvas createCanvasImpl (Scale scale, int pixelWidth, int pixelHeight) {
    return new RoboCanvas(this, new RoboCanvasImage(this, scale, pixelWidth, pixelHeight,
                                                    config.interpolateCanvasDrawing));
  }

  static CGBitmapContext createCGBitmap(int width, int height) {
    return CGBitmapContext.create(width, height, 8, 4 * width, colorSpace, new CGBitmapInfo(
      CGImageAlphaInfo.PremultipliedLast.value()));
  }

  // called when our view appears
  void viewDidInit(CGRect bounds) {
    defaultFramebuffer = gl.glGetInteger(GL20.GL_FRAMEBUFFER_BINDING);
    if (defaultFramebuffer == 0) throw new IllegalStateException(
      "Failed to determine defaultFramebuffer");
    boundsChanged(bounds);
  }

  void boundsChanged(CGRect bounds) {
    viewportChanged(scale().scaledCeil((float)bounds.getWidth()),
                    scale().scaledCeil((float)bounds.getHeight()));
  }

  IPoint transformTouch(float x, float y) {
    return touchTemp.set(x/touchScale, y/touchScale);
  }
}
