//
// $Id$

package playn.tests.core;

import playn.core.ImmediateLayer;
import playn.core.Surface;
import static playn.core.PlayN.*;

public class SurfaceTest extends Test {
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
    ImmediateLayer unclipped = graphics().createImmediateLayer(new ImmediateLayer.Renderer() {
      public void render (Surface surf) {
        drawLine(surf, 10, 50, 60, 100, 15);
        drawLine(surf, 80, 100, 130, 50, 10);
        drawLine(surf, 10, 120, 130, 180, 10);
      }
    });
    graphics().rootLayer().add(unclipped);
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
}
