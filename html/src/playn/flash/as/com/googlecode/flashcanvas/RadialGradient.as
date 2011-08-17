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
    import flash.display.GradientType;
    import flash.geom.Matrix;

    public class RadialGradient extends CanvasGradient
    {
        private var gradientStartFrom:int = 0;
        private var minRatio:Number = 0;
        private var range:Number = 255;

        public function RadialGradient(x0:Number, y0:Number, r0:Number, x1:Number, y1:Number, r1:Number)
        {
            type = GradientType.RADIAL;

            // If x0 = x1 and y0 = y1 and r0 = r1, then the radial gradient
            // must paint nothing.
            if (x0 == x1 && y0 == y1 && r0 == r1)
                return;

            // find which radius is longer, that will be outer ring
            var tx:Number, ty:Number, d:Number, dx:Number, dy:Number;

            if (r0 > r1)
            {
                // x0, x1 is center
                // gradientStartFrom 0
                tx = x0 - r0;
                ty = y0 - r0;
                d  = r0 * 2;
                dx = x1 - x0;
                dy = y1 - y0;

                minRatio = r1 / r0 * 255;
                focalPointRatio = Math.sqrt(dx * dx + dy * dy) / (r0 - r1);
            }
            else
            {
                tx = x1 - r1;
                ty = y1 - r1;
                d  = r1 * 2;
                dx = x0 - x1;
                dy = y0 - y1;

                minRatio = r0 / r1 * 255;
                focalPointRatio = Math.sqrt(dx * dx + dy * dy) / (r1 - r0);
                gradientStartFrom = 1;
            }

            range = 255 - minRatio;
            var rotation:Number = Math.atan2(dy, dx);
            matrix = new Matrix();
            matrix.createGradientBox(d, d, rotation, tx, ty);
        }

        override public function addColorStop(offset:Number, color:String):void
        {
            if (gradientStartFrom == 0)
                offset = 1 - offset;
            super.addColorStop(offset, color);
        }

        override public function get ratios():Array
        {
            var ary:Array = [];
            for (var i:int = 0, n:int = colorStops.length; i < n; i++)
            {
                ary[i] = colorStops[i].offset * range + minRatio;
            }
            return ary;
        }
    }
}
