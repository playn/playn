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

import forplay.core.Asserts;
import forplay.core.GroupLayerImpl;
import forplay.core.GroupLayer;
import forplay.core.Layer;

public class FlashGroupLayer extends FlashLayer implements GroupLayer {

  private GroupLayerImpl<FlashLayer> impl = new GroupLayerImpl<FlashLayer>();

  FlashGroupLayer(DisplayObjectContainer container) {
    super(container);
  }

  FlashGroupLayer() {
    super(Sprite.create());
  }

  static FlashGroupLayer getRoot() {
    return new FlashGroupLayer(Sprite.getRootSprite());
  }

  @Override
  public Layer get(int index) {
    return impl.children.get(index);
  }

  @Override
  public void add(Layer layer) {
    impl.add(this, (FlashLayer) layer);
    ((FlashLayer) layer).update();
    container().addChild(display(layer));
  }

  @Override
  public void add(int index, Layer layer) {
    impl.add(this, index, (FlashLayer) layer);
    ((FlashLayer) layer).update();
    container().addChildAt(display(layer), index);
  }

  @Override
  public void remove(Layer layer) {
    impl.remove(this, (FlashLayer) layer);
    container().removeChild(display(layer));
  }

  @Override
  public void remove(int index) {
    impl.remove(this, index);
    container().removeChildAt(index);
  }

  @Override
  public void clear() {
    impl.clear(this);
  }

  @Override
  public int size() {
    return impl.children.size();
  }

  @Override
  public void destroy() {
    super.destroy();
    impl.destroy(this);
  }

  @Override
  public void onAdd() {
    super.onAdd();
    impl.onAdd(this);
  }

  @Override
  public void onRemove() {
    super.onRemove();
    impl.onRemove(this);
  }

  protected void updateChildren() {
    for (Layer l : impl.children) {
      ((FlashLayer) l).update();
    }
  }

  private DisplayObjectContainer container() {
    return display().cast();
  }

  private FlashLayer flash(Layer layer) {
    Asserts.checkArgument(layer instanceof FlashLayer);
    return (FlashLayer) layer;
  }

  private DisplayObject display(Layer l) {
    return flash(l).display();
  }
}
