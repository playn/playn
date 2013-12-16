/**
 * Copyright 2013 The PlayN Authors
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
package playn.ios;

import cli.OpenTK.Audio.OpenAL.AL;

import playn.core.AbstractSound;

/**
 * An implementation of Sound using OpenAL. This is used for brief sound effects.
 */
public class IOSSoundOAL extends AbstractSound<Integer> {
  // our "impl" is the OpenAL buffer ID that contains our sound data

  private final IOSAudio audio;
  private int sourceIdx;

  public IOSSoundOAL(IOSAudio audio) {
    this.audio = audio;
  }

  /** Returns the OpenAL buffer id for this sound. */
  public int bufferId() {
    return impl;
  }

  @Override
  protected boolean prepareImpl() {
    return true; // no preparation needed in OpenAL
  }

  @Override
  protected boolean playingImpl() {
    return audio.isPlaying(sourceIdx, this);
  }

  @Override
  protected boolean playImpl() {
    sourceIdx = audio.play(this, volume, looping);
    return true; // TODO
  }

  @Override
  protected void stopImpl() {
    audio.stop(sourceIdx, this);
  }

  @Override
  protected void setLoopingImpl(boolean looping) {
    audio.setLooping(sourceIdx, this, looping);
  }

  @Override
  protected void setVolumeImpl(float volume) {
    audio.setVolume(sourceIdx, this, volume);
  }

  @Override
  protected void releaseImpl() {
    AL.DeleteBuffer(impl);
  }
}
