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
package playn.android;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import playn.core.Audio;

class AndroidAudio implements Audio {

  private final AndroidPlatform platform;
  // TODO: holding strong references to all sound files is problematic
  private final List<AndroidSound> sounds = new ArrayList<AndroidSound>();

  public AndroidAudio(AndroidPlatform platform) {
    this.platform = platform;
  }

  AndroidSound createSound(String path) throws IOException {
    // MediaPlayer should really be used to play compressed sounds and other file formats
    // AudioTrack cannot handle. However, the MediaPlayer implementation is currently the only
    // version of AndroidSound we have written, so we'll use it here regardless of format.
    AndroidSound sound = new AndroidCompressedSound(platform.assets(), path);
    sounds.add(sound);
    return sound;
  }

  public void onDestroy() {
    for (AndroidSound sound : sounds) {
      sound.onDestroy();
    }
  }

  public void onPause() {
    for (AndroidSound sound : sounds) {
      sound.onPause();
    }
  }

  public void onResume() {
    for (AndroidSound sound : sounds) {
      sound.onResume();
    }
  }
}
