package antlr

import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTree

import java.util.HashMap

fun main(args: Array<String>) {
    try {
        val input = "def add(a,b) {a+b} \n" +
                    "let c = add(10,15) \n" +
                    "println(c)"
        val lexer = KongLexer(ANTLRInputStream(input))
        val parser = KongParser(CommonTokenStream(lexer))
        parser.buildParseTree = true
        val tree = parser.prog()

        val scope = Scope()
        val functions = HashMap<String, Function>()
        val symbolVisitor = SymbolVisitor(functions)
        symbolVisitor.visit(tree)
        val visitor = EvalVisitor(scope, functions)
        visitor.visit(tree)
    } catch (e: Exception) {
        if (e.message != null) {
            System.err.println(e.message)
        } else {
            e.printStackTrace()
        }
    }

}
