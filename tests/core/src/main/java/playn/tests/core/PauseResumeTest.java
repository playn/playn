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
 * Tests focus lost/gained and app paused/resumed notifications.
 */
public class PauseResumeTest extends Test {

  private final List<String> notifications = new ArrayList<String>();
  private ImageLayer layer;

  public PauseResumeTest (TestsGame game) {
    super(game, "PauseResume", "Tests pause/resume notifications.");
  }

  private int elapsed(double start) {
    return (int)Math.round((game.plat.time() - start)/1000);
  }

  @Override public void init() {
    double start = game.plat.time();
    conns.add(game.plat.lifecycle.connect(event -> {
      switch (event) {
      case PAUSE:
        game.log.info("Paused " + elapsed(start));
        notifications.add("Paused at " + elapsed(start) + "s");
        break;
      case RESUME:
        game.log.info("Resumed " + elapsed(start));
        notifications.add("Resumed at " + elapsed(start) + "s");
        updateDisplay();
        break;
      default:
        break; // nada
      }
    }));
    conns.add(game.plat.input().focus.connect(focus -> {
      game.log.info("Focus changed: " + focus);
      notifications.add((focus ? "Gained" : "Lost") + " focus at " + elapsed(start) + "s");
      updateDisplay();
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
    TextBlock block = new TextBlock(game.graphics.layoutText(
      buf.toString(), new TextFormat(), TextWrap.MANUAL));
    Canvas canvas = game.graphics.createCanvas(block.bounds.width(), block.bounds.height());
    canvas.setFillColor(0xFF000000);
    block.fill(canvas, TextBlock.Align.LEFT, 0, 0);
    layer.setTile(canvas.toTexture());
  }
}
