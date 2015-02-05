/**
 * Copyright 2010-2015 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playn.robovm;

import org.robovm.apple.uikit.UIInterfaceOrientation;

/** Events emitted when the device is rotated. */
public abstract class RoboOrientEvent {

  /** An event emitted when the device is about to rotate. */
  public static class WillRotate extends RoboOrientEvent {
    /** The orientation to which we're rotation. */
    public final UIInterfaceOrientation toOrient;
    /** The duration of the rotation animation, in seconds. */
    public final double duration;

    public WillRotate (UIInterfaceOrientation toOrient, double duration) {
      this.toOrient = toOrient;
      this.duration = duration;
    }
  }

  /** An event emitted when the device has just finished rotating. */
  public static class DidRotate extends RoboOrientEvent {
    /** The orientation from which we just rotated. */
    public final UIInterfaceOrientation fromOrient;

    public DidRotate (UIInterfaceOrientation fromOrient) {
      this.fromOrient = fromOrient;
    }
  }
}
