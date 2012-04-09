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

package flash.geom;

import com.google.gwt.core.client.JavaScriptObject;

import flash.gwt.FlashImport;

@FlashImport({"flash.geom.Rectangle"})
final public class Rectangle extends JavaScriptObject {
  protected Rectangle() {}

  public static native Rectangle create(float sx, float sy, float sw, float sh) /*-{
    return new flash.geom.Rectangle(sx, sy, sw, sh);
  }-*/;

  public native int getX() /*-{
    return this.x;
  }-*/;

  public native int getY() /*-{
    return this.y;
  }-*/;

  public native int getWidth() /*-{
    return this.width;
  }-*/;

  public native int getHeight() /*-{
    return this.height;
  }-*/;
}
