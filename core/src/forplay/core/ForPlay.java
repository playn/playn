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
 * The main ForPlay interface. The static methods in this class provide access to
 * the various available subsystems.
 * 
 * <p>
 * You must register a {@link Platform} before calling any of these methods. For
 * example, <code>JavaPlatform.register();</code>.
 * </p>
 */
public class ForPlay {

  private static Platform platform;
  
  public static RegularExpression regularExpression() {
    return platform.regularExpression();
  }

  /**
   * Gets the {@link Audio} interface.
   */
  public static Audio audio() {
    return platform.audio();
  }

  /**
   * Gets the current time, as a double value in milliseconds since January 1,
   * 1970, 00:00:00 GMT.
   * 
   * <p>
   * This is equivalent to the standard JRE <code>new Date().getTime();</code>,
   * but it slightly more terse, and avoids the use of <code>long</code> values,
   * which are best to avoid when translating to Javascript.
   * </p>
   */
  public static double currentTime() {
    return platform.time();
  }

  /**
   * Gets the {@link Graphics} interface.
   */
  public static Graphics graphics() {
    return platform.graphics();
  }
  
  public static AssetManager assetManager() {
    return platform.assetManager();
  }

  /**
   * Gets the {@link Json} interface.
   */
  public static Json json() {
    return platform.json();
  }

  /**
   * Gets the {@link Keyboard} input interface.
   */
  public static Keyboard keyboard() {
    return platform.keyboard();
  }

  /**
   * Gets the {@link Log} interface.
   */
  public static Log log() {
    return platform.log();
  }

  /**
   * Gets the {@link Net} interface.
   */
  public static Net net() {
    return platform.net();
  }

  /**
   * Gets the {@link Pointer} input interface.
   */
  public static Pointer pointer() {
    return platform.pointer();
  }

  /**
   * Gets the {@link Mouse} input interface if supported, or null otherwise.
   */
  public static Mouse mouse() {
    return platform.mouse();
  }

  /**
   * Gets the {@link Touch} input interface if supported, or null otherwise.
   */
  public static Touch touch() {
    return platform.touch();
  }

  /**
   * Gets the {@link Storage} storage interface.
   */
  public static Storage storage() {
    return platform.storage();
  }

  /**
   * Gets the {@link Analytics} analytics interface.
   */
  public static Analytics analytics() {
    return platform.analytics();
  }

  /**
   * Gets a random floating-point value in the range [0, 1).
   */
  public static float random() {
    return platform.random();
  }

  /**
   * Call this method to start your {@link Game}. It must be called only once,
   * and all work after this call is made will be performed in {@link Game}'s
   * callback methods.
   */
  public static void run(Game game) {
    platform.run(game);
  }

  /**
   * Opens the given URL in the default browser.
   * @param url URL
   */
  public static void openURL(String url) {
    platform.openURL(url);
  }

  /**
   * Called in a {@link Platform}'s registration method. Do not call this
   * directly unless you're implementing a new platform.
   */
  public static void setPlatform(Platform platform) {
    ForPlay.platform = platform;
  }

  // Non-instantiable
  private ForPlay() {
  }
}
