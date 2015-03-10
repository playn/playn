#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.java;

import playn.java.LWJGLPlatform;

import ${package}.core.${JavaGameClassName};

public class ${JavaGameClassName}Java {

  public static void main (String[] args) {
    LWJGLPlatform.Config config = new LWJGLPlatform.Config();
    // use config to customize the Java platform, if needed
    LWJGLPlatform plat = new LWJGLPlatform(config);
    new ${JavaGameClassName}(plat);
    plat.start();
  }
}
