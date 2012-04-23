/**
 * Copyright 2010 The PlayN Authors
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
package playn.java;

import java.awt.TexturePaint;

import playn.core.gl.GLPattern;
import playn.core.gl.ImageGL;

class JavaPattern implements GLPattern {

  private final ImageGL image;
  final TexturePaint paint;

  JavaPattern(ImageGL image, TexturePaint paint) {
    this.image = image;
    this.paint = paint;
  }

  @Override
  public ImageGL image() {
    return image;
  }
}
