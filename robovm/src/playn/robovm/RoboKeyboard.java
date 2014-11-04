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

import org.robovm.apple.uikit.UIAlertView;
import org.robovm.apple.uikit.UIAlertViewDelegateAdapter;
import org.robovm.apple.uikit.UIAlertViewStyle;
import org.robovm.apple.uikit.UIKeyboardType;
import org.robovm.apple.uikit.UIReturnKeyType;
import org.robovm.apple.uikit.UITextAutocapitalizationType;
import org.robovm.apple.uikit.UITextAutocorrectionType;
import org.robovm.apple.uikit.UITextField;

import playn.core.KeyboardImpl;
import playn.core.util.Callback;

public class RoboKeyboard extends KeyboardImpl {

  private final RoboPlatform platform;

  public RoboKeyboard(RoboPlatform platform) {
    this.platform = platform;
  }

  @Override
  public boolean hasHardwareKeyboard() {
    return false;
  }

  @Override
  public void getText(TextType textType, String label, String initVal,
      final Callback<String> callback) {
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
    case NUMBER:   field.setKeyboardType(UIKeyboardType.NumberPad); break;
    case EMAIL:   field.setKeyboardType(UIKeyboardType.EmailAddress); break;
    case URL:     field.setKeyboardType(UIKeyboardType.URL); break;
    case DEFAULT: field.setKeyboardType(UIKeyboardType.Default); break;
    }

    // TODO: Customize these per getText() call?
    field.setAutocorrectionType(UITextAutocorrectionType.Yes);
    field.setAutocapitalizationType(UITextAutocapitalizationType.Sentences);
    field.setSecureTextEntry(false); // TODO: if nothing else, can do this one as a TextType

    view.setDelegate(new UIAlertViewDelegateAdapter() {
      public void clicked(UIAlertView view, long buttonIndex) {
        // Cancel is at button index 0
        platform.notifySuccess(callback, buttonIndex == 0 ? null : field.getText());
      }
    });
    view.show();
  }
}
