// Copyright 2011 Google Inc. All Rights Reserved.

package forplay.html;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Internet Explorer's {@code XDomainRequest} object, which must be used instead of
 * {@link XMLHttpRequest} for cross domain requests.
 * 
 * @see http://msdn.microsoft.com/en-us/library/cc288060(v=vs.85).aspx
 */
public final class XDomainRequest extends JavaScriptObject {

  public static interface Handler {
    void onError(XDomainRequest xdr);

    void onLoad(XDomainRequest xdr);

    void onProgress(XDomainRequest xdr);

    void onTimeout(XDomainRequest xdr);
  }

  public static native XDomainRequest create() /*-{
    return new $wnd.XDomainRequest();
  }-*/;

  protected XDomainRequest() {
  }

  /**
   * Aborts the current request.
   */
  public native void abort() /*-{
    this.abort();
  }-*/;

  /**
   * Gets the response text.
   * 
   * @return the response text
   */
  public native String getResponseText() /*-{
    return this.responseText;
  }-*/;

  /**
   * Gets the content type.
   * 
   * @return the content type
   */
  public native String getStatus() /*-{
    return this.contentType;
  }-*/;

  public native int getTimeout() /*-{
    return this.timeout;
  }-*/;

  /**
   * Opens an asynchronous connection.
   * 
   * @param httpMethod the HTTP method to use, one of {@literal GET} or {@literal POST}
   * @param url the URL to be opened
   */
  public native void open(String httpMethod, String url) /*-{
    this.open(httpMethod, url, true);
  }-*/;

  /**
   * Initiates a request with no request data.
   */
  public native void send() /*-{
    this.send();
  }-*/;

  /**
   * Initiates a request with data. If there is no data, specify null.
   * 
   * @param requestData the data to be sent with the request
   */
  public native void send(String requestData) /*-{
    this.send(requestData);
  }-*/;

  /**
   * Sets the {@link Handler} to be notified when the object's state changes.
   * 
   * @param handler the handler to be called when the state changes
   */
  public native void setHandler(Handler handler) /*-{
    // The 'this' context is always supposed to point to the xdr object in the handler, but we
    // reference it via closure to be extra sure.
    var _this = this;

    this.onerror = $entry(function() {
      handler.@forplay.html.XDomainRequest.Handler::onError(Lforplay/html/XDomainRequest;)(_this);
    });

    this.onload = $entry(function() {
      handler.@forplay.html.XDomainRequest.Handler::onLoad(Lforplay/html/XDomainRequest;)(_this);
    });

    this.onprogress = $entry(function() {
      handler.@forplay.html.XDomainRequest.Handler::onProgress(Lforplay/html/XDomainRequest;)(_this);
    });

    this.ontimeout = $entry(function() {
      handler.@forplay.html.XDomainRequest.Handler::onTimeout(Lforplay/html/XDomainRequest;)(_this);
    });
  }-*/;
}
