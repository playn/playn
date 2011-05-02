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

import javax.swing.JComponent;
import javax.swing.JFrame;

import forplay.core.Storage;

import forplay.core.Audio;
import forplay.core.ForPlay;
import forplay.core.Game;
import forplay.core.Json;
import forplay.core.Keyboard;
import forplay.core.Log;
import forplay.core.Net;
import forplay.core.Platform;
import forplay.core.Pointer;
import forplay.core.RegularExpression;

public class JavaPlatform implements Platform {

  private static final float MAX_DELTA = 100;
  private static final float FRAME_TIME = 50;

  public static JavaPlatform register() {
    JavaPlatform platform = new JavaPlatform();
    ForPlay.setPlatform(platform);
    return platform;
  }

  private JComponent component;
  private JFrame frame;
  private Game game;

  private JavaRegularExpression regularExpression = new JavaRegularExpression();
  private JavaAudio audio;
  private JavaGraphics graphics;
  private JavaJson json = new JavaJson();
  private JavaKeyboard keyboard;
  private JavaLog log = new JavaLog();
  private JavaNet net = new JavaNet();
  private JavaPointer pointer;
  private JavaStorage storage;
  private JavaAssetManager assetManager = new JavaAssetManager();

  private int updateRate = 0;

  private JavaPlatform() {
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
  public Storage storage() {
    return storage;
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
    ensureFrame();

    audio = new JavaAudio();
    graphics = new JavaGraphics(frame, component);
    keyboard = new JavaKeyboard(frame);
    pointer = new JavaPointer(component);
    storage = new JavaStorage();

    game.init();

    // Don't set the game until after ensureFrame(). This keeps paint() from
    // being called early.
    this.game = game;
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
      private double lastTime;

      @Override
      public void paint(Graphics g) {
        if (game != null) {
          double now = time();
          float delta = (float)(now - lastTime);
          if (delta > MAX_DELTA) {
            delta = MAX_DELTA;
          }
          lastTime = now;

          if (updateRate == 0) {
            game.update(delta);
            accum = 0;
          } else {
            accum += delta;
            while (accum > updateRate) {
              game.update(updateRate);
              accum -= updateRate;
            }
          }

          game.paint(accum / updateRate);

          int width = component.getWidth();
          int height = component.getHeight();
          JavaCanvas canvas = new JavaCanvas((Graphics2D) g, width, height);
          graphics.rootLayer().paint(canvas);
        }

        repaint((long) FRAME_TIME);
      }
    };
    frame.add(component);
    frame.setResizable(false);

    component.setPreferredSize(new Dimension(640, 480));
    frame.pack();

    frame.setVisible(true);
  }

  @Override
  public RegularExpression regularExpression() {
    return regularExpression;
  }
}
