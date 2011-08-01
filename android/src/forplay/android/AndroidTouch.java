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
package forplay.android;

import forplay.core.Touch;

public class AndroidTouch implements Touch {
  private Listener listener;

  public void onTouchStart(Event[] touches) {
    if (listener != null)
      listener.onTouchStart(touches);
  }

  public void onTouchMove(Event[] touches) {
    if (listener != null)
      listener.onTouchMove(touches);
  }

  public void onTouchEnd(Event[] touches) {
    if (listener != null)
      listener.onTouchEnd(touches);
  }

  @Override
  public void setListener(Listener listener) {
    this.listener = listener;
  }
}
