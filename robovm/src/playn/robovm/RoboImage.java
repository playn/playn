/**
 * Copyright 2014 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playn.robovm;

import org.robovm.apple.coregraphics.CGImage;
import playn.core.gl.GLContext;
import playn.core.gl.Scale;

/**
 * Implements {@link Image} based on a static bitmap.
 */
public class RoboImage extends RoboAbstractImage {

  protected CGImage image; // only mutated by RoboAsyncImage

  public RoboImage(GLContext ctx, CGImage image, Scale scale) {
    super(ctx, scale);
    this.image = image;
  }

  @Override
  public CGImage cgImage() {
    return image;
  }

  @Override
  public float width() {
    return scale.invScaled(image.getWidth());
  }

  @Override
  public float height() {
    return scale.invScaled(image.getHeight());
  }

  @Override
  protected void updateTexture(int tex) {
    ((RoboGLContext) ctx).updateTexture(tex, image);
  }
}
