/**
 * Copyright 2011 The PlayN Authors
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
package playn.flash;

import com.google.gwt.core.client.JavaScriptObject;

import flash.events.Event;
import flash.events.EventDispatcher;
import flash.events.EventType;
import flash.gwt.FlashImport;
import playn.core.AbstractSound;

class FlashSound extends AbstractSound<FlashSound.NativeSound> {

  private SoundChannel soundChannel;

  @FlashImport({"flash.media.SoundChannel"})
  final static class SoundChannel extends EventDispatcher {
    public static final EventType SOUND_COMPLETE = EventType.make("soundComplete");
    protected SoundChannel() {}
    public native void stop() /*-{
      this.stop();
    }-*/;
    public native float volume() /*-{
      return this.soundTransform.volume;
    }-*/;
    public native void setVolume(float volume) /*-{
      this.soundTransform.volume = volume;
    }-*/;
  }

  @FlashImport({
    "flash.net.URLRequest", "flash.media.Sound", "flash.events.Event", "flash.events.IOErrorEvent"
  })
  final static class NativeSound extends JavaScriptObject {
    protected NativeSound() {}

    public static native void loadSound(String uri, FlashSound flashSound) /*-{
      var s = new flash.media.Sound();
      s.addEventListener(Event.COMPLETE, function(event) {
        flashSound.@playn.core.AbstractSound::onLoaded(Ljava/lang/Object;)(s);
      });
      s.addEventListener(IOErrorEvent.IO_ERROR, function(event) {
        flashSound.@playn.core.AbstractSound::onLoadError(Ljava/lang/Throwable;)(new Error("IOErrorEvent.IO_ERROR"));
      });
      s.load(new URLRequest(uri));
    }-*/;

    public native SoundChannel play(boolean looping) /*-{
      return this.play(0, looping ? 99999999 : 0);
    }-*/;
  }

  public FlashSound(String url) {
    NativeSound.loadSound(url, this);
  }

  @Override
  protected boolean playImpl() {
    soundChannel = impl.play(looping);
    soundChannel.setVolume(volume);
    soundChannel.addEventListener(SoundChannel.SOUND_COMPLETE, new EventHandler<Event>() {
      @Override
      public void handleEvent(Event evt) {
        playing = false;
        soundChannel = null;
      }
    }, false, 0, false);
    return true;
  }

  @Override
  protected void stopImpl() {
    if (soundChannel != null) {
      soundChannel.stop();
      soundChannel = null;
    }
  }

  @Override
  protected void setLoopingImpl(boolean looping) {
    // nothing to do here, we pass looping into impl.play()
  }

  @Override
  protected void setVolumeImpl(float volume) {
    if (soundChannel != null) {
      soundChannel.setVolume(volume);
    }
  }
}
