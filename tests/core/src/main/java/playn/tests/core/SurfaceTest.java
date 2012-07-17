//
// $Id$

package playn.tests.core;

import java.util.ArrayList;
import java.util.List;

import playn.core.ImmediateLayer;
import playn.core.Image;
import playn.core.Pattern;
import playn.core.Surface;
import playn.core.SurfaceLayer;
import static playn.core.PlayN.*;

public class SurfaceTest extends Test {

  private List<SurfaceLayer> dots = new ArrayList<SurfaceLayer>();

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
    int samples = 128; // big enough to force a buffer size increase
    final float[] verts = new float[(samples+1)*4];
    final int[] indices = new int[samples*6];
    tessellateCurve(0, 40*(float)Math.PI, verts, indices, new F() {
      public float apply (float x) { return (float)Math.sin(x/20)*50; }
    });
    final Pattern pattern = assets().getImage("images/tile.png").toPattern();
    final Image orange = assets().getImage("images/orange.png");

    // draw some wide lines
    addTest("drawLine with width", 10, 10, 120, 120, new ImmediateLayer.Renderer() {
      public void render (Surface surf) {
        drawLine(surf, 0, 0, 50, 50, 15);
        drawLine(surf, 70, 50, 120, 0, 10);
        drawLine(surf, 0, 70, 120, 120, 10);
      }
    });

    addTest("left & right half are same color", 10, 160, 100, 25, new ImmediateLayer.Renderer() {
      public void render (Surface surf) {
        surf.setFillColor(0xFF0000FF).fillRect(0, 0, 100, 25);
        // these two alpha fills should look the same
        surf.setFillColor(0x80FF0000).fillRect(0, 0, 50, 25);
        surf.setAlpha(0.5f).setFillColor(0xFFFF0000).fillRect(50, 0, 50, 25).setAlpha(1f);
      }
    });

    addTest("fillRect and drawImage at 50% alpha", 10, 240, 100, 100, new ImmediateLayer.Renderer() {
      public void render (Surface surf) {
        surf.setFillColor(0xFF0000FF).fillRect(0, 0, 100, 50);
        surf.setAlpha(0.5f);
        surf.drawImage(orange, 55, 5);
        surf.fillRect(0, 50, 50, 50);
        surf.drawImage(orange, 55, 55);
        surf.setAlpha(1f);
      }
    });

    addTest("fillRect/Triangles with pattern", 180, 10, 120, 220, new ImmediateLayer.Renderer() {
      public void render (Surface surf) {
        // fill some shapes with patterns
        surf.setFillPattern(pattern).fillRect(10, 0, 100, 100);
        // use same fill pattern for the triangles
        surf.translate(0, 160);
        surf.fillTriangles(verts, indices);
      }
    });

    // draw some randomly jiggling dots in the right half of the screen
    float hwidth = graphics().width()/2, height = graphics().height();
    for (int ii = 0; ii < 10; ii++) {
      SurfaceLayer dot = graphics().createSurfaceLayer(10, 10);
      dot.surface().setFillColor(0xFFFF0000);
      dot.surface().fillRect(0, 0, 5, 5);
      dot.surface().fillRect(5, 5, 5, 5);
      dot.surface().setFillColor(0xFF0000FF);
      dot.surface().fillRect(5, 0, 5, 5);
      dot.surface().fillRect(0, 5, 5, 5);
      dot.setTranslation(hwidth + random()*hwidth, random()*height);
      dots.add(dot);
      // System.err.println("Created dot at " + dot.transform());
      graphics().rootLayer().add(dot);
    }
  }

  protected void addTest(String descrip, float lx, float ly, float lwidth, float lheight,
                         ImmediateLayer.Renderer renderer) {
    graphics().rootLayer().addAt(graphics().createImmediateLayer(renderer), lx, ly);
    addDescrip(descrip, lx, ly+lheight+5, lwidth);
  }

  @Override
  public void paint(float alpha) {
    super.paint(alpha);

    float hwidth = graphics().width()/2, height = graphics().height();
    for (SurfaceLayer dot : dots) {
      if (random() > 0.95) {
        dot.setTranslation(hwidth + random()*hwidth, random()*height);
      }
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
