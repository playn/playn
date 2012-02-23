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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import playn.core.Audio;
import playn.core.Sound;

class AndroidAudio implements Audio {
  private List<AndroidSound> sounds = new ArrayList<AndroidSound>();

  public AndroidAudio() { }

  Sound createSound(String path, InputStream in) throws IOException {
    String extension = path.substring(path.lastIndexOf('.'));
    AndroidSound sound;

    /*
     * MediaPlayer should really be used to play compressed sounds and
     * other file formats AudioTrack cannot handle. However, the MediaPlayer
     * implementation is currently the only version of AndroidSound we
     * have written, so we'll use it here regardless of format.
     */
    try {
      sound = new AndroidCompressedSound(in, extension);
      sounds.add(sound);
    }catch (IOException e) {
      sound = null;
    }
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
