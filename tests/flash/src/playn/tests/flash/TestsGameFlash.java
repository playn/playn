/**
 * Copyright 2010 The PlayN Authors
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
package playn.tests.flash;

import playn.core.PlayN;
import playn.flash.FlashGame;
import playn.flash.FlashPlatform;

import playn.tests.core.TestsGame;

public class TestsGameFlash extends FlashGame {

  @Override
  public void start() {
    FlashPlatform platform = FlashPlatform.register();
    platform.assetManager().setPathPrefix("testsgame/");
    PlayN.run(new TestsGame());
  }
}
