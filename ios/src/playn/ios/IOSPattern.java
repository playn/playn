/**
 * Copyright 2012 The PlayN Authors
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
package playn.ios;

import cli.MonoTouch.CoreGraphics.CGColor;
import cli.MonoTouch.UIKit.UIColor;
import cli.MonoTouch.UIKit.UIImage;

import playn.core.Pattern;

class IOSPattern implements Pattern
{
  CGColor colorWithPattern;

  IOSPattern(IOSAbstractImage image) {
    // this is a circuitous route, but I'm not savvy enough to find a more direct one
    colorWithPattern = UIColor.FromPatternImage(new UIImage(image.cgImage())).get_CGColor();
  }
}
