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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import playn.core.Exec;
import playn.core.Net;
import react.RFuture;
import react.RPromise;

public class JavaNet extends Net {

  private final Exec exec;

  public JavaNet(Exec exec) {
    this.exec = exec;
  }

  @Override public WebSocket createWebSocket(String url, WebSocket.Listener listener) {
    return new JavaWebSocket(exec, url, listener);
  }

  @Override protected RFuture<Response> execute(final Builder req) {
    final RPromise<Response> result = exec.deferredPromise();
    exec.invokeAsync(new Runnable() {
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

            InputStream stream = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
            byte[] payload = stream == null ? new byte[0] : JavaAssets.toByteArray(stream);

            String encoding = conn.getContentEncoding();
            if (encoding == null) encoding = UTF8;

            result.succeed(new Response.Binary(code, payload, encoding) {
              @Override protected Map<String,List<String>> extractHeaders() {
                return conn.getHeaderFields();
              }
            });
          } finally {
            conn.disconnect();
          }

        } catch (MalformedURLException e) {
          result.fail(e);
        } catch (IOException e) {
          result.fail(e);
        }
      }
      @Override
      public String toString() {
        return "JavaNet." + req.method().toLowerCase() + "(" + req.url + ")";
      }
    });
    return result;
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
