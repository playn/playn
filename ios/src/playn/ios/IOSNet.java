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

import cli.System.AsyncCallback;
import cli.System.IAsyncResult;
import cli.System.IO.StreamReader;
import cli.System.IO.StreamWriter;
import cli.System.Net.HttpStatusCode;
import cli.System.Net.HttpWebResponse;
import cli.System.Net.WebRequest;

import playn.core.NetImpl;
import playn.core.util.Callback;

public class IOSNet extends NetImpl {

  public IOSNet(IOSPlatform platform) {
    super(platform);
  }

  @Override
  public void get(String url, Callback<String> callback) {
    try {
      final WebRequest req = WebRequest.Create(url);
      req.BeginGetResponse(gotResponse(req, callback), null);
    } catch (Throwable t) {
      callback.onFailure(t);
    }
  }

  @Override
  public void post(String url, final String data, final Callback<String> callback) {
    try {
      final WebRequest req = WebRequest.Create(url);
      req.set_Method("POST");
      req.BeginGetRequestStream(new AsyncCallback(new AsyncCallback.Method() {
        @Override
        public void Invoke(IAsyncResult result) {
          try {
            StreamWriter out = new StreamWriter(req.GetRequestStream());
            out.Write(data);
            out.Close();
            req.BeginGetResponse(gotResponse(req, callback), null);
          } catch (Throwable t) {
            notifyFailure(callback, t);
          }
        }
      }), null);
    } catch (Throwable t) {
      callback.onFailure(t);
    }
  }

  protected AsyncCallback gotResponse (final WebRequest req, final Callback<String> callback) {
    return new AsyncCallback(new AsyncCallback.Method() {
      @Override
      public void Invoke(IAsyncResult result) {
        StreamReader reader = null;
        try {
          HttpWebResponse rsp = (HttpWebResponse) req.EndGetResponse(result);
          HttpStatusCode code = rsp.get_StatusCode();
          if (code.Value == HttpStatusCode.OK) {
            reader = new StreamReader(rsp.GetResponseStream());
            notifySuccess(callback, reader.ReadToEnd());
          } else {
            notifyFailure(callback, new HttpException(code.Value, rsp.get_StatusDescription()));
          }
        } catch (final Throwable t) {
          notifyFailure(callback, t);
        } finally {
          if (reader != null)
            reader.Close();
        }
      }
    });
  }
}
