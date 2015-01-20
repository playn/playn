#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.java;

import playn.java.JavaPlatform;

import ${package}.core.${JavaGameClassName};

public class ${JavaGameClassName}Java {

  public static void main (String[] args) {
    JavaPlatform.Config config = new JavaPlatform.Config();
    // use config to customize the Java platform, if needed
    JavaPlatform plat = new JavaPlatform(config);
    new ${JavaGameClassName}(plat);
    plat.start();
  }
}
