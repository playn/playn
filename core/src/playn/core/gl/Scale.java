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
package playn.core.gl;

import java.util.ArrayList;
import java.util.List;

import pythagoras.f.MathUtil;

import playn.core.Asserts;

/**
 * Encapsulates a scale factor, provides useful utility methods.
 */
public class Scale {

  /** Used by {@link #getScaledResources}. */
  public static class ScaledResource {
    /** The scale factor for this resource. */
    public final Scale scale;

    /**
     * The path to the resource, including any scale factor annotation. If the scale is one, the
     * image path is unadjusted. If the scale is greater than one, the scale is tacked onto the
     * image path (before the extension). The scale factor will be converted to an integer per the
     * following examples:
     * <ul>
     * <li> Scale factor 2: {@code foo.png} becomes {@code foo@2x.png}</li>
     * <li> Scale factor 4: {@code foo.png} becomes {@code foo@4x.png}</li>
     * <li> Scale factor 1.5: {@code foo.png} becomes {@code foo@15x.png}</li>
     * <li> Scale factor 1.25: {@code foo.png} becomes {@code foo@13x.png}</li>
     * </ul>
     */
    public final String path;

    public ScaledResource(Scale scale, String path) {
      this.scale = scale;
      this.path = path;
    }
  }

  /** An unscaled scale factor singleton. */
  public static final Scale ONE = new Scale(1);

  /** The scale factor for HiDPI mode, or 1 if HDPI mode is not enabled. */
  public final float factor;

  public Scale (float factor) {
    Asserts.checkArgument(factor >= 1, "Scale factor cannot be less than one.");
    this.factor = factor;
  }

  /** Returns the supplied length scaled by our scale factor. */
  public float scaled(float length) {
    return factor*length;
  }

  /** Returns the supplied length scaled by our scale factor and rounded up. */
  public int scaledCeil(float length) {
    return MathUtil.iceil(scaled(length));
  }

  /** Returns the supplied length scaled by our scale factor and rounded down. */
  public int scaledFloor(float length) {
    return MathUtil.ifloor(scaled(length));
  }

  /** Returns the supplied length inverse scaled by our scale factor. */
  public float invScaled(float length) {
    return length/factor;
  }

  /** Returns the supplied length inverse scaled by our scale factor and rounded down. */
  public int invScaledFloor(float length) {
    return MathUtil.ifloor(invScaled(length));
  }

  /** Returns the supplied length inverse scaled by our scale factor and rounded up. */
  public int invScaledCeil(float length) {
    return MathUtil.iceil(invScaled(length));
  }

  /**
   * Returns an ordered series of scaled resources to try when loading an asset. The highest
   * resolution will be tried first, then half that resolution and so forth down to a normal
   * resolution image. In general this is simply {@code 2, 1}, but on a Retina iPad, it could be
   * {@code 4, 2, 1}.
   */
  public List<ScaledResource> getScaledResources(String path) {
    List<ScaledResource> rsrcs = new ArrayList<ScaledResource>();
    rsrcs.add(new ScaledResource(this, computePath(path, factor)));
    for (float rscale = factor/2; rscale > 1; rscale /= 2) {
      rsrcs.add(new ScaledResource(new Scale(rscale), computePath(path, rscale)));
    }
    rsrcs.add(new ScaledResource(ONE, path));
    return rsrcs;
  }

  @Override
  public String toString() {
    return "x" + factor;
  }

  private String computePath(String path, float scale) {
    int scaleFactor = (int)(scale * 10);
    if (scaleFactor % 10 == 0)
      scaleFactor /= 10;
    int didx = path.lastIndexOf(".");
    if (didx == -1) {
      return path; // no extension!?
    } else {
      return path.substring(0, didx) + "@" + scaleFactor + "x" + path.substring(didx);
    }
  }
}
