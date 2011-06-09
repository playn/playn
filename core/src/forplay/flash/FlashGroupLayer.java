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

import flash.display.DisplayObject;
import flash.display.DisplayObjectContainer;
import flash.display.Sprite;

import java.util.ArrayList;
import java.util.List;

import forplay.core.Asserts;
import forplay.core.GroupLayer;
import forplay.core.Layer;

/**
 *
 */
public class FlashGroupLayer extends FlashLayer implements GroupLayer {
  
  FlashGroupLayer(DisplayObjectContainer container) {
    super(container);
  }
  
  /**
   * @param rootLayer
   */
  FlashGroupLayer() {
    super(Sprite.create());
  }

  static FlashGroupLayer getRoot() {
    return new FlashGroupLayer(Sprite.getRootSprite());
  }
  
  List<Layer> layers = new ArrayList<Layer>();
  /* (non-Javadoc)
   * @see forplay.core.GroupLayer#add(forplay.core.Layer)
   */
  @Override
  public void add(Layer layer) {
    layers.add(layer);
    ((FlashLayer) layer).update();
    container().addChild(display(layer));
  }

  private DisplayObjectContainer container() {
    return display().cast();
  }

  /**
   * @param layer
   */
  private FlashLayer flash(Layer layer) {
    Asserts.checkArgument(layer instanceof FlashLayer);
    return (FlashLayer) layer;    
  }

  /* (non-Javadoc)
   * @see forplay.core.GroupLayer#add(int, forplay.core.Layer)
   */
  @Override
  public void add(int index, Layer layer) {
    layers.add(index, layer);
    ((FlashLayer) layer).update();
    container().addChildAt(display(layer), index);
  }

  /* (non-Javadoc)
   * @see forplay.core.GroupLayer#clear()
   */
  @Override
  public void clear() {
    for (Layer l : layers) {
      container().removeChild(display(l));
    }
    layers.clear();
    
  }

  /**
   * @param l
   * @return
   */
  private DisplayObject display(Layer l) {
    return flash(l).display();
  }

  /* (non-Javadoc)
   * @see forplay.core.GroupLayer#get(int)
   */
  @Override
  public Layer get(int index) {
    // TODO Auto-generated method stub
    return layers.get(index);
  }

  /* (non-Javadoc)
   * @see forplay.core.GroupLayer#remove(forplay.core.Layer)
   */
  @Override
  public void remove(Layer layer) {
    layers.remove(layer);
    container().removeChild(display(layer));
  }

  /* (non-Javadoc)
   * @see forplay.core.GroupLayer#remove(int)
   */
  @Override
  public void remove(int index) {
    layers.remove(index);
    container().removeChildAt(index);
  }

  /* (non-Javadoc)
   * @see forplay.core.GroupLayer#size()
   */
  @Override
  public int size() {
    // TODO Auto-generated method stub
    return layers.size();
  }

  /* (non-Javadoc)
   * @see forplay.core.Layer#destroy()
   */
  @Override
  public void destroy() {
    super.destroy();
    for (Layer l : layers) {
      l.destroy();
    }
  }

  protected void updateChildren() {
    for (Layer l : layers) {
      ((FlashLayer) l).update();
    }
  }
}
