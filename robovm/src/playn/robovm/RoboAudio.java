/**
 * Copyright 2014 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playn.robovm;

import java.io.File;

import org.robovm.apple.audiotoolbox.AudioSession;
import org.robovm.apple.avfoundation.AVAudioPlayer;
import org.robovm.apple.foundation.NSError;
import org.robovm.apple.foundation.NSURL;

import playn.core.AudioImpl;
import playn.core.Sound;

public class RoboAudio extends AudioImpl {

  private final RoboPlatform platform;
  // private final AudioContext actx;
  // private final int[] result = new int[1]; // used for AL.GetSource

  // private final int[] sources;
  // private final RoboSoundOAL[] active;
  // private final int[] started;

  public RoboAudio(RoboPlatform platform, int numSources) {
    super(platform);
    this.platform = platform;
    // actx = new AudioContext();

    // // obtain our desired number of sources
    // sources = new int[numSources];
    // AL.GenSources(sources.length, sources);
    // active = new RoboSoundOAL[sources.length];
    // started = new int[sources.length];

    // AudioSession.Initialize();
    // AudioSession.SetActive(true);

    // // clear and restore our OAL context on audio session interruption
    // AudioSession.add_Interrupted(new EventHandler(new EventHandler.Method() {
    //   public void Invoke(Object sender, EventArgs event) {
    //     AudioSession.SetActive(false);
    //     Alc.MakeContextCurrent(ContextHandle.Zero);
    //   }
    // }));
    // AudioSession.add_Resumed(new EventHandler(new EventHandler.Method() {
    //   public void Invoke(Object sender, EventArgs event) {
    //     actx.MakeCurrent(); // calls Alc.MakeContextCurrent under the hood
    //     AudioSession.SetActive(true);
    //   }
    // }));
  }

  public Sound createSound(File path, boolean isMusic) {
    // if the file is meant to be music, or if it's not uncompressed CAFF, we need to use
    // AVAudioPlayer; if it's uncompressed CAFF, we can use OpenAL
    return (isMusic || !path.getName().endsWith(".caf")) ?
      createAVAP(new NSURL(path)) : createOAL(path);
  }

  Sound createAVAP(final NSURL url) {
    final RoboSoundAVAP sound = new RoboSoundAVAP();
    platform.invokeAsync(new Runnable() {
      public void run () {
        NSError.NSErrorPtr error = new NSError.NSErrorPtr();
        AVAudioPlayer player = new AVAudioPlayer(url, error);
        if (error.get() == null) {
          dispatchLoaded(sound, player);
        } else {
          String errstr = error.get().toString();
          platform.log().warn("Error loading sound [" + url + ", " + errstr + "]");
          dispatchLoadError(sound, new Exception(errstr));
        }
      }
    });
    return sound;
  }

  Sound createOAL(File assetPath) {
    final RoboSoundOAL sound = new RoboSoundOAL(this);
    // ThreadPool.QueueUserWorkItem(new WaitCallback(new WaitCallback.Method() {
    //   public void Invoke(Object arg) {
    //     Path path = (Path) arg;
    //     int bufferId = 0;
    //     try {
    //       bufferId = AL.GenBuffer();
    //       CAFLoader.load(path, bufferId);
    //       dispatchLoaded(sound, bufferId);
    //     } catch (Throwable t) {
    //       if (bufferId != 0)
    //         AL.DeleteBuffer(bufferId);
    //       dispatchLoadError(sound, t);
    //     }
    //   }
    // }), assetPath);
    return sound;
  }

  boolean isPlaying(int sourceIdx, RoboSoundOAL sound) {
    // if (active[sourceIdx] != sound)
    //   return false;
    // AL.GetSource(sources[sourceIdx], ALGetSourcei.wrap(ALGetSourcei.SourceState), result);
    // return (result[0] == ALSourceState.Playing);
    return false; // TODO
  }

  int play(RoboSoundOAL sound, float volume, boolean looping) {
    // // find a source that's not currently playing
    // int sourceIdx = -1, eldestIdx = 0;
    // for (int ii = 0; ii < sources.length; ii++) {
    //   if (!isPlaying(ii, active[ii])) {
    //     sourceIdx = ii;
    //     break;
    //   } else if (started[ii] < started[eldestIdx]) {
    //     eldestIdx = ii;
    //   }
    // }
    // // if all of our sources are playing, stop the oldest source and steal it
    // if (sourceIdx < 0) {
    //   stop(eldestIdx, active[eldestIdx]);
    //   sourceIdx = eldestIdx;
    // }
    // // prepare the source to play this sound's buffer
    // int sourceId = sources[sourceIdx];
    // AL.Source(sourceId, ALSourcei.wrap(ALSourcei.Buffer), sound.bufferId());
    // AL.Source(sourceId, ALSourcef.wrap(ALSourcef.Gain), volume);
    // AL.Source(sourceId, ALSourceb.wrap(ALSourceb.Looping), looping);
    // AL.SourcePlay(sourceId);
    // active[sourceIdx] = sound;
    // started[sourceIdx] = platform.tick();
    // return sourceIdx;
    return 0; // TODO
  }

  void stop(int sourceIdx, RoboSoundOAL sound) {
    // if (active[sourceIdx] == sound)
    //   AL.SourceStop(sources[sourceIdx]);
  }

  void setLooping(int sourceIdx, RoboSoundOAL sound, boolean looping) {
    // if (active[sourceIdx] == sound)
    //   AL.Source(sources[sourceIdx], ALSourceb.wrap(ALSourceb.Looping), looping);
  }

  void setVolume(int sourceIdx, RoboSoundOAL sound, float volume) {
    // if (active[sourceIdx] == sound)
    //   // OpenAL uses gain between 0 and 1, rather than raw db-based gain
    //   AL.Source(sources[sourceIdx], ALSourcef.wrap(ALSourcef.Gain), volume);
  }

  void terminate() {
    // if (actx.get_IsProcessing()) actx.Suspend();
    // actx.Dispose();
  }
}
