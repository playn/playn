/**
 * Copyright 2014 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package playn.robovm;

import org.robovm.apple.coregraphics.CGPoint;
import org.robovm.apple.foundation.NSSet;
import org.robovm.apple.uikit.UIAlertView;
import org.robovm.apple.uikit.UIAlertViewDelegateAdapter;
import org.robovm.apple.uikit.UIAlertViewStyle;
import org.robovm.apple.uikit.UIEvent;
import org.robovm.apple.uikit.UIKeyboardType;
import org.robovm.apple.uikit.UIReturnKeyType;
import org.robovm.apple.uikit.UITextAutocapitalizationType;
import org.robovm.apple.uikit.UITextAutocorrectionType;
import org.robovm.apple.uikit.UITextField;
import org.robovm.apple.uikit.UITouch;

import playn.core.*;
import pythagoras.f.IPoint;
import react.RFuture;
import react.RPromise;

public class RoboInput extends Input {

  private final RoboPlatform plat;

  public RoboInput(RoboPlatform plat) {
    super(plat);
    this.plat = plat;
  }

  @Override public boolean hasTouch () { return true; }

  @Override public RFuture<String> getText(Keyboard.TextType textType, String label,
                                           String initVal) {
    final RPromise<String> result = plat.exec().deferredPromise();

    UIAlertView view = new UIAlertView();
    if (label != null)
      view.setTitle(label);
    view.addButton("Cancel");
    view.addButton("OK");
    view.setAlertViewStyle(UIAlertViewStyle.PlainTextInput);

    final UITextField field = view.getTextField(0);
    field.setReturnKeyType(UIReturnKeyType.Done);
    if (initVal != null) {
      field.setText(initVal);
    }

    switch (textType) {
      case  NUMBER: field.setKeyboardType(UIKeyboardType.NumberPad); break;
      case   EMAIL: field.setKeyboardType(UIKeyboardType.EmailAddress); break;
      case     URL: field.setKeyboardType(UIKeyboardType.URL); break;
      case DEFAULT: field.setKeyboardType(UIKeyboardType.Default); break;
    }

    // TODO: Customize these per getText() call?
    field.setAutocorrectionType(UITextAutocorrectionType.Yes);
    field.setAutocapitalizationType(UITextAutocapitalizationType.Sentences);
    field.setSecureTextEntry(false); // TODO: if nothing else, can do this one as a TextType

    view.setDelegate(new UIAlertViewDelegateAdapter() {
      public void clicked(UIAlertView view, long buttonIndex) {
        // Cancel is at button index 0
        result.succeed(buttonIndex == 0 ? null : field.getText());
      }
    });
    view.show();

    return result;
  }

  @Override public RFuture<Boolean> sysDialog(String title, String text, String ok, String cancel) {
    final RPromise<Boolean> result = plat.exec().deferredPromise();

    UIAlertView view = new UIAlertView();
    view.setTitle(title);
    view.setMessage(text);
    if (cancel != null) view.addButton(cancel);
    view.addButton(ok);
    view.setAlertViewStyle(UIAlertViewStyle.Default);

    view.setDelegate(new UIAlertViewDelegateAdapter() {
      public void clicked(UIAlertView view, long buttonIndex) {
        result.succeed(buttonIndex == 1);
      }
    });
    view.show();

    return result;
  }

  void onTouchesBegan(NSSet<UITouch> touches, UIEvent event) {
    plat.dispatchEvent(touchEvents, toEvents(touches, event, Touch.Event.Kind.START));
  }
  void onTouchesMoved(NSSet<UITouch> touches, UIEvent event) {
    plat.dispatchEvent(touchEvents, toEvents(touches, event, Touch.Event.Kind.MOVE));
  }
  void onTouchesEnded(NSSet<UITouch> touches, UIEvent event) {
    plat.dispatchEvent(touchEvents, toEvents(touches, event, Touch.Event.Kind.END));
  }
  void onTouchesCancelled(NSSet<UITouch> touches, UIEvent event) {
    plat.dispatchEvent(touchEvents, toEvents(touches, event, Touch.Event.Kind.CANCEL));
  }

  private Touch.Event[] toEvents (NSSet<UITouch> touches, UIEvent event, Touch.Event.Kind kind) {
    final Touch.Event[] events = new Touch.Event[touches.size()];
    int idx = 0;
    for (UITouch touch : touches) {
      CGPoint loc = touch.getLocationInView(touch.getView());
      // transform the point based on our current scale
      IPoint xloc = plat.graphics().transformTouch((float)loc.getX(), (float)loc.getY());
      // on iOS the memory address of the UITouch object is the unique id
      int id = (int)touch.getHandle();
      events[idx++] = new Touch.Event(0, touch.getTimestamp() * 1000, xloc.x(), xloc.y(), kind, id);
    }
    return events;
  }
}
