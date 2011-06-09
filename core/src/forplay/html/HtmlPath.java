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
package forplay.html;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.JsArrayNumber;

import forplay.core.Asserts;
import forplay.core.Path;

class HtmlPath implements Path {

  private static final int CMD_ARC = 3;
  private static final int CMD_CLOSE = 4;
  private static final int CMD_LINE = 1;
  private static final int CMD_MOVE = 0;
  private static final int CMD_QUAD = 2;

  private JsArrayNumber list = JsArrayNumber.createArray().cast();

  @Override
  public void arcTo(float radius, float x, float y) {
    list.push(CMD_ARC);
    list.push(radius);
    list.push(x);
    list.push(y);
  }

  @Override
  public void close() {
    list.push(CMD_CLOSE);
  }

  @Override
  public void lineTo(float x, float y) {
    list.push(CMD_LINE);
    list.push(x);
    list.push(y);
  }

  @Override
  public void moveTo(float x, float y) {
    list.push(CMD_MOVE);
    list.push(x);
    list.push(y);
  }

  @Override
  public void quadraticCurveTo(float cpx, float cpy, float x, float y) {
    list.push(CMD_QUAD);
    list.push(cpx);
    list.push(cpy);
    list.push(x);
    list.push(y);
  }

  @Override
  public void reset() {
    list.setLength(0);
  }

  void replay(Context2d ctx) {
    ctx.beginPath();

    int len = list.length(), i = 0;
    double x = 0, y = 0;
    while (i < len) {
      switch ((int) list.get(i++)) {
        case CMD_MOVE: {
          x = list.get(i++);
          y = list.get(i++);
          ctx.moveTo(x, y);
          break;
        }
        case CMD_LINE: {
          x = list.get(i++);
          y = list.get(i++);
          ctx.lineTo(x, y);
          break;
        }
        case CMD_QUAD: {
          double cpx = list.get(i++);
          double cpy = list.get(i++);
          x = list.get(i++);
          y = list.get(i++);
          ctx.quadraticCurveTo(cpx, cpy, x, y);
          break;
        }
        case CMD_ARC: {
          double curX = x, curY = 0;
          double radius = list.get(i++);
          x = list.get(i++);
          y = list.get(i++);
          ctx.arcTo(curX, curY, x, y, radius);
          break;
        }
        case CMD_CLOSE: {
          ctx.closePath();
          break;
        }

        default:
          throw new AssertionError("Corrupt command list");
      }
    }
  }

  float[] getVertices() {
    int len = list.length();
    Asserts.check(len % 2 == 0);
    float[] vertices = new float[len];
    for (int v = 0; v < len;) {
      int cmd = (int) list.get(v);
      if (v == vertices.length -2) {
        Asserts.check(cmd == CMD_CLOSE);
      } else {
        Asserts.check(cmd == CMD_MOVE);
      }
      vertices[v] = (float) list.get(v + 1);
      vertices[v+1] = (float) list.get(v + 2);
    }
    return vertices;
  }

  void replay(Path path) {
    path.reset();

    int len = list.length(), i = 0;
    float x = 0, y = 0;
    while (i < len) {
      switch ((int) list.get(i++)) {
        case CMD_MOVE: {
          x = (float) list.get(i++);
          y = (float) list.get(i++);
          path.moveTo(x, y);
          break;
        }
        case CMD_LINE: {
          x = (float) list.get(i++);
          y = (float) list.get(i++);
          path.lineTo(x, y);
          break;
        }
        case CMD_QUAD: {
          float cpx = (float) list.get(i++);
          float cpy = (float) list.get(i++);
          x = (float) list.get(i++);
          y = (float) list.get(i++);
          path.quadraticCurveTo(cpx, cpy, x, y);
          break;
        }
        case CMD_ARC: {
          float radius = (float) list.get(i++);
          x = (float) list.get(i++);
          y = (float) list.get(i++);
          path.arcTo(x, y, radius);
          break;
        }
        case CMD_CLOSE: {
          path.close();
          break;
        }

        default:
          throw new AssertionError("Corrupt command list");
      }
    }

  }
}
