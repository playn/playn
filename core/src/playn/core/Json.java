/**
 * Copyright 2010 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package playn.core;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * PlayN JSON parsing and serialization interface.
 */
public interface Json {

  /**
   * A writer interface for serializing data to JSON.
   */
  interface Writer {

    /**
     * Specifies the key to be used for the next value entry.
     */
    void key(String key);

    /**
     * Writes a boolean value.
     */
    void value(boolean x);

    /**
     * Writes an integer value.
     */
    void value(int x);

    /**
     * Writes a double value.
     */
    void value(double x);

    /**
     * Writes a string value.
     */
    void value(String x);

    /**
     * Begins a new nested object.
     */
    void object();

    /**
     * Ends the current nested object.
     */
    void endObject();

    /**
     * Begins a new nested array.
     */
    void array();

    /**
     * Ends the current nested array.
     */
    void endArray();

    /**
     * Serializes the object associated with this writer. The writer may be reused after this call.
     */
    String write();
  }

  /**
   * A JSON array that assumes all values are of a uniform JSON type.
   */
  abstract class TypedArray<T> implements Iterable<T> {
    /**
     * Returns the number of values in this array.
     */
    public abstract int length();

    /**
     * Returns the value at the given index, or the default value for <code>T</code> if there's
     * a value of a different type at the index.
     *
     * @throws ArrayIndexOutOfBoundsException if <code>index &lt; 0</code> or <code>index >=
     * length</code>
     */
    public T get(int index) {
      if (index < 0 || index >= length()) {
        throw new ArrayIndexOutOfBoundsException(index);
      }
      return getImpl(index);
    }

    /**
     * Returns an iterator over the values of the assumed type in this array. If a value at a given
     * index isn't of the assumed type, the default value for the assumed type will be returned by
     * <code>next</code>.
     */
    @Override
    public Iterator<T> iterator () {
      return new Iterator<T>() {
        @Override
        public boolean hasNext () {
          return index < length();
        }

        @Override
        public T next () {
          if (index >= length()) {
              throw new NoSuchElementException();
          }
          return getImpl(index++);
        }

        @Override
        public void remove () {
          throw new UnsupportedOperationException();
        }

        private int index;
      };
    }

    protected abstract T getImpl(int index);
  }

  /**
   * Represents a parsed JSON array as a simple int->value map.
   */
  interface Array {

    /**
     * Gets the length of this array.
     */
    int length();

    /**
     * Gets the boolean value at the given index, or <code>false</code> if there is no value at this
     * index.
     */
    boolean getBoolean(int index);

    /**
     * Gets the integer value at the given index, or <code>0</code> if there is no value at this
     * index.
     */
    int getInt(int index);

    /**
     * Gets the double value at the given index, or <code>0</code> if there is no value at this
     * index.
     */
    double getNumber(int index);

    /**
     * Gets the string value at the given index, or <code>null</code> if there is no value at this
     * index.
     */
    String getString(int index);

    /**
     * Gets the object value at the given index, or <code>null</code> if there is no value at this
     * index.
     */
    Object getObject(int index);

    /**
     * Gets the array value at the given index, or <code>null</code> if there is no value at this
     * index.
     */
    Array getArray(int index);

    /**
     * Gets an array of boolean values at the given index, or <code>null</code> if there is no value
     * at this index.
     */
    TypedArray<Boolean> getBooleanArray(int index);

    /**
     * Gets an array of integer values at the given index, or <code>null</code> if there is no value
     * at this index.
     */
    TypedArray<Integer> getIntArray(int index);

    /**
     * Gets an array of double values at the given index, or <code>null</code> if there is no value
     * at this index.
     */
    TypedArray<Double> getNumberArray(int index);

    /**
     * Gets an array of string values at the given index, or <code>null</code> if there is no value
     * at this index.
     */
    TypedArray<String> getStringArray(int index);

    /**
     * Gets an array of object values at the given index, or <code>null</code> if there is no value
     * at this index.
     */
    TypedArray<Object> getObjectArray(int index);
  }

  /**
   * Represents a parsed JSON object as a simple string->value map.
   */
  interface Object {

    /**
     * Gets the boolean value at the given key, or <code>false</code> if there is no value at this
     * key.
     */
    boolean getBoolean(String key);

    /**
     * Gets the integer value at the given key, or <code>0</code> if there is no value at this key.
     */
    int getInt(String key);

    /**
     * Gets the double value at the given key, or <code>0</code> if there is no value at this key.
     */
    double getNumber(String key);

    /**
     * Gets the string value at the given key, or <code>null</code> if there is no value at this
     * key.
     */
    String getString(String key);

    /**
     * Gets the object value at the given key, or <code>null</code> if there is no value at this
     * key.
     */
    Object getObject(String key);

    /**
     * Gets the array value at the given key, or <code>null</code> if there is no value at this key.
     */
    Array getArray(String key);

    /**
     * Gets an array of Boolean values at the given key, or <code>null</code> if there is no value
     * at this key.
     */
    TypedArray<Boolean> getBooleanArray(String key);

    /**
     * Gets an array of Integer values at the given key, or <code>null</code> if there is no value
     * at this key.
     */
    TypedArray<Integer> getIntArray(String key);

    /**
     * Gets an array of double values at the given key, or <code>null</code> if there is no value
     * at this key.
     */
    TypedArray<Double> getNumberArray(String key);

    /**
     * Gets an array of string values at the given key, or <code>null</code> if there is no value
     * at this key.
     */
    TypedArray<String> getStringArray(String key);

    /**
     * Gets an array of object values at the given key, or <code>null</code> if there is no value
     * at this key.
     */
    TypedArray<Object> getObjectArray(String key);

    /**
     * Returns true if this object contains a value at the specified key, false if not.
     */
    boolean containsKey(String key);

    /**
     * Gets a set of keys for this JSON object.
     */
    TypedArray<String> getKeys();
  }

  /**
   * Creates a new {@link Writer}, which can be used to serialize data into the JSON format.
   *
   * <code>
   * // An example of using the JSON writer interface.
   * Json.Writer w = json.newWriter();
   *
   * // Simple values.
   * w.key("x");
   * w.value(10);
   * w.key("y");
   * w.value(10);
   *
   * // Nested object.
   * w.key("nestedObject");
   * w.object();
   * w.key("id");
   * w.value("xyz123");
   * w.endObject();
   *
   * // Nested array.
   * w.key("nestedArray");
   * w.array();
   * for (int i = 0; i < 5; ++i) {
   *   w.value(i);
   * }
   * w.endArray();
   *
   * String jsonString = w.write();
   *
   * // Produces:
   * {
   *   'x': 10,
   *   'y': 10,
   *   'nestedObject': {
   *     'id': 'xyz123'
   *   },
   *   'nestedArray': [
   *     1, 2, 3, 4, 5
   *   ]
   * }
   * </code>
   */
  Writer newWriter();

  /**
   * Parses the given JSON string into an {@link Object} that can be dynamically introspected.
   */
  Object parse(String json);
}
