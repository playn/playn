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
package forplay.test.manualtests.java;

import forplay.core.ForPlay;
import forplay.java.JavaAssetManager;
import forplay.java.JavaPlatform;
import forplay.test.manualtests.core.ManualTestsGame;

public class ManualTestsGameJava {

  public static void main(String[] args) {
    JavaPlatform platform = JavaPlatform.register();
    JavaAssetManager assets = platform.assetManager();
    assets.setPathPrefix("src/forplay/test/manualtests/resources");
    ForPlay.run(new ManualTestsGame());
    platform.setTitle("Manual Tests");
  }
}
