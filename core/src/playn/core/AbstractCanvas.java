/**
 * Copyright 2013 The PlayN Authors
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
package playn.core;

/**
 * A base {@link Canvas} implementation shared by all platforms.
 */
public abstract class AbstractCanvas implements Canvas {

  protected final float width, height;

  @Override
  public float width() {
    return width;
  }

  @Override
  public float height() {
    return height;
  }

  protected AbstractCanvas(float width, float height) {
    this.width = width;
    this.height = height;
  }
}
