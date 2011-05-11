/**
 * Copyright 2010 The ForPlay Authors
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
package forplay.html;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ResourcePrototype;
import com.google.gwt.xhr.client.ReadyStateChangeHandler;
import com.google.gwt.xhr.client.XMLHttpRequest;

import forplay.core.AbstractCachingAssetManager;
import forplay.core.AutoClientBundleWithLookup;
import forplay.core.ForPlay;
import forplay.core.Image;
import forplay.core.ResourceCallback;
import forplay.core.Sound;

import java.util.HashMap;
import java.util.Map;

public class HtmlAssetManager extends AbstractCachingAssetManager {

  private String pathPrefix = "";

  public void setPathPrefix(String prefix) {
    pathPrefix = prefix;
  }

  private Map<String, AutoClientBundleWithLookup> clientBundles = new HashMap<String, AutoClientBundleWithLookup>();

  public void addClientBundle(String regExp, AutoClientBundleWithLookup clientBundle) {
    clientBundles.put(regExp, clientBundle);
  }

  @Override
  protected void doGetText(final String path, final ResourceCallback<String> callback) {
    XMLHttpRequest xhr = XMLHttpRequest.create();
    xhr.setOnReadyStateChange(new ReadyStateChangeHandler() {
      @Override
      public void onReadyStateChange(XMLHttpRequest xhr) {
        if (xhr.getReadyState() == XMLHttpRequest.DONE) {
          int status = xhr.getStatus();
          // status code of 0 will be returned for non-http requests (e.g., file://)
          if (status != 0 && (status < 200 || status >= 400)) {
            callback.error(new RuntimeException("Error getting " + pathPrefix + path + " : " + xhr.getStatusText()));
          } else {
            callback.done(xhr.getResponseText());
          }
        }
      }
    });
    xhr.open("GET", pathPrefix + path);
    xhr.send();
  }

  @Override
  protected Sound loadSound(String path) {
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

  private Sound adaptSound(String url) {
    HtmlAudio audio = (HtmlAudio) ForPlay.audio();
    HtmlSound sound = audio.createSound(url);
    return sound;
  }

  @Override
  protected Image loadImage(String path) {
    String url = pathPrefix + path;

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

  /**
   * Determine the resource key from a given path.
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
    for (Map.Entry<String, AutoClientBundleWithLookup> entry : clientBundles.entrySet()) {
      String regExp = entry.getKey();
      if (RegExp.compile(regExp).exec(collection) != null) {
        clientBundle = entry.getValue();
      }
    }
    return clientBundle;
  }

  private Image adaptImage(String url) {
    ImageElement img = Document.get().createImageElement();
    img.setSrc(url);
    return new HtmlImage(img);
  }
}
