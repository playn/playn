/**
 * Copyright 2010 The PlayN Authors
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
package playn.java;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import playn.core.Audio;
import playn.core.Exec;

public class JavaAudio extends Audio {

  private final Exec exec;

  public JavaAudio(Exec exec) {
    this.exec = exec;
  }

  /**
   * Creates a sound instance from the audio data available via {@code in}.
   *
   * @param rsrc an resource instance via which the audio data can be read.
   * @param music if true, a custom {@link Clip} implementation will be used which can handle long
   * audio clips; if false, the default Java clip implementation is used which cannot handle long
   * audio clips.
   */
  public JavaSound createSound(final JavaAssets.Resource rsrc, final boolean music) {
    final JavaSound sound = new JavaSound(exec);
    exec.invokeAsync(new Runnable() {
      public void run () {
        try {
          AudioInputStream ais = rsrc.openAudioStream();
          Clip clip = AudioSystem.getClip();
          if (music) {
            clip = new BigClip(clip);
          }
          AudioFormat baseFormat = ais.getFormat();
          if (baseFormat.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
            AudioFormat decodedFormat = new AudioFormat(
              AudioFormat.Encoding.PCM_SIGNED,
              baseFormat.getSampleRate(),
              16, // we have to force sample size to 16
              baseFormat.getChannels(),
              baseFormat.getChannels()*2,
              baseFormat.getSampleRate(),
              false // big endian
              );
            ais = AudioSystem.getAudioInputStream(decodedFormat, ais);
          }
          clip.open(ais);
          sound.succeed(clip);
        } catch (Exception e) {
          sound.fail(e);
        }
      }
    });
    return sound;
  }
}
