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
    JavaPlatform.Config config = new JavaPlatform.Config();
    for (String arg : args) {
      if (arg.startsWith("@") && arg.endsWith("x")) {
        config.scaleFactor = Float.parseFloat(arg.substring(1, arg.length()-1));
      }
    }
    config.width = 800;
    config.height = 600;
    JavaPlatform platform = JavaPlatform.register(config);
    platform.setTitle("Tests");
    // let the caller know that we accept some args
    platform.log().info("Usage: TestsGameJava [@Nx] [test#]");
    platform.log().info("  [@Nx] specifies a scale factor: @2x, @1.5x");
    platform.log().info("  [test#] specifies a test to launch directly: test0, test12 ");
    TestsGame.args = args;
    PlayN.run(new TestsGame());
  }
}
