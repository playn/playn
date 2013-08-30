//
// $Id$

package playn.tests.core;

import java.util.ArrayList;
import java.util.List;

import pythagoras.f.FloatMath;
import pythagoras.f.Rectangle;

import playn.core.AssetWatcher;
import playn.core.GroupLayer;
import playn.core.Image;
import playn.core.ImageLayer;
import playn.core.ImmediateLayer;
import playn.core.Pattern;
import playn.core.Surface;
import playn.core.SurfaceImage;
import static playn.core.PlayN.*;

public class SurfaceTest extends Test {

  private List<ImageLayer> dots = new ArrayList<ImageLayer>();
  private SurfaceImage paintUpped;
  private Rectangle dotBox;
  private int elapsed;

  @Override
  public String getName() {
    return "SurfaceTest";
  }

  @Override
  public String getDescription() {
    return "Tests various Surface rendering features.";
  }

  @Override
  public void init() {
    final Image tile = assets().getImage("images/tile.png");
    final Image orange = assets().getImage("images/orange.png");
    AssetWatcher watcher = new AssetWatcher(new AssetWatcher.Listener() {
      @Override public void done() {
        addTests(orange, tile);
      }
      @Override public void error(Throwable err) {
        addDescrip("Error: " + err.getMessage(), 10, errY, graphics().width()-20);
        errY += 30;
      }
      private float errY = 10;
    });
    watcher.add(tile);
    watcher.add(orange);
    watcher.start();
  }

  @Override
  public void dispose() {
    dots.clear();
    paintUpped = null;
  }

  protected void addTests (final Image orange, Image tile) {
    final Pattern pattern = tile.toPattern();

    int samples = 128; // big enough to force a buffer size increase
    final float[] verts = new float[(samples+1)*4];
    final int[] indices = new int[samples*6];
    tessellateCurve(0, 40*(float)Math.PI, verts, indices, new F() {
      public float apply (float x) { return (float)Math.sin(x/20)*50; }
    });

    float ygap = 20, ypos = 10;

    // draw some wide lines
    ypos = ygap + addTest(10, ypos, new ImmediateLayer.Renderer() {
      public void render (Surface surf) {
        drawLine(surf, 0, 0, 50, 50, 15);
        drawLine(surf, 70, 50, 120, 0, 10);
        drawLine(surf, 0, 70, 120, 120, 10);
      }
    }, 120, 120, "drawLine with width");

    ypos = ygap + addTest(20, ypos, new ImmediateLayer.Renderer() {
      public void render (Surface surf) {
        surf.setFillColor(0xFF0000FF).fillRect(0, 0, 100, 25);
        // these two alpha fills should look the same
        surf.setFillColor(0x80FF0000).fillRect(0, 0, 50, 25);
        surf.setAlpha(0.5f).setFillColor(0xFFFF0000).fillRect(50, 0, 50, 25).setAlpha(1f);
      }
    }, 100, 25, "left and right half both same color");

    ypos = ygap + addTest(20, ypos, new ImmediateLayer.Renderer() {
      public void render (Surface surf) {
        surf.setFillColor(0xFF0000FF).fillRect(0, 0, 100, 50);
        surf.setAlpha(0.5f);
        surf.drawImage(orange, 55, 5);
        surf.fillRect(0, 50, 50, 50);
        surf.drawImage(orange, 55, 55);
        surf.setAlpha(1f);
      }
    }, 100, 100, "fillRect and drawImage at 50% alpha");

    ypos = 10;

    ypos = ygap + addTest(160, ypos, new ImmediateLayer.Renderer() {
      public void render (Surface surf) {
        // fill some shapes with patterns
        surf.setFillPattern(pattern).fillRect(10, 0, 100, 100);
        // use same fill pattern for the triangles
        surf.translate(0, 160);
        surf.fillTriangles(verts, indices);
      }
    }, 120, 210, "ImmediateLayer patterned fillRect, fillTriangles");

    SurfaceImage patted = graphics().createSurface(100, 100);
    patted.surface().setFillPattern(pattern).fillRect(0, 0, 100, 100);
    ypos = ygap + addTest(170, ypos, graphics().createImageLayer(patted),
                          "SurfaceImage patterned fillRect");

    ypos = 10;

    // fill a patterned quad in a clipped group layer
    final int twidth = 150, theight = 75;
    GroupLayer group = graphics().createGroupLayer();
    ypos = ygap + addTest(315, 10, group, twidth, theight,
                          "Clipped pattern should not exceed grey rectangle");
    group.add(graphics().createImmediateLayer(new ImmediateLayer.Renderer() {
      public void render (Surface surf) {
        surf.setFillColor(0xFFCCCCCC).fillRect(0, 0, twidth, theight);
      }
    }));
    group.add(graphics().createImmediateLayer(twidth, theight, new ImmediateLayer.Renderer() {
      public void render (Surface surf) {
        surf.setFillPattern(pattern).fillRect(-10, -10, twidth+20, theight+20);
      }
    }));

    // draw some randomly jiggling dots inside a bounded region
    dotBox = new Rectangle(315, ypos, 200, 100);
    ypos = ygap + addTest(dotBox.x, dotBox.y, new ImmediateLayer.Renderer() {
      public void render (Surface surf) {
        surf.setFillColor(0xFFCCCCCC).fillRect(0, 0, dotBox.width, dotBox.height);
      }
    }, dotBox.width, dotBox.height, "Randomly positioned SurfaceImages");
    for (int ii = 0; ii < 10; ii++) {
      SurfaceImage dot = graphics().createSurface(10, 10);
      dot.surface().setFillColor(0xFFFF0000);
      dot.surface().fillRect(0, 0, 5, 5);
      dot.surface().fillRect(5, 5, 5, 5);
      dot.surface().setFillColor(0xFF0000FF);
      dot.surface().fillRect(5, 0, 5, 5);
      dot.surface().fillRect(0, 5, 5, 5);
      ImageLayer dotl = graphics().createImageLayer(dot);
      dotl.setTranslation(dotBox.x + random()*(dotBox.width-10),
                          dotBox.y + random()*(dotBox.height-10));
      dots.add(dotl);

      // System.err.println("Created dot at " + dotl.transform());
      graphics().rootLayer().add(dotl);
    }

    // add a surface layer that is updated on every call to paint (a bad practice, but one that
    // should actually work)
    paintUpped = graphics().createSurface(100, 100);
    ypos = ygap + addTest(315, ypos, graphics().createImageLayer(paintUpped),
                          "SurfaceImage updated in paint()");
  }

  protected float addTest(float lx, float ly, ImmediateLayer.Renderer renderer,
                          float lwidth, float lheight, String descrip) {
    return addTest(lx, ly, graphics().createImmediateLayer(renderer), lwidth, lheight, descrip);
  }

  @Override
  public void update(int delta) {
    elapsed += delta;
  }

  @Override
  public void paint(float alpha) {
    for (ImageLayer dot : dots) {
      if (random() > 0.95) {
        dot.setTranslation(dotBox.x + random()*(dotBox.width-10),
                           dotBox.y + random()*(dotBox.height-10));
      }
    }

    if (paintUpped != null) {
      float now = (elapsed + UPDATE_RATE*alpha)/1000;
      float sin = Math.abs(FloatMath.sin(now)), cos = Math.abs(FloatMath.cos(now));
      int sinColor = (int)(sin * 255), cosColor = (int)(cos * 255);
      int c1 = (0xFF << 24) | (sinColor << 16) | (cosColor << 8);
      int c2 = (0xFF << 24) | (cosColor << 16) | (sinColor << 8);
      paintUpped.surface().clear();
      paintUpped.surface().setFillColor(c1).fillRect(0, 0, 50, 50);
      paintUpped.surface().setFillColor(c2).fillRect(50, 50, 50, 50);
    }
  }

  void drawLine(Surface surf, float x1, float y1, float x2, float y2, float width) {
    float xmin = Math.min(x1, x2), xmax = Math.max(x1, x2);
    float ymin = Math.min(y1, y2), ymax = Math.max(y1, y2);

    surf.setFillColor(0xFF0000AA);
    surf.fillRect(xmin, ymin, xmax-xmin, ymax-ymin);

    surf.setFillColor(0xFF99FFCC);
    surf.drawLine(x1, y1, x2, y2, width);

    surf.setFillColor(0xFFFF0000);
    surf.fillRect(x1, y1, 1, 1);
    surf.fillRect(x2, y2, 1, 1);
  }

  private interface F {
    public float apply (float x);
  }

  void tessellateCurve (float minx, float maxx, float[] verts, int[] indices, F f) {
    int slices = (verts.length-1)/4, vv = 0;
    float dx = (maxx-minx)/slices;
    for (float x = minx; x < maxx; x += dx) {
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
