/**
 * Copyright 2010 The ForPlay Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package forplay.html;

import com.allen_sauer.gwt.voices.client.FlashSound;
import com.allen_sauer.gwt.voices.client.Html5Sound;
import com.allen_sauer.gwt.voices.client.SoundController;
import com.allen_sauer.gwt.voices.client.ui.FlashMovie;

import forplay.core.Audio;
import forplay.core.ForPlay;

/**
 * This class is temporarily public, in order to expose {@link #isFlash9Installed()}, to assist with
 * in game Flash detection.
 */
public class HtmlAudio implements Audio {

  private SoundController soundController;

  private static final boolean PREFER_FLASH_AUDIO = true;

  @SuppressWarnings("deprecation")
  public HtmlAudio() {
    soundController = new SoundController();
    soundController.setPreferredSoundType(PREFER_FLASH_AUDIO ? FlashSound.class : Html5Sound.class);
  }

  HtmlSound createSound(String url) {
    return new HtmlSound(soundController.createSound("audio/mpeg", url));
  }

  public boolean isFlash9Installed() {
    ForPlay.log().debug("FlashMovie.isExternalInterfaceSupported: " + FlashMovie.isExternalInterfaceSupported());
    ForPlay.log().debug("FlashMovie.getMajorVersion: "+FlashMovie.getMajorVersion());
    return FlashMovie.isExternalInterfaceSupported() && FlashMovie.getMajorVersion() >= 9;
  }
}
