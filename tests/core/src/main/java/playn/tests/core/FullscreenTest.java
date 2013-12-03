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

import playn.core.ImageLayer;

import static playn.core.PlayN.*;

public class FullscreenTest extends Test {
  private static Host host;

  public static void setHost (Host host) {
    FullscreenTest.host = host;
  }

  public static class Mode {
    public int width, height, depth;

    @Override public String toString () {
      return "" + width + "x" + height + "x" + depth;
    }
  }

  public interface Host {
    Mode[] enumerateModes();
    void setMode (Mode mode);
  }

  @Override public void init () {
    final float spacing = 5;
    float y = spacing, x = spacing, nextX = spacing;
    for (final Mode mode : host.enumerateModes()) {
      ImageLayer button = createButton(mode.toString(), new Runnable() {
        @Override public void run () {
          host.setMode(mode);
        }
      });
      graphics().rootLayer().add(button);
      if (y + button.height() + spacing >= graphics().height()) {
        x = nextX + spacing;
        y = spacing;
      }
      button.setTranslation(x, y);
      y += button.height() + spacing;
      nextX = Math.max(nextX, x + button.width());
    }
  }

  @Override public boolean available () {
    return host != null;
  }

  @Override public String getName () {
    return "Full Screen";
  }

  @Override public String getDescription () {
    return "Tests support for full screen modes";
  }
}
