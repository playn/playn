//
// $Id$

package playn.tests.core;

import pythagoras.f.FloatMath;
import pythagoras.f.Rectangle;

import playn.core.*;
import playn.scene.*;
import react.Slot;

public class SubImageTest extends Test {

  private ImageLayer osci;

  public SubImageTest (TestsGame game) {
    super(game, "SubImageTest", "Tests sub-image rendering in various circumstances.");
  }

  @Override public void init() {
    // create a canvas image and draw subimages of that
    int r = 30;
    Canvas canvas = game.graphics.createCanvas(2*r, 2*r);
    canvas.setFillColor(0xFF99CCFF).fillCircle(r, r, r);
    fragment("CanvasImage", canvas.image, 250, 160);

    // draw subimages of a simple static image
    game.assets.getImage("images/orange.png").state.onSuccess(new Slot<Image>() {
      public void onEmit (Image orange) {
        fragment("Image", orange, 250, 10);

        final Texture oreptex = game.graphics.createTexture(orange);
        oreptex.setRepeat(true, true);
        final float pw = orange.width(), ph = orange.height(), phw = pw/2, phh = ph/2;

        // tile a sub-image, oh my!
        ImageLayer tiled = new ImageLayer(oreptex);
        tiled.region = new Rectangle(0, phh/2, pw, phh);
        tiled.setSize(100, 100);
        addTest(10, 10, tiled, "ImageLayer tiled with subimage of Image");

        // TODO: how to do in new world order?
        // // use a subimage as a fill pattern
        // CanvasImage pat = graphics().createImage(100, 100);
        // pat.canvas().setFillPattern(orangerep.toPattern());
        // pat.canvas().fillRect(0, 0, 100, 100);
        // addTest(10, 160, graphics().createImageLayer(pat), "Canvas filled with subimage");

        // TODO: this is no longer interesting because it's all textures at this point, so we've
        // already covered this case above with the stock image

        // // tile a sub-image of a surface image, oh my!
        // SurfaceImage surf = graphics().createSurface(orange.width(), orange.height());
        // surf.surface().drawImage(orange, 0, 0);
        // Image.Region surfrep = surf.subImage(0, phh/2, pw, phh);
        // surfrep.setRepeat(true, true);
        // ImageLayer surftiled = graphics().createImageLayer(surfrep);
        // surftiled.setSize(100, 100);
        // addTest(10, 300, surftiled, "ImageLayer tiled with subimage of SurfaceImage");

        // // draw a subimage to a canvas
        // CanvasImage split = graphics().createImage(orange.width(), orange.height());
        // split.canvas().drawImage(orange.subImage(0, 0, phw, phh), phw, phh);
        // split.canvas().drawImage(orange.subImage(phw, 0, phw, phh), 0, phh);
        // split.canvas().drawImage(orange.subImage(0, phh, phw, phh), phw, 0);
        // split.canvas().drawImage(orange.subImage(phw, phh, phw, phh), 0, 0);
        // addTest(140, 10, graphics().createImageLayer(split), "draw subimg into Canvas", 80);

        // draw a subimage in an immediate layer
        addTest(130, 100, new Layer() {
          @Override protected void paintImpl (Surface surf) {
            surf.draw(oreptex, 0 , 0  , pw, phh, 0, phh/2, pw, phh);
            surf.draw(oreptex, pw, 0  , pw, phh, 0, phh/2, pw, phh);
            surf.draw(oreptex, 0 , phh, pw, phh, 0, phh/2, pw, phh);
            surf.draw(oreptex, pw, phh, pw, phh, 0, phh/2, pw, phh);
          }
        }, 2*pw, 2*phh, "draw subimg into Surface", 100);

        // draw an image layer whose sub-image region oscillates
        osci = new ImageLayer(oreptex);
        osci.region = new Rectangle(0, 0, orange.width(), orange.height());
        addTest(130, 190, osci, "ImageLayer with subimage with changing width", 100);
      }
    }).onFailure(logFailure("Failed to load orange image"));

    conns.add(game.paint.connect(new Slot<TestsGame>() {
      public void onEmit (TestsGame game) {
        if (osci != null) {
          float t = game.paintTick/1000f;
          // round the width so that it sometimes goes to zero; just to be sure zero doesn't choke
          osci.region.width = Math.round(Math.abs(FloatMath.sin(t)) * osci.texture().displayWidth);
        }
      }
    }));
  }

  protected void fragment (String source, Image image, float ox, float oy) {
    float hw = image.width()/2f, hh = image.height()/2f;
    Rectangle ul = new Rectangle(0, 0, hw, hh);
    Rectangle ur = new Rectangle(hw, 0, hw, hh);
    Rectangle ll = new Rectangle(0, hh, hw, hh);
    Rectangle lr = new Rectangle(hw, hh, hw, hh);
    Rectangle ctr = new Rectangle(hw/2, hh/2, hw, hh);

    Texture tex = game.graphics.createTexture(image);
    float dx = hw + 10, dy = hh + 10;
    GroupLayer group = new GroupLayer();
    group.addAt(create(tex, ul), 0, 0);
    group.addAt(create(tex, ur), dx, 0);
    group.addAt(create(tex, ll), 0, dy);
    group.addAt(create(tex, lr), dx, dy);
    group.addAt(create(tex, ctr), dx/2, 2*dy);

    float xoff = image.width() + 20;
    group.addAt(create(tex, ul).setScale(2), xoff, 0);
    group.addAt(create(tex, ur).setScale(2), xoff+2*dx, 0);
    group.addAt(create(tex, ll).setScale(2), xoff, 2*dy);
    group.addAt(create(tex, lr).setScale(2), xoff+2*dx, 2*dy);

    game.rootLayer.addAt(group, ox, oy);
    addDescrip(source + " split into subimages, and scaled", ox, oy + image.height()*2 + 25,
               3*image.width()+40);
  }

  protected ImageLayer create (Texture tex, Rectangle region) {
    ImageLayer layer = new ImageLayer(tex);
    layer.region = region;
    return layer;
  }
  protected ImageLayer scaleLayer(ImageLayer layer, float scale) {
    layer.setScale(scale);
    return layer;
  }
}
