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

import playn.java.LWJGLPlatform;
import playn.tests.core.TestsGame;

public class TestsGameJavaLWJGL {

  public static void main(String[] args) {
    // configure and create the Java platform
    LWJGLPlatform.Config config = new LWJGLPlatform.Config();
    config.width = 800;
    config.height = 600;
    config.appName = "Tests";
    // for (String arg : args) {
    //   if (arg.startsWith("@") && arg.endsWith("x")) {
    //     config.scaleFactor = Float.parseFloat(arg.substring(1, arg.length()-1));
    //   }
    // }
    LWJGLPlatform plat = new LWJGLPlatform(config);

    // let the caller know that we accept some args
    plat.log().info("Usage: TestsGameJava [@Nx] [test#]");
    // plat.log().info("Usage: TestsGameJava [test#]");
    // plat.log().info("  [@Nx] specifies a scale factor: @2x, @1.5x");
    plat.log().info("  [test#] specifies a test to launch directly: test0, test12 ");

    // create our the game, initialization happens in ctor
    TestsGame game = new TestsGame(plat, args);

    // and start the game loop
    plat.start();
  }
}
