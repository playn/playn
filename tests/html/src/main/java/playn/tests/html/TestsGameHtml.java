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
package playn.tests.html;

import com.google.gwt.user.client.Window;

import playn.core.PlayN;
import playn.html.HtmlGame;
import playn.html.HtmlPlatform;

import playn.tests.core.TestsGame;

public class TestsGameHtml extends HtmlGame {

  @Override
  public void start() {
    HtmlPlatform.Configuration config = new HtmlPlatform.Configuration();
    try {
      config.scaleFactor = Float.parseFloat(Window.Location.getParameter("scale"));
    } catch (Exception e) {} // oh well
    HtmlPlatform platform = HtmlPlatform.register(config);
    platform.setTitle("Tests");
    platform.assets().setPathPrefix("testsgame/");
    HtmlPlatform.disableRightClickContextMenu();
    PlayN.run(new TestsGame());
  }
}
