#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
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
package ${package}.html;

import playn.html.HtmlAssetManager;

import playn.core.PlayN;
import playn.html.HtmlGame;
import playn.html.HtmlPlatform;

import ${package}.core.${ProjectName}Game;

public class ${ProjectName}GameHtml extends HtmlGame {

  @Override
  public void start() {
    HtmlAssetManager assets = HtmlPlatform.register().assetManager();
    assets.setPathPrefix("${ProjectName}/");
    PlayN.run(new ${ProjectName}Game());
  }
}
