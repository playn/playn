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
 * Contains configuration for laying out and drawing single- or multi-line text to a {@link
 * Canvas}.
 */
public class TextFormat {

  /** Used to align multiline text. */
  public static enum Alignment {
    LEFT {
      @Override
      public float getX(float textWidth, float lineWidth) {
        return 0;
      }
    },
    CENTER {
      @Override
      public float getX(float textWidth, float lineWidth) {
        return (lineWidth - textWidth)/2;
      }
    },
    RIGHT {
      @Override
      public float getX(float textWidth, float lineWidth) {
        return (lineWidth - textWidth);
      }
    };

    /** Returns the x offset for text with the specified width rendered on a line of the specified
     * width. */
    public abstract float getX(float textWidth, float lineWidth);
  };

  /** Used to model the different text effects: shadow and outline. */
  public static abstract class Effect {
    /** Indicates that no text effect should be used. */
    public static final Effect NONE = new Effect() {
      @Override
      public String toString() {
        return "none";
      }
    };

    /** Creates a shadow effect as specified. */
    public static Effect shadow (int shadowColor, float shadowOffsetX, float shadowOffsetY) {
      return new Shadow(shadowColor, shadowOffsetX, shadowOffsetY);
    }

    /** Creates an outline effect as specified. */
    public static Effect outline (int outlineColor) {
      return new Outline(outlineColor);
    }

    /** Contains metadata for the shadow effect. */
    public static final class Shadow extends Effect {
      /** The color of the shadow (as {@code 0xAARRGGBB}). */
      public final int shadowColor;

      /** The pixel offset of the shadow in the x-direction. */
      public final float shadowOffsetX;

      /** The pixel offset of the shadow in the y-direction. */
      public final float shadowOffsetY;

      public Shadow (int shadowColor, float shadowOffsetX, float shadowOffsetY) {
        this.shadowColor = shadowColor;
        this.shadowOffsetX = shadowOffsetX;
        this.shadowOffsetY = shadowOffsetY;
      }

      @Override
      public float adjustWidth (float width) {
        return width + Math.abs(shadowOffsetX);
      }

      @Override
      public float adjustHeight (float height) {
        return height + Math.abs(shadowOffsetY);
      }

      @Override
      public Integer getAltColor () {
        return shadowColor;
      }

      @Override
      public String toString() {
        return "shadow [color=" + Integer.toHexString(shadowColor) +
          ", offX=" + shadowOffsetX + ", offY=" + shadowOffsetY + "]";
      }
    }

    /** Contains metadata for the outline effect. */
    public static final class Outline extends Effect {
      /** The color of the outline (as {@code 0xAARRGGBB}). */
      public final int outlineColor;

      public Outline (int outlineColor) {
        this.outlineColor = outlineColor;
      }

      @Override
      public float adjustWidth (float width) {
        return width + 4;
      }
      @Override
      public float adjustHeight (float height) {
        return height + 4;
      }
      @Override
      public Integer getAltColor () {
        return outlineColor;
      }

      @Override
      public String toString() {
        return "outline [color=" + Integer.toHexString(outlineColor) + "]";
      }
    }

    /** Used internally for text layout calculations. */
    public float adjustWidth (float width) {
      return width;
    }

    /** Used internally for text layout calculations. */
    public float adjustHeight (float height) {
      return height;
    }

    /** Used internally for text rendering. */
    public Integer getAltColor () {
      return null;
    }

    private Effect () {} // disallow other classes to extend this one
  }

  /** The font in which to render the text (or null which indicates that the default font should be
   * used). */
  public final Font font;

  /** The width at which to wrap lines of text (or {@link Float#MAX_VALUE} if the text should not
   * be wrapped. */
  public final float wrapWidth;

  /** The alignment to use for multiline text. */
  public final Alignment align;

  /** The fill color to be used for the text (as {@code 0xAARRGGBB}). */
  public final int textColor;

  /** The effect to apply to the text when rendering, or {@link Effect#NONE}. */
  public final Effect effect;

  /** Creates a default text format instance. */
  public TextFormat() {
    this(null, Float.MAX_VALUE, Alignment.LEFT, 0xFF000000, Effect.NONE);
  }

  /** Creates a configured text format instance. */
  public TextFormat(Font font, float wrapWidth, Alignment align, int textColor, Effect effect) {
    this.font = font;
    this.wrapWidth = wrapWidth;
    this.align = align;
    this.textColor = textColor;
    this.effect = effect;
  }

  /** Returns true if line wrapping is desired. */
  public boolean shouldWrap() {
    return wrapWidth != Float.MAX_VALUE;
  }

  /** Returns a clone of this text format with the font configured as specified. */
  public TextFormat withFont(Font font) {
    return new TextFormat(font, this.wrapWidth, this.align, this.textColor, this.effect);
  }

  /** Returns a clone of this text format with the wrap width and alignment configured as
   * specified. */
  public TextFormat withWrapping(float wrapWidth, Alignment align) {
    return new TextFormat(this.font, wrapWidth, align, this.textColor, this.effect);
  }

  /** Returns a clone of this text format with the wrap width configured as specified. */
  public TextFormat withWrapWidth(float wrapWidth) {
    return new TextFormat(this.font, wrapWidth, this.align, this.textColor, this.effect);
  }

  /** Returns a clone of this text format with the alignment configured as specified. */
  public TextFormat withAlignment(Alignment align) {
    return new TextFormat(this.font, this.wrapWidth, align, this.textColor, this.effect);
  }

  /** Returns a clone of this text format with the text color configured as specified. */
  public TextFormat withTextColor(int textColor) {
    return new TextFormat(this.font, this.wrapWidth, this.align, textColor, this.effect);
  }

  /** Returns a clone of this text format with the effect configured as specified. */
  public TextFormat withEffect(Effect effect) {
    return new TextFormat(this.font, this.wrapWidth, this.align, this.textColor, effect);
  }

  @Override
  public String toString() {
    String wrapStr = shouldWrap() ? ""+wrapWidth : "n/a";
    return "[font=" + font + ", wrapWidth=" + wrapStr + ", align=" + align +
      ", textColor=" + Integer.toHexString(textColor) + ", effect=" + effect + "]";
  }
}
