/**
 * Copyright 2011 The PlayN Authors
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
package playn.core.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import playn.core.Json;
import playn.core.Json.TypedArray;

/**
 * Extends an {@link ArrayList} with helper methods to determine the underlying JSON type of the list element.
 */
class JsonArray implements Json.Array {
  private final ArrayList<Object> list;
  
  /**
   * Creates an empty {@link JsonArray} with the default capacity.
   */
  public JsonArray() {
    list = new ArrayList<Object>();
  }

  /**
   * Creates an empty {@link JsonArray} from the given collection of objects.
   */
  JsonArray(Collection<? extends Object> collection) {
    list = new ArrayList<Object>(collection);
  }

  /**
   * Creates a {@link JsonArray} from an array of contents.
   */
  static JsonArray from(Object... contents) {
    return new JsonArray(Arrays.asList(contents));
  }

  /**
   * Creates a {@link JsonBuilder} for a {@link JsonArray}.
   */
  public static JsonBuilder<JsonArray> builder() {
    return new JsonBuilder<JsonArray>(new JsonArray());
  }
  
  public void add(java.lang.Object value) {
    JsonImpl.checkJsonType(value);
    list.add(value);
  }
  
  public void add(int index, java.lang.Object value) {
    JsonImpl.checkJsonType(value);
    // TODO(mmastrac): Use an array rather than ArrayList to make this more efficient
    while (list.size() < index)
      list.add(null);
    list.add(index, value);
  }

  /**
   * Returns the {@link JsonArray} at the given index, or null if it does not exist or is the wrong type.
   */
  public Json.Array getArray(int key) {
    return getArray(key, (Json.Array)null);
  }

  /**
   * Returns the {@link JsonArray} at the given index, or the default if it does not exist or is the wrong type.
   */
  public Json.Array getArray(int key, Json.Array default_) {
    Object o = get(key);
    return (o instanceof Json.Array) ? (Json.Array)get(key) : default_;
  }

  @Override
  public <T> TypedArray<T> getArray(int index, Class<T> jsonType) {
    Json.Array array = getArray(index);
    return array == null ? null : new JsonTypedArray<T>(array, jsonType);
  }

  /**
   * Returns the {@link Boolean} at the given index, or false if it does not exist or is the wrong type.
   */
  public boolean getBoolean(int key) {
    return getBoolean(key, false);
  }

  /**
   * Returns the {@link Boolean} at the given index, or the default if it does not exist or is the wrong type.
   */
  public boolean getBoolean(int key, boolean default_) {
    Object o = get(key);
    return o instanceof Boolean ? (Boolean)o : default_;
  }

  /**
   * Returns the {@link Double} at the given index, or 0.0 if it does not exist or is the wrong type.
   */
  public double getDouble(int key) {
    return getDouble(key, 0);
  }

  /**
   * Returns the {@link Double} at the given index, or the default if it does not exist or is the wrong type.
   */
  public double getDouble(int key, double default_) {
    Object o = get(key);
    return o instanceof Number ? ((Number)o).doubleValue() : default_;
  }

  /**
   * Returns the {@link Float} at the given index, or 0.0f if it does not exist or is the wrong type.
   */
  public float getNumber(int key) {
    return getNumber(key, 0);
  }

  /**
   * Returns the {@link Float} at the given index, or the default if it does not exist or is the wrong type.
   */
  public float getNumber(int key, float default_) {
    Object o = get(key);
    return o instanceof Number ? ((Number)o).floatValue() : default_;
  }

  /**
   * Returns the {@link Integer} at the given index, or 0 if it does not exist or is the wrong type.
   */
  public int getInt(int key) {
    return getInt(key, 0);
  }

  /**
   * Returns the {@link Integer} at the given index, or the default if it does not exist or is the wrong type.
   */
  public int getInt(int key, int default_) {
    Object o = get(key);
    return o instanceof Number ? ((Number)o).intValue() : default_;
  }

  /**
   * Returns the {@link Long} at the given index, or 0 if it does not exist or is the wrong type.
   */
  public long getLong(int key) {
    return getLong(key, 0);
  }

  /**
   * Returns the {@link Long} at the given index, or the default if it does not exist or is the
   * wrong type.
   */
  public long getLong(int key, long default_) {
    Object o = get(key);
    return o instanceof Number ? ((Number)o).longValue() : default_;
  }

  /**
   * Returns the {@link JsonObject} at the given index, or null if it does not exist or is the wrong type.
   */
  public Json.Object getObject(int key) {
    return getObject(key, null);
  }

  /**
   * Returns the {@link JsonObject} at the given index, or the default if it does not exist or is the wrong type.
   */
  public Json.Object getObject(int key, Json.Object default_) {
    Object o = get(key);
    return o instanceof Json.Object ? (Json.Object)get(key) : default_;
  }

  /**
   * Returns the {@link String} at the given index, or null if it does not exist or is the wrong type.
   */
  public String getString(int key) {
    return getString(key, null);
  }

  /**
   * Returns the {@link String} at the given index, or the default if it does not exist or is the wrong type.
   */
  public String getString(int key, String default_) {
    Object o = get(key);
    return (o instanceof String) ? (String)o : default_;
  }

  /**
   * Returns true if the array has a array element at that index.
   */
  public boolean isArray(int key) {
    return get(key) instanceof Json.Array;
  }

  /**
   * Returns true if the array has a boolean element at that index.
   */
  public boolean isBoolean(int key) {
    return get(key) instanceof Boolean;
  }

  /**
   * Returns true if the array has a null element at that index.
   */
  public boolean isNull(int key) {
    return get(key) == null;
  }

  /**
   * Returns true if the array has a number element at that index.
   */
  public boolean isNumber(int key) {
    return get(key) instanceof Number;
  }

  /**
   * Returns true if the array has a string element at that index.
   */
  public boolean isString(int key) {
    return get(key) instanceof String;
  }

  /**
   * Returns true if the array has a object element at that index.
   */
  public boolean isObject(int key) {
    return get(key) instanceof Json.Object;
  }

  @Override
  public int length() {
    return list.size();
  }

  @Override
  public void remove(int index) {
    if (index < 0 || index >= list.size())
      return;
    list.remove(index);
  }
  
  @Override
  public void set(int index, java.lang.Object value) {
    JsonImpl.checkJsonType(value);
    // TODO(mmastrac): Use an array rather than ArrayList to make this more efficient
    while (list.size() <= index)
      list.add(null);
    list.set(index, value);
  }

  @Override
  public String toString() {
    return list.toString();
  }
  
  @Override
  public <T extends JsonSink<T>> JsonSink<T> write(JsonSink<T> sink) {
    for (int i = 0; i < list.size(); i++)
      sink.value(list.get(i));
    return sink;
  }
  
  /**
   * Returns the underlying object at the given index, or null if it does not exist or is out of
   * bounds (to match the HTML implementation).
   */
  Object get(int key) {
    return (key >= 0 && key < list.size()) ? list.get(key) : null;
  }
 
}
