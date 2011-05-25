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
package forplay.bench.core;

import static forplay.core.ForPlay.*;

import forplay.core.Game;
import forplay.core.GroupLayer;
import forplay.core.Keyboard;
import forplay.core.Pointer;

public class Bench implements Game, Keyboard.Listener, Pointer.Listener {

  private static final TimeTest[] TESTS = new TimeTest[] {
    new SurfaceTimeTest(),
    new LayerTimeTest(),
  };

  private GroupLayer benchLayer;
  private int curTextIndex = -1;
  private TimeTest curTest;

  @Override
  public void init() {
    graphics().setSize(800, 600);
    nextTest();
  }

  @Override
  public void update(float delta) {
  }

  @Override
  public void paint(float alpha) {
    if (curTest != null) {
      curTest.paint();
      if (curTest.done()) {
        nextTest();
      }
    }
  }

  @Override
  public void onPointerStart(float x, float y) {
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

  private void cleanup() {
    if (curTest != null) {
      curTest.cleanup();
      graphics().rootLayer().remove(benchLayer);
    }
    benchLayer = graphics().createGroupLayer();
    graphics().rootLayer().add(benchLayer);
  }

  private void nextTest() {
    cleanup();

    if (++curTextIndex == TESTS.length) {
      for (TimeTest test : TESTS) {
        log().info(test.getClass().getName() + " : " + test.score());
      }
      curTest = null;
      return;
    }

    curTest = TESTS[curTextIndex];
    curTest.init(benchLayer);
  }
}
