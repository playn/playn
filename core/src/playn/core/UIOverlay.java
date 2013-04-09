package playn.core;

import pythagoras.f.IRectangle;

/**
 * Platforms that support native widgets need to put those widgets in a native container above
 * the PlayN GL layers. This interface exists to ease the delivery of that container to other
 * platform-specific bits, and to offer some common functionality to non-platform-specific bits.
 */
public interface UIOverlay
{
  /**
   * Returns true if the underlying platform has an overlay container for native widgets.
   */
  boolean hasOverlay();

  /**
   * A method to mask out the overlay over the given area. Pointer events should not interact with
   * the overlay within this area and native widgets should be invisible within this area,
   * allowing the PlayN layers under it to be visible.
   */
  void hideOverlay(IRectangle area);
}
