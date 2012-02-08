#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.html;

import playn.core.PlayN;
import playn.html.HtmlGame;
import playn.html.HtmlPlatform;

import ${package}.core.${JavaGameClassName};

public class ${JavaGameClassName}Html extends HtmlGame {

  @Override
  public void start() {
    HtmlPlatform platform = HtmlPlatform.register();
    platform.assets().setPathPrefix("${rootArtifactId}/");
    PlayN.run(new ${JavaGameClassName}());
  }
}
