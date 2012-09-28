//
// $Id$

package playn.java;

import org.junit.Test;
import static org.junit.Assert.*;

import playn.core.PlayN;
import playn.core.Log;
import playn.core.Image;
import playn.tests.AbstractPlayNTest;

/**
 * Tests various JavaImage behavior.
 */
public class JavaImageTest extends AbstractPlayNTest {

  @Test
  public void testMissingImage() {
    final StringBuilder buf = new StringBuilder();
    PlayN.log().setCollector(new Log.Collector() {
      public void logged(Log.Level level, String msg, Throwable cause) {
        buf.append(msg).append("\n");
      }
    });

    Image missing;
    try {
      missing = PlayN.assets().getImageSync("missing.png");
    } finally {
      PlayN.log().setCollector(null);
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
