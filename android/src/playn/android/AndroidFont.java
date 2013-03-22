/**
 * Copyright 2011 The PlayN Authors
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
package playn.android;

import android.graphics.Typeface;

import java.util.EnumMap;
import java.util.Map;

import playn.core.AbstractFont;

class AndroidFont extends AbstractFont {

  private static final String[] NO_HACKS = {};

  public static AndroidFont DEFAULT = new AndroidFont(
    "Default", Style.PLAIN, 14, Typeface.DEFAULT, null);

  public final Typeface typeface;
  public final String[] ligatureHacks;

  public AndroidFont(String name, Style style, float size, Typeface typeface,
                     String[] ligatureHacks) {
    super(name, style, size);
    this.typeface = (typeface != null) ? typeface :
      Typeface.create(name, TO_ANDROID_STYLE.get(style));
    this.ligatureHacks = (ligatureHacks != null) ? ligatureHacks : NO_HACKS;
  }

  protected static final Map<Style,Integer> TO_ANDROID_STYLE =
    new EnumMap<Style,Integer>(Style.class);
  static {
    TO_ANDROID_STYLE.put(Style.PLAIN,       Typeface.NORMAL);
    TO_ANDROID_STYLE.put(Style.BOLD,        Typeface.BOLD);
    TO_ANDROID_STYLE.put(Style.ITALIC,      Typeface.ITALIC);
    TO_ANDROID_STYLE.put(Style.BOLD_ITALIC, Typeface.BOLD_ITALIC);
  }
}
