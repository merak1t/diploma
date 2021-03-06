// Copyright (C) 2005 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.caja.lexer;

import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * Tokenizes javascript source.
 *
 * @author mikesamuel@gmail.com (Mike Samuel)
 */
public class JsLexer implements TokenStream<JsTokenType> {
  private TokenStream<JsTokenType> ts;

  private static PunctuationTrie<?> JAVASCRIPT_PUNCTUATOR;
  static {
    TreeMap<String, Void> javascriptPunctuation = new TreeMap<String, Void>();
    for (Punctuation p : Punctuation.values()) {
      javascriptPunctuation.put(p.toString(), null);
    }
    JAVASCRIPT_PUNCTUATOR = new PunctuationTrie<Void>(javascriptPunctuation);
  }

  public JsLexer(CharProducer producer) {
    this(producer, false);
  }

  public JsLexer(CharProducer producer, boolean isQuasiliteral) {
    this.ts = new WordClassifier(
        new InputElementSplitter(producer, JAVASCRIPT_PUNCTUATOR,
                                 isQuasiliteral));
  }

  public boolean hasNext() throws ParseException { return ts.hasNext(); }

  public Token<JsTokenType> next() throws ParseException { return ts.next(); }

  /**
   * According to
   * <tt>http://www.mozilla.org/js/language/js20/rationale/syntax.html</tt>
   * <blockquote>
   *   "To support error recovery, JavaScript 2.0's lexical grammar must be
   *   made independent of its syntactic grammar. To make the lexical grammar
   *   independent of the syntactic grammar, JavaScript 2.0 determines whether
   *   a / starts a regular expression or is a division (or /=) operator solely
   *   based on the previous token."</blockquote>
   * <p>That page then lists the tokens that can precede a RegExp literal, and
   * says:
   * <blockquote>
   *   "Regardless of the previous token, // is interpreted as the beginning
   *   of a comment."</blockquote>
   *
   * <p>This scheme is inconsistent with EcmaScript 3 and planned successors
   * which do not have a context-free lexical grammar.  This approximation works
   * well in practice, but will fail in some cases, such as after a ++/--
   * operator that turns out to be a prefix operator.
   *
   * <p>Since that document was written, the set of proposed reserved keywords
   * for EcmaScript 4 has changed.  David-Sarah Hopwood suggested changing the
   * preceder set in a mail titled "JavaScript lexing" on google-com.google.caja-discuss
   * which concluded:
   * <blockquote>
   *   "I think you should:
   *   <ol type="a">
   *   <li>remove 'field', 'is', 'namespace', 'use', '->', '..', '@', '^^',
   *       and '^^=' from validPreceders, and add 'void';
   *   <li>document that [Caja] does not allow '++' or '--' just before a
   *       regexp literal;
   *   <li>c) document that [Caja] does not allow a regexp literal as the first
   *       token of an expression statement.
   *   </ol>
   * </blockquote>
   */
  private static final Pattern TOKEN_BEFORE_REGEXP_LITERAL_RE;
  static {
    StringBuilder sb = new StringBuilder();
    String[] validPreceders = new String[] {
        "!", "!=", "!==", "#", "%", "%=", "&", "&&", "&&=", "&=", "(", "*",
        "*=", "+", "+=", ",", "-", "-=", ".", "...", "/", "/=", ":", "::", ";",
        "<", "<<", "<<=", "<=", "=", "==", "===", ">", ">=", ">>", ">>=", ">>>",
        ">>>=", "?", "[", "^", "^=", "{", "|", "|=", "||", "||=", "~",
        "abstract", "break", "case", "catch", "class", "const", "continue",
        "debugger", "default", "delete", "do", "else", "enum", "export",
        "extends", "final", "finally", "for", "function", "goto", "if",
        "implements", "import", "in", "instanceof", "native", "new", "package",
        "return", "static", "switch", "synchronized", "throw", "throws",
        "transient", "try", "typeof", "var", "void", "volatile", "while",
        "with",
    };
    sb.append("^(?:");
    for (int i = 0; i < validPreceders.length; i++) {
      if (i != 0) { sb.append('|'); }
      sb.append("(?:");
      sb.append(Pattern.quote(validPreceders[i]));
      sb.append(')');
    }
    sb.append(")$");
    TOKEN_BEFORE_REGEXP_LITERAL_RE = Pattern.compile(sb.toString());
  }

  static boolean isRegexp(String previous) {
    if (TOKEN_BEFORE_REGEXP_LITERAL_RE.matcher(previous).find()) {
      // There is one case the above doesn't handle.
      // If the preceding token is a number that ends with a decimal point, the
      // regex will mistake it as a "." token which is in validPreceders.
      if (previous.length() >= 2 && previous.endsWith(".")) {
        char secondToLast = previous.charAt(previous.length() - 2);
        if (secondToLast >= '0' && secondToLast <= '9') { return false; }
      }
      return true;
    }
    return false;
  }

  private static Pattern INTEGER_LITERAL_RE = Pattern.compile(
      "^[+-]?((?:0[xX][0-9a-fA-F]*)"
      + "|(?:0[0-7]*)"
      + "|(?:[1-9][0-9]*))"
      + "$");

  static class WordClassifier implements TokenStream<JsTokenType> {

    private TokenStream<JsTokenType> stream;

    public WordClassifier(TokenStream<JsTokenType> stream) {
      this.stream = stream;
    }

    public boolean hasNext() throws ParseException {
      return stream.hasNext();
    }

    public Token<JsTokenType> next() throws ParseException {
      Token<JsTokenType> tok = stream.next();
      if (tok.type == JsTokenType.WORD) {
        JsTokenType type = JsTokenType.WORD;
        if (null != Keyword.fromString(tok.text)) {
          type = JsTokenType.KEYWORD;
        } else if (0 < tok.text.length()) {
          char ch = tok.text.charAt(0);
          if ((ch >= '0' && ch <= '9') || '-' == ch || '+' == ch || '.' == ch) {
            // Verify tok is a well formed numeric token.
            if (INTEGER_LITERAL_RE.matcher(tok.text).matches()) {
              type = JsTokenType.INTEGER;
            } else {
              String text = tok.text;
              try {
                Double.parseDouble(text);
                type = JsTokenType.FLOAT;
              } catch (NumberFormatException ex) {
                // Not a valid numeric token.  Will be rejected as an identifier
                // by the parser.
              }
            }
          }
        }
        if (JsTokenType.WORD != type) {
          tok = Token.instance(tok.text, type, tok.pos);
        }
      }
      return tok;
    }

  }  // WordClassifier

  public static boolean isJsSpace(char ch) {
    // From http://www.mozilla.org/js/language/es4/formal/lexer-grammar.html
    switch (ch) {
      case ' ': case '\t': case '\r': case '\n': case '\u000c':
      case '\u00A0': case '\u2000': case '\u2001': case '\u2002':
      case '\u2003': case '\u2004': case '\u2005': case '\u2006':
      case '\u2007': case '\u2008': case '\u2009': case '\u200A':
      case '\u200B': case '\u3000':

      // Treat the Byte Order Marker as whitespace as discussed at
      // http://code.google.com/p/google-caja/issues/detail?id=148
      case '\uFEFF':
        return true;
      default:
        return false;
    }
  }

  public static boolean isJsLineSeparator(char ch) {
    switch (ch) {
      case '\r': case '\n': case '\u2028': case '\u2029':
        return true;
      default:
        return false;
    }
  }

  public static PunctuationTrie<?> getPunctuationTrie() {
    return JAVASCRIPT_PUNCTUATOR;
  }
}
