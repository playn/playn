/**
 * Copyright 2013 The PlayN Authors
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

import cli.MonoTouch.CoreAnimation.CAShapeLayer;
import cli.MonoTouch.CoreGraphics.CGPath;
import cli.MonoTouch.UIKit.UIEvent;
import cli.MonoTouch.UIKit.UIView;
import cli.System.Drawing.PointF;
import cli.System.Drawing.RectangleF;

import pythagoras.f.IRectangle;

import playn.core.UIOverlay;

public class IOSUIOverlay extends UIView implements UIOverlay {

  private RectangleF hidden;

  public IOSUIOverlay(RectangleF bounds) {
    super(bounds);
    set_MultipleTouchEnabled(true);
  }

  @Override
  public boolean PointInside(PointF pointF, UIEvent uiEvent) {
    // if it's masked, we don't want it
    if (hidden != null && hidden.Contains(pointF)) return false;

    // only accept the touch if it is hitting one of our native widgets
    for (UIView view : get_Subviews()) {
      if (view.PointInside(ConvertPointToView(pointF, view), uiEvent)) return true;
    }
    return false;
  }

  @Override
  public boolean hasOverlay() {
    return true;
  }

  @Override
  public void hideOverlay(IRectangle area) {
    RectangleF updated =
      area == null ? null : new RectangleF(area.x(), area.y(), area.width(), area.height());
    if (updated != null && updated.equals(hidden)) return; // NOOP
    hidden = updated;
    updateHidden();
  }

  protected void updateHidden() {
    if (hidden == null) {
      get_Layer().set_Mask(null);
      return;
    }

    RectangleF bounds = get_Bounds();
    CAShapeLayer maskLayer = new CAShapeLayer();
    // draw four rectangles surrounding the area we want to hide, and create a mask out of it.
    CGPath path = new CGPath();
    // top
    path.AddRect(new RectangleF(0, 0, bounds.get_Width(), hidden.get_Top()));
    // bottom
    path.AddRect(new RectangleF(0, hidden.get_Bottom(), bounds.get_Width(),
      bounds.get_Bottom() - hidden.get_Bottom()));
    // left
    path.AddRect(new RectangleF(0, hidden.get_Top(), hidden.get_Left(), hidden.get_Height()));
    // right
    path.AddRect(new RectangleF(hidden.get_Right(), hidden.get_Top(),
      bounds.get_Right() - hidden.get_Right(), hidden.get_Height()));
    maskLayer.set_Path(path);
    get_Layer().set_Mask(maskLayer);
  }
}
