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

  @Override
  public Object parse(String json) {
    return new ObjectImpl((JsonObject) playn.flash.json.Json.instance().parse(json));
  }

  static class ArrayImpl implements Array {

    private final JsonArray arr;

    public ArrayImpl(JsonArray arr) {
      this.arr = (arr != null) ? arr : playn.flash.json.Json.instance().createArray();
    }

    @Override
    public Array getArray(int index) {
      return new ArrayImpl((JsonArray) arr.get(index));
    }

    @Override
    public boolean getBoolean(int index) {
      return valueToBoolean(arr.get(index));
    }

    @Override
    public int getInt(int index) {
      return (int) getNumber(index);
    }

    @Override
    public double getNumber(int index) {
      return valueToNumber(arr.get(index));
    }

    @Override
    public Object getObject(int index) {
      return valueToObject(arr.get(index));
    }

    @Override
    public String getString(int index) {
      return valueToString(arr.get(index));
    }

    @Override
    public TypedArray<Boolean> getBooleanArray(int index) {
      return asBooleanArray((JsonArray) arr.get(index));
    }

    @Override
    public TypedArray<Integer> getIntArray(int index) {
      return asIntArray((JsonArray) arr.get(index));
    }

    @Override
    public TypedArray<Double> getNumberArray(int index) {
      return asNumberArray((JsonArray) arr.get(index));
    }

    @Override
    public TypedArray<String> getStringArray(int index) {
      return asStringArray((JsonArray) arr.get(index));
    }

    @Override
    public TypedArray<Object> getObjectArray(int index) {
      return asObjectArray((JsonArray) arr.get(index));
    }

    @Override
    public int length() {
      return arr.length();
    }
  }

  static class ObjectImpl implements Object {

    private final JsonObject obj;

    public ObjectImpl(JsonObject obj) {
      this.obj = obj != null ? obj : playn.flash.json.Json.instance().createObject();
    }

    @Override
    public Array getArray(String key) {
      return new ArrayImpl((JsonArray) obj.get(key));
    }

    @Override
    public boolean getBoolean(String key) {
      return valueToBoolean(obj.get(key));
    }

    @Override
    public int getInt(String key) {
      return (int) getNumber(key);
    }

    @Override
    public boolean containsKey(String key) {
      return obj.hasKey(key);
    }

    @Override
    public TypedArray<String> getKeys() {
      return new TypedArray<String>() {
        @Override
        public int length() {
          return obj.keys().length;
        }
        @Override
        protected String getImpl(int index) {
          return obj.keys()[index];
        }
      };
    }

    @Override
    public double getNumber(String key) {
      return valueToNumber(obj.get(key));
    }

    @Override
    public Object getObject(String key) {
      return valueToObject(obj.get(key));
    }

    @Override
    public String getString(String key) {
      return valueToString(obj.get(key));
    }

    @Override
    public TypedArray<Boolean> getBooleanArray(String key) {
      return asBooleanArray((JsonArray) obj.get(key));
    }

    @Override
    public TypedArray<Integer> getIntArray(String key) {
      return asIntArray((JsonArray) obj.get(key));
    }

    @Override
    public TypedArray<Double> getNumberArray(String key) {
      return asNumberArray((JsonArray) obj.get(key));
    }

    @Override
    public TypedArray<String> getStringArray(String key) {
      return asStringArray((JsonArray) obj.get(key));
    }

    @Override
    public TypedArray<Object> getObjectArray(String key) {
      return asObjectArray((JsonArray) obj.get(key));
    }
  }

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

  private static boolean valueToBoolean(JsonValue value) {
    return value != null ? ((JsonBoolean) value).getBoolean() : false;
  }

  private static double valueToNumber(JsonValue value) {
    return value != null ? ((JsonNumber) value).getNumber() : 0;
  }

  // TODO: the default value handling of this and valueToObject differ from the other backends
  private static String valueToString(JsonValue value) {
    return value != null ? ((JsonString) value).getString() : "";
  }

  private static Object valueToObject(JsonValue value) {
    return new ObjectImpl((JsonObject) value);
  }

  private static TypedArray<Boolean> asBooleanArray(final JsonArray jsa) {
    return jsa == null ? null : new TypedArray<Boolean>() {
      @Override
      public int length() {
        return jsa.length();
      }
      @Override
      protected Boolean getImpl(int index) {
        return valueToBoolean(jsa.get(index));
      }
    };
  }

  private static TypedArray<Integer> asIntArray(final JsonArray jsa) {
    return jsa == null ? null : new TypedArray<Integer>() {
      @Override
      public int length() {
        return jsa.length();
      }
      @Override
      protected Integer getImpl(int index) {
        return (int)valueToNumber(jsa.get(index));
      }
    };
  }

  private static TypedArray<Double> asNumberArray(final JsonArray jsa) {
    return jsa == null ? null : new TypedArray<Double>() {
      @Override
      public int length() {
        return jsa.length();
      }
      @Override
      protected Double getImpl(int index) {
        return valueToNumber(jsa.get(index));
      }
    };
  }

  private static TypedArray<String> asStringArray(final JsonArray jsa) {
    return jsa == null ? null : new TypedArray<String>() {
      @Override
      public int length() {
        return jsa.length();
      }
      @Override
      protected String getImpl(int index) {
        return valueToString(jsa.get(index));
      }
    };
  }

  private static TypedArray<Object> asObjectArray(final JsonArray jsa) {
    return jsa == null ? null : new TypedArray<Object>() {
      @Override
      public int length() {
        return jsa.length();
      }
      @Override
      protected Object getImpl(int index) {
        return valueToObject(jsa.get(index));
      }
    };
  }
}
