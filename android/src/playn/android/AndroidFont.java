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

import java.util.EnumMap;
import java.util.Map;

import android.graphics.Typeface;

import playn.core.Font;

class AndroidFont {

  private static final String[] NO_HACKS = {};

  public static final AndroidFont DEFAULT = new AndroidFont(Typeface.DEFAULT, 14, null);

  public final Typeface typeface;
  public final float size;
  public final String[] ligatureHacks;

  public AndroidFont(Typeface typeface, float size, String[] ligatureHacks) {
    this.typeface = typeface;
    this.size = size;
    this.ligatureHacks = (ligatureHacks != null) ? ligatureHacks : NO_HACKS;
  }

  public static Typeface create (Font font) {
    return Typeface.create(font.name, TO_ANDROID_STYLE.get(font.style));
  }

  protected static final Map<Font.Style,Integer> TO_ANDROID_STYLE =
    new EnumMap<Font.Style,Integer>(Font.Style.class);
  static {
    TO_ANDROID_STYLE.put(Font.Style.PLAIN,       Typeface.NORMAL);
    TO_ANDROID_STYLE.put(Font.Style.BOLD,        Typeface.BOLD);
    TO_ANDROID_STYLE.put(Font.Style.ITALIC,      Typeface.ITALIC);
    TO_ANDROID_STYLE.put(Font.Style.BOLD_ITALIC, Typeface.BOLD_ITALIC);
  }
}
