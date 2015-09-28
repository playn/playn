/**
 * Copyright 2011 The PlayN Authors
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
package playn.android;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import react.RFuture;
import react.RPromise;

import playn.core.Exec;
import playn.core.Net;

public class AndroidNet extends Net {

  private final Exec exec;

  public AndroidNet (Exec exec) {
    this.exec = exec;
  }

  @Override public WebSocket createWebSocket(String url, WebSocket.Listener listener) {
    return new AndroidWebSocket(exec, url, listener);
  }

  @Override protected RFuture<Response> execute(final Builder req) {
    final RPromise<Response> result = exec.deferredPromise();
    exec.invokeAsync(new Runnable() {
      @Override public void run() {
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
        HttpProtocolParams.setHttpElementCharset(params, HTTP.UTF_8);
        HttpClient httpclient = new DefaultHttpClient(params);

        // prepare the request
        HttpRequestBase hreq = null;
        try {
          if (req.isPost()) {
            HttpPost httppost = new HttpPost(req.url);
            if (req.payloadString != null) {
              httppost.setEntity(new StringEntity(req.payloadString, HTTP.UTF_8));
            } else if (req.payloadBytes != null) {
              httppost.setEntity(new ByteArrayEntity(req.payloadBytes));
            }
            hreq = httppost;

            // override the content type
            hreq.addHeader(HTTP.CONTENT_TYPE, req.contentType());
          } else {
            hreq = new HttpGet(req.url);
          }
          for (Header header : req.headers) {
            hreq.addHeader(header.name, header.value);
          }

          // execute the request and process the response
          final HttpResponse response = httpclient.execute(hreq);
          int code = response.getStatusLine().getStatusCode();
          HttpEntity entity = response.getEntity();

          Response impl;
          if (entity == null) {
            impl = new Response(code) {
              @Override
              public String payloadString() {
                return "";
              }
              @Override
              public byte[] payload() {
                return new byte[0];
              }
              @Override
              protected Map<String, List<String>> extractHeaders() {
                return extractResponseHeaders(response);
              }
            };

          } else {
            byte[] data = EntityUtils.toByteArray(entity);
            String encoding = EntityUtils.getContentCharSet(entity);
            if (encoding == null) {
              encoding = HTTP.UTF_8;
            }
            impl = new Response.Binary(code, data, encoding) {
              @Override
              protected Map<String,List<String>> extractHeaders() {
                return extractResponseHeaders(response);
              }
            };
          }

          result.succeed(impl);
        } catch (Throwable t) {
          result.fail(t);
        }
      }

      @Override
      public String toString() {
        return "AndroidNet.exec(" + req.method() + ", " + req.url + ")";
      }

      private Map<String,List<String>> extractResponseHeaders(HttpResponse response) {
        Map<String,List<String>> hmap = new HashMap<String,List<String>>();
        for (org.apache.http.Header header : response.getAllHeaders()) {
          String name = header.getName();
          List<String> values = hmap.get(name);
          if (values == null) {
            hmap.put(name, values = new ArrayList<String>());
          }
          values.add(header.getValue());
        }
        return hmap;
      }
    });
    return result;
  }
}
