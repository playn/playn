/**
 * Copyright 2012 The PlayN Authors
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
package playn.ios;

import cli.MonoTouch.AVFoundation.AVAudioPlayer;

import playn.core.AbstractSound;

/**
 * An implementation of Sound using the AVAudioPlayer. This is used for compressed audio,
 * especially lengthy music tracks.
 */
public class IOSSoundAVAP extends AbstractSound<AVAudioPlayer> {

  @Override
  protected boolean prepareImpl() {
    return impl.PrepareToPlay();
  }

  @Override
  protected boolean playingImpl() {
    return impl.get_Playing();
  }

  @Override
  protected boolean playImpl() {
    impl.set_CurrentTime(0);
    return impl.Play();
  }

  @Override
  protected void stopImpl() {
    impl.Stop();
    impl.set_CurrentTime(0);
  }

  @Override
  protected void setLoopingImpl(boolean looping) {
    impl.set_NumberOfLoops(looping ? -1 : 0);
  }

  @Override
  protected void setVolumeImpl(float volume) {
    impl.set_Volume(volume);
  }

  @Override
  protected void releaseImpl() {
    impl.Dispose();
  }
}
