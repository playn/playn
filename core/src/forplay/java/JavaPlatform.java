/**
 * Copyright 2010 The ForPlay Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package forplay.java;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JFrame;

import forplay.core.Analytics;
import forplay.core.Audio;
import forplay.core.ForPlay;
import forplay.core.Game;
import forplay.core.Json;
import forplay.core.Keyboard;
import forplay.core.Log;
import forplay.core.Net;
import forplay.core.Platform;
import forplay.core.Pointer;
import forplay.core.Mouse;
import forplay.core.Touch;
import forplay.core.Storage;
import forplay.core.RegularExpression;

public class JavaPlatform implements Platform {
  // Maximum delta time to consider between update() calls (in milliseconds). If the delta between
  // two update()s is greater than MAX_DELTA, we clamp to MAX_DELTA.
  private static final float MAX_DELTA = 100;

  // Minimum time between any two paint() calls (in milliseconds). We will paint every
  // FRAME_TIME ms, which is equivalent to (1000 * 1 / FRAME_TIME) frames per second.
  // TODO(pdr): this is set ridiculously low because we're using Java's software renderer which
  // causes the paint loop to be quite slow. Setting this to 10 prevents hitching that occurs when
  // we try to squeeze a paint() near max bound of FRAME_TIME.
  private static final float FRAME_TIME = 10;

  public static JavaPlatform register() {
    JavaPlatform platform = new JavaPlatform();
    ForPlay.setPlatform(platform);
    platform.init();
    return platform;
  }

  private JComponent component;
  private JFrame frame;
  private Game game;

  private JavaRegularExpression regularExpression = new JavaRegularExpression();
  private JavaAudio audio = new JavaAudio();
  private JavaGraphics graphics;
  private JavaJson json = new JavaJson();
  private JavaKeyboard keyboard;
  private JavaLog log = new JavaLog();
  private JavaNet net = new JavaNet();
  private JavaPointer pointer;
  private JavaMouse mouse;
  private JavaStorage storage = new JavaStorage();
  private JavaAssetManager assetManager = new JavaAssetManager();

  private int updateRate = 0;
  private Analytics analytics = new JavaAnalytics();

  private JavaPlatform() {
    ensureFrame();
    graphics = new JavaGraphics(frame, component);
    keyboard = new JavaKeyboard(frame);
    pointer = new JavaPointer(component);
    mouse = new JavaMouse(component);
  }

  private void init () {
    storage.init();
  }

  @Override
  public Audio audio() {
    return audio;
  }

  @Override
  public JavaGraphics graphics() {
    return graphics;
  }

  @Override
  public Json json() {
    return json;
  }

  @Override
  public Keyboard keyboard() {
    return keyboard;
  }

  @Override
  public Log log() {
    return log;
  }

  @Override
  public Net net() {
    return net;
  }

  @Override
  public Pointer pointer() {
    return pointer;
  }

  @Override
  public Mouse mouse() {
    return mouse;
  }

  @Override
  public Touch touch() {
    // TODO(pdr): need to implement this.
    throw new UnsupportedOperationException("Touch is not yet supported on the Java platform");
  }

  @Override
  public Storage storage() {
    return storage;
  }

  @Override
  public Analytics analytics() {
    return analytics;
  }
  
  @Override
  public JavaAssetManager assetManager() {
    return assetManager;
  }

  @Override
  public float random() {
    return (float) Math.random();
  }

  @Override
  public void run(final Game game) {
    this.updateRate = game.updateRate();
    this.game = game;

    game.init();
    frame.setVisible(true);
  }

  @Override
  public double time() {
    return System.currentTimeMillis();
  }

  private void ensureFrame() {
    frame = new JFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    component = new JComponent() {
      private float accum = updateRate;
      private double lastUpdateTime;
      private double lastPaintTime;
      private boolean isPaintDirty;

      @Override
      public void paint(Graphics g) {
        isPaintDirty = false; // clean by default

        if (game != null) {
          double now = time();
          float updateDelta = (float)(now - lastUpdateTime);
          if (updateDelta > 1) {
            updateDelta = updateDelta > MAX_DELTA ? MAX_DELTA : updateDelta;
            lastUpdateTime = now;

            if (updateRate == 0) {
              game.update(updateDelta);
              accum = 0;
              isPaintDirty = true; // we made a mess
            } else {
              accum += updateDelta;
              while (accum > updateRate) {
                game.update(updateRate);
                accum -= updateRate;
                isPaintDirty = true; // we made a mess
              }
            }
          }

          float paintDelta = (float)(now - lastPaintTime);
          if (isPaintDirty || paintDelta > FRAME_TIME) {
            if (updateRate == 0) {
              game.paint(0);
            } else {
              game.paint(accum / updateRate);
            }

            int width = component.getWidth();
            int height = component.getHeight();
            JavaCanvas canvas = new JavaCanvas((Graphics2D) g, width, height);
            graphics.rootLayer().paint(canvas);

            lastPaintTime = now;
          }
        }

        try {
          Thread.sleep(1L);
        } catch (InterruptedException e) {
          // ignore
        }
        repaint();
      }
    };
    component.setOpaque(true); // ensures graphics context is not cleared automatically
    frame.add(component);
    frame.setResizable(false);

    component.setPreferredSize(new Dimension(640, 480));
    frame.pack();
  }

  @Override
  public RegularExpression regularExpression() {
    return regularExpression;
  }

  @Override
  public void openURL(String url) {
    System.out.println("Opening url: " + url);
    String browser = "chrome ";
    if (System.getProperty("os.name", "-").contains("indows"))
      browser = "rundll32 url.dll,FileProtocolHandler ";
    try {
      Runtime.getRuntime().exec(browser + url);
    } catch (IOException e) {
    }
  }

  /**
   * Sets the title of the window.
   * 
   * @param title the window title
   */
  public void setTitle(String title) {
    frame.setTitle(title);
  }
}
