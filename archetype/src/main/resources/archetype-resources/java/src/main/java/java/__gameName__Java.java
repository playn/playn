#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.java;

import playn.core.PlayN;
import playn.java.JavaPlatform;

import ${package}.core.${gameName};

public class ${gameName}Java {

  public static void main(String[] args) {
    JavaPlatform platform = JavaPlatform.register();
    platform.assetManager().setPathPrefix("src/main/java/${packageInPathFormat}/resources");
    PlayN.run(new ${gameName}());
  }
}
