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

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
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
import com.google.gwt.typedarrays.shared.TypedArrays;
import com.google.gwt.user.client.Window;
import com.google.gwt.xhr.client.ReadyStateChangeHandler;
import com.google.gwt.xhr.client.XMLHttpRequest;

import playn.core.*;
import playn.html.XDomainRequest.Handler;
import react.Function;
import react.RFuture;
import react.RPromise;

public class HtmlAssets extends Assets {

  /**
   * Whether or not to log successful progress of {@code XMLHTTPRequest} and
   * {@code XDomainRequest} requests.
   */
  private static final boolean LOG_XHR_SUCCESS = false;

  private final HtmlPlatform plat;
  private final Map<String, AutoClientBundleWithLookup> clientBundles =
    new HashMap<String, AutoClientBundleWithLookup>();
  private String pathPrefix = GWT.getModuleBaseForStaticFiles();
  private Scale assetScale = null;
  private ImageManifest imageManifest;

  /** See {@link #setImageManifest}. */
  public interface ImageManifest {
    /** Returns the pixel {@code {width, height}} for the image at {@code path}.
      * The path <em>will not</em> contain any configured path prefix; it will be the path passed
      * to {@link #getImageSync}. */
    int[] imageSize(String path);
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
    if (imageManifest == null) throw new UnsupportedOperationException(
      "getImageSync(" + path + ")");
    else {
      for (Scale.ScaledResource rsrc : assetScale().getScaledResources(path)) {
        int[] size = imageManifest.imageSize(rsrc.path);
        if (size == null) continue; // try other scales
        return getImage(rsrc.path, rsrc.scale).preload(size[0], size[1]);
      }
      return new HtmlImage(plat.graphics(), new Throwable("Image missing from manifest: " + path));
    }
  }

  @Override public Image getImage (String path) {
    Scale assetScale = (this.assetScale == null) ? Scale.ONE : this.assetScale;
    List<Scale.ScaledResource> rsrcs = assetScale.getScaledResources(path);
    return getImage(rsrcs.get(0).path, rsrcs.get(0).scale);
  }

  protected HtmlImage getImage (String path, Scale scale) {
    String url = pathPrefix + path;
    AutoClientBundleWithLookup clientBundle = getBundle(path);
    if (clientBundle != null) {
      String key = getKey(path);
      ImageResource resource = (ImageResource) getResource(key, clientBundle);
      if (resource != null) url = resource.getSafeUri().asString();
    }
    return adaptImage(url, scale);
  }

  @Override public Image getRemoteImage(String url) {
    return adaptImage(url, Scale.ONE);
  }

  @Override public Image getRemoteImage(String url, int width, int height) {
    return adaptImage(url, Scale.ONE).preload(width, height);
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
    return plat.audio().createSound(url);
  }

  @Override
  public String getTextSync(String path) throws Exception {
    throw new UnsupportedOperationException("getTextSync(" + path + ")");
  }

  @Override
  public RFuture<String> getText(final String path) {
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
      return doXhr(fullPath, XMLHttpRequest.ResponseType.Default).
        map(new Function<XMLHttpRequest,String>() {
          public String apply (XMLHttpRequest xhr) {
            return xhr.getResponseText();
          }
        });
    } catch (JavaScriptException e) {
      if (Window.Navigator.getUserAgent().indexOf("MSIE") != -1) {
        return doXdr(fullPath).map(new Function<XDomainRequest,String>() {
          public String apply (XDomainRequest xdr) {
            return xdr.getResponseText();
          }
        });
      } else {
        throw e;
      }
    }
  }

  @Override
  public ByteBuffer getBytesSync(String path) throws Exception {
    throw new UnsupportedOperationException("getByteSync(" + path + ")");
  }

  @Override
  public RFuture<ByteBuffer> getBytes(final String path) {
    if (!TypedArrays.isSupported()) return RFuture.failure(
      new UnsupportedOperationException("TypedArrays not supported by this browser."));
    return doXhr(pathPrefix + path, XMLHttpRequest.ResponseType.ArrayBuffer).
      map(new Function<XMLHttpRequest,ByteBuffer>() {
        public ByteBuffer apply (XMLHttpRequest xhr) {
          return TypedArrayHelper.wrap(xhr.getResponseArrayBuffer());
        }
      });
  }

  @Override protected ImageImpl.Data load (String path) throws Exception {
    throw new UnsupportedOperationException("unused");
  }

  @Override protected ImageImpl createImage (boolean async, int rwid, int rhei, String source) {
    throw new UnsupportedOperationException("unused");
  }

  HtmlAssets(HtmlPlatform plat) {
    super(plat.exec());
    this.plat = plat;
  }

  private Scale assetScale() {
    return (assetScale != null) ? assetScale : plat.graphics().scale();
  }

  private RFuture<XDomainRequest> doXdr(final String path) {
    final RPromise<XDomainRequest> result = RPromise.create();
    XDomainRequest xdr = XDomainRequest.create();
    xdr.setHandler(new Handler() {
      @Override public void onTimeout(XDomainRequest xdr) {
        plat.log().error("xdr::onTimeout[" + path + "]()");
        result.fail(new Exception("Error getting " + path + " : " + xdr.getStatus()));
      }

      @Override public void onProgress(XDomainRequest xdr) {
        if (LOG_XHR_SUCCESS) plat.log().debug("xdr::onProgress[" + path + "]()");
      }

      @Override public void onLoad(XDomainRequest xdr) {
        if (LOG_XHR_SUCCESS) plat.log().debug("xdr::onLoad[" + path + "]()");
        result.succeed(xdr);
      }

      @Override public void onError(XDomainRequest xdr) {
        plat.log().error("xdr::onError[" + path + "]()");
        result.fail(new Exception("Error getting " + path + " : " + xdr.getStatus()));
      }
    });
    if (LOG_XHR_SUCCESS) plat.log().debug("xdr.open('GET', '" + path + "')...");
    xdr.open("GET", path);
    if (LOG_XHR_SUCCESS) plat.log().debug("xdr.send()...");
    xdr.send();
    return result;
  }

  private RFuture<XMLHttpRequest> doXhr(final String path, XMLHttpRequest.ResponseType rtype) {
    final RPromise<XMLHttpRequest> result = RPromise.create();
    XMLHttpRequest xhr = XMLHttpRequest.create();

    // IE needs the XHR to be opened before setting the response type
    if (LOG_XHR_SUCCESS) plat.log().debug("xhr.open('GET', '" + path + "')...");
    xhr.open("GET", path);

    xhr.setResponseType(rtype);
    xhr.setOnReadyStateChange(new ReadyStateChangeHandler() {
      @Override public void onReadyStateChange(XMLHttpRequest xhr) {
        int readyState = xhr.getReadyState();
        if (readyState == XMLHttpRequest.DONE) {
          int status = xhr.getStatus();
          // status code 0 will be returned for non-http requests, e.g. file://
          if (status != 0 && (status < 200 || status >= 400)) {
            plat.log().error("xhr::onReadyStateChange[" + path + "]" +
                             "(readyState = " + readyState + "; status = " + status + ")");
            result.fail(new Exception("Error getting " + path + " : " + xhr.getStatusText()));
          } else {
            if (LOG_XHR_SUCCESS) plat.log().debug("xhr::onReadyStateChange[" + path + "]" +
                                                  "(readyState = " + readyState +
                                                  "; status = " + status + ")");
            result.succeed(xhr);
          }
        }
      }
    });

    if (LOG_XHR_SUCCESS) plat.log().debug("xhr.send()...");
    xhr.send();
    return result;
  }

  private String getKey (String fullPath) {
    String key = fullPath.substring(fullPath.lastIndexOf('/') + 1);
    int dotCharIdx = key.indexOf('.');
    return dotCharIdx != -1 ? key.substring(0, dotCharIdx) : key;
  }

  private ResourcePrototype getResource (String key, AutoClientBundleWithLookup clientBundle) {
    ResourcePrototype resource = clientBundle.getResource(key);
    return resource;
  }

  private AutoClientBundleWithLookup getBundle (String collection) {
    AutoClientBundleWithLookup clientBundle = null;
    for (Map.Entry<String, AutoClientBundleWithLookup> entry : clientBundles.entrySet()) {
      String regExp = entry.getKey();
      if (RegExp.compile(regExp).exec(collection) != null) {
        clientBundle = entry.getValue();
      }
    }
    return clientBundle;
  }

  private HtmlImage adaptImage (String url, Scale scale) {
    ImageElement img = Document.get().createImageElement();
    // when the server provides an appropriate `Access-Control-Allow-Origin` response header,
    // allow images to be served cross origin on supported, CORS enabled, browsers
    setCrossOrigin(img, "anonymous");
    img.setSrc(url);
    return new HtmlImage(plat.graphics(), scale, img, url);
  }

  /**
   * Set the state of the {@code crossOrigin} attribute for CORS.
   *
   * @param elem the DOM element on which to set the {@code crossOrigin} attribute
   * @param state one of {@code "anonymous"} or {@code "use-credentials"}
   */
  private native void setCrossOrigin(Element elem, String state) /*-{
    if ('crossOrigin' in elem) elem.setAttribute('crossOrigin', state);
  }-*/;
}
