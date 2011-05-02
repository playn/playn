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
package forplay.android;

import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Xfermode;
import forplay.core.Surface;
import forplay.core.Surface.LineCap;
import forplay.core.Surface.LineJoin;

class AndroidSurfaceState {

  private Paint paint;
  private int fillColor;
  private int strokeColor;
  private AndroidGradient gradient;
  private AndroidPattern pattern;

  AndroidSurfaceState() {
    this(new Paint(), 0xff000000, 0xffffffff, null, null);
  }

  AndroidSurfaceState(AndroidSurfaceState toCopy) {
    this(toCopy.paint, toCopy.fillColor, toCopy.strokeColor, toCopy.gradient, toCopy.pattern);
  }

  AndroidSurfaceState(Paint paint, int fillColor, int strokeColor, AndroidGradient gradient, AndroidPattern pattern) {
    this.paint = paint;
    this.fillColor = fillColor;
    this.strokeColor = strokeColor;
  }

  void setFillColor(int color) {
    fillColor = color;
  }

  void setFillGradient(AndroidGradient gradient) {
    this.gradient = gradient;
  }

  void setFillPattern(AndroidPattern pattern) {
    this.pattern = pattern;
  }

  void setLineCap(Surface.LineCap cap) {
    paint.setStrokeCap(convertCap(cap));
  }

  void setLineJoin(Surface.LineJoin join) {
    paint.setStrokeJoin(convertJoin(join));
  }

  void setMiterLimit(float miter) {
    paint.setStrokeMiter(miter);
  }

  void setStrokeColor(int color) {
    strokeColor = color;
  }

  Paint prepareFill() {
    paint.setStyle(Style.FILL);
    if (gradient != null) {
      paint.setShader(gradient.shader);
    } else if (pattern != null) {
      paint.setShader(pattern.shader);
    } else {
      paint.setColor(fillColor);
    }
    return paint;
  }

  Paint prepareStroke() {
    paint.setStyle(Style.STROKE);
    paint.setColor(strokeColor);
    return paint;
  }

  void setCompositeOperation(Surface.Composite composite) {
    paint.setXfermode(convertComposite(composite));
  }

  void setStrokeWidth(float strokeWidth) {
    paint.setStrokeWidth(strokeWidth);
  }

  private Cap convertCap(LineCap cap) {
    switch (cap) {
      case BUTT: return Cap.BUTT;
      case ROUND: return Cap.ROUND;
      case SQUARE: return Cap.SQUARE;
    }
    return Cap.BUTT;
  }

  private Xfermode convertComposite(Surface.Composite composite) {
    switch (composite) {
      case DST_ATOP: return new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP);
      case DST_IN: return new PorterDuffXfermode(PorterDuff.Mode.DST_IN);
      case DST_OUT: return new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);
      case DST_OVER: return new PorterDuffXfermode(PorterDuff.Mode.DST_OVER);
      case SRC: return new PorterDuffXfermode(PorterDuff.Mode.SRC);
      case SRC_ATOP: return new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP);
      case SRC_IN: return new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
      case SRC_OUT: return new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT);
      case SRC_OVER: return new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER);
      case XOR: return new PorterDuffXfermode(PorterDuff.Mode.XOR);
    }
    return new PorterDuffXfermode(PorterDuff.Mode.SRC);
  }

  private Join convertJoin(LineJoin join) {
    switch (join) {
      case BEVEL: return Join.BEVEL;
      case MITER: return Join.MITER;
      case ROUND: return Join.ROUND;
    }
    return Join.MITER;
  }
}
