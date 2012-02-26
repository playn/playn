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

package playn.flash;

import flash.display.DisplayObject;
import flash.gwt.FlashImport;

import playn.core.AbstractLayer;
import playn.core.InternalTransform;
import playn.core.StockInternalTransform;

@FlashImport({"flash.display.Sprite"})
public class FlashLayer extends AbstractLayer {

  protected final DisplayObject displayObject;

  public FlashLayer(DisplayObject displayObject) {
    this.displayObject = displayObject;
//    ((InteractiveObject) displayObject).setMouseEnabled(false);
  }

  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);
    display().setVisible(visible);
  }

  @Override
  public void setAlpha(float alpha) {
    super.setAlpha(alpha);
    display().setAlpha(alpha);
  }

  DisplayObject display() {
    return displayObject;
  }

  public void update() {
    updateDisplay();
    updateChildren();
  }

  protected void updateChildren() {
    // TODO Auto-generated method stub
  }

  private void updateDisplay() {
//    display().setX((int) originX);
//    display().setY((int) originY);
    InternalTransform x = new StockInternalTransform();
    x.concatenate(transform, originX, originY);
    display().setTransform(x.m00(), x.m01(), x.m10(), x.m11(), x.tx(), x.ty());
  }
}
