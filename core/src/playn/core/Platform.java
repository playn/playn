/**
 * Copyright 2010 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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

/**
 * Generic platform interface. New platforms are defined as implementations of this interface.
 */
public interface Platform {

  enum Type { JAVA, HTML, ANDROID, IOS, FLASH, STUB }

  void run(Game game);

  Platform.Type type();

  double time();

  int tick();

  float random();

  void openURL(String url);

  void invokeLater(Runnable runnable);

  void setLifecycleListener(PlayN.LifecycleListener listener);

  void setPropagateEvents(boolean propagate);

  Audio audio();

  Graphics graphics();

  Assets assets();

  Json json();

  Keyboard keyboard();

  Log log();

  Net net();

  Pointer pointer();

  Mouse mouse();

  Touch touch();

  Storage storage();

  Analytics analytics();

  RegularExpression regularExpression();
}
