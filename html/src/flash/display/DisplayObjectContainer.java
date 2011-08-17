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

import com.google.gwt.core.client.JsArray;

import flash.geom.Point;

/**
 * Implementation of <a href="http://livedocs.adobe.com/flash/9.0/ActionScriptLangRefV3/flash/display/DisplayObjectContainer.html">
 * flash.display.DisplayObjectContainer</a>
 */
public class DisplayObjectContainer extends InteractiveObject {
  protected DisplayObjectContainer() {}

  /**
   *   Determines whether or not the children of the object are mouse enabled.

   * @return
   */
  final public native boolean isMouseChildren() /*-{
    return this.mouseChildren;
  }-*/;
  
  final public native void setMouseChildren(boolean enabled) /*-{
    this.mouseChildren = enabled;;
  }-*/;
  
  final public native void setTabChildren(boolean enabled) /*-{
    this.tabChildren = enabled;
  }-*/;
  
  /**
   * Returns the number of children of this object.
   * @return
   */
  final public native int getNumChildren() /*-{
    return this.numChildren;
  }-*/;

  /**
   * Determines whether the children of the object are tab enabled.
   * @return
   */
  final public native boolean isTabChildren() /*-{
    return this.tabChildren;
  }-*/;
  
  
  /**
   * Adds a child DisplayObject instance to this DisplayObjectContainer instance.
   */
  final public native DisplayObject addChild(DisplayObject child) /*-{
    return this.addChild(child);
  }-*/;
  
  /**
   * Adds a child DisplayObject instance to this DisplayObjectContainer instance.
   */
  final public native DisplayObject addChildAt(DisplayObject child, int index) /*-{
    return this.addChildAt(child, index);
  }-*/;      
  
      
  /**
   * Indicates whether the security restrictions would cause any display objects
   *  to be omitted from the list returned by calling the 
   *  DisplayObjectContainer.getObjectsUnderPoint() method with the specified 
   *  point point.
   * @param point
   * @return
   */
  final public native boolean areInaccessibleObjectsUnderPoint(Point point) /*-{
    return this.areInaccessibleObjectsUnderPoint(point);
  }-*/;
 
      
  /**
   * Determines whether the specified display object is a child of the 
   * DisplayObjectContainer instance or the instance itself.
   * @param child
   * @return
   */
  final public native boolean contains(DisplayObject child) /*-{
    return this.contains(child);
  }-*/;
  
  /**
   * Returns the child display object instance that exists at the specified index.
   * @param index
   * @return
   */
  final public native DisplayObject getChildAt(int index) /*-{
    return this.getChildAt(index);
  }-*/;
  
  /**
   * Returns the child display object that exists with the specified name.
   * @param name
   * @return
   */
  final public native DisplayObject getChildByName(String name) /*-{
    return this.getChildByName(name);
  }-*/;
  
          
  /**
   *  Returns the index position of a child DisplayObject instance.
   * @param child
   * @return
   */
  final public native int getChildIndex(DisplayObject child) /*-{
    return this.getChildIndex(child);
  }-*/;
          
  /**
   * Returns an array of objects that lie under the specified point and are
   * children (or grandchildren, and so on) of this DisplayObjectContainer instance.
   * @param point
   * @return
   */
  final public native JsArray<DisplayObject> getObjectsUnderPoint(Point point) /*-{
     return this.getObjectsUnderPoint(point);
  }-*/;
        
  /**
   * Removes the specified child DisplayObject instance from the child list of the DisplayObjectContainer instance.
   * @param child
   * @return
   */
  final public native DisplayObject removeChild(DisplayObject child) /*-{
     return this.removeChild(child);
  }-*/;
  
  /**
   * Removes a child DisplayObject from the specified index position in the 
   * child list of the DisplayObjectContainer.
   * @param index
   * @return
   */
  final public native DisplayObject removeChildAt(int index) /*-{
    return this.removeChildAt(index);
  }-*/;
  
  /**
   * Changes the position of an existing child in the display object container.
   * @param child
   * @param index
   */
  final public native void setChildIndex(DisplayObject child, int index) /*-{
    this.setChildIndex(child, index);
  }-*/;
       
  /**
   * Swaps the z-order (front-to-back order) of the two specified child objects.
   */
  final public native void swapChildren(DisplayObject child1, DisplayObject child2) /*-{
    this.swapChildren(child1, child2);
  }-*/;
 
  /**
   * Swaps the z-order (front-to-back order) of the child objects at the two
   *  specified index positions in the child list.
   * @param index1
   * @param index2
   */
  final public native void swapChildrenAt(int index1, int index2) /*-{
    this.swapChildrenAt(child1, child2);
  }-*/;
}
