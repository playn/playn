package playn.core;


public abstract class PaddedTextLayout extends AbstractTextLayout
{
  // this is used to reserve one pixel of padding around the edge of our rendered text which makes
  // antialising work much more nicely
  protected final float pad;

  @Override
  public float width() {
    // reserve a pixel on the left and right to make antialiasing work better
    return super.width() + 2*pad;
  }

  @Override
  public float height() {
    // reserve a pixel on the top and bottom to make antialiasing work better
    return super.height() + 2*pad;
  }

  protected PaddedTextLayout (Graphics gfx, String text, TextFormat format)
  {
    super(text, format);
    this.pad = 1/gfx.scaleFactor();
  }
}
