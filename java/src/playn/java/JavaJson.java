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
package playn.java;

import playn.core.Json;
import playn.core.TypedArrayBuilder;
import playn.java.json.JSONArray;
import playn.java.json.JSONException;
import playn.java.json.JSONObject;
import playn.java.json.JSONWriter;

import java.io.StringWriter;
import java.util.Arrays;

/**
 * Public because it's currently being used by Android.
 */
public class JavaJson implements Json {

  static class JavaWriter implements Json.Writer {
    private StringWriter sw;
    private JSONWriter w;

    JavaWriter() {
      reset();
    }

    public Writer key(String key) {
      try {
        w.key(key);
      } catch (JSONException e) {
        throw new RuntimeException(e);
      }
      return this;
    }

    public Writer value(boolean x) {
      try {
        w.value(x);
      } catch (JSONException e) {
        throw new RuntimeException(e);
      }
      return this;
    }

    public Writer value(int x) {
      try {
        w.value(x);
      } catch (JSONException e) {
        throw new RuntimeException(e);
      }
      return this;
    }

    public Writer value(double x) {
      try {
        w.value(x);
      } catch (JSONException e) {
        throw new RuntimeException(e);
      }
      return this;
    }

    public Writer value(String x) {
      try {
        w.value(x);
      } catch (JSONException e) {
        throw new RuntimeException(e);
      }
      return this;
    }

    public Writer object() {
      try {
        w.object();
      } catch (JSONException e) {
        throw new RuntimeException(e);
      }
      return this;
    }

    public Writer endObject() {
      try {
        w.endObject();
      } catch (JSONException e) {
        throw new RuntimeException(e);
      }
      return this;
    }

    public Writer array() {
      try {
        w.array();
      } catch (JSONException e) {
        throw new RuntimeException(e);
      }
      return this;
    }

    public Writer endArray() {
      try {
        w.endArray();
      } catch (JSONException e) {
        throw new RuntimeException(e);
      }
      return this;
    }

    public String write() {
      String result = sw.toString();
      reset();
      return result;
    }

    private void reset() {
      sw = new StringWriter();
      w = new JSONWriter(sw);
    }
  }

  static class JavaObject implements Json.Object {
    private JSONObject jso;

    JavaObject(JSONObject jso) {
      this.jso = jso;
    }

    public boolean getBoolean(String key) {
      return jso.optBoolean(key);
    }

    public int getInt(String key) {
      return jso.optInt(key);
    }

    public double getNumber(String key) {
      return jso.optDouble(key);
    }

    public String getString(String key) {
      return jso.optString(key);
    }

    public Json.Object getObject(String key) {
      JSONObject o = jso.optJSONObject(key);
      return o == null ? null : new JavaObject(o);
    }

    public Json.Array getArray(String key) {
      JSONArray a = jso.optJSONArray(key);
      return a == null ? null : new JavaArray(a);
    }

    public <T> TypedArray<T> getArray(String key, Class<T> arrayType) {
      return arrayBuilder.build(jso.optJSONArray(key), arrayType);
    }

    public boolean containsKey(String key) {
      return (jso == null) ? false : jso.has(key);
    }

    public Json.TypedArray<String> getKeys() {
      String[] names;
      if (jso == null || (names = JSONObject.getNames(jso)) == null) {
        return arrayBuilder.build(new JSONArray(), String.class);
      }
      return arrayBuilder.build(new JSONArray(Arrays.asList(names)), String.class);
    }
  }

  static class JavaArray implements Json.Array {
    private JSONArray jsa;

    JavaArray(JSONArray jsa) {
      this.jsa = jsa;
    }

    public int length() {
      return jsa.length();
    }

    public boolean getBoolean(int index) {
      return jsa.optBoolean(index);
    }

    public int getInt(int index) {
      return jsa.optInt(index);
    }

    public double getNumber(int index) {
      return jsa.optDouble(index);
    }

    public String getString(int index) {
      return jsa.optString(index);
    }

    public Json.Object getObject(int index) {
      JSONObject o = jsa.optJSONObject(index);
      return o == null ? null : new JavaObject(o);
    }

    public Json.Array getArray(int index) {
      JSONArray a = jsa.optJSONArray(index);
      return a == null ? null : new JavaArray(a);
    }

    public <T> TypedArray<T> getArray(int index, Class<T> arrayType) {
      return arrayBuilder.build(jsa.optJSONArray(index), arrayType);
    }
  }

  @Override
  public Writer newWriter() {
    return new JavaWriter();
  }

  @Override
  public Object parse(String json) {
    try {
      return new JavaObject(new JSONObject(json));
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }

  private static TypedArrayBuilder<JSONArray> arrayBuilder = new TypedArrayBuilder<JSONArray>() {
    public int length(JSONArray array) {
      return array.length();
    }
    public Json.Object getObject(JSONArray array, int index) {
      JSONObject o = array.optJSONObject(index);
      return o == null ? null : new JavaObject(o);
    }
    public Boolean getBoolean(JSONArray array, int index) {
      return array.optBoolean(index);
    }
    public Integer getInt(JSONArray array, int index) {
      return array.optInt(index);
    }
    public Double getNumber(JSONArray array, int index) {
      return array.optDouble(index);
    }
    public String getString(JSONArray array, int index) {
      return array.optString(index);
    }
  };
}
