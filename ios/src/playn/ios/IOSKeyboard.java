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

import cli.MonoTouch.UIKit.UIAlertView;
import cli.MonoTouch.UIKit.UIAlertViewStyle;
import cli.MonoTouch.UIKit.UIButtonEventArgs;
import cli.MonoTouch.UIKit.UIColor;
import cli.MonoTouch.UIKit.UIKeyboardType;
import cli.MonoTouch.UIKit.UIReturnKeyType;
import cli.MonoTouch.UIKit.UITextAutocapitalizationType;
import cli.MonoTouch.UIKit.UITextAutocorrectionType;
import cli.MonoTouch.UIKit.UITextField;
import cli.System.Drawing.RectangleF;
import playn.core.Keyboard;
import playn.core.PlayN;
import playn.core.util.Callback;

public class IOSKeyboard implements Keyboard
{
  @Override
  public void setListener(Listener listener) {
    PlayN.log().warn("iOS cannot generate keyboard events. Use Keyboard.getText() instead");
  }

  @Override
  public boolean hasHardwareKeyboard() {
    return false;
  }

  @Override
  public void getText(TextType textType, String label, String initVal,
      final Callback<String> callback) {
    UIAlertView view = new UIAlertView();
    view.set_Title(label);
    view.AddButton("Cancel");
    view.AddButton("OK");
    view.set_AlertViewStyle(UIAlertViewStyle.wrap(UIAlertViewStyle.PlainTextInput));

    final UITextField field = view.GetTextField(0);
    field.set_ReturnKeyType(UIReturnKeyType.wrap(UIReturnKeyType.Done));

    switch (textType) {
    case NUMBER: field.set_KeyboardType(UIKeyboardType.wrap(UIKeyboardType.NumberPad)); break;
    case EMAIL: field.set_KeyboardType(UIKeyboardType.wrap(UIKeyboardType.EmailAddress)); break;
    case URL: field.set_KeyboardType(UIKeyboardType.wrap(UIKeyboardType.Url)); break;
    case DEFAULT: field.set_KeyboardType(UIKeyboardType.wrap(UIKeyboardType.Default)); break;
    }

    // TODO: Customize these per getText() call?
    field.set_AutocorrectionType(UITextAutocorrectionType.wrap(UITextAutocorrectionType.No));
    field.set_AutocapitalizationType(
      UITextAutocapitalizationType.wrap(UITextAutocapitalizationType.None));
    field.set_SecureTextEntry(false); // TODO: if nothing else, can do this one as a TextType

    view.add_Clicked(new cli.System.EventHandler$$00601_$$$_Lcli__MonoTouch__UIKit__UIButtonEventArgs_$$$$_(
      new cli.System.EventHandler$$00601_$$$_Lcli__MonoTouch__UIKit__UIButtonEventArgs_$$$$_.Method() {
        @Override public void Invoke(Object sender, UIButtonEventArgs args) {
          // Cancel is at button index 0
          callback.onSuccess(args.get_ButtonIndex() == 0 ? null : field.get_Text());
        }
      }));

    view.Show();
  }
}
