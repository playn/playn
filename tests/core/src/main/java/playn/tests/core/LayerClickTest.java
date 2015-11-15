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
import react.Slot;

import playn.core.*;
import playn.scene.*;
import playn.scene.Pointer;
import playn.scene.Touch;

class LayerClickTest extends Test {

  public LayerClickTest (TestsGame game) {
    super(game, "LayerClick",
          "Tests the hit testing and click/touch processing provided for layers.");
  }

  @Override public void init() {
    Image orange = game.assets.getImage("images/orange.png");

    ImageLayer l1 = new ImageLayer(orange);
    game.rootLayer.addAt(l1.setScale(2).setRotation(FloatMath.PI/8), 50, 50);
    l1.events().connect(new Mover(l1).listener(game.input));

    ImageLayer l2 = new ImageLayer(orange);
    game.rootLayer.addAt(l2.setScale(1.5f).setRotation(FloatMath.PI/4), 150, 50);
    l2.events().connect(new Mover(l2).listener(game.input));

    Image mdb = game.assets.getRemoteImage("https://graph.facebook.com/10153516625660820/picture");
    final ImageLayer l3 = new ImageLayer(mdb);
    game.rootLayer.addAt(l3.setRotation(-FloatMath.PI/4), 50, 150);
    l3.events().connect(new Mover(l3).listener(game.input));
  }

  protected static class Mover {
    private final Layer layer;

    public Mover (Layer layer) {
      this.layer = layer;
    }

    public Slot<Object> listener (Input input) {
      return input.hasTouch() ? touch() : pointer();
    }

    public Pointer.Listener pointer () {
      return new Pointer.Listener() {
        public void onStart(Pointer.Interaction event) {
          doStart(event.x(), event.y());
        }
        public void onDrag(Pointer.Interaction event) {
          doMove(event.x(), event.y());
        }
      };
    }

    public Touch.Listener touch () {
      return new Touch.Listener() {
        public void onStart(Touch.Interaction event) {
          doStart(event.x(), event.y());
        }
        public void onMove(Touch.Interaction event) {
          doMove(event.x(), event.y());
        }
      };
    }

    protected void doStart(float x, float y) {
      _lstart = layer.transform().translation();
      _pstart = new Vector(x, y);
    }
    protected void doMove(float x, float y) {
      Vector delta = new Vector(x, y).subtractLocal(_pstart);
      layer.setTranslation(_lstart.x + delta.x, _lstart.y + delta.y);
    }

    protected Vector _lstart, _pstart;
  }
}
