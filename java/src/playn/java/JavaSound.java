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
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

import pythagoras.f.FloatMath;
import pythagoras.f.MathUtil;

import playn.core.PlayN;
import playn.core.Sound;
import playn.core.util.Callback;
import playn.core.util.Callbacks;

class JavaSound implements Sound {

  private Clip clip;
  private boolean looping;

  private List<Callback<? super Sound>> callbacks;

  JavaSound(final String name, final InputStream inputStream) {
    JavaAssets.doResourceAction(new Runnable() {
      public void run () {
        try {
          init(name, inputStream);
          callbacks = Callbacks.dispatchSuccessClear(callbacks, JavaSound.this);
        } catch (Exception e) {
          PlayN.log().warn("Sound initialization failed '" + name + "': " + e);
          callbacks = Callbacks.dispatchFailureClear(callbacks, e);
        }
      }
    });
  }

  private void init(String name, InputStream inputStream) throws Exception {
    clip = AudioSystem.getClip();
    AudioInputStream ais = AudioSystem.getAudioInputStream(inputStream);
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
    clip.open(ais);
  }

  @Override
  public boolean play() {
    if (clip == null) {
      return false;
    }
    clip.setFramePosition(0);
    if (looping) {
      clip.loop(Clip.LOOP_CONTINUOUSLY);
    } else {
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
  public float volume() {
    FloatControl volctrl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
    return toVolume(volctrl.getValue());
  }

  @Override
  public void setVolume(float volume) {
    FloatControl volctrl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
    volctrl.setValue(toGain(MathUtil.clamp(volume, 0, 1)));
  }

  @Override
  public boolean isPlaying() {
    return (clip != null) && clip.isActive();
  }

  @Override
  public void addCallback(Callback<? super Sound> callback) {
    if (clip != null) {
      callback.onSuccess(this);
    } else {
      callbacks = Callbacks.createAdd(callbacks, callback);
    }
  }

  protected void finalize() {
    if (clip != null) {
      clip.close();
      clip = null;
    }
  }

  protected static float toVolume (float gain) {
    return FloatMath.pow(10, gain/20);
  }

  protected static float toGain (float volume) {
    return 20 * FloatMath.log10(volume);
  }
}
