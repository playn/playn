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

import playn.core.ImageLayer;
import playn.core.Keyboard;
import playn.core.Net;
import playn.core.Platform;
import playn.core.util.Callback;
import static playn.core.PlayN.*;

public class NetTest extends Test {

  private ImageLayer output;
  private String lastPostURL;
  private Net.WebSocket _websock;

  @Override
  public String getName() {
    return "NetTest";
  }

  @Override
  public String getDescription() {
    return "Tests network support.";
  }

  @Override
  public void init() {
    output = graphics().createImageLayer();
    graphics().rootLayer().addAt(output, 10, 60);
    displayText("HTTP response shown here.");

    float x = 10;
    x = addButton("Google", new Runnable() {
      public void run () {
        loadURL("http://www.google.com/");
      }
    }, x, 10);

    x = addButton("Enter URL", new Runnable() {
      public void run () {
        getText("Enter URL:", new TextCB() {
          @Override protected void gotText (String url) {
            loadURL(url);
          }
        });
      }
    }, x, 10);

    x = addButton("Post Test", new Runnable() {
      public void run () {
        getText("Enter POST body:", new TextCB() {
          @Override protected void gotText(String data) {
            Net.Builder b = net().req("http://www.posttestserver.com/post.php").setPayload(data);
            // don't add the header on HTML because it causes CORS freakoutery
            if (platformType() != Platform.Type.HTML) {
              b.addHeader("playn-test", "we love to test!");
            }
            b.execute(new Callback<Net.Response>() {
              public void onSuccess (Net.Response rsp) {
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
              }
              public void onFailure (Throwable cause) {
                displayText(cause.toString());
              }
            });
          }
        });
      }
    }, x, 10);

    x = addButton("Fetch Posted Body", new Runnable() {
      public void run () {
        if (lastPostURL == null) displayText("Click 'Post Test' to post some data first.");
        else net().req(lastPostURL).execute(displayer);
      }
    }, x, 10);

    x = addButton("WS Connect", new Runnable() {
      public void run () {
        if (_websock != null) displayText("Already connected.");
        _websock = net().createWebSocket("ws://echo.websocket.org", new Net.WebSocket.Listener() {
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
      }
    }, x, 10);

    x = addButton("WS Send", new Runnable() {
      public void run () {
        if (_websock == null) displayText("WebSocket not open.");
        else getText("Enter message:", new TextCB() {
          @Override protected void gotText(String msg) {
            if (_websock == null) displayText("WebSocket disappeared.");
            else {
              _websock.send(msg);
              displayText("WebSocket sent: " + msg);
            }
          }
        });
      }
    }, x, 10);

    x = addButton("WS Close", new Runnable() {
      public void run () {
        if (_websock == null) displayText("WebSocket not open.");
        else _websock.close();
      }
    }, x, 10);
  }

  protected void getText (String label, TextCB callback) {
    keyboard().getText(Keyboard.TextType.DEFAULT, label, "", callback);
  }

  protected void loadURL (String url) {
    displayText("Loading: " + url);
    try {
      net().req(url).execute(displayer);
    } catch (Exception e) {
      displayText(e.toString());
    }
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

  protected void displayText (String text) {
    output.setImage(formatText(TEXT_FMT.withWrapWidth(graphics().width()-20), text, false));
  }

  private Callback<Net.Response> displayer = new Callback<Net.Response>() {
    public void onSuccess (Net.Response rsp) {
      displayResult(rsp);
    }
    public void onFailure (Throwable cause) {
    displayText(cause.toString());
    }
  };

  private abstract class TextCB implements Callback<String> {
    public void onSuccess(String text) {
      if (text != null && text.length() > 0) gotText(text);
    }
    public void onFailure (Throwable cause) {
      displayText(cause.toString());
    }
    protected abstract void gotText(String text);
  }
}
