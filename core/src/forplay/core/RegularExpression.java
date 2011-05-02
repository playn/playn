package forplay.core;

/**
 * TODO: flush out. way out.
 */
public interface RegularExpression {
  /**
   * Simple check to see if a regexp finds at least one match in the source
   * string.  NOTE: this method's semantics do NOT match the JDK's semantics for
   * String.matches.
   * @param regexp a regular expression
   * @param source a source string to match against
   * @return true if the regexp is found anywhere in the source, or false
   */
  boolean matches(String regexp, String source);
}
