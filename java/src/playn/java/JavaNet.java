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

import playn.core.Net;
import playn.core.util.Callback;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class JavaNet implements Net {

  private static final int BUF_SIZE = 4096;

  @Override
  public void get(String urlStr, Callback<String> callback) {
    // TODO: Make this non-blocking so that it doesn't differ from the html
    // version's behavior.
    try {
      URL url = new URL(canonicalizeUrl(urlStr));
      InputStream stream = url.openStream();
      InputStreamReader reader = new InputStreamReader(stream);

      callback.onSuccess(readFully(reader));
    } catch (MalformedURLException e) {
      callback.onFailure(e);
    } catch (IOException e) {
      callback.onFailure(e);
    }
  }

  @Override
  public void post(String urlStr, String data, Callback<String> callback) {
    // TODO: Make this non-blocking so that it doesn't differ from the html
    // version's behavior.
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
      callback.onSuccess(readFully(new InputStreamReader(conn.getInputStream())));
      conn.disconnect();
    } catch (MalformedURLException e) {
      callback.onFailure(e);
    } catch (IOException e) {
      callback.onFailure(e);
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
