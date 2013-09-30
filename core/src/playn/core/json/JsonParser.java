/**
 * Copyright 2011 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package playn.core.json;

import java.math.BigInteger;

/**
 * Simple JSON parser.
 *
 * <pre>
 * Object json = {@link JsonParser}.any().from("{\"a\":[true,false], \"b\":1}");
 * Number json = ({@link Number}){@link JsonParser}.any().from("123.456e7");
 * JsonObject json = {@link JsonParser}.object().from("{\"a\":[true,false], \"b\":1}");
 * JsonArray json = {@link JsonParser}.array().from("[1, {\"a\":[true,false], \"b\":1}]");
 * </pre>
 */
final class JsonParser {
  private int linePos = 1, rowPos, charOffset, utf8adjust;
  private int tokenLinePos, tokenCharPos, tokenCharOffset;
  private Object value;
  private Token token;
  private StringBuilder reusableBuffer = new StringBuilder();

  private boolean eof;
  private int index;
  private String string;
  private int bufferLength;

  private static final char[] TRUE = { 'r', 'u', 'e' };
  private static final char[] FALSE = { 'a', 'l', 's', 'e' };
  private static final char[] NULL = { 'u', 'l', 'l' };

  /**
   * The tokens available in JSON.
   */
  private enum Token {
    EOF(false), NULL(true), TRUE(true), FALSE(true), STRING(true), NUMBER(true), COMMA(false), COLON(false), //
    OBJECT_START(true), OBJECT_END(false), ARRAY_START(true), ARRAY_END(false);
    public boolean isValue;

    Token(boolean isValue) {
      this.isValue = isValue;
    }
  }

  /**
   * Returns a type-safe parser context for a {@link JsonObject}, {@link JsonArray} or "any" type from which you can
   * parse a {@link String}.
   */
  public static final class JsonParserContext<T> {
    private final Class<T> clazz;

    JsonParserContext(Class<T> clazz) {
      this.clazz = clazz;
    }

    /**
     * Parses the current JSON type from a {@link String}.
     */
    public T from(String s) throws JsonParserException {
      return new JsonParser(s).parse(clazz);
    }
  }

  JsonParser(String s) throws JsonParserException {
    this.string = s;
    this.bufferLength = s.length();
    eof = (s.length() == 0);
  }

  /**
   * Parses a {@link JsonObject} from a source.
   *
   * <pre>
   * JsonObject json = {@link JsonParser}.object().from("{\"a\":[true,false], \"b\":1}");
   * </pre>
   */
  public static JsonParserContext<JsonObject> object() {
    return new JsonParserContext<JsonObject>(JsonObject.class);
  }

  /**
   * Parses a {@link JsonArray} from a source.
   *
   * <pre>
   * JsonArray json = {@link JsonParser}.array().from("[1, {\"a\":[true,false], \"b\":1}]");
   * </pre>
   */
  public static JsonParserContext<JsonArray> array() {
    return new JsonParserContext<JsonArray>(JsonArray.class);
  }

  /**
   * Parses any object from a source. For any valid JSON, returns either a null (for the JSON string 'null'), a
   * {@link String}, a {@link Number}, a {@link Boolean}, a {@link JsonObject} or a {@link JsonArray}.
   *
   * <pre>
   * Object json = {@link JsonParser}.any().from("{\"a\":[true,false], \"b\":1}");
   * Number json = ({@link Number}){@link JsonParser}.any().from("123.456e7");
   * </pre>
   */
  public static JsonParserContext<Object> any() {
    return new JsonParserContext<Object>(Object.class);
  }

  /**
   * Parse a single JSON value from the string, expecting an EOF at the end.
   */
  @SuppressWarnings("unchecked")
  <T> T parse(Class<T> clazz) throws JsonParserException {
    advanceToken();
    Object parsed = currentValue();
    if (advanceToken() != Token.EOF)
      throw createParseException(null, "Expected end of input, got " + token, true);
    if (clazz != Object.class && (parsed == null || clazz != parsed.getClass()))
      throw createParseException(null, "JSON did not contain the correct type, expected " + clazz.getName()
          + ".", true);
    return (T)(parsed);
  }

  /**
   * Starts parsing a JSON value at the current token position.
   */
  private Object currentValue() throws JsonParserException {
    // Only a value start token should appear when we're in the context of parsing a JSON value
    if (token.isValue)
      return value;
    throw createParseException(null, "Expected JSON value, got " + token, true);
  }

  /**
   * Consumes a token, first eating up any whitespace ahead of it. Note that number tokens are not necessarily valid
   * numbers.
   */
  private Token advanceToken() throws JsonParserException {
    int c = advanceChar();
    while (isWhitespace(c))
      c = advanceChar();

    tokenLinePos = linePos;
    tokenCharPos = index - rowPos - utf8adjust;
    tokenCharOffset = charOffset + index;

    switch (c) {
    case -1:
      return token = Token.EOF;
    case '[': // Inlined function to avoid additional stack
      JsonArray list = new JsonArray();
      if (advanceToken() != Token.ARRAY_END)
        while (true) {
          list.add(currentValue());
          if (advanceToken() == Token.ARRAY_END)
            break;
          if (token != Token.COMMA)
            throw createParseException(null, "Expected a comma or end of the array instead of " + token,
                true);
          if (advanceToken() == Token.ARRAY_END)
            throw createParseException(null, "Trailing comma found in array", true);
        }
      value = list;
      return token = Token.ARRAY_START;
    case ']':
      return token = Token.ARRAY_END;
    case ',':
      return token = Token.COMMA;
    case ':':
      return token = Token.COLON;
    case '{': // Inlined function to avoid additional stack
      JsonObject map = new JsonObject();
      if (advanceToken() != Token.OBJECT_END)
        while (true) {
          if (token != Token.STRING)
            throw createParseException(null, "Expected STRING, got " + token, true);
          String key = (String)value;
          if (advanceToken() != Token.COLON)
            throw createParseException(null, "Expected COLON, got " + token, true);
          advanceToken();
          map.put(key, currentValue());
          if (advanceToken() == Token.OBJECT_END)
            break;
          if (token != Token.COMMA)
            throw createParseException(null, "Expected a comma or end of the object instead of " + token,
                true);
          if (advanceToken() == Token.OBJECT_END)
            throw createParseException(null, "Trailing object found in array", true);
        }
      value = map;
      return token = Token.OBJECT_START;
    case '}':
      return token = Token.OBJECT_END;
    case 't':
      consumeKeyword((char)c, TRUE);
      value = Boolean.TRUE;
      return token = Token.TRUE;
    case 'f':
      consumeKeyword((char)c, FALSE);
      value = Boolean.FALSE;
      return token = Token.FALSE;
    case 'n':
      consumeKeyword((char)c, NULL);
      value = null;
      return token = Token.NULL;
    case '\"':
      value = consumeTokenString();
      return token = Token.STRING;
    case '-':
    case '0':
    case '1':
    case '2':
    case '3':
    case '4':
    case '5':
    case '6':
    case '7':
    case '8':
    case '9':
      value = consumeTokenNumber((char)c);
      return token = Token.NUMBER;
    case '+':
    case '.':
      throw createParseException(null, "Numbers may not start with '" + (char)c + "'", true);
    default:
    }

    if (isAsciiLetter(c))
      throw createHelpfulException((char)c, null, 0);

    throw createParseException(null, "Unexpected character: " + (char)c, true);
  }

  /**
   * Expects a given string at the current position.
   */
  private void consumeKeyword(char first, char[] expected) throws JsonParserException {
    for (int i = 0; i < expected.length; i++)
      if (advanceChar() != expected[i])
        throw createHelpfulException(first, expected, i);

    // The token should end with something other than an ASCII letter
    if (isAsciiLetter(peekChar()))
      throw createHelpfulException(first, expected, expected.length);
  }

  /**
   * Steps through to the end of the current number token (a non-digit token).
   */
  private Number consumeTokenNumber(char c) throws JsonParserException {
    int start = index - 1;
    int end = index;

    boolean isDouble = false;
    while (isDigitCharacter(peekChar())) {
      char next = (char)advanceChar();
      isDouble = next == '.' || next == 'e' || next == 'E' || isDouble;
      end++;
    }

    String number = string.substring(start, end);

    try {
      if (isDouble) {
        // Special zero handling to match JSON spec. Leading zero is only allowed if next character is . or e
        if (number.charAt(0) == '0') {
          if (number.charAt(1) == '.') {
            if (number.length() == 2)
              throw createParseException(null, "Malformed number: " + number, true);
          } else if (number.charAt(1) != 'e' && number.charAt(1) != 'E')
            throw createParseException(null, "Malformed number: " + number, true);
        }
        if (number.charAt(0) == '-') {
          if (number.charAt(1) == '0') {
            if (number.charAt(2) == '.') {
              if (number.length() == 3)
                throw createParseException(null, "Malformed number: " + number, true);
            } else if (number.charAt(2) != 'e' && number.charAt(2) != 'E')
              throw createParseException(null, "Malformed number: " + number, true);
          } else if (number.charAt(1) == '.') {
            throw createParseException(null, "Malformed number: " + number, true);
          }
        }

        return Double.parseDouble(number);
      }

      // Special zero handling to match JSON spec. No leading zeros allowed for integers.
      if (number.charAt(0) == '0') {
        if (number.length() == 1)
          return 0;
        throw createParseException(null, "Malformed number: " + number, true);
      }
      if (number.length() > 1 && number.charAt(0) == '-' && number.charAt(1) == '0') {
        if (number.length() == 2)
          return -0.0;
        throw createParseException(null, "Malformed number: " + number, true);
      }

      // HACK: Attempt to parse using the approximate best type for this
      int length = number.charAt(0) == '-' ? number.length() - 1 : number.length();
      if (length < 10) // 2 147 483 647
        return Integer.parseInt(number);
      if (length < 19) // 9 223 372 036 854 775 807
        return Long.parseLong(number);
      return new BigInteger(number);
    } catch (NumberFormatException e) {
      throw createParseException(e, "Malformed number: " + number, true);
    }
  }

  /**
   * Steps through to the end of the current string token (the unescaped double quote).
   */
  private String consumeTokenString() throws JsonParserException {
    reusableBuffer.setLength(0);
    while (true) {
      char c = stringChar();

      switch (c) {
      case '\"':
        return reusableBuffer.toString();
      case '\\':
        int escape = advanceChar();
        switch (escape) {
        case -1:
          throw createParseException(null, "EOF encountered in the middle of a string escape", false);
        case 'b':
          reusableBuffer.append('\b');
          break;
        case 'f':
          reusableBuffer.append('\f');
          break;
        case 'n':
          reusableBuffer.append('\n');
          break;
        case 'r':
          reusableBuffer.append('\r');
          break;
        case 't':
          reusableBuffer.append('\t');
          break;
        case '"':
        case '/':
        case '\\':
          reusableBuffer.append((char)escape);
          break;
        case 'u':
          reusableBuffer.append((char)(stringHexChar() << 12 | stringHexChar() << 8 //
              | stringHexChar() << 4 | stringHexChar()));
          break;
        default:
          throw createParseException(null, "Invalid escape: \\" + (char)escape, false);
        }
        break;
      default:
        reusableBuffer.append(c);
      }
    }
  }

  /**
   * Advances a character, throwing if it is illegal in the context of a JSON string.
   */
  private char stringChar() throws JsonParserException {
    int c = advanceChar();
    if (c == -1)
      throw createParseException(null, "String was not terminated before end of input", true);
    if (c < 32)
      throw createParseException(null,
          "Strings may not contain control characters: 0x" + Integer.toString(c, 16), false);
    return (char)c;
  }

  /**
   * Advances a character, throwing if it is illegal in the context of a JSON string hex unicode escape.
   */
  private int stringHexChar() throws JsonParserException {
    // GWT-compatible Character.digit(char, int)
    int c = "0123456789abcdef0123456789ABCDEF".indexOf(advanceChar()) % 16;
    if (c == -1)
      throw createParseException(null, "Expected unicode hex escape character", false);
    return c;
  }

  /**
   * Quick test for digit characters.
   */
  private boolean isDigitCharacter(int c) {
    return (c >= '0' && c <= '9') || c == 'e' || c == 'E' || c == '.' || c == '+' || c == '-';
  }

  /**
   * Quick test for whitespace characters.
   */
  private boolean isWhitespace(int c) {
    return c == ' ' || c == '\n' || c == '\r' || c == '\t';
  }

  /**
   * Quick test for ASCII letter characters.
   */
  private boolean isAsciiLetter(int c) {
    return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
  }

  /**
   * Peek one char ahead, don't advance, returns {@link Token#EOF} on end of input.
   */
  private int peekChar() {
    return eof ? -1 : string.charAt(index);
  }

  /**
   * Advance one character ahead, or return {@link Token#EOF} on end of input.
   */
  private int advanceChar() throws JsonParserException {
    if (eof)
      return -1;
    int c = string.charAt(index);
    if (c == '\n') {
      linePos++;
      rowPos = index + 1;
      utf8adjust = 0;
    }

    index++;
    if (index >= bufferLength)
      eof = true;

    return c;
  }

  /**
   * Throws a helpful exception based on the current alphanumeric token.
   */
  private JsonParserException createHelpfulException(char first, char[] expected, int failurePosition)
      throws JsonParserException {
    // Build the first part of the token
    StringBuilder errorToken = new StringBuilder(first
        + (expected == null ? "" : new String(expected, 0, failurePosition)));

    // Consume the whole pseudo-token to make a better error message
    while (isAsciiLetter(peekChar()) && errorToken.length() < 15)
      errorToken.append((char)advanceChar());

    return createParseException(null, "Unexpected token '" + errorToken + "'"
        + (expected == null ? "" : ". Did you mean '" + first + new String(expected) + "'?"), true);
  }

  /**
   * Creates a {@link JsonParserException} and fills it from the current line and char position.
   */
  private JsonParserException createParseException(Exception e, String message, boolean tokenPos) {
    if (tokenPos)
      return new JsonParserException(e, message + " on line " + tokenLinePos + ", char " + tokenCharPos,
          tokenLinePos, tokenCharPos, tokenCharOffset);
    else {
      int charPos = Math.max(1, index - rowPos - utf8adjust);
      return new JsonParserException(e, message + " on line " + linePos + ", char " + charPos, linePos, charPos,
          index + charOffset);
    }
  }
}
