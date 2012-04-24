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

    final ImageLayer layer1 = graphics().createImageLayer(orange);
    layer1.setScale(2);
    layer1.setRotation(FloatMath.PI/8);
    layer1.setTranslation(50, 50);
    graphics().rootLayer().add(layer1);
    layer1.addListener(new Pointer.Listener() {
      public void onPointerStart(Pointer.Event event) {
        _lstart = layer1.transform().translation();
        _pstart = new Vector(event.x(), event.y());
      }
      public void onPointerDrag(Pointer.Event event) {
        Vector delta = new Vector(event.x(), event.y()).subtractLocal(_pstart);
        layer1.setTranslation(_lstart.x + delta.x, _lstart.y + delta.y);
      }
      public void onPointerEnd(Pointer.Event event) {
        // nada
      }
      protected Vector _lstart, _pstart;
    });

    final ImageLayer layer2 = graphics().createImageLayer(orange);
    layer2.setScale(1.5f);
    layer2.setRotation(FloatMath.PI/4);
    layer2.setTranslation(150, 50);
    graphics().rootLayer().add(layer2);
    layer2.addListener(new Pointer.Listener() {
      public void onPointerStart(Pointer.Event event) {
        _lstart = layer2.transform().translation();
        _pstart = new Vector(event.x(), event.y());
      }
      public void onPointerDrag(Pointer.Event event) {
        Vector delta = new Vector(event.x(), event.y()).subtractLocal(_pstart);
        layer2.setTranslation(_lstart.x + delta.x, _lstart.y + delta.y);
      }
      public void onPointerEnd(Pointer.Event event) {
        // nada
      }
      protected Vector _lstart, _pstart;
    });

    final ImageLayer layer3 = graphics().createImageLayer(orange);
    layer3.setRotation(-FloatMath.PI/4);
    layer3.setTranslation(50, 150);
    graphics().rootLayer().add(layer3);
    layer3.addListener(new Pointer.Listener() {
      public void onPointerStart(Pointer.Event event) {
        _lstart = layer3.transform().translation();
        _pstart = new Vector(event.x(), event.y());
      }
      public void onPointerDrag(Pointer.Event event) {
        Vector delta = new Vector(event.x(), event.y()).subtractLocal(_pstart);
        layer3.setTranslation(_lstart.x + delta.x, _lstart.y + delta.y);
      }
      public void onPointerEnd(Pointer.Event event) {
        // nada
      }
      protected Vector _lstart, _pstart;
    });
  }
}
