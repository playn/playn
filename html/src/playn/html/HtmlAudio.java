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

import com.allen_sauer.gwt.voices.client.Sound;
import com.allen_sauer.gwt.voices.client.SoundController;
import com.allen_sauer.gwt.voices.client.SoundType;
import com.allen_sauer.gwt.voices.client.ui.FlashMovie;
import com.google.gwt.dom.client.Element;

import playn.core.Audio;

/**
 * This class is temporarily public, in order to expose {@link #isFlash9AudioPluginMissing()}, to
 * assist with in game Flash detection.
 */
public class HtmlAudio extends Audio {

  private final HtmlPlatform plat;
  private Element audioContext;
  private SoundController soundController = new SoundController();

  public HtmlAudio (HtmlPlatform plat) {
    this.plat = plat;
    @SuppressWarnings("deprecation") SoundType[] types = soundController.getPreferredSoundTypes();
    plat.log().debug("Preferred sound type(s): " + types);
    // Attempt to create Web Audio API audio context
    audioContext = maybeCreateAudioContext();
  }

  /**
   * Creates a sound instance from the supplied URL.
   */
  public HtmlSound createSound (String url) {
    Sound sound = soundController.createSound("audio/mpeg", url);
    // HtmlPlatform.log.debug(sound.getClass().getName() + " " + sound.getUrl());
    return new HtmlSound(plat.exec(), sound);
  }

  /**
   * Borrowed form com.allen_sauer.gwt.voices.client.WebAudioSound#createAudioContext()
   */
  private static native Element maybeCreateAudioContext () /*-{
    try { return new AudioContext(); } catch (ignore) {}
    try { return new webkitAudioContext(); } catch (ignore) {}
    return null;
  }-*/;

  boolean isFlash9AudioPluginMissing () {
    // Web Audio API is available; Flash not needed
    if (audioContext != null) return false;
    // Is Flash one of the requested audio types?
    @SuppressWarnings("deprecation") SoundType[] types = soundController.getPreferredSoundTypes();
    for (SoundType type : types) {
      if (type == SoundType.FLASH) return isFlash9AudioPluginMissingImpl();
    }
    // Flash audio is not one of the request sound types
    return false;
  }

  boolean isFlash9AudioPluginMissingImpl() {
    boolean supported = FlashMovie.isExternalInterfaceSupported();
    int version = FlashMovie.getMajorVersion();
    plat.log().debug("FlashMovie.isExternalInterfaceSupported: " + supported +
                     ", getMajorVersion: " + version);
    // if Flash plugin is operational and at least version 9, then it's not "missing"
    return (supported && version < 9);
  }
}
