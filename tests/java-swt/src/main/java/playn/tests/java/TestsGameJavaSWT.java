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

import java.util.ArrayList;
import java.util.List;

// import org.lwjgl.opengl.Display;
// import org.lwjgl.opengl.DisplayMode;

import playn.java.SWTPlatform;

// import playn.tests.core.FullscreenTest;
import playn.tests.core.TestsGame;
// import static playn.tests.core.FullscreenTest.*;

public class TestsGameJavaSWT {

  public static void main(String[] args) {
    SWTPlatform.Config config = new SWTPlatform.Config();
    config.width = 800;
    config.height = 600;
    config.appName = "Tests";
    // for (String arg : args) {
    //   if (arg.startsWith("@") && arg.endsWith("x")) {
    //     config.scaleFactor = Float.parseFloat(arg.substring(1, arg.length()-1));
    //   }
    // }
    final SWTPlatform platform = new SWTPlatform(config);

    // // plug in a lwjgl implementation for fullscreen test
    // FullscreenTest.setHost(new FullscreenTest.Host() {
    //   @Override public void setMode (Mode mode) {
    //     platform.graphics().setSize(mode.width, mode.height, true);
    //   }

    //   @Override public Mode[] enumerateModes () {
    //     List<Mode> modes = new ArrayList<Mode>();
    //     try {
    //       DisplayMode desktop = Display.getDesktopDisplayMode();
    //       int bpp = desktop.getBitsPerPixel();
    //       for (DisplayMode dmode : Display.getAvailableDisplayModes()) {
    //         if (dmode.getBitsPerPixel() != bpp) {
    //           continue;
    //         }
    //         Mode mode = new Mode();
    //         mode.width = dmode.getWidth();
    //         mode.height = dmode.getHeight();
    //         mode.depth = dmode.getBitsPerPixel();
    //         modes.add(mode);
    //       }
    //     }
    //     catch (LWJGLException ex) {
    //       throw new RuntimeException(ex);
    //     }
    //     return modes.toArray(new Mode[modes.size()]);
    //   }
    // });

    // create our the game, initialization happens in ctor
    new TestsGame(platform, args);

    platform.start();
  }
}
