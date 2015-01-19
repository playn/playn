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
package playn.java;

import org.junit.Test;
import static org.junit.Assert.*;

import playn.core.Log;
import playn.core.Image;

/**
 * Tests various JavaImage behavior.
 */
public class JavaImageTest extends AbstractPlayNTest {

  @Test public void testMissingImage() {
    final StringBuilder buf = new StringBuilder();
    plat.log().setCollector(new Log.Collector() {
      public void logged(Log.Level level, String msg, Throwable cause) {
        buf.append(msg).append("\n");
      }
    });

    Image missing;
    try {
      missing = plat.assets().getImageSync("missing.png");
    } finally {
      plat.log().setCollector(null);
    }

    assertNotNull(missing);

    // ensure that width/height do not NPE
    missing.width();
    missing.height();

    // TODO: depending on the error text is somewhat fragile, but I want to be sure that a
    // reasonably appropriate error was logged
    String errlog = buf.toString();
    assertTrue(errlog + " must contain 'Could not load image'",
               errlog.startsWith("Could not load image"));
    assertTrue(errlog + " must contain 'missing.png'",
               errlog.contains("missing.png"));
  }
}
