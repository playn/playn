/**
 * Copyright 2011 The ForPlay Authors
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
package forplay.flash;

import com.google.gwt.core.client.JavaScriptObject;

import flash.gwt.FlashImport;

import forplay.core.Sound;

class FlashSound implements Sound {

  private NativeSound sound;

  private boolean looping;

  private boolean isPlaying;

  private SoundChannel soundChannel;

  @FlashImport({"flash.media.SoundChannel"})
  final static class SoundChannel extends JavaScriptObject {
    protected SoundChannel() {}
    public native void stop() /*-{
      this.stop();
    }-*/;
    /**
     * @param volume
     */
    public native void setVolume(float volume) /*-{
      this.soundTransform.volume = volume;
      
    }-*/;  
  }
  
  @FlashImport({"flash.net.URLRequest", "flash.media.Sound"})
  final static class NativeSound extends JavaScriptObject {

    protected NativeSound() {
    }

    public static native NativeSound createSound(String uri) /*-{
      var s = new flash.media.Sound();
      s.load(new URLRequest(uri));
      return s;
    }-*/;

    public native SoundChannel play(boolean looping) /*-{
      this.play(0, looping ? 99999999 : 0);
    }-*/;
  }

  public static FlashSound createSound(String uri) {
    return new FlashSound(NativeSound.createSound(uri));
  }

  public FlashSound(NativeSound sound) {
    this.sound = sound;
  }

  @Override
  public void stop() {
    isPlaying = false;
    if (soundChannel != null) {
      soundChannel.stop();
    }
  }

  @Override
  public void setLooping(boolean looping) {
    this.looping = looping;
  }

  /* (non-Javadoc)
   * @see forplay.core.Sound#isPlaying()
   */
  @Override
  public boolean isPlaying() {
    // TODO Auto-generated method stub
    return isPlaying;
  }

  /* (non-Javadoc)
   * @see forplay.core.Sound#setVolume(float)
   */
  @Override
  public void setVolume(float volume) {
    if (soundChannel != null) {
      soundChannel.setVolume(volume);
    }
    
  }

  /* (non-Javadoc)
   * @see forplay.core.Sound#play()
   */
  @Override
  public boolean play() {
    soundChannel = sound.play(looping);
    isPlaying = true;
    return true;
  }
}
