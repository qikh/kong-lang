package evaluator

import ast.*
import types.INTEGER_OBJ
import types.Integer
import types.Null
import types.Object

val TRUE = types.Boolean(true)
val FALSE = types.Boolean(false)
val NULL = types.Null()

fun Eval(node: Node): types.Object {
    when (node) {
        is Program -> return evalStatements(node.statements)
        is ExpressionStatement -> return Eval(node.expression)
        is IntegerLiteral -> return types.Integer(node.value)
        is BooleanLiteral -> return nativeBoolToBoolean(node.value)
        is PrefixExpression -> return evalPrefixExpression(node.operator, Eval(node.right))
        is InfixExpression -> return evalInfixExpression(node.operator, Eval(node.left), Eval(node.right))
        else -> return NULL
    }
}

fun evalInfixExpression(operator: String, left: Object, right: Object): Object {
    if (left.type() == types.INTEGER_OBJ && right.type() == types.INTEGER_OBJ) {
        return evalIntegerInfixExpression(operator, left, right)

    } else {
        return NULL
    }
}

fun evalIntegerInfixExpression(operator: String, left: Object, right: Object): Object {
    val leftVal = (left as Integer).value
    val rightVal = (right as Integer).value

    when (operator) {
        "+" -> return Integer(leftVal + rightVal)
        "-" -> return Integer(leftVal - rightVal)
        "*" -> return Integer(leftVal * rightVal)
        "/" -> return Integer(leftVal / rightVal)
        "<" -> return nativeBoolToBoolean(leftVal < rightVal)
        ">" -> return nativeBoolToBoolean(leftVal > rightVal)
        "==" -> return nativeBoolToBoolean(leftVal == rightVal)
        "!=" -> return nativeBoolToBoolean(leftVal != rightVal)
        else -> return NULL
    }
}

fun evalPrefixExpression(operator: String, right: Object): Object {
    when (operator) {
        "!" -> return evalBangOperatorExpression(right)
        "-" -> return evalMinusPrefixOperatorExpression(right)
        else -> return NULL
    }
}

fun evalMinusPrefixOperatorExpression(right: Object): Object {
    if (right.type() != INTEGER_OBJ) {
        return NULL
    }

    val value = (right as types.Integer).value
    return types.Integer(-value)
}

fun evalBangOperatorExpression(right: Object): Object {
    when (right) {
        TRUE -> return FALSE
        FALSE -> return TRUE
        NULL -> return TRUE
        else -> return FALSE
    }

}

fun nativeBoolToBoolean(value: Boolean): types.Boolean {
    if (value) {
        return TRUE
    } else {
        return FALSE
    }

}

fun evalStatements(statements: List<out Statement>): Object {
    var result: types.Object = Null()

    statements.forEach { result = Eval(it) }

    return result
}
