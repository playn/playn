/**
 * Copyright 2011 The ForPlay Authors
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
package forplay.core;

/**
 * Defines some shared events.
 */
public class Events {

  /** The base for all input events. */
  public interface Input {
    /**
     * The time at which this event was generated, in milliseconds. This time's magnitude is not
     * portable (i.e. may not be the same across backends), clients must interpret it as only a
     * monotonically increasing value.
     */
    double time();

    // TODO(mdb): a mechanism to determine which modifier keys are pressed, if any

    abstract class Impl implements Input {
      private final double time;

      @Override
      public double time() {
        return time;
      }

      protected Impl(double time) {
        this.time = time;
      }
    }
  }

  /** The base for all events with pointer position. */
  public interface Position extends Input {
    /**
     * The x-coordinate associated with this event.
     */
    float x();

    /**
     * The y-coordinate associated with this event.
     */
    float y();

    abstract class Impl extends Input.Impl implements Position {
      private final float x, y;

      @Override
      public float x() {
        return x;
      }

      @Override
      public float y() {
        return y;
      }

      protected Impl(double time, float x, float y) {
        super(time);
        this.x = x;
        this.y = y;
      }
    }
  }
}
