/**
 * Copyright 2010 The ForPlay Authors
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
package forplay.core;

/**
 * Simple ForPlay logging interface.
 */
public interface Log {

  /**
   * An error message.
   * 
   * @param msg the message to display
   * 
   * @param e the exception to log
   */
  void error(String msg, Throwable e);

  /**
   * An error message.
   * 
   * @param msg the message to display
   */
  void error(String msg);

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
}
