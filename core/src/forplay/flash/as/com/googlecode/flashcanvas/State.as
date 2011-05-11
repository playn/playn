/*
 * FlashCanvas
 *
 * Copyright (c) 2009      Tim Cameron Ryan
 * Copyright (c) 2009-2011 FlashCanvas Project
 * Licensed under the MIT License.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 * @author Colin Leung (developed ASCanvas)
 * @author Tim Cameron Ryan
 * @author Shinya Muramatsu
 * @see    http://code.google.com/p/ascanvas/
 */

package com.googlecode.flashcanvas
{
    import flash.display.CapsStyle;
    import flash.display.JointStyle;
    import flash.geom.Matrix;

    public class State
    {
        public var transformMatrix:Matrix;
        public var clippingPath:Path;
        public var strokeStyle:Object;
        public var fillStyle:Object;
        public var globalAlpha:Number;
        public var lineWidth:Number;
        public var lineCap:String;
        public var lineJoin:String;
        public var miterLimit:Number;
        public var shadowOffsetX:Number;
        public var shadowOffsetY:Number;
        public var shadowBlur:Number;
        public var shadowColor:CSSColor;
        public var globalCompositeOperation:String;
        public var font:String;
        public var textAlign:String;
        public var textBaseline:String;
        public var lineScale:Number;

        public function State()
        {
            transformMatrix          = new Matrix();
            clippingPath             = new Path();
            strokeStyle              = new CSSColor("#000000");
            fillStyle                = new CSSColor("#000000");
            globalAlpha              = 1.0;
            lineWidth                = 1.0;
            lineCap                  = CapsStyle.NONE;
            lineJoin                 = JointStyle.MITER;
            miterLimit               = 10.0;
            shadowOffsetX            = 0;
            shadowOffsetY            = 0;
            shadowBlur               = 0;
            shadowColor              = new CSSColor("rgba(0,0,0,0)");
            globalCompositeOperation = "source-over";
            font                     = "10px sans-serif";
            textAlign                = "start";
            textBaseline             = "alphabetic";
            lineScale                = 1.0;
        }

        public function clone():State
        {
            var state:State = new State();
            state.transformMatrix          = transformMatrix.clone();
            state.clippingPath             = clippingPath.clone();
            state.strokeStyle              = strokeStyle;
            state.fillStyle                = fillStyle;
            state.globalAlpha              = globalAlpha;
            state.lineWidth                = lineWidth;
            state.lineCap                  = lineCap;
            state.lineJoin                 = lineJoin;
            state.miterLimit               = miterLimit;
            state.shadowOffsetX            = shadowOffsetX;
            state.shadowOffsetY            = shadowOffsetY;
            state.shadowBlur               = shadowBlur;
            state.shadowColor              = shadowColor;
            state.globalCompositeOperation = globalCompositeOperation;
            state.font                     = font;
            state.textAlign                = textAlign;
            state.textBaseline             = textBaseline;
            state.lineScale                = lineScale;
            return state;
        }
    }
}
