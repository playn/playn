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
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Ignore;
import org.junit.Test;

public class InternalJsonWriterTest {
  /**
   * Test emitting simple values.
   */
  @Test
  public void testSimpleValues() {
    assertEquals("true", new JsonStringWriter().value(true).write());
    assertEquals("null", new JsonStringWriter().nul().write());
    assertEquals("1.0", new JsonStringWriter().value(1.0).write());
    assertEquals("1.0", new JsonStringWriter().value(1.0f).write());
    assertEquals("1", new JsonStringWriter().value(1).write());
    assertEquals("\"abc\"", new JsonStringWriter().value("abc").write());
  }

  /**
   * Test various ways of writing null, as well as various situations.
   */
  @Test
  public void testNull() {
    assertEquals("null", new JsonStringWriter().value((String)null).write());
    assertEquals("null", new JsonStringWriter().value((Number)null).write());
    assertEquals("null", new JsonStringWriter().nul().write());
    assertEquals("[null]", new JsonStringWriter().array().value((String)null).end().write());
    assertEquals("[null]", new JsonStringWriter().array().value((Number)null).end().write());
    assertEquals("[null]", new JsonStringWriter().array().nul().end().write());
    assertEquals("{\"a\":null}", new JsonStringWriter().
                 object().value("a", (String)null).end().write());
    assertEquals("{\"a\":null}", new JsonStringWriter().
                 object().value("a", (Number)null).end().write());
    assertEquals("{\"a\":null}", new JsonStringWriter().object().nul("a").end().write());
  }

  /**
   * Test escaping of chars < 256.
   */
  @Test
  public void testStringControlCharacters() {
    StringBuilder chars = new StringBuilder();
    for (int i = 0; i < 0xa0; i++)
      chars.append((char)i);

    assertEquals(
      "\"\\u0000\\u0001\\u0002\\u0003\\u0004\\u0005\\u0006\\u0007\\b\\t\\n\\u000b\\f\\r\\u000e\\u000f"
      + "\\u0010\\u0011\\u0012\\u0013\\u0014\\u0015\\u0016\\u0017\\u0018\\u0019\\u001a\\u001b\\u001c"
      + "\\u001d\\u001e\\u001f !\\\"#$%&'()*+,-./0123456789:;<=>?@"
      + "ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\\u0080\\u0081\\u0082"
      + "\\u0083\\u0084\\u0085\\u0086\\u0087\\u0088\\u0089\\u008a\\u008b\\u008c\\u008d\\u008e\\u008f"
      + "\\u0090\\u0091\\u0092\\u0093\\u0094\\u0095\\u0096\\u0097\\u0098\\u0099\\u009a\\u009b\\u009c"
      + "\\u009d\\u009e\\u009f\"", JsonStringWriter.toString(chars.toString()));
  }

  /**
   * Test escaping of chars < 256.
   */
  @Test
  public void testEscape() {
    StringBuilder chars = new StringBuilder();
    for (int i = 0; i < 0xa0; i++)
      chars.append((char)i);

    assertEquals(
      "\\u0000\\u0001\\u0002\\u0003\\u0004\\u0005\\u0006\\u0007\\b\\t\\n\\u000b\\f\\r\\u000e\\u000f"
      + "\\u0010\\u0011\\u0012\\u0013\\u0014\\u0015\\u0016\\u0017\\u0018\\u0019\\u001a\\u001b\\u001c"
      + "\\u001d\\u001e\\u001f !\\\"#$%&'()*+,-./0123456789:;<=>?@"
      + "ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\\u0080\\u0081\\u0082"
      + "\\u0083\\u0084\\u0085\\u0086\\u0087\\u0088\\u0089\\u008a\\u008b\\u008c\\u008d\\u008e\\u008f"
      + "\\u0090\\u0091\\u0092\\u0093\\u0094\\u0095\\u0096\\u0097\\u0098\\u0099\\u009a\\u009b\\u009c"
      + "\\u009d\\u009e\\u009f", JsonWriterBase.escape(chars.toString()));
  }

  @Test
  public void testWriteToSystemOutLikeStream() {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    new JsonAppendableWriter(
      new PrintStream(bytes)).object().value("a", 1).value("b", 2).end().done();
    assertEquals("{\"a\":1,\"b\":2}", new String(bytes.toByteArray(), Charset.forName("UTF-8")));
  }

  /**
   * Test escaping of / when following < to handle &lt;/script&gt;.
   */
  @Test
  public void testScriptEndEscaping() {
    assertEquals("\"<\\/script>\"", JsonStringWriter.toString("</script>"));
    assertEquals("\"/script\"", JsonStringWriter.toString("/script"));
  }

  /**
   * Test a simple array.
   */
  @Test
  public void testArray() {
    String json = new JsonStringWriter().array().value(true).value(false).value(true).end().write();
    assertEquals("[true,false,true]", json);
  }

  /**
   * Test an empty array.
   */
  @Test
  public void testArrayEmpty() {
    String json = new JsonStringWriter().array().end().write();
    assertEquals("[]", json);
  }

  /**
   * Test an array of empty arrays.
   */
  @Test
  public void testArrayOfEmpty() {
    String json = new JsonStringWriter().array().array().end().array().end().end().write();
    assertEquals("[[],[]]", json);
  }

  /**
   * Test a nested array.
   */
  @Test
  public void testNestedArray() {
    String json = new JsonStringWriter().array().array().array().value(true).value(false).
      value(true).end().end().end().write();
    assertEquals("[[[true,false,true]]]", json);
  }

  /**
   * Test a nested array.
   */
  @Test
  public void testNestedArray2() {
    String json = new JsonStringWriter().array().value(true).array().array().value(false).end().end().
      value(true).end().write();
    assertEquals("[true,[[false]],true]", json);
  }

  /**
   * Test a simple object.
   */
  @Test
  public void testObject() {
    String json = new JsonStringWriter().object().value("a", true).value("b", false).
      value("c", true).end().write();
    assertEquals("{\"a\":true,\"b\":false,\"c\":true}", json);
  }

  /**
   * Test a nested object.
   */
  @Test
  public void testNestedObject() {
    String json = new JsonStringWriter().object().object("a").value("b", false).value("c", true).
      end().end().write();
    assertEquals("{\"a\":{\"b\":false,\"c\":true}}", json);
  }

  /**
   * Test a nested object and array.
   */
  @Test
  public void testNestedObjectArray() {
    //@formatter:off
    String json = new JsonStringWriter()
      .object()
      .object("a")
      .array("b")
      .object()
      .value("a", 1)
      .value("b", 2)
      .end()
      .object()
      .value("c", 1.0)
      .value("d", 2.0)
      .end()
      .end()
      .value("c", JsonArray.from("a", "b", "c"))
      .end()
      .end()
      .write();
    //@formatter:on
    assertEquals("{\"a\":{\"b\":[{\"a\":1,\"b\":2},{\"c\":1.0,\"d\":2.0}]," +
                 "\"c\":[\"a\",\"b\",\"c\"]}}", json);
  }

  /**
   * Tests the {@link Appendable} code.
   */
  @Test
  public void testAppendable() {
    StringWriter writer = new StringWriter();
    new JsonAppendableWriter(writer).object().value("abc", "def").end().done();
    assertEquals("{\"abc\":\"def\"}", writer.toString());
  }

  @Test
  public void testQuickJson() {
    assertEquals("true", JsonStringWriter.toString(true));
  }

  @Test
  public void testQuickJsonArray() {
    assertEquals("[1,2,3]", JsonStringWriter.toString(JsonArray.from(1, 2, 3)));
  }

  @Test
  public void testQuickArray() {
    assertEquals("[1,2,3]", JsonStringWriter.toString(Arrays.asList(1, 2, 3)));
  }

  @Test
  public void testQuickArrayEmpty() {
    assertEquals("[]", JsonStringWriter.toString(Collections.emptyList()));
  }

  @Ignore("unsupported right now")
  @Test
  public void testQuickObjectArray() {
    assertEquals("[1,2,3]", JsonStringWriter.toString(new Object[] { 1, 2, 3 }));
  }

  @Ignore("unsupported right now")
  @Test
  public void testQuickObjectArrayNested() {
    assertEquals("[[1,2],[[3]]]", JsonStringWriter.toString(
                   new Object[] { new Object[] { 1, 2 }, new Object[] { new Object[] { 3 } } }));
  }

  @Ignore("unsupported right now")
  @Test
  public void testQuickObjectArrayEmpty() {
    assertEquals("[]", JsonStringWriter.toString(new Object[0]));
  }

  @Ignore("unsupported right now")
  @Test
  public void testObjectArrayInMap() {
    JsonObject o = new JsonObject();
    o.put("array of string", new String[] { "a", "b", "c" });
    o.put("array of Boolean", new Boolean[] { true, false });
    o.put("array of int", new int[] { 1, 2, 3 });
    o.put("array of JsonObject", new JsonObject[] { new JsonObject(), null });
    assertEquals("{\"array of JsonObject\":[{},null],\"array of Boolean\":[true,false]," +
                 "\"array of string\":[\"a\",\"b\",\"c\"],\"array of int\":[1,2,3]}",
                 JsonStringWriter.toString(o));
  }

  @Test
  public void testFailureNoKeyInObject() {
    try {
      new JsonStringWriter().object().value(true).end().write();
      fail();
    } catch (JsonWriterException e) {
      // OK
    }
  }

  @Test
  public void testFailureNoKeyInObject2() {
    try {
      new JsonStringWriter().object().value("a", 1).value(true).end().write();
      fail();
    } catch (JsonWriterException e) {
      // OK
    }
  }

  @Test
  public void testFailureKeyInArray() {
    try {
      new JsonStringWriter().array().value("x", true).end().write();
      fail();
    } catch (JsonWriterException e) {
      // OK
    }
  }

  @Test
  public void testFailureKeyInArray2() {
    try {
      new JsonStringWriter().array().value(1).value("x", true).end().write();
      fail();
    } catch (JsonWriterException e) {
      // OK
    }
  }

  @Test
  public void testFailureNotFullyClosed() {
    try {
      new JsonStringWriter().array().value(1).write();
      fail();
    } catch (JsonWriterException e) {
      // OK
    }
  }

  @Test
  public void testFailureNotFullyClosed2() {
    try {
      new JsonStringWriter().array().write();
      fail();
    } catch (JsonWriterException e) {
      // OK
    }
  }

  @Test
  public void testFailureEmpty() {
    try {
      new JsonStringWriter().write();
      fail();
    } catch (JsonWriterException e) {
      // OK
    }
  }

  @Test
  public void testFailureEmpty2() {
    try {
      new JsonStringWriter().end();
      fail();
    } catch (JsonWriterException e) {
      // OK
    }
  }

  @Test
  public void testFailureMoreThanOneRoot() {
    try {
      new JsonStringWriter().value(1).value(1).write();
      fail();
    } catch (JsonWriterException e) {
      // OK
    }
  }

  @Test
  public void testFailureMoreThanOneRoot2() {
    try {
      new JsonStringWriter().array().value(1).end().value(1).write();
      fail();
    } catch (JsonWriterException e) {
      // OK
    }
  }

  @Test
  public void testFailureMoreThanOneRoot3() {
    try {
      new JsonStringWriter().array().value(1).end().array().value(1).end().write();
      fail();
    } catch (JsonWriterException e) {
      // OK
    }
  }
}
