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

import forplay.core.Log;

class HtmlLog implements Log {

  public HtmlLog() {
  }

  @Override
  public void error(String msg) {
    com.allen_sauer.gwt.log.client.Log.error(msg);
  }

  @Override
  public void error(String msg, Throwable e) {
    com.allen_sauer.gwt.log.client.Log.error(msg, e);
  }
  
  @Override
  public void debug(String msg) {
    com.allen_sauer.gwt.log.client.Log.debug(msg);
  }
  
  @Override
  public void debug(String msg, Throwable e) {
    com.allen_sauer.gwt.log.client.Log.debug(msg, e);
  }

  @Override
  public void info(String msg) {
    com.allen_sauer.gwt.log.client.Log.info(msg);
  }

  public void info(String msg, Throwable e) {
    com.allen_sauer.gwt.log.client.Log.info(msg, e);
  }

  @Override
  public void warn(String msg) {
    com.allen_sauer.gwt.log.client.Log.warn(msg);
  }

  @Override
  public void warn(String msg, Throwable e) {
    com.allen_sauer.gwt.log.client.Log.warn(msg, e);
  }
}
