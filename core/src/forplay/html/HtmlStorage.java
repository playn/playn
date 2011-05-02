/**
 * Copyright 2011 The ForPlay Authors
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
package forplay.html;

import java.util.HashMap;
import java.util.Map;

import forplay.core.Storage;

/**
 * HtmlStorage attempts to use Html5's local storage if supported, and falls
 * back to using an in-memory map.
 */
class HtmlStorage implements Storage {
  private Map<String, String> storageMap;
  private boolean isPersisted;

  public HtmlStorage() {
    com.google.gwt.storage.client.Storage storage =
        com.google.gwt.storage.client.Storage.getLocalStorageIfSupported();
    if (storage != null) {
      storageMap = new com.google.gwt.storage.client.StorageMap(storage);
      isPersisted = true;
    } else {
      storageMap = new HashMap<String, String>();
      isPersisted = false;
    }
  }

  @Override
  public void setItem(String key, String value) throws RuntimeException {
    // do not store data if the key is null or empty (a restriction of
    // LocalStorage)
    if (key == null || key == "") {
      return;
    }
    storageMap.put(key, value);
  }

  @Override
  public void removeItem(String key) {
    try {
      storageMap.remove(key);
    } catch (RuntimeException e) {
      // ignore
    }
  }

  @Override
  public String getItem(String key) {
    try {
      return storageMap.get(key);
    } catch (RuntimeException e) {
      // ignore
    }
    return null;
  }

  @Override
  public boolean isPersisted() {
    return isPersisted;
  }
}
