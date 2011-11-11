/**
 * Copyright 2011 The PlayN Authors
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
package playn.java;

import java.io.InputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineEvent.Type;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;

import playn.core.Asserts;
import playn.core.PlayN;
import playn.core.Sound;

class JavaSound implements Sound {

  private final String name;
  private Clip clip;
  private boolean looping;
  private boolean playing;

  public JavaSound(String name, InputStream in) {
    this.name = name;

    try {
      clip = AudioSystem.getClip();
    } catch (LineUnavailableException e) {
      PlayN.log().warn("Unable to create clip for " + name);
      return; // give up
    } catch (IllegalArgumentException e) {
      // OpenJDK on Linux may throw java.lang.IllegalArgumentException: No line matching interface
      // Clip supporting format PCM_SIGNED unknown sample rate, 16 bit, stereo, 4 bytes/frame,
      // big-endian is supported.
      PlayN.log().info("Failed to load sound " + name + " due to " + e.toString());
      return; // give up
    }

    AudioInputStream ais;
    try {
      // PlayN.log().info("calling AudioSystem.getAudioInputStream()");
      ais = AudioSystem.getAudioInputStream(in);
    } catch (Exception e) {
      PlayN.log().warn("Failed to create audio stream for " + name, e);
      return; // give up
    }

    try {
      // PlayN.log().info("calling clip.open()");
      clip.open(ais);
    } catch (Exception e) {
      PlayN.log().warn("Failed to open sound " + name, e);
      return;
    }

    clip.addLineListener(new LineListener() {
      @Override
      public void update(LineEvent event) {
        Type type = event.getType();
        if (LineEvent.Type.STOP == type) {
          // PlayN.log().info("STOP EVENT");
          playing = false;
        }
      }
    });
  }

  @Override
  public boolean play() {
    // PlayN.log().info("play()");
    if (playing ||         // we have not yet received LineEvent.Type.STOP
        clip == null ||    // we have no audio clip to play
        clip.isActive()) { // this should be caught by playing == true, but just in case...
      return false;
    }
    if (looping) {
      clip.loop(Clip.LOOP_CONTINUOUSLY);
    }
    // PlayN.log().info("calling clip.start()");
    clip.start();
    playing = true;
    return true;
  }

  @Override
  public void stop() {
    // PlayN.log().info("stop()");
    if (clip == null ||     // no audio clip to stop
        !clip.isActive()) { // clip is not playing
      return;
    }
    // PlayN.log().info("Calling clip.stop()");
    clip.stop();
    // PlayN.log().info("Calling clip.flush()");
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
