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
package playn.core;

/**
 * Simple PlayN logging interface.
 */
public interface Log {

  /** Tags a log message with a level. */
  enum Level { DEBUG, INFO, WARN, ERROR }

  /** Allows for collection of log messages (in addition to standard logging).
   * See {@link #setCollector}. */
  interface Collector {
    /**
     * Called when a message is logged.
     *
     * @param level the level at which the message was logged.
     * @param msg the message that was logged.
     * @param e the exception logged with the message, or null.
     */
    void logged(Level level, String msg, Throwable e);
  }

  /**
   * Configures a log message collector. This allows games to intercept (and record and submit with
   * bug reports, for example) all messages logged via the PlayN logging system. This will include
   * errors logged internally by PlayN code.
   */
  void setCollector(Collector collector);

  /**
   * Configures the minimum log level that will be logged. Messages at a level lower than
   * {@code level} will be suppressed. Note that all messages are still passed to any registered
   * {@link Collector}, but suppressed messages are not sent to the platform logging system.
   */
  void setMinLevel(Level level);

  /**
   * An debug message.
   *
   * @param msg the message to display
   */
  void debug(String msg);

  /**
   * An debug message.
   *
   * @param msg the message to display
   * @param e the exception to log
   */
  void debug(String msg, Throwable e);

  /**
   * An informational message.
   *
   * @param msg the message to display
   */
  void info(String msg);

  /**
   * /** An info message.
   *
   * @param msg the message to display
   * @param e the exception to log
   */
  void info(String msg, Throwable e);

  /**
   * An warning message.
   *
   * @param msg the message to display
   */
  void warn(String msg);

  /**
   * An warning message.
   *
   * @param msg the message to display
   * @param e the exception to log
   */
  void warn(String msg, Throwable e);

  /**
   * An error message.
   *
   * @param msg the message to display
   */
  void error(String msg);

  /**
   * An error message.
   *
   * @param msg the message to display
   * @param e the exception to log
   */
  void error(String msg, Throwable e);
}
