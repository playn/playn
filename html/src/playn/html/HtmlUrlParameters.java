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

/**
 * Interface providing a central place to document all URL query parameters and values which affect
 * the HTML platform.
 */
public interface HtmlUrlParameters {

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

  /**
   * Enable GL error checking with {@code glerrors=check}.
   */
  public static boolean checkGLErrors = "check".equals(Window.Location.getParameter("glerrors"));

  /**
   * Enable quad-at-a-time shader with {@code glshader=quad}.
   */
  public static boolean quadShader = "quad".equals(Window.Location.getParameter("glshader"));
}
