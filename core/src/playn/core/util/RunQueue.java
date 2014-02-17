/**
 * Copyright 2012 The PlayN Authors
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
package playn.core.util;

import playn.core.AbstractPlatform;
import playn.core.Platform;

/**
 * Manages a queue of runnables. Used to implement {@link Platform#invokeLater} by the various
 * platforms.
 */
public class RunQueue {

  private final AbstractPlatform platform;
  private Entry head;

  private class Entry {
    public final Runnable runnable;
    public Entry next;
    public Entry(Runnable runnable) {
      this.runnable = runnable;
    }
  }

  /**
   * Creates a run queue.
   */
  public RunQueue(AbstractPlatform platform) {
    this.platform = platform;
  }

  /**
   * Executes all pending runnables (in the order they were added).
   */
  public void execute() {
    Entry head;
    synchronized(this) {
      head = this.head;
      this.head = null;
    }
    while (head != null) {
      try {
        head.runnable.run();
      } catch (Throwable t) {
        platform.reportError("Failure executing runnable: " + head.runnable, t);
      }
      head = head.next;
    }
  }

  /**
   * Adds {@code runnable} to the end of the queue.
   */
  public synchronized void add(Runnable runnable) {
    if (head == null) {
      head = new Entry(runnable);
    } else {
      Entry parent = head;
      while (parent.next != null) {
        parent = parent.next;
      }
      parent.next = new Entry(runnable);
    }
  }
}
