/**
 * Copyright 2010 The ForPlay Authors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package forplay.bench.core;

import static forplay.core.ForPlay.*;

import forplay.core.GroupLayer;
import forplay.core.Image;
import forplay.core.Surface;
import forplay.core.SurfaceLayer;

import java.util.ArrayList;
import java.util.List;

class SurfaceTimeTest extends TimeTest {

  static class Entity {
    private final Image image;
    private float x, y, scale, rotation;

    public Entity(Image image) {
      this.image = image;
      this.scale = random() + 0.5f;
    }
  }

  private SurfaceLayer surfLayer;
  private Image background;
  private List<Entity> ents = new ArrayList<Entity>();
  private Image[] images;

  @Override
  protected void advance() {
    int image = (int) (random() * images.length);
    Entity ent = new Entity(images[image]);
    ents.add(ent);

    int w = graphics().width(), h = graphics().height();
    ent.x = random() * w;
    ent.y = random() * h;
    ent.rotation = random() * (float) Math.PI * 2;
  }

  @Override
  protected int count() {
    return ents.size();
  }

  @Override
  protected void retreat() {
    if (ents.size() == 0) {
      // TODO(jgw): What can we do about this case?
      return;
    }

    ents.remove(ents.size() - 1);
  }

  @Override
  void init(GroupLayer root) {
    background = assetManager().getImage("images/background.png");

    images = new Image[LayerTimeTest.IMAGES.length];
    for (int i = 0; i < images.length; ++i) {
      images[i] = assetManager().getImage("images/" + LayerTimeTest.IMAGES[i] + ".png");
    }

    surfLayer = graphics().createSurfaceLayer(graphics().width(), graphics().height());
    root.add(surfLayer);
  }

  @Override
  String name() {
    return "Surface";
  }

  @Override
  void cleanup() {
  }

  @Override
  double score() {
    return ents.size();
  }

  @Override
  protected void doPaint() {
    Surface surf = surfLayer.surface();
    surf.drawImage(background, 0, 0);
    for (Entity ent : ents) {
      surf.save();
      {
        surf.translate(ent.x, ent.y);
        surf.scale(ent.scale, ent.scale);
        surf.rotate(ent.rotation);
        surf.drawImage(ent.image, 0, 0);
      }
      surf.restore();

      ent.rotation += (float) Math.PI * 0.01;
    }
  }
}
