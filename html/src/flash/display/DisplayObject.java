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

import flash.events.EventType;
import flash.geom.Point;
import flash.geom.Transform;
import flash.geom.Rectangle;
import flash.filters.BitmapFilter;
import flash.events.EventDispatcher;

/**
 * Implementation of 
 * <a href="http://livedocs.adobe.com/flash/9.0/ActionScriptLangRefV3/flash/display/DisplayObject.html">
 * flash.display.DisplayObject</a>
 */
public class DisplayObject extends EventDispatcher {
  protected DisplayObject() {}
  /**
   * Indicates the alpha transparency value of the object specified.
   */
  final public native double getAlpha() /*-{ return this.alpha; }-*/;
  
  final public native void setAlpha(double alpha) /*-{ this.alpha = alpha; }-*/;

  final private native String getBlendMode0() /*-{
    return this.blendMode;
  }-*/;

  final private native void setBlendMode0(String blendMode) /*-{
    this.blendMode = blendMode;
  }-*/;
  
  /**
   * A value from the BlendMode class that specifies which blend mode to use.
   * @return
   */
  final public BlendMode getBlendMode() {
    return BlendMode.valueOfNative(getBlendMode0());
  }
  
  
  final public void setBlendMode(BlendMode blendMode) {
    setBlendMode0(blendMode.nativeEnum());
  }
  
  /**
   * If set to true, Flash Player or Adobe AIR caches an internal bitmap representation of the display object.
   * @return
   */
  final public native boolean getCacheAsBitmap() /*-{
    return this.cacheAsBitmap;
  }-*/;
  
  final public native void setCacheAsBitmap(boolean cache) /*-{
    this.cacheAsBitmap = cache;
  }-*/;
  
  /**
   * An indexed array that contains each filter object currently associated with the display object.
   */
  final public native JsArray<BitmapFilter> getFilters() /*-{
    return this.filters;
  }-*/;
  
  final public native void setFilters(JsArray<BitmapFilter> filters) /*-{
    this.filters = filters;
  }-*/;
  
  /**
   * Indicates the height of the display object, in pixels.
   * @return
   */
  final public native int getHeight() /*-{
     return this.height;
  }-*/;
  
  final public native void setHeight(int height) /*-{
    this.height = height;
  }-*/;
    
  /**
   * Returns a LoaderInfo object containing information about loading the file
   *   to which this display object belongs.
   * @return
   */
  final public native LoaderInfo getLoaderInfo() /*-{
     return this.loaderInfo;
  }-*/;
  
  /**
   * The calling display object is masked by the specified mask object.
   */
  final public native DisplayObject getMask() /*-{
     return this.mask;
  }-*/;
  
  final public native void setMask(DisplayObject mask) /*-{
    this.mask = mask;;
  }-*/;
  
  /**
   *   [read-only] Indicates the x coordinate of the mouse position, in pixels.
   * @return
   */
  final public native double getMouseX() /*-{
    return this.mouseX;
  }-*/;
  
  /**
   * Indicates the y coordinate of the mouse position, in pixels.
   * @return
   */
  final public native double getMouseY() /*-{
    return this.mouseY;
  }-*/;
 
  /**
   * Indicates the instance name of the DisplayObject.
   */
  final public native String getName() /*-{
    return this.name;
  }-*/;

  final public native void setName(String name) /*-{
    this.name = name;;
}-*/;
  
//  DisplayObject
//          opaqueBackground : Object
//  Specifies whether the display object is opaque with a certain background color.
  /**
   * Indicates the DisplayObjectContainer object that contains this display 
   * object.
   */
  final public native DisplayObjectContainer getParent() /*-{
    return this.parent;
  }-*/;
 
  /**
   * For a display object in a loaded SWF file, the root property is the 
   * top-most display object in the portion of the display list's tree structure
   * e represented by that SWF file.
   * @return
   */
  final public native DisplayObject getRoot() /*-{
    return this.root;
  }-*/;
  
  /**
   * Indicates the rotation of the DisplayObject instance, in degrees, from its
   * original orientation.
   * @return
   */
  final public native double getRotation() /*-{
    return this.rotation;
  }-*/;
  
  final public native void setRotation(double rotation) /*-{
    this.rotation = rotation;
   }-*/;
  
  /**
   * The current scaling grid that is in effect.
   * @return
   */
  final public native Rectangle getScale9Grid() /*-{
    return this.scale9Grid;
  }-*/;
  
  final public native void setScale9Grid(Rectangle scale9Grid) /*-{
    this.scale9Grid = scale9Grid;
  }-*/;
 
  /**
   * Indicates the horizontal scale (percentage) of the object as applied 
   * from the registration point.
   * @return
   */
  final public native double getScaleX() /*-{
    return this.scaleX;
  }-*/;
  
  
  final public native void setScaleX(double scaleX) /*-{
    this.scaleX = scaleX;
  }-*/;
  
  /**
   * Indicates the vertical scale (percentage) of an object as applied from the
   * registration point of the object.
   * @return
   */
  final public native double getScaleY() /*-{
    return this.scaleY;
  }-*/;


  final public native void setScaleY(double scaleY) /*-{
    this.scaleY= scaleY;
  }-*/;

  /**
   * The scroll rectangle bounds of the display object.
   * @return
   */
  final public native Rectangle getScrollRect() /*-{
    return this.scrollRect;
  }-*/;

  final public native void setScrollRect(Rectangle scrollRect) /*-{
    this.scrollRect = scrollRect;
  }-*/;

  /**
   * The Stage of the display object.
   * @return
   */
  final public native Stage getStage() /*-{
    return this.stage;
  }-*/;
  
  /**
   * An object with properties pertaining to a display object's matrix, color 
   * transform, and pixel bounds.
   * @return
   */
  final public native Transform getTransform() /*-{
    return this.transform;
  }-*/;
  
  final public native void setTransform(Transform transform) /*-{
    this.transform = transform;
  }-*/;
  
  /**
   * @param m00
   * @param m01
   * @param m10
   * @param m11
   * @param ty 
   * @param tx 
   */
  final public native void setTransform(float m00, float m01, float m10, float m11, float tx, float ty) /*-{
    this.transform.matrix = new flash.geom.Matrix(m00, m01, m10, m11, tx, ty);    
  }-*/;
  
  /**
   * Whether or not the display object is visible.
   * @return
   */
  final public native boolean visible() /*-{
    return this.visible;
  }-*/;
  
  
  final public native void setVisible(boolean visible) /*-{
    this.visible = visible;
  }-*/;
  
  /**
   * Indicates the width of the display object, in pixels.
   * @return
   */
  final public native int getWidth() /*-{
    return this.width;
  }-*/;
  
  /**
   * 
   * @param width
   */
  final public native void setWidth(int width) /*-{
    this.width = width;;
  }-*/;
  
  /**
   * Indicates the x coordinate of the DisplayObject instance relative to the 
   * local coordinates of the parent DisplayObjectContainer.
   * @return
   */
  final public native double getX() /*-{
    return this.x;
  }-*/;
  
  /**
   * Indicates the y coordinate of the DisplayObject instance relative to the 
   * local coordinates of the parent DisplayObjectContainer.
   * @return
   */
  final public native double getY() /*-{
    return this.x;
  }-*/;
  
  final public native void setX(double x) /*-{
    this.x = x;
  }-*/;
  
  final public native void setY(double y) /*-{
    this.y = y;
  }-*/;
  
  
  /**
   * Returns a rectangle that defines the area of the display object relative
   * to the coordinate system of the targetCoordinateSpace object.
   */
  final public native Rectangle getBounds(DisplayObject targetCoordinateSpace) /*-{
    return this.getBounds(targetCoordinatespace);
  }-*/;
  
  /**
   * Returns a rectangle that defines the boundary of the display object, based
   * on the coordinate system defined by the targetCoordinateSpace parameter, excluding any strokes on shapes.
   * @param targetCoordinateSpace
   * @return
   */
  final public native Rectangle getRect(DisplayObject targetCoordinateSpace) /*-{
    return this.getRect(targetCoordinatespace);
  }-*/;
  
 
  /**
   * Converts the point object from the Stage (global) coordinates to the 
   * display object's (local) coordinates.
   * @param point
   * @return
   */
  final public native Point globalToLocal(Point point) /*-{
    return this.globalToLocal(point);
  }-*/;
  
  /**
   * Evaluates the display object to see if it overlaps or intersects with 
   * the obj display object.
   * @param obj
   * @return
   */
  final public native boolean hitTestObject(DisplayObject obj) /*-{
    return this.hitTestObject(obj);
  }-*/;
  
  /**
   * Evaluates the display object to see if it overlaps or intersects with
   *  the point specified by the x and y parameters.
   * @param x
   * @param y
   * @param shapeFlag
   * @return
   */
  final public native boolean hitTestPoint(double x, double y, boolean shapeFlag) /*-{
     return this.hitTestPoint(x, y, shapeFlag);
  }-*/;
  
  /**
   * Converts the point object from the display object's (local) coordinates to 
   * the Stage (global) coordinates. 
   * @param point
   * @return
   */
  final public native Point localToGlobal(Point point) /*-{
     return this.localToGlobal(point);
  }-*/;

  
  /**
   *   Dispatched when a display object is added to the display list.
   */
  final public static EventType ADDED = EventType.make("added");
      
  /**
   * Dispatched when a display object is added to the on stage display list, 
   * either directly or through the addition of a sub tree in which the display 
   * object is contained.
   */
  final public static EventType ADDED_TO_STAGE = EventType.make("addedToStage");
  
     
  /**
   * Dispatched when the playhead is entering a new frame.  
   */
  final public static EventType ENTERFRAME = EventType.make("enterFrame");
  
          
  /**
   * Dispatched when a display object is about to be removed from the display 
   * list. 
   */
  final public static EventType REMOVED = EventType.make("removed");
          
  /**
   * Dispatched when a display object is about to be removed from the display
   *  list, either directly or through the removal of a sub tree in which the 
   *  display object is contained. 
   */
  final public static EventType REMOVED_FROM_STAGE = EventType.make("removedFromStage");
  
          
  /**
   * Dispatched when the display list is about to be updated and rendered.
   */
  final public static EventType RENDER = EventType.make("render");
}
