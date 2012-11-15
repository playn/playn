/**
 * Copyright 2011 The PlayN Authors
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
package playn.core;

/**
 * Stores settings in a key/value map. This will attempt to store persistently, but will fall back
 * to an in-memory map. Use {@link #isPersisted} to check if the data are being persisted.
 */
public interface Storage {

  /**
   * Represents a batch of edits to be applied to storage in one transaction. Individual edits are
   * expensive on some platforms, and this batch interface allows multiple edits to be applied
   * substantially more efficiently (more than an order of magnitude) on those platforms. If you're
   * going to make hundreds or thousands of changes at once, use this mechanism.
   */
  interface Batch {
    /** Adds an update to the batch. */
    void setItem(String key, String data);

    /** Adds an deletion to the batch. */
    void removeItem(String key);

    /** Commits the batch, applying all queued changes. Attempts to call {@link #setItem} or
     * {@link #removeItem} after a call to this method will fail. */
    void commit();
  }

  /**
   * Sets the value associated with the specified key to {@code data}.
   *
   * @param key identifies the value.
   * @param data the value associated with the key, which must not be null.
   */
  public void setItem(String key, String data);

  /**
   * Removes the item in the Storage associated with the specified key.
   *
   * @param key identifies the value.
   */
  public void removeItem(String key);

  /**
   * Returns the item associated with {@code key}, or null if no item is associated with it.
   *
   * @param key identifies the value.
   * @return the value associated with the given key, or null.
   */
  public String getItem(String key);

  /**
   * Creates a {@link Batch} that can be used to effect multiple changes to storage in a single,
   * more efficient, operation.
   */
  public Batch startBatch();

  /**
   * Returns an object that can be used to iterate over all storage keys. <em>Note:</em> changes
   * made to storage while iterating over the keys will not be reflected in the iteration, nor will
   * they conflict with it.
   */
  public Iterable<String> keys();

  /**
   * Returns true if storage data will be persistent across restarts.
   */
  public boolean isPersisted();
}
