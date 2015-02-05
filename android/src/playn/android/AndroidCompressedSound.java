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

import android.media.MediaPlayer;

/**
 * An implementation of AndroidSound using the Android MediaPlayer class.
 */
public class AndroidCompressedSound extends AndroidSound<MediaPlayer> {

  private final AndroidAudio audio;
  private final AndroidAudio.Resolver<MediaPlayer> resolver;
  private int position;

  public AndroidCompressedSound(AndroidAudio audio, AndroidAudio.Resolver<MediaPlayer> resolver) {
    super(audio.plat.exec());
    this.audio = audio;
    this.resolver = resolver;
    resolve();
  }

  @Override public void succeed (MediaPlayer impl) {
    super.succeed(impl);
    impl.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
      public void onCompletion(MediaPlayer mp) {
        audio.onStopped(AndroidCompressedSound.this);
      }
    });
  }

  @Override
  protected boolean playingImpl() {
    return impl.isPlaying();
  }

  @Override
  protected boolean playImpl() {
    audio.onPlaying(this);
    impl.seekTo(position); // play from stored position if there is one
    impl.start();
    position = 0;
    return true;
  }

  @Override
  protected void stopImpl() {
    audio.onStopped(this);
    impl.pause();
  }

  @Override
  protected void setLoopingImpl(boolean looping) {
    impl.setLooping(looping);
  }

  @Override
  protected void setVolumeImpl(float volume) {
    impl.setVolume(volume, volume);
  }

  @Override
  protected void releaseImpl() {
    if (impl.isPlaying())
      impl.stop();
    impl.release();
  }

  private void resolve() {
    resolver.resolve(AndroidCompressedSound.this);
  }

  // The following methods are called by AndroidAudio during the Activity lifecycle

  @Override
  void onPause() {
    if (impl != null) {
      // note our current play position
      if (impl.isPlaying()) {
        position = impl.getCurrentPosition();
      }
      // release our media player and reset ourselves to the unloaded state
      impl.release();
      impl = null;
    }
  }

  @Override
  void onResume() {
    resolve(); // re-resolve our media player
  }
}
