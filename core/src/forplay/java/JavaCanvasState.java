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

  JavaCanvasState() {
    this(0xff000000, 0xffffffff, null, null, new AffineTransform(), 1.0f, LineCap.SQUARE,
        LineJoin.MITER, 10.0f, null, Composite.SRC_OVER);
 }

  JavaCanvasState(JavaCanvasState toCopy) {
    this(toCopy.fillColor, toCopy.strokeColor, toCopy.fillGradient, toCopy.fillPattern, toCopy.transform,
        toCopy.strokeWidth, toCopy.lineCap, toCopy.lineJoin, toCopy.miterLimit, toCopy.clip,
        toCopy.composite);
  }

  JavaCanvasState(int fillColor, int strokeColor, JavaGradient fillGradient, JavaPattern fillPattern,
      AffineTransform transform, float strokeWidth, LineCap lineCap, LineJoin lineJoin,
      float miterLimit, JavaPath clip, Composite composite) {
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
  }

  // TODO: optimize this so we're not setting this stuff all the time.
  void prepareStroke(Graphics2D gfx) {
    gfx.setStroke(new BasicStroke(strokeWidth, convertLineCap(), convertLineJoin(), miterLimit));
    gfx.setColor(convertColor(strokeColor));
    gfx.setClip(clip != null ? clip.path : null);
    gfx.setComposite(convertComposite(composite));
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
    gfx.setComposite(convertComposite(composite));
  }

  private Color convertColor(int color) {
    float a = (color >>> 24) / 255.0f;
    float r = ((color >>> 16) & 0xff) / 255.0f;
    float g = ((color >>> 8) & 0xff) / 255.0f;
    float b = (color & 0xff) / 255.0f;

    return new Color(r, g, b, a);
  }

  private java.awt.Composite convertComposite(Canvas.Composite composite) {
    switch (composite) {
      case DST_ATOP: return AlphaComposite.DstAtop;
      case DST_IN: return AlphaComposite.DstIn;
      case DST_OUT: return AlphaComposite.DstOut;
      case DST_OVER: return AlphaComposite.DstOver;
      case SRC: return AlphaComposite.Src;
      case SRC_ATOP: return AlphaComposite.SrcAtop;
      case SRC_IN: return AlphaComposite.SrcIn;
      case SRC_OUT: return AlphaComposite.SrcOut;
      case SRC_OVER: return AlphaComposite.SrcOver;
      case XOR: return AlphaComposite.Xor;
    }
    return AlphaComposite.Src;
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
