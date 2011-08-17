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
 * @author Shinya Muramatsu
 */

package com.googlecode.flashcanvas
{
    import flash.display.BitmapData;
    import flash.events.ErrorEvent;
    import flash.events.Event;
    import flash.geom.Point;
    import flash.geom.Rectangle;

    public class CanvasPattern
    {
        private var _image:Image;
        private var _repetition:String;
        private var _bitmapData:BitmapData;

        public function CanvasPattern(image:Image, repetition:String)
        {
            _image      = image;
            _repetition = repetition;

            // If the image is ready for use
            if (image.complete)
            {
                // Set BitmapData immediately
                _setBitmapData();
            }

            // If the image is not yet ready
            else
            {
                // Register event listeners
                image.addEventListener("load", _loadHandler);
                image.addEventListener(ErrorEvent.ERROR, _errorHandler);
            }
        }

        private function _loadHandler(event:Event):void
        {
            // Remove the event listeners
            var image:Image = event.target as Image;
            image.removeEventListener("load", _loadHandler);
            image.removeEventListener(ErrorEvent.ERROR, _errorHandler);

            // Set BitmapData
            _setBitmapData();
        }

        private function _errorHandler(event:ErrorEvent):void
        {
            // Remove the event listeners
            var image:Image = event.target as Image;
            image.removeEventListener("load", _loadHandler);
            image.removeEventListener(ErrorEvent.ERROR, _errorHandler);
        }

        private function _setBitmapData():void
        {
            var sourceBitmapData:BitmapData = _image.bitmapData;
            var width:int                   = sourceBitmapData.width;
            var height:int                  = sourceBitmapData.height;
            var sourceRect:Rectangle        = sourceBitmapData.rect;
            var destPoint:Point             = new Point(0, 0);

            _bitmapData = new BitmapData(width, height, true, 0);
            _bitmapData.copyPixels(sourceBitmapData, sourceRect, destPoint);
        }

        public function get bitmapData():BitmapData
        {
            return _bitmapData;
        }

        public function get repetition():String
        {
            return _repetition;
        }
    }
}
