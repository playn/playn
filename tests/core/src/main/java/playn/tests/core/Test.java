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

import react.Closeable;
import react.Slot;

import playn.core.*;
import playn.scene.*;
import static playn.tests.core.TestsGame.game;

public abstract class Test {

  public static final int UPDATE_RATE = 25;

  public final String name;
  public final String descrip;

  protected final TestsGame game;
  protected final Closeable.Set conns = new Closeable.Set();

  public Test (TestsGame game, String name, String descrip) {
    this.game = game;
    this.name = name;
    this.descrip = descrip;
  }

  public void init() {
  }

  public void dispose() {
    conns.close();
  }

  public boolean usesPositionalInputs () {
    return false;
  }

  public boolean available () {
    return true;
  }

  protected float addTest(float lx, float ly, Layer layer, String descrip) {
    return addTest(lx, ly, layer, descrip, layer.width());
  }

  protected float addTest(float lx, float ly, Layer layer, String descrip, float twidth) {
    return addTest(lx, ly, layer, layer.width(), layer.height(), descrip, twidth);
  }

  protected float addTest(float lx, float ly, Layer layer, float lwidth, float lheight,
                          String descrip) {
    return addTest(lx, ly, layer, lwidth, lheight, descrip, lwidth);
  }

  protected float addTest(float lx, float ly, Layer layer, float lwidth, float lheight,
                          String descrip, float twidth) {
    game.rootLayer.addAt(layer, lx + (twidth-lwidth)/2, ly);
    return addDescrip(descrip, lx, ly + lheight + 5, twidth);
  }

  protected float addDescrip(String descrip, float x, float y, float width) {
    ImageLayer layer = createDescripLayer(descrip, width);
    game.rootLayer.addAt(layer, Math.round(x + (width - layer.width())/2), y);
    return y + layer.height();
  }

  protected ImageLayer createDescripLayer(String descrip, float width) {
    return new ImageLayer(game.ui.wrapText(descrip, width, TextBlock.Align.CENTER));
  }

  protected float addButton (String text, Runnable onClick, float x, float y) {
    ImageLayer button = game.ui.createButton(text, onClick);
    game.rootLayer.addAt(button, x, y);
    return x + button.width() + 10;
  }

  protected QuadBatch createSepiaBatch() {
    return new TriangleBatch(game.graphics.gl, new TriangleBatch.Source() {
      @Override protected String textureTint () {
        return super.textureTint() +
          "  float grey = dot(textureColor.rgb, vec3(0.299, 0.587, 0.114));\n" +
          "  textureColor = vec4(grey * vec3(1.2, 1.0, 0.8), textureColor.a);\n";
      }
    });
  }

  protected Slot<Throwable> logFailure (final String message) {
    return new Slot<Throwable>() {
      public void onEmit (Throwable cause) {
        game.log.warn(message, cause);
      }
    };
  }
}
