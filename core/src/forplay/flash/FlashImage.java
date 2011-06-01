/**
 * Copyright 2010 The ForPlay Authors
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
package forplay.flash;

import flash.gwt.FlashImport;

import java.util.ArrayList;
import java.util.List;

import forplay.core.Image;
import forplay.core.ResourceCallback;
import flash.display.BitmapData;

@FlashImport({"flash.display.Loader", "flash.events.Event", "flash.net.URLRequest"})
class FlashImage implements Image {
  private List<ResourceCallback<Image>> callbacks = new ArrayList<ResourceCallback<Image>>();
  
  private BitmapData imageData = null;
  FlashImage(String url) {
    scheduleLoad(url);
  }

  /**
   * @param url
   */
  private native void scheduleLoad(String url) /*-{
     var loader = new Loader();
     var self = this;
     loader.contentLoaderInfo.addEventListener(flash.events.Event.COMPLETE,
        function(event) {
          self.@forplay.flash.FlashImage::imageData = event.target.content.bitmapData;
          self.@forplay.flash.FlashImage::runCallbacks(Z)(true);
        }); 
     loader.load(new URLRequest(url));   
  }-*/;

  @Override
  public int height() {
    return imageData == null ? 0 : imageData.getHeight();
  }

  @Override
  public void replaceWith(Image image) {
    imageData = ((FlashImage) image).imageData;
  }

  private void runCallbacks(boolean success) {
    for (ResourceCallback<Image> cb : callbacks) {
      if (success) {
        cb.done(this);
      } else {
        cb.error(new Exception("Error loading image"));
      }
    }
    callbacks.clear();
  }
  
  @Override
  public int width() {
    return imageData == null ? 0 : imageData.getWidth();
  }

  @Override
  public boolean isReady() {
    return imageData != null;
  }

  /* (non-Javadoc)
   * @see forplay.core.Image#addCallback(forplay.core.ResourceCallback)
   */
  @Override
  public void addCallback(ResourceCallback<Image> callback) {
    callbacks.add(callback);
    if (isReady()) {
      runCallbacks(true);
    }
  }

  /**
   * @return
   */
  BitmapData bitmapData() {
    // TODO Auto-generated method stub
    return imageData;
  }
}
