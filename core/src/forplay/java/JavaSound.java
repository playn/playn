/**
 * Copyright 2011 The ForPlay Authors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package forplay.java;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineEvent.Type;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import forplay.core.Asserts;
import forplay.core.ForPlay;
import forplay.core.Sound;

class JavaSound implements Sound {

  private Clip clip;
  private boolean looping;
  private boolean playing;
  private InputStream inputStream;
  private final File file;

  public JavaSound(File file) {
    this.file = file;

    try {
      clip = AudioSystem.getClip();
    } catch (LineUnavailableException e) {
      ForPlay.log().warn("Unable to create clip for " + file);
      // give up
      return;
    } catch (IllegalArgumentException e) {
      /*
       * OpenJDK on Linux may throw java.lang.IllegalArgumentException: No line matching interface
       * Clip supporting format PCM_SIGNED unknown sample rate, 16 bit, stereo, 4 bytes/frame,
       * big-endian is supported.
       */
      ForPlay.log().info("Failed to load sound " + file + " due to " + e.toString());
      // give up
      return;
    }

    clip.addLineListener(new LineListener() {
      @Override
      public void update(LineEvent event) {
        Type type = event.getType();
        if (LineEvent.Type.STOP == type) {
          // ForPlay.log().info("STOP EVENT");
          try {
            clip.close();
          } finally {
            playing = false;
          }
        }
      }
    });

  }

  @Override
  public boolean play() {
    // ForPlay.log().info("play()");
    if (playing) {
      // we have not yet received LineEvent.Type.STOP
      return false;
    }
    if (clip == null) {
      // no audio clip to play
      return false;
    }
    if (clip.isActive()) {
      // already playing
      return false;
    }

    try {
      inputStream = new FileInputStream(file);
    } catch (FileNotFoundException e) {
      ForPlay.log().warn("Sound file not found " + file);
      // give up
      return false;
    }

    AudioInputStream ais;
    try {
      // ForPlay.log().info("calling AudioSystem.getAudioInputStream()");
      ais = AudioSystem.getAudioInputStream(inputStream);
    } catch (UnsupportedAudioFileException e) {
      ForPlay.log().warn(
          "Failed to play sound " + file + " due to failure to get audio stream caused by "
          + e.toString(), e);
      return false;
    } catch (IOException e) {
      ForPlay.log().warn(
          "Failed to play sound " + file + " due to failure to get audio stream caused by "
          + e.toString(), e);
      return false;
    }
    try {
      // ForPlay.log().info("calling clip.open()");
      clip.open(ais);
    } catch (IOException e) {
      ForPlay.log().warn(
          "Failed to play sound " + file + " due to failure to open clip caused by " + e.toString(),
          e);
      return false;
    } catch (LineUnavailableException e) {
      ForPlay.log().info(
          "Not playing sound " + file + " due to failure to open clip caused by " + e.toString());
    } catch (IllegalArgumentException e) {
      ForPlay.log().info(
          "Not playing sound " + file + " due to failure to open clip caused by " + e.toString());
      return false;
    } catch (IllegalStateException e) {
      // Line may already be open
      // TODO(fredsa): figure out why this happens
      ForPlay.log().info(
          "Not playing sound " + file + " due to failure to open clip caused by " + e.toString());
      return false;
    }
    if (looping) {
      clip.loop(Clip.LOOP_CONTINUOUSLY);
    }
    // ForPlay.log().info("calling clip.start()");
    clip.start();
    playing = true;
    return true;
  }

  @Override
  public void stop() {
    // ForPlay.log().info("stop()");
    if (clip == null) {
      // no audio clip to stop
      return;
    }
    if (!clip.isActive()) {
      // clip is not playing
      return;
    }
    // ForPlay.log().info("Calling clip.stop()");
    clip.stop();
    // ForPlay.log().info("Calling clip.flush()");
    clip.flush();
  }

  @Override
  public void setLooping(boolean looping) {
    this.looping = looping;
  }

  @Override
  public void setVolume(float volume) {
    Asserts.checkArgument(0f <= volume && volume <= 1f, "Must ensure 0f <= volume <= 1f");
    // TODO implement
  }

  @Override
  public boolean isPlaying() {
    return playing;
  }
}
