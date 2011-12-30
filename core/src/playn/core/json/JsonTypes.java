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

import playn.core.Json;

/**
 * Type-checking routines. These live in their own file so that we can replace them wholesale in GWT
 * mode.
 */
class JsonTypes {
  public static boolean isArray(Object o) {
    return o instanceof Json.Array;
  }

  public static boolean isObject(Object o) {
    return o instanceof Json.Object;
  }

}
