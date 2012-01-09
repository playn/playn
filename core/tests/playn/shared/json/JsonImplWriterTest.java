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
 * Test the public-facing {@link Json} writer API. The underlying writer is exercised in other
 * tests.
 *
 * Tests in this class are re-used to test HTML mode.
 */
public class JsonImplWriterTest extends AbstractJsonTest {
  @Test
  public void testWriter() {
    String s = json().newWriter().array()
        .value(true)
        .value(1)
        .nul()
        .value(json().createArray())
        .array()
          .value(1)
          .value(2)
          .value(json().createArray())
        .end()
        .value(json().createObject())
        .object()
          .value("a", "b")
          .value("c", "d")
          .value("e", json().createObject())
        .end()
        .value("hi")
    .end().write();

    assertEquals("[true,1,null,[],[1,2,[]],{},{\"a\":\"b\",\"c\":\"d\",\"e\":{}},\"hi\"]", s);
  }
}
