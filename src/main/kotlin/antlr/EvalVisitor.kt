package antlr

import antlr.KongParser.*
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.misc.NotNull
import java.util.*

class EvalVisitor(var scope: Scope?,
                  val functions: Map<String, Function>) : KongBaseVisitor<NodeValue>() {

    override fun visitFunctionDeclStatement(ctx: FunctionDeclStatementContext): NodeValue {
        return NodeValue.VOID
    }


    // '-' expression                           #unaryMinusExpression
    override fun visitUnaryMinusExpression(ctx: UnaryMinusExpressionContext): NodeValue {
        val v = this.visit(ctx.expression())
        if (!v.isNumber) {
            throw EvalException(ctx)
        }
        return NodeValue(-1 * v.asDouble()!!)
    }

    // '!' expression                           #notExpression
    override fun visitNotExpression(ctx: NotExpressionContext): NodeValue {
        val v = this.visit(ctx.expression())
        if (!v.isBoolean) {
            throw EvalException(ctx)
        }
        return NodeValue(v.asBoolean()?.not())
    }

    // expression '^' expression                #powerExpression
    override fun visitPowerExpression(ctx: PowerExpressionContext): NodeValue {
        val lhs = this.visit(ctx.expression(0))
        val rhs = this.visit(ctx.expression(1))
        if (lhs.isNumber && rhs.isNumber) {
            return NodeValue(Math.pow(lhs.asDouble()!!, rhs.asDouble()!!))
        }
        throw EvalException(ctx)
    }

    // expression '*' expression                #multiplyExpression
    override fun visitMultiplyExpression(ctx: MultiplyExpressionContext): NodeValue {
        val lhs = this.visit(ctx.expression(0))
        val rhs = this.visit(ctx.expression(1))
        if (lhs == null || rhs == null) {
            System.err.println("lhs $lhs rhs $rhs")
            throw EvalException(ctx)
        }

        // number * number
        if (lhs.isNumber && rhs.isNumber) {
            return NodeValue(lhs.asDouble()!! * rhs.asDouble()!!)
        }

        // string * number
        if (lhs.isString && rhs.isNumber) {
            val str = StringBuilder()
            val stop = rhs.asDouble()!!.toInt()
            for (i in 0..stop - 1) {
                str.append(lhs.asString())
            }
            return NodeValue(str.toString())
        }

        // list * number
        if (lhs.isList && rhs.isNumber) {
            val total = ArrayList<NodeValue>()
            val stop = rhs.asDouble()!!.toInt()
            for (i in 0..stop - 1) {
                total.addAll(lhs.asList())
            }
            return NodeValue(total)
        }
        throw EvalException(ctx)
    }

    // expression '/' expression                #divideExpression
    override fun visitDivideExpression(ctx: DivideExpressionContext): NodeValue {
        val lhs = this.visit(ctx.expression(0))
        val rhs = this.visit(ctx.expression(1))
        if (lhs.isNumber && rhs.isNumber) {
            return NodeValue(lhs.asDouble()!! / rhs.asDouble()!!)
        }
        throw EvalException(ctx)
    }

    // expression '%' expression                #modulusExpression
    override fun visitModulusExpression(ctx: ModulusExpressionContext): NodeValue {
        val lhs = this.visit(ctx.expression(0))
        val rhs = this.visit(ctx.expression(1))
        if (lhs.isNumber && rhs.isNumber) {
            return NodeValue(lhs.asDouble()!! % rhs.asDouble()!!)
        }
        throw EvalException(ctx)
    }

    // expression '+' expression                #addExpression
    override fun visitAddExpression(@NotNull ctx: AddExpressionContext): NodeValue {
        val lhs = this.visit(ctx.expression(0))
        val rhs = this.visit(ctx.expression(1))

        if (lhs == null || rhs == null) {
            throw EvalException(ctx)
        }

        // number + number
        if (lhs.isNumber && rhs.isNumber) {
            return NodeValue(lhs.asDouble()!! + rhs.asDouble()!!)
        }

        // list + any
        if (lhs.isList) {
            val list = lhs.asList()
            return NodeValue(list.plus(rhs))
        }

        // string + any
        if (lhs.isString) {
            return NodeValue(lhs.asString() + "" + rhs.toString())
        }

        // any + string
        if (rhs.isString) {
            return NodeValue(lhs.toString() + "" + rhs.asString())
        }

        return NodeValue(lhs.toString() + rhs.toString())
    }

    // expression '-' expression                #subtractExpression
    override fun visitSubtractExpression(ctx: SubtractExpressionContext): NodeValue {
        val lhs = this.visit(ctx.expression(0))
        val rhs = this.visit(ctx.expression(1))
        if (lhs.isNumber && rhs.isNumber) {
            return NodeValue(lhs.asDouble()!! - rhs.asDouble()!!)
        }
        if (lhs.isList) {
            val list = lhs.asList()
            return NodeValue(list.minus(rhs))
        }
        throw EvalException(ctx)
    }

    // expression '>=' expression               #gtEqExpression
    override fun visitGtEqExpression(ctx: GtEqExpressionContext): NodeValue {
        val lhs = this.visit(ctx.expression(0))
        val rhs = this.visit(ctx.expression(1))
        if (lhs.isNumber && rhs.isNumber) {
            return NodeValue(lhs.asDouble() >= rhs.asDouble())
        }
        if (lhs.isString && rhs.isString) {
            return NodeValue(lhs.asString().compareTo(rhs.asString()) >= 0)
        }
        throw EvalException(ctx)
    }

    // expression '<=' expression               #ltEqExpression
    override fun visitLtEqExpression(ctx: LtEqExpressionContext): NodeValue {
        val lhs = this.visit(ctx.expression(0))
        val rhs = this.visit(ctx.expression(1))
        if (lhs.isNumber && rhs.isNumber) {
            return NodeValue(lhs.asDouble() <= rhs.asDouble())
        }
        if (lhs.isString && rhs.isString) {
            return NodeValue(lhs.asString().compareTo(rhs.asString()) <= 0)
        }
        throw EvalException(ctx)
    }

    // expression '>' expression                #gtExpression
    override fun visitGtExpression(ctx: GtExpressionContext): NodeValue {
        val lhs = this.visit(ctx.expression(0))
        val rhs = this.visit(ctx.expression(1))
        if (lhs.isNumber && rhs.isNumber) {
            return NodeValue(lhs.asDouble() > rhs.asDouble())
        }
        if (lhs.isString && rhs.isString) {
            return NodeValue(lhs.asString().compareTo(rhs.asString()) > 0)
        }
        throw EvalException(ctx)
    }

    // expression '<' expression                #ltExpression
    override fun visitLtExpression(ctx: LtExpressionContext): NodeValue {
        val lhs = this.visit(ctx.expression(0))
        val rhs = this.visit(ctx.expression(1))
        if (lhs.isNumber && rhs.isNumber) {
            return NodeValue(lhs.asDouble() < rhs.asDouble())
        }
        if (lhs.isString && rhs.isString) {
            return NodeValue(lhs.asString().compareTo(rhs.asString()) < 0)
        }
        throw EvalException(ctx)
    }

    // expression '==' expression               #eqExpression
    override fun visitEqExpression(@NotNull ctx: EqExpressionContext): NodeValue {
        val lhs = this.visit(ctx.expression(0))
        val rhs = this.visit(ctx.expression(1))
        if (lhs == null) {
            throw EvalException(ctx)
        }
        return NodeValue(lhs == rhs)
    }

    // expression '!=' expression               #notEqExpression
    override fun visitNotEqExpression(@NotNull ctx: NotEqExpressionContext): NodeValue {
        val lhs = this.visit(ctx.expression(0))
        val rhs = this.visit(ctx.expression(1))
        return NodeValue(lhs != rhs)
    }

    // expression '&&' expression               #andExpression
    override fun visitAndExpression(ctx: AndExpressionContext): NodeValue {
        val lhs = this.visit(ctx.expression(0))
        val rhs = this.visit(ctx.expression(1))

        if (!lhs.isBoolean || !rhs.isBoolean) {
            throw EvalException(ctx)
        }
        return NodeValue(lhs.asBoolean()!! && rhs.asBoolean()!!)
    }

    // expression '||' expression               #orExpression
    override fun visitOrExpression(ctx: OrExpressionContext): NodeValue {
        val lhs = this.visit(ctx.expression(0))
        val rhs = this.visit(ctx.expression(1))

        if (!lhs.isBoolean || !rhs.isBoolean) {
            throw EvalException(ctx)
        }
        return NodeValue(lhs.asBoolean()!! || rhs.asBoolean()!!)
    }

    // expression '?' expression ':' expression #ternaryExpression
    override fun visitTernaryExpression(ctx: TernaryExpressionContext): NodeValue {
        val condition = this.visit(ctx.expression(0))
        if (condition.asBoolean()!!) {
            return NodeValue(this.visit(ctx.expression(1)))
        } else {
            return NodeValue(this.visit(ctx.expression(2)))
        }
    }


    // Number                                   #numberExpression
    override fun visitIntExpression(ctx: IntExpressionContext): NodeValue {
        return NodeValue(Integer.valueOf(ctx.text))
    }

    // Number                                   #numberExpression
    override fun visitFloatExpression(ctx: FloatExpressionContext): NodeValue {
        return NodeValue(java.lang.Double.valueOf(ctx.text))
    }

    // Bool                                     #boolExpression
    override fun visitBoolExpression(ctx: BoolExpressionContext): NodeValue {
        return NodeValue(java.lang.Boolean.valueOf(ctx.text))
    }

    // Null                                     #nullExpression
    override fun visitNullExpression(ctx: NullExpressionContext): NodeValue {
        return NodeValue.NULL
    }

    private fun resolveIndexes(ctx: ParserRuleContext, nodeValue: NodeValue, indexes: List<ExpressionContext>): NodeValue {
        var value = nodeValue
        for (ec in indexes) {
            val idx = this.visit(ec)
            if (!idx.isNumber || !value.isList && !value.isString) {
                throw EvalException("Problem resolving indexes on $value at $idx", ec)
            }
            val i = idx.asInt()!!
            if (value.isString) {
                value = NodeValue(value.asString().substring(i, i + 1))
            } else {
                value = value.asList()[i]
            }
        }
        return value
    }

    private fun setAtIndex(ctx: ParserRuleContext, indexes: List<ExpressionContext>, nodeValue: NodeValue,
                           newVal: NodeValue) {
        var value = nodeValue
        if (!value.isList) {
            throw EvalException(ctx)
        }
        // TODO some more list size checking in here
        for (i in 0..indexes.size - 1 - 1) {
            val idx = this.visit(indexes[i])
            if (!idx.isNumber) {
                throw EvalException(ctx)
            }
            value = value.asList()[idx.asInt()]
        }
        val idx = this.visit(indexes[indexes.size - 1])
        if (!idx.isNumber) {
            throw EvalException(ctx)
        }
        value.asList()[idx.asInt()] = newVal
    }

    // functionCall indexes?                    #functionCallExpression
    override fun visitFunctionCallExpression(ctx: FunctionCallExpressionContext): NodeValue {
        val key = this.visit(ctx.functionCall())
        return key
    }


    // Identifier indexes?                      #identifierExpression
    override fun visitIdentifierExpression(ctx: IdentifierExpressionContext): NodeValue {
        val id = ctx.ID().text
        var key = scope?.resolve(id)

        if (ctx.indexes() != null && key != null) {
            val exps = ctx.indexes().expression()
            key = resolveIndexes(ctx, key, exps)
        }
        return key ?: NodeValue.VOID
    }

    // String indexes?                          #stringExpression
    override fun visitStringExpression(ctx: StringExpressionContext): NodeValue {
        var text = ctx.text
        text = text.substring(1, text.length - 1).replace("\\\\(.)".toRegex(), "$1")
        var key = NodeValue(text)
        if (ctx.indexes() != null) {
            val exps = ctx.indexes().expression()
            key = resolveIndexes(ctx, key, exps)
        }
        return key
    }

    // '(' expression ')' indexes?              #expressionExpression
    override fun visitExpressionExpression(ctx: ExpressionExpressionContext): NodeValue {
        var value = this.visit(ctx.expression())
        if (ctx.indexes() != null) {
            val exps = ctx.indexes().expression()
            value = resolveIndexes(ctx, value, exps)
        }
        return value
    }

    // assignment
    // : Identifier indexes? '=' expression
    // ;
    override fun visitAssignmentStatement(ctx: AssignmentStatementContext): NodeValue {
        val newVal = this.visit(ctx.expression())
        if (ctx.indexes() != null) {
            val value = scope?.resolve(ctx.ID().text)
            val exps = ctx.indexes().expression()
            if (value != null) {
                setAtIndex(ctx, exps, value, newVal)
            }
        } else {
            val id = ctx.ID().text
            scope?.assign(id, newVal)
        }
        return NodeValue.VOID
    }

    // Identifier '(' exprList? ')' #identifierFunctionCall
    override fun visitIdentifierFunctionCall(ctx: IdentifierFunctionCallContext): NodeValue {
        val params = if (ctx.expressionList() != null) ctx.expressionList().expression() else ArrayList<ExpressionContext>()
        val id = ctx.ID().text + params.size
        val function = functions[id]
        if (function != null) {
            return function.invoke(params, functions, scope)
        }
        throw EvalException(ctx)
    }

    // Println '(' expression? ')'  #printlnFunctionCall
    override fun visitPrintlnFunctionCall(ctx: PrintlnFunctionCallContext): NodeValue {
        println(this.visit(ctx.expression()))
        return NodeValue.VOID
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
    override fun visitIfStatement(ctx: IfStatementContext): NodeValue {

        // if ...
        if (this.visit(ctx.ifStat().expression()).asBoolean()!!) {
            return this.visit(ctx.ifStat().block())
        }

        // else if ...
        for (i in 0..ctx.elseIfStat().size - 1) {
            if (this.visit(ctx.elseIfStat(i).expression()).asBoolean()!!) {
                return this.visit(ctx.elseIfStat(i).block())
            }
        }

        // else ...
        if (ctx.elseStat() != null) {
            return this.visit(ctx.elseStat().block())
        }

        return NodeValue.VOID
    }

    // block
    // : (statement | functionDecl)* (Return expression)?
    // ;
    override fun visitStatementList(ctx: StatementListContext): NodeValue {

        scope = Scope(scope) // create new local scope

        var lastResult: NodeValue? = null

        for (sx in ctx.statement()) {
            lastResult = this.visit(sx) ?: lastResult
        }

        scope = scope?.parent()

        if (lastResult != null && !lastResult?.isVoid) {
            returnValue.value = lastResult
            scope = scope?.parent()
            throw returnValue
        }
        return NodeValue.VOID
    }

    // forStatement
    // : For Identifier '=' expression To expression OBrace block CBrace
    // ;
    override fun visitForStatement(ctx: ForStatementContext): NodeValue {
        val start = this.visit(ctx.INT(0)).asInt()!!
        val stop = this.visit(ctx.INT(1)).asInt()!!
        for (i in start..stop) {
            scope?.assign(ctx.ID().text, NodeValue(i))
            val returnValue = this.visit(ctx.block())
            if (returnValue !== NodeValue.VOID) {
                return returnValue
            }
        }
        return NodeValue.VOID
    }

    override fun visitReturnStatement(ctx: ReturnStatementContext): NodeValue {

        val ex = ctx.expression()
        if (ex != null) {
            val value = this.visit(ex)

            returnValue.value = value
            scope = scope?.parent()
            throw returnValue
        }
        return NodeValue.VOID
    }

    companion object {

        val returnValue = ReturnValue()
    }
}
