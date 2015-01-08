//
// $Id$

package playn.tests.core;

import pythagoras.f.FloatMath;
import pythagoras.f.Rectangle;

import playn.core.*;
import playn.scene.*;
import react.Slot;

public class SubImageTest extends Test {

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

        final Texture otex = game.graphics.createTexture(orange);
        final float pw = orange.width(), ph = orange.height(), phw = pw/2, phh = ph/2;

        // create tileable sub-texture
        Texture subtex = game.graphics.createTexture(
          pw, phh, Texture.Config.DEFAULT.repeat(true, true));
        new TextureSurface(game.graphics, game.defaultBatch, subtex).begin().
          clear().draw(otex, 0, 0, pw, phh, 0, phh/2, pw, phh).end().close();

        // tile a sub-image, oh my!
        ImageLayer tiled = new ImageLayer(subtex);
        tiled.setSize(100, 100);
        addTest(10, 10, tiled, "ImageLayer tiled with sub-texture");

        // draw a subimage to a canvas
        Canvas split = game.graphics.createCanvas(orange.width(), orange.height());
        split.drawImage(orange, phw, phh, phw, phh, 0, 0, phw, phh);
        split.drawImage(orange,   0, phh, phw, phh, phw, 0, phw, phh);
        split.drawImage(orange, phw,   0, phw, phh, 0, phh, phw, phh);
        split.drawImage(orange,   0,   0, phw, phh, phw, phh, phw, phh);
        addTest(140, 10, new ImageLayer(game.graphics, split.image), "draw subimg into Canvas", 80);

        // draw a subimage in an immediate layer
        addTest(130, 100, new Layer() {
          @Override protected void paintImpl (Surface surf) {
            surf.draw(otex, 0 , 0  , pw, phh, 0, phh/2, pw, phh);
            surf.draw(otex, pw, 0  , pw, phh, 0, phh/2, pw, phh);
            surf.draw(otex, 0 , phh, pw, phh, 0, phh/2, pw, phh);
            surf.draw(otex, pw, phh, pw, phh, 0, phh/2, pw, phh);
          }
        }, 2*pw, 2*phh, "Draw sub-images immediate", 100);

        // draw an image layer whose image region oscillates
        final ImageLayer osci = new ImageLayer(otex);
        osci.region = new Rectangle(0, 0, orange.width(), orange.height());
        addTest(10, 150, osci, "ImageLayer with changing width", 100);

        conns.add(game.paint.connect(new Slot<TestsGame>() {
          public void onEmit (TestsGame game) {
            float t = game.paintTick/1000f;
            // round the width so that it sometimes goes to zero; just to be sure zero doesn't choke
            osci.region.width = Math.round(
              Math.abs(FloatMath.sin(t)) * osci.texture().displayWidth);
          }
        }));
      }
    }).onFailure(logFailure("Failed to load orange image"));
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
