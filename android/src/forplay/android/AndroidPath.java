/**
 * Copyright 2010 The ForPlay Authors
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
package forplay.android;

import android.graphics.Path;

class AndroidPath implements forplay.core.Path {

  Path path;

  AndroidPath() {
    path = new Path();
  }

  @Override
  public void arcTo(float radius, float x, float y) {
    // TODO: convert this.
    path.lineTo(x, y);
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
