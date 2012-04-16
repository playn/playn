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

import com.allen_sauer.gwt.voices.client.FlashSound;
import com.allen_sauer.gwt.voices.client.Sound;
import com.allen_sauer.gwt.voices.client.SoundController;
import com.allen_sauer.gwt.voices.client.ui.FlashMovie;
import com.google.gwt.dom.client.Element;

import playn.core.Audio;

/**
 * This class is temporarily public, in order to expose {@link #isFlash9AudioPluginMissing()}, to
 * assist with in game Flash detection.
 */
public class HtmlAudio implements Audio {

  private Element audioContext;
  private SoundController soundController = new SoundController();

  /**
   * Borrowed form com.allen_sauer.gwt.voices.client.WebAudioSound#createAudioContext()
   */
  private static native Element maybeCreateAudioContext() /*-{
    try {
      return new AudioContext();
    } catch (ignore) {
    }

    try {
      return new webkitAudioContext();
    } catch (ignore) {
    }

    return null;
  }-*/;

  @SuppressWarnings("deprecation")
  public void init() {
    HtmlPlatform.log.debug("Preferred sound type(s): " + soundController.getPreferredSoundType());

    // Attempt to create Web Audio API audio context
    audioContext = maybeCreateAudioContext();
  }

  HtmlSound createSound(String url) {
    Sound sound = soundController.createSound("audio/mpeg", url);
    // HtmlPlatform.log.debug(sound.getClass().getName() + " " + sound.getUrl());
    return new HtmlSound(sound);
  }

  @SuppressWarnings("deprecation")
  public boolean isFlash9AudioPluginMissing() {
    if (audioContext != null) {
      // Web Audio API is available; Flash not needed
      return false;
    }

    // Is Flash one of the requested audio types?
    for (Class<?> clzz : soundController.getPreferredSoundType()) {
      if (clzz == FlashSound.class) {
        return isFlash9AudioPluginMissingImpl();
      }
    }

    // Flash audio is not one of the request sound types
    return false;
  }

  public boolean isFlash9AudioPluginMissingImpl() {
    HtmlPlatform.log.debug(
        "FlashMovie.isExternalInterfaceSupported: " + FlashMovie.isExternalInterfaceSupported());
    HtmlPlatform.log.debug("FlashMovie.getMajorVersion: " + FlashMovie.getMajorVersion());
    if (FlashMovie.isExternalInterfaceSupported() && FlashMovie.getMajorVersion() >= 9) {
      // Flash plugin operational and is at least version 9
      return false;
    }

    // Flash plugin not installed or disabled
    return true;
  }
}
