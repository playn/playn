/**
 * Copyright 2011 The PlayN Authors
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
package playn.html;

import static com.allen_sauer.gwt.voices.client.Sound.LoadState.LOAD_STATE_NOT_SUPPORTED;
import static com.allen_sauer.gwt.voices.client.Sound.LoadState.LOAD_STATE_SUPPORT_NOT_KNOWN;
import static com.allen_sauer.gwt.voices.client.Sound.LoadState.LOAD_STATE_SUPPORTED_AND_READY;
import static com.allen_sauer.gwt.voices.client.Sound.LoadState.LOAD_STATE_SUPPORTED_MAYBE_READY;
import static com.allen_sauer.gwt.voices.client.Sound.LoadState.LOAD_STATE_SUPPORTED_NOT_READY;
import static com.allen_sauer.gwt.voices.client.Sound.LoadState.LOAD_STATE_UNINITIALIZED;

import com.allen_sauer.gwt.voices.client.handler.PlaybackCompleteEvent;
import com.allen_sauer.gwt.voices.client.handler.SoundHandler;
import com.allen_sauer.gwt.voices.client.handler.SoundLoadStateChangeEvent;
import com.allen_sauer.gwt.voices.client.Sound.LoadState;

import playn.core.AbstractSound;
import playn.core.Asserts;

class HtmlSound extends AbstractSound {

  private final com.allen_sauer.gwt.voices.client.Sound sound;

  private boolean playing;

  HtmlSound(com.allen_sauer.gwt.voices.client.Sound sound) {
    this.sound = sound;
    sound.addEventHandler(new SoundHandler() {
        @Override
      public void onSoundLoadStateChange(SoundLoadStateChangeEvent event) {
        LoadState loadState = event.getLoadState();
        switch (loadState) {
          case LOAD_STATE_UNINITIALIZED:
          case LOAD_STATE_SUPPORTED_NOT_READY:
            // ignore
            break;
          case LOAD_STATE_SUPPORTED_AND_READY:
          case LOAD_STATE_SUPPORT_NOT_KNOWN:
          case LOAD_STATE_SUPPORTED_MAYBE_READY:
            onLoadComplete();
            break;
          case LOAD_STATE_NOT_SUPPORTED:
            onLoadError(new RuntimeException(loadState.name()));
            break;
          default:
            throw new RuntimeException("Unrecognized sound load state "
                + loadState.name());
        }
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
