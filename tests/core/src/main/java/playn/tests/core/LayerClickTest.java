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
package playn.tests.core;

import pythagoras.f.FloatMath;
import pythagoras.f.Vector;

import playn.core.Image;
import playn.core.ImageLayer;
import playn.core.Touch;
import playn.core.Layer;
import playn.core.Pointer;
import static playn.core.PlayN.*;

class LayerClickTest extends Test {

  @Override
  public String getName() {
    return "LayerClickTest";
  }

  @Override
  public String getDescription() {
    return "Tests the hit testing and click/touch processing provided for layers.";
  }

  @Override
  public void init() {
    Image orange = assets().getImage("images/orange.png");
    Image mdb = assets().getRemoteImage("https://graph.facebook.com/samskivert/picture");

    final ImageLayer layer1 = graphics().createImageLayer(orange);
    layer1.setScale(2);
    layer1.setRotation(FloatMath.PI/8);
    layer1.setTranslation(50, 50);
    graphics().rootLayer().add(layer1);
    if (touch().hasTouch()) {
      layer1.addListener((Touch.LayerListener)new Mover(layer1));
    } else {
      layer1.addListener((Pointer.Listener)new Mover(layer1));
    }

    final ImageLayer layer2 = graphics().createImageLayer(orange);
    layer2.setScale(1.5f);
    layer2.setRotation(FloatMath.PI/4);
    layer2.setTranslation(150, 50);
    graphics().rootLayer().add(layer2);
    if (touch().hasTouch()) {
      layer2.addListener((Touch.LayerListener)new Mover(layer2));
    } else {
      layer2.addListener((Pointer.Listener)new Mover(layer2));
    }

    final ImageLayer layer3 = graphics().createImageLayer(mdb);
    layer3.setRotation(-FloatMath.PI/4);
    layer3.setTranslation(50, 150);
    graphics().rootLayer().add(layer3);
    if (touch().hasTouch()) {
      layer3.addListener((Touch.LayerListener)new Mover(layer3));
    } else {
      layer3.addListener((Pointer.Listener)new Mover(layer3));
    }
  }

  protected static class Mover implements Pointer.Listener, Touch.LayerListener {
    private final Layer layer;

    public Mover (Layer layer) {
      this.layer = layer;
    }

    public void onTouchStart(Touch.Event event) {
      onStart(event.x(), event.y());
    }
    public void onTouchMove(Touch.Event event) {
      onMove(event.x(), event.y());
    }
    public void onTouchEnd(Touch.Event event) {
      // nada
    }
    public void onTouchCancel(Touch.Event event) {
      // nada
    }

    public void onPointerStart(Pointer.Event event) {
      onStart(event.x(), event.y());
    }
    public void onPointerDrag(Pointer.Event event) {
      onMove(event.x(), event.y());
    }
    public void onPointerEnd(Pointer.Event event) {
      // nada
    }
    public void onPointerCancel(Pointer.Event event) {
      // nada
    }

    protected void onStart(float x, float y) {
      _lstart = layer.transform().translation();
      _pstart = new Vector(x, y);
    }
    protected void onMove(float x, float y) {
      Vector delta = new Vector(x, y).subtractLocal(_pstart);
      layer.setTranslation(_lstart.x + delta.x, _lstart.y + delta.y);
    }

    protected Vector _lstart, _pstart;
  }
}
