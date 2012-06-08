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

import java.nio.ByteBuffer;

import playn.core.util.Callback;

/**
 * PlayN network interface.
 *
 * TODO(jgw): This is quite anemic at the moment, but it's a starting point.
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
}
