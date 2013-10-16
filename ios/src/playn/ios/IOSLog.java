/**
 * Copyright 2012 The PlayN Authors
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
package playn.ios;

import java.io.PrintWriter;
import java.io.StringWriter;

import cli.System.Console;

import playn.core.LogImpl;

public class IOSLog extends LogImpl {

  @Override
  protected void logImpl(Level level, String msg, Throwable e) {
    Console.WriteLine(level + ": " + msg);

    if (e != null) {
      try {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        Console.WriteLine(sw.toString());
      } catch (Throwable t) {
        Console.WriteLine(e);
        Console.WriteLine("<stack trace generation failed: " + t + ">");
      }
    }
  }
}
