/**
 * Copyright 2010-2015 The PlayN Authors
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
package playn.android;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.InputType;
import android.widget.EditText;

import playn.core.*;
import react.RFuture;
import react.RPromise;

public class AndroidInput extends Input {

  private final AndroidPlatform plat;

  public AndroidInput (AndroidPlatform plat) {
    this.plat = plat;
  }

  @Override
  public boolean hasHardwareKeyboard() {
    return false; // TODO: return true for devices that have a hardware keyboard
  }

  @Override public RFuture<String> getText(final Keyboard.TextType ttype, final String label,
                                           final String initVal) {
    final RPromise<String> result = plat.deferredPromise();
    plat.activity.runOnUiThread(new Runnable() {
      public void run () {
        final AlertDialog.Builder alert = new AlertDialog.Builder(plat.activity);

        alert.setMessage(label);

        // Set an EditText view to get user input
        final EditText input = new EditText(plat.activity);
        final int inputType;
        switch (ttype) {
        case NUMBER:
            inputType = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED;
            break;
        case EMAIL:
            inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
            break;
        case URL:
            inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI;
            break;
        case DEFAULT:
        default:
            inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL;
            break;
        }
        input.setInputType(inputType);
        input.setText(initVal);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
            result.succeed(input.getText().toString());
          }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
            result.succeed(null);
          }
        });
        alert.show();
      }
    });
    return result;
  }
}
