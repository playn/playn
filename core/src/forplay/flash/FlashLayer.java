/*
 * Copyright 2011 Google Inc.
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

import flash.display.DisplayObject;
import flash.gwt.FlashImport;

import forplay.core.Transform;
import forplay.core.AbstractLayer;

@FlashImport({"flash.display.Sprite"})
public class FlashLayer extends AbstractLayer {

  
  protected final DisplayObject displayObject;

  /**
   * @param sprite
   */
  public FlashLayer(DisplayObject displayObject) {
    this.displayObject = displayObject;
  }

  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);
    display().setVisible(visible);
  }

  DisplayObject display() {
    return displayObject;
  }

  public void update() {
    updateDisplay();
    updateChildren();
  }

  /**
   * 
   */
  protected void updateChildren() {
    // TODO Auto-generated method stub
    
  }

   /**
   * 
   */
  private void updateDisplay() {
//    display().setX((int) originX);
//    display().setY((int) originY);
    Transform x = new Transform(Transform.IDENTITY);
    x.setTranslation(originX, originY);
    x.transform(transform.m00(), transform.m01(), transform.m10(), 
        transform.m11(), transform.tx() - originX, transform.ty() - originY);
    x.translate(-originX, -originY);
    display().setTransform(x.m00(), x.m01(), x.m10(),
        x.m11(), x.tx(), x.ty());
  }
  
  
}
