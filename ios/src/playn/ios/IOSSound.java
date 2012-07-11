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

import cli.MonoTouch.AVFoundation.AVAudioPlayer;

import playn.core.ResourceCallback;
import playn.core.Sound;

/**
 * An implementation of Sound using the AVAudioPlayer.
 */
public class IOSSound implements Sound {

  private final AVAudioPlayer player;

  public IOSSound (AVAudioPlayer player) {
    this.player = player;
    this.player.PrepareToPlay();
  }

  @Override
  public boolean play() {
    player.set_CurrentTime(0);
    return player.Play();
  }

  @Override
  public void stop() {
    player.Pause();
    player.set_CurrentTime(0);
  }

  @Override
  public void setLooping(boolean looping) {
    player.set_NumberOfLoops(looping ? -1 : 0);
  }

  @Override
  public void setVolume(float volume) {
    player.set_Volume(volume);
  }

  @Override
  public boolean isPlaying() {
    return player.get_Playing();
  }

  @Override
  public void addCallback(ResourceCallback<? super Sound> callback) {
    callback.done(this);
  }

  protected void finalize() {
    player.Dispose(); // meh
  }
}
