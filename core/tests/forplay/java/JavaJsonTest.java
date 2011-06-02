/**
 * Copyright 2010 The ForPlay Authors
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
package forplay.java;

import static forplay.core.ForPlay.*;
import static org.junit.Assert.*;

import org.junit.Test;

import forplay.core.Json;
import forplay.tests.AbstractForPlayTest;

public class JavaJsonTest extends AbstractForPlayTest {

  /**
   * Ensures that getArray and getObject return null when pulling missing keys.
   */
  @Test
  public void testMissingKey() {
    Json.Object o = json().parse("{\"array\":[]}");
    assertNull(o.getArray("notthere"));
    assertNull(o.getObject("notthere"));

    Json.Array a = o.getArray("array");
    assertNull(a.getArray(0));
    assertNull(a.getArray(1));
  }
}
