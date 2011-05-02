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

import java.awt.Color;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;

import forplay.core.Gradient;

class JavaGradient implements Gradient {

  static JavaGradient createLinear(float x0, float y0, float x1, float y1, float[] positions, int[] colors) {
    Point2D.Float start = new Point2D.Float(x0, y0);
    Point2D.Float end = new Point2D.Float(x1, y1);
    Color[] javaColors = convertColors(colors);
    LinearGradientPaint p = new LinearGradientPaint(start, end, positions, javaColors);
    return new JavaGradient(p);
  }

  static JavaGradient createRadial(float x, float y, float r, float[] positions, int[] colors) {
    Point2D.Float center = new Point2D.Float(x, y);
    RadialGradientPaint p = new RadialGradientPaint(center, r, positions, convertColors(colors));
    return new JavaGradient(p);
  }

  private static Color[] convertColors(int[] colors) {
    Color[] javaColors = new Color[colors.length];
    for (int i = 0; i < colors.length; ++i) {
      javaColors[i] = new Color(colors[i], true);
    }
    return javaColors;
  }

  Paint paint;

  private JavaGradient(Paint paint) {
    this.paint = paint;
  }
}
