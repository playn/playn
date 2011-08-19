/*
 * Copyright 2011 Google Inc.
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

package flash.display;

import flash.gwt.FlashImport;

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
