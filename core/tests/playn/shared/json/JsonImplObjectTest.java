/**
 * Copyright 2011 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package playn.shared.json;

import static junit.framework.Assert.*;

import org.junit.Test;

import playn.core.Json;
import playn.core.Json.TypedArray;

/**
 * Tests in this class are re-used to test HTML mode.
 */
public class JsonImplObjectTest extends AbstractJsonTest {
  @Test
  public void testIsObject() {
    assertTrue(json().isObject(json().createObject()));
    assertTrue(json().isObject(json().parse("{\"a\":1, \"b\":2}")));
    assertFalse(json().isObject(json().createArray()));
    assertFalse(json().isObject(json().parseArray("[1,2]")));

    // Test some other other things to make sure they don't look like objects
    assertFalse(json().isObject(""));
    assertFalse(json().isObject(new Double(1)));
    assertFalse(json().isObject(null));
  }

  @Test
  public void testObjectBasics() {
    Json.Object o = json().parse("{\"a\":1, \"b\":2}");
    TypedArray<String> keys = o.keys();
    for (String s : keys) {
      assertTrue(s.equals("a") || s.equals("b"));
    }
    assertEquals(2, keys.length());

    o.put("b", "string");
    assertTrue(o.isString("b"));

    o.remove("b");
    assertFalse(o.isString("b"));
    assertTrue(o.isNull("b"));

    assertEquals(1, o.keys().length());
  }

  @Test
  public void testObjectTypes() {
    Json.Object o = json().parse("{\"null\": null, \"number\":1, \"string\":\"string\", " +
                                 "\"boolean\":true, \"array\":[], \"object\":{}}");

    assertTrue(o.isArray("array"));
    assertFalse(o.isArray("boolean"));
    assertFalse(o.isArray("number"));
    assertFalse(o.isArray("null"));
    assertFalse(o.isArray("string"));
    assertFalse(o.isArray("object"));
    assertFalse(o.isArray("does not exist"));

    assertFalse(o.isBoolean("array"));
    assertTrue(o.isBoolean("boolean"));
    assertFalse(o.isBoolean("number"));
    assertFalse(o.isBoolean("null"));
    assertFalse(o.isBoolean("string"));
    assertFalse(o.isBoolean("object"));
    assertFalse(o.isBoolean("does not exist"));

    assertFalse(o.isNumber("array"));
    assertFalse(o.isNumber("boolean"));
    assertTrue(o.isNumber("number"));
    assertFalse(o.isNumber("null"));
    assertFalse(o.isNumber("string"));
    assertFalse(o.isNumber("object"));
    assertFalse(o.isNumber("does not exist"));

    assertFalse(o.isNull("array"));
    assertFalse(o.isNull("boolean"));
    assertFalse(o.isNull("number"));
    assertTrue(o.isNull("null"));
    assertFalse(o.isNull("string"));
    assertFalse(o.isNull("object"));
    assertTrue(o.isNull("does not exist"));

    assertFalse(o.isString("array"));
    assertFalse(o.isString("boolean"));
    assertFalse(o.isString("number"));
    assertFalse(o.isString("null"));
    assertTrue(o.isString("string"));
    assertFalse(o.isString("object"));
    assertFalse(o.isString("does not exist"));

    assertFalse(o.isObject("array"));
    assertFalse(o.isObject("boolean"));
    assertFalse(o.isObject("number"));
    assertFalse(o.isObject("null"));
    assertFalse(o.isObject("string"));
    assertTrue(o.isObject("object"));
    assertFalse(o.isObject("does not exist"));
  }
}
