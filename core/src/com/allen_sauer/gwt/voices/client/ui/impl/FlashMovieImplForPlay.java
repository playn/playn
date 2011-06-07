/*
 * Copyright 2011 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.allen_sauer.gwt.voices.client.ui.impl;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;

import forplay.core.ForPlay;

/**
 * This class allows us to replacing the normal compile time deferred binding implementation
 * decision, with a runtime decision, based on the runtime user agent. Doing so, allows us to use
 * {@literal safari} as a single {@literal user.agent} permutation, which speeds up compile time,
 * but still provides Flash detection in IE.
 */
public class FlashMovieImplForPlay extends FlashMovieImpl {

  private FlashMovieImpl impl;

  public FlashMovieImplForPlay() {
    if (Window.Navigator.getUserAgent().indexOf("MSIE") != -1) {
      impl = new FlashMovieImplIE6();
    } else {
      impl = new FlashMovieImplSafari();
    }
    ForPlay.log().debug("CHOSE: " + impl.getClass().getName());
  }

  public Element createElementMaybeSetURL(String id, String url) {
    return impl.createElementMaybeSetURL(id, url);
  }

  public int getMajorVersion() {
    return impl.getMajorVersion();
  }

  public String getVersionString() {
    return impl.getVersionString();
  }

  protected String getRawVersionString() {
    return impl.getRawVersionString();
  }
}
