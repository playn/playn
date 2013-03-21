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
import java.nio.ByteBuffer;
import java.util.List;

import playn.core.util.Callback;

/**
 * PlayN network interface.
 */
public interface Net {

  /** Encapsulates a web socket. */
  interface WebSocket {
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
  class HttpException extends IOException {
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

  /** Builds a request and allows it to be configured and executed. */
  interface Builder {
    /** Configures the payload of this request as a UTF-8 string with content type "text/plain".
     * This converts the request to a POST. */
    Builder setPayload(String payload);

    /** Configures the payload of this request as a UTF-8 string with content type configured as
     * "{@code contentType}; charset=UTF-8". The supplied content type should probably be something
     * like {@code text/plain} or {@code text/xml} or {@code application/json}. This converts the
     * request to a POST. */
    Builder setPayload(String payload, String contentType);

    /** Configures the payload of this request as raw bytes with content type
     * "application/octet-stream". This converts the request to a POST. */
    Builder setPayload(byte[] payload);

    /** Configures the payload of this request as raw bytes with the specified content type. This
     * converts the request to a POST. */
    Builder setPayload(byte[] payload, String contentType);

    /** Adds the supplied request header.
     * @param name the name of the header (e.g. {@code Authorization}).
     * @param value the value of the header. */
    Builder addHeader(String name, String value);

    /** Executes this request, delivering the response via {@code callback}. */
    void execute(Callback<Response> callback);
  }

  /** Communicates an HTTP response to the caller. */
  interface Response {
    /** Returns the HTTP response code provided by the server. */
    int responseCode();

    /** Returns the names of all headers returned by the server. */
    Iterable<String> headerNames();

    /** Returns the value of the header with the specified name, or null. If there are multiple
     * response headers with this name, one will be chosen using an undefined algorithm. */
    String header(String name);

    /** Returns the value of all headers with the specified name, or the empty list.
     * <p><em>NOTE:</em> on the iOS backend, repeated headers will be coalesced into a single
     * header separated by commas. This sucks but we can't "undo" the coalescing without breaking
     * otherwise normal headers that happent to contain commas. Complain to Apple.</p> */
    List<String> headers(String name);

    /** Returns the response payload as a string, decoded using the character set specified in the
     * response's content type. */
    String payloadString();

    /** Returns the response payload as raw bytes. Note: this is not available on the HTML
     * backend. */
    byte[] payload();
  }

  /**
   * Create a websocket with given URL and listener.
   */
  WebSocket createWebSocket(String url, WebSocket.Listener listener);

  /**
   * Performs an HTTP GET request to the specified URL.
   */
  void get(String url, Callback<String> callback);

  /**
   * Performs an HTTP POST request to the specified URL.
   */
  void post(String url, String data, Callback<String> callback);

  /**
   * Creates a builder for a request with the specified URL.
   */
  Builder req(String url);
}
