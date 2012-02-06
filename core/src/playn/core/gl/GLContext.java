/**
 * Copyright 2010 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package playn.core.gl;

import java.util.ArrayList;

import playn.core.InternalTransform;
import playn.core.PlayN;
import playn.core.StockInternalTransform;

public abstract class GLContext {

  // a queue of pending actions to execute on the GL thread
  private ArrayList<Runnable> penders = new ArrayList<Runnable>();

  // our shaders
  protected GLShader curShader;
  protected GLShader.Texture texShader;
  protected GLShader.Color colorShader;

  /** Creates a framebuffer that will render into the supplied texture. The created framebuffer
   * must be left bound at the end of this call. */
  public abstract Object createFramebuffer(Object tex);

  public abstract void deleteFramebuffer(Object fbuf);

  public abstract void bindFramebuffer(Object fbuf, int width, int height);

  public abstract Object createTexture(boolean repeatX, boolean repeatY);

  public abstract Object createTexture(int width, int height, boolean repeatX, boolean repeatY);

  public abstract void destroyTexture(Object tex);

  /** Starts a series of drawing commands that are clipped to the specified rectangle (in view
   * coordinates, not OpenGL coordinates). Thus must be followed by a call to {@link #endClipped}
   * when the clipped drawing commands are done. */
  public abstract void startClipped(int x, int y, int width, int height);

  /** Ends a series of drawing commands that were clipped per a call to {@link #startClipped}. */
  public abstract void endClipped();

  public abstract void clear(float r, float g, float b, float a);

  /**
   * NOOP except when debugging, checks whether any GL errors have occurred, logging them if so.
   */
  public abstract void checkGLError(String op);

  /**
   * Processes any pending GL actions. Should be called once per frame.
   */
  public void processPending() {
    synchronized (penders) {
      if (!penders.isEmpty()) {
        for (Runnable pender : penders) {
          try {
            pender.run();
          } catch (Throwable t) {
            PlayN.log().warn("Pending GL action choked.", t);
          }
        }
        penders.clear();
      }
    }
  }

  public void queueDestroyTexture(final Object tex) {
    queuePender(new Runnable() {
      public void run() {
        destroyTexture(tex);
      }
    });
  }

  public void queueDeleteFramebuffer(final Object fbuf) {
    queuePender(new Runnable() {
      public void run() {
        deleteFramebuffer(fbuf);
      }
    });
  }

  public InternalTransform createTransform() {
    return new StockInternalTransform();
  }

  public void drawTexture(Object tex, float texWidth, float texHeight, InternalTransform local,
                          float dw, float dh, boolean repeatX, boolean repeatY, float alpha) {
    drawTexture(tex, texWidth, texHeight, local, 0, 0, dw, dh, repeatX, repeatY, alpha);
  }

  public void drawTexture(Object tex, float texWidth, float texHeight, InternalTransform local,
                          float dx, float dy, float dw, float dh,
                          boolean repeatX, boolean repeatY, float alpha) {
    float sw = repeatX ? dw : texWidth, sh = repeatY ? dh : texHeight;
    drawTexture(tex, texWidth, texHeight, local, dx, dy, dw, dh, 0, 0, sw, sh, alpha);
  }

  public void drawTexture(Object tex, float texWidth, float texHeight, InternalTransform local,
                          float dx, float dy, float dw, float dh,
                          float sx, float sy, float sw, float sh, float alpha) {
    texShader.prepare(tex, alpha);
    checkGLError("drawTexture shader prepared");

    sx /= texWidth;
    sw /= texWidth;
    sy /= texHeight;
    sh /= texHeight;

    int idx = texShader.beginPrimitive(4, 4);
    texShader.buildVertex(local, dx, dy, sx, sy);
    texShader.buildVertex(local, dx + dw, dy, sx + sw, sy);
    texShader.buildVertex(local, dx, dy + dh, sx, sy + sh);
    texShader.buildVertex(local, dx + dw, dy + dh, sx + sw, sy + sh);

    texShader.addElement(idx + 0);
    texShader.addElement(idx + 1);
    texShader.addElement(idx + 2);
    texShader.addElement(idx + 3);
    checkGLError("drawTexture end");
  }

  public void fillRect(InternalTransform local, float dx, float dy, float dw, float dh,
                       float texWidth, float texHeight, Object tex, float alpha) {
    texShader.prepare(tex, alpha);
    checkGLError("fillRect tex shader prepared");

    float sx = dx / texWidth, sy = dy / texHeight;
    float sw = dw / texWidth, sh = dh / texHeight;

    int idx = texShader.beginPrimitive(4, 4);
    texShader.buildVertex(local, dx, dy, sx, sy);
    texShader.buildVertex(local, dx + dw, dy, sx + sw, sy);
    texShader.buildVertex(local, dx, dy + dh, sx, sy + sh);
    texShader.buildVertex(local, dx + dw, dy + sy, sx + sw, sy + sh);

    texShader.addElement(idx + 0);
    texShader.addElement(idx + 1);
    texShader.addElement(idx + 2);
    texShader.addElement(idx + 3);
    checkGLError("fillRect tex end");
  }

  public void fillRect(InternalTransform local, float dx, float dy, float dw, float dh,
                       int color, float alpha) {
    colorShader.prepare(color, alpha);
    checkGLError("fillRect color shader prepared");

    int idx = colorShader.beginPrimitive(4, 4);
    colorShader.buildVertex(local, dx, dy);
    colorShader.buildVertex(local, dx + dw, dy);
    colorShader.buildVertex(local, dx, dy + dh);
    colorShader.buildVertex(local, dx + dw, dy + dh);

    colorShader.addElement(idx + 0);
    colorShader.addElement(idx + 1);
    colorShader.addElement(idx + 2);
    colorShader.addElement(idx + 3);
    checkGLError("fillRect color end");
  }

  public void fillPoly(InternalTransform local, float[] positions, int color, float alpha) {
    colorShader.prepare(color, alpha);
    checkGLError("fillPoly shader prepared");

    // FIXME: Rewrite to take advantage of GL_TRIANGLE_STRIP
    int idx = colorShader.beginPrimitive(4, 6); // FIXME: This won't work for non-line polys.
    int points = positions.length / 2;
    for (int i = 0; i < points; ++i) {
      float dx = positions[i * 2];
      float dy = positions[i * 2 + 1];
      colorShader.buildVertex(local, dx, dy);
    }

    int a = idx + 0, b = idx + 1, c = idx + 2;
    int tris = points - 2;
    for (int i = 0; i < tris; i++) {
      colorShader.addElement(a);
      colorShader.addElement(b);
      colorShader.addElement(c);
      a = c;
      b = a + 1;
      c = (i == tris - 2) ? idx : b + 1;
    }
    checkGLError("fillPoly end");
  }

  public void flush() {
    if (curShader != null) {
      checkGLError("flush()");
      curShader.flush();
      curShader = null;
    }
  }

  /**
   * Makes the supplied shader the current shader, flushing any previous shader.
   */
  public boolean useShader(GLShader shader) {
    if (curShader == shader)
      return false;
    checkGLError("useShader");
    flush();
    curShader = shader;
    return true;
  }

  private void queuePender(Runnable pender) {
    synchronized (penders) {
      penders.add(pender);
    }
  }
}
