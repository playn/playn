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

public class GLUtil {

  /**
   * Returns the next largest power of two, or zero if x is already a power of two.
   */
  public static int nextPowerOfTwo(int x) {
    assert x < 0x10000;

    int bit = 0x8000, highest = -1, count = 0;
    for (int i = 15; i >= 0; --i, bit >>= 1) {
      if ((x & bit) != 0) {
        ++count;
        if (highest == -1) {
          highest = i;
        }
      }
    }
    if (count <= 1) {
      return 0;
    }
    return 1 << (highest + 1);
  }
}
