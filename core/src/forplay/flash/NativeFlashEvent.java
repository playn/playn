/*
 * Copyright 2010 Google Inc.
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

import com.google.gwt.core.client.JavaScriptObject;

/**
 *
 */
final public class NativeFlashEvent extends JavaScriptObject {

  protected NativeFlashEvent() {}
  public void preventDefault() {
    //To change body of created methods use File | Settings | File Templates.
  }

  public int getClientX() {
    return 0;
  }

  public int getClientY() {
    return 0;
  }

  public int getKeyCode() {
    return 0;
  }
}
