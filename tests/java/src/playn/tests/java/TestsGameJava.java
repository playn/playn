/**
 * Copyright 2010 The PlayN Authors
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
package playn.tests.java;

import playn.core.PlayN;
import playn.java.JavaPlatform;

import playn.tests.core.TestsGame;

public class TestsGameJava {

  public static void main(String[] args) {
    JavaPlatform platform = JavaPlatform.register(args.length == 0 ? 1 : Float.parseFloat(args[0]));
    platform.setTitle("Tests");
    platform.assets().setPathPrefix("playn/tests/resources");
    PlayN.run(new TestsGame());
  }
}
