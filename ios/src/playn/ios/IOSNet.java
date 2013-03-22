/**
 * Copyright 2012 The PlayN Authors
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
package playn.ios;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cli.System.Convert;
import cli.System.Runtime.InteropServices.Marshal;

import cli.MonoTouch.Foundation.NSData;
import cli.MonoTouch.Foundation.NSDictionary;
import cli.MonoTouch.Foundation.NSError;
import cli.MonoTouch.Foundation.NSHttpUrlResponse;
import cli.MonoTouch.Foundation.NSMutableData;
import cli.MonoTouch.Foundation.NSMutableUrlRequest;
import cli.MonoTouch.Foundation.NSObject;
import cli.MonoTouch.Foundation.NSString;
import cli.MonoTouch.Foundation.NSStringEncoding;
import cli.MonoTouch.Foundation.NSUrl;
import cli.MonoTouch.Foundation.NSUrlConnection;
import cli.MonoTouch.Foundation.NSUrlConnectionDelegate;
import cli.MonoTouch.Foundation.NSUrlRequest;
import cli.MonoTouch.Foundation.NSUrlResponse;

import playn.core.NetImpl;
import playn.core.util.Callback;

public class IOSNet extends NetImpl {

  public IOSNet(IOSPlatform platform) {
    super(platform);
  }

  @Override
  protected void execute(BuilderImpl req, Callback<Response> callback) {
    NSMutableUrlRequest mreq = new NSMutableUrlRequest(new NSUrl(req.url));
    for (Header header : req.headers) {
      mreq.set_Item(header.name, header.value);
    }
    mreq.set_HttpMethod(req.method());
    if (req.isPost()) {
      mreq.set_Item("Content-type", req.contentType());
      if (req.payloadString != null) {
        mreq.set_Body(NSData.FromString(req.payloadString,
                                        NSStringEncoding.wrap(NSStringEncoding.UTF8)));
      } else {
        mreq.set_Body(NSData.FromArray(req.payloadBytes));
      }
    }
    sendRequest(mreq, callback);
  }

  protected void sendRequest(NSUrlRequest req, final Callback<Response> callback) {
    new NSUrlConnection(req, new NSUrlConnectionDelegate() {
      private NSMutableData data;
      private int rspCode = -1;
      private NSDictionary headers;

      @Override
      public void ReceivedResponse(NSUrlConnection conn, NSUrlResponse rsp) {
        // if we are redirected, we may accumulate data as we bounce through requests, so we reset
        // our data accumulator each time we receive the response headers
        data = new NSMutableData();
        // note our most recent response code
        if (rsp instanceof NSHttpUrlResponse) {
          NSHttpUrlResponse hrsp = (NSHttpUrlResponse)rsp;
          rspCode = hrsp.get_StatusCode();
          headers = hrsp.get_AllHeaderFields();
        }
      }
      @Override
      public void ReceivedData(NSUrlConnection conn, NSData data) {
        this.data.AppendData(data);
      }
      @Override
      public void FailedWithError (NSUrlConnection conn, NSError error) {
        String errmsg = error.get_LocalizedDescription();
        Exception exn = rspCode > 0 ? new HttpException(rspCode, errmsg) : new Exception(errmsg);
        platform.notifyFailure(callback, exn);
      }
      @Override
      public void FinishedLoading (NSUrlConnection conn) {
        platform.notifySuccess(callback, new ResponseImpl(rspCode) {
          @Override
          protected Map<String,List<String>> extractHeaders() {
            Map<String,List<String>> headerMap = new HashMap<String,List<String>>();
            for (NSObject key : headers.get_Keys()) {
              // iOS concatenates all repeated headers into a single header separated by commas,
              // which is known to be a fucking stupid thing to do, but hey, they're doing it!
              headerMap.put(key.ToString(),
                            Collections.singletonList(headers.get_Item(key).ToString()));
            }
            return headerMap;
          }
          @Override
          public String payloadString() {
            return NSString.op_Implicit(
              NSString.FromData(data, NSStringEncoding.wrap(NSStringEncoding.UTF8)));
          }
          @Override
          public byte[] payload() {
            int length = Convert.ToInt32(data.get_Length());
            byte[] bytes = new byte[length];
            Marshal.Copy(data.get_Bytes(), bytes, 0, length);
            return bytes;
          }
        });
      }
    }, true);
  }
}
