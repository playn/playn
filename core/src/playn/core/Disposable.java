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
 * Indicates that this instance should be closed when no longer needed to free up resources.
 *
 * <p>We'd just use {@link AutoCloseable} directly except that it annoyingly declares {@code close}
 * to throw an arbitrary exception which makes life annoying for callers. Because we extend
 * auto-closeable a disposable can be used anywhere an auto-closeable can, we're just less
 * burdensome.
 */
public interface Disposable extends AutoCloseable {

  /** Disposes this resource. */
  void close ();
}
