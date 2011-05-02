/**
 * Copyright 2010 The ForPlay Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package forplay.java;

import forplay.core.Path;

import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;

class JavaPath implements Path {

  Path2D path = new GeneralPath();

  @Override
  public void reset() {
    path.reset();
  }

  @Override
  public void close() {
    path.closePath();
  }

  @Override
  public void moveTo(float x, float y) {
    path.moveTo(x, y);
  }

  @Override
  public void lineTo(float x, float y) {
    path.lineTo(x, y);
  }

  @Override
  public void quadraticCurveTo(float cpx, float cpy, float x, float y) {
    path.quadTo(cpx, cpy, x, y);
  }

  @Override
  public void arcTo(float radius, float x, float y) {
    // TODO: convert this form to one that Arc2D accepts.
    path.lineTo(x, y);
  }
}
