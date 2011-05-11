
/*
 * Copyright (C) 2008 Archie L. Cobbs <archie@dellroad.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Id: BitmapData.java 3 2008-02-21 23:49:03Z archie.cobbs $
 */

package flash.display;

import com.google.gwt.core.client.JavaScriptObject;

import flash.gwt.FlashImport;


/**
 * @see <a href="http://livedocs.adobe.com/flash/8/main/00001942.html#wp137587">ActionScript 2.0 Language Reference</a>
 */
@FlashImport({"flash.display.BitmapData"})
final public class BitmapData extends JavaScriptObject {

    protected BitmapData() {}
    
    public static BitmapData create(int width, int height) {
        return create(width, height, true, 0xFFFFFFFF);
    }

    public static BitmapData create(int width, int height, boolean transparent,
      int fillColor) {
        return createBitmapData(width, height, transparent, fillColor);
    }

    public native void draw(Object source) /*-{
      this.draw(source);
    }-*/;
  
    public native int getWidth() /*-{
        return this.width;
    }-*/;

    public native int getHeight() /*-{
        return this.height;
    }-*/;

    public native boolean isTransparent() /*-{
        return this.transparent;
    }-*/;

    public native int getPixel(int x, int y) /*-{
        return this.getPixel(x, y);
    }-*/;
    public native void setPixel(int x, int y, int color) /*-{
        return this.setPixel(x, y, color);
    }-*/;

    public native int getPixel32(int x, int y) /*-{
        return this.getPixel32(x, y);
    }-*/;
    public native void setPixel32(int x, int y, int color) /*-{
        return this.setPixel32(x, y, color);
    }-*/;

    

    public native void dispose() /*-{
        this.dispose();
    }-*/;

    private native JavaScriptObject cloneJSO() /*-{
        return this.clone();
    }-*/;

    public static BitmapData loadBitmap(String id) {
        return loadBitmapJSO(id);
    }

    private static native BitmapData loadBitmapJSO(String id) /*-{
        return flash.display.BitmapData.loadBitmap(id);
    }-*/;

    private static native BitmapData createBitmapData(int width,
      int height, boolean transparent, int fillColor) /*-{
        return new flash.display.BitmapData(width, height,
          transparent, fillColor);
    }-*/;

}

