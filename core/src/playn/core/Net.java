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
package playn.core;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import react.Function;
import react.RFuture;

/**
 * PlayN network interface.
 */
public abstract class Net {

  /** Encapsulates a web socket. */
  public static interface WebSocket {
    /** Notifies game of web socket events. */
    interface Listener {
      /** Reports that a requested web socket is now open and ready for use. */
      void onOpen();

      /** Reports that a text message has arrived on a web socket. */
      void onTextMessage(String msg);

      /** Reports that a binary message has arrived on a web socket. */
      void onDataMessage(ByteBuffer msg);

      /** Reports that a web socket has been closed. */
      void onClose();

      /** Reports that a web socket has encountered an error.
       * TODO: is it closed as a result of this? */
      void onError(String reason);
    }

    /** Requests that this web socket be closed. This will result in a call to {@link
     * Listener#onClose} when the socket closure is completed. */
    void close();

    /** Queues the supplied text message to be sent over the socket. */
    void send(String data);

    /** Queues the supplied binary message to be sent over the socket. */
    void send(ByteBuffer data);
  }

  /** Used to report HTTP error responses by {@link #get} and {@link #post}. */
  public static class HttpException extends IOException {
    /** The HTTP error code reported by the server. */
    public final int errorCode;

    public HttpException(int errorCode, String message) {
      super(message);
      this.errorCode = errorCode;
    }

    @Override
    public String toString() {
      String msg = getLocalizedMessage();
      return "HTTP " + errorCode + (msg == null ? "" : (": " + msg));
    }
  }

  /** Contains data for an HTTP header. Used by {@link Builder}. */
  public static class Header {
    public final String name, value;

    public Header(String name, String value) {
      this.name = name;
      this.value = value;
    }
  }

  /** Builds a request and allows it to be configured and executed. */
  public class Builder {
    public final String url;
    public final List<Header> headers = new ArrayList<>();
    public String contentType = "text/plain";
    public String payloadString;
    public byte[] payloadBytes;

    /** Configures the payload of this request as a UTF-8 string with content type "text/plain".
     * This converts the request to a POST. */
    public Builder setPayload(String payload) {
      return setPayload(payload, "text/plain");
    }

    /** Configures the payload of this request as a UTF-8 string with content type configured as
     * "{@code contentType}; charset=UTF-8". The supplied content type should probably be something
     * like {@code text/plain} or {@code text/xml} or {@code application/json}. This converts the
     * request to a POST. */
    public Builder setPayload(String payload, String contentType) {
      this.payloadString = payload;
      this.contentType = contentType;
      return this;
    }

    /** Configures the payload of this request as raw bytes with content type
     * "application/octet-stream". This converts the request to a POST. */
    public Builder setPayload(byte[] payload) {
      return setPayload(payload, "application/octet-stream");
    }

    /** Configures the payload of this request as raw bytes with the specified content type. This
     * converts the request to a POST. */
    public Builder setPayload(byte[] payload, String contentType) {
      this.payloadBytes = payload;
      this.contentType = contentType;
      return this;
    }

    /** Adds the supplied request header.
     * @param name the name of the header (e.g. {@code Authorization}).
     * @param value the value of the header. */
    public Builder addHeader(String name, String value) {
      headers.add(new Header(name, value));
      return this;
    }

    /** Executes this request, delivering the response via {@code callback}. */
    public RFuture<Response> execute() {
      return Net.this.execute(this);
    }

    public boolean isPost() {
      return payloadString != null || payloadBytes != null;
    }

    public String method() {
      return isPost() ? "POST" : "GET";
    }

    public String contentType() {
      return contentType + (payloadString != null ? ("; charset=" + UTF8) : "");
    }

    protected Builder(String url) {
      assert url.startsWith("http:") || url.startsWith("https:") :
        "Only http and https URLs are supported";
      this.url = url;
    }
  }

  /** Communicates an HTTP response to the caller. */
  public static abstract class Response {
    private int responseCode;
    private Map<String,List<String>> headersMap;

    /** Used to deliver binary response data. */
    public static abstract class Binary extends Response {
      private final byte[] payload;
      private final String encoding;

      public Binary(int responseCode, byte[] payload, String encoding) {
        super(responseCode);
        this.payload = payload;
        this.encoding = encoding;
      }

      @Override public String payloadString() {
        try {
          return new String(payload, encoding);
        } catch (UnsupportedEncodingException uee) {
          return uee.toString();
        }
      }

      @Override public byte[] payload() {
        return payload;
      }
    }

    /** Returns the HTTP response code provided by the server. */
    public int responseCode() {
      return this.responseCode;
    }

    /** Returns the names of all headers returned by the server. */
    public Iterable<String> headerNames() {
      return headers().keySet();
    }

    /** Returns the value of the header with the specified name, or null. If there are multiple
     * response headers with this name, one will be chosen using an undefined algorithm. */
    public String header(String name) {
      List<String> values = headers().get(name);
      return (values == null) ? null : values.get(0);
    }

    /** Returns the value of all headers with the specified name, or the empty list.
     * <p><em>NOTE:</em> on the iOS backend, repeated headers will be coalesced into a single
     * header separated by commas. This sucks but we can't "undo" the coalescing without breaking
     * otherwise normal headers that happent to contain commas. Complain to Apple.</p> */
    public List<String> headers(String name) {
      List<String> values = headers().get(name);
      return values == null ? Collections.<String>emptyList() : values;
    }

    /** Returns the response payload as a string, decoded using the character set specified in the
     * response's content type. */
    public abstract String payloadString();

    /** Returns the response payload as raw bytes. Note: this is not available on the HTML
     * backend. */
    public byte[] payload() {
      throw new UnsupportedOperationException();
    }

    /**
     * Returns the response payload as an {@link Image}. Note: this is not available on the HTML
     * backend.
     * @param scale the scale to use for the loaded image. Probably {@code Scale.ONE} unless you
     * know you're loading a HiDPI image.
     * @throws Exception if image decoding fails.
     */
    public Image payloadImage(Scale scale) throws Exception {
      throw new UnsupportedOperationException();
    }

    protected Response(int responseCode) {
      this.responseCode = responseCode;
    }

    protected abstract Map<String,List<String>> extractHeaders();

    private Map<String,List<String>> headers() {
      if (headersMap == null) {
        headersMap = extractHeaders();
      }
      return headersMap;
    }
  }

  protected static final String UTF8 = "UTF-8";

  /**
   * Create a websocket with given URL and listener.
   */
  public WebSocket createWebSocket(String url, WebSocket.Listener listener) {
    throw new UnsupportedOperationException();
  }

  /**
   * Performs an HTTP GET request to the specified URL.
   */
  public RFuture<String> get(String url) {
    return req(url).execute().map(GET_PAYLOAD);
  }

  /**
   * Performs an HTTP POST request to the specified URL.
   */
  public RFuture<String> post(String url, String data) {
    return req(url).setPayload(data).execute().map(GET_PAYLOAD);
  }

  /**
   * Creates a builder for a request with the specified URL.
   */
  public Builder req (String url) {
    return new Builder(url);
  }

  protected RFuture<Response> execute(Builder req) {
    return RFuture.failure(new UnsupportedOperationException());
  }

  private static final Function<Response,String> GET_PAYLOAD = new Function<Response,String>() {
    public String apply (Response rsp) { return rsp.payloadString(); }
  };
}
