package antlr

import antlr.KongParser.ExpressionContext

import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.TerminalNode

class Function(val id: String, val params: List<TerminalNode>, val block: ParseTree) {

    fun invoke(params: List<ExpressionContext>, functions: Map<String, Function>, outerScope: Scope?): NodeValue {
        if (params.size != this.params.size) {
            throw RuntimeException("Illegal Function call")
        }
        var functionScope = Scope(outerScope) // create function functionScope
        val evalVisitor = EvalVisitor(functionScope, functions)
        for (i in this.params.indices) {
            val value = evalVisitor.visit(params[i])
            functionScope.assignParam(this.params[i].text, value)
        }
        var ret: NodeValue?
        try {
            ret = evalVisitor.visit(this.block)
        } catch (returnValue: ReturnValue) {
            ret = returnValue.value
        }

        return ret ?: NodeValue.VOID
    }
}
