/**
 * Copyright 2010 The PlayN Authors
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
package playn.html;

import com.google.gwt.xhr.client.ReadyStateChangeHandler;
import com.google.gwt.xhr.client.XMLHttpRequest;

import playn.core.NetImpl;
import playn.core.util.Callback;

public class HtmlNet extends NetImpl {

  public HtmlNet(HtmlPlatform platform) {
    super(platform);
  }

  @Override
  public WebSocket createWebSocket(String url, WebSocket.Listener listener) {
    return new HtmlWebSocket(url, listener);
  }

  public void get(String url, final Callback<String> callback) {
    try {
      XMLHttpRequest xhr = XMLHttpRequest.create();
      xhr.open("GET", url);
      xhr.setOnReadyStateChange(new ReadyStateChangeHandler() {
        @Override
        public void onReadyStateChange(XMLHttpRequest xhr) {
          if (xhr.getReadyState() == XMLHttpRequest.DONE) {
            if (xhr.getStatus() == 200) {
              callback.onSuccess(xhr.getResponseText());
            } else {
              callback.onFailure(new HttpException(xhr.getStatus(), xhr.getStatusText()));
            }
          }
        }
      });
      xhr.send();
    } catch (Exception e) {
      callback.onFailure(e);
    }
  }

  @Override
  public void post(String url, String data, final Callback<String> callback) {
    try {
      XMLHttpRequest xhr = XMLHttpRequest.create();
      xhr.open("POST", url);
      xhr.setOnReadyStateChange(new ReadyStateChangeHandler() {
        @Override
        public void onReadyStateChange(XMLHttpRequest xhr) {
          if (xhr.getReadyState() == XMLHttpRequest.DONE) {
            if (xhr.getStatus() == 200) {
              callback.onSuccess(xhr.getResponseText());
            } else {
              callback.onFailure(new HttpException(xhr.getStatus(), xhr.getStatusText()));
            }
          }
        }
      });
      xhr.send(data);
    } catch (Exception e) {
      callback.onFailure(e);
    }
  }
}
