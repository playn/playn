/**
 * Copyright 2011 The ForPlay Authors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package forplay.html;

import static forplay.core.ForPlay.*;
import forplay.core.Analytics;
import forplay.core.ForPlay;

public class HtmlAnalytics implements Analytics {

  /**
   * Random value, determined once per game / session, so that multiple events with the same sample
   * rate will be consistently logged -or- not logged in any given session. We don't use Google
   * Analytics' <a href=
   * "http://code.google.com/apis/analytics/docs/gaJS/gaJSApiBasicConfiguration.html#_gat.GA_Tracker_._setSampleRate"
   * >{@literal _setSampleRate}</a> here because that would affect all events in the session.
   */
  private float random;

  public void init() {
    random = HtmlUrlParameters.Analytics.getRandom();
    log().debug("Analytics random = " + random);
  }

  @Override
  public void logEvent(Category category, String action) {
    float sampleRate = category.getSampleRate();
    boolean shouldLog = shouldLogEvent(sampleRate);
    ForPlay.log().debug(
        "Analytics#logEvent(sampleRate=" + sampleRate + ", cagegory=" + category + ", action="
            + action + ") => " + (shouldLog ? "Logging" : "NOT logging"));
    if (shouldLog) {
      logEventImpl(category.getCategory(), action);
    }
  }

  @Override
  public void logEvent(Category category, String action, String label, int value) {
    float sampleRate = category.getSampleRate();
    boolean shouldLog = shouldLogEvent(sampleRate);
    ForPlay.log().debug(
        "Analytics#logEvent(sampleRate=" + sampleRate + ", category=" + category + ", action="
            + action + ", label=" + label + ", value=" + value + ") => "
            + (shouldLog ? "Logging" : "NOT logging"));
    if (shouldLog) {
      logEventImpl(category.getCategory(), action, label, value);
    }
  }

  public native void logEventImpl(String category, String action) /*-{
    $wnd._gaq.push([
        '_trackEvent', category, action
    ]);
  }-*/;

  public native void logEventImpl(String category, String action, String label, int value) /*-{
    $wnd._gaq.push([
        '_trackEvent', category, action, label, value
    ]);
  }-*/;

  private boolean shouldLogEvent(float sampleRate) {
    return random <= sampleRate;
  }
}
