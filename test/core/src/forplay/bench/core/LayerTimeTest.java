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
import forplay.core.ImageLayer;

import java.util.ArrayList;
import java.util.List;

class LayerTimeTest extends TimeTest {

  static final String[] IMAGES = new String[] {
    "boy", "bug", "gemblue", "gemgreen", "gemorange", "girlcat", "girlhorn", "girlpink",
    "girlprincess", "heart", "rock", "star", "treeshort"
  };

  private ImageLayer bgLayer;
  private List<ImageLayer> layers = new ArrayList<ImageLayer>();
  private Image[] images;
  private GroupLayer root;

  @Override
  protected void advance() {
    int image = (int) (random() * images.length);
    ImageLayer layer = graphics().createImageLayer(images[image]);
    layers.add(layer);
    root.add(layer);

    int w = graphics().width(), h = graphics().height();
    layer.transform().translate(random() * w, random() * h);
    float scale = random() + 1.0f;
    layer.transform().scale(scale, scale);
    layer.transform().rotate(random() * (float) Math.PI * 2);
  }

  @Override
  protected int count() {
    return layers.size();
  }

  @Override
  protected void retreat() {
    if (layers.size() == 0) {
      // TODO(jgw): What can we do about this case?
      return;
    }

    ImageLayer layer = layers.get(layers.size() - 1);
    layers.remove(layers.size() - 1);
    root.remove(layer);
  }

  @Override
  void init(GroupLayer root) {
    this.root = root;

    Image bg = assetManager().getImage("images/background.png");

    images = new Image[IMAGES.length];
    for (int i = 0; i < IMAGES.length; ++i) {
      images[i] = assetManager().getImage("images/" + IMAGES[i] + ".png");
    }

    bgLayer = graphics().createImageLayer(bg);
    root.add(bgLayer);
  }

  @Override
  String name() {
    return "Layers";
  }

  @Override
  void cleanup() {
  }

  @Override
  double score() {
    return layers.size();
  }

  @Override
  protected void doPaint() {
    for (ImageLayer p : layers) {
      p.transform().rotate(0.01f * (float) Math.PI);
    }
  }
}
