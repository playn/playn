/**
 * Copyright 2011 The PlayN Authors
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
 * Defines some shared events.
 */
public class Events {

  /** The base for all input events. */
  public interface Input {
    /**
     * Return whether the default action normally taken by the platform will be prevented.
     */
    boolean getPreventDefault();

    /**
     * Set whether the default action normally taken by the platform as a result of the event should
     * be performed. By default, the default action is not prevented.
     * <p>
     * For example, pressing the down key in a browser typically scrolls the window. Calling
     * {@code setPreventDefault(true)} prevents this action.
     * <p>
     * Note: this must be set from inside the event handler callback (e.g., onKeyUp()). If it is
     * called after the callback has returned, setPreventDefault will have no effect.
     */
    void setPreventDefault(boolean preventDefault);

    /**
     * The time at which this event was generated, in milliseconds. This time's magnitude is not
     * portable (i.e. may not be the same across backends), clients must interpret it as only a
     * monotonically increasing value.
     */
    double time();

    // TODO(mdb): a mechanism to determine which modifier keys are pressed, if any

    abstract class Impl implements Input {
      private final double time;
      private boolean preventDefault; // default false

      @Override
      public double time() {
        return time;
      }

      protected Impl(double time) {
        this.time = time;
      }

      @Override
      public void setPreventDefault(boolean preventDefault) {
        this.preventDefault = preventDefault;
      }

      @Override
      public boolean getPreventDefault() {
        return preventDefault;
      }

      protected String name() {
          return "Events.Input";
      }

      @Override
      public String toString() {
        StringBuilder builder = new StringBuilder(name()).append('[');
        addFields(builder);
        return builder.append(']').toString();
      }

      protected void addFields(StringBuilder builder) {
        builder.append("time=").append(time).append(", preventDefault=").append(preventDefault);
      }

    }
  }

  /** The base for all events with pointer position. */
  public interface Position extends Input {
    /**
     * The screen x-coordinate associated with this event.
     */
    float x();

    /**
     * The screen y-coordinate associated with this event.
     */
    float y();

    /**
     * The x-coordinate associated with this event transformed into the receiving layer's
     * coordinate system. See {@link Pointer#addListener}, etc.
     */
    float localX();

    /**
     * The y-coordinate associated with this event transformed into the receiving layer's
     * coordinate system. See {@link Pointer#addListener}, etc.
     */
    float localY();

    abstract class Impl extends Input.Impl implements Position {
      private final float x, y, localX, localY;

      @Override
      public float x() {
        return x;
      }

      @Override
      public float y() {
        return y;
      }

      @Override
      public float localX() {
        return localX;
      }

      @Override
      public float localY() {
        return localY;
      }

      protected Impl(double time, float x, float y) {
        this(time, x, y, x, y);
      }

      protected Impl(double time, float x, float y, float localX, float localY) {
        super(time);
        this.x = x;
        this.y = y;
        this.localX = localX;
        this.localY = localY;
      }

      @Override
      protected String name() {
        return "Events.Position";
      }

      @Override
      protected void addFields(StringBuilder builder) {
        super.addFields(builder);
        builder.append(", x=").append(x).append(", y=").append(y);
      }
    }
  }
}
