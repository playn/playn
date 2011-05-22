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

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;

/**
 * Abstract input class for handling events on the page and root element.
 * <p>
 * This class supports the case where we want to capture some events on just a
 * specific element, but other events anywhere on the page.
 * An example of this is mouse events, where we only want to catch mouse down
 * events on a specific element, but we want mouse up events anywhere.
 */
abstract class HtmlInput {
  /**
   * Capture events that occur anywhere on the page.
   * <p>
   * Note that event values will be relative to the page (not the
   * rootElement) {@see #getRelativeX(NativeEvent, Element)} and {@see
   * #getRelativeY(NativeEvent, Element)}.
   */
  protected void capturePageEvent(String eventName, final EventHandler handler) {
    HtmlPlatform.captureEvent(eventName, new EventHandler() {
      @Override
      public void handleEvent(NativeEvent evt) {
        handler.handleEvent(evt);
      }
    });
  }

  /**
   * Capture events that occur on the target element only.
   */
  protected void captureEvent(final Element elem, String eventName, final EventHandler handler) {
    // register regular event handler on the element
    HtmlPlatform.captureEvent(elem, eventName, handler);
  }

  /**
   * Gets the event's x-position relative to a given element.
   * 
   * @param e native event
   * @param target the element whose coordinate system is to be used
   * @return the relative x-position
   */
  protected static float getRelativeX(NativeEvent e, Element target) {
    return e.getClientX() - target.getAbsoluteLeft() + target.getScrollLeft()
        + target.getOwnerDocument().getScrollLeft();
  }

  /**
   * Gets the event's y-position relative to a given element.
   * 
   * @param e native event
   * @param target the element whose coordinate system is to be used
   * @return the relative y-position
   */
  protected static float getRelativeY(NativeEvent e, Element target) {
    return e.getClientY() - target.getAbsoluteTop() + target.getScrollTop()
        + target.getOwnerDocument().getScrollTop();
  }
}
