/**
 * Copyright 2010 The ForPlay Authors
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
package forplay.core;

import forplay.core.Layer;

/**
 * Base {@link Layer} implementation shared among platforms (to avoid reinventing the transform
 * logic).
 */
public abstract class AbstractLayer implements Layer {

  protected Transform transform;
  protected float originX, originY;
  private GroupLayer parent;
  private boolean isDestroyed;

  protected AbstractLayer() {
    transform = new Transform();
  }

  @Override
  public void destroy() {
    if (parent() != null) {
      parent().remove(this);
    }
    isDestroyed = true;
  }

  @Override
  public boolean isDestroyed() {
    return isDestroyed;
  }

  @Override
  public void setOrigin(float x, float y) {
    this.originX = x;
    this.originY = y;
  }

  @Override
  public void setRotation(float angle) {
    transform.setRotation(angle);
  }

  @Override
  public void setScale(float s) {
    assert s != 0;
    transform.setScale(s);
  }

  @Override
  public void setScale(float x, float y) {
    assert x != 0 && y != 0;
    transform.setScale(x, y);
  }

  @Override
  public void setTranslation(float x, float y) {
    transform.setTranslation(x, y);
  }

  @Override
  public Transform transform() {
    return transform;
  }

  @Override
  public GroupLayer parent() {
    return parent;
  }

  public void onAdd() {
    assert !isDestroyed : "Illegal to use destroyed layers";
  }

  public void onRemove() {
  }

  public void setParent(GroupLayer parent) {
    this.parent = parent;
  }
}
