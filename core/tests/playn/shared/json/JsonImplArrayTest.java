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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import org.junit.Test;

import playn.core.Json;
import playn.core.Json.TypedArray;

/**
 * Tests in this class are re-used to test HTML mode.
 */
public class JsonImplArrayTest extends AbstractJsonTest {
  @Test
  public void testArrayBasics() {
    Json.Array a = json().parseArray("[1,2]");
    assertEquals(2, a.length());
    
    a.remove(0);
    assertEquals(1, a.length());
    
    a.remove(-1);
    a.remove(10);
    assertEquals(1, a.length());
  }

  @Test
  public void testArrayTypeChecks() {
    Json.Array a = json().parseArray("[[], true, 1, null, \"\", {}]");

    checkTypes(a);
  }

  @Test
  public void testArrayAdd() {
    Json.Array a = json().createArray();
    a.add(2, json().createObject());
    assertEquals(3, a.length());
    assertTrue(a.isNull(0));
    assertTrue(a.isNull(1));
    assertTrue(a.isObject(2));
    
    a.set(0, null);
    a.set(1, "abcdef");

    a.add(0, 123);
    a.add(0, true);
    a.add(0, json().createArray());

    assertEquals(6, a.length());
    
    checkTypes(a);
  }
  
  @Test
  public void testArrayAppend() {
    Json.Array a = json().createArray();
    a.add(json().createArray());
    a.add(true);
    a.add(123);
    a.add(null);
    a.add("abcdef");
    a.add(json().createObject());
    
    checkTypes(a);
  }

  @Test
  public void testArraySet() {
    Json.Array a = json().createArray();
    a.set(5, json().createObject());
    assertEquals(6, a.length());
    a.set(1, true);
    a.set(2, 123);
    a.set(3, null);
    a.set(4, "abcdef");
    a.set(0, json().createArray());
    assertEquals(6, a.length());
    
    checkTypes(a);
  }
  
  @Test
  public void testIsArray() {
    assertFalse(json().isArray(json().createObject()));
    assertFalse(json().isArray(json().parse("{\"a\":1, \"b\":2}")));
    assertTrue(json().isArray(json().createArray()));
    assertTrue(json().isArray(json().parseArray("[1,2]")));

    // Test some other other things to make sure they don't look like arrays
    assertFalse(json().isArray(""));
    assertFalse(json().isArray(new Double(1)));
    assertFalse(json().isArray(null));
  }
  
  @Test
  public void testTypedArray() {
    Json.Array a = json().parseArray("[[1,2,3], [\"1\", \"2\"]]");
    TypedArray<Integer> ta = a.getArray(0, Integer.class);
    assertEquals(3, ta.length());
    assertEquals(1, (int)ta.get(0));
    assertEquals(2, (int)ta.get(1));
    assertEquals(3, (int)ta.get(2));
    
    TypedArray<String> ta2 = a.getArray(1, String.class);
    assertEquals(2, ta2.length());
    assertEquals("1", ta2.get(0));
    assertEquals("2", ta2.get(1));
  }
  
  private void checkTypes(Json.Array a) {
    assertTrue(a.isArray(0));
    assertFalse(a.isArray(1));
    assertFalse(a.isArray(2));
    assertFalse(a.isArray(3));
    assertFalse(a.isArray(4));
    assertFalse(a.isArray(5));
    assertFalse(a.isArray(6));

    assertFalse(a.isBoolean(0));
    assertTrue(a.isBoolean(1));
    assertFalse(a.isBoolean(2));
    assertFalse(a.isBoolean(3));
    assertFalse(a.isBoolean(4));
    assertFalse(a.isBoolean(5));
    assertFalse(a.isBoolean(6));

    assertFalse(a.isNumber(0));
    assertFalse(a.isNumber(1));
    assertTrue(a.isNumber(2));
    assertFalse(a.isNumber(3));
    assertFalse(a.isNumber(4));
    assertFalse(a.isNumber(5));
    assertFalse(a.isNumber(6));

    assertFalse(a.isNull(0));
    assertFalse(a.isNull(1));
    assertFalse(a.isNull(2));
    assertTrue(a.isNull(3));
    assertFalse(a.isNull(4));
    assertFalse(a.isNull(5));
    assertTrue(a.isNull(6)); // undefined == null

    assertFalse(a.isString(0));
    assertFalse(a.isString(1));
    assertFalse(a.isString(2));
    assertFalse(a.isString(3));
    assertTrue(a.isString(4));
    assertFalse(a.isString(5));
    assertFalse(a.isString(6));

    assertFalse(a.isObject(0));
    assertFalse(a.isObject(1));
    assertFalse(a.isObject(2));
    assertFalse(a.isObject(3));
    assertFalse(a.isObject(4));
    assertTrue(a.isObject(5));
    assertFalse(a.isObject(6));
  }
  
}
