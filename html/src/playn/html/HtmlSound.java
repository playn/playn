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

import com.allen_sauer.gwt.voices.client.handler.PlaybackCompleteEvent;
import com.allen_sauer.gwt.voices.client.handler.SoundHandler;
import com.allen_sauer.gwt.voices.client.handler.SoundLoadStateChangeEvent;
import com.allen_sauer.gwt.voices.client.Sound.LoadState;

import com.allen_sauer.gwt.voices.client.Sound;

import playn.core.AbstractSound;

class HtmlSound extends AbstractSound<Sound> {

  HtmlSound(final Sound sound) {
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
          onLoaded(sound);
          break;
        case LOAD_STATE_NOT_SUPPORTED:
          onLoadError(new RuntimeException(loadState.name()));
          break;
        default:
          throw new RuntimeException("Unrecognized sound load state " + loadState.name());
        }
      }

      @Override
      public void onPlaybackComplete(PlaybackCompleteEvent event) {
        playing = false;
      }
    });
  }

  @Override
  protected boolean playImpl() {
    return impl.play();
  }

  @Override
  protected void stopImpl() {
    impl.stop();
  }

  @Override
  protected void setLoopingImpl(boolean looping) {
    impl.setLooping(looping);
  }

  @Override
  protected void setVolumeImpl(float volume) {
    impl.setVolume((int) (volume * 100));
  }

  @Override
  protected void releaseImpl() {
    // TODO: anything?
  }
}
