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

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer;
import android.media.SoundPool;

import playn.core.AbstractSound;
import playn.core.AudioImpl;

public class AndroidAudio extends AudioImpl {

  interface Resolver<I> {
    void resolve(AndroidSound<I> sound);
  }

  private final Set<AndroidSound<?>> playing = new HashSet<AndroidSound<?>>();
  private final AndroidPlatform platform;
  private final Map<Integer,PooledSound> loadingSounds = new HashMap<Integer,PooledSound>();
  private final SoundPool pool;

  private class PooledSound extends AbstractSound<Integer> {
    public final int soundId;
    private int streamId;

    public PooledSound(int soundId) {
      this.soundId = soundId;
    }

    @Override
    public String toString() {
      return "pooled:" + soundId;
    }

    @Override
    protected boolean playingImpl() {
      return false; // no way to tell
    }

    @Override
    protected boolean playImpl() {
      streamId = pool.play(soundId, volume, volume, 1, looping ? -1 : 0, 1);
      return (streamId != 0);
    }

    @Override
    protected boolean prepareImpl() {
      pool.play(soundId, 0, 0, 0, 0, 1);
      return true; // Well, it's not prepared, but preparING...
    }

    @Override
    protected void stopImpl() {
      if (streamId != 0) {
        pool.stop(streamId);
        streamId = 0;
      }
    }

    @Override
    protected void setLoopingImpl(boolean looping) {
      if (streamId != 0) {
        pool.setLoop(streamId, looping ? -1 : 0);
      }
    }

    @Override
    protected void setVolumeImpl(float volume) {
      if (streamId != 0) {
        pool.setVolume(streamId, volume, volume);
      }
    }

    @Override
    protected void releaseImpl() {
      pool.unload(soundId);
    }
  }

  public AndroidAudio(final AndroidPlatform platform) {
    super(platform);
    this.platform = platform;
    this.pool = new SoundPool(platform.activity.maxSimultaneousSounds(),
                              AudioManager.STREAM_MUSIC, 0);
    this.pool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
      public void onLoadComplete(SoundPool soundPool, int soundId, int status) {
        PooledSound sound = loadingSounds.remove(soundId);
        if (sound == null) {
          platform.log().warn("Got load complete for unknown sound [id=" + soundId + "]");
        } else if (status == 0) {
          dispatchLoaded(sound, soundId);
        } else {
          dispatchLoadError(sound, new Exception("Sound load failed [errcode=" + status + "]"));
        }
      }
    });
  }

  /**
   * Creates a sound instance from the supplied asset file descriptor.
   */
  public AbstractSound<?> createSound(AssetFileDescriptor fd) {
    PooledSound sound = new PooledSound(pool.load(fd, 1));
    loadingSounds.put(sound.soundId, sound);
    return sound;
  }

  /**
   * Creates a sound instance from the supplied file descriptor offset.
   */
  public AbstractSound<?> createSound(FileDescriptor fd, long offset, long length) {
    PooledSound sound = new PooledSound(pool.load(fd, offset, length, 1));
    loadingSounds.put(sound.soundId, sound);
    return sound;
  }

  AbstractSound<?> createSound(final String path) {
    try {
      return createSound(platform.assets().openAssetFd(path));
    } catch (IOException ioe) {
      PooledSound sound = new PooledSound(0);
      sound.onLoadError(ioe);
      return sound;
    }
  }

  AbstractSound<?> createMusic(final String path) {
    // MediaPlayer should really be used to play compressed sounds and other file formats
    // AudioTrack cannot handle. However, the MediaPlayer implementation is currently the only
    // version of AndroidSound we have written, so we'll use it here regardless of format.
    return new AndroidCompressedSound(this, new Resolver<MediaPlayer>() {
      public void resolve (final AndroidSound<MediaPlayer> sound) {
        // we need to create the media player before starting the background task because the media
        // player will dispatch callbacks based on the looper associated with the thread on which
        // it was created; resolve() will be called from the main PlayN thread and we want
        // callbacks to be dispatched on that same thread
        final MediaPlayer mp = new MediaPlayer();
        platform.invokeAsync(new Runnable() {
          public void run () {
            try {
              AssetFileDescriptor fd = platform.assets().openAssetFd(path);
              mp.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
              fd.close();
              mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(final MediaPlayer mp) {
                  dispatchLoaded(sound, mp);
                }
              });
              mp.setOnErrorListener(new OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                  String errmsg = "MediaPlayer prepare failure [what=" + what + ", x=" + extra + "]";
                  platform.log().warn(errmsg);
                  dispatchLoadError(sound, new Exception(errmsg));
                  return false;
                }
              });
              mp.prepareAsync();
            } catch (Exception e) {
              platform.log().warn("Sound load error '" + path + "'", e);
              dispatchLoadError(sound, e);
            }
          }
        });
      }
    });
  }

  public void onPause() {
    // handle our pooled sounds
    pool.autoPause();

    // now handle our musics
    if (!playing.isEmpty())
      AndroidPlatform.debugLog("Pausing " + playing.size() + " playing sounds.");
    for (AndroidSound<?> sound : playing) {
      sound.onPause();
    }
  }

  public void onResume() {
    // handle our pooled sounds
    pool.autoResume();

    // copy and clear out the playing set, the playing sounds will re-add themselves once they are
    // (asynchronously) re-resolved and resume playing
    Set<AndroidSound<?>> wasPlaying = new HashSet<AndroidSound<?>>(playing);
    playing.clear();
    if (!wasPlaying.isEmpty())
      AndroidPlatform.debugLog("Resuming " + wasPlaying.size() + " playing sounds.");
    for (AndroidSound<?> sound : wasPlaying) {
      sound.onResume();
    }
  }

  public void onDestroy() {
    for (AndroidSound<?> sound : playing) {
      sound.release();
    }
    playing.clear();
    pool.release();
  }

  void onPlaying(AndroidSound<?> sound) {
    AndroidPlatform.debugLog("Playing " + sound);
    playing.add(sound);
  }

  void onStopped(AndroidSound<?> sound) {
    AndroidPlatform.debugLog("Stopped " + sound);
    playing.remove(sound);
  }
}
