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
 * The main PlayN interface. The static methods in this class provide access to
 * the various available subsystems.
 *
 * <p>
 * You must register a {@link Platform} before calling any of these methods. For
 * example, <code>JavaPlatform.register();</code>.
 * </p>
 */
public class PlayN {

  private static Platform platform;

  /**
   * Call this method to start your {@link Game}. It must be called only once,
   * and all work after this call is made will be performed in {@link Game}'s
   * callback methods.
   */
  public static void run(Game game) {
    platform.run(game);
  }

  /**
   * Returns the current time, as a double value in millis since January 1, 1970, 00:00:00 GMT.
   *
   * <p> This is equivalent to the standard JRE {@code new Date().getTime();}, but it slightly
   * terser, and avoids the use of {@code long} values, which are best avoided when translating to
   * JavaScript. </p>
   */
  public static double currentTime() {
    return platform.time();
  }

  /**
   * Gets a random floating-point value in the range [0, 1).
   */
  public static float random() {
    return platform.random();
  }

  /**
   * Opens the given URL in the default browser.
   */
  public static void openURL(String url) {
    platform.openURL(url);
  }

  /**
   * Returns the platform {@link Platform.Type}.
   */
  public static Platform.Type platformType() {
    return platform.type();
  }

  /**
   * Returns the {@link Audio} service.
   */
  public static Audio audio() {
    return platform.audio();
  }

  /**
   * Returns the {@link Graphics} service.
   */
  public static Graphics graphics() {
    return platform.graphics();
  }

  /**
   * Returns the {@link Assets} service.
   */
  public static Assets assets() {
    return platform.assets();
  }

  /**
   * Returns the {@link Json} service.
   */
  public static Json json() {
    return platform.json();
  }

  /**
   * Returns the {@link Keyboard} input service.
   */
  public static Keyboard keyboard() {
    return platform.keyboard();
  }

  /**
   * Returns the {@link Log} service.
   */
  public static Log log() {
    return platform.log();
  }

  /**
   * Returns the {@link Net} service.
   */
  public static Net net() {
    return platform.net();
  }

  /**
   * Returns the {@link Pointer} input service.
   */
  public static Pointer pointer() {
    return platform.pointer();
  }

  /**
   * Returns the {@link Mouse} input service (if supported, null otherwise).
   */
  public static Mouse mouse() {
    return platform.mouse();
  }

  /**
   * Returns the {@link RegularExpression} service.
   */
  public static RegularExpression regularExpression() {
    return platform.regularExpression();
  }

  /**
   * Returns the {@link Touch} input service (if supported, null otherwise).
   */
  public static Touch touch() {
    return platform.touch();
  }

  /**
   * Returns the {@link Storage} storage service.
   */
  public static Storage storage() {
    return platform.storage();
  }

  /**
   * Returns the {@link Analytics} analytics service.
   */
  public static Analytics analytics() {
    return platform.analytics();
  }

  /**
   * Configures the current {@link Platform}. Do not call this directly unless you're implementing
   * a new platform.
   */
  public static void setPlatform(Platform platform) {
    PlayN.platform = platform;
  }

  // Non-instantiable
  private PlayN() {
  }
}
