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
package playn.core;

/**
 * A gradient fill pattern created by {@link Canvas#createGradient}.
 */
public abstract class Gradient {

  /** Used to create gradients. */
  public static abstract class Config {
    public final int[] colors;
    public final float[] positions;

    protected Config (int[] colors, float[] positions) {
      this.colors = colors;
      this.positions = positions;
    }
  }

  /** Creates a linear gradient fill pattern. {@code (x0, y0)} and {@code (x1, y1)} specify the
    * start and end positions, while {@code (colors, positions)} specifies the color stops. */
  public static class Linear extends Config {
    public final float x0, y0, x1, y1;

    public Linear (float x0, float y0, float x1, float y1, int[] colors, float[] positions) {
      super(colors, positions);
      this.x0 = x0;
      this.y0 = y0;
      this.x1 = x1;
      this.y1 = y1;
    }
  }

  /** Creates a radial gradient fill pattern. {@code (x, y, r)} specifies the circle covered by
    * this gradient, while {@code (colors, positions)} specifies the list of color stops. */
  public static class Radial extends Config {
    public final float x, y, r;

    public Radial (float x, float y, float r, int[] colors, float[] positions) {
      super(colors, positions);
      this.x = x;
      this.y = y;
      this.r = r;
    }
  }
}
