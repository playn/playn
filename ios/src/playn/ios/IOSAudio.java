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

import cli.System.Threading.ThreadPool;
import cli.System.Threading.WaitCallback;

import cli.MonoTouch.AVFoundation.AVAudioPlayer;
import cli.MonoTouch.Foundation.NSError;
import cli.MonoTouch.Foundation.NSUrl;

import playn.core.AudioImpl;
import playn.core.Sound;

public class IOSAudio extends AudioImpl {

  public IOSAudio(IOSPlatform platform) {
    super(platform);
  }

  public Sound createSound(NSUrl url) {
    final IOSSound sound = new IOSSound();
    ThreadPool.QueueUserWorkItem(new WaitCallback(new WaitCallback.Method() {
      public void Invoke(Object arg) {
        NSUrl url = (NSUrl) arg;
        NSError[] error = new NSError[1];
        AVAudioPlayer player = AVAudioPlayer.FromUrl(url, error);
        if (error[0] == null) {
          dispatchLoaded(sound, player);
        } else {
          platform.log().warn("Error loading sound [" + url + ", " + error[0] + "]");
          dispatchLoadError(sound, new Exception(error[0].ToString()));
        }
      }
    }), url);
    return sound;
  }

  Sound createSound(String path) {
    return createSound(NSUrl.FromFilename(path));
  }
}
