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
package playn.html;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
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

import pythagoras.f.MathUtil;

import playn.core.AbstractAssets;
import playn.core.AsyncImage;
import playn.core.AutoClientBundleWithLookup;
import playn.core.Image;
import playn.core.PlayN;
import playn.core.Sound;
import playn.core.gl.Scale;
import playn.core.util.Callback;
import playn.html.XDomainRequest.Handler;

public class HtmlAssets extends AbstractAssets<Void> {

  /**
   * Whether or not to log successful progress of {@code XMLHTTPRequest} and
   * {@code XDomainRequest} requests.
   */
  private static final boolean LOG_XHR_SUCCESS = false;

  private final HtmlPlatform platform;
  private final Map<String, AutoClientBundleWithLookup> clientBundles =
    new HashMap<String, AutoClientBundleWithLookup>();
  private String pathPrefix = "";
  private Scale assetScale = null;
  private ImageManifest imageManifest;

  /** See {@link #setImageManifest}. */
  public interface ImageManifest {
    /** Returns {@code {width, height}} for the image at {@code path}. The path <em>will not</em>
     * contain any configured path prefix; it will be the path passed to {@link #getImageSync}. */
    float[] imageSize(String path);
  }

  public void setPathPrefix(String prefix) {
    pathPrefix = prefix;
  }

  /** Configures our image manifest. This is used to support {@link #getImageSync} in the HTML
   * backend. Images for which manifest entries exist will be synchronously loadable and will be
   * configured with their correct size immediately. Their image data will still be loaded
   * asynchronously, but games that were written to use sync loaded images will most likely work
   * with this small hack. */
  public void setImageManifest(ImageManifest manifest) {
    imageManifest = manifest;
  }

  /**
   * Configures the default scale to use for assets. This allows one to specify an intermediate
   * graphics scale (like 1.5) and scale the 2x imagery down to 1.5x instead of scaling the 1.5x
   * imagery up (or displaying nothing at all).
   */
  public void setAssetScale(float scaleFactor) {
    this.assetScale = new Scale(scaleFactor);
  }

  public void addClientBundle(String regExp, AutoClientBundleWithLookup clientBundle) {
    clientBundles.put(regExp, clientBundle);
  }

  @Override
  public Image getImageSync(String path) {
    if (imageManifest == null)
      throw new UnsupportedOperationException("getImageSync(" + path + ")");
    else {
      for (Scale.ScaledResource rsrc : assetScale().getScaledResources(path)) {
        float[] size = imageManifest.imageSize(rsrc.path);
        if (size == null) continue; // try other scales
        HtmlImage image = getImage(rsrc.path, rsrc.scale);
        image.img.setWidth(MathUtil.iceil(size[0]));
        image.img.setHeight(MathUtil.iceil(size[1]));
        return image;
      }
      return createErrorImage(new Throwable("Image missing from manifest: " + path));
    }
  }

  @Override
  public Image getImage(String path) {
    return getImage(path, Scale.ONE);
  }

  protected HtmlImage getImage(String path, Scale scale) {
    String url = pathPrefix + path;
    AutoClientBundleWithLookup clientBundle = getBundle(path);
    if (clientBundle != null) {
      String key = getKey(path);
      ImageResource resource = (ImageResource) getResource(key, clientBundle);
      if (resource != null) {
        url = resource.getSafeUri().asString();
      }
    }
    return adaptImage(url, scale);
  }

  @Override
  public Image getRemoteImage(String url) {
    return adaptImage(url, Scale.ONE);
  }

  @Override
  public Image getRemoteImage(String url, float width, float height) {
    HtmlImage image = adaptImage(url, Scale.ONE);
    image.img.setWidth(MathUtil.iceil(width));
    image.img.setHeight(MathUtil.iceil(height));
    return image;
  }

  @Override
  public Sound getSound(String path) {
    String url = pathPrefix + path;
    AutoClientBundleWithLookup clientBundle = getBundle(path);
    if (clientBundle != null) {
      String key = getKey(path);
      DataResource resource = (DataResource) getResource(key, clientBundle);
      if (resource != null) {
        url = resource.getSafeUri().asString();
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
  public void getText(final String path, final Callback<String> callback) {
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

  @Override
  public byte[] getBytesSync(String path) throws Exception {
    throw new UnsupportedOperationException("getTextSync(" + path + ")");
  }

  @Override
  public void getBytes(final String path, final Callback<byte[]> callback) {
    throw new UnsupportedOperationException("getText(" + path + ")");
  }

  @Override
  protected Image createErrorImage(Throwable cause, float width, float height) {
    ImageElement img = Document.get().createImageElement();
    img.setWidth(MathUtil.iceil(width));
    img.setHeight(MathUtil.iceil(height));
    // TODO: proper error image that reports failure to callbacks
    return new HtmlImage(platform.graphics().ctx(), Scale.ONE, img);
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

  HtmlAssets(HtmlPlatform platform) {
    super(platform);
    this.platform = platform;
  }

  private Scale assetScale() {
    return (assetScale != null) ? assetScale : platform.graphics().scale();
  }

  private void doXdr(final String fullPath, final Callback<String> callback) {
    XDomainRequest xdr = XDomainRequest.create();
    xdr.setHandler(new Handler() {

      @Override
      public void onTimeout(XDomainRequest xdr) {
        PlayN.log().error("xdr::onTimeout[" + fullPath + "]()");
        callback.onFailure(
          new RuntimeException("Error getting " + fullPath + " : " + xdr.getStatus()));
      }

      @Override
      public void onProgress(XDomainRequest xdr) {
        if (LOG_XHR_SUCCESS) {
          PlayN.log().debug("xdr::onProgress[" + fullPath + "]()");
        }
      }

      @Override
      public void onLoad(XDomainRequest xdr) {
        if (LOG_XHR_SUCCESS) {
          PlayN.log().debug("xdr::onLoad[" + fullPath + "]()");
        }
        callback.onSuccess(xdr.getResponseText());
      }

      @Override
      public void onError(XDomainRequest xdr) {
        PlayN.log().error("xdr::onError[" + fullPath + "]()");
        callback.onFailure(
          new RuntimeException("Error getting " + fullPath + " : " + xdr.getStatus()));
      }
    });

    if (LOG_XHR_SUCCESS) {
      PlayN.log().debug("xdr.open('GET', '" + fullPath + "')...");
    }
    xdr.open("GET", fullPath);

    if (LOG_XHR_SUCCESS) {
      PlayN.log().debug("xdr.send()...");
    }
    xdr.send();
  }

  private void doXhr(final String fullPath, final Callback<String> callback) {
    XMLHttpRequest xhr = XMLHttpRequest.create();
    xhr.setOnReadyStateChange(new ReadyStateChangeHandler() {
      @Override
      public void onReadyStateChange(XMLHttpRequest xhr) {
        int readyState = xhr.getReadyState();
        if (readyState == XMLHttpRequest.DONE) {
          int status = xhr.getStatus();
          // status code 0 will be returned for non-http requests, e.g. file://
          if (status != 0 && (status < 200 || status >= 400)) {
            PlayN.log().error(
                "xhr::onReadyStateChange[" + fullPath + "](readyState = " + readyState
                    + "; status = " + status + ")");
            callback.onFailure(
              new RuntimeException("Error getting " + fullPath + " : " + xhr.getStatusText()));
          } else {
            if (LOG_XHR_SUCCESS) {
              PlayN.log().debug(
                  "xhr::onReadyStateChange[" + fullPath + "](readyState = " + readyState
                      + "; status = " + status + ")");
            }
            // TODO(fredsa): Remove try-catch and materialized exception once issue 6562 is fixed
            // http://code.google.com/p/google-web-toolkit/issues/detail?id=6562
            try {
              callback.onSuccess(xhr.getResponseText());
            } catch(JavaScriptException e) {
              if (GWT.isProdMode()) {
                throw e;
              } else {
                JavaScriptException materialized = new JavaScriptException(e.getName(),
                    e.getDescription());
                materialized.setStackTrace(e.getStackTrace());
                throw materialized;
              }
            }
          }
        }
      }
    });

    if (LOG_XHR_SUCCESS) {
      PlayN.log().debug("xhr.open('GET', '" + fullPath + "')...");
    }
    xhr.open("GET", fullPath);

    if (LOG_XHR_SUCCESS) {
      PlayN.log().debug("xhr.send()...");
    }
    xhr.send();
  }

  private Sound adaptSound(String url) {
    HtmlAudio audio = (HtmlAudio) PlayN.audio();
    HtmlSound sound = audio.createSound(url);
    return sound;
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

  private HtmlImage adaptImage(String url, Scale scale) {
    ImageElement img = Document.get().createImageElement();
    /*
     * When the server provides an appropriate {@literal Access-Control-Allow-Origin} response
     * header, allow images to be served cross origin on supported, CORS enabled, browsers.
     */
    setCrossOrigin(img, "anonymous");
    img.setSrc(url);
    return new HtmlImage(platform.graphics().ctx(), scale, img);
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
