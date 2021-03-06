// Copyright (C) 2009 Google Inc.
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

package com.google.caja.ancillary.opt;

import com.google.caja.lexer.*;
import com.google.caja.parser.js.*;
import com.google.caja.render.Concatenator;
import com.google.caja.render.JsMinimalPrinter;
import com.google.caja.reporting.*;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Optimizes JavaScript code.
 *
 * @author mikesamuel@gmail.com
 */
public class JsOptimizer {
  private final List<Statement> compUnits = Lists.newArrayList();
  private ParseTreeKB optimizer;
  private boolean rename;
  private final MessageQueue mq;

  public JsOptimizer(MessageQueue mq) { this.mq = mq; }

  /**
   * Register an input for optimization.
   */
  public JsOptimizer addInput(Statement stmt) {
    compUnits.add(stmt);
    return this;
  }

  /**
   * Sets the environment file to use during optimization.
   * The environment file contains facts about the environment in which the
   * optimized output is expected to run, so allows for interpreter-specific
   * optimization.
   */
  public JsOptimizer setEnvJson(ObjectConstructor envJson) {
    if (optimizer == null) { optimizer = new ParseTreeKB(); }
    List<? extends ObjProperty> props = envJson.children();
    for (ObjProperty prop : props) {
      // JSON had better not have getters
      ValueProperty vprop = (ValueProperty) prop;
      Expression value = vprop.getValueExpr().fold(false); // fold negative nums
      if (!(value instanceof Literal)) {
        // True for "*useragent*" property inserted by JSKB.
        continue;
      }
      StringLiteral sl = vprop.getPropertyNameNode();
      String rawExpr = sl.getValue();
      rawExpr = " " + rawExpr.substring(1, rawExpr.length() - 1) + " ";
      CharProducer valueCp = CharProducer.Factory.fromJsString(
          CharProducer.Factory.fromString(rawExpr, sl.getFilePosition()));
      try {
        Expression expr = jsExpr(valueCp, DevNullMessageQueue.singleton());
        optimizer.addFact(expr, Fact.is((Literal) value));
      } catch (ParseException ex) {
        continue;  // Triggered for browser specific extensions such as for each
      }
    }
    return this;
  }

  /**
   * Sets a flag telling the optimizer whether to rename local variables where
   * doing so would not change semantics on an interpreter that does not allow
   * aliasing of {@code eval}.
   */
  public JsOptimizer setRename(boolean rename) {
    this.rename = rename;
    return this;
  }

  /**
   * Returns an optimized version of the concatenation of the programs
   * registered via {@link #addInput}.
   */
  public Statement optimize() {
    Block block = new Block(FilePosition.UNKNOWN, compUnits);
    // Do first since this improves the performance of the ConstVarInliner.
    VarCollector.optimize(block);
    if (optimizer != null) {
      block = optimizer.optimize(block, mq);
    }
    if (rename) {
      // We pool after the ConstLocalOptimizer invoked by optimizer has run.
      block = ConstantPooler.optimize(block);
      // Now we shorten any long names introduced by the constant pooler.
      block = new LocalVarRenamer(mq).optimize(block);
    }
    // Finally we rearrange statements and convert conditionals to expressions
    // where it will make things shorter.
    return (Statement) StatementSimplifier.optimize(block, mq);
  }

  public static void main(String... args) throws IOException {
    MessageQueue mq = new SimpleMessageQueue();
    MessageContext mc = new MessageContext();
    JsOptimizer opt = new JsOptimizer(mq);
    opt.setRename(true);
    opt.setEnvJson(new ObjectConstructor(FilePosition.UNKNOWN));
    try {
      for (int i = 0, n = args.length; i < n; ++i) {
        String arg = args[i];
        if ("--norename".equals(arg)) {
          opt.setRename(false);
        } else if (arg.startsWith("--envjson=")) {
          String jsonfile = arg.substring(arg.indexOf('=') + 1);
          CharProducer json = CharProducer.Factory.fromFile(
              new File(jsonfile), "UTF-8");
          opt.setEnvJson((ObjectConstructor) jsExpr(json, mq));
        } else {
          if ("--".equals(arg)) { ++i; }
          for (;i < n; ++i) {
            CharProducer cp = CharProducer.Factory.fromFile(
                new File(args[i]), "UTF-8");
            mc.addInputSource(cp.getCurrentPosition().source());
            opt.addInput(js(cp, mq));
          }
        }
      }
    } catch (ParseException ex) {
      ex.toMessageQueue(mq);
    }
    Statement out = opt.optimize();
    for (Message msg : mq.getMessages()) {
      msg.format(mc, System.err);
      System.err.println();
    }
    JsMinimalPrinter printer = new JsMinimalPrinter(
        new Concatenator(System.out, null));
    RenderContext rc = new RenderContext(printer)
        .withPropertyNameQuotingMode(PropertyNameQuotingMode.NO_QUOTES);
    if (out instanceof Block) {
      ((Block) out).renderBody(rc);
    } else {
      out.render(rc);
    }
    printer.noMoreTokens();
  }

  private static Block js(CharProducer cp, MessageQueue mq)
      throws ParseException {
    return jsParser(cp, mq).parse();
  }

  private static Expression jsExpr(CharProducer cp, MessageQueue mq)
      throws ParseException {
    Parser p = jsParser(cp, mq);
    Expression e = p.parseExpression(true);
    p.getTokenQueue().expectEmpty();
    return e;
  }

  private static Parser jsParser(CharProducer cp, MessageQueue mq) {
    JsLexer lexer = new JsLexer(cp, false);
    JsTokenQueue tq = new JsTokenQueue(lexer, cp.getCurrentPosition().source());
    tq.setInputRange(cp.filePositionForOffsets(cp.getOffset(), cp.getLimit()));
    return new Parser(tq, mq);
  }
}
