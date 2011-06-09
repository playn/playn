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
package forplay.java;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import forplay.core.Asserts;
import forplay.core.ForPlay;
import forplay.core.Sound;

class JavaJLayerSound implements Sound {

  private final File file;

  private Player player;

  private Thread thread;

  private Runnable runnable = new Runnable() {
    public void run() {
      try {
        player.play();
      } catch (JavaLayerException e) {
        ForPlay.log().warn("Failed to play sound file: " + file.getName(), e);
      } catch (Throwable e) {
        ForPlay.log().warn("Failed to play sound file: " + file.getName(), e);
      }
    }
  };

  public JavaJLayerSound(File file) {
    this.file = file;
    InputStream is;
    try {
      is = new FileInputStream(file);
    } catch (FileNotFoundException e) {
      ForPlay.log().warn("Failed to locate sound file: " + file.getName(), e);
      return;
    }
    try {
      player = new Player(is);
    } catch (JavaLayerException e) {
      ForPlay.log().warn("Failed to locate create player for sound file: " + file.getName(), e);
      return;
    }
    thread = new Thread(runnable);
  }

  @Override
  public boolean play() {
    if (thread == null) {
      // failure in constructor prevented thread from being created
      return false;
    }
    stop();
    if (thread.isAlive()) {
      ForPlay.log().warn(
          "Sound thread has not yet terminated. Will not play sound file: " + file.getName());
      return false;
    }
    try {
      thread.start();
      return true;
    } catch (IllegalThreadStateException ignore) {
      // thread must have just started
      return false;
    }
  }

  @Override
  public void stop() {
    // TODO implement
  }

  @Override
  public void setLooping(boolean looping) {
    if (looping) {
      ForPlay.log().info(
          "Sorry, looping not currently supported in Java. Will play sound file once: "
              + file.getName());
    }
  }

  @Override
  public void setVolume(float volume) {
    Asserts.checkArgument(0f <= volume && volume <= 1f, "Must ensure 0f <= volume <= 1f");
    // TODO implement
  }

  @Override
  public boolean isPlaying() {
    // TODO implement
    return false;
  }
}
