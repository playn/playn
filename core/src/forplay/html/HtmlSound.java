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
package forplay.html;

import com.allen_sauer.gwt.voices.client.handler.PlaybackCompleteEvent;
import com.allen_sauer.gwt.voices.client.handler.SoundHandler;
import com.allen_sauer.gwt.voices.client.handler.SoundLoadStateChangeEvent;

import forplay.core.Asserts;
import forplay.core.Sound;

class HtmlSound implements Sound {

  private final com.allen_sauer.gwt.voices.client.Sound sound;
  private boolean playing;

  HtmlSound(com.allen_sauer.gwt.voices.client.Sound sound) {
    this.sound = sound;
    sound.addEventHandler(new SoundHandler() {
      @Override
      public void onSoundLoadStateChange(SoundLoadStateChangeEvent event) {
      }

      @Override
      public void onPlaybackComplete(PlaybackCompleteEvent event) {
        playing = false;
      }
    });
  }

  @Override
  public boolean play() {
    playing = true;
    return sound.play();
  }

  @Override
  public void stop() {
    sound.stop();
    playing = false;
  }

  @Override
  public void setLooping(boolean looping) {
    sound.setLooping(looping);
  }

  @Override
  public void setVolume(float volume) {
    Asserts.checkArgument(0f <= volume && volume <= 1f, "Must ensure 0f <= volume <= 1f");
    sound.setVolume((int) (volume * 100));
  }

  @Override
  public boolean isPlaying() {
    return playing;
  }
}
