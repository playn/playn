/**
 * Copyright 2010 The PlayN Authors
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

import com.google.gwt.canvas.dom.client.CanvasGradient;
import com.google.gwt.canvas.dom.client.Context2d;

import playn.core.Gradient;

public class HtmlGradient extends Gradient {

  final CanvasGradient gradient;

  public HtmlGradient (Context2d ctx, Config config) {
    if (config instanceof Linear) {
      Linear cfg = (Linear)config;
      gradient = ctx.createLinearGradient(cfg.x0, cfg.y0, cfg.x1, cfg.y1);
      for (int ii = 0; ii < cfg.colors.length; ++ii) {
        gradient.addColorStop(cfg.positions[ii], HtmlGraphics.cssColorString(cfg.colors[ii]));
      }
    } else if (config instanceof Radial) {
      Radial cfg = (Radial)config;
      gradient = ctx.createRadialGradient(cfg.x, cfg.y, 0, cfg.x, cfg.y, cfg.r);
      for (int ii = 0; ii < cfg.colors.length; ++ii) {
        gradient.addColorStop(cfg.positions[ii], HtmlGraphics.cssColorString(cfg.colors[ii]));
      }
    } else throw new IllegalArgumentException("?? " + config);
  }
}
