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

  /** Creates a framebuffer that will render into the supplied texture. The created framebuffer
   * must be left bound at the end of this call. */
  public abstract Object createFramebuffer(Object tex);

  public abstract void deleteFramebuffer(Object fbuf);

  public abstract void bindFramebuffer(Object fbuf, int width, int height);

  public abstract Object createTexture(boolean repeatX, boolean repeatY);

  public abstract Object createTexture(int width, int height, boolean repeatX, boolean repeatY);

  public abstract void destroyTexture(Object tex);

  public void drawTexture(
      Object tex, float texWidth, float texHeight, InternalTransform local,
      float dw, float dh, boolean repeatX, boolean repeatY, float alpha) {
    drawTexture(tex, texWidth, texHeight, local, 0, 0, dw, dh, repeatX, repeatY, alpha);
  }

  public void drawTexture(
      Object tex, float texWidth, float texHeight, InternalTransform local,
      float dx, float dy, float dw, float dh, boolean repeatX, boolean repeatY, float alpha) {
    float sw = repeatX ? dw : texWidth, sh = repeatY ? dh : texHeight;
    drawTexture(tex, texWidth, texHeight, local, dx, dy, dw, dh, 0, 0, sw, sh, alpha);
  }

  public abstract void drawTexture(
      Object tex, float texWidth, float texHeight, InternalTransform local,
      float dx, float dy, float dw, float dh, float sx, float sy, float sw, float sh, float alpha);

  public abstract void fillRect(
      InternalTransform local, float dx, float dy, float dw, float dh,
      float texWidth, float texHeight, Object tex, float alpha);

  public abstract void fillRect(
    InternalTransform local, float dx, float dy, float dw, float dh, int color, float alpha);

  public abstract void fillPoly(InternalTransform local, float[] positions, int color, float alpha);

  public abstract void clear(float r, float g, float b, float a);

  public abstract void flush();

  private void queuePender(Runnable pender) {
    synchronized (penders) {
      penders.add(pender);
    }
  }
}
