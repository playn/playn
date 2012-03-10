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
import cli.MonoTouch.AVFoundation.AVStatusEventArgs;
import cli.MonoTouch.Foundation.NSError;
import cli.MonoTouch.Foundation.NSUrl;
import playn.core.Asserts;
import playn.core.PlayN;
import playn.core.ResourceCallback;

import playn.core.Sound;

class IOSSound implements Sound
{
  private String path;
  private AVAudioPlayer player;

  public IOSSound (String path) {
    this.path = path;
  }

  @Override
  public boolean play() {
    Asserts.check(path != null, "Asked to play() a null file");

    PlayN.log().debug("play() [" + path +"]");
    NSError[] error = new NSError[1];
    player = AVAudioPlayer.FromUrl(NSUrl.FromFilename(path), error);
    if (error[0] != null) {
      PlayN.log().warn("Error starting sound [" + path + ", " + error[0] + "]");
      return false;
    }

    player.add_FinishedPlaying(
      new cli.System.EventHandler$$00601_$$$_Lcli__MonoTouch__AVFoundation__AVStatusEventArgs_$$$$_(new cli.System.EventHandler$$00601_$$$_Lcli__MonoTouch__AVFoundation__AVStatusEventArgs_$$$$_.Method() {
        public void Invoke(Object obj, AVStatusEventArgs args) {
          PlayN.log().debug("sound finished [" + path + "]");
          if (player != null) {
            // TODO: can't really Dispose here
            //player.Dispose();
            player = null;
          }
        }
      }));
    return player.Play();
  }

  @Override
  public void stop() {
    Asserts.check(path != null, "Asked to stop() a null file");

    PlayN.log().debug("stop() [" + path + "]");
    if (player != null) {
      // TODO: Does this trigger FinishedPlaying on the player?
      player.Stop();
    }
  }

  @Override
  public void setLooping(boolean looping) {
    // TODO
  }

  @Override
  public void setVolume(float volume) {
    // TODO
  }

  @Override
  public boolean isPlaying() {
    return player != null && player.get_Playing();
  }

  @Override
  public void addCallback(ResourceCallback<Sound> callback) {
    // TODO
    callback.done(this); // we're always ready
  }
}
