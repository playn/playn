//
// $Id$

package forplay.java;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import forplay.core.ForPlay;
import forplay.core.Image;
import forplay.tests.AbstractForPlayTest;

/**
 * Tests various JavaImage behavior.
 */
public class JavaImageTest extends AbstractForPlayTest {

  @Test
  public void testMissingImage() {
    Image missing = ForPlay.assetManager().getImage("missing.png");
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
    stockOut = System.out;
    stockErr = System.err;
    System.setOut(new PrintStream(capOut = new ByteArrayOutputStream()));
    System.setErr(new PrintStream(capErr = new ByteArrayOutputStream()));
  }

  @After
  public void restoreStdOutErr() {
    System.setOut(stockOut);
    System.setErr(stockErr);
    capOut = capErr = null;
  }

  private PrintStream stockOut, stockErr;
  private ByteArrayOutputStream capOut, capErr;
}
