/**
 * Copyright 2011 The ForPlay Authors
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
package forplay.android;

import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import forplay.core.Net;

class AndroidNet implements Net {

  public void get(String url, Callback callback) {
    doHttp(false, url, null, callback);
  }

  public void post(String url, String data, Callback callback) {
    doHttp(true, url, data, callback);
  }

  private void doHttp(boolean isPost, String url, String data, Callback callback) {
    // TODO: use AsyncTask
    HttpClient httpclient = new DefaultHttpClient();
    HttpRequestBase req = null;
    if (isPost) {
      HttpPost httppost = new HttpPost(url);
      if (data != null) {
        try {
          httppost.setEntity(new StringEntity(data));
        } catch (UnsupportedEncodingException e) {
          // TODO Auto-generated catch block
          callback.failure(e);
        }
      }
      req = httppost;
    } else {
      req = new HttpGet(url);
    }
    try {   
        HttpResponse response = httpclient.execute(req);
        callback.success(EntityUtils.toString(response.getEntity()));
    } catch (Exception e) {
        callback.failure(e);
    }
  }
}
