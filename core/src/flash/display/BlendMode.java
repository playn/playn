// Copyright 2011 Google Inc. All Rights Reserved.

package flash.display;

/**
 * @author cromwellian@google.com (Your Name Here)
 */
public enum BlendMode {
  ADD, ALPHA, DARKEN, DIFFERENCE, ERASE, HARDLIGHT, INVERT, LAYER, 
  LIGHTEN, MULTIPLY, NORMAL, OVERLAY, SCREEN, SUBTRACT;
  ;

  public String nativeEnum() {
    return name().toLowerCase();
  }
  
  public static BlendMode valueOfNative(String nativeEnum) {
    return valueOf(nativeEnum.toUpperCase());
  }
}
