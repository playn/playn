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
    import flash.display.Bitmap;
    import flash.display.BitmapData;
    import flash.display.BlendMode;
    import flash.display.CapsStyle;
    import flash.display.Graphics;
    import flash.display.InterpolationMethod;
    import flash.display.JointStyle;
    import flash.display.LineScaleMode;
    import flash.display.Shape;
    import flash.display.SpreadMethod;
    import flash.events.ErrorEvent;
    import flash.events.Event;
    import flash.filters.GlowFilter;
    import flash.geom.ColorTransform;
    import flash.geom.Matrix;
    import flash.geom.Point;
    import flash.geom.Rectangle;
    import flash.text.TextField;
    import flash.text.TextFieldAutoSize;
    import flash.text.TextFormat;
    import flash.utils.ByteArray;

    public class CanvasRenderingContext2D
    {
        // back-reference to the canvas
        private var _canvas:Canvas;

        // vector shape
        private var shape:Shape;

        // clipping region
        private var clippingMask:Shape;

        // path commands and data
        private var path:Path;

        // first point of the current subpath
        private var startingPoint:Point;

        // last point of the current subpath
        private var currentPoint:Point;

        // stack of drawing states
        private var stateStack:Array = [];

        // drawing state
        private var state:State;

        // queue used in drawImage()
        private var taskQueue:Array = [];

        public function CanvasRenderingContext2D(canvas:Canvas)
        {
            _canvas = canvas;

            shape        = new Shape();
            clippingMask = new Shape();
            shape.mask   = clippingMask;

            path          = new Path();
            startingPoint = new Point();
            currentPoint  = new Point();

            state = new State();
        }

        public function resize(width:int, height:int):void
        {
            // initialize bitmapdata
            _canvas.resize(width, height);

            // initialize drawing states
            stateStack = [];
            state = new State();

            // draw initial clipping region
            beginPath();
            rect(0, 0, width, height);
            clip();

            // clear the current path
            beginPath();
        }

        /*
         * back-reference to the canvas
         */

        public function get canvas():Canvas
        {
            return _canvas;
        }

        /*
         * state
         */

        public function save():void
        {
            stateStack.push(state.clone());
        }

        public function restore():void
        {
            if (stateStack.length == 0)
                return;

            state = stateStack.pop();

            // redraw clipping image
            var graphics:Graphics = clippingMask.graphics;
            graphics.clear();
            graphics.beginFill(0x000000);
            state.clippingPath.draw(graphics);
            graphics.endFill();
        }

        /*
         * transformations
         */

        public function scale(x:Number, y:Number):void
        {
            if (isFinite(x) && isFinite(y))
            {
                var matrix:Matrix = state.transformMatrix.clone();
                state.transformMatrix.identity();
                state.transformMatrix.scale(x, y);
                state.transformMatrix.concat(matrix);

                state.lineScale *= Math.sqrt(Math.abs(x * y));
            }
        }

        public function rotate(angle:Number):void
        {
            if (isFinite(angle))
            {
                var matrix:Matrix = state.transformMatrix.clone();
                state.transformMatrix.identity();
                state.transformMatrix.rotate(angle);
                state.transformMatrix.concat(matrix);
            }
        }

        public function translate(x:Number, y:Number):void
        {
            if (isFinite(x) && isFinite(y))
            {
                var matrix:Matrix = state.transformMatrix.clone();
                state.transformMatrix.identity();
                state.transformMatrix.translate(x, y);
                state.transformMatrix.concat(matrix);
            }
        }

        public function transform(m11:Number, m12:Number, m21:Number, m22:Number, dx:Number, dy:Number):void
        {
            if (isFinite(m11) && isFinite(m21) && isFinite(dx) &&
                isFinite(m12) && isFinite(m22) && isFinite(dy))
            {
                var matrix:Matrix = state.transformMatrix.clone();
                state.transformMatrix = new Matrix(m11, m12, m21, m22, dx, dy);
                state.transformMatrix.concat(matrix);

                state.lineScale *= Math.sqrt(Math.abs(m11 * m22 - m12 * m21));
            }
        }

        public function setTransform(m11:Number, m12:Number, m21:Number, m22:Number, dx:Number, dy:Number):void
        {
            if (isFinite(m11) && isFinite(m21) && isFinite(dx) &&
                isFinite(m12) && isFinite(m22) && isFinite(dy))
            {
                state.transformMatrix = new Matrix(m11, m12, m21, m22, dx, dy);

                state.lineScale = Math.sqrt(Math.abs(m11 * m22 - m12 * m21));
            }
        }

        /*
         * compositing
         */

        public function get globalAlpha():Number
        {
            return state.globalAlpha;
        }

        public function set globalAlpha(value:Number):void
        {
            if (isFinite(value) && 0.0 <= value && value <= 1.0)
                state.globalAlpha = value;
        }

        public function get globalCompositeOperation():String
        {
            return state.globalCompositeOperation;
        }

        public function set globalCompositeOperation(value:String):void
        {
            state.globalCompositeOperation = value;
        }

        /*
         * colors and styles
         */

        public function get strokeStyle():*
        {
            if (state.strokeStyle is CSSColor)
                return state.strokeStyle.toString();
            else
                return state.strokeStyle;
        }

        public function set strokeStyle(value:*):void
        {
            if (value is String)
            {
                try
                {
                    state.strokeStyle = new CSSColor(value);
                }
                catch (e:ArgumentError)
                {
                    // Ignore the value
                }
            }
            else if (value is CanvasGradient || value is CanvasPattern)
            {
                state.strokeStyle = value;
            }
        }

        public function get fillStyle():*
        {
            if (state.fillStyle is CSSColor)
                return state.fillStyle.toString();
            else
                return state.fillStyle;
        }

        public function set fillStyle(value:*):void
        {
            if (value is String)
            {
                try
                {
                    state.fillStyle = new CSSColor(value);
                }
                catch (e:ArgumentError)
                {
                    // Ignore the value
                }
            }
            else if (value is CanvasGradient || value is CanvasPattern)
            {
                state.fillStyle = value;
            }
        }

        public function createLinearGradient(x0:Number, y0:Number, x1:Number, y1:Number):LinearGradient
        {
            return new LinearGradient(x0, y0, x1, y1);
        }

        public function createRadialGradient(x0:Number, y0:Number, r0:Number, x1:Number, y1:Number, r1:Number):RadialGradient
        {
            return new RadialGradient(x0, y0, r0, x1, y1, r1);
        }

        public function createPattern(image:*, repetition:String):CanvasPattern
        {
            return new CanvasPattern(image, repetition);
        }

        /*
         * line caps/joins
         */

        public function get lineWidth():Number
        {
            return state.lineWidth;
        }

        public function set lineWidth(value:Number):void
        {
            if (isFinite(value) && value > 0)
                state.lineWidth = value;
        }

        public function get lineCap():String
        {
            if (state.lineCap == CapsStyle.NONE)
                return "butt";
            else if (state.lineCap == CapsStyle.ROUND)
                return "round";
            else
                return "square";
        }

        public function set lineCap(value:String):void
        {
            if (value == "butt")
                state.lineCap = CapsStyle.NONE;
            else if (value == "round")
                state.lineCap = CapsStyle.ROUND;
            else if (value == "square")
                state.lineCap = CapsStyle.SQUARE;
        }

        public function get lineJoin():String
        {
            if (state.lineJoin == JointStyle.BEVEL)
                return "bevel";
            else if (state.lineJoin == JointStyle.ROUND)
                return "round";
            else
                return "miter";
        }

        public function set lineJoin(value:String):void
        {
            if (value == "bevel")
                state.lineJoin = JointStyle.BEVEL;
            else if (value == "round")
                state.lineJoin = JointStyle.ROUND;
            else if (value == "miter")
                state.lineJoin = JointStyle.MITER;
        }

        public function get miterLimit():Number
        {
            return state.miterLimit;
        }

        public function set miterLimit(value:Number):void
        {
            if (isFinite(value) && value > 0)
                state.miterLimit = value;
        }

        /*
         * shadows
         */

        public function get shadowOffsetX():Number
        {
            return state.shadowOffsetX;
        }

        public function set shadowOffsetX(value:Number):void
        {
            state.shadowOffsetX = value;
        }

        public function get shadowOffsetY():Number
        {
            return state.shadowOffsetY;
        }

        public function set shadowOffsetY(value:Number):void
        {
            state.shadowOffsetY = value;
        }

        public function get shadowBlur():Number
        {
            return state.shadowBlur;
        }

        public function set shadowBlur(value:Number):void
        {
            state.shadowBlur = value;
        }

        public function get shadowColor():String
        {
            return state.shadowColor.toString();
        }

        public function set shadowColor(value:String):void
        {
            try
            {
                state.shadowColor = new CSSColor(value);
            }
            catch (e:ArgumentError)
            {
                // Ignore the value
            }
        }

        /*
         * rects
         */

        public function clearRect(x:Number, y:Number, w:Number, h:Number):void
        {
            if (!isFinite(x) || !isFinite(y) || !isFinite(w) || !isFinite(h))
                return;

            var graphics:Graphics = shape.graphics;

            graphics.beginFill(0x000000);
            graphics.drawRect(x, y, w, h);
            graphics.endFill();

            _canvas.bitmapData.draw(shape, state.transformMatrix, null, BlendMode.ERASE);

            graphics.clear();
        }

        public function fillRect(x:Number, y:Number, w:Number, h:Number):void
        {
            if (!isFinite(x) || !isFinite(y) || !isFinite(w) || !isFinite(h))
                return;

            var p1:Point = _getTransformedPoint(x, y);
            var p2:Point = _getTransformedPoint(x + w, y);
            var p3:Point = _getTransformedPoint(x + w, y + h);
            var p4:Point = _getTransformedPoint(x, y + h);

            var graphics:Graphics = shape.graphics;

            _setFillStyle(graphics);
            graphics.moveTo(p1.x, p1.y);
            graphics.lineTo(p2.x, p2.y);
            graphics.lineTo(p3.x, p3.y);
            graphics.lineTo(p4.x, p4.y);
            graphics.lineTo(p1.x, p1.y);
            graphics.endFill();

            _renderShape();
        }

        public function strokeRect(x:Number, y:Number, w:Number, h:Number):void
        {
            if (!isFinite(x) || !isFinite(y) || !isFinite(w) || !isFinite(h))
                return;

            var p1:Point = _getTransformedPoint(x, y);
            var p2:Point = _getTransformedPoint(x + w, y);
            var p3:Point = _getTransformedPoint(x + w, y + h);
            var p4:Point = _getTransformedPoint(x, y + h);

            var graphics:Graphics = shape.graphics;

            _setStrokeStyle(graphics);
            graphics.moveTo(p1.x, p1.y);
            graphics.lineTo(p2.x, p2.y);
            graphics.lineTo(p3.x, p3.y);
            graphics.lineTo(p4.x, p4.y);
            graphics.lineTo(p1.x, p1.y);

            _renderShape();
        }

        /*
         * path API
         */

        public function beginPath():void
        {
            path.initialize();
        }

        public function closePath():void
        {
            if (path.commands.length == 0)
                return;

            path.commands.push(GraphicsPathCommand.LINE_TO);
            path.data.push(startingPoint.x, startingPoint.y);

            currentPoint.x = startingPoint.x;
            currentPoint.y = startingPoint.y;
        }

        public function moveTo(x:Number, y:Number):void
        {
            if (!isFinite(x) || !isFinite(y))
                return;

            var p:Point = _getTransformedPoint(x, y);

            path.commands.push(GraphicsPathCommand.MOVE_TO);
            path.data.push(p.x, p.y);

            startingPoint.x = currentPoint.x = p.x;
            startingPoint.y = currentPoint.y = p.y;
        }

        public function lineTo(x:Number, y:Number):void
        {
            if (!isFinite(x) || !isFinite(y))
                return;

            // check that path contains subpaths
            if (path.commands.length == 0)
                moveTo(x, y);

            var p:Point = _getTransformedPoint(x, y);

            path.commands.push(GraphicsPathCommand.LINE_TO);
            path.data.push(p.x, p.y);

            currentPoint.x = p.x;
            currentPoint.y = p.y;
        }

        public function quadraticCurveTo(cpx:Number, cpy:Number, x:Number, y:Number):void
        {
            if (!isFinite(cpx) || !isFinite(cpy) || !isFinite(x) || !isFinite(y))
                return;

            // check that path contains subpaths
            if (path.commands.length == 0)
                moveTo(cpx, cpy);

            var cp:Point = _getTransformedPoint(cpx, cpy);
            var  p:Point = _getTransformedPoint(x, y);

            path.commands.push(GraphicsPathCommand.CURVE_TO);
            path.data.push(cp.x, cp.y, p.x, p.y);

            currentPoint.x = p.x;
            currentPoint.y = p.y;
        }

        /*
         * Cubic bezier curve is approximated by four quadratic bezier curves.
         * The approximation uses MidPoint algorithm by Timothee Groleau.
         *
         * @see http://www.timotheegroleau.com/Flash/articles/cubic_bezier_in_flash.htm
         */
        public function bezierCurveTo(cp1x:Number, cp1y:Number, cp2x:Number, cp2y:Number, x:Number, y:Number):void
        {
            if (!isFinite(cp1x) || !isFinite(cp1y) || !isFinite(cp2x) || !isFinite(cp2y) || !isFinite(x) || !isFinite(y))
                return;

            // check that path contains subpaths
            if (path.commands.length == 0)
                moveTo(cp1x, cp1y);

            var p0:Point = currentPoint;
            var p1:Point = _getTransformedPoint(cp1x, cp1y);
            var p2:Point = _getTransformedPoint(cp2x, cp2y);
            var p3:Point = _getTransformedPoint(x, y);

            // calculate base points
            var bp1:Point = Point.interpolate(p0, p1, 0.25);
            var bp2:Point = Point.interpolate(p3, p2, 0.25);

            // get 1/16 of the [p3, p0] segment
            var dx:Number = (p3.x - p0.x) / 16;
            var dy:Number = (p3.y - p0.y) / 16;

            // calculate control points
            var cp1:Point = Point.interpolate( p1,  p0, 0.375);
            var cp2:Point = Point.interpolate(bp2, bp1, 0.375);
            var cp3:Point = Point.interpolate(bp1, bp2, 0.375);
            var cp4:Point = Point.interpolate( p2,  p3, 0.375);
            cp2.x -= dx;
            cp2.y -= dy;
            cp3.x += dx;
            cp3.y += dy;

            // calculate anchor points
            var ap1:Point = Point.interpolate(cp1, cp2, 0.5);
            var ap2:Point = Point.interpolate(bp1, bp2, 0.5);
            var ap3:Point = Point.interpolate(cp3, cp4, 0.5);

            // four quadratic subsegments
            path.commands.push(
                GraphicsPathCommand.CURVE_TO,
                GraphicsPathCommand.CURVE_TO,
                GraphicsPathCommand.CURVE_TO,
                GraphicsPathCommand.CURVE_TO
            );
            path.data.push(
                cp1.x, cp1.y, ap1.x, ap1.y,
                cp2.x, cp2.y, ap2.x, ap2.y,
                cp3.x, cp3.y, ap3.x, ap3.y,
                cp4.x, cp4.y,  p3.x,  p3.y
            );

            currentPoint.x = p3.x;
            currentPoint.y = p3.y;
        }

        /*
         * arcTo() is decomposed into lineTo() and arc().
         *
         * @see http://d.hatena.ne.jp/mindcat/20100131/1264958828
         */
        public function arcTo(x1:Number, y1:Number, x2:Number, y2:Number, radius:Number):void
        {
            if (!isFinite(x1) || !isFinite(y1) || !isFinite(x2) || !isFinite(y2) || !isFinite(radius))
                return;

            // check that path contains subpaths
            if (path.commands.length == 0)
                moveTo(x1, y1);

            var p0:Point  = _getUntransformedPoint(currentPoint.x, currentPoint.y);
            var a1:Number = p0.y - y1;
            var b1:Number = p0.x - x1;
            var a2:Number = y2   - y1;
            var b2:Number = x2   - x1;
            var mm:Number = Math.abs(a1 * b2 - b1 * a2);

            if (mm < 1.0e-8 || radius === 0)
            {
                lineTo(x1, y1);
            }
            else
            {
                var dd:Number = a1 * a1 + b1 * b1;
                var cc:Number = a2 * a2 + b2 * b2;
                var tt:Number = a1 * a2 + b1 * b2;
                var k1:Number = radius * Math.sqrt(dd) / mm;
                var k2:Number = radius * Math.sqrt(cc) / mm;
                var j1:Number = k1 * tt / dd;
                var j2:Number = k2 * tt / cc;
                var cx:Number = k1 * b2 + k2 * b1;
                var cy:Number = k1 * a2 + k2 * a1;
                var px:Number = b1 * (k2 + j1);
                var py:Number = a1 * (k2 + j1);
                var qx:Number = b2 * (k1 + j2);
                var qy:Number = a2 * (k1 + j2);
                var startAngle:Number = Math.atan2(py - cy, px - cx);
                var endAngle:Number   = Math.atan2(qy - cy, qx - cx);

                lineTo(px + x1, py + y1);
                arc(cx + x1, cy + y1, radius, startAngle, endAngle, b1 * a2 > b2 * a1);
            }
        }

        public function rect(x:Number, y:Number, w:Number, h:Number):void
        {
            if (!isFinite(x) || !isFinite(y) || !isFinite(w) || !isFinite(h))
                return;

            var p1:Point = _getTransformedPoint(x, y);
            var p2:Point = _getTransformedPoint(x + w, y);
            var p3:Point = _getTransformedPoint(x + w, y + h);
            var p4:Point = _getTransformedPoint(x, y + h);

            path.commands.push(
                GraphicsPathCommand.MOVE_TO,
                GraphicsPathCommand.LINE_TO,
                GraphicsPathCommand.LINE_TO,
                GraphicsPathCommand.LINE_TO,
                GraphicsPathCommand.LINE_TO
            );
            path.data.push(
                p1.x, p1.y,
                p2.x, p2.y,
                p3.x, p3.y,
                p4.x, p4.y,
                p1.x, p1.y
            );

            startingPoint.x = currentPoint.x = p1.x;
            startingPoint.y = currentPoint.y = p1.y;
        }

        /*
         * Arc is approximated by quadratic bezier curves.
         */
        public function arc(x:Number, y:Number, radius:Number, startAngle:Number, endAngle:Number, anticlockwise:Boolean = false):void
        {
            if (!isFinite(x) || !isFinite(y) || !isFinite(radius) ||
                !isFinite(startAngle) || !isFinite(endAngle))
                return;

            var startX:Number = x + radius * Math.cos(startAngle);
            var startY:Number = y + radius * Math.sin(startAngle);

            // check that path contains subpaths
            if (path.commands.length == 0)
                moveTo(startX, startY);
            else
                lineTo(startX, startY);

            if (startAngle == endAngle)
                return;

            var theta:Number = endAngle - startAngle;
            var PI2:Number   = Math.PI * 2;

            if (anticlockwise)
            {
                if (theta <= -PI2)
                    theta = PI2;
                else while (theta >= 0)
                    theta -= PI2;
            }
            else
            {
                if (theta >= PI2)
                    theta = PI2;
                else while (theta <= 0)
                    theta += PI2;
            }

            var angle:Number     = startAngle;
            var segments:Number  = Math.ceil(Math.abs(theta) / (Math.PI / 4));
            var delta:Number     = theta / (segments * 2);
            var radiusMid:Number = radius / Math.cos(delta);

            for (var i:int = 0; i < segments; i++)
            {
                angle += delta;
                var cpx:Number = x + Math.cos(angle) * radiusMid;
                var cpy:Number = y + Math.sin(angle) * radiusMid;
                var cp:Point   = _getTransformedPoint(cpx, cpy);

                angle += delta;
                var apx:Number = x + Math.cos(angle) * radius;
                var apy:Number = y + Math.sin(angle) * radius;
                var ap:Point   = _getTransformedPoint(apx, apy);

                path.commands.push(GraphicsPathCommand.CURVE_TO);
                path.data.push(cp.x, cp.y, ap.x, ap.y);
            }

            if (theta == PI2)
            {
                var endX:Number = x + radius * Math.cos(endAngle);
                var endY:Number = y + radius * Math.sin(endAngle);
                moveTo(endX, endY);
            }
            else
            {
                currentPoint.x = ap.x;
                currentPoint.y = ap.y;
            }
        }

        public function fill():void
        {
            var graphics:Graphics = shape.graphics;
            _setFillStyle(graphics);
            path.draw(graphics);
            graphics.endFill();
            _renderShape();
        }

        public function stroke():void
        {
            var graphics:Graphics = shape.graphics;
            _setStrokeStyle(graphics);
            path.draw(graphics);
            _renderShape();
        }

        public function clip():void
        {
            // extract path
            state.clippingPath = path.clone();

            // draw paths
            var graphics:Graphics = clippingMask.graphics;
            graphics.clear();
            graphics.beginFill(0x000000);
            path.draw(graphics);
            graphics.endFill();
        }

        public function isPointInPath(x:Number, y:Number):*
        {
            // TODO: Implement
        }

        /*
         * text
         */

        public function get font():*
        {
            return state.font;
        }

        public function set font(value:String):void
        {
            state.font = value;
        }

        private function _parseFont():TextFormat
        {
            var format:TextFormat = new TextFormat;
            var fontData:Array = state.font.split(" ");

            format.italic = fontData[0] == "italic";
            format.size = parseFloat(fontData[2]);
            format.font = fontData.slice(3).join(" ").replace(/["']/g, "");

            var weight:Number = parseInt(fontData[1]);
            format.bold = (!isNaN(weight) && weight > 400 || fontData[1] == "bold");

            return format;
        }

        public function get textAlign():*
        {
            return state.textAlign;
        }

        public function set textAlign(value:String):void
        {
            switch (value)
            {
                case "start":
                case "end":
                case "left":
                case "right":
                case "center":
                    state.textAlign = value;
            }
        }

        public function get textBaseline():*
        {
            return state.textBaseline;
        }

        public function set textBaseline(value:String):void
        {
            switch (value)
            {
                case "top":
                case "hanging":
                case "middle":
                case "alphabetic":
                case "ideographic":
                case "bottom":
                    state.textBaseline = value;
            }
        }

        public function fillText(text:String, x:Number, y:Number, maxWidth:Number = Infinity):void
        {
            _renderText(text, x, y, maxWidth);
        }

        public function strokeText(text:String, x:Number, y:Number, maxWidth:Number = Infinity):void
        {
            _renderText(text, x, y, maxWidth, true);
        }

        public function measureText():*
        {
            // TODO: Implement
        }

        /*
         * drawing images
         */

        public function drawImage(image:Image, ...args:Array):void
        {
            var argc:int = args.length;

            if (!(argc == 2 && isFinite(args[0]) && isFinite(args[1]) ||
                  argc == 4 && isFinite(args[0]) && isFinite(args[1])
                            && isFinite(args[2]) && isFinite(args[3]) ||
                  argc == 8 && isFinite(args[0]) && isFinite(args[1])
                            && isFinite(args[2]) && isFinite(args[3])
                            && isFinite(args[4]) && isFinite(args[5])
                            && isFinite(args[6]) && isFinite(args[7])))
                return;

            // If the image is ready for use
            if (image.complete)
            {
                // Render the image immediately
                _renderImage(image.bitmapData, args);
            }

            // If the image is not yet ready
            else
            {
                // Enqueue the task
                taskQueue.push({
                    image: image,
                    args:  args,
                    state: state.clone()
                });

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

            // Process the tasks in order
            while (taskQueue.length > 0)
            {
                // Get the next image object
                image = taskQueue[0].image;

                // If the BitmapData is not ready, we defer the execution of
                // the remaining tasks.
                if (!image.complete)
                    return;

                // Dequeue a task object
                var task:Object = taskQueue.shift();

                // Render the image
                var args:Array  = task.args;
                var state:State = task.state;
                _renderImage(image.bitmapData, args, state);
            }
        }

        private function _errorHandler(event:ErrorEvent):void
        {
            // Remove tasks for the image which made an error.
            for (var i:int = taskQueue.length - 1; i >= 0; i--)
            {
                if (taskQueue[i].image == event.target)
                    taskQueue.splice(i, 1);
            }

            // Process the remaining tasks in the queue.
            _loadHandler(event);
        }

        /*
         * pixel manipulation
         */

        public function createImageData():ImageData
        {
            // TODO: Implement
            return new ImageData;
        }

        public function getImageData(sx:Number, sy:Number, sw:Number, sh:Number):ImageData
        {
            // TODO: Implement
            return new ImageData;
        }

        public function putImageData(data:ImageData, dx:Number, dy:Number, dirtyX:Number, dirtyY:Number, dirtyWidth:Number, dirtyHeight:Number):void
        {
            // TODO: Implement
        }

        /*
         * private methods
         */

        private function _getTransformedPoint(x:Number, y:Number):Point
        {
            return state.transformMatrix.transformPoint(new Point(x, y));
        }

        private function _getUntransformedPoint(x:Number, y:Number):Point
        {
            var matrix:Matrix = state.transformMatrix.clone();
            matrix.invert();
            return matrix.transformPoint(new Point(x, y));
        }

        private function _setStrokeStyle(graphics:Graphics):void
        {
            var style:Object         = state.strokeStyle;
            var thickness:Number     = state.lineWidth * state.lineScale;
            var color:uint           = 0x000000;
            var alpha:Number         = 0.0;
            var pixelHinting:Boolean = true;

            if (style is CSSColor)
            {
                color = style.color;
                alpha = style.alpha * state.globalAlpha;

                if (thickness < 1)
                    alpha *= thickness;
            }

            graphics.lineStyle(thickness, color, alpha, pixelHinting, LineScaleMode.NORMAL, state.lineCap, state.lineJoin, state.miterLimit);

            if (style is CanvasGradient)
            {
                var alphas:Array = style.alphas;

                // When there are no stops, the gradient is transparent black.
                if (alphas.length == 0)
                    return;

                if (state.globalAlpha < 1)
                {
                    for (var i:int = 0, n:int = alphas.length; i < n; i++)
                    {
                        alphas[i] *= state.globalAlpha;
                    }
                }

                var matrix:Matrix = style.matrix.clone();
                matrix.concat(state.transformMatrix);

                graphics.lineGradientStyle(style.type, style.colors, alphas, style.ratios, matrix, SpreadMethod.PAD, InterpolationMethod.RGB, style.focalPointRatio);
            }
            else if (style is CanvasPattern)
            {
                // Flash 9 does not support this API.
            }
        }

        private function _setFillStyle(graphics:Graphics):void
        {
            // disable stroke
            graphics.lineStyle();

            var style:Object = state.fillStyle;

            if (style is CSSColor)
            {
                var color:uint   = style.color;
                var alpha:Number = style.alpha * state.globalAlpha;
                graphics.beginFill(color, alpha);
            }
            else if (style is CanvasGradient)
            {
                var alphas:Array = style.alphas;

                // When there are no stops, the gradient is transparent black.
                if (alphas.length == 0)
                {
                    graphics.beginFill(0x000000, 0.0);
                    return;
                }

                if (state.globalAlpha < 1)
                {
                    for (var i:int = 0, n:int = alphas.length; i < n; i++)
                    {
                        alphas[i] *= state.globalAlpha;
                    }
                }

                var matrix:Matrix = style.matrix.clone();
                matrix.concat(state.transformMatrix);

                graphics.beginGradientFill(style.type, style.colors, alphas, style.ratios, matrix, SpreadMethod.PAD, InterpolationMethod.RGB, style.focalPointRatio);
            }
            else if (style is CanvasPattern)
            {
                var bitmap:BitmapData = style.bitmapData;

                if (!bitmap)
                {
                    graphics.beginFill(0x000000, state.globalAlpha);
                    return;
                }

                if (state.globalAlpha < 1)
                {
                    var colorTransform:ColorTransform =
                        new ColorTransform(1, 1, 1, state.globalAlpha);

                    // Make a translucent BitmapData
                    bitmap = style.bitmapData.clone();
                    bitmap.colorTransform(bitmap.rect, colorTransform);
                }

                // TODO: support repetition other than 'repeat'.
                graphics.beginBitmapFill(bitmap, state.transformMatrix);
            }
        }

        private function _renderShape():void
        {
            _canvas.bitmapData.draw(shape);
            shape.graphics.clear();
        }

        private function _renderText(text:String, x:Number, y:Number, maxWidth:Number, isStroke:Boolean = false):void
        {
            if (/^\s*$/.test(text))
                return;

            if (!isFinite(x) || !isFinite(y) || isNaN(maxWidth))
                return;

            // If maxWidth is less than or equal to zero, return without doing
            // anything.
            if (maxWidth <= 0)
                return;

            var textFormat:TextFormat = _parseFont();

            var style:Object = isStroke ? state.strokeStyle : state.fillStyle;

            // Set text color
            if (style is CSSColor)
                textFormat.color = style.color;

            // Create TextField object
            var textField:TextField     = new TextField();
            textField.autoSize          = TextFieldAutoSize.LEFT;
            textField.defaultTextFormat = textFormat;
            textField.text              = text.replace(/[\t\n\f\r]/g, " ");

            // Get the size of the text
            var width:int  = textField.textWidth;
            var height:int = textField.textHeight;
            var ascent:int = textField.getLineMetrics(0).ascent;

            // Remove 2px margins around the text
            var matrix:Matrix = new Matrix();
            matrix.translate(-2, -2);

            if (isStroke)
            {
                // Draw an outline of the text
                var color:uint = style is CSSColor ? style.color : 0x000000;
                var glowFilter:GlowFilter =
                    new GlowFilter(color, 1.0, 2, 2, 8, 1, true, true);
                textField.filters = [glowFilter];
            }

            // Convert the text into BitmapData
            var bitmapData:BitmapData = new BitmapData(width, height, true, 0);
            bitmapData.draw(textField, matrix);

            // Adjust x coordinates
            switch (state.textAlign)
            {
                case "start": if (_canvas.dir == "rtl") x -= width; break;
                case "end": if (_canvas.dir != "rtl") x -= width; break;
                case "left": break;
                case "right": x -= width; break;
                case "center": x -= width / 2; break;
            }

            // Adjust y coordinates
            switch (state.textBaseline)
            {
                case "top":
                case "hanging": break;
                case "middle": y -= height / 2; break;
                case "alphabetic":
                case "ideographic": y -= ascent; break;
                case "bottom": y -= height; break;
            }

            // Create transformation matrix
            matrix = new Matrix();
            matrix.translate(x, y);
            matrix.concat(state.transformMatrix);

            // Calculate alpha multiplier
            var alpha:Number = state.globalAlpha;
            if (style is CSSColor)
                alpha *= style.alpha;

            var colorTransform:ColorTransform = null;
            if (alpha < 1)
            {
                // Make the BitmapData translucent
                colorTransform = new ColorTransform(1, 1, 1, alpha);
            }

            // Render the BitmapData to the Canvas
            _canvas.bitmapData.draw(bitmapData, matrix, colorTransform, null, null, true);

            // Release the memory
            bitmapData.dispose();
        }

        public function _renderImage(bitmapData:BitmapData, args:Array, state:State = null):void
        {
            // Get the drawing state at the time drawImage() was called
            state = state || this.state;

            var sx:Number;
            var sy:Number;
            var sw:Number;
            var sh:Number;
            var dx:Number;
            var dy:Number;
            var dw:Number;
            var dh:Number;

            if (args.length == 8)
            {
                // Define the source and destination rectangles
                sx = args[0];
                sy = args[1];
                sw = args[2];
                sh = args[3];
                dx = args[4];
                dy = args[5];
                dw = args[6];
                dh = args[7];

                if (sw < 0)
                {
                    sx += sw;
                    sw = -sw;
                }
                if (sh < 0)
                {
                    sy += sh;
                    sh = -sh;
                }
            }
            else
            {
                // Use whole of the image as a source
                sx = 0;
                sy = 0;
                sw = bitmapData.width;
                sh = bitmapData.height;
                dx = args[0];
                dy = args[1];
                dw = args[2] || sw;
                dh = args[3] || sh;
            }

            if (dw < 0)
            {
                dx += dw;
                dw = -dw;
            }
            if (dh < 0)
            {
                dy += dh;
                dh = -dh;
            }

            // Clip the region within the source rectangle
            var source:BitmapData    = new BitmapData(sw, sh, true, 0);
            var sourceRect:Rectangle = new Rectangle(sx, sy, sw, sh);
            var destPoint:Point      = new Point();
            source.copyPixels(bitmapData, sourceRect, destPoint);

            // Create transformation matrix
            var matrix:Matrix = new Matrix();
            matrix.scale(dw / sw, dh / sh);
            matrix.translate(dx, dy);
            matrix.concat(state.transformMatrix);

            var colorTransform:ColorTransform = null;
            if (state.globalAlpha < 1)
            {
                // Make the image translucent
                colorTransform = new ColorTransform(1, 1, 1, state.globalAlpha);
            }

            // Render the image to the Canvas
            _canvas.bitmapData.draw(source, matrix, colorTransform, null, null, true);

            // Release the memory
            source.dispose();
        }
    }
}
