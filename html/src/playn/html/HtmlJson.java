/**
 * Copyright 2010 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playn.html;

import java.util.ArrayList;

import com.google.gwt.core.client.JavaScriptObject;

import playn.core.Asserts;
import playn.core.Json;
import playn.core.TypedArrayBuilder;

class HtmlJson implements Json {

  class HtmlWriter implements Json.Writer {
    private StringBuilder sb = new StringBuilder();
    private String key;
    private ArrayList<Boolean> inArrayStack = new ArrayList<Boolean>();
    private ArrayList<Boolean> isFirstValueStack = new ArrayList<Boolean>();

    @Override
    public void array() {
      maybePrependKey();
      sb.append("[");
      pushInArray(true);
      pushIsFirstValue(true);
    }

    @Override
    public void endArray() {
      sb.append("]");
      popInArray();
      popIsFirstValue();
    }

    @Override
    public void endObject() {
      sb.append("}");
      popInArray();
      popIsFirstValue();
    }

    @Override
    public void key(String key) {
      Asserts.checkState(this.key == null);
      this.key = key;
    }

    @Override
    public void object() {
      maybePrependKey(true);
      sb.append("{");
      pushInArray(false);
      pushIsFirstValue(true);
    }

    @Override
    public void value(boolean x) {
      maybePrependKey();
      sb.append(x);
    }

    @Override
    public void value(double x) {
      maybePrependKey();
      sb.append(x);
    }

    @Override
    public void value(int x) {
      maybePrependKey();
      sb.append(x);
    }

    @Override
    public void value(String x) {
      maybePrependKey();
      sb.append("\"");
      sb.append(x);
      sb.append("\"");
    }

    @Override
    public String write() {
      return sb.toString();
    }

    private void maybePrependKey() {
      maybePrependKey(false);
    }

    /**
     * Prepend the key if not in an array.
     *
     * Note: if this isn't the first key, we output a leading comma as well.
     */
    private void maybePrependKey(boolean isObject) {
      // Special case for the opening object.
      if (isObject && inArrayStack.size() == 0) {
        return;
      }

      if (isFirstValue()) {
        popIsFirstValue();
        pushIsFirstValue(false);
      } else {
        sb.append(",");
      }

      if (inArray()) {
        Asserts.checkState(this.key == null);
      } else {
        Asserts.checkState(this.key != null);
        sb.append("\"");
        sb.append(key);
        sb.append("\":");
        key = null;
      }
    }

    private void pushInArray(boolean inArray) {
      inArrayStack.add(inArray);
    }

    private boolean popInArray() {
      return inArrayStack.remove(inArrayStack.size() - 1);
    }

    private boolean inArray() {
      return inArrayStack.get(inArrayStack.size() - 1);
    }

    private void pushIsFirstValue(boolean isFirstValue) {
      isFirstValueStack.add(isFirstValue);
    }

    private boolean popIsFirstValue() {
      return isFirstValueStack.remove(isFirstValueStack.size() - 1);
    }

    private boolean isFirstValue() {
      return isFirstValueStack.get(isFirstValueStack.size() - 1);
    }
  }

  static class HtmlArray extends JavaScriptObject implements Json.Array {
    protected HtmlArray() {
    }

    @Override
    public final native Array getArray(int index) /*-{
      return this[index];
    }-*/;

    @Override
    public final native boolean getBoolean(int index) /*-{
      return this[index];
    }-*/;

    // FIXME TODO XXX: remove this parseFloat once we fix all the JSON
    @Override
    public final native double getNumber(int index) /*-{
      return parseFloat(this[index]);
    }-*/;

    @Override
    public final native int getInt(int index) /*-{
      if (!this[index]) return 0;
      return parseInt(this[index]);
    }-*/;

    @Override
    public final native Object getObject(int index) /*-{
      return this[index];
    }-*/;

    @Override
    public final native String getString(int index) /*-{
      return this[index];
    }-*/;

    public final <T> TypedArray<T> getArray(int index, Class<T> arrayType) {
      return arrayBuilder.build(getArray(index), arrayType);
    }

    @Override
    public final native int length() /*-{
      return this.length;
    }-*/;
  }

  static class HtmlObject extends JavaScriptObject implements Json.Object {
    protected HtmlObject() {
    }

    @Override
    public final native Array getArray(String key) /*-{
      return this[key];
    }-*/;

    @Override
    public final native boolean getBoolean(String key) /*-{
      return this[key];
    }-*/;

    @Override
    public final native int getInt(String key) /*-{
      if (!this[key]) return 0;
      return parseInt(this[key]);
    }-*/;

    // FIXME TODO XXX: remove this parseFloat once we fix all the JSON
    @Override
    public final native double getNumber(String key) /*-{
      return parseFloat(this[key]);
    }-*/;

    @Override
    public final native Object getObject(String key) /*-{
      return this[key];
    }-*/;

    @Override
    public final native String getString(String key) /*-{
      return this[key];
    }-*/;

    @Override
    public final <T> TypedArray<T> getArray(String key, Class<T> arrayType) {
      return arrayBuilder.build(getArray(key), arrayType);
    }

    @Override
    public final native boolean containsKey(String key) /*-{
      return this.hasOwnProperty(key);
    }-*/;

    @Override
    public final TypedArray<String> getKeys() {
      return arrayBuilder.build(getNativeKeys(), String.class);
    }

    private final native Array getNativeKeys() /*-{
      if (Object.prototype.keys) { return this.keys(); }
      var keys = [];
      for (var key in this) if (this.hasOwnProperty(key)) {
        keys.push(key);
      }
      return keys;
    }-*/;

  }

  @Override
  public Writer newWriter() {
    return new HtmlWriter();
  }

  @Override
  public Object parse(String json) {
    HtmlObject object = jsonParse(json).cast();
    return object;
  }

  private static native JavaScriptObject jsonParse(String json) /*-{
    return JSON.parse(json);
  }-*/;

  private static TypedArrayBuilder<Array> arrayBuilder = new TypedArrayBuilder<Array>() {
    public int length(Array array) {
      return array.length();
    }
    public Json.Object getObject(Array array, int index) {
      return array.getObject(index);
    }
    public Boolean getBoolean(Array array, int index) {
      return array.getBoolean(index);
    }
    public Integer getInt(Array array, int index) {
      return array.getInt(index);
    }
    public Double getNumber(Array array, int index) {
      return array.getNumber(index);
    }
    public String getString(Array array, int index) {
      return array.getString(index);
    }
  };
}
