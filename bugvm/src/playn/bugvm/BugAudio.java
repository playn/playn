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
package playn.bugvm;

import java.io.File;

import com.bugvm.apple.avfoundation.AVAudioPlayer;
import com.bugvm.apple.avfoundation.AVAudioSession;
import com.bugvm.apple.avfoundation.AVAudioSessionSetActiveOptions;
import com.bugvm.apple.foundation.NSErrorException;
import com.bugvm.apple.foundation.NSURL;

import playn.core.Audio;
import playn.core.Sound;
import static playn.bugvm.OpenAL.*;

public class BugAudio extends Audio {

  private final BugPlatform plat;
  private final AVAudioSession session;
  private final long oalDevice;
  private final long oalContext;

  private final int[] sources;
  private final BugSoundOAL[] active;
  private final int[] started;

  public BugAudio(BugPlatform plat, int numSources) {
    this.plat = plat;

    session = AVAudioSession.getSharedInstance();
    try {
      session.setActive(true, AVAudioSessionSetActiveOptions.None);
    } catch (NSErrorException e) {
      plat.log().error("Unable to activate audio session: " + e);
    }

    oalDevice = alcOpenDevice(null);
    if (oalDevice != 0) {
      oalContext = alcCreateContext(oalDevice, null);
      alcMakeContextCurrent(oalContext);
    } else {
      plat.log().warn("Unable to open OpenAL device. Disabling OAL sound.");
      oalContext = 0;
    }

    // obtain our desired number of sources
    sources = new int[numSources];
    alGenSources(numSources, sources);
    active = new BugSoundOAL[sources.length];
    started = new int[sources.length];

    // TODO: this should use AVAudioSessionInterruptionNotification

    // clear and restore our OAL context on audio session interruption
    // AudioSession.add_Interrupted(new EventHandler(new EventHandler.Method() {
    //   public void Invoke(Object sender, EventArgs event) {
    //     // not needed?: session.setActive(false, null);
    //     OpenAL.alcMakeContextCurrent(0);
    //   }
    // }));
    // AudioSession.add_Resumed(new EventHandler(new EventHandler.Method() {
    //   public void Invoke(Object sender, EventArgs event) {
    //     OpenAL.alcMakeContextCurrent(alcContext);
    //     // not needed?: session.setActive(true, null);
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
    final BugSoundAVAP sound = new BugSoundAVAP(plat);
    plat.exec().invokeAsync(new Runnable() {
      public void run () {
        try {
          sound.succeed(new AVAudioPlayer(url));
        } catch (Exception e) {
          plat.log().warn("Error loading sound [" + url + "]", e);
          sound.fail(e);
        }
      }
    });
    return sound;
  }

  Sound createOAL(final File assetPath) {
    final BugSoundOAL sound = new BugSoundOAL(plat);
    plat.exec().invokeAsync(new Runnable() {
      public void run () {
        int bufferId = 0;
        try {
          bufferId = alGenBuffer();
          CAFLoader.load(assetPath, bufferId);
          sound.succeed(bufferId);
        } catch (Throwable t) {
          if (bufferId != 0) alDeleteBuffer(bufferId);
          sound.fail(t);
        }
      }
    });
    return sound;
  }

  boolean isPlaying(int sourceIdx, BugSoundOAL sound) {
    if (active[sourceIdx] != sound)
      return false;
    int[] result = new int[1];
    alGetSourcei(sources[sourceIdx], AL_SOURCE_STATE, result);
    return (result[0] == AL_PLAYING);
  }

  int play(BugSoundOAL sound, float volume, boolean looping) {
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
    alSourcei(sourceId, AL_BUFFER, sound.bufferId());
    alSourcef(sourceId, AL_GAIN, volume);
    alSourcei(sourceId, AL_LOOPING, looping ? AL_TRUE : AL_FALSE);
    alSourcePlay(sourceId);
    active[sourceIdx] = sound;
    started[sourceIdx] = plat.tick();
    return sourceIdx;
  }

  void stop(int sourceIdx, BugSoundOAL sound) {
    if (active[sourceIdx] == sound) {
      alSourceStop(sources[sourceIdx]);
    }
  }

  void delete(BugSoundOAL sound) {
    alDeleteBuffer(sound.bufferId());
  }

  void setLooping(int sourceIdx, BugSoundOAL sound, boolean looping) {
    if (active[sourceIdx] == sound) {
      alSourcei(sources[sourceIdx], AL_LOOPING, looping ? AL_TRUE : AL_FALSE);
    }
  }

  void setVolume(int sourceIdx, BugSoundOAL sound, float volume) {
    if (active[sourceIdx] == sound) {
      // OpenAL uses gain between 0 and 1, rather than raw db-based gain
      alSourcef(sources[sourceIdx], AL_GAIN, volume);
    }
  }

  void terminate() {
    // TODO: ?
    // if (actx.get_IsProcessing()) actx.Suspend();
    // actx.Dispose();
  }
}
