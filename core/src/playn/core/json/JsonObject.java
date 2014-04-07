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

import java.util.TreeMap;
import java.util.Map;

import playn.core.Json;
import playn.core.Json.TypedArray;

/**
 * Extends a {@link Map} with helper methods to determine the underlying JSON type of the map
 * element.
 */
class JsonObject implements Json.Object {
  private final Map<String, Object> map;

  /**
   * Creates an empty {@link JsonObject} with the default capacity.
   */
  public JsonObject() {
    // we use a tree map here to ensure predictable iteration order; for better or worse we have a
    // bunch of tests that rely on a specific iteration order and those broke when we moved from
    // JDK7 to 8; now we use an API that guarantees iteration order
    map = new TreeMap<String, Object>();
  }

  /**
   * Creates a {@link JsonBuilder} for a {@link JsonObject}.
   */
  public static JsonBuilder<JsonObject> builder() {
    return new JsonBuilder<JsonObject>(new JsonObject());
  }

  /**
   * Returns the {@link JsonArray} at the given key, or null if it does not exist or is the wrong
   * type.
   */
  public Json.Array getArray(String key) {
    return getArray(key, (Json.Array) null);
  }

  /**
   * Returns the {@link JsonArray} at the given key, or the default if it does not exist or is the
   * wrong type.
   */
  public Json.Array getArray(String key, Json.Array default_) {
    Object o = get(key);
    return (o instanceof Json.Array) ? (Json.Array) o : default_;
  }

  /**
   * Returns the {@link Boolean} at the given key, or false if it does not exist or is the wrong
   * type.
   */
  public boolean getBoolean(String key) {
    return getBoolean(key, false);
  }

  /**
   * Returns the {@link Boolean} at the given key, or the default if it does not exist or is the
   * wrong type.
   */
  public boolean getBoolean(String key, boolean default_) {
    Object o = get(key);
    return o instanceof Boolean ? (Boolean) o : default_;
  }

  /**
   * Returns the {@link Double} at the given key, or 0.0 if it does not exist or is the wrong type.
   */
  public double getDouble(String key) {
    return getDouble(key, 0);
  }

  /**
   * Returns the {@link Double} at the given key, or the default if it does not exist or is the
   * wrong type.
   */
  public double getDouble(String key, double default_) {
    Object o = get(key);
    return o instanceof Number ? ((Number) o).doubleValue() : default_;
  }

  /**
   * Returns the {@link Float} at the given key, or 0.0f if it does not exist or is the wrong type.
   */
  public float getNumber(String key) {
    return getNumber(key, 0);
  }

  /**
   * Returns the {@link Float} at the given key, or the default if it does not exist or is the wrong
   * type.
   */
  public float getNumber(String key, float default_) {
    Object o = get(key);
    return o instanceof Number ? ((Number) o).floatValue() : default_;
  }

  /**
   * Returns the {@link Integer} at the given key, or 0 if it does not exist or is the wrong type.
   */
  public int getInt(String key) {
    return getInt(key, 0);
  }

  /**
   * Returns the {@link Integer} at the given key, or the default if it does not exist or is the
   * wrong type.
   */
  public int getInt(String key, int default_) {
    Object o = get(key);
    return o instanceof Number ? ((Number) o).intValue() : default_;
  }

  /**
   * Returns the {@link Long} at the given key, or 0 if it does not exist or is the wrong type.
   */
  public long getLong(String key) {
    return getLong(key, 0L);
  }

  /**
   * Returns the {@link Long} at the given key, or the default if it does not exist or is the
   * wrong type.
   */
  public long getLong(String key, long default_) {
    Object o = get(key);
    return o instanceof Number ? ((Number) o).longValue() : default_;
  }

  /**
   * Returns the {@link JsonObject} at the given key, or null if it does not exist or is the wrong
   * type.
   */
  public Json.Object getObject(String key) {
    return getObject(key, null);
  }

  /**
   * Returns the {@link JsonObject} at the given key, or the default if it does not exist or is the
   * wrong type.
   */
  public Json.Object getObject(String key, Json.Object default_) {
    Object o = get(key);
    return (o instanceof JsonObject) ? (JsonObject)o : default_;
  }

  /**
   * Returns the {@link String} at the given key, or null if it does not exist or is the wrong type.
   */
  public String getString(String key) {
    return getString(key, null);
  }

  /**
   * Returns the {@link String} at the given key, or the default if it does not exist or is the
   * wrong type.
   */
  public String getString(String key, String default_) {
    Object o = get(key);
    return (o instanceof String) ? (String)o : default_;
  }

  /**
   * Returns true if the object has an element at that key (even if that element is null).
   */
  public boolean containsKey(String key) {
    return map.containsKey(key);
  }

  /**
   * Returns true if the object has an array element at that key.
   */
  public boolean isArray(String key) {
    return get(key) instanceof Json.Array;
  }

  /**
   * Returns true if the object has a boolean element at that key.
   */
  public boolean isBoolean(String key) {
    return get(key) instanceof Boolean;
  }

  /**
   * Returns true if the object has a null element at that key.
   */
  public boolean isNull(String key) {
    return get(key) == null;
  }

  /**
   * Returns true if the object has a number element at that key.
   */
  public boolean isNumber(String key) {
    return get(key) instanceof Number;
  }

  /**
   * Returns true if the object has a object element at that key.
   */
  public boolean isString(String key) {
    return get(key) instanceof String;
  }

  /**
   * Returns true if the object has a string element at that key.
   */
  public boolean isObject(String key) {
    return get(key) instanceof Json.Object;
  }

  @Override
  public <T> TypedArray<T> getArray(String key, Class<T> valueType) {
    return getArray(key, valueType, null);
  }

  @Override
  public <T> TypedArray<T> getArray(String key, Class<T> valueType, TypedArray<T> dflt) {
    Json.Array array = getArray(key);
    return array == null ? dflt : new JsonTypedArray<T>(array, valueType);
  }

  @Override
  public TypedArray<String> keys() {
    return new JsonStringTypedArray(map.keySet());
  }

  @Override
  public void put(String key, Object value) {
    JsonImpl.checkJsonType(value);
    map.put(key, value);
  }

  @Override
  public void remove(String key) {
    map.remove(key);
  }

  @Override
  public String toString() {
    return map.toString();
  }

  @Override
  public <T extends JsonSink<T>> JsonSink<T> write(JsonSink<T> sink) {
    for (Map.Entry<String, Object> entry : map.entrySet())
      sink.value(entry.getKey(), entry.getValue());
    return sink;
  }

  /**
   * Gets the JSON value at the given key.
   */
  Object get(String key) {
    return map.get(key);
  }
}
