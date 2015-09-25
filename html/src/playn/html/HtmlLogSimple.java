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
package playn.html;

/**
 * Trivially log all output to {@link System#out}. Note, critical messages are
 * not separated out to {@link System#err}, since that causes the two output
 * streams to become mixed and log messages unreadable. Consider enabling
 * PlayN's enhanced logging capabilities if you need additional features
 * beyond what is provided here.
 */
class HtmlLogSimple extends HtmlLog {

  // Instantiated via GWT.create()
  private HtmlLogSimple() {
  }

  @Override protected void logImpl(Level level, String msg, Throwable e) {
    String lmsg = level + ": " + msg;
    if (e != null) lmsg += ": " + e.getMessage();
    // keep console output intact by using System.out for both
    System.out.println(lmsg);
    if (e != null) {
      e.printStackTrace(System.out);
    }
    // also send it to the browser's console
    sendToBrowserConsole(lmsg, e);
  }

  private native void sendToBrowserConsole(String msg, Throwable e) /*-{
    if ($wnd.console && $wnd.console.info) {
      if (e != null) {
        $wnd.console.info(msg, e);
      } else {
        $wnd.console.info(msg);
      }
    }
  }-*/;
}
