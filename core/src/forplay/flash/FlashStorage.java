// Copyright 2011 Google Inc. All Rights Reserved.

package forplay.flash;

import com.google.gwt.core.client.JavaScriptObject;

import flash.net.SharedObject;

import forplay.core.Storage;

/**
 * @author cromwellian@google.com (Your Name Here)
 *
 */
public class FlashStorage implements Storage {

  private SharedObject shared;

  public FlashStorage() {
    shared = SharedObject.getLocal("forplay", null, false);
  }

  /* (non-Javadoc)
   * @see forplay.core.Storage#getItem(java.lang.String)
   */
  @Override
  public String getItem(String key) {
    // TODO Auto-generated method stub
    return get(shared.getData(), key);
  }

  /**
   * @param data
   * @param key
   * @return
   */
  private native String get(JavaScriptObject data, String key) /*-{
    // TODO Auto-generated method stub
    return data[key];
  }-*/;

  /* (non-Javadoc)
   * @see forplay.core.Storage#isPersisted()
   */
  @Override
  public boolean isPersisted() {
    // TODO Auto-generated method stub
    return true; 
  }

  /* (non-Javadoc)
   * @see forplay.core.Storage#removeItem(java.lang.String)
   */
  @Override
  public void removeItem(String key) {
   remove(shared.getData(), key);
   shared.flush(0);
  }

  /**
   * @param data
   * @param key
   */
  private native void remove(JavaScriptObject data, String key) /*-{
    delete data[key];
  }-*/;

  /* (non-Javadoc)
   * @see forplay.core.Storage#setItem(java.lang.String, java.lang.String)
   */
  @Override
  public void setItem(String key, String data) throws RuntimeException {
    set(shared.getData(), key, data);
    shared.flush(0);
  }

  /**
   * @param data
   * @param key
   * @param data2
   */
  private native void set(JavaScriptObject data, String key, String data2) /*-{
    data[key]=data2;
  }-*/;

}
