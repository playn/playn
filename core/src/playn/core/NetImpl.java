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
package playn.core;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import playn.core.util.Callback;

/**
 * An abstract implementation of {@link Net} shared by multiple backends.
 */
public abstract class NetImpl implements Net {

  protected static final String UTF8 = "UTF-8";

  protected class Header {
    public final String name;
    public final String value;

    public Header(String name, String value) {
      this.name = name;
      this.value = value;
    }
  }

  protected class BuilderImpl implements Builder {
    public final String url;
    public final List<Header> headers = new ArrayList<Header>();
    public String contentType = "text/plain";
    public String payloadString;
    public byte[] payloadBytes;

    public BuilderImpl(String url) {
      assert url.startsWith("http:") || url.startsWith("https:") :
        "Only http and https URLs are supported";
      this.url = url;
    }

    public boolean isPost() {
      return payloadString != null || payloadBytes != null;
    }

    public String method() {
      return isPost() ? "POST" : "GET";
    }

    public String contentType() {
      return contentType + (payloadString != null ? ("; charset=" + UTF8) : "");
    }

    @Override
    public Builder setPayload(String payload) {
      return setPayload(payload, "text/plain");
    }

    @Override
    public Builder setPayload(String payload, String contentType) {
      this.payloadString = payload;
      this.contentType = contentType;
      return this;
    }

    @Override
    public Builder setPayload(byte[] payload) {
      return setPayload(payload, "application/octet-stream");
    }

    @Override
    public Builder setPayload(byte[] payload, String contentType) {
      this.payloadBytes = payload;
      this.contentType = contentType;
      return this;
    }

    @Override
    public Builder addHeader(String name, String value) {
      headers.add(new Header(name, value));
      return this;
    }

    @Override
    public void execute (Callback<Response> callback) {
      NetImpl.this.execute(this, callback);
    }
  }

  protected abstract class ResponseImpl implements Response {
    private int responseCode;
    private Map<String,List<String>> headersMap;

    public ResponseImpl(int responseCode) {
      this.responseCode = responseCode;
    }

    @Override
    public int responseCode() {
      return this.responseCode;
    }

    @Override
    public Iterable<String> headerNames() {
      return headers().keySet();
    }

    @Override
    public String header(String name) {
      List<String> values = headers().get(name);
      return (values == null) ? null : values.get(0);
    }

    @Override public List<String> headers(String name) {
      List<String> values = headers().get(name);
      return values == null ? Collections.<String>emptyList() : values;
    }

    private Map<String,List<String>> headers() {
      if (headersMap == null) {
        headersMap = extractHeaders();
      }
      return headersMap;
    }

    protected abstract Map<String,List<String>> extractHeaders();
  }

  protected abstract class StringResponse extends ResponseImpl {
    private final String payload;

    public StringResponse(int responseCode, String payload) {
      super(responseCode);
      this.payload = payload;
    }

    @Override
    public String payloadString() {
      return payload;
    }

    @Override
    public byte[] payload() {
      throw new UnsupportedOperationException();
    }
  }

  protected abstract class BinaryResponse extends ResponseImpl {
    private final byte[] payload;
    private final String encoding;

    public BinaryResponse(int responseCode, byte[] payload, String encoding) {
      super(responseCode);
      this.payload = payload;
      this.encoding = encoding;
    }

    @Override
    public String payloadString() {
      try {
        return new String(payload, encoding);
      } catch (UnsupportedEncodingException uee) {
        return uee.toString();
      }
    }

    @Override
    public byte[] payload() {
      return payload;
    }
  }

  protected final AbstractPlatform platform;

  @Override
  public WebSocket createWebSocket(String url, WebSocket.Listener listener) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void get(String url, Callback<String> callback) {
    req(url).execute(adapt(callback));
  }

  @Override
  public void post(String url, String data, Callback<String> callback) {
    req(url).setPayload(data).execute(adapt(callback));
  }

  @Override
  public Builder req(String url) {
    return new BuilderImpl(url);
  }

  protected NetImpl(AbstractPlatform platform) {
    this.platform = platform;
  }

  protected void execute(BuilderImpl req, Callback<Response> callback) {
    throw new UnsupportedOperationException();
  }

  private Callback<Response> adapt (final Callback<String> callback) {
    return new Callback.Chain<Response>(callback) {
      public void onSuccess(Response rsp) {
        if (rsp.responseCode() == 200) {
          callback.onSuccess(rsp.payloadString());
        } else {
          callback.onFailure(new HttpException(rsp.responseCode(), rsp.payloadString()));
        }
      }
    };
  }
}
