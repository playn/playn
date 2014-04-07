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
package playn.core.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.Arrays;

import org.junit.Test;

public class InternalJsonTypesTest {
  @Test
  public void testObjectInt() {
    JsonObject o = new JsonObject();
    o.put("key", 1);
    assertEquals(1, o.getInt("key"));
    assertEquals(1.0, o.getDouble("key"), 0.0001f);
    assertEquals(1.0f, o.getNumber("key"), 0.0001f);
    assertEquals(1, o.getNumber("key"), 0.0001f);
    assertEquals(1, o.get("key"));

    assertEquals(null, o.getString("key"));
    assertEquals("foo", o.getString("key", "foo"));
    assertFalse(o.isNull("key"));
  }

  @Test
  public void testObjectString() {
    JsonObject o = new JsonObject();
    o.put("key", "1");
    assertEquals(0, o.getInt("key"));
    assertEquals(0, o.getDouble("key"), 0.0001f);
    assertEquals(0f, o.getNumber("key"), 0.0001f);
    assertEquals("1", o.get("key"));
    assertFalse(o.isNull("key"));
  }

  @Test
  public void testObjectNull() {
    JsonObject o = new JsonObject();
    o.put("key", null);
    assertEquals(0, o.getInt("key"));
    assertEquals(0, o.getDouble("key"), 0.0001f);
    assertEquals(0f, o.getNumber("key"), 0.0001f);
    assertEquals(null, o.get("key"));
    assertTrue(o.isNull("key"));
  }

  @Test
  public void testArrayInt() {
    JsonArray o = new JsonArray(Arrays.asList((String)null, null, null, null));
    o.set(3, 1);
    assertEquals(1, o.getInt(3));
    assertEquals(1.0, o.getDouble(3), 0.0001f);
    assertEquals(1.0f, o.getNumber(3), 0.0001f);
    assertEquals(1, o.getNumber(3), 0.0001f);
    assertEquals(1, o.get(3));

    assertEquals(null, o.getString(3));
    assertEquals("foo", o.getString(3, "foo"));
    assertFalse(o.isNull(3));
  }

  @Test
  public void testArrayString() {
    JsonArray o = new JsonArray(Arrays.asList((String)null, null, null, null));
    o.set(3, "1");
    assertEquals(0, o.getInt(3));
    assertEquals(0, o.getDouble(3), 0.0001f);
    assertEquals(0, o.getNumber(3), 0.0001f);
    assertEquals("1", o.get(3));
    assertFalse(o.isNull(3));
  }

  @Test
  public void testArrayNull() {
    JsonArray a = new JsonArray(Arrays.asList((String)null, null, null, null));
    a.set(3, null);
    assertEquals(0, a.getInt(3));
    assertEquals(0, a.getDouble(3), 0.0001f);
    assertEquals(0, a.getNumber(3), 0.0001f);
    assertEquals(null, a.get(3));
    assertTrue(a.isNull(3));
  }

  @Test
  public void testArrayBounds() {
    JsonArray a = new JsonArray(Arrays.asList((String)null, null, null, null));
    assertEquals(0, a.getInt(4));
    assertEquals(0, a.getDouble(4), 0.0001f);
    assertEquals(0, a.getNumber(4), 0.0001f);
    assertEquals(null, a.get(4));
    assertTrue(a.isNull(4));
  }

  @Test
  public void testJsonArrayBuilder() {
    //@formatter:off
    JsonArray a = JsonArray.builder()
        .value(true)
        .value(1.0)
        .value(1.0f)
        .value(1)
        .value(new BigInteger("1234567890"))
        .value("hi")
        .object()
          .value("abc", 123)
        .end()
        .array()
          .value(1)
          .nul()
        .end()
        .array(JsonArray.from(1, 2, 3))
        .object(JsonObject.builder().nul("a").nul("b").nul("c").done())
      .done();
    //@formatter:on

    assertEquals(
        "[true,1.0,1.0,1,1234567890,\"hi\",{\"abc\":123},[1,null],[1,2,3],{\"a\":null,\"b\":null,\"c\":null}]",
        JsonStringWriter.toString(a));
  }

  @Test
  public void testJsonObjectBuilder() {
    //@formatter:off
    JsonObject a = JsonObject.builder()
        .value("bool", true)
        .value("double", 1.0)
        .value("float", 1.0f)
        .value("int", 1)
        .value("bigint", new BigInteger("1234567890"))
        .value("string", "hi")
        .nul("null")
        .object("object")
          .value("abc", 123)
        .end()
        .array("array")
          .value(1)
          .nul()
        .end()
        .array("existingArray", JsonArray.from(1, 2, 3))
        .object("existingObject", JsonObject.builder().nul("a").nul("b").nul("c").done())
      .done();
    //@formatter:on

    assertEquals(
      "{\"array\":[1,null],\"bigint\":1234567890,\"bool\":true,\"double\":1.0," +
      "\"existingArray\":[1,2,3],\"existingObject\":{\"a\":null,\"b\":null,\"c\":null}," +
      "\"float\":1.0,\"int\":1,\"null\":null,\"object\":{\"abc\":123},\"string\":\"hi\"}",
        JsonStringWriter.toString(a));
  }

  @Test(expected = JsonWriterException.class)
  public void testJsonArrayBuilderFailCantCloseRoot() {
    JsonArray.builder().end();
  }

  @Test(expected = JsonWriterException.class)
  public void testJsonArrayBuilderFailCantAddKeyToArray() {
    JsonArray.builder().value("abc", 1);
  }

  @Test(expected = JsonWriterException.class)
  public void testJsonArrayBuilderFailCantAddNonKeyToObject() {
    JsonObject.builder().value(1);
  }

}
