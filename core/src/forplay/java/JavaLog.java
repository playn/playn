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
package forplay.java;

import forplay.core.Log;

class JavaLog implements Log {

  @Override
  public void error(String msg) {
    error(msg, null);
  }

  @Override
  public void error(String msg, Throwable e) {
    System.err.println(msg);
    if (e != null) {
      e.printStackTrace(System.err);
    }
  }

  @Override
  public void debug(String msg) {
    info(msg);
  }

  @Override
  public void debug(String msg, Throwable e) {
    info(msg, e);
  }

  @Override
  public void info(String msg) {
    info(msg, null);
  }

  @Override
  public void info(String msg, Throwable e) {
    System.out.println(msg);
    if (e != null) {
      e.printStackTrace(System.out);
    }
  }

  @Override
  public void warn(String msg) {
    error(msg);
  }

  @Override
  public void warn(String msg, Throwable e) {
    error(msg, e);
  }
}
