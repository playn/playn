/**
 * Copyright 2010 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playn.flash;

import flash.gwt.FlashImport;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import playn.core.Image;
import playn.core.ResourceCallback;
import flash.display.BitmapData;

@FlashImport({"flash.display.Loader", "flash.events.Event", "flash.net.URLRequest"})
class FlashImage implements Image {

  private List<ResourceCallback<? super Image>> callbacks =
    new ArrayList<ResourceCallback<? super Image>>();

  private BitmapData imageData = null;

  FlashImage(String url) {
    scheduleLoad(url);
  }

  FlashImage(BitmapData data) {
    this.imageData = data;
  }

  private native void scheduleLoad(String url) /*-{
     var loader = new Loader();
     var self = this;
     loader.contentLoaderInfo.addEventListener(flash.events.Event.COMPLETE,
        function(event) {
          self.@playn.flash.FlashImage::imageData = event.target.content.bitmapData;
          self.@playn.flash.FlashImage::runCallbacks(Z)(true);
        });
      loader.addEventListener(flash.events.IOErrorEvent.IO_ERROR,  function() {} );
      loader.contentLoaderInfo.addEventListener(flash.events.IOErrorEvent.IO_ERROR,  function() {} );
     loader.load(new URLRequest(url));
  }-*/;

  @Override
  public int width() {
    return imageData == null ? 0 : imageData.getWidth();
  }

  @Override
  public int height() {
    return imageData == null ? 0 : imageData.getHeight();
  }

  @Override
  public boolean isReady() {
    return imageData != null;
  }

  @Override
  public void addCallback(ResourceCallback<? super Image> callback) {
    callbacks.add(callback);
    if (isReady()) {
      runCallbacks(true);
    }
  }

  @Override
  public Region subImage(float x, float y, float width, float height) {
    return new FlashImageRegion(this, x, y, width, height);
  }

  BitmapData bitmapData() {
    return imageData;
  }

  private void runCallbacks(boolean success) {
    for (ResourceCallback<? super Image> cb : callbacks) {
      if (success) {
        cb.done(this);
      } else {
        cb.error(new Exception("Error loading image"));
      }
    }
    callbacks.clear();
  }
}
