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

import playn.core.ImageLayer;
import playn.core.Keyboard;
import playn.core.Net;
import playn.core.util.Callback;
import static playn.core.PlayN.*;

public class NetTest extends Test {

  private ImageLayer output;
  private String lastPostURL;

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
        getText("Enter URL:", new Callback<String>() {
          public void onSuccess (String url) {
            if (url != null && url.length() > 0) loadURL(url);
          }
          public void onFailure (Throwable cause) {
            displayText(cause.toString());
          }
        });
      }
    }, x, 10);

    x = addButton("Post Test", new Runnable() {
      public void run () {
        getText("Enter POST body:", new Callback<String>() {
          public void onSuccess (String data) {
            if (data == null || data.length() == 0) return;
            net().req("http://www.posttestserver.com/post.php").setPayload(data).
              addHeader("playn-test", "we love to test!").
              execute(new Callback<Net.Response>() {
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
          public void onFailure (Throwable cause) {
            displayText(cause.toString());
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
  }

  protected void getText (String label, Callback<String> callback) {
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
}
