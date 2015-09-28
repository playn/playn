/**
 * Copyright 2010-2015 The PlayN Authors
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
package playn.android;

import android.app.Activity;
import android.os.AsyncTask;

import playn.core.Exec;
import playn.core.Log;
import playn.core.Platform;

public class AndroidExec extends Exec.Default {

  private final Activity activity;

  public AndroidExec (Platform plat, Activity activity) {
    super(plat);
    this.activity = activity;
  }

  protected boolean isPaused () { return false; }

  @Override public void invokeLater(Runnable action) {
    // if we're paused, we need to run these on the main app thread instead of queueing them up for
    // processing on the run queue, because the run queue isn't processed while we're paused; the
    // main thread will ensure they're run serially, but also that they don't linger until the next
    // time the app is resumed (if that happens at all)
    if (isPaused()) activity.runOnUiThread(action);
    else super.invokeLater(action);
  }

  @Override public boolean isAsyncSupported () { return true; }

  @Override public void invokeAsync(final Runnable action) {
    activity.runOnUiThread(new Runnable() {
      public void run () {
        new AsyncTask<Void,Void,Void>() {
          @Override public Void doInBackground(Void... params) {
            try {
              action.run();
            } catch (Throwable t) {
              plat.reportError("Async task failure [task=" + action + "]", t);
            }
            return null;
          }
        }.execute();
      }
    });
  }
}
