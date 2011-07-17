/**
 * Copyright 2011 The ForPlay Authors
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
package forplay.test.manualtests.core;

import static forplay.core.ForPlay.*;

import forplay.core.Color;
import forplay.core.Game;
import forplay.core.Mouse;
import forplay.core.SurfaceLayer;

public class ManualTestsGame implements Game, Mouse.Listener {
  ManualTest[] tests = new ManualTest[] {new ImageTypeTest(), new AlphaLayerTest(), /*, new YourTest()*/};
  int currentTest;

  @Override
  public void init() {
    // display basic instructions
    log().info("Manual Tests. Right click to go to the next test.");

    // add a listener for pointer (mouse, touch) input
    mouse().setListener(this);

    currentTest = -1;
    nextTest();
  }

  ManualTest currentTest() {
    return tests[currentTest];
  }

  void nextTest() {
    currentTest = (currentTest + 1) % tests.length;
    // setup root layer for next test
    graphics().rootLayer().clear();
    SurfaceLayer bg = graphics().createSurfaceLayer(graphics().width(), graphics().height());
    bg.surface().setFillColor(Color.rgb(255, 255, 255));
    bg.surface().fillRect(0, 0, bg.surface().width(), bg.surface().height());
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

  @Override
  public void onMouseDown(float x, float y, int button) {
    if (button == 2) {
      nextTest();
      return;
    }
    currentTest().onMouseDown(x, y, button);
  }

  @Override
  public void onMouseUp(float x, float y, int button) {
    currentTest().onMouseUp(x, y, button);
  }

  @Override
  public void onMouseMove(float x, float y) {
    currentTest().onMouseMove(x, y);
  }

  @Override
  public void onMouseWheelScroll(float velocity) {
    currentTest().onMouseWheelScroll(velocity);
  }
}
