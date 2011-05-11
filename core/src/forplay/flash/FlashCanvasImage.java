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

import forplay.core.Canvas;
import forplay.core.CanvasImage;
import forplay.core.Image;
import forplay.core.ResourceCallback;

/**
 *
 */
public class FlashCanvasImage implements CanvasImage {

  public FlashCanvasImage(FlashCanvas surface) {
  }

  @Override
  public Canvas canvas() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public int height() {
    return 0;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void replaceWith(Image image) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public int width() {
    return 0;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public boolean isReady() {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  /* (non-Javadoc)
   * @see forplay.core.Image#addCallback(forplay.core.ResourceCallback)
   */
  @Override
  public void addCallback(ResourceCallback<Image> callback) {
    // TODO Auto-generated method stub
    
  }
}
