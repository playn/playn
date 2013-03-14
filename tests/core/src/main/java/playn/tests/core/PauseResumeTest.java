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
package playn.tests.core;

import java.util.ArrayList;
import java.util.List;

import static playn.core.PlayN.*;
import playn.core.CanvasImage;
import playn.core.ImageLayer;
import playn.core.TextFormat;
import playn.core.TextLayout;

/**
 * Tests pause/resume notifications.
 */
public class PauseResumeTest extends Test {

  private final List<String> notifications = new ArrayList<String>();
  private ImageLayer layer;

  @Override
  public String getName() {
    return "PauseResumeTest";
  }

  @Override
  public String getDescription() {
    return "Tests pause/resume notifications.";
  }

  @Override
  public void init() {
    setLifecycleListener(new LifecycleListener() {
      private double start = currentTime();
      private int elapsed() {
        return (int)Math.round((currentTime() - start)/1000);
      }

      @Override
      public void onPause() {
        log().info("Paused " + elapsed());
        notifications.add("Paused at " + elapsed() + "s");
      }
      @Override
      public void onResume() {
        log().info("Resumed " + elapsed());
        notifications.add("Resumed at " + elapsed() + "s");
        updateDisplay();
      }
      @Override
      public void onExit() {} // nada
    });

    layer = graphics().createImageLayer();
    updateDisplay();
    graphics().rootLayer().addAt(layer, 15, 15);
  }

  @Override
  public void dispose() {
    setLifecycleListener(null);
  }

  protected void updateDisplay() {
    StringBuffer buf = new StringBuffer();
    if (notifications.isEmpty()) {
      buf.append("No notifications. Pause and resume the game to generate some.");
    } else {
      buf.append("Notifications:\n");
      for (String note : notifications)
        buf.append(note).append("\n");
    }
    TextLayout layout = graphics().layoutText(buf.toString(), new TextFormat());
    CanvasImage image = graphics().createImage(layout.width(), layout.height());
    image.canvas().setFillColor(0xFF000000);
    image.canvas().fillText(layout, 0, 0);
    layer.setImage(image);
  }
}
