package playn.core;

import pythagoras.f.IRectangle;

/**
 * A NOOP touch service for use on platforms that don't support native overlay widgets
 */
public class UIOverlayStub implements UIOverlay {
  @Override
  public boolean hasOverlay() {
    return false;
  }

  @Override
  public void hideOverlay(IRectangle area) {
  }
}
