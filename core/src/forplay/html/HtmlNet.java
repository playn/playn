/**
 * Copyright 2010 The ForPlay Authors
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
package forplay.html;

import com.google.gwt.xhr.client.ReadyStateChangeHandler;
import com.google.gwt.xhr.client.XMLHttpRequest;

import forplay.core.Net;

public class HtmlNet implements Net {

  public void get(String url, final Callback callback) {
    try {
      XMLHttpRequest xhr = XMLHttpRequest.create();
      xhr.open("GET", url);
      xhr.setOnReadyStateChange(new ReadyStateChangeHandler() {
        @Override
        public void onReadyStateChange(XMLHttpRequest xhr) {
          if (xhr.getReadyState() == XMLHttpRequest.DONE) {
            if (xhr.getStatus() >= 400) {
              callback.failure(new RuntimeException("Bad HTTP status code: " + xhr.getStatus()));
            } else {
              callback.success(xhr.getResponseText());
            }
          }
        }
      });
      xhr.send();
    } catch (Exception e) {
      callback.failure(e);
    }
  }

  public void post(String url, String data, final Callback callback) {
    try {
      XMLHttpRequest xhr = XMLHttpRequest.create();
      xhr.open("POST", url);
      xhr.setOnReadyStateChange(new ReadyStateChangeHandler() {
        @Override
        public void onReadyStateChange(XMLHttpRequest xhr) {
          if (xhr.getReadyState() == XMLHttpRequest.DONE) {
            if (xhr.getStatus() >= 400) {
              callback.failure(new RuntimeException("Bad HTTP status code: " + xhr.getStatus()));
            } else {
              callback.success(xhr.getResponseText());
            }
          }
        }
      });
      xhr.send(data);
    } catch (Exception e) {
      callback.failure(e);
    }
  }
}
