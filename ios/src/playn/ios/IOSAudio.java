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

import playn.core.Audio;
import playn.core.Sound;

public class IOSAudio implements Audio {

  private final IOSPlatform platform;

  public IOSAudio(IOSPlatform platform) {
    this.platform = platform;
  }

  Sound createSound(String path) {
    final IOSSound sound = new IOSSound();
    ThreadPool.QueueUserWorkItem(new WaitCallback(new WaitCallback.Method() {
      public void Invoke(Object arg) {
        final String path = (String) arg;
        final NSError[] error = new NSError[1];
        final AVAudioPlayer player = AVAudioPlayer.FromUrl(NSUrl.FromFilename(path), error);
        platform.invokeLater(new Runnable() {
          public void run () {
            if (error[0] == null) {
              sound.setPlayer(player);
            } else {
              platform.log().warn("Error loading sound [" + path + ", " + error[0] + "]");
              sound.setError(new Exception(error[0].ToString()));
            }
          }
        });
      }
    }), path);
    return sound;
  }
}
