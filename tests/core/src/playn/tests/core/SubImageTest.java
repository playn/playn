//
// $Id$

package playn.tests.core;

import playn.core.GroupLayer;
import playn.core.Canvas;
import playn.core.CanvasImage;
import playn.core.Image;
import playn.core.ImageLayer;
import static playn.core.PlayN.*;

public class SubImageTest extends Test {

  @Override
  public String getName() {
    return "SubImageTest";
  }

  @Override
  public String getDescription() {
    return "Tests sub-image rendering in various circumstances.";
  }

  @Override
  public void init() {
    // draw subimages of a simple static image
    Image pea = assets().getImage("images/pea.png");
    fragment(pea, 10, 10);

    // create a canvas image and draw subimages of that
    int r = 50;
    CanvasImage cimg = graphics().createImage(2*r, 2*r);
    Canvas canvas = cimg.canvas();
    canvas.setFillColor(0xFF99CCFF);
    canvas.fillCircle(r, r, r);
    fragment(cimg, 200, 10);

    // tile a sub-image, oh my!
    float pw = pea.width(), ph = pea.height();
    ImageLayer tiled = graphics().createImageLayer(pea.subImage(0, ph/4, pw, ph/2));
    tiled.setRepeatX(true);
    tiled.setRepeatY(true);
    tiled.setSize(100, 100);
    graphics().rootLayer().addAt(tiled, 10, 150);

    // draw a subimage to a canvas
    float phw = pw/2, phh = ph/2;
    CanvasImage split = graphics().createImage(pea.width(), pea.height());
    split.canvas().drawImage(pea.subImage(0, 0, phw, phh), phw, phh);
    split.canvas().drawImage(pea.subImage(phw, 0, phw, phh), 0, phh);
    split.canvas().drawImage(pea.subImage(0, phh, phw, phh), phw, 0);
    split.canvas().drawImage(pea.subImage(phw, phh, phw, phh), 0, 0);
    graphics().rootLayer().addAt(graphics().createImageLayer(split), 130, 150);

    // use a subimage as a fill pattern
    CanvasImage pat = graphics().createImage(100, 100);
    pat.canvas().setFillPattern(pea.subImage(0, phw/2, pw, phw).toPattern());
    pat.canvas().fillRect(0, 0, 100, 100);
    graphics().rootLayer().addAt(graphics().createImageLayer(pat), 10, 270);
  }

  protected void fragment (Image image, float ox, float oy) {
    int hw = image.width()/2, hh = image.height()/2;
    Image ul = image.subImage(0, 0, hw, hh);
    Image ur = image.subImage(hw, 0, hw, hh);
    Image ll = image.subImage(0, hh, hw, hh);
    Image lr = image.subImage(hw, hh, hw, hh);
    Image ctr = image.subImage(hw/2, hh/2, hw, hh);

    float dx = hw + 10, dy = hh + 10;
    GroupLayer group = graphics().createGroupLayer();
    group.addAt(graphics().createImageLayer(ul), 0, 0);
    group.addAt(graphics().createImageLayer(ur), dx, 0);
    group.addAt(graphics().createImageLayer(ll), 0, dy);
    group.addAt(graphics().createImageLayer(lr), dx, dy);
    group.addAt(graphics().createImageLayer(ctr), dx/2, 2*dy);

    float xoff = image.width() + 20;
    group.addAt(scaleLayer(graphics().createImageLayer(ul), 2), xoff, 0);
    group.addAt(scaleLayer(graphics().createImageLayer(ur), 2), xoff+2*dx, 0);
    group.addAt(scaleLayer(graphics().createImageLayer(ll), 2), xoff, 2*dy);
    group.addAt(scaleLayer(graphics().createImageLayer(lr), 2), xoff+2*dx, 2*dy);

    graphics().rootLayer().addAt(group, ox, oy);
  }

  protected ImageLayer scaleLayer (ImageLayer layer, float scale) {
    layer.setScale(scale);
    return layer;
  }
}
