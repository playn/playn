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

import flash.geom.Rectangle;
import flash.gwt.FlashImport;

@FlashImport({"flash.display.Shape", "flash.geom.Matrix"})
final public class Shape extends DisplayObject {
   protected Shape() {}
   

   public static native Shape create(int w, int h) /*-{
      var shape =  new flash.display.Shape();
      shape.width = w;
      shape.height = h;
      return shape;
   }-*/;


   /**
    * Specifies the Graphics object that belongs to this sprite where vector 
    * drawing commands can occur.
    * @return
    */
   public native Graphics getGraphics() /*-{
     return this.graphics;
   }-*/;
   

}
