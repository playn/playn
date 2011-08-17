/*
 * FlashCanvas
 *
 * Copyright (c) 2009      Shinya Muramatsu
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
 * @author Shinya Muramatsu
 * @see    http://code.google.com/p/ascanvas/
 */

package com.googlecode.flashcanvas
{
    import flash.geom.Matrix;

    public class CanvasGradient
    {
        public var type:String;
        public var colorStops:Array = [];
        public var matrix:Matrix = null;
        public var focalPointRatio:Number = 0;

        public function CanvasGradient()
        {
        }

        public function addColorStop(offset:Number, color:String):void
        {
            var deleteCount:int = 0;

            for (var i:int = colorStops.length; i > 0; i--)
            {
                if (colorStops[i - 1].offset <= offset)
                {
                    // All but the first and last stop added at each point
                    // to be ignored.
                    if (i > 1 && colorStops[i - 2].offset == offset)
                    {
                        i--;
                        deleteCount = 1;
                    }
                    break;
                }
            }

            colorStops.splice(i, deleteCount, new ColorStop(offset, color));
        }

        public function get colors():Array
        {
            var ary:Array = [];
            for (var i:int = 0, n:int = colorStops.length; i < n; i++)
            {
                ary[i] = colorStops[i].color;
            }
            return ary;
        }

        public function get alphas():Array
        {
            var ary:Array = [];
            for (var i:int = 0, n:int = colorStops.length; i < n; i++)
            {
                ary[i] = colorStops[i].alpha;
            }
            return ary;
        }

        public function get ratios():Array
        {
            var ary:Array = [];
            for (var i:int = 0, n:int = colorStops.length; i < n; i++)
            {
                ary[i] = colorStops[i].offset * 255;
            }
            return ary;
        }
    }
}
