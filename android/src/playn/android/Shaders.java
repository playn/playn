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

public class Shaders {
  // FIXME: When we get Android projects correctly including resources when
  // built from Eclipse, we should change the pathPrefix to playn/core/gl and delete
  // the duplicate .txt files in the Android project.
  static final String pathPrefix = "playn/android";
  static final String vertexShader = "vertex-shader.txt";
  static final String texFragmentShader = "tex-fragment-shader.txt";
  static final String colorFragmentShader = "color-fragment-shader.txt";
}
