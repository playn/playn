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
package playn.core;

import pythagoras.f.MathUtil;

/**
 * Tinting related utility methods.
 */
public class Tint {

  /** A tint that does not change the underlying color. */
  public static final int NOOP_TINT = 0xFFFFFFFF;

  /** Returns the combination of {@code curTint} and {@code tint}. */
  public static int combine(int curTint, int tint) {
    int newA = ((((curTint >> 24) & 0xFF) * (((tint >> 24) & 0xFF)+1)) & 0xFF00) << 16;
    if ((tint & 0xFFFFFF) == 0xFFFFFF) { // fast path to just combine alpha
      return newA | (curTint & 0xFFFFFF);
    }

    // otherwise combine all the channels (beware the bit mask-and-shiftery!)
    int newR = ((((curTint >> 16) & 0xFF) * (((tint >> 16) & 0xFF)+1)) & 0xFF00) << 8;
    int newG =  (((curTint >>  8) & 0xFF) * (((tint >>  8) & 0xFF)+1)) & 0xFF00;
    int newB =  (((curTint        & 0xFF) * ((tint         & 0xFF)+1)) >> 8) & 0xFF;
    return newA | newR | newG | newB;
  }

  /** Sets the alpha component of {@code tint} to {@code alpha}.
    * @return the new tint. */
  public static int setAlpha(int tint, float alpha) {
    int ialpha = (int)(0xFF * MathUtil.clamp(alpha, 0, 1));
    return (ialpha << 24) | (tint & 0xFFFFFF);
  }

  /** Returns the alpha component of {@code tint} as a float between {@code [0, 1]}. */
  public static float getAlpha(int tint) {
    return ((tint >> 24) & 0xFF) / 255f;
  }
}
