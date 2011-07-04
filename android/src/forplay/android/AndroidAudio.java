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
package forplay.android;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import forplay.core.Audio;
import forplay.core.Sound;

class AndroidAudio implements Audio {

  private int ordinal;
  private List<AndroidSound> sounds = new ArrayList<AndroidSound>();

  public AndroidAudio() {
  }

  Sound getSound(String path, InputStream in) throws IOException {
    String extension = path.substring(path.lastIndexOf('.'));

    // TODO: This doesn't explicitly clean up these cache files beyond
    // deleteOnExit (which I'm not actually sure works on Android). It's
    // probably safe to delete them once we hand off the FD to the media player,
    // but this needs testing.
    File tmp = new File(AndroidPlatform.instance.activity.getCacheDir(), "sound-" + ordinal++
        + extension);
    tmp.deleteOnExit();
    try {
      FileOutputStream out = new FileOutputStream(tmp);
      try {
        byte[] buffer = new byte[16 * 1024];
        while (true) {
          int r = in.read(buffer);
          if (r < 0)
            break;
          out.write(buffer, 0, r);
        }
      } finally {
        out.close();
      }
    } finally {
      in.close();
    }

    // The media player runs in a different process with a different user ID.
    // This player may not have access to the play we want to play, so we pass a
    // FileDescriptor instead.
    FileInputStream ins = new FileInputStream(tmp);
    MediaPlayer mp;

    try {
      mp = new MediaPlayer();
      mp.setDataSource(ins.getFD());
      // TODO: This blocks until the media is ready to play. Do we even need the
      // buffering update listener below?
      mp.prepare();
    } finally {
      ins.close();
    }

    final AndroidSound sound = new AndroidSound(path, mp);
    mp.setOnBufferingUpdateListener(new OnBufferingUpdateListener() {
      boolean loaded;

      @Override
      public void onBufferingUpdate(MediaPlayer mp, int percent) {
        if (!loaded) {
          sound.loaded();
          loaded = true;
        }
      }
    });

    sounds.add(sound);
    return sound;
  }

  public void destroy() {
    for (AndroidSound sound : sounds) {
      sound.stop();
    }
  }

  public void pause() {
    for (AndroidSound sound : sounds) {
      sound.pause();
    }
  }

  public void resume() {
    for (AndroidSound sound : sounds) {
      sound.resume();
    }
  }
}
