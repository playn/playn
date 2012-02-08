#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.java;

import playn.core.PlayN;
import playn.java.JavaPlatform;

import ${package}.core.${JavaGameClassName};

public class ${JavaGameClassName}Java {

  public static void main(String[] args) {
    JavaPlatform platform = JavaPlatform.register();
    platform.assets().setPathPrefix("${packageInPathFormat}/resources");
    PlayN.run(new ${JavaGameClassName}());
  }
}
