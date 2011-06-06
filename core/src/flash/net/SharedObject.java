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

package flash.net;

import com.google.gwt.core.client.JavaScriptObject;

import flash.events.EventType;

import flash.gwt.FlashImport;

import flash.events.EventDispatcher;

/**
 * Implementation of 
 * <a href="http://help.adobe.com/en_US/FlashPlatform/reference/actionscript/3/flash/net/SharedObject.html">
 * flash.net.SharedObject</a>
 */
@FlashImport({"flash.net.SharedObject"})
public class SharedObject extends EventDispatcher {
  protected SharedObject() {}
  
  /**
   * Returns a reference to a locally persistent shared object that is only available to the current client.
   * @param name
   * @param localPath
   * @return
   */
  public static native SharedObject getLocal(String name, String localPath, boolean secure) /*-{
    return flash.net.SharedObject.getLocal(name, localPath, secure); 
  }-*/;
  
  /**
   * The collection of attributes assigned to the data property of the object; these attributes can be shared and stored.
   * @return
   */
  final public native JavaScriptObject getData() /*-{
    return this.data;
  }-*/;
  
  /**
   * The current size of the shared object, in bytes.
   * @return
   */
  final public native int size() /*-{
    return this.size;
  }-*/;
  
  
  /**
   * For local shared objects, purges all of the data and deletes the shared object from the disk.
   */
  public final native void clear() /*-{
    this.clear();
  }-*/;
  
  
  /**
   * Closes the connection between a remote shared object and the server.
   */
  public final native void close() /*-{
    this.close();
  }-*/;
  
  /**
   * Immediately writes a locally persistent shared object to a local file.
   * @param minDiskSpace
   * @return
   */
  public final native String flush(int minDiskSpace) /*-{
    return this.flush(minDiskSpace);
  }-*/;
  
  /**
   * Updates the value of a property in a shared object and indicates to the server that the value of the property has changed.
   * @param propertyName
   * @param value
   */
  public final native void setProperty(String propertyName, Object value) /*-{
    this.setProperty(propertyName, value);
  }-*/;
  
  
  /**
   * Dispatched when an exception is thrown asynchronously — that is, from native asynchronous code.
   */
  public static final EventType ASYNCERROR = EventType.make("asyncError");
  /**
   * Dispatched when a SharedObject instance is reporting its status or error condition.
   */
  public static final EventType NETSTATUS = EventType.make("netStatus");
  
  /**
   * Dispatched when a remote shared object has been updated by the server.
   */
  public static final EventType SYNC = EventType.make("sync");
}
