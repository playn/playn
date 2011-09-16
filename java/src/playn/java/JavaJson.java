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

    public void key(String key) {
      try {
        w.key(key);
      } catch (JSONException e) {
        throw new RuntimeException(e);
      }
    }

    public void value(boolean x) {
      try {
        w.value(x);
      } catch (JSONException e) {
        throw new RuntimeException(e);
      }
    }

    public void value(int x) {
      try {
        w.value(x);
      } catch (JSONException e) {
        throw new RuntimeException(e);
      }
    }

    public void value(double x) {
      try {
        w.value(x);
      } catch (JSONException e) {
        throw new RuntimeException(e);
      }
    }

    public void value(String x) {
      try {
        w.value(x);
      } catch (JSONException e) {
        throw new RuntimeException(e);
      }
    }

    public void object() {
      try {
        w.object();
      } catch (JSONException e) {
        throw new RuntimeException(e);
      }
    }

    public void endObject() {
      try {
        w.endObject();
      } catch (JSONException e) {
        throw new RuntimeException(e);
      }
    }

    public void array() {
      try {
        w.array();
      } catch (JSONException e) {
        throw new RuntimeException(e);
      }
    }

    public void endArray() {
      try {
        w.endArray();
      } catch (JSONException e) {
        throw new RuntimeException(e);
      }
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

    public TypedArray<Boolean> getBooleanArray(String key) {
      return asBooleanArray(jso.optJSONArray(key));
    }

    public TypedArray<Integer> getIntArray(String key) {
      return asIntArray(jso.optJSONArray(key));
    }

    public TypedArray<Double> getNumberArray(String key) {
      return asNumberArray(jso.optJSONArray(key));
    }

    public TypedArray<String> getStringArray(String key) {
      return asStringArray(jso.optJSONArray(key));
    }

    public TypedArray<Object> getObjectArray(String key) {
      return asObjectArray(jso.optJSONArray(key));
    }

    public boolean containsKey(String key) {
      return (jso == null) ? false : jso.has(key);
    }

    public Json.TypedArray<String> getKeys() {
      String[] names;
      if (jso == null || (names = JSONObject.getNames(jso)) == null) {
        return asStringArray(new JSONArray());
      }
      return asStringArray(new JSONArray(Arrays.asList(names)));
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

    public TypedArray<Boolean> getBooleanArray(int index) {
      return asBooleanArray(jsa.optJSONArray(index));
    }

    public TypedArray<Integer> getIntArray(int index) {
      return asIntArray(jsa.optJSONArray(index));
    }

    public TypedArray<Double> getNumberArray(int index) {
      return asNumberArray(jsa.optJSONArray(index));
    }

    public TypedArray<String> getStringArray(int index) {
      return asStringArray(jsa.optJSONArray(index));
    }

    public TypedArray<Object> getObjectArray(int index) {
      return asObjectArray(jsa.optJSONArray(index));
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

  private static TypedArray<Boolean> asBooleanArray(final JSONArray jsa) {
    return jsa == null ? null : new TypedArray<Boolean>() {
      @Override
      public int length() {
        return jsa.length();
      }
      @Override
      protected Boolean getImpl(int index) {
        return jsa.optBoolean(index);
      }
    };
  }

  private static TypedArray<Integer> asIntArray(final JSONArray jsa) {
    return jsa == null ? null : new TypedArray<Integer>() {
      @Override
      public int length() {
        return jsa.length();
      }
      @Override
      protected Integer getImpl(int index) {
        return jsa.optInt(index);
      }
    };
  }

  private static TypedArray<Double> asNumberArray(final JSONArray jsa) {
    return jsa == null ? null : new TypedArray<Double>() {
      @Override
      public int length() {
        return jsa.length();
      }
      @Override
      protected Double getImpl(int index) {
        return jsa.optDouble(index);
      }
    };
  }

  private static TypedArray<String> asStringArray(final JSONArray jsa) {
    return jsa == null ? null : new TypedArray<String>() {
      @Override
      public int length() {
        return jsa.length();
      }
      @Override
      protected String getImpl(int index) {
        return jsa.optString(index);
      }
    };
  }

  private static TypedArray<Object> asObjectArray(final JSONArray jsa) {
    return jsa == null ? null : new TypedArray<Object>() {
      @Override
      public int length() {
        return jsa.length();
      }
      @Override
      protected Object getImpl(int index) {
        JSONObject o = jsa.optJSONObject(index);
        return o == null ? null : new JavaObject(o);
      }
    };
  }
}
