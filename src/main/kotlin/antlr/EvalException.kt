package antlr

import org.antlr.v4.runtime.ParserRuleContext

class EvalException : RuntimeException {
    constructor(msg: String) : super(msg)
    constructor(msg: String, ctx: ParserRuleContext) : this(msg + " line:" + ctx.start.line)
    constructor(ctx: ParserRuleContext) : this("Illegal expression: " + ctx.text, ctx)
}
