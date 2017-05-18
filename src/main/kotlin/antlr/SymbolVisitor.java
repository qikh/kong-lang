package antlr;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SymbolVisitor extends KongBaseVisitor<NodeValue> {

  Map<String, Function> functions;

  public SymbolVisitor(Map<String, Function> functions) {
    this.functions = functions;
  }

  @Override
  public NodeValue visitFunctionDeclStatement(KongParser.FunctionDeclStatementContext ctx) {
    List<TerminalNode> params = ctx.idList() != null ? ctx.idList().ID() : new ArrayList<TerminalNode>();
    ParseTree block = ctx.block();
    String id = ctx.ID().getText() + params.size();
    functions.put(id, new Function(id, params, block));
    return NodeValue.VOID;
  }
}
