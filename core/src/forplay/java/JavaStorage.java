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
package forplay.java;

import java.util.HashMap;
import java.util.Map;

import forplay.core.Storage;

/**
 * JavaStorage does not support persistent storage yet, so data is not persisted.
 * 
 * TODO(pdr): add on-disk storage.
 */
class JavaStorage implements Storage {
  private Map<String, String> storageMap;
  private boolean isPersisted;

  public JavaStorage() {
    storageMap = new HashMap<String, String>();
    isPersisted = false;
  }

  @Override
  public void setItem(String key, String value) throws RuntimeException {
    storageMap.put(key, value);
  }

  @Override
  public void removeItem(String key) {
    storageMap.remove(key);
  }

  @Override
  public String getItem(String key) {
    return storageMap.get(key);
  }

  @Override
  public boolean isPersisted() {
    return isPersisted;
  }
}
