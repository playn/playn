/**
 * Copyright 2010 The ForPlay Authors
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
package forplay.java;

import forplay.core.Net;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class JavaNet implements Net {

  private static final int BUF_SIZE = 4096;

  public void get(String urlStr, Callback callback) {
    // TODO: Make this non-blocking so that it doesn't differ from the html
    // version's behavior.
    try {
      URL url = new URL(canonicalizeUrl(urlStr));
      InputStream stream = url.openStream();
      InputStreamReader reader = new InputStreamReader(stream);

      callback.success(readFully(reader));
    } catch (MalformedURLException e) {
      callback.failure(e);
    } catch (IOException e) {
      callback.failure(e);
    }
  }

  public void post(String urlStr, String data, Callback callback) {
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
      readFully(new InputStreamReader(conn.getInputStream()));
      conn.disconnect();
    } catch (MalformedURLException e) {
      callback.failure(e);
    } catch (IOException e) {
      callback.failure(e);
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
    while (-1 != reader.read(buf)) {
      result.append(buf);
    }
    return result.toString();
  }
}
