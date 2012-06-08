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
package playn.java;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

import org.java_websocket.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import playn.core.Net;

public class JavaWebSocket implements Net.WebSocket {

  private abstract class Event {
    abstract boolean handle();
  }

  private class OpenEvent extends Event {
    boolean handle() {
      listener.onOpen();
      return true;
    }
  }

  private class CloseEvent extends Event {
    boolean handle() {
      listener.onClose();
      return false;
    }
  }

  private class DataMessageEvent extends Event {
    ByteBuffer dataMessage;

    DataMessageEvent(ByteBuffer buffer) {
      dataMessage = buffer;
    }

    boolean handle() {
      listener.onDataMessage(dataMessage);
      return true;
    }
  }

  private class TextMessageEvent extends Event {
    final String textMessage;

    TextMessageEvent(String message) {
      textMessage = message;
    }

    boolean handle() {
      listener.onTextMessage(textMessage);
      return true;
    }
  }

  private class ErrorEvent extends Event {
    final String reason;

    ErrorEvent(String reason) {
      this.reason = reason;
    }

    boolean handle() {
      listener.onError(reason);
      return true;
    }
  }

  private final WebSocketClient socket;
  private final Net.WebSocket.Listener listener;
  private final Queue<Event> pendingEvents = new LinkedList<Event>();

  JavaWebSocket(String uri, Listener listener) {
    this.listener = listener;

    URI juri = null;
    try {
      juri = new URI(uri);
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }

    socket = new WebSocketClient(juri) {
      @Override
      public void onMessage(ByteBuffer buffer) {
        addEvent(new DataMessageEvent(buffer));
      }

      @Override
      public void onMessage(String msg) {
        addEvent(new TextMessageEvent(msg));
      }

      @Override
      public void onError(Exception e) {
        addEvent(new ErrorEvent(e.getMessage()));
      }

      @Override
      public void onClose(int arg0, String arg1, boolean arg2) {
        addEvent(new CloseEvent());
      }

      @Override
      public void onOpen(ServerHandshake handshake) {
        addEvent(new OpenEvent());
      }
    };
    socket.connect();
  }

  @Override
  public void close() {
    socket.close();
  }

  @Override
  public void send(String data) {
    try {
      socket.send(data);
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void send(ByteBuffer data) {
    try {
      // TODO(haustein) check for underlying array or send in chunks?
      byte[] bytes = new byte[data.remaining()];
      data.get(bytes);
      socket.send(bytes);
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  synchronized boolean update() {
    Event e;
    boolean ret = true;
    while (null != (e = pendingEvents.poll())) {
      ret = e.handle();
    }
    return ret;
  }

  private synchronized void addEvent(Event e) {
    pendingEvents.add(e);
  }
}
