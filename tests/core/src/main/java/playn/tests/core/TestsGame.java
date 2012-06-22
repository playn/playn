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

import playn.core.Game;
import playn.core.ImmediateLayer;
import playn.core.Mouse;
import playn.core.Keyboard;
import playn.core.Surface;
import playn.core.Touch;
import static playn.core.PlayN.*;

public class TestsGame implements Game {
  Test[] tests = new Test[] {
    new PauseResumeTest(),
    new TextTest(),
    new SubImageTest(),
    new SurfaceTest(),
    new CanvasTest(),
    new ImmediateTest(),
    new ImageTypeTest(),
    new AlphaLayerTest(),
    new DepthTest(),
    new ClearBackgroundTest(),
    new LayerClickTest(),
    new GetTextTest(),
    new PointerMouseTouchTest(),
    new MouseWheelTest(),
    new ShaderTest(),
    new ClippedGroupTest(),
    /*new YourTest(),*/
  };
  int currentTest;

  @Override
  public void init() {
    // display basic instructions
    log().info("Right click, touch with two fingers, or type f to go to the next test.");

    // add a listener for mouse and touch inputs
    mouse().setListener(new Mouse.Adapter() {
      @Override
      public void onMouseDown(Mouse.ButtonEvent event) {
        if (event.button() == Mouse.BUTTON_RIGHT)
          advanceTest(1);
        else if (event.button() == Mouse.BUTTON_MIDDLE)
          advanceTest(-1);
      }
    });
    touch().setListener(new Touch.Adapter() {
      @Override
      public void onTouchStart(Touch.Event[] touches) {
        // android doesn't bundle multiple touches into a single event, instead we'll get a
        // separate event array with a single event with a touch with id > 0
        if (touches.length > 2 || touches[0].id() > 1)
          advanceTest(-1);
        else if (touches.length > 1 || touches[0].id() > 0)
          advanceTest(1);
      }
    });
    keyboard().setListener(new Keyboard.Adapter() {
      @Override
      public void onKeyTyped(Keyboard.TypedEvent event) {
        if (event.typedChar() == 'f')
          advanceTest(1);
        else if (event.typedChar() == 'b')
          advanceTest(-1);
      }
    });
    advanceTest(currentTest = 0);
  }

  Test currentTest() {
    return tests[currentTest];
  }

  void advanceTest(int delta) {
    currentTest = (currentTest + tests.length + delta) % tests.length;

    // setup root layer for next test
    graphics().rootLayer().clear();
    ImmediateLayer bg = graphics().createImmediateLayer(new ImmediateLayer.Renderer() {
      public void render(Surface surf) {
        surf.setFillColor(0xFFFFFFFF).fillRect(0, 0, graphics().width(), graphics().height());
      }
    });
    bg.setDepth(Float.NEGATIVE_INFINITY); // render behind everything
    graphics().rootLayer().add(bg);

    log().info("Starting " + currentTest().getName());
    log().info(" Description: " + currentTest().getDescription());
    currentTest().init();
  }

  @Override
  public void paint(float alpha) {
    currentTest().paint(alpha);
  }

  @Override
  public void update(float delta) {
    currentTest().update(delta);
  }

  @Override
  public int updateRate() {
    return currentTest().updateRate();
  }
}
