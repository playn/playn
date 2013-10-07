/**
 * Copyright 2010 The PlayN Authors
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

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ResourcePrototype;

import flash.gwt.FlashImport;

import playn.core.AbstractAssets;
import playn.core.AsyncImage;
import playn.core.AutoClientBundleWithLookup;
import playn.core.Image;
import playn.core.Sound;
import playn.core.gl.Scale;
import playn.core.util.Callback;

@FlashImport({"flash.net.URLLoader", "flash.net.URLRequest"})
public class FlashAssets extends AbstractAssets<Void> {

  private final Map<String, AutoClientBundleWithLookup> clientBundles =
    new HashMap<String, AutoClientBundleWithLookup>();
  private final FlashPlatform platform;
  private String pathPrefix = "";

  public FlashAssets(FlashPlatform platform) {
    super(platform);
    this.platform = platform;
  }

  public void setPathPrefix(String prefix) {
    pathPrefix = prefix;
  }

  public void addClientBundle(String regExp, AutoClientBundleWithLookup clientBundle) {
    clientBundles.put(regExp, clientBundle);
  }

  @Override
  public Image getImageSync(String path) {
    throw new UnsupportedOperationException("getImageSync(" + path + ")");
  }

  @Override
  public Image getImage(String path) {
    String url = pathPrefix + path;
    platform.log().info("Looking to load " + url);
    AutoClientBundleWithLookup clientBundle = getBundle(path);
    if (clientBundle != null) {
      String key = getKey(path);
      ImageResource resource = (ImageResource) getResource(key, clientBundle);
      if (resource != null) {
        url = resource.getURL();
      }
    }
    return adaptImage(url);
  }

  @Override
  public Image getRemoteImage(String url) {
    return adaptImage(url);
  }

  @Override
  public Image getRemoteImage(String url, float width, float height) {
    // TODO: the necessary jiggery pokery to return the desired width/height until load
    return adaptImage(url);
  }

  @Override
  public Sound getSound(String path) {
    String url = pathPrefix + path;
    AutoClientBundleWithLookup clientBundle = getBundle(path);
    if (clientBundle != null) {
      String key = getKey(path);
      DataResource resource = (DataResource) getResource(key, clientBundle);
      if (resource != null) {
        url = resource.getUrl();
      }
    } else {
      url += ".mp3";
    }
    return adaptSound(url);
  }

  @Override
  public String getTextSync(String path) throws Exception {
    throw new UnsupportedOperationException("getTextSync(" + path + ")");
  }

  @Override
  public void getText(String path, Callback<String> callback) {
    loadText(pathPrefix + path, callback);
  }

  @Override
  public byte[] getBytesSync(String path) throws Exception {
    throw new UnsupportedOperationException("getBytesSync(" + path + ")");
  }

  @Override
  public void getBytes(String path, Callback<byte[]> callback) {
    throw new UnsupportedOperationException("getBytes(" + path + ")");
  }

  @Override
  protected Image createErrorImage(Throwable cause, float width, float height) {
    // TODO: proper error image that reports failure to callbacks
    return new FlashImage("error");
  }

  @Override
  protected Image createStaticImage(Void iimpl, Scale scale) {
    throw new UnsupportedOperationException("unused");
  }

  @Override
  protected AsyncImage<Void> createAsyncImage(float width, float height) {
    throw new UnsupportedOperationException("unused");
  }

  @Override
  protected Image loadImage(String path, ImageReceiver<Void> recv) {
    throw new UnsupportedOperationException("unused");
  }

  private Sound adaptSound(String url) {
    FlashSound sound = platform.audio().createSound(url);
    return sound;
  }

  /**
   * Determine the resource key from a giveb path.
   *
   * @param fullPath full path, with or without a file extension
   * @return the key by which the resource can be looked up
   */
  private String getKey(String fullPath) {
    String key = fullPath.substring(fullPath.lastIndexOf('/')+1);
    int dotCharIdx = key.indexOf('.');
    return dotCharIdx != -1 ? key.substring(0, dotCharIdx) : key;
  }

  private ResourcePrototype getResource(String key, AutoClientBundleWithLookup clientBundle) {
    ResourcePrototype resource = clientBundle.getResource(key);
    return resource;
  }

  private AutoClientBundleWithLookup getBundle(String collection) {
    AutoClientBundleWithLookup clientBundle = null;
//    for (Map.Entry<String, AutoClientBundleWithLookup> entry : clientBundles.entrySet()) {
//      String regExp = entry.getKey();
//      if (RegExp.compile(regExp).exec(collection) != null) {
//        clientBundle = entry.getValue();
//      }
//    }
    return clientBundle;
  }

  private Image adaptImage(String url) {
    platform.log().info("Loading " + url);
    return new FlashImage(url);
  }

  static native void loadText(String path, Callback<String> callback) /*-{
     var req = new flash.net.URLRequest(path);
     var loader = new flash.net.URLLoader();
     loader.addEventListener("complete", function(evt) {
       var l2 = flash.net.URLLoader(evt.target);
       callback.@playn.core.util.Callback::onSuccess(Ljava/lang/Object;)(l2.data);
     });
     try {
       loader.load(req);
     } catch(error) {
       flash.external.ExternalInterface.call("window.console.log", "Can't load " + path + " because " + error);
     }
  }-*/;
}
