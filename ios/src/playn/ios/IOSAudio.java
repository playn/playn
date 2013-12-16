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

import cli.System.EventArgs;
import cli.System.EventHandler;
import cli.System.Threading.ThreadPool;
import cli.System.Threading.WaitCallback;

import cli.MonoTouch.AVFoundation.AVAudioPlayer;
import cli.MonoTouch.AudioToolbox.AudioSession;
import cli.MonoTouch.Foundation.NSError;
import cli.MonoTouch.Foundation.NSUrl;

import cli.OpenTK.Audio.AudioContext;
import cli.OpenTK.Audio.OpenAL.AL;
import cli.OpenTK.Audio.OpenAL.ALGetSourcei;
import cli.OpenTK.Audio.OpenAL.ALSourceState;
import cli.OpenTK.Audio.OpenAL.ALSourceb;
import cli.OpenTK.Audio.OpenAL.ALSourcef;
import cli.OpenTK.Audio.OpenAL.ALSourcei;
import cli.OpenTK.Audio.OpenAL.Alc;
import cli.OpenTK.ContextHandle;

import playn.core.AudioImpl;
import playn.core.Sound;

public class IOSAudio extends AudioImpl {

  private final AudioContext actx;
  private final int[] result = new int[1]; // used for AL.GetSource

  private final int[] sources;
  private final IOSSoundOAL[] active;
  private final int[] started;

  public IOSAudio(IOSPlatform platform, int numSources) {
    super(platform);
    actx = new AudioContext();

    // obtain our desired number of sources
    sources = new int[numSources];
    AL.GenSources(sources.length, sources);
    active = new IOSSoundOAL[sources.length];
    started = new int[sources.length];

    // clear and restore our OAL context on audio session interruption
    AudioSession.add_Interrupted(new EventHandler(new EventHandler.Method() {
      public void Invoke(Object sender, EventArgs event) {
        Alc.MakeContextCurrent(ContextHandle.Zero);
      }
    }));
    AudioSession.add_Resumed(new EventHandler(new EventHandler.Method() {
      public void Invoke(Object sender, EventArgs event) {
        actx.MakeCurrent(); // calls Alc.MakeContextCurrent under the hood
      }
    }));
  }

  public Sound createSound(String path, boolean isMusic) {
    // if the file is meant to be music, or if it's not uncompressed CAFF, we need to use
    // AVAudioPlayer; if it's uncompressed CAFF, we can use OpenAL
    return (isMusic || !path.endsWith(".caf")) ?
      createAVAP(NSUrl.FromFilename(path)) : createOAL(path);
  }

  Sound createAVAP(NSUrl url) {
    final IOSSoundAVAP sound = new IOSSoundAVAP();
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

  Sound createOAL(String assetPath) {
    final IOSSoundOAL sound = new IOSSoundOAL(this);
    ThreadPool.QueueUserWorkItem(new WaitCallback(new WaitCallback.Method() {
      public void Invoke(Object arg) {
        String path = (String) arg;
        int bufferId = 0;
        try {
          bufferId = AL.GenBuffer();
          CAFLoader.load(path, bufferId);
          dispatchLoaded(sound, bufferId);
        } catch (Throwable t) {
          if (bufferId != 0)
            AL.DeleteBuffer(bufferId);
          dispatchLoadError(sound, t);
        }
      }
    }), assetPath);
    return sound;
  }

  boolean isPlaying(int sourceIdx, IOSSoundOAL sound) {
    if (active[sourceIdx] != sound)
      return false;
    AL.GetSource(sources[sourceIdx], ALGetSourcei.wrap(ALGetSourcei.SourceState), result);
    return (result[0] == ALSourceState.Playing);
  }

  int play(IOSSoundOAL sound, float volume, boolean looping) {
    // find a source that's not currently playing
    int sourceIdx = -1, eldestIdx = 0;
    for (int ii = 0; ii < sources.length; ii++) {
      if (!isPlaying(ii, active[ii])) {
        sourceIdx = ii;
        break;
      } else if (started[ii] < started[eldestIdx]) {
        eldestIdx = ii;
      }
    }
    // if all of our sources are playing, stop the oldest source and steal it
    if (sourceIdx < 0) {
      stop(eldestIdx, active[eldestIdx]);
      sourceIdx = eldestIdx;
    }
    // prepare the source to play this sound's buffer
    int sourceId = sources[sourceIdx];
    AL.Source(sourceId, ALSourcei.wrap(ALSourcei.Buffer), sound.bufferId());
    AL.Source(sourceId, ALSourcef.wrap(ALSourcef.Gain), volume);
    AL.Source(sourceId, ALSourceb.wrap(ALSourceb.Looping), looping);
    AL.SourcePlay(sourceId);
    active[sourceIdx] = sound;
    started[sourceIdx] = platform.tick();
    return sourceIdx;
  }

  void stop(int sourceIdx, IOSSoundOAL sound) {
    if (active[sourceIdx] == sound)
      AL.SourceStop(sources[sourceIdx]);
  }

  void setLooping(int sourceIdx, IOSSoundOAL sound, boolean looping) {
    if (active[sourceIdx] == sound)
      AL.Source(sources[sourceIdx], ALSourceb.wrap(ALSourceb.Looping), looping);
  }

  void setVolume(int sourceIdx, IOSSoundOAL sound, float volume) {
    if (active[sourceIdx] == sound)
      // OpenAL uses gain between 0 and 1, rather than raw db-based gain
      AL.Source(sources[sourceIdx], ALSourcef.wrap(ALSourcef.Gain), volume);
  }
}
