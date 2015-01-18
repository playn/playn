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

import playn.core.*;
import playn.scene.*;
import react.Slot;

/**
 * Tests pause/resume notifications.
 */
public class PauseResumeTest extends Test {

  private final List<String> notifications = new ArrayList<String>();
  private ImageLayer layer;

  public PauseResumeTest (TestsGame game) {
    super(game, "PauseResume", "Tests pause/resume notifications.");
  }

  @Override public void init() {
    conns.add(game.plat.lifecycle.connect(new Slot<Platform.Lifecycle>() {
      private double start = game.plat.time();
      private int elapsed() {
        return (int)Math.round((game.plat.time() - start)/1000);
      }

      public void onEmit (Platform.Lifecycle event) {
        switch (event) {
        case PAUSE:
          game.log.info("Paused " + elapsed());
          notifications.add("Paused at " + elapsed() + "s");
          break;
        case RESUME:
          game.log.info("Resumed " + elapsed());
          notifications.add("Resumed at " + elapsed() + "s");
          updateDisplay();
          break;
        default:
          break; // nada
        }
      }
    }));

    game.rootLayer.addAt(layer = new ImageLayer(), 15, 15);
    updateDisplay();
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
    TextLayout layout = game.graphics.layoutText(buf.toString(), new TextFormat());
    Canvas canvas = game.graphics.createCanvas(layout.size);
    canvas.setFillColor(0xFF000000).fillText(layout, 0, 0);
    layer.setTile(canvas.toTexture());
  }
}
