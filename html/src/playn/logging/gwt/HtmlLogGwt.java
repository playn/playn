/**
 * Copyright 2014 The PlayN Authors
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
package playn.logging.gwt;

import java.util.logging.Logger;

import playn.html.HtmlLog;

/**
 * Logs output to GWT's emulation of the java.util.logging.Logger.
 */
class HtmlLogGwt extends HtmlLog {

  private final Logger logger = Logger.getLogger("PlayN");

  // Instantiated via GWT.create()
  private HtmlLogGwt() {
  }

  @Override
  protected void logImpl(Level level, String msg, Throwable e) {
    switch (level) {
      case DEBUG: logger.log(java.util.logging.Level.FINE, msg, e); break;
      case WARN:  logger.log(java.util.logging.Level.WARNING, msg, e); break;
      case ERROR: logger.log(java.util.logging.Level.SEVERE, msg, e); break;
      default:    logger.log(java.util.logging.Level.INFO, msg, e); break;
    }
  }
}
