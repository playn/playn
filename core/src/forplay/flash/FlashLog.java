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
package forplay.flash;

import forplay.core.Log;

class FlashLog implements Log {

  public FlashLog() {
  }

  @Override
  public void error(String msg) {
    log(msg, null);
  }

  @Override
  public void error(String msg, Throwable e) {
    log(msg, e);
  }

  @Override
  public void info(String msg) {
    log(msg, null);
  }

  @Override
  public void info(String msg, Throwable e) {
    log(msg, e);
  }

  @Override
  public void warn(String msg) {
    log(msg, null);
  }

  @Override
  public void warn(String msg, Throwable e) {
    log(msg, e);
  }

  @Override
  public void debug(String msg) {
    log(msg, null);
  }

  @Override
  public void debug(String msg, Throwable e) {
    log(msg, e);
  }

  private native void log(String msg, Throwable e) /*-{
    var logMsg = msg + (e == null ? "" : (" " + e));
    flash.external.ExternalInterface.call("window.console.log", logMsg);
  }-*/;
}
