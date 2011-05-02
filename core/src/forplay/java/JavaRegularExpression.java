package forplay.java;

import java.util.regex.Pattern;

import forplay.core.RegularExpression;

public class JavaRegularExpression implements RegularExpression {

  /**
   * Equivalent to Pattern.compile(regexp).matcher(source).find()
   */
  @Override
  public boolean matches(String regexp, String source) {
    return Pattern.compile(regexp).matcher(source).find();
  }

}
