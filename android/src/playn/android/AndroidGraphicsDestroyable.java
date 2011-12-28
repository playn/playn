/**
 * Copyright 2011 The PlayN Authors
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
package playn.android;

/**
 * Allows the contents of an object to be destroyed after the original Java object has disappeared.
 * This is used to safely destroy GL objects on the GL thread.
 * 
 * Classes that implement this interface must not hold references to the objects they are
 * destroying.
 */
abstract class AndroidGraphicsDestroyable {
  /**
   * Destroys the underlying native resources immediately.
   */
  public abstract void destroy(AndroidGraphics gfx);

  /**
   * Destroys this object now. Assumes that the caller is correctly running on the GL thread.
   */
  public void destroyNow() {
    AndroidGraphics gfx = AndroidPlatform.instance.graphics();
    destroy(gfx);
  }

  /**
   * Defers the destruction of this object until the GL thread is running.
   */
  public void destroyLater() {
    AndroidGraphics gfx = AndroidPlatform.instance.graphics();
    gfx.queueDestroyable(this);
  }
}
