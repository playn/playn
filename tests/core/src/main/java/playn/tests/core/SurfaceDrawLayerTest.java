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
package playn.tests.core;

import pythagoras.f.FloatMath;

import playn.core.GroupLayer;
import playn.core.Image;
import playn.core.ImageLayer;
import playn.core.ImmediateLayer;
import playn.core.Surface;
import playn.core.SurfaceImage;
import playn.core.util.Callback;
import static playn.core.PlayN.*;

public class SurfaceDrawLayerTest extends Test {

  private ImageLayer rotator;
  private float elapsed;

  @Override
  public String getName() {
    return "SurfaceDrawLayerTest";
  }

  @Override
  public String getDescription() {
    return "Tests rendering sub-graphs into surfaces.";
  }

  @Override
  public void init() {
    assets().getImage("images/orange.png").addCallback(new Callback<Image>() {
      public void onSuccess(Image orange) {
        addTests(orange);
      }
      public void onFailure(Throwable cause) {
        log().warn("Failed to load image", cause);
      }
    });
  }

  @Override
  public void update(int delta) {
    elapsed += delta;
  }

  @Override
  public void paint(float alpha) {
    if (rotator != null) {
      float now = elapsed + alpha*UPDATE_RATE;
      rotator.setRotation(now/1000f);
    }
  }

  protected void addTests(Image orange) {
    float owidth = orange.width(), oheight = orange.height();
    float twidth = 3*owidth, theight = oheight;

    // create a small subgraph of a couple of image layers
    final GroupLayer group = graphics().createGroupLayer();
    group.addAt(graphics().createImageLayer(orange), owidth/2, 0);
    rotator = graphics().createImageLayer(orange);
    group.addAt(rotator.setOrigin(owidth/2, oheight/2), 2*owidth, oheight/2);
    rotator.setRotation(FloatMath.PI/2);

    // add the subgraph to the scene directly
    final float gap = 25;
    float x = gap, y = gap;
    graphics().rootLayer().addAt(group, x, y);
    addDescrip("Added to rootLayer", x, y+theight+gap, twidth);
    x += twidth+gap;

    // render the subgraph in an immediate layer, with a few variants
    graphics().rootLayer().addAt(graphics().createImmediateLayer(new ImmediateLayer.Renderer() {
      public void render (Surface surf) {
        // since the group layer has a +gap+gap offset, we adjust for that here
        surf.translate(-gap, -gap).drawLayer(group);
      }
    }), x, y);
    addDescrip("ImmediateLayer drawLayer", x, y+theight+gap, twidth);
    x += twidth+gap;

    graphics().rootLayer().addAt(graphics().createImmediateLayer(new ImmediateLayer.Renderer() {
      public void render (Surface surf) {
        surf.translate(-gap, -gap).drawLayer(group);
      }
    }).setTint(0xFFDD0000), x, y);
    addDescrip("ImmediateLayer drawLayer (with tint)", x, y+theight+gap, twidth);
    x += twidth+gap;

    graphics().rootLayer().addAt(graphics().createImmediateLayer(new ImmediateLayer.Renderer() {
      public void render (Surface surf) {
        surf.translate(-gap, -gap).drawLayer(group);
      }
    }).setShader(createSepiaShader()), x, y);
    addDescrip("ImmediateLayer drawLayer (with sepia shader)", x, y+theight+gap, twidth);
    x += twidth+gap;

    // lastly create a surface image and render the layer thereinto; this one won't animate
    SurfaceImage image = graphics().createSurface(twidth, theight);
    image.surface().translate(-gap, -gap).drawLayer(group);
    graphics().rootLayer().addAt(graphics().createImageLayer(image), x, y);
    addDescrip("SurfaceImage drawLayer (won't animate)", x, y+theight+gap, twidth);
    x += twidth+gap;
  }
}
