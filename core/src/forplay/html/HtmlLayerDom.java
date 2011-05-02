/**
 * Copyright 2010 The ForPlay Authors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package forplay.html;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Visibility;

import forplay.core.AbstractLayer;

class HtmlLayerDom extends AbstractLayer {

  private static final float EPSILON = 0.001f;

  private static final String[] PREFIXES = new String[] {
    "Moz", "webkit", "ms", "O"
  };

  private static String transformName, transformOriginName;
  private static String translateSuffix;

  static {
    int i = 0;
    for (; i < PREFIXES.length; ++i) {
      String prefix = PREFIXES[i];
      if (hasStyle(prefix + "Transform")) {
        transformName = prefix + "Transform";
        transformOriginName = prefix + "TransformOrigin";
        break;
      }
    }

    // Fallback: Assume 'transform' will work.
    if (i == PREFIXES.length) {
      transformName = "transform";
      transformOriginName = "transformOrigin";
    }

    // Hack: FF4 appears to require a 'px' suffix on the translation components of the matrix.
    translateSuffix = ("MozTransform".equals(transformName)) ? "px" : "";
  }

  private static native boolean hasStyle(String name) /*-{
    return (document.body.style[name] !== undefined);
  }-*/;

  private final Element elem;
  private boolean shown;

  HtmlLayerDom(Element elem) {
    this.elem = elem;
    elem.getStyle().setPosition(Position.ABSOLUTE);
    elem.getStyle().setVisibility(Visibility.HIDDEN);
    elem.getStyle().setProperty(transformOriginName, "0 0");
  }

  public void setOrigin(float x, float y) {
    super.setOrigin(x, y);

    String origin = css(x) + "px " + css(y) + "px";
    elem.getStyle().setProperty(transformOriginName, origin);
  }

  Element element() {
    return elem;
  }

  void update() {
    if (!shown) {
      shown = true;
      elem.getStyle().setVisibility(Visibility.VISIBLE);
    }

    float m00 = transform.m00();
    float m01 = transform.m01();
    float m10 = transform.m10();
    float m11 = transform.m11();
    float m20 = transform.tx() - originX;
    float m21 = transform.ty() - originY;

    String matrix = "matrix3d(" +
      css(m00) + "," + css(m01) + ",0,0," +
      css(m10) + "," + css(m11) + ",0,0," +
      "0,0,1,0," +
      xlate(m20) + "," + xlate(m21) + ",0,1" +
    ")";

    elem.getStyle().setProperty(transformName, matrix);
  }

  private String xlate(float x) {
    return css(x) + translateSuffix;
  }

  private String css(float x) {
    // This is necessary because CSS transforms don't accept the floating point form 1.00e1,
    // which is what you naturally get from very small (or large) floating point values.
    return (Math.abs(x) < EPSILON) ? "0" : Float.toString(x);
  }
}
