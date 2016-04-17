/**
 * Copyright 2014 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playn.bugvm;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bugvm.apple.foundation.NSData;
import com.bugvm.apple.foundation.NSError;
import com.bugvm.apple.foundation.NSHTTPURLResponse;
import com.bugvm.apple.foundation.NSMutableData;
import com.bugvm.apple.foundation.NSMutableURLRequest;
import com.bugvm.apple.foundation.NSURL;
import com.bugvm.apple.foundation.NSURLConnection;
import com.bugvm.apple.foundation.NSURLConnectionDataDelegateAdapter;
import com.bugvm.apple.foundation.NSURLRequest;
import com.bugvm.apple.foundation.NSURLResponse;

import playn.core.Exec;
import playn.core.Net;
import react.RFuture;
import react.RPromise;

public class BugNet extends Net {

  private final Exec exec;

  public BugNet(Exec exec) {
    this.exec = exec;
  }

  @Override public WebSocket createWebSocket(String url, WebSocket.Listener listener) {
    return new BugWebSocket(exec, url, listener);
  }

  @Override protected RFuture<Response> execute(Builder req) {
    RPromise<Response> result = exec.deferredPromise();

    NSMutableURLRequest mreq = new NSMutableURLRequest();
    mreq.setURL(new NSURL(req.url));
    for (Header header : req.headers) {
      mreq.setHTTPHeaderField(header.name, header.value);
    }
    mreq.setHTTPMethod(req.method());
    if (req.isPost()) {
      mreq.setHTTPHeaderField("Content-type", req.contentType());
      if (req.payloadString != null) {
        try {
          mreq.setHTTPBody(new NSData(req.payloadString.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException uee) {
          throw new RuntimeException(uee);
        }
      } else {
        mreq.setHTTPBody(new NSData(req.payloadBytes));
      }
    }
    sendRequest(mreq, result);
    return result;
  }

  protected void sendRequest(NSURLRequest req, final RPromise<Response> result) {
    new NSURLConnection(req, new NSURLConnectionDataDelegateAdapter() {
      private NSMutableData data;
      private int rspCode = -1;
      private Map<String,String> headers;

      @Override public void didReceiveResponse(NSURLConnection conn, NSURLResponse rsp) {
        // if we are redirected, we may accumulate data as we bounce through requests, so we reset
        // our data accumulator each time we receive the response headers
        data = new NSMutableData();
        // note our most recent response code
        if (rsp instanceof NSHTTPURLResponse) {
          NSHTTPURLResponse hrsp = (NSHTTPURLResponse)rsp;
          rspCode = (int)hrsp.getStatusCode();
          headers = hrsp.getAllHeaderFields();
        }
      }

      @Override public void didReceiveData(NSURLConnection conn, NSData data) {
        this.data.append(data);
      }

      @Override public void didFail(NSURLConnection conn, NSError error) {
        String errmsg = error.getLocalizedDescription();
        result.fail(rspCode > 0 ? new HttpException(rspCode, errmsg) : new Exception(errmsg));
      }

      @Override public void didFinishLoading(NSURLConnection conn) {
        result.succeed(new Response(rspCode) {
          @Override protected Map<String,List<String>> extractHeaders() {
            Map<String,List<String>> headerMap = new HashMap<String,List<String>>();
            if (headers != null) {
              for (Map.Entry<String,String> entry : headers.entrySet()) {
                // iOS concatenates all repeated headers into a single header separated by commas,
                // which is known to be a fucking stupid thing to do, but hey, they're doing it!
                headerMap.put(entry.getKey(), Collections.singletonList(entry.getValue()));
              }
            }
            return headerMap;
          }
          @Override public String payloadString() {
            try {
              return new String(data.getBytes(), "UTF-8");
            } catch (UnsupportedEncodingException uee) {
              throw new RuntimeException(uee);
            }
          }
          @Override public byte[] payload() {
            return data.getBytes();
          }
        });
      }
    }, true);
  }
}
