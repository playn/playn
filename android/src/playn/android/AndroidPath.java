/**
 * Copyright 2010 The PlayN Authors
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
package playn.android;

import android.graphics.Path;

class AndroidPath implements playn.core.Path {

  Path path;

  AndroidPath() {
    path = new Path();
  }

  @Override
  public void bezierTo(float c1x, float c1y, float c2x, float c2y, float x, float y) {
    path.cubicTo(c1x, c1y, c2x, c2y, x, y);
  }

  @Override
  public void close() {
    path.close();
  }

  @Override
  public void lineTo(float x, float y) {
    path.lineTo(x, y);
  }

  @Override
  public void moveTo(float x, float y) {
    path.moveTo(x, y);
  }

  @Override
  public void quadraticCurveTo(float cpx, float cpy, float x, float y) {
    path.quadTo(cpx, cpy, x, y);
  }

  @Override
  public void reset() {
    path.reset();
  }
}
