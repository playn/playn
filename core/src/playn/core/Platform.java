/**
 * Copyright 2010-2015 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0  (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playn.core;

import react.RPromise;
import react.Signal;
import react.Slot;
import react.UnitSlot;

/**
 * Provides access to all PlayN cross-platform services.
 */
public abstract class Platform {

  /** Defines the lifecycle events. */
  public static enum Lifecycle { PAUSE, RESUME, EXIT };

  /** A signal emitted with lifecycle events. */
  public Signal<Lifecycle> lifecycle = Signal.create();

  /** This signal will be emitted at the start of every frame after the platform is {@link
    * #start}ed. Games should connect to it to drive their main loop. */
  public Signal<Platform> frame = Signal.create();

  /** Used by {@link #reportError}. */
  public static class Error {
    public final String message;
    public final Throwable cause;
    public Error (String message, Throwable cause) {
      this.message = message;
      this.cause = cause;
    }
  }

  /** Any errors reported via {@link #reportError} will be emitted to this signal in addition to
    * being logged. Games can connect to this signal if they wish to capture and record platform
    * errors. */
  public Signal<Error> errors = Signal.create();

  /** Enumerates the supported platform types. */
  public static enum Type { JAVA, HTML, ANDROID, IOS, STUB }

  /** Returns the platform {@link Platform.Type}. */
  public abstract Platform.Type type ();

  /** Returns the current time, as a double value in millis since January 1, 1970, 00:00:00 GMT.
    * This is equivalent to the standard JRE {@code new Date().getTime();}, but is terser and
    * avoids the use of {@code long}, which is best avoided when translating to JavaScript. */
  public abstract double time ();

  /** Returns the number of milliseconds that have elapsed since the game started. */
  public abstract int tick ();

  /** Opens the given URL in the default browser. */
  public abstract void openURL (String url);

  /** Returns the {@link Assets} service. */
  public abstract Assets assets ();

  /** Returns the {@link Audio} service. */
  public abstract Audio audio ();

  /** Returns the {@link Graphics} service. */
  public abstract Graphics graphics ();

  /** Returns the {@link Input} service. */
  public abstract Input input ();

  /** Returns the {@link Json} service. */
  public abstract Json json ();

  /** Returns the {@link Log} service. */
  public abstract Log log ();

  /** Returns the {@link Net} service. */
  public abstract Net net ();

  /** Returns the {@link Storage} storage service. */
  public abstract Storage storage ();

  /**
   * Queues the supplied runnable for invocation on the game thread prior to the next frame. Note:
   * this uses {@link #invokeLater(UnitSlot)} so feel free to cut out the middle man.
   */
  public void invokeLater (final Runnable runnable) {
    invokeLater(new Slot<Platform>() { public void onEmit (Platform plat) { runnable.run(); }});
  }

  /**
   * Connects {@code action} to the {@link #frame} signal at a high priority and for a single
   * execution. This ensures that it runs before the game's normal callbacks.
   */
  public void invokeLater (Slot<? super Platform> action) {
    frame.connect(action).atPrio(Short.MAX_VALUE).once();
  }

  /**
   * Creates a promise which defers notification of success or failure to the game thread,
   * regardless of what thread on which it is completed. Note that even if it is completed on the
   * game thread, it will still defer completion until the next frame.
   */
  public <T> RPromise<T> deferredPromise () {
    return new RPromise<T>() {
      @Override public void succeed (final T value) {
        invokeLater(new Slot<Platform>() {
          public void onEmit (Platform plat) { superSucceed(value); }
        });
      }
      @Override public void fail (final Throwable cause) {
        invokeLater(new Slot<Platform>() {
          public void onEmit (Platform plat) { superFail(cause); }
        });
      }
      private void superSucceed (T value) { super.succeed(value); }
      private void superFail (Throwable cause) { super.fail(cause); }
    };
  }

  /**
   * Returns whether this platform supports async (background) operations.
   * HTML doesn't, most other platforms do.
   */
  public boolean isAsyncSupported  () {
    return false;
  }

  /**
   * Invokes the supplied action on a separate thread.
   * @throws UnsupportedOperationException if the platform does not support async operations.
   */
  public void invokeAsync (Runnable action) {
    throw new UnsupportedOperationException();
  }

  /**
   * Called when a backend (or other framework code) encounters an exception that it can recover
   * from, but which it would like to report in some orderly fashion. <em>NOTE:</em> this method
   * may be called from threads other than the main PlayN thread.
   */
  public void reportError (String message, Throwable cause) {
    errors.emit(new Error(message, cause));
    log().warn(message, cause);
  }
}
