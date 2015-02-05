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

import org.robovm.apple.foundation.Foundation;

import playn.core.Log;

public class RoboLog extends Log {

  private final StringWriter strOut = new StringWriter();
  private final PrintWriter logOut = new PrintWriter(strOut);

  @Override protected void logImpl(Level level, String msg, Throwable e) {
    Foundation.log(level + ": " + msg);
    if (e != null) {
      e.printStackTrace(logOut);
      StringBuffer buf = strOut.getBuffer();
      for (String line : buf.toString().split("\n")) Foundation.log(line);
      buf.setLength(0);
    }
  }
}
