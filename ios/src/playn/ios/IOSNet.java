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

import cli.MonoTouch.Foundation.NSData;
import cli.MonoTouch.Foundation.NSError;
import cli.MonoTouch.Foundation.NSHttpUrlResponse;
import cli.MonoTouch.Foundation.NSMutableData;
import cli.MonoTouch.Foundation.NSMutableUrlRequest;
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
  public void get(String url, Callback<String> callback) {
    sendRequest(new NSUrlRequest(new NSUrl(url)), callback);
  }

  @Override
  public void post(String url, String data, Callback<String> callback) {
    NSMutableUrlRequest req = new NSMutableUrlRequest(new NSUrl(url));
    req.set_HttpMethod("POST");
    req.set_Body(NSData.FromString(data, NSStringEncoding.wrap(NSStringEncoding.UTF8)));
    sendRequest(req, callback);
  }

  protected void sendRequest(NSUrlRequest req, final Callback<String> callback) {
    new NSUrlConnection(req, new NSUrlConnectionDelegate() {
      private NSMutableData data;
      private int rspCode = -1;

      @Override
      public void ReceivedResponse(NSUrlConnection conn, NSUrlResponse rsp) {
        // if we are redirected, we may accumulate data as we bounce through requests, so we reset
        // our data accumulator each time we receive the response headers
        data = new NSMutableData();
        // note our most recent response code
        if (rsp instanceof NSHttpUrlResponse) {
          rspCode = ((NSHttpUrlResponse)rsp).get_StatusCode();
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
        NSString nsResult = NSString.FromData(data, NSStringEncoding.wrap(NSStringEncoding.UTF8));
        String result = NSString.op_Implicit(nsResult);
        if (rspCode == 200) {
          platform.notifySuccess(callback, result);
        } else {
          platform.notifyFailure(callback, new HttpException(rspCode, result));
        }
      }
    }, true);
  }
}
