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

import playn.core.Sound;
import playn.core.ResourceCallback;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;

import playn.core.Asserts;
import playn.core.PlayN;
import playn.core.Sound;
import playn.core.ResourceCallback;

class JavaSound implements Sound {

  private final String name;
  private Clip clip;
  private boolean looping;

  private List<ResourceCallback<Sound>> callbacks;

  JavaSound(String name, final InputStream inputStream) {
    this.name = name;

    JavaAssets.doResourceAction(new Runnable() {
      public void run () {
        init(inputStream);
        if (callbacks != null) {
          for (ResourceCallback<Sound> callback : callbacks) {
            callback.done(JavaSound.this);
          }
          callbacks = null;
        }
      }
    });
  }

  private void init(InputStream inputStream) {
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
      ais = AudioSystem.getAudioInputStream(inputStream);
      if (name.endsWith(".mp3")) {
        AudioFormat baseFormat = ais.getFormat();
        AudioFormat decodedFormat = new AudioFormat(
          AudioFormat.Encoding.PCM_SIGNED,
          baseFormat.getSampleRate(),
          16, // we have to force sample size to 16
          baseFormat.getChannels(),
          baseFormat.getChannels()*2,
          baseFormat.getSampleRate(),
          false // big endian
          );
        ais = AudioSystem.getAudioInputStream(decodedFormat, ais);
      }
    } catch (Exception e) {
      PlayN.log().warn("Failed to create audio stream for " + name, e);
      return; // give up
    }

    try {
      clip.open(ais);
    } catch (Exception e) {
      PlayN.log().warn("Failed to open sound " + name, e);
      return; // give up
    }
  }

  @Override
  public boolean play() {
    if (clip == null) {
      return false;
    }
    if (looping) {
      clip.loop(Clip.LOOP_CONTINUOUSLY);
    } else {
      clip.setFramePosition(0);
      clip.start();
    }
    return true;
  }

  @Override
  public void stop() {
    if (clip == null) {
      return;
    }
    clip.stop();
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
    return (clip != null) && clip.isActive();
  }

  @Override
  public void addCallback(ResourceCallback<Sound> callback) {
    if (clip != null) {
      callback.done(this);
    } else {
      if (callbacks == null) {
        callbacks = new ArrayList<ResourceCallback<Sound>>();
      }
      callbacks.add(callback);
    }
  }
}
