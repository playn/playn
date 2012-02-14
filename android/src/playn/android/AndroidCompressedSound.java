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

import static playn.core.PlayN.log;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An implementation of AndroidSound using the Android MediaPlayer
 * class.
 */
public class AndroidCompressedSound extends AndroidSound {
  private File cachedFile;
  private boolean paused, prepared, looping, playOnPrepare;
  private float volume = 0.99f;
  private int position;
  private MediaPlayer mp;

  public AndroidCompressedSound(InputStream in, String extension) throws IOException {
    cachedFile = new File(AndroidPlatform.instance.activity.getFilesDir(), "sound-" + Integer.toHexString(hashCode())
        + extension);
    try {
      FileOutputStream out = new FileOutputStream(cachedFile);
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

    try {
      resetMp();
    }catch(IOException e) {
      log().error("IOException thrown building MediaPlayer for sound.");
      onLoadError(e);
    }
  }

  /**
   * Play the sound. Calling multiple times in succession will reset
   * playback each time instead of playing multiple instances of
   * the sound.
   */
  @Override
  public boolean play() {
    if (!prepared) {
      playOnPrepare = true;
    } else {
      mp.seekTo(position);  //Play from stored position if there is one
      mp.start();
      position = 0;
    }
    return true;
  }

  /**
   * Stop playback and reset the playhead position.
   */
  @Override
  public void stop() {
    if (mp == null) return;
    mp.pause();
    mp.seekTo(0);
  }

  @Override
  public void setLooping(boolean looping) {
    this.looping = looping;
    if (mp != null) mp.setLooping(looping);
  }

  @Override
  public void setVolume(float volume) {
    this.volume = volume < 0 ? 0 : volume >= 1 ? 0.99f : volume;
    if (mp != null) mp.setVolume(this.volume, this.volume);
  }

  @Override
  public boolean isPlaying() {
    return mp == null ? false : mp.isPlaying();
  }

  /*
   * The following methods are called by AndroidAudio
   * during the Activity lifecycle
   */
  @Override
  void onPause() {
    if (mp == null) {
      return;
    } else {
      if (mp.isPlaying()) {
        position = mp.getCurrentPosition();
        paused = true;
      }
      mp.release();
    }
  }

  @Override
  void onResume() {
    try {
      resetMp();
      if (paused) { //If the sound was playing when onPause() was called
        paused = false;
        play();  //Queue up to play when prepared.
      }
    }catch (IOException e) {
      log().error("IOException thrown resetting MediaPlayer for sound in onResume()");
    }
  }

  @Override
  void onDestroy() {
    cachedFile.delete();
    if (mp != null) {
      mp.stop();
      mp.release();
    }
  }

  void prepared() {
    prepared = true;
    Log.d("playn", "Prepared");
    onLoadComplete();
    if (playOnPrepare) {
      playOnPrepare = false;
      play();
    }
  }

  private void resetMp() throws IOException {
    mp = new MediaPlayer();
    prepared = false;
    FileInputStream ins = new FileInputStream(cachedFile);
    try {
      mp.setDataSource(ins.getFD());
      mp.setOnPreparedListener(new SoundPreparedListener(this));
      mp.setOnErrorListener(new OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
          log().error("Error preparing MediaPlayer while loading sound.");
          onLoadError(new RuntimeException("Error preparing MediaPlayer while loading sound"));
          return false;
        }
      });
      mp.setLooping(looping);
      mp.setVolume(volume, volume);
      mp.prepareAsync();
    } finally {
      ins.close();
    }
  }

  @Override
  public void finalize() {
    onDestroy();
  }

  private class SoundPreparedListener implements MediaPlayer.OnPreparedListener {
    AndroidCompressedSound sound;

    SoundPreparedListener(AndroidCompressedSound sound) {
      this.sound = sound;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (sound != null) sound.prepared();
    }
  }
}
