/**
 * Copyright 2012 The PlayN Authors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playn.core;

import pythagoras.f.MathUtil;
import react.RPromise;

/**
 * An implementation detail. Not part of the public API.
 */
public abstract class SoundImpl<I> extends Sound {

  protected boolean playing, looping;
  protected float volume = 1;
  protected I impl;

  public SoundImpl (Exec exec) {
    super(exec.<Sound>deferredPromise());
  }

  /** Configures this sound with its platform implementation.
    * This may be called from any thread. */
  public synchronized void succeed (I impl) {
    this.impl = impl;
    setVolumeImpl(volume);
    setLoopingImpl(looping);
    if (playing) playImpl();
    ((RPromise<Sound>)state).succeed(this);
  }

  /** Configures this sound with an error in lieu of its platform implementation.
    * This may be called from any thread. */
  public synchronized void fail (Throwable error) {
    ((RPromise<Sound>)state).fail(error);
  }

  @Override
  public boolean prepare() {
    return (impl != null) ? prepareImpl() : false;
  }

  @Override
  public boolean isPlaying() {
    return (impl != null) ? playingImpl() : playing;
  }

  @Override
  public boolean play() {
    this.playing = true;
    return (impl == null) ? false : playImpl();
  }

  @Override
  public void stop() {
    this.playing = false;
    if (impl != null) stopImpl();
  }

  @Override
  public void setLooping(boolean looping) {
    this.looping = looping;
    if (impl != null) setLoopingImpl(looping);
  }

  @Override
  public float volume() {
    return volume;
  }

  @Override
  public void setVolume(float volume) {
    this.volume = MathUtil.clamp(volume, 0, 1);
    if (impl != null) setVolumeImpl(this.volume);
  }

  @Override
  public void release() {
    if (impl != null) {
      releaseImpl();
      impl = null;
    }
  }

  @Override
  protected void finalize() {
    release();
  }

  protected boolean prepareImpl() {
    return false;
  }
  protected boolean playingImpl() {
    return playing;
  }

  protected abstract boolean playImpl();
  protected abstract void stopImpl();
  protected abstract void setLoopingImpl(boolean looping);
  protected abstract void setVolumeImpl(float volume);
  protected abstract void releaseImpl();
}
