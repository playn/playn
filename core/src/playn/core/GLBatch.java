/**
 * Copyright 2010-2015 The PlayN Authors
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
package playn.core;

/**
 * A batch manages the delivery of groups of drawing calls to the GPU. It is usually a combination
 * of a {@link GLProgram} and one or more buffers.
 */
public abstract class GLBatch {

  /**
   * Must be called before this batch is used to accumulate and send drawing commands.
   */
  public abstract void begin (float fbufWidth, float fbufHeight);

  /**
   * Sends any accumulated drawing calls to the GPU. Depending on the nature of the batch, this may
   * be necessary before certain state changes (like switching to a new texture).
   */
  public abstract void flush ();

  /**
   * Must be called when one is done using this batch to accumulate and send drawing commands.
   * Note: implementations should probably automatically flush any pending calls in this call
   * rather than require the caller to remember to call {@link #flush} manually.
   */
  public abstract void end ();

  /**
   * Releases any GPU resources retained by this batch. This should be called when the batch will
   * never again be used.
   */
  public abstract void destroy ();
}
