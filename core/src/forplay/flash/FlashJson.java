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
package forplay.flash;

import forplay.flash.json.JsonArray;
import forplay.flash.json.JsonNumber;
import forplay.flash.json.JsonBoolean;
import forplay.flash.json.JsonValue;
import forplay.flash.json.JsonString;
import forplay.flash.json.JsonObject;

import forplay.core.Json;

class FlashJson  implements Json {


  /* (non-Javadoc)
   * @see forplay.core.Json#parse(java.lang.String)
   */
  @Override
  public Object parse(String json) {
    return new ObjectImpl((JsonObject) forplay.flash.json.Json.instance().parse(json));
   
  }

  static class ArrayImpl implements Array {

    private final JsonArray arr;

    /**
     * @param jsonArray
     */
    public ArrayImpl(JsonArray arr) {
      this.arr = arr != null ? arr : forplay.flash.json.Json.instance().createArray();
    }

    /* (non-Javadoc)
     * @see forplay.core.Json.Array#getArray(int)
     */
    @Override
    public Array getArray(int index) {
     return new ArrayImpl((JsonArray) arr.get(index));
    }

    /* (non-Javadoc)
     * @see forplay.core.Json.Array#getBoolean(int)
     */
    @Override
    public boolean getBoolean(int index) {
      // TODO Auto-generated method stub
      JsonValue b =  arr.get(index);
      return b != null ? ((JsonBoolean)b).getBoolean() : false;
    }

    /* (non-Javadoc)
     * @see forplay.core.Json.Array#getInt(int)
     */
    @Override
    public int getInt(int index) {
      // TODO Auto-generated method stub
      return (int) getNumber(index);
    }

    /* (non-Javadoc)
     * @see forplay.core.Json.Array#getNumber(int)
     */
    @Override
    public double getNumber(int index) {
      JsonValue n = arr.get(index);
      return n != null ? ((JsonNumber) n).getNumber() : 0;
    }

    /* (non-Javadoc)
     * @see forplay.core.Json.Array#getObject(int)
     */
    @Override
    public Object getObject(int index) {
      return new ObjectImpl((JsonObject) arr.get(index));
    }

    /* (non-Javadoc)
     * @see forplay.core.Json.Array#getString(int)
     */
    @Override
    public String getString(int index) {
      JsonValue s = arr.get(index);
      return s != null ? ((JsonString)s).getString() : "";
    }

    /* (non-Javadoc)
     * @see forplay.core.Json.Array#length()
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
      this.obj = obj != null ? obj : forplay.flash.json.Json.instance().createObject();
    }

    /* (non-Javadoc)
     * @see forplay.core.Json.Object#getArray(java.lang.String)
     */
    @Override
    public Array getArray(String key) {
      return new ArrayImpl((JsonArray) obj.get(key));
    }

    /* (non-Javadoc)
     * @see forplay.core.Json.Object#getBoolean(java.lang.String)
     */
    @Override
    public boolean getBoolean(String key) {
      JsonValue o = obj.get(key);
      return o != null ? ((JsonBoolean)o).getBoolean() : false;
    }

    /* (non-Javadoc)
     * @see forplay.core.Json.Object#getInt(java.lang.String)
     */
    @Override
    public int getInt(String key) {
      return (int) getNumber(key);
    }

    /* (non-Javadoc)
     * @see forplay.core.Json.Object#getKeys()
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
     * @see forplay.core.Json.Object#getNumber(java.lang.String)
     */
    @Override
    public double getNumber(String key) {
      JsonValue n = obj.get(key);
      return n != null ? ((JsonNumber) n).getNumber() : 0;
    }

    /* (non-Javadoc)
     * @see forplay.core.Json.Object#getObject(java.lang.String)
     */
    @Override
    public Object getObject(String key) {
      return new ObjectImpl((JsonObject) obj.get(key));
    }

    /* (non-Javadoc)
     * @see forplay.core.Json.Object#getString(java.lang.String)
     */
    @Override
    public String getString(String key) {
      JsonValue s = obj.get(key);
      return s != null ? ((JsonString) s).getString() : "";
    }
    
  }
  /* (non-Javadoc)
   * @see forplay.core.Json#newWriter()
   */
  @Override
  public Writer newWriter() {
    // TODO Auto-generated method stub
    return null;
  }

}
