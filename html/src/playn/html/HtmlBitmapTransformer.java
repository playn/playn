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
package playn.html;

import com.google.gwt.dom.client.ImageElement;

import playn.core.Image;

/**
 * Enables the transformation of Html image bitmaps.
 */
public interface HtmlBitmapTransformer extends Image.BitmapTransformer
{
  /**
   * Transforms the supplied bitmap into a new bitmap which will be used as the source data for a
   * new PlayN image. <em>Do not</em> modify the bitmap passed into this method or you will break
   * things.
   */
  ImageElement transform(ImageElement image);
}
