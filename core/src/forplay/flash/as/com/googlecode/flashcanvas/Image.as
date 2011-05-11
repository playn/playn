/*
 * FlashCanvas
 *
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
    import flash.display.Bitmap;
    import flash.display.BitmapData;
    import flash.display.Loader;
    import flash.display.LoaderInfo;
    import flash.events.ErrorEvent;
    import flash.events.Event;
    import flash.events.EventDispatcher;
    import flash.events.IOErrorEvent;
    import flash.net.URLRequest;
    import flash.utils.ByteArray;

    public class Image extends EventDispatcher
    {
        private var _src:String;
        private var _complete:Boolean = false;
        private var _bitmapData:BitmapData;

        public function set src(value:String):void
        {
            // If the same image is being loaded
            if (value == _src)
                return;

            // Initialize attributes
            _src        = value;
            _complete   = false;
            _bitmapData = null;

            // Create a Loader object
            var loader:Loader = new Loader();

            // Register event listeners
            loader.contentLoaderInfo.addEventListener(Event.COMPLETE, _completeHandler);
            loader.contentLoaderInfo.addEventListener(IOErrorEvent.IO_ERROR, _ioErrorHandler);

            if (value.slice(0, 11) == "data:image/")
            {
                // Decode data URI
                var data:String         = value.slice(value.indexOf(",") + 1);
                var byteArray:ByteArray = Base64.decode(data);
                loader.loadBytes(byteArray);
            }
            else
            {
                // If the file is in other domain
                if (/^https?:\/\//.test(value))
                {
                    // Rewrite the URL to load the file via a proxy script
                    value = Config.proxy + '?url=' + value;
                }

                // Load the image
                var request:URLRequest = new URLRequest(value);
                loader.load(request);
            }
        }

        private function _completeHandler(event:Event):void
        {
            // Remove the event listeners
            var loaderInfo:LoaderInfo = event.target as LoaderInfo;
            loaderInfo.removeEventListener(Event.COMPLETE, _completeHandler);
            loaderInfo.removeEventListener(IOErrorEvent.IO_ERROR, _ioErrorHandler);

            // Set BitmapData of the image
            _bitmapData = Bitmap(loaderInfo.content).bitmapData;

            // Set the complete attribute true
            _complete = true;

            // Fire a load event
            dispatchEvent(new Event("load"));

            // Release the memory
            loaderInfo.loader.unload();
        }

        private function _ioErrorHandler(event:IOErrorEvent):void
        {
            // Remove the event listeners
            var loaderInfo:LoaderInfo = event.target as LoaderInfo;
            loaderInfo.removeEventListener(Event.COMPLETE, _completeHandler);
            loaderInfo.removeEventListener(IOErrorEvent.IO_ERROR, _ioErrorHandler);

            // Fire an error event
            dispatchEvent(new ErrorEvent(ErrorEvent.ERROR));
        }

        public function get src():String
        {
            return _src;
        }

        public function get complete():Boolean
        {
            return _complete;
        }

        public function get bitmapData():BitmapData
        {
            return _bitmapData;
        }
    }
}
