/**
 * Copyright 2012 The PlayN Authors
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
package playn.tests.core;

import java.nio.ByteBuffer;

import react.RFuture;
import react.SignalView;
import react.Slot;

import playn.core.*;
import playn.scene.*;
import static playn.tests.core.TestsGame.game;

public class NetTest extends Test {

  private ImageLayer output;
  private String lastPostURL;
  private Net.WebSocket _websock;

  public NetTest (TestsGame game) {
    super(game, "Net", "Tests network support.");
  }

  @Override public void init() {
    output = new ImageLayer();
    game.rootLayer.addAt(output, 10, 60);
    displayText("HTTP response shown here.");

    float x = 10;
    x = addButton("Google", x, 10, () -> loadURL("http://www.google.com/"));

    x = addButton("Enter URL", x, 10,
                  () -> getText("Enter URL:").onSuccess(withText(this::loadURL)));

    x = addButton("Post Test", x, 10, () -> getText("Enter POST body:").onSuccess(withText(data -> {
      Net.Builder b = game.net.req("http://www.posttestserver.com/post.php").setPayload(data);
      // don't add the header on HTML because it causes CORS freakoutery
      if (game.plat.type() != Platform.Type.HTML) {
        b.addHeader("playn-test", "we love to test!");
      }
      b.execute().onFailure(this::displayError).onSuccess(rsp -> {
        String[] lines = rsp.payloadString().split("[\r\n]+");
        String urlPre = "View it at ";
        for (String line : lines) {
          System.err.println(line + " " + line.startsWith(urlPre) + " " + urlPre);
          if (line.startsWith(urlPre)) {
            lastPostURL = line.substring(urlPre.length());
            break;
          }
        }
        displayResult(rsp);
      });
    })));

    x = addButton("Fetch Posted Body", x, 10, () -> {
      if (lastPostURL == null) displayText("Click 'Post Test' to post some data first.");
      else game.net.req(lastPostURL).execute().
        onFailure(this::displayError).
        onSuccess(this::displayResult);
    });

    x = addButton("WS Connect", x, 10, () -> {
      if (_websock != null) displayText("Already connected.");
      _websock = game.net.createWebSocket("ws://echo.websocket.org", new Net.WebSocket.Listener() {
        public void onOpen() {
          displayText("WebSocket connected.");
        }
        public void onTextMessage(String msg) {
          displayText("Got WebSocket message: " + msg);
        }
        public void onDataMessage(ByteBuffer msg) {
          displayText("Got WebSocket data message: " + msg.limit());
        }
        public void onClose() {
          displayText("WebSocket closed.");
          _websock = null;
        }
        public void onError(String reason) {
          displayText("Got WebSocket error: " + reason);
          _websock = null;
        }
      });
      displayText("WebSocket connection started.");
    });

    x = addButton("WS Send", x, 10, () -> {
      if (_websock == null) displayText("WebSocket not open.");
      else getText("Enter message:").onSuccess(withText(msg -> {
        if (_websock == null) displayText("WebSocket disappeared.");
        else {
          _websock.send(msg);
          displayText("WebSocket sent: " + msg);
        }
      }));
    });

    x = addButton("WS Close", x, 10, () -> {
      if (_websock == null) displayText("WebSocket not open.");
      else _websock.close();
    });
  }

  protected RFuture<String> getText (String label) {
    return game.input.getText(Keyboard.TextType.DEFAULT, label, "");
  }

  protected void loadURL (String url) {
    displayText("Loading: " + url);
    try {
      game.net.req(url).execute().onSuccess(this::displayResult).onFailure(this::displayError);
    } catch (Exception e) {
      displayText(e.toString());
    }
  }

  protected void displayText (String text) {
    output.setTile(game.ui.wrapText(text, game.graphics.viewSize.width()-20, TextBlock.Align.LEFT));
  }

  protected void displayResult (Net.Response rsp) {
    StringBuilder buf = new StringBuilder();
    buf.append("Response code: ").append(rsp.responseCode());
    buf.append("\n\nHeaders:\n");
    for (String header : rsp.headerNames()) {
      buf.append(header).append(":");
      int vv = 0;
      for (String value : rsp.headers(header)) {
        if (vv++ > 0) buf.append(",");
        buf.append(" ").append(value);
      }
      buf.append("\n");
    }
    buf.append("\nBody:\n");
    String payload = rsp.payloadString();
    if (payload.length() > 1024) payload = payload.substring(0, 1024) + "...";
    buf.append(payload);
    displayText(buf.toString());
  }

  protected void displayError (Throwable error) {
    displayText(error.toString());
  }

  interface TextCB { void gotText (String text); };
  protected SignalView.Listener<String> withText (TextCB cb) {
    return text -> {
      if (text != null && text.length() > 0) cb.gotText(text);
    };
  }
}
