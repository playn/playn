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

import java.util.Collection;
import java.util.Map;

import playn.core.Json;

/**
 * Common interface for things that accept JSON objects. Normally not referenced by users.
 *
 * @param <SELF> A subclass of {@link JsonSink}.
 */
public interface JsonSink<SELF extends JsonSink<SELF>> {
  /**
   * Emits the start of an array.
   */
  SELF array(Collection<?> c);

  /**
   * Emits the start of an array.
   */
  SELF array(Json.Array c);

  /**
   * Emits the start of an array with a key.
   */
  SELF array(String key, Collection<?> c);

  /**
   * Emits the start of an array with a key.
   */
  SELF array(String key, Json.Array c);

  /**
   * Emits the start of an object.
   */
  SELF object(Map<?, ?> map);

  /**
   * Emits the start of an object.
   */
  SELF object(Json.Object map);

  /**
   * Emits the start of an object with a key.
   */
  SELF object(String key, Map<?, ?> map);

  /**
   * Emits the start of an object with a key.
   */
  SELF object(String key, Json.Object map);

  /**
   * Emits a 'null' token.
   */
  SELF nul();

  /**
   * Emits a 'null' token with a key.
   */
  SELF nul(String key);

  /**
   * Emits an object if it is a JSON-compatible type, otherwise throws an exception.
   */
  SELF value(Object o);

  /**
   * Emits an object with a key if it is a JSON-compatible type, otherwise throws an exception.
   */
  SELF value(String key, Object o);

  /**
   * Emits a string value (or null).
   */
  SELF value(String s);

  /**
   * Emits a boolean value.
   */
  SELF value(boolean b);

  /**
   * Emits a {@link Number} value.
   */
  SELF value(Number n);

  /**
   * Emits a string value (or null) with a key.
   */
  SELF value(String key, String s);

  /**
   * Emits a boolean value with a key.
   */
  SELF value(String key, boolean b);

  /**
   * Emits a {@link Number} value with a key.
   */
  SELF value(String key, Number n);

  /**
   * Starts an array.
   */
  SELF array();

  /**
   * Starts an object.
   */
  SELF object();

  /**
   * Starts an array within an object, prefixed with a key.
   */
  SELF array(String key);

  /**
   * Starts an object within an object, prefixed with a key.
   */
  SELF object(String key);

  /**
   * Ends the current array or object.
   */
  SELF end();
}
