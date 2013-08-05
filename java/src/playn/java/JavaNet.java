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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import com.google.common.io.ByteStreams;

import playn.core.NetImpl;
import playn.core.util.Callback;

public class JavaNet extends NetImpl {

  public JavaNet(JavaPlatform platform) {
    super(platform);
  }

  @Override
  public WebSocket createWebSocket(String url, WebSocket.Listener listener) {
    return new JavaWebSocket(platform, url, listener);
  }

  @Override
  protected void execute(final BuilderImpl req, final Callback<Response> callback) {
    platform.invokeAsync(new Runnable() {
      @Override
      public void run() {
        try {
          // configure the request
          URL url = new URL(canonicalizeUrl(req.url));
          final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
          for (Header header : req.headers) {
            conn.setRequestProperty(header.name, header.value);
          }
          conn.setRequestMethod(req.method());
          if (req.isPost()) {
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setAllowUserInteraction(false);
            conn.setRequestProperty("Content-type", req.contentType());
            conn.connect();
            conn.getOutputStream().write(
              (req.payloadString == null) ? req.payloadBytes : req.payloadString.getBytes(UTF8));
            conn.getOutputStream().close();
          }

          // issue the request and process the response
          try {
            int code = conn.getResponseCode();
            byte[] payload = ByteStreams.toByteArray(
              code >= 400 ? conn.getErrorStream() : conn.getInputStream());
            String encoding = conn.getContentEncoding();
            if (encoding == null) encoding = UTF8;
            platform.notifySuccess(callback, new BinaryResponse(code, payload, encoding) {
              @Override
              protected Map<String,List<String>> extractHeaders() {
                return conn.getHeaderFields();
              }
            });
          } finally {
            conn.disconnect();
          }

        } catch (MalformedURLException e) {
          platform.notifyFailure(callback, e);
        } catch (IOException e) {
          platform.notifyFailure(callback, e);
        }
      }
      @Override
      public String toString() {
        return "JavaNet." + req.method().toLowerCase() + "(" + req.url + ")";
      }
    });
  }

  // Super-simple url-cleanup: assumes it either starts with "http", or that
  // it's an absolute path on the current server.
  private String canonicalizeUrl(String url) {
    if (!url.startsWith("http")) {
      return "http://" + server() + url;
    }
    return url;
  }

  // TODO: Make this specifiable somewhere.
  private String server() {
    return "127.0.0.1:8080";
  }
}
