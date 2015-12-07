/**
 * Copyright 2013 The PlayN Authors
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
package playn.android;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import playn.core.Exec;
import playn.core.Net;

public class AndroidWebSocket implements Net.WebSocket {

  private final WebSocketClient socket;

  public AndroidWebSocket(final Exec exec, String uri, final Listener listener) {
    URI juri = null;
    try {
      juri = new URI(uri);
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }

    socket = new WebSocketClient(juri, new Draft_17()) {
      @Override public void onMessage(final ByteBuffer buffer) {
        exec.invokeLater(new Runnable() { public void run() { listener.onDataMessage(buffer); }});
      }
      @Override public void onMessage(final String msg) {
        exec.invokeLater(new Runnable() { public void run() { listener.onTextMessage(msg); }});
      }
      @Override public void onError(final Exception e) {
        exec.invokeLater(new Runnable() { public void run() { listener.onError(e.getMessage()); }});
      }
      @Override public void onClose(int arg0, String arg1, boolean arg2) {
        exec.invokeLater(new Runnable() { public void run() { listener.onClose(); }});
      }
      @Override public void onOpen(ServerHandshake handshake) {
        exec.invokeLater(new Runnable() { public void run() { listener.onOpen(); }});
      }
    };
    socket.connect();
  }

  @Override public void close() {
    socket.close();
  }

  @Override public void send(String data) {
    try {
      socket.getConnection().send(data);
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  @Override public void send(ByteBuffer data) {
    try {
      socket.getConnection().send(data);
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }
}
