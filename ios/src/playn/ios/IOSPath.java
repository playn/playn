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

import cli.MonoTouch.CoreGraphics.CGPath;
import cli.System.Drawing.PointF;

import playn.core.Path;

public class IOSPath implements Path
{
  CGPath cgPath = new CGPath();

  @Override
  public void reset() {
    cgPath.Dispose();
    cgPath = new CGPath();
  }

  @Override
  public void close() {
    cgPath.CloseSubpath();
  }

  @Override
  public void moveTo(float x, float y) {
    cgPath.MoveToPoint(x, y);
  }

  @Override
  public void lineTo(float x, float y) {
    cgPath.AddLineToPoint(x, y);
  }

  @Override
  public void quadraticCurveTo(float cpx, float cpy, float x, float y) {
    cgPath.AddQuadCurveToPoint(cpx, cpy, x, y);
  }

  @Override
  public void arcTo(float radius, float x, float y) {
    PointF cur = cgPath.get_CurrentPoint();
    cgPath.AddArcToPoint(cur.get_X(), cur.get_Y(), x, y, radius);
  }

  protected void finalize() {
    cgPath.Dispose(); // meh
  }
}
