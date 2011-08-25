#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.android;

import playn.android.GameActivity;
import playn.core.PlayN;

import ${package}.core.${gameName};

public class ${gameName}Activity extends GameActivity {

  @Override
  public void main(){
    platform().assetManager().setPathPrefix("${packageInPathFormat}/resources");
    PlayN.run(new ${gameName}());
  }
}
