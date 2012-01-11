/**
 * Copyright 2012 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package playn.ios;

import playn.core.InternalTransform;
import playn.core.gl.GLContext;

class IOSGLContext extends GLContext
{
  public int viewWidth, viewHeight;

  @Override
  public Object createFramebuffer(Object tex) {
    throw new RuntimeException("TODO");
  }

  @Override
  public void deleteFramebuffer(Object fbuf) {
    throw new RuntimeException("TODO");
  }

  @Override
  public void bindFramebuffer(Object fbuf, int width, int height) {
    throw new RuntimeException("TODO");
  }

  @Override
  public Object createTexture(boolean repeatX, boolean repeatY) {
    throw new RuntimeException("TODO");
  }

  @Override
  public Object createTexture(int width, int height, boolean repeatX, boolean repeatY) {
    throw new RuntimeException("TODO");
  }

  @Override
  public void destroyTexture(Object tex) {
    throw new RuntimeException("TODO");
  }

  @Override
  public void drawTexture(Object tex, float texWidth, float texHeight, InternalTransform local,
                          float dx, float dy, float dw, float dh,
                          float sx, float sy, float sw, float sh, float alpha) {
    throw new RuntimeException("TODO");
  }

  @Override
  public void fillRect(InternalTransform local, float dx, float dy, float dw, float dh,
                       float texWidth, float texHeight, Object tex, float alpha) {
    throw new RuntimeException("TODO");
  }

  @Override
  public void fillRect(InternalTransform local, float dx, float dy, float dw, float dh,
                       int color, float alpha) {
    throw new RuntimeException("TODO");
  }

  @Override
  public void fillPoly(InternalTransform local, float[] positions, int color, float alpha) {
    throw new RuntimeException("TODO");
  }

  @Override
  public void clear(float r, float g, float b, float a) {
    throw new RuntimeException("TODO");
  }

  @Override
  public void flush() {
    throw new RuntimeException("TODO");
  }
}
