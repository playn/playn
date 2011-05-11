/**
 * Copyright 2010 The ForPlay Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package forplay.android;

import forplay.core.Log;

class AndroidLog implements Log {

  @Override
  public void info(String msg) {
    android.util.Log.i("forplay", msg);
  }

  @Override
  public void info(String msg, Throwable t) {
    android.util.Log.i("forplay", msg, t);
  }
  
  @Override
  public void debug(String msg) {
    android.util.Log.d("forplay", msg);
  }
  
  @Override
  public void error(String msg) {
    android.util.Log.e("forplay", msg);
  }

  /* (non-Javadoc)
   * @see forplay.core.Log#error(java.lang.String, java.lang.Throwable)
   */
  @Override
  public void error(String msg, Throwable e) {
    android.util.Log.e("forplay", msg, e);
    
  }

  /* (non-Javadoc)
   * @see forplay.core.Log#warn(java.lang.String)
   */
  @Override
  public void warn(String msg) {
   android.util.Log.w("forplay", msg);
    
  }

  /* (non-Javadoc)
   * @see forplay.core.Log#warn(java.lang.String, java.lang.Throwable)
   */
  @Override
  public void warn(String msg, Throwable e) {
    android.util.Log.w("forplay", e);
    
  }

  /* (non-Javadoc)
   * @see forplay.core.Log#debug(java.lang.String, java.lang.Throwable)
   */
  @Override
  public void debug(String msg, Throwable e) {
    android.util.Log.d("forplay", msg,e );
  }
}
