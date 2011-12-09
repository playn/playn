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

package playn.flash;

import com.google.gwt.core.client.JavaScriptObject;

import flash.net.SharedObject;

import playn.core.Storage;

public class FlashStorage implements Storage {

  private SharedObject shared;

  public FlashStorage() {
    shared = SharedObject.getLocal("playn", null, false);
  }

  @Override
  public String getItem(String key) {
    // TODO Auto-generated method stub
    return get(shared.getData(), key);
  }

  @Override
  public void removeItem(String key) {
   remove(shared.getData(), key);
   shared.flush(0);
  }

  @Override
  public void setItem(String key, String data) throws RuntimeException {
    set(shared.getData(), key, data);
    shared.flush(0);
  }

  @Override
  public Iterable<String> keys() {
    throw new UnsupportedOperationException(); // TODO
  }

  @Override
  public boolean isPersisted() {
    // TODO Auto-generated method stub
    return true;
  }

  private native String get(JavaScriptObject data, String key) /*-{
    // TODO Auto-generated method stub
    return data[key];
  }-*/;

  private native void set(JavaScriptObject data, String key, String data2) /*-{
    data[key]=data2;
  }-*/;

  private native void remove(JavaScriptObject data, String key) /*-{
    delete data[key];
  }-*/;
}
