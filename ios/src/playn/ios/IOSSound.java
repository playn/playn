/**
 * Copyright 2012 The PlayN Authors
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

import java.util.List;

import cli.MonoTouch.AVFoundation.AVAudioPlayer;

import playn.core.Sound;
import playn.core.util.Callback;
import playn.core.util.Callbacks;

/**
 * An implementation of Sound using the AVAudioPlayer.
 */
public class IOSSound implements Sound {

  private List<Callback<? super Sound>> callbacks;
  private AVAudioPlayer player;
  private boolean playOnLoad;
  private Throwable error;

  // @Override
  // public boolean prepare() {
  //   if (player == null) return false;
  //   return player.PrepareToPlay();
  // }

  @Override
  public boolean play() {
    if (player == null) {
      playOnLoad = true;
      return false;
    }
    player.set_CurrentTime(0);
    return player.Play();
  }

  @Override
  public void stop() {
    if (player == null) {
      playOnLoad = false;
      return;
    }
    player.Stop();
    player.set_CurrentTime(0);
  }

  @Override
  public void setLooping(boolean looping) {
    if (player == null) return;
    player.set_NumberOfLoops(looping ? -1 : 0);
  }

  @Override
  public float volume() {
    return (player == null) ? 0 : player.get_Volume();
  }

  @Override
  public void setVolume(float volume) {
    if (player == null) return;
    player.set_Volume(volume);
  }

  @Override
  public boolean isPlaying() {
    return (player == null) ? false : player.get_Playing();
  }

  @Override
  public void addCallback(Callback<? super Sound> callback) {
    if (player != null)
      callback.onSuccess(this);
    else if (error != null)
      callback.onFailure(error);
    else
      callbacks = Callbacks.createAdd(callbacks, callback);
  }

  void setPlayer(AVAudioPlayer player) {
    this.player = player;
    if (playOnLoad) {
      playOnLoad = false;
      play();
    }
    callbacks = Callbacks.dispatchSuccessClear(callbacks, this);
  }

  void setError(Throwable error) {
    this.error = error;
    callbacks = Callbacks.dispatchFailureClear(callbacks, error);
  }

  protected void finalize() {
    if (player != null)
      player.Dispose(); // meh
  }
}
