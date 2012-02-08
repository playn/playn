#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.flash;

import playn.core.PlayN;
import playn.flash.FlashGame;
import playn.flash.FlashPlatform;

import ${package}.core.${JavaGameClassName};

public class ${JavaGameClassName}Flash extends FlashGame {

  @Override
  public void start() {
    FlashPlatform platform = FlashPlatform.register();
    platform.assets().setPathPrefix("${rootArtifactId}flash/");
    PlayN.run(new ${JavaGameClassName}());
  }
}
