/**
 * Copyright 2011 The PlayN Authors
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
package playn.core;

import playn.core.util.Callback;

/**
 * A sound.
 */
public interface Sound {

  /** A sound that does nothing. Useful for optionally playing sound or no sound. */
  public static class Silence implements Sound {
    @Override
    public boolean prepare() {
      return false;
    }
    @Override
    public boolean play() {
      return false;
    }
    @Override
    public void stop() {
    }
    @Override
    public void setLooping(boolean looping) {
    }
    @Override
    public float volume() {
      return 0;
    }
    @Override
    public void setVolume(float volume) {
    }
    @Override
    public boolean isPlaying() {
      return false;
    }
    @Override
    public void release() {
    }
    @Override
    public void addCallback(Callback<? super Sound> callback) {
      callback.onSuccess(this);
    }
  }

  /** Represents a sound that failed to load. Reports the supplied error to all listeners. */
  public static class Error extends Silence {
    private final Exception error;

    public Error (Exception error) {
      this.error = error;
    }

    @Override
    public void addCallback(Callback<? super Sound> callback) {
      callback.onFailure(error);
    }
  }

  /**
   * Prepares this sound to be played by preloading it into audio buffers. This expresses a desire
   * to have subsequent calls to {@link #play} start emitting sound with the lowest possible
   * latency.
   *
   * @return {@literal true} if preloading occurred, false if unsupported or preloading failed
   */
  boolean prepare();

  /**
   * If possible, begin playback of this audio stream. The audio system will make best efforts to
   * playback this sound. However, lack of audio or codec support, or a (temporary) unavailability
   * of audio channels may prevent playback. If the audio system is certain that audio playback
   * failed, this method will return {@literal false}. However, a return value of {@literal true}
   * does not guarantee that playback will in fact succeed.
   *
   * @return {@literal true} if it's likely that audio playback will proceed
   */
  boolean play();

  /**
   * Stop playback of the current audio stream as soon as possible, and reset the sound position to
   * its starting position, such that a subsequent call to {@link #play()} will cause the audio file
   * to being playback from the beginning of the audio stream.
   */
  void stop();

  /**
   * Set whether audio stream playback should be looped indefinitely or not.
   *
   * @param looping {@literal true} if the audio stream should be looped indefinitely
   */
  void setLooping(boolean looping);

  /**
   * @return the current volume of this sound, a value between {@literal 0.0} and {@literal 1.0}.
   */
  float volume();

  /**
   * @param volume new volume between {@literal 0.0} and {@literal 1.0}
   */
  void setVolume(float volume);

  /**
   * Determine whether this audio stream is currently playing.
   *
   * @return {@literal true} if the audio stream is currently playing
   */
  boolean isPlaying();

  /**
   * Releases resources used by this sound. It will no longer be usable after release. This will
   * also happen automatically when this sound is garbage collected, but one may need to manually
   * release sounds sooner to avoid running out of audio resources.
   */
  void release();

  /**
   * Adds a callback to be notified when this sound has loaded. If the sound is
   * already loaded the callback will be notified immediately. The callback is
   * discarded once the sound is loaded.
   */
  void addCallback(Callback<? super Sound> callback);
}
