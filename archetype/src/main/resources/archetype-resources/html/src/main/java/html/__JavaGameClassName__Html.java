#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.html;

import com.google.gwt.core.client.EntryPoint;
import playn.html.HtmlPlatform;
import ${package}.core.${JavaGameClassName};

public class ${JavaGameClassName}Html implements EntryPoint {

  @Override public void onModuleLoad () {
    HtmlPlatform.Config config = new HtmlPlatform.Config();
    // use config to customize the HTML platform, if needed
    HtmlPlatform plat = new HtmlPlatform(config);
    plat.assets().setPathPrefix("${rootArtifactId}/");
    new ${JavaGameClassName}(plat);
    plat.start();
  }
}
