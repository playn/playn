#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.android;

import playn.android.GameActivity;

import ${package}.core.${JavaGameClassName};

public class ${JavaGameClassName}Activity extends GameActivity {

  @Override public void main () {
    new ${JavaGameClassName}(platform());
  }
}
