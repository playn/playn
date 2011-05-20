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
  private float random = ForPlay.random();

  public HtmlAnalytics() {
    log().debug("Analytics random = " + random);
  }

  @Override
  public void logEvent(String event, float sampleRate) {
    boolean shouldLog = shouldLogEvent(sampleRate);
    log().debug(
        "Analytics#logEvent(" + event + ", " + sampleRate + ") => "
            + (shouldLog ? "Logging" : "NOT logging"));
    if (shouldLog) {
      logEventImpl(event);
    }
  }

  private boolean shouldLogEvent(float sampleRate) {
    return random <= sampleRate;
  }

  public native void logEventImpl(String event) /*-{
    $wnd._gaq.push([
        '_trackPageview', event
    ]);
  }-*/;
}
