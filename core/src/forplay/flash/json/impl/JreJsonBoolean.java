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

import forplay.flash.json.JsonBoolean;
import forplay.flash.json.JsonType;
import forplay.flash.json.JsonValue;

/**
 * Server-side implementation of JsonBoolean.
 */
public class JreJsonBoolean extends JreJsonValue implements JsonBoolean {

  private boolean bool;
  public JreJsonBoolean(boolean bool) {
    this.bool = bool;
  }

  public boolean getBoolean() {
    return bool;
  }

  public Object getObject() {
    return getBoolean();
  }

  public JsonType getType() {
    return JsonType.BOOLEAN;
  }

  @Override
  public boolean jsEquals(JsonValue value) {
    return getObject().equals(((JreJsonValue)value).getObject());
  }

  @Override
  public void traverse(forplay.flash.json.impl.JsonVisitor visitor, JsonContext ctx) {
    visitor.visit(getBoolean(), ctx);
  }

  public String toJson() throws IllegalStateException {
    return String.valueOf(bool);
  }
}
