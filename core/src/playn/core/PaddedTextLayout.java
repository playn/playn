/**
 * Copyright 2013 The PlayN Authors
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
 * Base {@link TextLayout} implementation shared among platforms that need padding.
 */
public abstract class PaddedTextLayout extends AbstractTextLayout {

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

  protected PaddedTextLayout (Graphics gfx, String text, TextFormat format) {
    super(text, format);
    this.pad = 1/gfx.scaleFactor();
  }
}
