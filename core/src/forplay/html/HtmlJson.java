/**
 * Copyright 2010 The ForPlay Authors
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
package forplay.html;

import java.util.ArrayList;

import com.google.gwt.core.client.JavaScriptObject;

import forplay.core.Asserts;
import forplay.core.Json;

class HtmlJson implements Json {

  class HtmlWriter implements Json.Writer {
    private StringBuilder sb = new StringBuilder();
    private String key;
    private ArrayList<Boolean> inArrayStack = new ArrayList<Boolean>();

    @Override
    public void array() {
      maybePrependKey();
      sb.append("[");
      pushInArray(true);
    }

    @Override
    public void endArray() {
      sb.append("]");
      popInArray();
    }

    @Override
    public void endObject() {
      sb.append("}");
      popInArray();
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
    }

    @Override
    public void value(boolean x) {
      maybePrependKey();
      sb.append(x);
      sb.append(",");
    }

    @Override
    public void value(double x) {
      maybePrependKey();
      sb.append(x);
      sb.append(",");
    }

    @Override
    public void value(int x) {
      maybePrependKey();
      sb.append(x);
      sb.append(",");
    }

    @Override
    public void value(String x) {
      maybePrependKey();
      sb.append(x);
      sb.append(",");
    }

    @Override
    public String write() {
      return sb.toString();
    }

    private void maybePrependKey() {
      maybePrependKey(false);
    }

    private void maybePrependKey(boolean isObject) {
      // Special case for the opening object.
      if (isObject && inArrayStack.size() == 0) {
        return;
      }

      if (inArray()) {
        Asserts.checkState(this.key == null);
      } else {
        Asserts.checkState(this.key != null);
        sb.append("'");
        sb.append(key);
        sb.append("':");
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
    public final native Array getKeys() /*-{
      if (Object.prototype.keys) { return this.keys(); }
      var keys = [];
      for (var key in this) if (this.hasOwnProperty(key)) {
        keys.push(key);
      }
      return keys;
    }-*/;
    
  }

  private static native JavaScriptObject jsonParse(String json) /*-{
    return JSON.parse(json);
  }-*/;

  @Override
  public Writer newWriter() {
    return new HtmlWriter();
  }

  @Override
  public Object parse(String json) {
    HtmlObject object = jsonParse(json).cast();
    return object;
  }
}
