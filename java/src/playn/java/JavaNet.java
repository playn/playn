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
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import playn.core.NetImpl;
import playn.core.util.Callback;

public class JavaNet extends NetImpl {

  private static final int BUF_SIZE = 4096;
  private static final String ENCODING = "UTF-8";
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
    platform.invokeAsync(new Runnable() {
      @Override
      public void run() {
        try {
          URL url = new URL(canonicalizeUrl(urlStr));
          URLConnection conn = url.openConnection();
          if (conn instanceof HttpURLConnection) {
            processResponse((HttpURLConnection) conn, callback);
          } else {
            platform.notifySuccess(callback, readContent(conn));
          }

        } catch (MalformedURLException e) {
          platform.notifyFailure(callback, e);
        } catch (IOException e) {
          platform.notifyFailure(callback, e);
        }
      }
      @Override
      public String toString() {
        return "JavaNet.get(" + urlStr + ")";
      }
    });
  }

  @Override
  public void post(final String urlStr, final String data, final Callback<String> callback) {
    platform.invokeAsync(new Runnable() {
      @Override
      public void run() {
        try {
          URL url = new URL(canonicalizeUrl(urlStr));
          HttpURLConnection conn = (HttpURLConnection) url.openConnection();
          conn.setRequestMethod("POST");
          conn.setDoOutput(true);
          conn.setDoInput(true);
          conn.setAllowUserInteraction(false);
          conn.setRequestProperty("Content-type", "text/xml; charset=" + ENCODING);

          conn.connect();
          conn.getOutputStream().write(data.getBytes(ENCODING));
          conn.getOutputStream().close();
          processResponse(conn, callback);

        } catch (MalformedURLException e) {
          platform.notifyFailure(callback, e);
        } catch (IOException e) {
          platform.notifyFailure(callback, e);
        }
      }
      @Override
      public String toString() {
        return "JavaNet.post(" + urlStr + ")";
      }
    });
  }

  void update() {
    for (Iterator<JavaWebSocket> it = sockets.iterator(); it.hasNext(); ) {
      JavaWebSocket s = it.next();
      if (!s.update()) {
        it.remove();
      }
    }
  }

  private void processResponse(HttpURLConnection conn, Callback<String> callback)
      throws IOException {
    try {
      int code = conn.getResponseCode();
      if (code != HttpURLConnection.HTTP_OK) {
        throw new HttpException(code, conn.getResponseMessage());
      } else {
        platform.notifySuccess(callback, readContent(conn));
      }
    } finally {
      conn.disconnect();
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

  private String readContent(URLConnection conn) throws IOException {
    String encoding = conn.getContentEncoding() == null ? ENCODING : conn.getContentEncoding();
    InputStreamReader reader = new InputStreamReader(conn.getInputStream(), encoding);
    StringBuffer result = new StringBuffer();
    char[] buf = new char[BUF_SIZE];
    int len = 0;
    while (-1 != (len = reader.read(buf))) {
      result.append(buf, 0, len);
    }
    return result.toString();
  }
}
