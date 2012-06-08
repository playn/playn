/**
 * Copyright 2010 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playn.java;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import playn.core.NetImpl;
import playn.core.WebSocket;
import playn.core.util.Callback;

public class JavaNet extends NetImpl {

  private static final int BUF_SIZE = 4096;
  private List<JavaWebSocket> sockets = new ArrayList<JavaWebSocket>();

  public JavaNet(JavaPlatform platform) {
    super(platform);
  }

  @Override
  public WebSocket createWebSocket(String url, WebSocket.Listener listener) {
    JavaWebSocket socket = new JavaWebSocket(url, listener);
    sockets.add(socket);
    return socket;
  }

  @Override
  public void get(final String urlStr, final Callback<String> callback) {
    new Thread("JavaNet.get(" + urlStr + ")") {
      @Override
      public void run() {
        try {
          URL url = new URL(canonicalizeUrl(urlStr));
          InputStream stream = url.openStream();
          InputStreamReader reader = new InputStreamReader(stream);
          notifySuccess(callback, readFully(reader));

        } catch (MalformedURLException e) {
          notifyFailure(callback, e);
        } catch (IOException e) {
          notifyFailure(callback, e);
        }
      }
    }.start();
  }

  @Override
  public void post(final String urlStr, final String data, final Callback<String> callback) {
    new Thread("JavaNet.post(" + urlStr + ")") {
      public void run() {
        try {
          URL url = new URL(canonicalizeUrl(urlStr));
          HttpURLConnection conn = (HttpURLConnection) url.openConnection();
          conn.setRequestMethod("POST");
          conn.setDoOutput(true);
          conn.setDoInput(true);
          conn.setAllowUserInteraction(false);
          conn.setRequestProperty("Content-type", "text/xml; charset=UTF-8");

          conn.connect();
          conn.getOutputStream().write(data.getBytes("UTF-8"));
          conn.getOutputStream().close();
          String result = readFully(new InputStreamReader(conn.getInputStream()));
          conn.disconnect();
          notifySuccess(callback, result);

        } catch (MalformedURLException e) {
          notifyFailure(callback, e);
        } catch (IOException e) {
          notifyFailure(callback, e);
        }
      }
    }.start();
  }

  void update() {
    for (Iterator<JavaWebSocket> it = sockets.iterator(); it.hasNext(); ) {
      JavaWebSocket s = it.next();
      if (!s.update()) {
        it.remove();
      }
    }
  }

  // Super-simple url-cleanup: assumes it either starts with "http", or that
  // it's an absolute path on the current server.
  private String canonicalizeUrl(String url) {
    if (!url.startsWith("http")) {
      return "http://" + server() + url;
    }
    return url;
  }

  // TODO: Make this specifyable somewhere.
  private String server() {
    return "127.0.0.1:8080";
  }

  private String readFully(Reader reader) throws IOException {
    StringBuffer result = new StringBuffer();
    char[] buf = new char[BUF_SIZE];
    int len = 0;
    while (-1 != (len = reader.read(buf))) {
      result.append(buf, 0, len);
    }
    return result.toString();
  }
}
