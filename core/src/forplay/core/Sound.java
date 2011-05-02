/**
 * Copyright 2011 The ForPlay Authors
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
package forplay.core;

/**
 * TODO(fredsa): Add something like {@link Image#addCallback(ResourceCallback)}.
 */
public interface Sound {

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
   * @param volume new volume between {@literal 0.0} and {@literal 1.0}
   */
  void setVolume(float volume);

  /**
   * Determine whether this audio stream is currently playing.
   * 
   * @return {@literal true} if the audio stream is currently playing
   */
  boolean isPlaying();
}
