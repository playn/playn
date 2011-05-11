// Copyright 2011 Google Inc. All Rights Reserved.

package flash.events;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * @author cromwellian@google.com (Your Name Here)
 *
 */
final public class EventType extends JavaScriptObject {
  protected EventType() {}
  public static native EventType make(String type) /*-{
    return type;
  }-*/;
  
  public native String value() /*-{
    return this;
  }-*/;
  
}
