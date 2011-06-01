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

import forplay.flash.json.JsonArray;
import forplay.flash.json.JsonFactory;
import forplay.flash.json.JsonType;
import forplay.flash.json.JsonValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Server-side implementation of JsonArray.
 */
public class JreJsonArray extends JreJsonValue implements JsonArray {
  private ArrayList<JsonValue> arrayValues = new ArrayList<JsonValue>();

  private JsonFactory factory;

  public JreJsonArray(JsonFactory factory) {
    this.factory = factory;
  }

  public <T extends JsonValue> T get(int index) {
    @SuppressWarnings("unchecked") T value = (T) arrayValues.get(index);
    return value;
  }

  public Object getObject() {
    List<Object> objs = new ArrayList<Object>();
    for (JsonValue val : arrayValues) {
      objs.add(((JreJsonValue)val).getObject());
    }
    return objs;
  }

  public JsonType getType() {
    return forplay.flash.json.JsonType.ARRAY;
  }

  public int length() {
    return arrayValues.size();
  }

  @Override
  public void remove(int index) {
    arrayValues.remove(index);
  }

  public void set(int index, JsonValue value) {
    if (value == null) {
      value = factory.createNull();
    }
    if (index == arrayValues.size()) {
      arrayValues.add(index, value);
    } else {
      arrayValues.set(index, value);
    }
  }

  public void set(int index, String string) {
    set(index, factory.create(string));
  }

  public void set(int index, double number) {
    set(index, factory.create(number));
  }

  public void set(int index, boolean bool) {
    set(index, factory.create(bool));
  }

  @Override
  public boolean jsEquals(JsonValue value) {
    return getObject().equals(((JreJsonValue) value).getObject());
  }

  @Override
  public void traverse(forplay.flash.json.impl.JsonVisitor visitor, forplay.flash.json.impl.JsonContext ctx) {
    if (visitor.visit(this, ctx)) {
      JsonArrayContext arrayCtx = new JsonArrayContext(this);
      for (int i = 0; i < length(); i++) {
        arrayCtx.setCurrentIndex(i);
        if (visitor.visitIndex(arrayCtx.getCurrentIndex(), arrayCtx)) {
          visitor.accept(get(i), arrayCtx);
          arrayCtx.setFirst(false);         
        }
      }
    }
    visitor.endVisit(this, ctx);
  }

  public String toJson() {
    return JsonUtil.stringify(this);
  }
}
