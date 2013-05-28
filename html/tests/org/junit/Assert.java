/**
 * Copyright 2013 The PlayN Authors
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
package org.junit;

/**
 * GWT still uses JUnit 3, but we use JUnit 4. This bridges the gap for the tests run via GWT.
 */
public class Assert {
  @SuppressWarnings("deprecation")
  public static void assertTrue(boolean expr) {
    junit.framework.Assert.assertTrue(expr);
  }

  @SuppressWarnings("deprecation")
  public static void assertFalse(boolean expr) {
    junit.framework.Assert.assertFalse(expr);
  }

  @SuppressWarnings("deprecation")
  public static void assertEquals(long a, long b) {
    junit.framework.Assert.assertEquals(a, b);
  }

  @SuppressWarnings("deprecation")
  public static void assertEquals(double a,  double b, double epsilon) {
    junit.framework.Assert.assertEquals(a, b, epsilon);
  }

  @SuppressWarnings("deprecation")
  public static void assertEquals(Object a, Object b) {
    junit.framework.Assert.assertEquals(a, b);
  }
}
