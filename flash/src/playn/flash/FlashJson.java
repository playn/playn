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
package playn.flash;

import java.util.ArrayList;

import playn.core.Asserts;
import playn.core.Json;
import playn.flash.json.JsonArray;
import playn.flash.json.JsonBoolean;
import playn.flash.json.JsonNumber;
import playn.flash.json.JsonObject;
import playn.flash.json.JsonString;
import playn.flash.json.JsonValue;

class FlashJson implements Json {

  /* (non-Javadoc)
  * @see playn.core.Json#parse(java.lang.String)
  */
  @Override
  public Object parse(String json) {
    return new ObjectImpl((JsonObject) playn.flash.json.Json.instance().parse(json));
  }

  static class ArrayImpl implements Array {

    private final JsonArray arr;

    /**
     * @param jsonArray
     */
    public ArrayImpl(JsonArray arr) {
      this.arr = arr != null ? arr : playn.flash.json.Json.instance().createArray();
    }

    /* (non-Javadoc)
     * @see playn.core.Json.Array#getArray(int)
     */
    @Override
    public Array getArray(int index) {
      return new ArrayImpl((JsonArray) arr.get(index));
    }

    /* (non-Javadoc)
     * @see playn.core.Json.Array#getBoolean(int)
     */
    @Override
    public boolean getBoolean(int index) {
      // TODO Auto-generated method stub
      JsonValue b = arr.get(index);
      return b != null ? ((JsonBoolean) b).getBoolean() : false;
    }

    /* (non-Javadoc)
     * @see playn.core.Json.Array#getInt(int)
     */
    @Override
    public int getInt(int index) {
      // TODO Auto-generated method stub
      return (int) getNumber(index);
    }

    /* (non-Javadoc)
     * @see playn.core.Json.Array#getNumber(int)
     */
    @Override
    public double getNumber(int index) {
      JsonValue n = arr.get(index);
      return n != null ? ((JsonNumber) n).getNumber() : 0;
    }

    /* (non-Javadoc)
     * @see playn.core.Json.Array#getObject(int)
     */
    @Override
    public Object getObject(int index) {
      return new ObjectImpl((JsonObject) arr.get(index));
    }

    /* (non-Javadoc)
     * @see playn.core.Json.Array#getString(int)
     */
    @Override
    public String getString(int index) {
      JsonValue s = arr.get(index);
      return s != null ? ((JsonString) s).getString() : "";
    }

    /* (non-Javadoc)
     * @see playn.core.Json.Array#length()
     */
    @Override
    public int length() {
      return arr.length();
    }
  }

  static class ObjectImpl implements Object {

    private final JsonObject obj;

    /**
     * @param parse
     */
    public ObjectImpl(JsonObject obj) {
      this.obj = obj != null ? obj : playn.flash.json.Json.instance().createObject();
    }

    /* (non-Javadoc)
     * @see playn.core.Json.Object#getArray(java.lang.String)
     */
    @Override
    public Array getArray(String key) {
      return new ArrayImpl((JsonArray) obj.get(key));
    }

    /* (non-Javadoc)
     * @see playn.core.Json.Object#getBoolean(java.lang.String)
     */
    @Override
    public boolean getBoolean(String key) {
      JsonValue o = obj.get(key);
      return o != null ? ((JsonBoolean) o).getBoolean() : false;
    }

    /* (non-Javadoc)
     * @see playn.core.Json.Object#getInt(java.lang.String)
     */
    @Override
    public int getInt(String key) {
      return (int) getNumber(key);
    }

    /* (non-Javadoc)
     * @see playn.core.Json.Object#getKeys()
     */
    @Override
    public Array getKeys() {
      // TODO Auto-generated method stub
      return new Array() {

        @Override
        public Array getArray(int index) {
          // TODO Auto-generated method stub
          return null;
        }

        @Override
        public boolean getBoolean(int index) {
          // TODO Auto-generated method stub
          return false;
        }

        @Override
        public int getInt(int index) {
          // TODO Auto-generated method stub
          return 0;
        }

        @Override
        public double getNumber(int index) {
          // TODO Auto-generated method stub
          return 0;
        }

        @Override
        public Object getObject(int index) {
          // TODO Auto-generated method stub
          return null;
        }

        @Override
        public String getString(int index) {
          return obj.keys()[index];
        }

        @Override
        public int length() {
          return obj.keys().length;
        }
      };
    }

    /* (non-Javadoc)
     * @see playn.core.Json.Object#getNumber(java.lang.String)
     */
    @Override
    public double getNumber(String key) {
      JsonValue n = obj.get(key);
      return n != null ? ((JsonNumber) n).getNumber() : 0;
    }

    /* (non-Javadoc)
     * @see playn.core.Json.Object#getObject(java.lang.String)
     */
    @Override
    public Object getObject(String key) {
      return new ObjectImpl((JsonObject) obj.get(key));
    }

    /* (non-Javadoc)
     * @see playn.core.Json.Object#getString(java.lang.String)
     */
    @Override
    public String getString(String key) {
      JsonValue s = obj.get(key);
      return s != null ? ((JsonString) s).getString() : "";
    }
  }

  /* (non-Javadoc)
  * @see playn.core.Json#newWriter()
  */
  @Override
  public Writer newWriter() {
    // TODO Auto-generated method stub
    return new FlashWriter();
  }

  static class FlashWriter implements Writer {

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
}
