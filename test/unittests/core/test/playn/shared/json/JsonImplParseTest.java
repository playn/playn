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

import org.junit.Test;

import playn.core.Json;

/**
 * Test the public-facing {@link Json} parsing APIs. The underlying parser is exercised in other
 * tests.
 * 
 * Tests in this class are re-used to test HTML mode.
 */
public class JsonImplParseTest extends AbstractJsonTest {
  @Test
  public void testParseObject() {
    Json.Object o = json().parse("{\"a\":\"b\", \"b\": 1}");
    assertEquals("b", o.getString("a"));
    assertEquals(0, o.getInt("a"));
    assertEquals(0f, o.getNumber("a"), 0.001f);
    assertEquals(0.0, o.getDouble("a"), 0.001);

    assertEquals(1, o.getInt("b"));
    assertEquals(1f, o.getNumber("b"), 0.001f);
    assertEquals(1, o.getDouble("b"), 0.001f);
  }

  @Test
  public void testParseArray() {
    Json.Array a = json().parseArray("[\"a\", 1, true, [], null, {}]");
    assertEquals("a", a.getString(0));
    assertEquals(0, a.getInt(0));
    assertEquals(0f, a.getNumber(0), 0.001f);
    assertEquals(0.0, a.getDouble(0), 0.001);

    assertEquals(1, a.getInt(1));
    assertEquals(1f, a.getNumber(1), 0.001f);
    assertEquals(1, a.getDouble(1), 0.001f);
    
    assertEquals(true, a.getBoolean(2));
  }
}
