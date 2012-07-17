/**
 * Copyright 2011 The PlayN Authors
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

import playn.core.CanvasImage;
import playn.core.Font;
import playn.core.Game;
import playn.core.ImageLayer;
import playn.core.TextFormat;
import playn.core.TextLayout;
import static playn.core.PlayN.graphics;

public abstract class Test implements Game {

  public abstract String getName();

  public abstract String getDescription();

  @Override
  public void update(float delta) {
  }

  @Override
  public void paint(float alpha) {
  }

  public void dispose() {
  }

  @Override
  public int updateRate() {
    return 25;
  }

  protected ImageLayer createDescripLayer(String descrip, float width) {
    TextLayout layout = graphics().layoutText(
      descrip, new TextFormat().withFont(DESCRIP_FONT).withWrapping(
        width, TextFormat.Alignment.CENTER));
    CanvasImage image = graphics().createImage(layout.width(), layout.height());
    image.canvas().setFillColor(0xFF000000);
    image.canvas().fillText(layout, 0, 0);
    return graphics().createImageLayer(image);
  }

  protected void addDescrip(String descrip, float x, float y, float width) {
    ImageLayer layer = createDescripLayer(descrip, width);
    graphics().rootLayer().addAt(layer, Math.round(x + (width - layer.width())/2), y);
  }

  protected static Font DESCRIP_FONT = graphics().createFont("Helvetica", Font.Style.PLAIN, 12);
}
