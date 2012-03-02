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
import cli.System.Net.WebRequest;
import cli.System.Net.WebResponse;

import playn.core.Net;
import playn.core.util.Callback;

class IOSNet implements Net
{
  @Override
  public void get(String url, Callback<String> callback) {
    final WebRequest req = WebRequest.Create(url);
    req.BeginGetResponse(wrap(req, callback), null);
  }

  @Override
  public void post(String url, String data, Callback<String> callback) {
    final WebRequest req = WebRequest.Create(url);
    try {
      req.set_Method("POST");
      StreamWriter out = new StreamWriter(req.GetRequestStream());
      out.Write(data);
      out.Close();
      req.BeginGetResponse(wrap(req, callback), null);
    } catch (Throwable t) {
      callback.onFailure(t);
    }
  }

  protected AsyncCallback wrap (final WebRequest req, final Callback<String> callback) {
    return new AsyncCallback(new AsyncCallback.Method() {
      @Override
      public void Invoke(IAsyncResult result) {
        StreamReader reader = null;
        try {
          WebResponse rsp = req.EndGetResponse(result);
          reader = new StreamReader(rsp.GetResponseStream());
          callback.onSuccess(reader.ReadToEnd());
        } catch (Throwable t) {
          callback.onFailure(t);
        } finally {
          if (reader != null)
            reader.Close();
        }
      }
    });
  }
}
