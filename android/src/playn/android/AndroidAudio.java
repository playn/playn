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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer;
import android.os.AsyncTask;

import playn.core.AudioImpl;

class AndroidAudio extends AudioImpl {

  interface Resolver<I> {
    void resolve(AndroidSound<I> sound);
  }

  private final Set<AndroidSound<?>> playing = new HashSet<AndroidSound<?>>();

  public AndroidAudio(AndroidPlatform platform) {
    super(platform);
  }

  AndroidSound<?> createSound(final String path) throws IOException {
    // MediaPlayer should really be used to play compressed sounds and other file formats
    // AudioTrack cannot handle. However, the MediaPlayer implementation is currently the only
    // version of AndroidSound we have written, so we'll use it here regardless of format.
    return new AndroidCompressedSound(this, new Resolver<MediaPlayer>() {
      // this cache file will get cleaned up in our activity's onDestroy
      private String extension = path.substring(path.lastIndexOf('.'));
      private File cachedFile = ((AndroidPlatform) platform).assets().cacheAsset(
        path, "sound-" + Integer.toHexString(hashCode()) + extension);

      public void resolve (final AndroidSound<MediaPlayer> sound) {
        // we need to create the media player before starting the background task because the media
        // player will dispatch callbacks based on the looper associated with the thread on which
        // it was created; resolve() will be called from the main PlayN thread and we want
        // callbacks to be dispatched on that same thread
        final MediaPlayer mp = new MediaPlayer();
        new AsyncTask<Void,Void,Void>() {
          public Void doInBackground(Void... params) {
            try {
              FileInputStream ins = new FileInputStream(cachedFile);
              try {
                mp.setDataSource(ins.getFD());
                mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                  @Override
                  public void onPrepared(final MediaPlayer mp) {
                    dispatchLoaded(sound, mp);
                  }
                });
                mp.setOnErrorListener(new OnErrorListener() {
                  @Override
                  public boolean onError(MediaPlayer mp, int what, int extra) {
                    sound.onLoadError(new Exception("MediaPlayer prepare failure [what=" + what +
                                                    ", extra=" + extra + "]"));
                    return false;
                  }
                });
                mp.prepareAsync();
              } finally {
                ins.close();
              }
            } catch (Exception e) {
              dispatchLoadError(sound, e);
            }
            return null;
          }
        }.execute();
      }
    });
  }

  public void onPause() {
    if (!playing.isEmpty())
      platform.log().debug("Pausing " + playing.size() + " playing sounds.");
    for (AndroidSound<?> sound : playing) {
      sound.onPause();
    }
  }

  public void onResume() {
    // copy and clear out the playing set, the playing sounds will re-add themselves once they are
    // (asynchronously) re-resolved and resume playing
    Set<AndroidSound<?>> wasPlaying = new HashSet<AndroidSound<?>>(playing);
    playing.clear();
    if (!wasPlaying.isEmpty())
      platform.log().debug("Resuming " + wasPlaying.size() + " playing sounds.");
    for (AndroidSound<?> sound : wasPlaying) {
      sound.onResume();
    }
  }

  public void onDestroy() {
    for (AndroidSound<?> sound : playing) {
      sound.onDestroy();
    }
    playing.clear();
  }

  void onPlaying(AndroidSound<?> sound) {
    platform.log().debug("Playing " + sound);
    playing.add(sound);
  }

  void onStopped(AndroidSound<?> sound) {
    platform.log().debug("Stopped " + sound);
    playing.remove(sound);
  }
}
