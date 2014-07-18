/**
 * Copyright 2012 The PlayN Authors
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
package playn.core.util;

/**
 * Methods to support enums in PlayN.
 */
public class Enums
{
  /** Finds the enum instance in the given array whose name matches the given value. This is a
   * substitute for {@link Enum#valueOf(Class, String)}, which crashes on iOS. This is apparently
   * due to the fact that IKVM does not support {@link Class#getEnumConstants()}. However, the
   * static {@code values()} method on enum types is supported, so this method can be used instead.
   * <p>Just pass in the values directly, for example:
   * <pre>
   *   String enumName = ...;
   *   MyEnum value = Enums.valueOf(MyEnum.values(), enumName);
   * </pre></p> */
  public static <E extends Enum<?>> E valueOf (E[] values, String name){
    for (E e : values) {
      if (e.name().equals(name)) {
        return e;
      }
    }
    throw new IllegalArgumentException(
      name + " not found in enum " + values[0].getClass().getName());
  }
}
