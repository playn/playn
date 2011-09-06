/**
 * Copyright 2010 The PlayN Authors
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
package playn.html;

import playn.core.Audio;
import playn.core.PlayN;

import com.allen_sauer.gwt.voices.client.FlashSound;
import com.allen_sauer.gwt.voices.client.Sound;
import com.allen_sauer.gwt.voices.client.SoundController;
import com.allen_sauer.gwt.voices.client.ui.FlashMovie;

/**
 * This class is temporarily public, in order to expose {@link #isFlash9AudioPluginMissing()}, to
 * assist with in game Flash detection.
 */
public class HtmlAudio implements Audio {

  private SoundController soundController = new SoundController();

  @SuppressWarnings("deprecation")
  public void init() {
    PlayN.log().debug(
        "Preferred sound type: " + soundController.getPreferredSoundType().getName());
  }

  HtmlSound createSound(String url) {
    Sound sound = soundController.createSound("audio/mpeg", url);
    // PlayN.log().debug(sound.getClass().getName() + " " + sound.getUrl());
    return new HtmlSound(sound);
  }

  @SuppressWarnings("deprecation")
  public boolean isFlash9AudioPluginMissing() {
    if (soundController.getPreferredSoundType() != FlashSound.class) {
      PlayN.log().debug("HTML5 audio requested; skipping Flash check");
      return false;
    }

    PlayN.log().debug(
        "FlashMovie.isExternalInterfaceSupported: " + FlashMovie.isExternalInterfaceSupported());
    PlayN.log().debug("FlashMovie.getMajorVersion: " + FlashMovie.getMajorVersion());
    if (FlashMovie.isExternalInterfaceSupported() && FlashMovie.getMajorVersion() >= 9) {
      // Flash plugin operational and is at least version 9
      return false;
    }

    // Audio missing or disabled
    return true;
  }
}
