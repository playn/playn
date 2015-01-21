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

import react.RFuture;

/**
 * A single sound asset, which can be played, looped, etc.
 */
public class Sound {

  /** Represents a sound that failed to load. Reports the supplied error to all listeners. */
  public static class Error extends Sound {
    public Error (Exception error) {
      super(RFuture.<Sound>failure(error));
    }
  }

  /** Reports the asynchronous loading of this sound. This will be completed with success or
    * failure when the sound's asynchronous load completes. */
  public final RFuture<Sound> state;

  /**
   * Returns whether this sound is fully loaded. In general you'll want to react to {@link #state}
   * to do things only after a sound is loaded, but this method is useful if you want to just skip
   * playing a sound that's not fully loaded (because playing a sound that's not loaded will defer
   * the play request until it has loaded, which may result in mismatched audio and visuals).
   *
   * <p>Note: this is different from {@link #prepare}. This has to do with loading the sound bytes
   * from storage (or over the network in the case of the HTML backend). {@link #prepare} attempts
   * to ensure that the sound bytes are then transferred from CPU memory into the appropriate audio
   * buffers so that they can be played with the lowest possible latency.
   */
  public boolean isLoaded () { return state.isCompleteNow(); }

  /**
   * Prepares this sound to be played by preloading it into audio buffers. This expresses a desire
   * to have subsequent calls to {@link #play} start emitting sound with the lowest possible
   * latency.
   *
   * @return {@literal true} if preloading occurred, false if unsupported or preloading failed
   */
  public boolean prepare() {
    return false;
  }

  /**
   * If possible, begin playback of this audio stream. The audio system will make best efforts to
   * playback this sound. However, lack of audio or codec support, or a (temporary) unavailability
   * of audio channels may prevent playback. If the audio system is certain that audio playback
   * failed, this method will return {@literal false}. However, a return value of {@literal true}
   * does not guarantee that playback will in fact succeed.
   *
   * @return {@literal true} if it's likely that audio playback will proceed
   */
  public boolean play() {
    return false;
  }

  /**
   * Stop playback of the current audio stream as soon as possible, and reset the sound position to
   * its starting position, such that a subsequent call to {@link #play()} will cause the audio file
   * to being playback from the beginning of the audio stream.
   */
  public void stop() {}

  /**
   * Set whether audio stream playback should be looped indefinitely or not.
   *
   * @param looping {@literal true} if the audio stream should be looped indefinitely
   */
  public void setLooping(boolean looping) {}

  /**
   * @return the current volume of this sound, a value between {@literal 0.0} and {@literal 1.0}.
   */
  public float volume() {
    return 0;
  }

  /**
   * @param volume new volume between {@literal 0.0} and {@literal 1.0}
   */
  public void setVolume(float volume) {}

  /**
   * Determine whether this audio stream is currently playing.
   *
   * @return {@literal true} if the audio stream is currently playing
   */
  public boolean isPlaying() {
    return false;
  }

  /**
   * Releases resources used by this sound. It will no longer be usable after release. This will
   * also happen automatically when this sound is garbage collected, but one may need to manually
   * release sounds sooner to avoid running out of audio resources.
   */
  public void release() {}

  /** Creates the sound of silence. */
  public Sound () {
    this.state = RFuture.success(this);
  }

  protected Sound (RFuture<Sound> state) {
    this.state = state;
  }
}
