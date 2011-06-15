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
package forplay.java;

import forplay.core.Canvas;
import forplay.core.Canvas.Composite;
import forplay.core.Canvas.LineCap;
import forplay.core.Canvas.LineJoin;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

class JavaCanvasState {

  int fillColor;
  int strokeColor;
  JavaGradient fillGradient;
  JavaPattern fillPattern;
  AffineTransform transform;
  float strokeWidth;
  LineCap lineCap;
  LineJoin lineJoin;
  float miterLimit;
  JavaPath clip;
  Composite composite;
  float alpha;

  JavaCanvasState() {
    this(0xff000000, 0xffffffff, null, null, new AffineTransform(), 1.0f, LineCap.SQUARE,
        LineJoin.MITER, 10.0f, null, Composite.SRC_OVER, 1);
 }

  JavaCanvasState(JavaCanvasState toCopy) {
    this(toCopy.fillColor, toCopy.strokeColor, toCopy.fillGradient, toCopy.fillPattern,
        toCopy.transform, toCopy.strokeWidth, toCopy.lineCap, toCopy.lineJoin, toCopy.miterLimit,
        toCopy.clip, toCopy.composite, toCopy.alpha);
  }

  JavaCanvasState(int fillColor, int strokeColor, JavaGradient fillGradient,
      JavaPattern fillPattern, AffineTransform transform, float strokeWidth, LineCap lineCap,
      LineJoin lineJoin, float miterLimit, JavaPath clip, Composite composite, float alpha) {
    this.fillColor = fillColor;
    this.strokeColor = strokeColor;
    this.fillGradient = fillGradient;
    this.fillPattern = fillPattern;
    this.transform = transform;
    this.strokeWidth = strokeWidth;
    this.lineCap = lineCap;
    this.lineJoin = lineJoin;
    this.miterLimit = miterLimit;
    this.composite = composite;
    this.alpha = alpha;
  }

  // TODO: optimize this so we're not setting this stuff all the time.
  void prepareStroke(Graphics2D gfx) {
    gfx.setStroke(new BasicStroke(strokeWidth, convertLineCap(), convertLineJoin(), miterLimit));
    gfx.setColor(convertColor(strokeColor));
    gfx.setClip(clip != null ? clip.path : null);
    gfx.setComposite(convertComposite(composite, alpha));
  }

  // TODO: optimize this so we're not setting this stuff all the time.
  void prepareFill(Graphics2D gfx) {
    if (fillGradient != null) {
      gfx.setPaint(fillGradient.paint);
    } else if (fillPattern != null) {
      fillPattern.updateSize();
      gfx.setPaint(fillPattern.paint);
    } else {
      gfx.setPaint(convertColor(fillColor));
    }
    gfx.setClip(clip != null ? clip.path : null);
    gfx.setComposite(convertComposite(composite, alpha));
  }

  private Color convertColor(int color) {
    float a = (color >>> 24) / 255.0f;
    float r = ((color >>> 16) & 0xff) / 255.0f;
    float g = ((color >>> 8) & 0xff) / 255.0f;
    float b = (color & 0xff) / 255.0f;

    return new Color(r, g, b, a);
  }

  private java.awt.Composite convertComposite(Canvas.Composite composite, float alpha) {
    AlphaComposite ret;
    switch (composite) {
      case DST_ATOP: ret = AlphaComposite.DstAtop; break;
      case DST_IN: ret = AlphaComposite.DstIn; break;
      case DST_OUT: ret = AlphaComposite.DstOut; break;
      case DST_OVER: ret = AlphaComposite.DstOver; break;
      case SRC: ret = AlphaComposite.Src; break;
      case SRC_ATOP: ret = AlphaComposite.SrcAtop; break;
      case SRC_IN: ret = AlphaComposite.SrcIn; break;
      case SRC_OUT: ret = AlphaComposite.SrcOut; break;
      case SRC_OVER: ret = AlphaComposite.SrcOver; break;
      case XOR: ret = AlphaComposite.Xor; break;
      default: ret = AlphaComposite.Src; break;
    }
    if (alpha != 1) {
      return ret.derive(alpha);
    } else {
      return ret;
    }
  }

  private int convertLineCap() {
    switch (lineCap) {
      case BUTT:
        return BasicStroke.CAP_BUTT;
      case ROUND:
        return BasicStroke.CAP_ROUND;
      case SQUARE:
        return BasicStroke.CAP_SQUARE;
    }
    return BasicStroke.CAP_SQUARE;
  }

  private int convertLineJoin() {
    switch (lineJoin) {
      case BEVEL:
        return BasicStroke.JOIN_BEVEL;
      case MITER:
        return BasicStroke.JOIN_MITER;
      case ROUND:
        return BasicStroke.JOIN_ROUND;
    }
    return BasicStroke.JOIN_MITER;
  }
}
