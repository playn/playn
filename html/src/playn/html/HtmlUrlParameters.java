/**
 * Copyright 2011 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package playn.html;

import com.google.gwt.user.client.Window;

import playn.core.PlayN;
import playn.html.HtmlPlatform.Mode;

/**
 * Interface providing a central place to document all URL query parameters and values which affect
 * the HTML platform.
 */
public interface HtmlUrlParameters {

  public static class Analytics {
    static final String PARAM_NAME = "analytics";

    public static float getRandom() {
      float random = PlayN.random();
      String val = Window.Location.getParameter(PARAM_NAME);
      if (val != null) {
        try {
          random = Float.parseFloat(val);
        } catch (Exception ignore) {
        }
      }
      return random;
    }
  }

  /**
   * This interface serves solely as documentation for the URL parameter implemented by gwt-log.
   */
  public static interface Log {
    static final String DEBUG = "DEBUG";
    static final String ERROR = "ERROR";
    static final String FATAL = "FATAL";
    static final String INFO = "INFO";
    static final String PARAM_NAME = "log_level";
    static final String TRACE = "TRACE";
    static final String WARN = "WARN";
  }

  public static class Renderer {
    static final String CANVAS = "canvas";
    static final String DOM = "dom";
    static final String GL = "gl";
    static final String PARAM_NAME = "renderer";

    static Mode requestedMode() {
      String renderer = Window.Location.getParameter(PARAM_NAME);
      if (CANVAS.equals(renderer)) {
        return Mode.CANVAS;
      } else if (GL.equals(renderer)) {
        return Mode.WEBGL;
      } else if (DOM.equals(renderer)) {
        return Mode.DOM;
      }
      return Mode.AUTODETECT;
    }
  }

  /**
   * This interface serves solely as documentation for the URL parameter implemented by gwt-voices.
   */
  public static interface Sound {
    static final String FLASH = "flash";
    static final String HTML5 = "html5";
    static final String NATIVE = "native";
    static final String WEBAUDIO = "webaudio";
    static final String PARAM_NAME = "gwt-voices";
  }
}
