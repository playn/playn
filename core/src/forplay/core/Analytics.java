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

package forplay.core;

public interface Analytics {

  /**
   * Log an event with a given sampleRate, range {@literal [0.0 - 1.0)}.
   * 
   * @param eventText the text of the event to send
   * @param sampleRate likelihood that this event should be logged during this game session , range
   *          {@literal [0.0 - 1.0)}
   */
  void logEvent(String eventText, float sampleRate);
}
