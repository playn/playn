/**
 * Copyright 2012 The PlayN Authors
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
package playn.android;

import org.junit.Test;
import static org.junit.Assert.*;

import static playn.android.AndroidTextLayout.accountForLigatures;

public class AndroidTextLayoutTest {
  @Test public void testLigatureHackWorkaround () {
    String[] ligatures = { "fi", "ae" };

    String one = "My dog's name is fifi.";
    String two = "fififififi";
    String three = "I enjoy breathing the aether.";
    String four = "eaeaeaeae";

    // android will return two characters less in our length, so we simulate that here
    assertEquals(one, 2, accountForLigatures(one, 0, one.length()-2, ligatures));
    assertEquals(two, 5, accountForLigatures(two, 0, two.length()-5, ligatures));
    assertEquals(three, 1, accountForLigatures(three, 0, three.length()-1, ligatures));
    assertEquals(four, 4, accountForLigatures(four, 0, four.length()-4, ligatures));

    // now check that things work properly if we wrap before some or all of the ligatures
    assertEquals(one.substring(0, 15), 0, accountForLigatures(one, 0, 15, ligatures));
    assertEquals(one.substring(0, 19), 1, accountForLigatures(one, 0, 19-1, ligatures));
    assertEquals(two.substring(0, 4), 2, accountForLigatures(two, 0, 4-2, ligatures));
    assertEquals(two.substring(0, 6), 3, accountForLigatures(two, 0, 6-3, ligatures));
    assertEquals(two.substring(0, 8), 4, accountForLigatures(two, 0, 8-4, ligatures));
    assertEquals(four.substring(0, 1), 0, accountForLigatures(four, 0, 1, ligatures));
    assertEquals(four.substring(0, 3), 1, accountForLigatures(four, 0, 3-1, ligatures));

    // check that we work with offsets
    assertEquals(one.substring(10), 2, accountForLigatures(one, 10, one.length()-10-2, ligatures));
    assertEquals(two.substring(4), 3, accountForLigatures(two, 4, two.length()-4-3, ligatures));
    assertEquals(three.substring(10), 1, accountForLigatures(three, 10, three.length()-10-1,
                                                             ligatures));
    assertEquals(four.substring(1), 4, accountForLigatures(four, 1, four.length()-1-4, ligatures));
    assertEquals(four.substring(5), 2, accountForLigatures(four, 5, four.length()-5-2, ligatures));

    // we don't test wrapping that cuts a ligature in half, because I assume Paint.breakText won't
    // do that, but I may later discover to my chagrin that this assumption is invalid
  }
}
