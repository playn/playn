/**
 * Copyright 2011 The PlayN Authors
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
package playn.logging.enhanced;

import com.allen_sauer.gwt.log.client.Log;

import playn.html.HtmlLog;

/**
 * Provides enhanced logging capabilities, which can be enabled via your
 * applications' {@literal *.gwt.xml} module file.
 */
class HtmlLogEnhanced extends HtmlLog {

  // Instantiated via GWT.create()
  private HtmlLogEnhanced() {
    // Replaces GWT's uncaught exception handler and installs page onerror handler
    Log.setUncaughtExceptionHandler();
  }

  @Override
  protected void logImpl(Level level, String msg, Throwable e) {
    switch (level) {
    case DEBUG: Log.debug(msg, e); break;
    default:    Log.info(msg, e); break;
    case  WARN: Log.warn(msg, e); break;
    case ERROR: Log.error(msg, e); break;
    }
  }
}
