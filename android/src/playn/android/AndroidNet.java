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

import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import playn.core.NetImpl;
import playn.core.util.Callback;

class AndroidNet extends NetImpl {

  @Override
  public void get(String url, Callback<String> callback) {
    doHttp(false, url, null, callback);
  }

  @Override
  public void post(String url, String data, Callback<String> callback) {
    doHttp(true, url, data, callback);
  }

  AndroidNet(AndroidPlatform platform) {
    super(platform);
  }

  private void doHttp(final boolean isPost, final String url, final String data,
                      final Callback<String> callback) {
    platform.invokeAsync(new Runnable() {
      @Override
      public void run() {
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
        HttpProtocolParams.setHttpElementCharset(params, HTTP.UTF_8);
        HttpClient httpclient = new DefaultHttpClient(params);
        HttpRequestBase req = null;
        if (isPost) {
          HttpPost httppost = new HttpPost(url);
          if (data != null) {
            try {
              httppost.setEntity(new StringEntity(data, HTTP.UTF_8));
            } catch (UnsupportedEncodingException e) {
              platform.notifyFailure(callback, e);
            }
          }
          req = httppost;
        } else {
          req = new HttpGet(url);
        }
        try {
          HttpResponse response = httpclient.execute(req);
          StatusLine status = response.getStatusLine();
          int code = status.getStatusCode();
          String body = EntityUtils.toString(response.getEntity());
          if (code == HttpStatus.SC_OK) {
            platform.notifySuccess(callback, body);
          } else {
            platform.notifyFailure(callback, new HttpException(code, body));
          }
        } catch (Exception e) {
          platform.notifyFailure(callback, e);
        }
      }
      @Override
      public String toString() {
        return "AndroidNet.doHttp(" + isPost + ", " + url + ")";
      }
    });
  }
}
