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
package playn.html;

import playn.core.Json;
import playn.shared.json.JsonImplArrayTest;

import com.google.gwt.junit.client.GWTTestCase;

public class HtmlJsonArrayTest extends GWTTestCase {
  private class BaseArrayTest extends JsonImplArrayTest {
    @Override
    protected Json json() {
      return new HtmlJson();
    }
  }

  public void testArrayAdd() {
    new BaseArrayTest().testArrayAdd();
  }

  public void testArraySet() {
    new BaseArrayTest().testArraySet();
  }

  public void testArrayAppend() {
    new BaseArrayTest().testArrayAppend();
  }

  public void testArrayBasics() {
    new BaseArrayTest().testArrayBasics();
  }

  public void testArrayTypeChecks() {
    new BaseArrayTest().testArrayTypeChecks();
  }

  public void testIsArray() {
    new BaseArrayTest().testIsArray();
  }
  
  public void testTypedArray() {
    new BaseArrayTest().testTypedArray();
  }

  @Override
  public String getModuleName() {
    return "playn.html.HtmlTests";
  }
}
