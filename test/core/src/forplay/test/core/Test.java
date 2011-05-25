/**
 * Copyright 2010 The ForPlay Authors
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
package forplay.test.core;

import static forplay.core.ForPlay.assetManager;
import static forplay.core.ForPlay.graphics;
import static forplay.core.ForPlay.keyboard;
import static forplay.core.ForPlay.pointer;
import forplay.core.Game;
import forplay.core.GroupLayer;
import forplay.core.Image;
import forplay.core.ImageLayer;
import forplay.core.Keyboard;
import forplay.core.Pointer;

public class Test implements Game, Keyboard.Listener, Pointer.Listener {

  GroupLayer groupLayer;
  ImageLayer bgLayer;
  ImageLayer layer0, layer1, layer2, layer3;

  @Override
  public void init() {
    pointer().setListener(this);
    keyboard().setListener(this);

    Image background = assetManager().getImage("images/background.png");
    Image catgirl = assetManager().getImage("images/girlcat.png");

    groupLayer = graphics().createGroupLayer();
    groupLayer.setOrigin(128, 128);
    groupLayer.transform().translate(256, 256);
    graphics().rootLayer().add(groupLayer);

    bgLayer = graphics().createImageLayer(background);
    bgLayer.setWidth(256);
    bgLayer.setHeight(256);
    groupLayer.add(bgLayer);

    layer0 = graphics().createImageLayer(catgirl);
    layer1 = graphics().createImageLayer(catgirl);
    layer2 = graphics().createImageLayer(catgirl);
    layer3 = graphics().createImageLayer(catgirl);

    groupLayer.add(layer0);
    groupLayer.add(layer1);
    groupLayer.add(layer2);
    groupLayer.add(layer3);

    layer0.setOrigin(50, 100);
    layer1.setOrigin(50, 100);
    layer2.setOrigin(50, 100);
    layer3.setOrigin(50, 100);

    layer0.transform().translate(0, 0);
    layer1.transform().translate(256, 0);
    layer2.transform().translate(256, 256);
    layer3.transform().translate(0, 256);
  }

  private void doStuff() {
  }

  float angle = 0;
  float scale = 1;

  @Override
  public void update(float delta) {
    angle += delta * (float) Math.PI / 5000;
    scale = (float) Math.sin(angle) + 0.5f;

    layer0.transform().setRotation(angle);
    layer1.transform().setRotation(angle);
    layer2.transform().setRotation(angle);
    layer3.transform().setRotation(angle);

    layer0.transform().setScale(scale);
    layer1.transform().setScale(scale);
    layer2.transform().setScale(scale);
    layer3.transform().setScale(scale);

    groupLayer.transform().setRotation(angle);
    groupLayer.transform().setScale(scale);
  }

  @Override
  public void paint(float alpha) {
  }

  @Override
  public void onPointerStart(float x, float y) {
    doStuff();
  }

  @Override
  public void onPointerEnd(float x, float y) {
  }

  @Override
  public void onPointerDrag(float x, float y) {
  }

  @Override
  public void onKeyDown(int keyCode) {
  }

  @Override
  public void onKeyUp(int keyCode) {
  }

  @Override
  public int updateRate() {
    return 33;
  }
}
