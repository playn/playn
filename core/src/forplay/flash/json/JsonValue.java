/*
 * Copyright 2010 Google Inc.
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
package forplay.flash.json;

/**
 * Base interface for all Json values.
 */
public interface JsonValue {

  /**
   * Returns an enumeration representing the fundamental JSON type.
   */
  JsonType getType();

  /**
   * Returns a serialized JSON string representing this value.
   * @return
   */
  String toJson();

  /**
   * Equivalent of Javascript '==' operator comparison between two values.
   */
  boolean jsEquals(JsonValue value);
}
