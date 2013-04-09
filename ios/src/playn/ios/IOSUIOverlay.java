package playn.ios;

import cli.MonoTouch.CoreAnimation.CAShapeLayer;
import cli.MonoTouch.CoreGraphics.CGPath;
import cli.MonoTouch.UIKit.UIEvent;
import cli.MonoTouch.UIKit.UIView;
import cli.System.Drawing.PointF;
import cli.System.Drawing.RectangleF;
import playn.core.UIOverlay;
import pythagoras.f.IRectangle;

public class IOSUIOverlay extends UIView implements UIOverlay {
  public IOSUIOverlay(RectangleF bounds) {
    super(bounds);
    set_MultipleTouchEnabled(true);
  }

  @Override
  public boolean PointInside(PointF pointF, UIEvent uiEvent) {
    // if it's masked, we don't want it
    if (hidden != null && hidden.Contains(pointF)) return false;

    // only accept the touch if it is hitting one of our native widgets
    PointF screen = ConvertPointFromView(pointF, this);
    for (UIView view : get_Subviews()) {
      if (view.PointInside(ConvertPointToView(screen, view), uiEvent)) return true;
    }
    return false;
  }

  @Override
  public boolean hasOverlay() {
    return true;
  }

  @Override
  public void hideOverlay(IRectangle area) {
    hidden = area == null ? null : new RectangleF(area.x(), area.y(), area.width(), area.height());
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

  private RectangleF hidden;
}
