package playn.core;

import org.jbox2d.callbacks.DebugDraw;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.OBBViewportTransform;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;

import static playn.core.PlayN.log;

public class DebugDrawBox2D extends DebugDraw {

  private static String CANVASERROR =
      "Must set canvas (DebugDrawBox2D.setCanvas()) in DebugDrawBox2D before drawing.";

  private Canvas canvas;

  private float strokeWidth;

  private int strokeAlpha;

  private int fillAlpha;

  private float cameraX, cameraY, cameraScale = 1;

  private static float cacheFillR, cacheFillG, cacheFillB; // cached fill color

  private static float cacheStrokeR, cacheStrokeG, cacheStrokeB; // cached
                                                                 // stroke color

  private final Vec2 tempVec1 = new Vec2();

  private final Vec2 tempVec2 = new Vec2();

  private final Vec2 tempVec3 = new Vec2();

  public DebugDrawBox2D() {
    super(new OBBViewportTransform());
    viewportTransform.setYFlip(true);
    strokeWidth = 1.0f;
    strokeAlpha = 255;
    fillAlpha = 150;
  }

  @Override
  public void drawCircle(Vec2 center, float radius, Color3f color) {
    if (canvas == null) {
      log().error(CANVASERROR);
      return;
    }

    setFillColor(color);
    setStrokeColor(color);
    // calculate the effective radius
    tempVec1.set(center.x + radius, center.y + radius);
    getWorldToScreenToOut(tempVec1, tempVec1);
    getWorldToScreenToOut(center, tempVec2);
    canvas.fillCircle(tempVec2.x, tempVec2.y, tempVec1.x - tempVec2.x);
    canvas.strokeCircle(tempVec2.x, tempVec2.y, tempVec1.x - tempVec2.x);
  }

  @Override
  public void drawPoint(Vec2 argPoint, float argRadiusOnScreen, Color3f color) {
    if (canvas == null) {
      log().error(CANVASERROR);
      return;
    }

    setFillColor(color);
    setStrokeColor(color);
    getWorldToScreenToOut(argPoint, tempVec1);
    canvas.fillCircle(tempVec1.x, tempVec1.y, argRadiusOnScreen);
  }

  @Override
  public void drawSegment(Vec2 p1, Vec2 p2, Color3f color) {
    if (canvas == null) {
      log().error(CANVASERROR);
      return;
    }

    setStrokeColor(color);
    setFillColor(color);
    getWorldToScreenToOut(p1, tempVec1);
    getWorldToScreenToOut(p2, tempVec2);
    canvas.drawLine(tempVec1.x, tempVec1.y, tempVec2.x, tempVec2.y);
  }

  @Override
  public void drawSolidCircle(Vec2 center, float radius, Vec2 axis, Color3f color) {
    if (canvas == null) {
      log().error(CANVASERROR);
      return;
    }

    setFillColor(color);
    setStrokeColor(color);
    // calculate the effective radius
    tempVec1.set(center.x + radius, center.y + radius);
    getWorldToScreenToOut(tempVec1, tempVec1);
    getWorldToScreenToOut(center, tempVec2);
    getWorldToScreenToOut(axis, tempVec3);
    canvas.fillCircle(tempVec2.x, tempVec2.y, tempVec1.x - tempVec2.x);
    canvas.strokeCircle(tempVec2.x, tempVec2.y, tempVec1.x - tempVec2.x);
  }

  @Override
  public void drawSolidPolygon(Vec2[] vertices, int vertexCount, Color3f color) {
    if (canvas == null) {
      log().error(CANVASERROR);
      return;
    }

    setFillColor(color);
    setStrokeColor(color);
    Path path = canvas.createPath();
    for (int i = 0; i < vertexCount; i++) {
      getWorldToScreenToOut(vertices[i], tempVec1);
      if (i == 0) {
        path.moveTo(tempVec1.x, tempVec1.y);
      } else {
        path.lineTo(tempVec1.x, tempVec1.y);
      }
    }
    path.close();
    canvas.fillPath(path);
    canvas.strokePath(path);
  }

  @Override
  public void drawString(float x, float y, String s, Color3f color) {
    if (canvas == null) {
      log().error(CANVASERROR);
      return;
    }

    log().info("drawString not yet implemented in DebugDrawBox2D: " + s);
  }

  @Override
  public void drawTransform(Transform xf) {
    if (canvas == null) {
      log().error(CANVASERROR);
      return;
    }

    getWorldToScreenToOut(xf.p, tempVec1);
    tempVec2.setZero();
    // float k_axisScale = 0.4f;

    canvas.setStrokeColor(Color.rgb(1, 0, 0)); // note: violates strokeAlpha
    tempVec2.x = xf.p.x; // + k_axisScale * xf.R.m11; // Transform no longer scales
    tempVec2.y = xf.p.y; // + k_axisScale * xf.R.m12;
    getWorldToScreenToOut(tempVec2, tempVec2);
    canvas.drawLine(tempVec1.x, tempVec1.y, tempVec2.x, tempVec2.y);

    canvas.setStrokeColor(Color.rgb(0, 1, 0)); // note: violates strokeAlpha
    tempVec2.x = xf.p.x; // + k_axisScale * xf.R.m21; // Transform no longer scales
    tempVec2.y = xf.p.y; // + k_axisScale * xf.R.m22;
    getWorldToScreenToOut(tempVec2, tempVec2);
    canvas.drawLine(tempVec1.x, tempVec1.y, tempVec2.x, tempVec2.y);

    canvas.setStrokeColor(Color.argb(strokeAlpha, 1, 0, 0)); // restores strokeAlpha
  }

  public Canvas getCanvas() {
    return canvas;
  }

  public int getFillAlpha() {
    return fillAlpha;
  }

  public int getStrokeAlpha() {
    return strokeAlpha;
  }

  public float getStrokeWidth() {
    return strokeWidth;
  }

  @Override
  public void setCamera(float x, float y, float scale) {
    cameraX = x;
    cameraY = y;
    cameraScale = scale;
    updateCamera();
  }

  public void setCameraScale(float scale) {
    cameraScale = scale;
    updateCamera();
  }

  public void setCameraX(float x) {
    cameraX = x;
    updateCamera();
  }

  public void setCameraY(float y) {
    cameraY = y;
    updateCamera();
  }

  public void setCanvas(CanvasImage image) {
    this.canvas = image.canvas();
    canvas.setStrokeWidth(strokeWidth);
    setStrokeColorFromCache();
    setFillColorFromCache();
  }

  public void setFillAlpha(int fillAlpha) {
    this.fillAlpha = fillAlpha;
    if (canvas != null) {
      setFillColorFromCache();
    }
  }

  public void setFlipY(boolean flip) {
    viewportTransform.setYFlip(flip);
  }

  public void setStrokeAlpha(int strokeAlpha) {
    this.strokeAlpha = strokeAlpha;
    if (canvas != null) {
      setStrokeColorFromCache();
    }
  }

  public void setStrokeWidth(float strokeWidth) {
    this.strokeWidth = strokeWidth;
    if (canvas != null) {
      canvas.setStrokeWidth(strokeWidth);
    }
  }

  /**
   * Sets the fill color from a Color3f
   *
   * @param color color where (r,g,b) = (x,y,z)
   */
  private void setFillColor(Color3f color) {
    if (cacheFillR == color.x && cacheFillG == color.y && cacheFillB == color.z) {
      // no need to re-set the fill color, just use the cached values
    } else {
      cacheFillR = color.x;
      cacheFillG = color.y;
      cacheFillB = color.z;
      setFillColorFromCache();
    }
  }

  /**
   * Sets the fill color from the cached rgb values.
   */
  private void setFillColorFromCache() {
    canvas
        .setFillColor(
            Color.argb(fillAlpha, (int) (255 * cacheFillR), (int) (255 * cacheFillG),
                (int) (255 * cacheFillB)));
  }

  /**
   * Sets the stroke color from a Color3f
   *
   * @param color color where (r,g,b) = (x,y,z)
   */
  private void setStrokeColor(Color3f color) {
    if (cacheStrokeR == color.x && cacheStrokeG == color.y && cacheStrokeB == color.z) {
      // no need to re-set the stroke color, just use the cached values
    } else {
      cacheStrokeR = color.x;
      cacheStrokeG = color.y;
      cacheStrokeB = color.z;
      setStrokeColorFromCache();
    }
  }

  /**
   * Sets the fill color from the cached rgb values.
   */
  private void setStrokeColorFromCache() {
    canvas.setStrokeColor(
        Color.argb(strokeAlpha, (int) (255 * cacheStrokeR), (int) (255 * cacheStrokeG),
            (int) (255 * cacheStrokeB)));
  }

  private void updateCamera() {
    super.setCamera(cameraX, cameraY, cameraScale);
  }

  // @Override
  // public void clear() {
  //   canvas.clear();
  // }
}
