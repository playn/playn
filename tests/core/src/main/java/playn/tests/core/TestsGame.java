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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static playn.core.PlayN.graphics;

import playn.core.*;
import playn.core.Pointer.Event;
import static playn.core.PlayN.*;

public class TestsGame extends Game.Default {

  /** Helpful class for allowing selection of an one of a set of values for a test. */
  public static class NToggle<T> {
    public final ImageLayer layer = graphics().createImageLayer();
    public final String prefix;
    public final List<T> values = new ArrayList<T>();
    private int valueIdx;

    public NToggle(String name, T...values) {
      for (T value : values) {
        this.values.add(value);
      }
      this.prefix = name + ": ";
      layer.addListener(new Pointer.Adapter() {
        @Override
        public void onPointerStart(Event event) {
          set((valueIdx + 1) % NToggle.this.values.size());
        }
      });

      set(0);
    }

    public String toString(T value) {
      return value.toString();
    }

    public void set(int idx) {
      this.valueIdx = idx;
      layer.setImage(makeButtonImage(prefix + toString(values.get(idx))));
    }

    public T value() {
      return values.get(valueIdx);
    }

    public int valueIdx() {
      return valueIdx;
    }
  }

  public static class Toggle extends NToggle<Boolean> {
    public Toggle (String name) {
      super(name, Boolean.FALSE, Boolean.TRUE);
    }
  }

  public static Image makeButtonImage(String label) {
    TextLayout layout = graphics().layoutText(label, BUTTON_FMT);
    CanvasImage image = graphics().createImage(layout.width()+10, layout.height()+10);
    image.canvas().setFillColor(0xFFCCCCCC);
    image.canvas().fillRect(0, 0, image.width(), image.height());
    image.canvas().setFillColor(0xFF000000);
    image.canvas().fillText(layout, 5, 5);
    image.canvas().setStrokeColor(0xFF000000);
    image.canvas().strokeRect(0, 0, image.width()-1, image.height()-1);
    return image;
  }

  private Test[] tests = new Test[] {
    new CanvasTest(),
    new SurfaceTest(),
    new SurfaceDrawLayerTest(),
    new SubImageTest(),
    new ClippedGroupTest(),
    new CanvasStressTest(),
    new PauseResumeTest(),
    new ImmediateTest(),
    new TextTest(),
    new ScaledTextTest(),
    new GetTextTest(),
    new ImageTypeTest(),
    new AlphaLayerTest(),
    new ImageScalingTest(),
    new DepthTest(),
    new ClearBackgroundTest(),
    new LayerClickTest(),
    new PointerMouseTouchTest(),
    new MouseWheelTest(),
    new ShaderTest(),
    new SoundTest(),
    new NetTest(),
    new FullscreenTest(),
    /*new YourTest(),*/
  };
  private Test currentTest;

  public TestsGame () {
    super(Test.UPDATE_RATE);
  }

  @Override
  public void init() {
    // display basic instructions
    log().info("Right click, touch with two fingers, or type ESC to return to test menu.");

    // add a listener for mouse and touch inputs
    mouse().setListener(new Mouse.Adapter() {
      @Override
      public void onMouseDown(Mouse.ButtonEvent event) {
        if (currentTest != null && currentTest.usesPositionalInputs())
          return;
        if (event.button() == Mouse.BUTTON_RIGHT)
          displayMenuLater();
      }
    });
    touch().setListener(new Touch.Adapter() {
      @Override
      public void onTouchStart(Touch.Event[] touches) {
        if (currentTest != null && currentTest.usesPositionalInputs()) return;
        // Android and iOS handle touch events rather differently, so we need to do this finagling
        // to determine whether there is an active two or three finger touch
        for (Touch.Event event : touches)
          _active.add(event.id());
        if (_active.size() > 1)
          displayMenuLater();
      }
      @Override
      public void onTouchEnd(Touch.Event[] touches) {
        clearTouches(touches);
      }
      @Override
      public void onTouchCancel(Touch.Event[] touches) {
        clearTouches(touches);
      }
      protected void clearTouches(Touch.Event[] touches) {
        for (Touch.Event event : touches)
          _active.remove(event.id());
      }
      protected Set<Integer> _active = new HashSet<Integer>();
    });
    keyboard().setListener(new Keyboard.Adapter() {
      @Override
      public void onKeyDown(Keyboard.Event event) {
        if (event.key() == Key.ESCAPE || event.key() == Key.BACK)
          displayMenu();
      }
    });

    displayMenu();
    // startTest(tests[3]);
  }

  // defers display of menu by one frame to avoid the right click or touch being processed by the
  // menu when it is displayed
  void displayMenuLater() {
    invokeLater(new Runnable() {
      public void run() {
        displayMenu();
      }
    });
  }

  void displayMenu() {
    clearTest();
    clearRoot();
    GroupLayer root = graphics().rootLayer();
    root.add(createWhiteBackground());

    float gap = 20, x = gap, y = gap, maxHeight = 0;

    String info = "Renderer: ";
    if (graphics().ctx() == null) {
      info += "canvas";
    } else {
      info += "gl (quads=" + graphics().ctx().quadShaderInfo() + " tris=" +
        graphics().ctx().trisShaderInfo() + ")";
    }
    CanvasImage infoImg = Test.formatText(info, false);
    graphics().rootLayer().addAt(graphics().createImageLayer(infoImg), x, y);
    y += infoImg.height() + gap;

    for (Test test : tests) {
      if (!test.available()) {
        continue;
      }
      ImageLayer button = createButton(test);
      if (x + button.width() > graphics().width() - gap) {
        x = gap;
        y += maxHeight + gap;
        maxHeight = 0;
      }
      maxHeight = Math.max(maxHeight, button.height());
      root.addAt(button, x, y);
      x += button.width() + gap;
    }
  }

  ImageLayer createButton (final Test test) {
    ImageLayer layer = graphics().createImageLayer(makeButtonImage(test.getName()));
    layer.addListener(new Pointer.Adapter() {
      @Override public void onPointerStart(Pointer.Event event) {
        startTest(test);
      }
    });
    return layer;
  }

  void clearTest() {
    if (currentTest != null) {
      currentTest.dispose();
      currentTest = null;
    }
  }

  void startTest(Test test) {
    clearTest();
    currentTest = test;

    // setup root layer for next test
    clearRoot();

    GroupLayer root = graphics().rootLayer();
    root.add(createWhiteBackground());

    log().info("Starting " + currentTest.getName());
    log().info(" Description: " + currentTest.getDescription());
    currentTest.init();

    if (currentTest.usesPositionalInputs()) {
      // slap on a Back button if the test is testing the usual means of backing out
      ImageLayer back = Test.createButton("Back", new Runnable() {
        public void run () {
          displayMenuLater();
        }
      });
      root.addAt(back, graphics().width() - back.width(), 0);
    }
  }

  @Override
  public void paint(float alpha) {
    if (currentTest != null)
      currentTest.paint(alpha);
  }

  @Override
  public void update(int delta) {
    if (currentTest != null)
      currentTest.update(delta);
  }

  protected void clearRoot() {
    GroupLayer root = graphics().rootLayer();
    for (int ii = root.size()-1; ii >= 0; ii--) root.get(ii).destroy();
  }

  protected ImmediateLayer createWhiteBackground() {
    ImmediateLayer bg = graphics().createImmediateLayer(new ImmediateLayer.Renderer() {
      public void render(Surface surf) {
        surf.setFillColor(0xFFFFFFFF).fillRect(0, 0, graphics().width(), graphics().height());
      }
    });
    bg.setDepth(Float.NEGATIVE_INFINITY); // render behind everything
    return bg;
  }

  protected static TextFormat BUTTON_FMT = new TextFormat().withFont(
    graphics().createFont("Helvetica", Font.Style.PLAIN, 24));
}
