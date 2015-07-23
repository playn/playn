//
// $Id$

package playn.tests.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pythagoras.f.AffineTransform;
import pythagoras.f.FloatMath;
import pythagoras.f.Rectangle;
import react.RFuture;
import react.Slot;
import react.UnitSlot;

import playn.core.*;
import playn.scene.*;

public class SurfaceTest extends Test {

  private TextureSurface paintUpped;

  public SurfaceTest (TestsGame game) {
    super(game, "Surface", "Tests various Surface rendering features.");
  }

  @Override public void init() {
    final Image tile = game.assets.getImage("images/tile.png");
    final Image orange = game.assets.getImage("images/orange.png");
    Slot<Throwable> onError = new Slot<Throwable>() {
      float errY = 0;
      public void onEmit (Throwable err) {
        addDescrip("Error: " + err.getMessage(), 10, errY, game.graphics.viewSize.width()-20);
        errY += 30;
      }
    };
    tile.state.onFailure(onError);
    orange.state.onFailure(onError);
    RFuture.collect(Arrays.asList(tile.state, orange.state)).onSuccess(new UnitSlot() {
      public void onEmit () { addTests(orange, tile); }
    });
  }

  @Override
  public void dispose() {
    super.dispose();
    if (paintUpped != null) {
      paintUpped.close();
      paintUpped = null;
    }
  }

  protected void addTests (final Image orange, Image tile) {
    final Texture otex = orange.texture();
    final Texture ttex = tile.createTexture(Texture.Config.DEFAULT.repeat(true, true));

    // make samples big enough to force a buffer size increase
    final int samples = 128, hsamples = samples/2;
    final float[] verts = new float[(samples+1)*4];
    final int[] indices = new int[samples*6];
    tessellateCurve(0, 40*(float)Math.PI, verts, indices, new F() {
      public float apply (float x) { return (float)Math.sin(x/20)*50; }
    });

    float ygap = 20, ypos = 10;

    // draw some wide lines
    ypos = ygap + addTest(10, ypos, new Layer() {
      protected void paintImpl (Surface surf) {
        drawLine(surf,  0,  0,  50,  50, 15);
        drawLine(surf, 70, 50, 120,   0, 10);
        drawLine(surf,  0, 70, 120, 120, 10);
      }
    }, 120, 120, "drawLine with width");

    ypos = ygap + addTest(20, ypos, new Layer() {
      protected void paintImpl (Surface surf) {
        surf.setFillColor(0xFF0000FF).fillRect(0, 0, 100, 25);
        // these two alpha fills should look the same
        surf.setFillColor(0x80FF0000).fillRect(0, 0, 50, 25);
        surf.setAlpha(0.5f).setFillColor(0xFFFF0000).fillRect(50, 0, 50, 25).setAlpha(1f);
      }
    }, 100, 25, "left and right half both same color");

    ypos = ygap + addTest(20, ypos, new Layer() {
      protected void paintImpl (Surface surf) {
        surf.setFillColor(0xFF0000FF).fillRect(0, 0, 100, 50);
        surf.setAlpha(0.5f);
        surf.fillRect(0, 50, 50, 50);
        surf.draw(otex, 55, 5);
        surf.draw(otex, 55, 55);
        surf.setAlpha(1f);
      }
    }, 100, 100, "fillRect and drawImage at 50% alpha");

    ypos = 10;

    final TriangleBatch triangleBatch = new TriangleBatch(game.graphics.gl);
    final AffineTransform af = new AffineTransform().
      scale(game.graphics.scale().factor, game.graphics.scale().factor).
      translate(160, (ygap + 150));

    ypos = ygap + addTest(160, ypos, new Layer() {
      protected void paintImpl (Surface surf) {
        // fill some shapes with patterns
        surf.setFillPattern(ttex).fillRect(10, 0, 100, 100);
        // render a sliding window of half of our triangles to test the slice rendering
        triangleBatch.addTris(ttex, Tint.NOOP_TINT, af,
          verts, offset*4, (hsamples+1)*4, ttex.width(), ttex.height(),
          indices, offset*6, hsamples*6, offset*2);
        offset += doff;
        if (offset == 0) doff = 1;
        else if (offset == hsamples) doff = -1;
      }
      private int offset = 0, doff = 1;
    }.setBatch(triangleBatch), 120, 210, "ImmediateLayer patterned fillRect, fillTriangles");

    TextureSurface patted = game.createSurface(100, 100);
    patted.begin().clear().setFillPattern(ttex).fillRect(0, 0, 100, 100).end().close();
    ypos = ygap + addTest(170, ypos, new ImageLayer(patted.texture),
                          "SurfaceImage patterned fillRect");

    ypos = 10;

    // fill a patterned quad in a clipped group layer
    final int twidth = 150, theight = 75;
    GroupLayer group = new GroupLayer();
    ypos = ygap + addTest(315, 10, group, twidth, theight,
                          "Clipped pattern should not exceed grey rectangle");
    group.add(new Layer() {
      protected void paintImpl (Surface surf) {
        surf.setFillColor(0xFFCCCCCC).fillRect(0, 0, twidth, theight);
      }
    });
    group.add(new ClippedLayer(twidth, theight) {
      protected void paintClipped (Surface surf) {
        surf.setFillPattern(ttex).fillRect(-10, -10, twidth+20, theight+20);
      }
    });

    // add a surface layer that is updated on every call to paint
    // (a bad practice, but one that should actually work)
    paintUpped = game.createSurface(100, 100);
    ypos = ygap + addTest(315, ypos, new ImageLayer(paintUpped.texture),
                          "SurfaceImage updated in paint()");

    // draw some randomly jiggling dots inside a bounded region
    final List<ImageLayer> dots = new ArrayList<ImageLayer>();
    final Rectangle dotBox = new Rectangle(315, ypos, 200, 100);
    ypos = ygap + addTest(dotBox.x, dotBox.y, new Layer() {
      protected void paintImpl (Surface surf) {
        surf.setFillColor(0xFFCCCCCC).fillRect(0, 0, dotBox.width, dotBox.height);
      }
    }, dotBox.width, dotBox.height, "Randomly positioned SurfaceImages");
    for (int ii = 0; ii < 10; ii++) {
      TextureSurface dot = game.createSurface(10, 10);
      dot.begin().
        setFillColor(0xFFFF0000).fillRect(0, 0, 5, 5).fillRect(5, 5, 5, 5).
        setFillColor(0xFF0000FF).fillRect(5, 0, 5, 5).fillRect(0, 5, 5, 5).
        end().close();
      ImageLayer dotl = new ImageLayer(dot.texture);
      dotl.setTranslation(dotBox.x + (float)Math.random()*(dotBox.width-10),
                          dotBox.y + (float)Math.random()*(dotBox.height-10));
      dots.add(dotl);

      game.rootLayer.add(dotl);
    }

    conns.add(game.paint.connect(new Slot<Clock>() {
      public void onEmit (Clock clock) {
        for (ImageLayer dot : dots) {
          if (Math.random() > 0.95) {
            dot.setTranslation(dotBox.x + (float)Math.random()*(dotBox.width-10),
                               dotBox.y + (float)Math.random()*(dotBox.height-10));
          }
        }

        float now = clock.tick/1000f;
        float sin = Math.abs(FloatMath.sin(now)), cos = Math.abs(FloatMath.cos(now));
        int sinColor = (int)(sin * 255), cosColor = (int)(cos * 255);
        int c1 = (0xFF << 24) | (sinColor << 16) | (cosColor << 8);
        int c2 = (0xFF << 24) | (cosColor << 16) | (sinColor << 8);
        paintUpped.begin().clear().
          setFillColor(c1).fillRect(0, 0, 50, 50).
          setFillColor(c2).fillRect(50, 50, 50, 50).
          end();
      }
    }));
  }

  void drawLine(Surface surf, float x1, float y1, float x2, float y2, float width) {
    float xmin = Math.min(x1, x2), xmax = Math.max(x1, x2);
    float ymin = Math.min(y1, y2), ymax = Math.max(y1, y2);
    surf.setFillColor(0xFF0000AA).fillRect(xmin, ymin, xmax-xmin, ymax-ymin);
    surf.setFillColor(0xFF99FFCC).drawLine(x1, y1, x2, y2, width);
    surf.setFillColor(0xFFFF0000).fillRect(x1, y1, 1, 1).fillRect(x2, y2, 1, 1);
  }

  private interface F {
    public float apply (float x);
  }

  void tessellateCurve (float minx, float maxx, float[] verts, int[] indices, F f) {
    int slices = (verts.length-1)/4, vv = 0;
    float dx = (maxx-minx)/slices;
    for (float x = minx; vv < verts.length; x += dx) {
      verts[vv++] = x;
      verts[vv++] = 0;
      verts[vv++] = x;
      verts[vv++] = f.apply(x);
    }
    for (int ss = 0, ii = 0; ss < slices; ss++) {
      int base = ss*2;
      indices[ii++] = base;
      indices[ii++] = base+1;
      indices[ii++] = base+3;
      indices[ii++] = base;
      indices[ii++] = base+3;
      indices[ii++] = base+2;
    }
  }
}
