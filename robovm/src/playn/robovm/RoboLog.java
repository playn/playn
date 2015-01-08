/**
 * Copyright 2014 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playn.robovm;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import org.robovm.apple.foundation.Foundation;

import playn.core.Log;

public class RoboLog extends Log {

  // printStackTrace only uses println(), so this hackery routes that to Foundation.log
  private final PrintWriter logOut = new PrintWriter(new StringWriter()) {
    @Override public void println (String text) {
      Foundation.log(text);
    }
  };

  @Override protected void logImpl(Level level, String msg, Throwable e) {
    Foundation.log(level + ": " + msg);
    if (e != null) e.printStackTrace(logOut);
  }
}
