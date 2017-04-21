package evaluator

import ast.*
import types.*
import kotlin.Error

val TRUE = types.Boolean(true)
val FALSE = types.Boolean(false)
val NULL = types.Null()

fun Eval(node: Node): types.Object {
    when (node) {
        is Program -> return evalProgram(node)
        is ExpressionStatement -> return Eval(node.expression)
        is IntegerLiteral -> return types.Integer(node.value)
        is BooleanLiteral -> return nativeBoolToBoolean(node.value)
        is PrefixExpression -> {
            val rightVal = Eval(node.right)
            if (isError(rightVal)) {
                return rightVal
            }
            return evalPrefixExpression(node.operator, rightVal)
        }
        is InfixExpression -> {
            val leftVal = Eval(node.left)
            if (isError(leftVal)) {
                return leftVal
            }
            val rightVal = Eval(node.right)
            if (isError(rightVal)) {
                return rightVal
            }
            return evalInfixExpression(node.operator, leftVal, rightVal)
        }
        is BlockStatement -> return evalBlockStatement(node)
        is IfExpression -> return evalIfExpression(node)
        is ReturnStatement -> {
            val value = Eval(node.returnValue!!)
            if (isError(value)) {
                return value
            }
            return ReturnValue(value)
        }
        else -> return NULL
    }
}

fun isError(obj: types.Object): Boolean {
    if (obj != NULL) {
        return obj.type() == ERROR_OBJ
    }
    return false
}

fun evalBlockStatement(node: BlockStatement): Object {
    var result: types.Object = NULL

    for (statement in node.statements) {
        result = Eval(statement)

        if (result != NULL) {
            if (result.type() == RETURN_VALUE_OBJ || result.type() == ERROR_OBJ) {
                return result
            }
        }
    }

    return result
}

fun evalProgram(node: Program): Object {
    var result: types.Object = NULL

    for (statement in node.statements) {
        result = Eval(statement)

        when (result) {
            is ReturnValue -> return result.value
            is Error -> return result
        }

    }

    return result
}

fun evalIfExpression(node: IfExpression): Object {
    val condition = Eval(node.condition)

    if (isError(condition)) {
        return condition
    }

    if (isTruth(condition)) {
        return Eval(node.consequence)
    } else if (node.alternative != null) {
        return Eval(node.alternative)
    } else {
        return NULL
    }
}

fun isTruth(condition: Object): Boolean {
    when (condition) {
        NULL -> return false
        TRUE -> return true
        FALSE -> return false
        else -> return true
    }
}

fun evalInfixExpression(operator: String, left: Object, right: Object): Object {
    if (left.type() == types.INTEGER_OBJ && right.type() == types.INTEGER_OBJ) {
        return evalIntegerInfixExpression(operator, left, right)
    } else if (left.type() != right.type()) {
        return types.Error("type mismatch: ${left.type()} ${right.type()}")
    } else if (operator == "==") {
        return nativeBoolToBoolean(left == right)
    } else if (operator == "!=") {
        return nativeBoolToBoolean(left != right)
    } else {
        return types.Error("unknown operator: ${left.type()} $operator ${right.type()}")
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
        else -> return types.Error("unknown operator: ${left.type()} $operator ${right.type()}")
    }
}

fun evalPrefixExpression(operator: String, right: Object): Object {
    when (operator) {
        "!" -> return evalBangOperatorExpression(right)
        "-" -> return evalMinusPrefixOperatorExpression(right)
        else -> return types.Error("unknown operator: $operator ${right.type()}")
    }
}

fun evalMinusPrefixOperatorExpression(right: Object): Object {
    if (right.type() != INTEGER_OBJ) {
        return types.Error("unknown operator: -${right.type()}")
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

    statements.forEach {
        result = Eval(it)

        if (result is ReturnValue) {
            return (result as ReturnValue).value
        }
    }

    return result
}
