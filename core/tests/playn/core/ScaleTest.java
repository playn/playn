/**
 * Copyright 2010-2015 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playn.core;

import java.util.List;
import org.junit.Test;
import pythagoras.f.MathUtil;
import static org.junit.Assert.*;

public class ScaleTest {

  private void assertScalesMatch (List<Scale.ScaledResource> rsrcs, float... scales) {
    int ii = 0;
    assertEquals("Scale count does not match resource count", scales.length, rsrcs.size());
    for (Scale.ScaledResource rsrc : rsrcs) {
      assertEquals(rsrc.scale.factor, scales[ii++], MathUtil.EPSILON);
    }
  }

  @Test
  public void testScaledResources () {
    assertScalesMatch(new Scale(2).getScaledResources("test.png"), 2, 1);
    assertScalesMatch(new Scale(4).getScaledResources("test.png"), 4, 3, 2, 1);
    assertScalesMatch(new Scale(2.5f).getScaledResources("test.png"), 2.5f, 3, 2, 1);
    assertScalesMatch(new Scale(1.5f).getScaledResources("test.png"), 1.5f, 2, 1);
  }
}
