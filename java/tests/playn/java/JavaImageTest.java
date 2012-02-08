//
// $Id$

package playn.java;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import playn.core.PlayN;
import playn.core.Image;
import playn.tests.AbstractPlayNTest;

/**
 * Tests various JavaImage behavior.
 */
public class JavaImageTest extends AbstractPlayNTest {

  @Test
  public void testMissingImage() {
    Image missing = PlayN.assets().getImage("missing.png");
    assertNotNull(missing);

    // ensure that width/height do not NPE
    missing.width();
    missing.height();

    // TODO: depending on the error text is somewhat fragile, but I want to be sure that a
    // reasonably appropriate error was logged
    String errlog = capErr.toString();
    assertTrue(errlog.startsWith("Could not load image"));
    assertTrue(errlog.contains("missing.png"));
  }

  @Before
  public void captureStdOutErr() {
    stockErr = System.err;
    System.setErr(new PrintStream(capErr = new ByteArrayOutputStream()));
  }

  @After
  public void restoreStdOutErr() {
    System.setErr(stockErr);
    capErr = null;
  }

  private PrintStream stockErr;
  private ByteArrayOutputStream capErr;
}
