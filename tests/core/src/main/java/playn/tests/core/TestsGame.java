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

import playn.core.*;
import playn.scene.*;
import react.Slot;

public class TestsGame extends SceneGame<TestsGame> {

  public static TestsGame game;

  /** Helpful class for allowing selection of an one of a set of values for a test. */
  public static class NToggle<T> {
    public final ImageLayer layer = new ImageLayer();
    public final String prefix;
    public final List<T> values = new ArrayList<T>();
    private int valueIdx;

    public NToggle(String name, T...values) {
      for (T value : values) {
        this.values.add(value);
      }
      this.prefix = name + ": ";
      // layer.addListener(new Pointer.Adapter() {
      //   @Override
      //   public void onPointerStart(Event event) {
      //     set((valueIdx + 1) % NToggle.this.values.size());
      //   }
      // });

      set(0);
    }

    public String toString(T value) {
      return value.toString();
    }

    public void set(int idx) {
      this.valueIdx = idx;
      layer.setTexture(game.makeButtonTexture(prefix + toString(values.get(idx))));
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

  public Texture makeButtonTexture(String label) {
    TextLayout layout = graphics.layoutText(label, BUTTON_FMT);
    Canvas canvas = graphics.createCanvas(layout.size.width()+10, layout.size.height()+10);
    canvas.setFillColor(0xFFCCCCCC).fillRect(0, 0, canvas.width, canvas.height);
    canvas.setFillColor(0xFF000000).fillText(layout, 5, 5);
    canvas.setStrokeColor(0xFF000000).strokeRect(0, 0, canvas.width-1, canvas.height-1);
    return graphics.createTexture(canvas.image);
  }

  // args passed to the Java launcher
  public static String[] args = {};

  private Test[] tests;
  private Test currentTest;

  public final Platform plat;
  public final Assets assets;
  public final Graphics graphics;
  public final Keyboard keyboard;
  public final Log log;
  public final Mouse mouse;
  public final Net net;
  public final Pointer pointer;
  public final Storage storage;
  public final Touch touch;

  public TestsGame (Platform plat) {
    super(plat, Test.UPDATE_RATE);
    game = this;
    this.plat = plat;
    assets = plat.assets();
    graphics = plat.graphics();
    keyboard = plat.keyboard();
    log = plat.log();
    mouse = plat.mouse();
    net = plat.net();
    pointer = plat.pointer();
    storage = plat.storage();
    touch = plat.touch();
    BUTTON_FONT = graphics.createFont(new Font.Config("Helvetica", 24));
    BUTTON_FMT = new TextFormat().withFont(BUTTON_FONT);

    tests = new Test[] {
      new CanvasTest(this),
      new SurfaceTest(this),
      new SubImageTest(this),
      new ClippedGroupTest(this),
      new CanvasStressTest(this),
      new PauseResumeTest(this),
      new ImmediateTest(this),
      new TextTest(this),
      new ScaledTextTest(this),
      new GetTextTest(this),
      new ImageTypeTest(this),
      new AlphaLayerTest(this),
      new ImageScalingTest(this),
      new DepthTest(this),
      new ClearBackgroundTest(this),
      new LayerClickTest(this),
      new PointerMouseTouchTest(this),
      new MouseWheelTest(this),
      new ShaderTest(this),
      new SoundTest(this),
      new NetTest(this),
      new FullscreenTest(this),
      /*new YourTest(this),*/
    };
  }

  public SurfaceTexture createSurface (float width, float height) {
    return new SurfaceTexture(graphics, defaultBatch, width, height);
  }

  public void init() {
    // display basic instructions
    log.info("Right click, touch with two fingers, or type ESC to return to test menu.");

    // add global listeners which navigate back to the menu
    mouse.events.connect(new Mouse.ButtonSlot() {
      public void onEmit (Mouse.ButtonEvent event) {
        if (currentTest != null && currentTest.usesPositionalInputs()) return;
        if (event.button == Mouse.ButtonEvent.Id.RIGHT) displayMenuLater();
      }
    });
    touch.events.connect(new Slot<Touch.Event[]>() {
      public void onEmit (Touch.Event[] events) {
        if (currentTest != null && currentTest.usesPositionalInputs()) return;
        switch (events[0].kind) {
        case START:
          // Android and iOS handle touch events rather differently, so we need to do this
          // finagling to determine whether there is an active two or three finger touch
          for (Touch.Event event : events) _active.add(event.id);
          if (_active.size() > 1) displayMenuLater();
          break;
        case END:
        case CANCEL:
          for (Touch.Event event : events) _active.remove(event.id);
          break;
        }
      }
      protected Set<Integer> _active = new HashSet<Integer>();
    });
    keyboard.events.connect(new Keyboard.KeySlot() {
      public void onEmit (Keyboard.KeyEvent event) {
        if (event.down && (event.key == Key.ESCAPE || event.key == Key.BACK)) displayMenu();
      }
    });

    displayMenu();

    for (String arg : args) {
      if (arg.startsWith("test")) {
        startTest(tests[Integer.parseInt(arg.substring(4))]);
        break;
      }
    }
  }

  // defers display of menu by one frame to avoid the right click or touch being processed by the
  // menu when it is displayed
  void displayMenuLater() {
    plat.invokeLater(new Runnable() {
      public void run() {
        displayMenu();
      }
    });
  }

  void displayMenu() {
    clearTest();
    rootLayer.destroyAll();
    rootLayer.add(createWhiteBackground());

    float gap = 20, x = gap, y = gap, maxHeight = 0;

    String info = "Renderer: gl (batch=" + defaultBatch + ")";
    Texture infoTex = tests[0].formatText(info, false);
    rootLayer.addAt(new ImageLayer(infoTex), x, y);
    y += infoTex.displayHeight + gap;

    for (Test test : tests) {
      if (!test.available()) {
        continue;
      }
      ImageLayer button = createButton(test);
      if (x + button.width() > graphics.viewSize.width() - gap) {
        x = gap;
        y += maxHeight + gap;
        maxHeight = 0;
      }
      maxHeight = Math.max(maxHeight, button.height());
      rootLayer.addAt(button, x, y);
      x += button.width() + gap;
    }
  }

  ImageLayer createButton (final Test test) {
    ImageLayer layer = new ImageLayer(makeButtonTexture(test.getName()));
    // layer.addListener(new Pointer.Adapter() {
    //   @Override public void onPointerStart(Pointer.Event event) {
    //     startTest(test);
    //   }
    // });
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
    rootLayer.destroyAll();
    rootLayer.add(createWhiteBackground());

    log.info("Starting " + currentTest.getName());
    log.info(" Description: " + currentTest.getDescription());
    currentTest.init();

    if (currentTest.usesPositionalInputs()) {
      // slap on a Back button if the test is testing the usual means of backing out
      ImageLayer back = tests[0].createButton("Back", new Runnable() {
        public void run () {
          displayMenuLater();
        }
      });
      rootLayer.addAt(back, graphics.viewSize.width() - back.width(), 0);
    }
  }

  protected Layer createWhiteBackground() {
    Layer bg = new Layer() {
      protected void paintImpl (Surface surf) {
        surf.setFillColor(0xFFFFFFFF).fillRect(
          0, 0, graphics.viewSize.width(), graphics.viewSize.height());
      }
    };
    bg.setDepth(Float.NEGATIVE_INFINITY); // render behind everything
    return bg;
  }

  protected final Font BUTTON_FONT;
  protected final TextFormat BUTTON_FMT;
}
