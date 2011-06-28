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

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ResourcePrototype;
import com.google.gwt.user.client.Window;
import com.google.gwt.xhr.client.ReadyStateChangeHandler;
import com.google.gwt.xhr.client.XMLHttpRequest;

import forplay.core.AbstractCachingAssetManager;
import forplay.core.AutoClientBundleWithLookup;
import forplay.core.ForPlay;
import forplay.core.Image;
import forplay.core.ResourceCallback;
import forplay.core.Sound;
import forplay.html.XDomainRequest.Handler;

public class HtmlAssetManager extends AbstractCachingAssetManager {

  /**
   * Whether or not to log successful progress of {@code XMLHTTPRequest} and
   * {@code XDomainRequest} requests.
   */
  private static final boolean LOG_XHR_SUCCESS = false;

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
    final String fullPath = pathPrefix + path;
    /*
     * Except for IE, all browsers support on-domain and cross-domain XHR via
     * {@code XMLHTTPRequest}. IE, on the other hand, not only requires the use
     * of a non-standard {@code XDomainRequest} for cross-domain requests, but
     * doesn't allow on-domain requests to be issued via {@code XMLHTTPRequest},
     * even when {@code Access-Control-Allow-Origin} includes the current
     * document origin. Since we here don't always know if the current request
     * will be cross domain, we try XHR, and then fall back to XDR if the we're
     * running on IE.
     */
    try {
      doXhr(fullPath, callback);
    } catch (JavaScriptException e) {
      if (Window.Navigator.getUserAgent().indexOf("MSIE") != -1) {
        doXdr(fullPath, callback);
      } else {
        throw e;
      }
    }
  }

  private void doXdr(final String fullPath, final ResourceCallback<String> callback) {
    XDomainRequest xdr = XDomainRequest.create();
    xdr.setHandler(new Handler() {

      @Override
      public void onTimeout(XDomainRequest xdr) {
        ForPlay.log().error("xdr::onTimeout[" + fullPath + "]()");
        callback.error(new RuntimeException("Error getting " + fullPath + " : " + xdr.getStatus()));
      }

      @Override
      public void onProgress(XDomainRequest xdr) {
        if (LOG_XHR_SUCCESS) {
          ForPlay.log().debug("xdr::onProgress[" + fullPath + "]()");
        }
      }

      @Override
      public void onLoad(XDomainRequest xdr) {
        if (LOG_XHR_SUCCESS) {
          ForPlay.log().debug("xdr::onLoad[" + fullPath + "]()");
        }
        callback.done(xdr.getResponseText());
      }

      @Override
      public void onError(XDomainRequest xdr) {
        ForPlay.log().error("xdr::onError[" + fullPath + "]()");
        callback.error(new RuntimeException("Error getting " + fullPath + " : " + xdr.getStatus()));
      }
    });

    if (LOG_XHR_SUCCESS) {
      ForPlay.log().debug("xdr.open('GET', '" + fullPath + "')...");
    }
    xdr.open("GET", fullPath);

    if (LOG_XHR_SUCCESS) {
      ForPlay.log().debug("xdr.send()...");
    }
    xdr.send();
  }

  private void doXhr(final String fullPath, final ResourceCallback<String> callback) {
    XMLHttpRequest xhr = XMLHttpRequest.create();
    xhr.setOnReadyStateChange(new ReadyStateChangeHandler() {
      @Override
      public void onReadyStateChange(XMLHttpRequest xhr) {
        int readyState = xhr.getReadyState();
        if (readyState == XMLHttpRequest.DONE) {
          int status = xhr.getStatus();
          // status code 0 will be returned for non-http requests, e.g. file://
          if (status != 0 && (status < 200 || status >= 400)) {
            ForPlay.log().error(
                "xhr::onReadyStateChange[" + fullPath + "](readyState = " + readyState
                    + "; status = " + status + ")");
            callback.error(new RuntimeException("Error getting " + fullPath + " : "
                + xhr.getStatusText()));
          } else {
            if (LOG_XHR_SUCCESS) {
              ForPlay.log().debug(
                  "xhr::onReadyStateChange[" + fullPath + "](readyState = " + readyState
                      + "; status = " + status + ")");
            }
            callback.done(xhr.getResponseText());
          }
        }
      }
    });

    if (LOG_XHR_SUCCESS) {
      ForPlay.log().debug("xhr.open('GET', '" + fullPath + "')...");
    }
    xhr.open("GET", fullPath);

    if (LOG_XHR_SUCCESS) {
      ForPlay.log().debug("xhr.send()...");
    }
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
    String key = fullPath.substring(fullPath.lastIndexOf('/') + 1);
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
    /*
     * When the server provides an appropriate {@literal
     * Access-Control-Allow-Origin} response header, allow images to be served
     * cross origin on supported, CORS enabled, browsers.
     */
    setCrossOrigin(img, "anonymous");
    img.setSrc(url);
    return new HtmlImage(img);
  }

  /**
   * Set the state of the {@code crossOrigin} attribute for CORS.
   * 
   * @param elem the DOM element on which to set the {@code crossOrigin}
   *          attribute
   * @param state one of {@code "anonymous"} or {@code "use-credentials"}
   */
  private native void setCrossOrigin(Element elem, String state) /*-{
    if ('crossOrigin' in elem) {
      elem.setAttribute('crossOrigin', state);
    }
  }-*/;
}
