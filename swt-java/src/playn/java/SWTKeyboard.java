package playn.java;

import playn.core.util.Callback;

public class SWTKeyboard extends JavaKeyboard {

  // TODO

  @Override
  public void getText(TextType textType, String label, String initVal, Callback<String> callback) {
    callback.onFailure(new Exception("TODO"));
  }
}
