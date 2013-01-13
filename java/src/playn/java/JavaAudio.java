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

import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import playn.core.AudioImpl;

import javax.sound.sampled.Clip;

class JavaAudio extends AudioImpl {

  public JavaAudio(JavaPlatform platform) {
    super(platform);
  }

  JavaSound createSound(final String name, final InputStream in, final boolean music) {
    final JavaSound sound = new JavaSound();
    ((JavaPlatform) platform).invokeAsync(new Runnable() {
      public void run () {
        try {
          AudioInputStream ais = AudioSystem.getAudioInputStream(in);
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
          dispatchLoaded(sound, clip);
        } catch (Exception e) {
          dispatchLoadError(sound, e);
        }
      }
    });
    return sound;
  }
}
