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
package playn.android;

/**
 * Fix for the two missing function calls in SDK 2.2 Froyo's OpenGL API.
 */
public class AndroidGL20Native extends AndroidGL20 {

  @Override
  native public void glDrawElements(int mode, int count, int type, int offset);

  @Override
  native public void glVertexAttribPointer(int indx, int size, int type, boolean normalized,
      int stride, int ptr);

  AndroidGL20Native() {
  }

  static {
    System.loadLibrary("playn-android");
  }
}
