package forplay.html;

import com.google.gwt.regexp.shared.RegExp;

import forplay.core.RegularExpression;

public class HtmlRegularExpression implements RegularExpression {

  @Override
  public boolean matches(String regexp, String source) {
    return (RegExp.compile(regexp).exec(source) != null);
  }

}
