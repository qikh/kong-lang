package antlr;

import antlr.KongParser.AddExpressionContext;
import antlr.KongParser.AndExpressionContext;
import antlr.KongParser.AssignmentStatementContext;
import antlr.KongParser.BoolExpressionContext;
import antlr.KongParser.DivideExpressionContext;
import antlr.KongParser.EqExpressionContext;
import antlr.KongParser.ExpressionContext;
import antlr.KongParser.ExpressionExpressionContext;
import antlr.KongParser.FloatExpressionContext;
import antlr.KongParser.ForStatementContext;
import antlr.KongParser.FunctionCallExpressionContext;
import antlr.KongParser.FunctionDeclStatementContext;
import antlr.KongParser.GtEqExpressionContext;
import antlr.KongParser.GtExpressionContext;
import antlr.KongParser.IdentifierExpressionContext;
import antlr.KongParser.IdentifierFunctionCallContext;
import antlr.KongParser.IfStatementContext;
import antlr.KongParser.IntExpressionContext;
import antlr.KongParser.LtEqExpressionContext;
import antlr.KongParser.LtExpressionContext;
import antlr.KongParser.ModulusExpressionContext;
import antlr.KongParser.MultiplyExpressionContext;
import antlr.KongParser.NotEqExpressionContext;
import antlr.KongParser.NotExpressionContext;
import antlr.KongParser.NullExpressionContext;
import antlr.KongParser.OrExpressionContext;
import antlr.KongParser.PowerExpressionContext;
import antlr.KongParser.PrintlnFunctionCallContext;
import antlr.KongParser.ReturnStatementContext;
import antlr.KongParser.StatementContext;
import antlr.KongParser.StatementListContext;
import antlr.KongParser.StringExpressionContext;
import antlr.KongParser.SubtractExpressionContext;
import antlr.KongParser.TernaryExpressionContext;
import antlr.KongParser.UnaryMinusExpressionContext;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EvalVisitor extends KongBaseVisitor<NodeValue> {

  private static ReturnValue returnValue = new ReturnValue();
  private Scope scope;
  private Map<String, Function> functions;

  public EvalVisitor(Scope scope, Map<String, Function> functions) {
    this.scope = scope;
    this.functions = functions;
  }

  @Override
  public NodeValue visitFunctionDeclStatement(FunctionDeclStatementContext ctx) {
    return NodeValue.VOID;
  }


  // '-' expression                           #unaryMinusExpression
  @Override
  public NodeValue visitUnaryMinusExpression(UnaryMinusExpressionContext ctx) {
    NodeValue v = this.visit(ctx.expression());
    if (!v.isNumber()) {
      throw new EvalException(ctx);
    }
    return new NodeValue(-1 * v.asDouble());
  }

  // '!' expression                           #notExpression
  @Override
  public NodeValue visitNotExpression(NotExpressionContext ctx) {
    NodeValue v = this.visit(ctx.expression());
    if (!v.isBoolean()) {
      throw new EvalException(ctx);
    }
    return new NodeValue(!v.asBoolean());
  }

  // expression '^' expression                #powerExpression
  @Override
  public NodeValue visitPowerExpression(PowerExpressionContext ctx) {
    NodeValue lhs = this.visit(ctx.expression(0));
    NodeValue rhs = this.visit(ctx.expression(1));
    if (lhs.isNumber() && rhs.isNumber()) {
      return new NodeValue(Math.pow(lhs.asDouble(), rhs.asDouble()));
    }
    throw new EvalException(ctx);
  }

  // expression '*' expression                #multiplyExpression
  @Override
  public NodeValue visitMultiplyExpression(MultiplyExpressionContext ctx) {
    NodeValue lhs = this.visit(ctx.expression(0));
    NodeValue rhs = this.visit(ctx.expression(1));
    if (lhs == null || rhs == null) {
      System.err.println("lhs " + lhs + " rhs " + rhs);
      throw new EvalException(ctx);
    }

    // number * number
    if (lhs.isNumber() && rhs.isNumber()) {
      return new NodeValue(lhs.asDouble() * rhs.asDouble());
    }

    // string * number
    if (lhs.isString() && rhs.isNumber()) {
      StringBuilder str = new StringBuilder();
      int stop = rhs.asDouble().intValue();
      for (int i = 0; i < stop; i++) {
        str.append(lhs.asString());
      }
      return new NodeValue(str.toString());
    }

    // list * number
    if (lhs.isList() && rhs.isNumber()) {
      List<NodeValue> total = new ArrayList<NodeValue>();
      int stop = rhs.asDouble().intValue();
      for (int i = 0; i < stop; i++) {
        total.addAll(lhs.asList());
      }
      return new NodeValue(total);
    }
    throw new EvalException(ctx);
  }

  // expression '/' expression                #divideExpression
  @Override
  public NodeValue visitDivideExpression(DivideExpressionContext ctx) {
    NodeValue lhs = this.visit(ctx.expression(0));
    NodeValue rhs = this.visit(ctx.expression(1));
    if (lhs.isNumber() && rhs.isNumber()) {
      return new NodeValue(lhs.asDouble() / rhs.asDouble());
    }
    throw new EvalException(ctx);
  }

  // expression '%' expression                #modulusExpression
  @Override
  public NodeValue visitModulusExpression(ModulusExpressionContext ctx) {
    NodeValue lhs = this.visit(ctx.expression(0));
    NodeValue rhs = this.visit(ctx.expression(1));
    if (lhs.isNumber() && rhs.isNumber()) {
      return new NodeValue(lhs.asDouble() % rhs.asDouble());
    }
    throw new EvalException(ctx);
  }

  // expression '+' expression                #addExpression
  @Override
  public NodeValue visitAddExpression(@NotNull AddExpressionContext ctx) {
    NodeValue lhs = this.visit(ctx.expression(0));
    NodeValue rhs = this.visit(ctx.expression(1));

    if (lhs == null || rhs == null) {
      throw new EvalException(ctx);
    }

    // number + number
    if (lhs.isNumber() && rhs.isNumber()) {
      return new NodeValue(lhs.asDouble() + rhs.asDouble());
    }

    // list + any
    if (lhs.isList()) {
      List<NodeValue> list = lhs.asList();
      list.add(rhs);
      return new NodeValue(list);
    }

    // string + any
    if (lhs.isString()) {
      return new NodeValue(lhs.asString() + "" + rhs.toString());
    }

    // any + string
    if (rhs.isString()) {
      return new NodeValue(lhs.toString() + "" + rhs.asString());
    }

    return new NodeValue(lhs.toString() + rhs.toString());
  }

  // expression '-' expression                #subtractExpression
  @Override
  public NodeValue visitSubtractExpression(SubtractExpressionContext ctx) {
    NodeValue lhs = this.visit(ctx.expression(0));
    NodeValue rhs = this.visit(ctx.expression(1));
    if (lhs.isNumber() && rhs.isNumber()) {
      return new NodeValue(lhs.asDouble() - rhs.asDouble());
    }
    if (lhs.isList()) {
      List<NodeValue> list = lhs.asList();
      list.remove(rhs);
      return new NodeValue(list);
    }
    throw new EvalException(ctx);
  }

  // expression '>=' expression               #gtEqExpression
  @Override
  public NodeValue visitGtEqExpression(GtEqExpressionContext ctx) {
    NodeValue lhs = this.visit(ctx.expression(0));
    NodeValue rhs = this.visit(ctx.expression(1));
    if (lhs.isNumber() && rhs.isNumber()) {
      return new NodeValue(lhs.asDouble() >= rhs.asDouble());
    }
    if (lhs.isString() && rhs.isString()) {
      return new NodeValue(lhs.asString().compareTo(rhs.asString()) >= 0);
    }
    throw new EvalException(ctx);
  }

  // expression '<=' expression               #ltEqExpression
  @Override
  public NodeValue visitLtEqExpression(LtEqExpressionContext ctx) {
    NodeValue lhs = this.visit(ctx.expression(0));
    NodeValue rhs = this.visit(ctx.expression(1));
    if (lhs.isNumber() && rhs.isNumber()) {
      return new NodeValue(lhs.asDouble() <= rhs.asDouble());
    }
    if (lhs.isString() && rhs.isString()) {
      return new NodeValue(lhs.asString().compareTo(rhs.asString()) <= 0);
    }
    throw new EvalException(ctx);
  }

  // expression '>' expression                #gtExpression
  @Override
  public NodeValue visitGtExpression(GtExpressionContext ctx) {
    NodeValue lhs = this.visit(ctx.expression(0));
    NodeValue rhs = this.visit(ctx.expression(1));
    if (lhs.isNumber() && rhs.isNumber()) {
      return new NodeValue(lhs.asDouble() > rhs.asDouble());
    }
    if (lhs.isString() && rhs.isString()) {
      return new NodeValue(lhs.asString().compareTo(rhs.asString()) > 0);
    }
    throw new EvalException(ctx);
  }

  // expression '<' expression                #ltExpression
  @Override
  public NodeValue visitLtExpression(LtExpressionContext ctx) {
    NodeValue lhs = this.visit(ctx.expression(0));
    NodeValue rhs = this.visit(ctx.expression(1));
    if (lhs.isNumber() && rhs.isNumber()) {
      return new NodeValue(lhs.asDouble() < rhs.asDouble());
    }
    if (lhs.isString() && rhs.isString()) {
      return new NodeValue(lhs.asString().compareTo(rhs.asString()) < 0);
    }
    throw new EvalException(ctx);
  }

  // expression '==' expression               #eqExpression
  @Override
  public NodeValue visitEqExpression(@NotNull EqExpressionContext ctx) {
    NodeValue lhs = this.visit(ctx.expression(0));
    NodeValue rhs = this.visit(ctx.expression(1));
    if (lhs == null) {
      throw new EvalException(ctx);
    }
    return new NodeValue(lhs.equals(rhs));
  }

  // expression '!=' expression               #notEqExpression
  @Override
  public NodeValue visitNotEqExpression(@NotNull NotEqExpressionContext ctx) {
    NodeValue lhs = this.visit(ctx.expression(0));
    NodeValue rhs = this.visit(ctx.expression(1));
    return new NodeValue(!lhs.equals(rhs));
  }

  // expression '&&' expression               #andExpression
  @Override
  public NodeValue visitAndExpression(AndExpressionContext ctx) {
    NodeValue lhs = this.visit(ctx.expression(0));
    NodeValue rhs = this.visit(ctx.expression(1));

    if (!lhs.isBoolean() || !rhs.isBoolean()) {
      throw new EvalException(ctx);
    }
    return new NodeValue(lhs.asBoolean() && rhs.asBoolean());
  }

  // expression '||' expression               #orExpression
  @Override
  public NodeValue visitOrExpression(OrExpressionContext ctx) {
    NodeValue lhs = this.visit(ctx.expression(0));
    NodeValue rhs = this.visit(ctx.expression(1));

    if (!lhs.isBoolean() || !rhs.isBoolean()) {
      throw new EvalException(ctx);
    }
    return new NodeValue(lhs.asBoolean() || rhs.asBoolean());
  }

  // expression '?' expression ':' expression #ternaryExpression
  @Override
  public NodeValue visitTernaryExpression(TernaryExpressionContext ctx) {
    NodeValue condition = this.visit(ctx.expression(0));
    if (condition.asBoolean()) {
      return new NodeValue(this.visit(ctx.expression(1)));
    } else {
      return new NodeValue(this.visit(ctx.expression(2)));
    }
  }


  // Number                                   #numberExpression
  @Override
  public NodeValue visitIntExpression(@NotNull IntExpressionContext ctx) {
    return new NodeValue(Integer.valueOf(ctx.getText()));
  }

  // Number                                   #numberExpression
  @Override
  public NodeValue visitFloatExpression(@NotNull FloatExpressionContext ctx) {
    return new NodeValue(Double.valueOf(ctx.getText()));
  }

  // Bool                                     #boolExpression
  @Override
  public NodeValue visitBoolExpression(@NotNull BoolExpressionContext ctx) {
    return new NodeValue(Boolean.valueOf(ctx.getText()));
  }

  // Null                                     #nullExpression
  @Override
  public NodeValue visitNullExpression(@NotNull NullExpressionContext ctx) {
    return NodeValue.NULL;
  }

  private NodeValue resolveIndexes(ParserRuleContext ctx, NodeValue val, List<ExpressionContext> indexes) {
    for (ExpressionContext ec : indexes) {
      NodeValue idx = this.visit(ec);
      if (!idx.isNumber() || (!val.isList() && !val.isString())) {
        throw new EvalException("Problem resolving indexes on " + val + " at " + idx, ec);
      }
      int i = idx.asInt();
      if (val.isString()) {
        val = new NodeValue(val.asString().substring(i, i + 1));
      } else {
        val = val.asList().get(i);
      }
    }
    return val;
  }

  private void setAtIndex(ParserRuleContext ctx, List<ExpressionContext> indexes, NodeValue val, NodeValue newVal) {
    if (!val.isList()) {
      throw new EvalException(ctx);
    }
    // TODO some more list size checking in here
    for (int i = 0; i < indexes.size() - 1; i++) {
      NodeValue idx = this.visit(indexes.get(i));
      if (!idx.isNumber()) {
        throw new EvalException(ctx);
      }
      val = val.asList().get(idx.asDouble().intValue());
    }
    NodeValue idx = this.visit(indexes.get(indexes.size() - 1));
    if (!idx.isNumber()) {
      throw new EvalException(ctx);
    }
    val.asList().set(idx.asDouble().intValue(), newVal);
  }

  // functionCall indexes?                    #functionCallExpression
  @Override
  public NodeValue visitFunctionCallExpression(FunctionCallExpressionContext ctx) {
    NodeValue val = this.visit(ctx.functionCall());
    return val;
  }


  // Identifier indexes?                      #identifierExpression
  @Override
  public NodeValue visitIdentifierExpression(@NotNull IdentifierExpressionContext ctx) {
    String id = ctx.ID().getText();
    NodeValue val = scope.resolve(id);

    if (ctx.indexes() != null) {
      List<ExpressionContext> exps = ctx.indexes().expression();
      val = resolveIndexes(ctx, val, exps);
    }
    return val;
  }

  // String indexes?                          #stringExpression
  @Override
  public NodeValue visitStringExpression(@NotNull StringExpressionContext ctx) {
    String text = ctx.getText();
    text = text.substring(1, text.length() - 1).replaceAll("\\\\(.)", "$1");
    NodeValue val = new NodeValue(text);
    if (ctx.indexes() != null) {
      List<ExpressionContext> exps = ctx.indexes().expression();
      val = resolveIndexes(ctx, val, exps);
    }
    return val;
  }

  // '(' expression ')' indexes?              #expressionExpression
  @Override
  public NodeValue visitExpressionExpression(ExpressionExpressionContext ctx) {
    NodeValue val = this.visit(ctx.expression());
    if (ctx.indexes() != null) {
      List<ExpressionContext> exps = ctx.indexes().expression();
      val = resolveIndexes(ctx, val, exps);
    }
    return val;
  }

  // assignment
  // : Identifier indexes? '=' expression
  // ;
  @Override
  public NodeValue visitAssignmentStatement(@NotNull AssignmentStatementContext ctx) {
    NodeValue newVal = this.visit(ctx.expression());
    if (ctx.indexes() != null) {
      NodeValue val = scope.resolve(ctx.ID().getText());
      List<ExpressionContext> exps = ctx.indexes().expression();
      setAtIndex(ctx, exps, val, newVal);
    } else {
      String id = ctx.ID().getText();
      scope.assign(id, newVal);
    }
    return NodeValue.VOID;
  }

  // Identifier '(' exprList? ')' #identifierFunctionCall
  @Override
  public NodeValue visitIdentifierFunctionCall(IdentifierFunctionCallContext ctx) {
    List<ExpressionContext>
        params =
        ctx.expressionList() != null ? ctx.expressionList().expression() : new ArrayList<ExpressionContext>();
    String id = ctx.ID().getText() + params.size();
    Function function;
    if ((function = functions.get(id)) != null) {
      return function.invoke(params, functions, scope);
    }
    throw new EvalException(ctx);
  }

  // Println '(' expression? ')'  #printlnFunctionCall
  @Override
  public NodeValue visitPrintlnFunctionCall(@NotNull PrintlnFunctionCallContext ctx) {
    System.out.println(this.visit(ctx.expression()));
    return NodeValue.VOID;
  }

  // ifStatement
  //  : ifStat elseIfStat* elseStat? End
  //  ;
  //
  // ifStat
  //  : If expression Do block
  //  ;
  //
  // elseIfStat
  //  : Else If expression Do block
  //  ;
  //
  // elseStat
  //  : Else Do block
  //  ;
  @Override
  public NodeValue visitIfStatement(@NotNull IfStatementContext ctx) {

    // if ...
    if (this.visit(ctx.ifStat().expression()).asBoolean()) {
      return this.visit(ctx.ifStat().block());
    }

    // else if ...
    for (int i = 0; i < ctx.elseIfStat().size(); i++) {
      if (this.visit(ctx.elseIfStat(i).expression()).asBoolean()) {
        return this.visit(ctx.elseIfStat(i).block());
      }
    }

    // else ...
    if (ctx.elseStat() != null) {
      return this.visit(ctx.elseStat().block());
    }

    return NodeValue.VOID;
  }

  // block
  // : (statement | functionDecl)* (Return expression)?
  // ;
  @Override
  public NodeValue visitStatementList(StatementListContext ctx) {

    scope = new Scope(scope); // create new local scope

    for (StatementContext sx : ctx.statement()) {
      this.visit(sx);
    }

    scope = scope.parent();
    return NodeValue.VOID;
  }

  // forStatement
  // : For Identifier '=' expression To expression OBrace block CBrace
  // ;
  @Override
  public NodeValue visitForStatement(ForStatementContext ctx) {
    int start = this.visit(ctx.INT(0)).asInt();
    int stop = this.visit(ctx.INT(1)).asInt();
    for (int i = start; i <= stop; i++) {
      scope.assign(ctx.ID().getText(), new NodeValue(i));
      NodeValue returnValue = this.visit(ctx.block());
      if (returnValue != NodeValue.VOID) {
        return returnValue;
      }
    }
    return NodeValue.VOID;
  }

  @Override
  public NodeValue visitReturnStatement(ReturnStatementContext ctx) {

    ExpressionContext ex;
    if ((ex = ctx.expression()) != null) {
      NodeValue val = this.visit(ex);

      returnValue.value = val;
      scope = scope.parent();
      throw returnValue;
    }
    return NodeValue.VOID;
  }
}
