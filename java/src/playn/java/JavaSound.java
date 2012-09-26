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

import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

import pythagoras.f.FloatMath;

import playn.core.AbstractSound;

class JavaSound extends AbstractSound<Clip> {

  @Override
  protected boolean playingImpl() {
    return impl.isActive();
  }

  @Override
  protected boolean playImpl() {
    impl.setFramePosition(0);
    if (looping) {
      impl.loop(Clip.LOOP_CONTINUOUSLY);
    } else {
      impl.start();
    }
    return true;
  }

  @Override
  protected void stopImpl() {
    impl.stop();
    impl.flush();
  }

  @Override
  protected void setLoopingImpl(boolean looping) {
    // nothing to do here, we pass looping to impl.play()
  }

  @Override
  protected void setVolumeImpl(float volume) {
    FloatControl volctrl = (FloatControl) impl.getControl(FloatControl.Type.MASTER_GAIN);
    volctrl.setValue(toGain(volume));
  }

  // @Override
  // public float volume() {
  //   FloatControl volctrl = (FloatControl) impl.getControl(FloatControl.Type.MASTER_GAIN);
  //   return toVolume(volctrl.getValue());
  // }

  protected void finalize() {
    if (impl != null) {
      impl.close();
      impl = null;
    }
  }

  // protected static float toVolume (float gain) {
  //   return FloatMath.pow(10, gain/20);
  // }

  protected static float toGain (float volume) {
    return 20 * FloatMath.log10(volume);
  }
}
