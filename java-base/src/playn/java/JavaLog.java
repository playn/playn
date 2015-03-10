/**
 * Copyright 2010 The PlayN Authors
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
package playn.java;

import playn.core.Log;

class JavaLog extends Log {

  @Override protected void logImpl (Level level, String msg, Throwable e) {
    switch (level) {
    default:
      System.out.println(msg);
      if (e != null) e.printStackTrace(System.out);
      break;
    case WARN:
    case ERROR:
      System.err.println(msg);
      if (e != null) e.printStackTrace(System.err);
      break;
    }
  }
}
