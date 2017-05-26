package antlr

import org.antlr.v4.runtime.tree.TerminalNode
import java.util.*

class SymbolVisitor(internal var functions: MutableMap<String, Function>) : KongBaseVisitor<NodeValue>() {

    override fun visitFunctionDeclStatement(ctx: KongParser.FunctionDeclStatementContext): NodeValue {
        val params = if (ctx.idList() != null) ctx.idList().Identifier() else ArrayList<TerminalNode>()
        val block = ctx.expression() ?: ctx.block()
        val id = ctx.Identifier().text + params.size
        functions.put(id, Function(id, params, block))
        return NodeValue.VOID
    }
}
