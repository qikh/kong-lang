package antlr;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.HashMap;
import java.util.Map;

public class Main {

  public static void main(String[] args) {
    try {
      String input = "def add(a,b) {return a+b;} \n" +
                     "let c = add(10,15);" +
                     "println(c)";
      KongLexer lexer = new KongLexer(new ANTLRInputStream(input));
      KongParser parser = new KongParser(new CommonTokenStream(lexer));
      parser.setBuildParseTree(true);
      ParseTree tree = parser.prog();

      Scope scope = new Scope();
      Map<String, Function> functions = new HashMap<String, Function>();
      SymbolVisitor symbolVisitor = new SymbolVisitor(functions);
      symbolVisitor.visit(tree);
      EvalVisitor visitor = new EvalVisitor(scope, functions);
      visitor.visit(tree);
    } catch (Exception e) {
      if (e.getMessage() != null) {
        System.err.println(e.getMessage());
      } else {
        e.printStackTrace();
      }
    }
  }
}
