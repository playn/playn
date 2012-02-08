#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.android;

import playn.android.GameActivity;
import playn.core.PlayN;

import ${package}.core.${JavaGameClassName};

public class ${JavaGameClassName}Activity extends GameActivity {

  @Override
  public void main(){
    platform().assets().setPathPrefix("${packageInPathFormat}/resources");
    PlayN.run(new ${JavaGameClassName}());
  }
}
