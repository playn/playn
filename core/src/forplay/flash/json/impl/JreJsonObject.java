/*
 * Copyright 2010 Google Inc.
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
package forplay.flash.json.impl;

import forplay.flash.json.JsonFactory;
import forplay.flash.json.JsonObject;
import forplay.flash.json.JsonType;
import forplay.flash.json.JsonValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Server-side implementation of JsonObject.
 */
public class JreJsonObject extends JreJsonValue implements JsonObject {

  private JsonFactory factory;
  private Map<String, JsonValue> map = new LinkedHashMap<String, JsonValue>();

  public JreJsonObject(JsonFactory factory) {
    this.factory = factory;
  }

  public <T extends JsonValue> T get(String key) {
    @SuppressWarnings("unchecked") T value = (T) map.get(key);
    return value;
  }
  
  public Object getObject() {
    Map<String, Object> obj = new HashMap<String, Object>();
    for (Map.Entry<String, JsonValue> e : map.entrySet()) {
      obj.put(e.getKey(), ((JreJsonValue)e.getValue()).getObject());
    }
    return obj;
  }

  public JsonType getType() {
    return JsonType.OBJECT;
  }

  @Override
  public boolean hasKey(String key) {
    return map.containsKey(key);
  }

  public String[] keys() {
    return map.keySet().toArray(new String[map.size()]);
  }

  public void put(String key, JsonValue value) {
    if (value == null) {
      value = factory.createNull();
    }
    map.put(key, value);
  }

  public void put(String key, String value) {
    put(key, factory.create(value));
  }

  public void put(String key, double value) {
    put(key, factory.create(value));
  }

  public void put(String key, boolean bool) {
    put(key, factory.create(bool));
  }

  @Override
  public void remove(String key) {
    map.remove(key);
  }

  @Override
  public boolean jsEquals(JsonValue value) {
    return getObject().equals(((JreJsonValue)value).getObject());
  }

  public String toJson() {
    return JsonUtil.stringify(this);
  }

  public String toString() {
    return toJson();
  }

  @Override
  public void traverse(JsonVisitor visitor, JsonContext ctx) {
    if (visitor.visit(this, ctx)) {
      JsonObjectContext objCtx = new JsonObjectContext(this);
      for (String key : stringifyOrder(keys())) {
        objCtx.setCurrentKey(key);
        if (visitor.visitKey(objCtx.getCurrentKey(), objCtx)) {
          visitor.accept(get(key), objCtx);
          objCtx.setFirst(false);
        }
      }
    }
    visitor.endVisit(this, ctx);
  }

  private static List<String> stringifyOrder(String[] keys) {
    List<String> toReturn = new ArrayList<String>();
    List<String> nonNumeric = new ArrayList<String>();
    for (String key : keys) {
      if (key.matches("\\d+")) {
        toReturn.add(key);
      } else {
        nonNumeric.add(key);
      }
    }
    Collections.sort(toReturn);
    toReturn.addAll(nonNumeric);
    return toReturn;
  }
}
