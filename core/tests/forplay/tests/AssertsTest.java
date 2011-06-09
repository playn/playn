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
package forplay.tests;

import org.junit.Test;

import forplay.core.Asserts;

/**
 * Tests that {@link Asserts} behave as expected.
 */
public class AssertsTest
{
  @Test public void testPassingAsserts() {
    Asserts.check(true);
    Asserts.check(true, "unused");
    Asserts.check(true, "unused: %s", 0);

    Asserts.checkArgument(true);
    Asserts.checkArgument(true, "unused");
    Asserts.checkArgument(true, "unused: %s", 0);

    Asserts.checkState(true);
    Asserts.checkState(true, "unused");
    Asserts.checkState(true, "unused: %s", 0);

    Asserts.checkNotNull(this);
    Asserts.checkNotNull(this, "unused");
    Asserts.checkNotNull(this, "unused: %s", 0);

    Asserts.checkElementIndex(0, 10);
    Asserts.checkElementIndex(0, 10, "unused");
    Asserts.checkElementIndex(5, 10);
    Asserts.checkElementIndex(5, 10, "unused");
    Asserts.checkElementIndex(9, 10);
    Asserts.checkElementIndex(9, 10, "unused");

    Asserts.checkPositionIndex(0, 10);
    Asserts.checkPositionIndex(0, 10, "unused");
    Asserts.checkPositionIndex(5, 10);
    Asserts.checkPositionIndex(5, 10, "unused");
    Asserts.checkPositionIndex(10, 10);
    Asserts.checkPositionIndex(10, 10, "unused");

    Asserts.checkPositionIndexes(0, 1, 10);
    Asserts.checkPositionIndexes(4, 5, 10);
    Asserts.checkPositionIndexes(9, 10, 10);
  }

  // alas, the failing checks each have to be in separate methods

  @Test(expected=AssertionError.class) public void testFailingCheck1() {
    Asserts.check(false);
  }
  @Test(expected=AssertionError.class) public void testFailingCheck2() {
    Asserts.check(false, "unused");
  }
  @Test(expected=AssertionError.class) public void testFailingCheck3() {
    Asserts.check(false, "unused: %s", 0);
  }

  @Test(expected=IllegalArgumentException.class) public void testFailingCheckArgument1() {
    Asserts.checkArgument(false);
  }
  @Test(expected=IllegalArgumentException.class) public void testFailingCheckArgument2() {
    Asserts.checkArgument(false, "unused");
  }
  @Test(expected=IllegalArgumentException.class) public void testFailingCheckArgument3() {
    Asserts.checkArgument(false, "unused: %s", 0);
  }

  @Test(expected=IllegalStateException.class) public void testFailingCheckState1() {
    Asserts.checkState(false);
  }
  @Test(expected=IllegalStateException.class) public void testFailingCheckState2() {
    Asserts.checkState(false, "unused");
  }
  @Test(expected=IllegalStateException.class) public void testFailingCheckState3() {
    Asserts.checkState(false, "unused: %s", 0);
  }

  @Test(expected=NullPointerException.class) public void testFailingCheckNotNull1() {
    Asserts.checkNotNull(null);
  }
  @Test(expected=NullPointerException.class) public void testFailingCheckNotNull2() {
    Asserts.checkNotNull(null, "unused");
  }
  @Test(expected=NullPointerException.class) public void testFailingCheckNotNull3() {
    Asserts.checkNotNull(null, "unused: %s", 0);
  }

  @Test(expected=IndexOutOfBoundsException.class) public void testFailingCheckElIdx11() {
    Asserts.checkElementIndex(-1, 10);
  }
  @Test(expected=IndexOutOfBoundsException.class) public void testFailingCheckElIdx21() {
    Asserts.checkElementIndex(-1, 10, "unused");
  }
  @Test(expected=IndexOutOfBoundsException.class) public void testFailingCheckElIdx12() {
    Asserts.checkElementIndex(10, 10);
  }
  @Test(expected=IndexOutOfBoundsException.class) public void testFailingCheckElIdx22() {
    Asserts.checkElementIndex(10, 10, "unused");
  }

  @Test(expected=IndexOutOfBoundsException.class) public void testFailingCheckPosIdx11() {
    Asserts.checkPositionIndex(-1, 10);
  }
  @Test(expected=IndexOutOfBoundsException.class) public void testFailingCheckPosIdx21() {
    Asserts.checkPositionIndex(-1, 10, "unused");
  }
  @Test(expected=IndexOutOfBoundsException.class) public void testFailingCheckPosIdx12() {
    Asserts.checkPositionIndex(11, 10);
  }
  @Test(expected=IndexOutOfBoundsException.class) public void testFailingCheckPosIdx22() {
    Asserts.checkPositionIndex(11, 10, "unused");
  }

  @Test(expected=IndexOutOfBoundsException.class) public void testFailingCheckPosIdxs1() {
    Asserts.checkPositionIndexes(-1, 1, 10);
  }
  @Test(expected=IndexOutOfBoundsException.class) public void testFailingCheckPosIdxs2() {
    Asserts.checkPositionIndexes(0, 11, 10);
  }
  @Test(expected=IndexOutOfBoundsException.class) public void testFailingCheckPosIdxs3() {
    Asserts.checkPositionIndexes(-1, 11, 10);
  }
  @Test(expected=IndexOutOfBoundsException.class) public void testFailingCheckPosIdxs4() {
    Asserts.checkPositionIndexes(3, 2, 10);
  }
}
