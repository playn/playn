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
package playn.core;

/**
 * Base {@link TextLayout} implementation shared among platforms.
 */
// TODO: remove this annotation once we've nixed deprecated TextFormat bits
@SuppressWarnings("deprecation")
public abstract class AbstractTextLayout implements playn.core.TextLayout {

  protected final TextFormat format;
  // this is used to reserve one pixel of padding around the top and sides of our rendered text
  // which makes antialising work much more nicely
  protected final float pad;
  protected float width, height;

  protected interface Stamp<C> {
    void draw (C ctx, float x, float y);
  }

  @Override
  public float width() {
    // reserve a pixel on the left and right to make antialiasing work better
    return format.effect.adjustWidth(width) + 2*pad;
  }

  @Override
  public float height() {
    // reserve a pixel only on the top to make antialising work better; nearly no fonts jam up
    // against the bottom, so reserving a pixel down there just makes things look misaligned
    return format.effect.adjustHeight(height) + pad;
  }

  @Override
  public TextFormat format() {
    return format;
  }

  protected AbstractTextLayout(Graphics gfx, TextFormat format) {
    this.format = format;
    this.pad = 1/gfx.scaleFactor();
  }

  protected <C> void draw (C ctx, Stamp<C> stamp, Stamp<C> altStamp, float x, float y) {
    // account for our antialiasing padding
    x += pad;
    y += pad;

    if (format.effect instanceof TextFormat.Effect.Shadow) {
      TextFormat.Effect.Shadow seffect = (TextFormat.Effect.Shadow)format.effect;
      // if the shadow is negative, we need to move the real text down/right to keep everything
      // within our bounds
      float tx, sx, ty, sy;
      if (seffect.shadowOffsetX > 0) {
        tx = 0;
        sx = seffect.shadowOffsetX;
      } else {
        tx = -seffect.shadowOffsetX;
        sx = 0;
      }
      if (seffect.shadowOffsetY > 0) {
        ty = 0;
        sy = seffect.shadowOffsetY;
      } else {
        ty = -seffect.shadowOffsetY;
        sy = 0;
      }
      altStamp.draw(ctx, x + sx, y + sy);
      stamp.draw(ctx, x + tx, y + ty);

    } else if (format.effect instanceof TextFormat.Effect.PixelOutline) {
      altStamp.draw(ctx, x+0, y+0);
      altStamp.draw(ctx, x+0, y+1);
      altStamp.draw(ctx, x+0, y+2);
      altStamp.draw(ctx, x+1, y+0);
      altStamp.draw(ctx, x+1, y+2);
      altStamp.draw(ctx, x+2, y+0);
      altStamp.draw(ctx, x+2, y+1);
      altStamp.draw(ctx, x+2, y+2);
      stamp.draw(ctx, x+1, y+1);

    } else {
      stamp.draw(ctx, x, y);
    }
  }
}
