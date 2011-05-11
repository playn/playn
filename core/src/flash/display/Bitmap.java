// Copyright 2011 Google Inc. All Rights Reserved.

package flash.display;

import com.google.gwt.core.client.JavaScriptObject;

import flash.gwt.FlashImport;

/**
 * @author cromwellian@google.com (Your Name Here)
 *
 */
@FlashImport({"flash.display.Bitmap"})
final public class Bitmap extends DisplayObject {
  protected Bitmap() {}
  public static native Bitmap create(BitmapData data) /*-{
    return new flash.display.Bitmap(data);
  }-*/;
  /**
   * @param bitmapData
   */
  public native void setBitmapData(BitmapData bitmapData) /*-{
    this.bitmapData = bitmapData;
  }-*/;
}
