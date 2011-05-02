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
package forplay.core;

/**
 * Storage interface. This interface can be used to store settings and user data
 * in a key/value String pair.
 * 
 * This will attempt to store persistently, but will fall back to an in-memory
 * Map. Use {@link #isPersisted()} to check if the data is being persisted.
 */
public interface Storage {

  /**
   * Sets the value in the Storage associated with the specified key to the
   * specified data.
   * 
   * @param key the key to a value in the Storage
   * @param data the value associated with the key
   */
  public void setItem(String key, String data) throws RuntimeException;

  /**
   * Removes the item in the Storage associated with the specified key.
   * 
   * @param key the key to a value in the Storage
   */
  public void removeItem(String key);

  /**
   * Returns the item in the Storage associated with the specified key.
   * 
   * @param key the key to a value in the Storage
   * @return the value associated with the given key
   */
  public String getItem(String key);

  /**
   * Returns true if the Storage data is will be persistent across restarts.
   * 
   * @return true if the Storage data is being stored persistently
   */
  public boolean isPersisted();
}
