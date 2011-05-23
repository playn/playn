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

public interface Analytics {
  public static class Category {
    private final float sampleRate;
    private final String category;

    /**
     * @param sampleRate likelihood that this event should be logged during this
     *          game session, range {@literal [0.0 - 1.0)}
     * @param category the general event category, e.g. {@literal "Videos"}
     */
    public Category(float sampleRate, String category) {
      this.sampleRate = sampleRate;
      this.category = category;
    }

    @Override
    public String toString() {
      return getCategory() + "[rate=" + getSampleRate() + "]";
    }

    public float getSampleRate() {
      return sampleRate;
    }

    public String getCategory() {
      return category;
    }
  }

  public abstract class AnalyticsImpl implements Analytics {
    @Override
    public void logEvent(Category category, String action) {
      ForPlay.log().debug("Analytics#logEvent(category=" + category + ", action=" + action + ")");
    }

    @Override
    public void logEvent(Category category, String action, String label, int value) {
      ForPlay.log().debug(
          "Analytics#logEvent(category=" + category + ", action=" + action + ", label=" + label
              + ", value=" + value + ")");
    }
  }

  /**
   * Log an event with a given sampleRate, range {@literal [0.0 - 1.0)}.
   * 
   * @param category the analytics {@link Category}
   * @param action the action for the event, e.g. {@literal "Play"}
   */
  void logEvent(Category category, String action);

  /**
   * Log an event with a given sampleRate, range {@literal [0.0 - 1.0)}.
   * 
   * @param category the analytics {@link Category}
   * @param action the action for the event, e.g. {@literal "Play"}
   * @param label descriptor for the event
   * @param value value associated with the event. You can see your event values
   *          in the Overview, Categories, and Actions reports, where they are
   *          listed by event or aggregated across events, depending upon your
   *          report view
   */
  void logEvent(Category category, String action, String label, int value);
}
