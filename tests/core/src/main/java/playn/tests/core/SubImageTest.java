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
    super(game, "SubImage", "Tests sub-image rendering in various circumstances.");
  }

  @Override public void init() {
    // create a canvas image and draw subimages of that
    int r = 30;
    Canvas canvas = game.graphics.createCanvas(2*r, 2*r);
    canvas.setFillColor(0xFF99CCFF).fillCircle(r, r, r);
    fragment("CanvasImage", canvas.toTexture(), 250, 160);

    // draw subimages of a simple static image
    game.assets.getImage("images/orange.png").state.onSuccess(new Slot<Image>() {
      public void onEmit (Image orange) {
        final Texture otex = orange.texture();
        fragment("Image", otex, 250, 10);

        final float pw = orange.width(), ph = orange.height(), phw = pw/2, phh = ph/2;
        final Tile otile = otex.tile(0, phh/2, pw, phh);

        // create tileable sub-texture
        Texture subtex = game.graphics.createTexture(
          otile.width(), otile.height(), Texture.Config.DEFAULT.repeat(true, true));
        new TextureSurface(game.graphics, game.defaultBatch, subtex).begin().
          clear().draw(otile, 0, 0).end().close();

        // tile a sub-image, oh my!
        ImageLayer tiled = new ImageLayer(subtex);
        tiled.setSize(100, 100);
        addTest(10, 10, tiled, "Tile to reptex to ImageLayer");

        // draw a subimage to a canvas
        Canvas split = game.graphics.createCanvas(orange.width(), orange.height());
        split.draw(orange.region(  0,   0, phw, phh), phw, phh);
        split.draw(orange.region(phw,   0, phw, phh),   0, phh);
        split.draw(orange.region(  0, phh, phw, phh), phw,   0);
        split.draw(orange.region(phw, phh, phw, phh),   0,   0);
        addTest(140, 10, new ImageLayer(split.toTexture()), "Canvas draw Image.Region", 80);

        // draw a subimage in an immediate layer
        addTest(130, 100, new Layer() {
          @Override protected void paintImpl (Surface surf) {
            surf.draw(otile, 0 , 0  );
            surf.draw(otile, pw, 0  );
            surf.draw(otile, 0 , phh);
            surf.draw(otile, pw, phh);
          }
        }, 2*pw, 2*phh, "Surface draw Tile", 100);

        // draw an image layer whose image region oscillates
        final ImageLayer osci = new ImageLayer(otex);
        osci.region = new Rectangle(0, 0, orange.width(), orange.height());
        addTest(10, 150, osci, "ImageLayer with changing width", 100);

        conns.add(game.paint.connect(new Slot<Clock>() {
          public void onEmit (Clock clock) {
            float t = clock.tick/1000f;
            // round the width so that it sometimes goes to zero; just to be sure zero doesn't choke
            osci.region.width = Math.round(Math.abs(FloatMath.sin(t)) * osci.tile().width());
          }
        }));
      }
    }).onFailure(logFailure("Failed to load orange image"));
  }

  protected void fragment (String source, Texture tex, float ox, float oy) {
    float hw = tex.displayWidth/2f, hh = tex.displayHeight/2f;
    Tile ul = tex.tile(0, 0, hw, hh);
    Tile ur = tex.tile(hw, 0, hw, hh);
    Tile ll = tex.tile(0, hh, hw, hh);
    Tile lr = tex.tile(hw, hh, hw, hh);
    Tile ctr = tex.tile(hw/2, hh/2, hw, hh);

    float dx = hw + 10, dy = hh + 10;
    GroupLayer group = new GroupLayer();
    group.addAt(new ImageLayer(ul), 0, 0);
    group.addAt(new ImageLayer(ur), dx, 0);
    group.addAt(new ImageLayer(ll), 0, dy);
    group.addAt(new ImageLayer(lr), dx, dy);
    group.addAt(new ImageLayer(ctr), dx/2, 2*dy);

    float xoff = tex.displayWidth + 20;
    group.addAt(new ImageLayer(ul).setScale(2), xoff, 0);
    group.addAt(new ImageLayer(ur).setScale(2), xoff+2*dx, 0);
    group.addAt(new ImageLayer(ll).setScale(2), xoff, 2*dy);
    group.addAt(new ImageLayer(lr).setScale(2), xoff+2*dx, 2*dy);

    game.rootLayer.addAt(group, ox, oy);
    addDescrip(source + " to Texture to Tiles, and scaled", ox, oy + tex.displayHeight*2 + 25,
               3*tex.displayWidth+40);
  }
}
